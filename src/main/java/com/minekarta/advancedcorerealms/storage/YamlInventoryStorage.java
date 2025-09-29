package com.minekarta.advancedcorerealms.storage;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.SerializedInventory;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class YamlInventoryStorage implements InventoryStorage {

    private final AdvancedCoreRealms plugin;
    private final File inventoriesDir;

    public YamlInventoryStorage(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.inventoriesDir = new File(plugin.getDataFolder(), "inventories");
        if (!inventoriesDir.exists()) {
            inventoriesDir.mkdirs();
        }
    }

    private File getPlayerFile(UUID realmId, UUID playerUuid) {
        File realmDir = new File(inventoriesDir, realmId.toString());
        if (!realmDir.exists()) {
            realmDir.mkdirs();
        }
        return new File(realmDir, playerUuid.toString() + ".yml");
    }

    @Override
    public CompletableFuture<Void> savePlayerInventory(UUID realmId, UUID playerUuid, SerializedInventory data) {
        return CompletableFuture.runAsync(() -> {
            File playerFile = getPlayerFile(realmId, playerUuid);
            FileConfiguration config = new YamlConfiguration();
            config.set("mainContents", data.getMainContents());
            config.set("armorContents", data.getArmorContents());
            config.set("offhandContents", data.getOffhandContents());
            config.set("enderChestContents", data.getEnderChestContents());
            config.set("savedAt", data.getSavedAt().toString());
            config.set("sourceRealmId", data.getSourceRealmId() != null ? data.getSourceRealmId().toString() : null);
            try {
                config.save(playerFile);
            } catch (IOException e) {
                plugin.getLogger().log(java.util.logging.Level.SEVERE, "Could not save inventory for player " + playerUuid + " in realm " + realmId, e);
            }
        });
    }

    @Override
    public CompletableFuture<Optional<SerializedInventory>> loadPlayerInventory(UUID realmId, UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            File playerFile = getPlayerFile(realmId, playerUuid);
            if (!playerFile.exists()) {
                return Optional.empty();
            }
            FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
            String mainContents = config.getString("mainContents");
            String armorContents = config.getString("armorContents");
            String offhandContents = config.getString("offhandContents");
            String enderChestContents = config.getString("enderChestContents");
            String sourceRealmIdStr = config.getString("sourceRealmId");
            UUID sourceRealmId = sourceRealmIdStr != null ? UUID.fromString(sourceRealmIdStr) : null;

            SerializedInventory inventory = new SerializedInventory(mainContents, armorContents, offhandContents, enderChestContents, sourceRealmId);
            return Optional.of(inventory);
        });
    }

    @Override
    public CompletableFuture<Void> deletePlayerInventory(UUID realmId, UUID playerUuid) {
        return CompletableFuture.runAsync(() -> {
            File playerFile = getPlayerFile(realmId, playerUuid);
            if (playerFile.exists()) {
                playerFile.delete();
            }
        });
    }
}