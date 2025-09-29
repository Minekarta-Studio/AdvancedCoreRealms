package com.minekarta.advancedcorerealms.listeners;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.manager.RealmManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class InventoryListener implements Listener {

    private final AdvancedCoreRealms plugin;
    private final RealmManager realmManager;

    public InventoryListener(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.realmManager = plugin.getRealmManager();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (event.getView().getTitle().startsWith("AdvancedCoreRealms") ||
            event.getView().getTitle().startsWith("Realms |")) {
            event.setCancelled(true);
            return;
        }

        String currentWorldName = player.getWorld().getName();
        Optional<Realm> currentRealmOpt = realmManager.getRealmFromCacheByWorld(currentWorldName);
        if (currentRealmOpt.isEmpty()) {
            return; // Not in a realm, no restrictions
        }

        // The logic to save inventory on every click is very intensive.
        // This should be handled by the RealmInventoryService on world change or logout.
        // I am removing this call to prevent performance issues.
        // if (isInRealm(player)) {
        //     ItemStack[] inventory = player.getInventory().getContents();
        //     plugin.getPlayerDataManager().savePlayerInventory(player.getUniqueId(), currentWorldName, inventory);
        // }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        String currentWorldName = player.getWorld().getName();
        Optional<Realm> currentRealmOpt = realmManager.getRealmFromCacheByWorld(currentWorldName);

        if (currentRealmOpt.isEmpty()) {
            return; // Not in a realm, no restrictions
        }
        
        Realm currentRealm = currentRealmOpt.get();
        ItemStack droppedItem = event.getItemDrop().getItemStack();

        // This check is flawed because it doesn't know the destination.
        // A better approach would be to handle this on teleport.
        // However, to maintain existing functionality for now:
        if (!currentRealm.isItemTransferable(droppedItem.getType().name())) {
            // A simple check: if you are in a realm, you can't drop non-transferable items.
            // This prevents dropping them for another player to pick up and carry out.
            event.setCancelled(true);
            plugin.getLanguageManager().sendMessage(player, "error.item_not_transferable");
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (event.getView().getTitle().startsWith("AdvancedCoreRealms") ||
            event.getView().getTitle().startsWith("Realms |")) {
            event.setCancelled(true);
            return;
        }
    }

    private boolean isInRealm(Player player) {
        String worldName = player.getWorld().getName();
        return realmManager.getRealmFromCacheByWorld(worldName).isPresent();
    }
}