package com.minekarta.advancedcorerealms.listeners;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.api.AdvancedCorePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {
    
    private final AdvancedCoreRealms plugin;
    
    public PlayerConnectionListener(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Load player's realm data when they join
        // This is handled by the data manager, but we might want to do additional initialization here
        
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
        // Save any pending data when player leaves
        plugin.getWorldDataManager().saveData();
        
        // Remove the player from the AdvancedCorePlayer cache
        plugin.removeAdvancedCorePlayer(event.getPlayer());
    }
}