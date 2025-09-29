package com.minekarta.advancedcorerealms.realm;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.data.object.SerializedInventory;
import com.minekarta.advancedcorerealms.storage.InventoryStorage;
import com.minekarta.advancedcorerealms.utils.InventorySerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class RealmInventoryService {

    private final AdvancedCoreRealms plugin;
    private final InventoryStorage inventoryStorage;

    private final Map<UUID, SerializedInventory> preSwapCache = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> playerLocationCache = new ConcurrentHashMap<>();
    private final Map<UUID, CompletableFuture<Void>> ongoingOperations = new ConcurrentHashMap<>();

    public static final UUID GLOBAL_INVENTORY_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public RealmInventoryService(AdvancedCoreRealms plugin, InventoryStorage inventoryStorage) {
        this.plugin = plugin;
        this.inventoryStorage = inventoryStorage;
    }

    public void enterRealm(Player player, Realm realm) {
        UUID playerUUID = player.getUniqueId();
        UUID realmUUID = UUID.fromString(realm.getWorldName()); // Assuming world name is the UUID for now

        // Prevent simultaneous operations
        if (isOperationOngoing(playerUUID)) return;

        UUID sourceRealmId = playerLocationCache.getOrDefault(playerUUID, GLOBAL_INVENTORY_ID);
        if (sourceRealmId.equals(realmUUID)) return; // Already in this realm's inventory space

        CompletableFuture<Void> operation = CompletableFuture.runAsync(() -> {
            // 1. Serialize and cache current inventory
            SerializedInventory currentInventory = serializeFromPlayer(player, sourceRealmId);
            preSwapCache.put(playerUUID, currentInventory);

            // 2. Save the cached inventory to its source location
            inventoryStorage.savePlayerInventory(sourceRealmId, playerUUID, currentInventory);

            // 3. Load realm inventory and apply it
            inventoryStorage.loadPlayerInventory(realmUUID, playerUUID).thenAccept(optionalInventory -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    clearPlayerInventory(player);
                    optionalInventory.ifPresent(inv -> applySerializedInventory(player, inv));
                    playerLocationCache.put(playerUUID, realmUUID);
                    preSwapCache.remove(playerUUID); // Swap complete
                });
            }).exceptionally(ex -> {
                plugin.getLogger().log(java.util.logging.Level.SEVERE, "Failed to load inventory for player " + playerUUID + " in realm " + realmUUID, ex);
                // Restore original inventory on failure
                Bukkit.getScheduler().runTask(plugin, () -> applySerializedInventory(player, currentInventory));
                preSwapCache.remove(playerUUID);
                return null;
            });
        }).whenComplete((res, err) -> ongoingOperations.remove(playerUUID));

        ongoingOperations.put(playerUUID, operation);
    }

    public void exitRealm(Player player, Realm realm) {
        // Exiting a realm is treated as entering the "global" world
        UUID playerUUID = player.getUniqueId();
        UUID realmUUID = UUID.fromString(realm.getWorldName());

        if (isOperationOngoing(playerUUID)) return;

        UUID currentRealmId = playerLocationCache.getOrDefault(playerUUID, null);
        if (currentRealmId == null || !currentRealmId.equals(realmUUID)) {
            // Player is not registered in this realm, so no inventory to save.
            // This can happen if they were never properly teleported in.
            // We'll still try to load their global inventory.
        }

        CompletableFuture<Void> operation = CompletableFuture.runAsync(() -> {
            // 1. Serialize and save current (realm) inventory
            SerializedInventory realmInventory = serializeFromPlayer(player, realmUUID);
            inventoryStorage.savePlayerInventory(realmUUID, playerUUID, realmInventory);

            // 2. Load global inventory and apply it
            inventoryStorage.loadPlayerInventory(GLOBAL_INVENTORY_ID, playerUUID).thenAccept(optionalInventory -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    clearPlayerInventory(player);
                    // If global inventory exists, apply it. Otherwise, they get a fresh inventory.
                    optionalInventory.ifPresent(inv -> applySerializedInventory(player, inv));
                    playerLocationCache.put(playerUUID, GLOBAL_INVENTORY_ID);
                });
            }).exceptionally(ex -> {
                plugin.getLogger().log(java.util.logging.Level.SEVERE, "Failed to load global inventory for player " + playerUUID, ex);
                // As a fallback, do not clear their realm inventory to prevent data loss.
                return null;
            });
        }).whenComplete((res, err) -> ongoingOperations.remove(playerUUID));

        ongoingOperations.put(playerUUID, operation);
    }

    public void handlePlayerDisconnect(Player player) {
        UUID playerUUID = player.getUniqueId();

        // If a player disconnects mid-swap, their pre-swap inventory is in the cache.
        // We must ensure this is saved back to their source location.
        if (preSwapCache.containsKey(playerUUID)) {
            SerializedInventory preSwapInv = preSwapCache.get(playerUUID);
            inventoryStorage.savePlayerInventory(preSwapInv.getSourceRealmId(), playerUUID, preSwapInv)
                .thenRun(() -> plugin.getLogger().info("Successfully saved pre-swap inventory for disconnected player " + playerUUID))
                .exceptionally(ex -> {
                    plugin.getLogger().severe("FAILED to save pre-swap inventory for disconnected player " + playerUUID);
                    return null;
                });
            preSwapCache.remove(playerUUID);
        } else {
            // Standard disconnect, save their current inventory to their current location
            UUID currentRealmId = playerLocationCache.getOrDefault(playerUUID, GLOBAL_INVENTORY_ID);
            SerializedInventory currentInventory = serializeFromPlayer(player, currentRealmId);
            inventoryStorage.savePlayerInventory(currentRealmId, playerUUID, currentInventory)
                .thenRun(() -> plugin.getLogger().info("Saved inventory for disconnected player " + playerUUID + " in location " + currentRealmId))
                .exceptionally(ex -> {
                     plugin.getLogger().severe("FAILED to save inventory for disconnected player " + playerUUID);
                     return null;
                });
        }
        playerLocationCache.remove(playerUUID); // Clear location on disconnect
    }

    public void handlePlayerJoin(Player player) {
        // On join, they are in the global world by default.
        playerLocationCache.put(player.getUniqueId(), GLOBAL_INVENTORY_ID);
    }

    private SerializedInventory serializeFromPlayer(Player player, UUID sourceRealmId) {
        PlayerInventory inv = player.getInventory();
        try {
            String main = InventorySerializer.toBase64(inv.getContents());
            String armor = InventorySerializer.toBase64(inv.getArmorContents());
            String offhand = InventorySerializer.toBase64(new ItemStack[]{inv.getItemInOffHand()});
            String enderChest = InventorySerializer.toBase64(player.getEnderChest().getContents());
            return new SerializedInventory(main, armor, offhand, enderChest, sourceRealmId);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize inventory for player " + player.getUniqueId(), e);
        }
    }

    private void applySerializedInventory(Player player, SerializedInventory sInv) {
        try {
            ItemStack[] main = InventorySerializer.fromBase64(sInv.getMainContents());
            ItemStack[] armor = InventorySerializer.fromBase64(sInv.getArmorContents());
            ItemStack[] offhand = InventorySerializer.fromBase64(sInv.getOffhandContents());
            ItemStack[] enderChest = InventorySerializer.fromBase64(sInv.getEnderChestContents());

            player.getInventory().setContents(main);
            player.getInventory().setArmorContents(armor);
            player.getInventory().setItemInOffHand(offhand.length > 0 ? offhand[0] : null);
            player.getEnderChest().setContents(enderChest);
        } catch (IOException | ClassNotFoundException e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Failed to deserialize and apply inventory for player " + player.getUniqueId(), e);
        }
    }

    private void clearPlayerInventory(Player player) {
        PlayerInventory inv = player.getInventory();
        inv.clear();
        inv.setArmorContents(new ItemStack[4]);
        inv.setItemInOffHand(null);
        player.getEnderChest().clear();
    }

    private boolean isOperationOngoing(UUID playerUUID) {
        CompletableFuture<Void> existingOp = ongoingOperations.get(playerUUID);
        return existingOp != null && !existingOp.isDone();
    }
}