package com.minekarta.advancedcorerealms.listeners;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.manager.world.WorldManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryListener implements Listener {
    
    private final AdvancedCoreRealms plugin;
    private final WorldManager worldManager;
    
    public InventoryListener(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.worldManager = plugin.getWorldManager();
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        String currentWorldName = player.getWorld().getName();
        
        // Check if player is in a realm
        Realm currentRealm = plugin.getWorldDataManager().getRealm(currentWorldName);
        if (currentRealm == null) {
            return; // Not in a realm, no restrictions
        }
        
        // Save inventory when items are moved within realm
        if (isInRealm(player)) {
            ItemStack[] inventory = player.getInventory().getContents();
            plugin.getPlayerDataManager().savePlayerInventory(player.getUniqueId(), currentWorldName, inventory);
        }
    }
    
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        String currentWorldName = player.getWorld().getName();
        
        // Check if player is in a realm
        Realm currentRealm = plugin.getWorldDataManager().getRealm(currentWorldName);
        if (currentRealm == null) {
            return; // Not in a realm, no restrictions
        }
        
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        
        // If item is not in the transferable list, prevent dropping it outside the realm
        if (!currentRealm.isItemTransferable(droppedItem.getType().name())) {
            // If player is trying to leave the realm with a non-transferable item
            if (!isSameRealmWorld(player.getWorld().getName(), currentWorldName)) {
                event.setCancelled(true);
                player.sendMessage("You cannot drop this item outside of the realm!");
            }
        }
    }
    
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        String currentWorldName = player.getWorld().getName();
        
        // Check if player is in a realm
        Realm currentRealm = plugin.getWorldDataManager().getRealm(currentWorldName);
        if (currentRealm == null) {
            return; // Not in a realm, no restrictions
        }
        
        // Check if any non-transferable items are being dragged to armor slots (which might be considered 'leaving' the realm)
        for (int slot : event.getRawSlots()) {
            if (slot < 9) { // Hotbar slots - where items might be moved when leaving
                ItemStack currentItem = event.getOldCursor();
                if (currentItem != null && !currentRealm.isItemTransferable(currentItem.getType().name())) {
                    event.setCancelled(true);
                    player.sendMessage("You cannot take this item outside of the realm!");
                    return;
                }
            }
        }
    }
    
    private boolean isInRealm(Player player) {
        String worldName = player.getWorld().getName();
        return plugin.getWorldDataManager().getRealm(worldName) != null;
    }
    
    private boolean isSameRealmWorld(String world1, String world2) {
        return world1.equals(world2);
    }
}