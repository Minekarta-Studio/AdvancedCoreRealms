package com.minekarta.advancedcorerealms.listeners;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.WorldDataManager;
import com.minekarta.advancedcorerealms.data.object.Realm;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.PlayerInventory;

public class PlayerWorldListener implements Listener {
    
    private final AdvancedCoreRealms plugin;
    private final WorldDataManager worldDataManager;
    
    public PlayerWorldListener(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.worldDataManager = plugin.getWorldDataManager();
    }
    
    @EventHandler
    public void onPlayerWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World fromWorld = event.getFrom();
        World toWorld = player.getWorld(); // Current world after the change
        
        boolean fromIsRealm = isRealmWorld(fromWorld.getName());
        boolean toIsRealm = isRealmWorld(toWorld.getName());
        
        // If player is switching between realms or between a realm and a normal world
        if (fromIsRealm || toIsRealm) {
            if (plugin.getConfig().getBoolean("separate-inventories", true)) {
                // Get the player's current inventory to save
                PlayerInventory currentInventory = player.getInventory();
                org.bukkit.inventory.ItemStack[] contents = currentInventory.getContents();
                
                // Save inventory for the world they're leaving
                if (fromIsRealm) {
                    plugin.getPlayerDataManager().savePlayerInventory(player.getUniqueId(), fromWorld.getName(), contents);
                }
                
                // If entering a realm or leaving a realm, load appropriate inventory
                if (toIsRealm) {
                    // Load realm-specific inventory
                    org.bukkit.inventory.ItemStack[] realmInventory = plugin.getPlayerDataManager().loadPlayerInventory(player.getUniqueId(), toWorld.getName());
                    player.getInventory().setContents(realmInventory);
                } else if (fromIsRealm) {
                    // Going from realm to normal world, load world-specific inventory
                    // In this case, we would typically load the main world inventory if we were tracking it separately
                    // For now, we'll just clear and assume they had an inventory in the main world
                    org.bukkit.inventory.ItemStack[] mainWorldInventory = plugin.getPlayerDataManager().loadPlayerInventory(player.getUniqueId(), toWorld.getName());
                    if (mainWorldInventory != null) {
                        player.getInventory().setContents(mainWorldInventory);
                    }
                    // Otherwise, retain current inventory
                }
            }
        }
    }
    
    private boolean isRealmWorld(String worldName) {
        return worldDataManager.getRealm(worldName) != null;
    }
}