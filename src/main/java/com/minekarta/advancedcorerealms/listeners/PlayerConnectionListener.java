package com.minekarta.advancedcorerealms.listeners;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.api.AdvancedCorePlayer;
import com.minekarta.advancedcorerealms.realm.RealmInventoryService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {
    
    private final AdvancedCoreRealms plugin;
    private final RealmInventoryService realmInventoryService;
    
    public PlayerConnectionListener(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.realmInventoryService = plugin.getRealmInventoryService();
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Initialize player's location for inventory service
        realmInventoryService.handlePlayerJoin(event.getPlayer());

        // Initialize the player's AdvancedCorePlayer instance
        AdvancedCorePlayer advancedCorePlayer = plugin.getAdvancedCorePlayer(event.getPlayer());
        
        // Update the world border based on their current location
        // Delay the update to ensure the player is fully loaded
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            advancedCorePlayer.updateWorldBorder();
        }, 20L); // 1 second delay
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Handle inventory saving on disconnect
        realmInventoryService.handlePlayerDisconnect(event.getPlayer());

        // Save any pending data when player leaves
        plugin.getWorldDataManager().saveData();
        
        // Clear the player's state in the state manager
        plugin.getPlayerStateManager().clearState(event.getPlayer());
        
        // Remove the player from the AdvancedCorePlayer cache
        plugin.removeAdvancedCorePlayer(event.getPlayer());
    }
}