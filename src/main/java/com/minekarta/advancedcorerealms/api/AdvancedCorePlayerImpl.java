package com.minekarta.advancedcorerealms.api;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.nms.NMSWorldBorder;
import com.minekarta.advancedcorerealms.worldborder.BorderColor;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Implementation of the AdvancedCorePlayer interface
 */
public class AdvancedCorePlayerImpl implements AdvancedCorePlayer {
    
    private final AdvancedCoreRealms plugin;
    private final Player player;
    private final UUID uuid;
    private final NMSWorldBorder nmsWorldBorder;
    
    public AdvancedCorePlayerImpl(AdvancedCoreRealms plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.uuid = player.getUniqueId();
        this.nmsWorldBorder = NMSWorldBorder.getImplementation(plugin);
    }
    
    @Override
    public Player asPlayer() {
        return player;
    }
    
    @Override
    public UUID getUniqueId() {
        return uuid;
    }
    
    @Override
    public boolean hasWorldBorderEnabled() {
        return plugin.getPlayerDataManager().hasWorldBorderEnabled(uuid);
    }
    
    @Override
    public void toggleWorldBorder() {
        boolean currentState = hasWorldBorderEnabled();
        plugin.getPlayerDataManager().setWorldBorderEnabled(uuid, !currentState);
        
        // If the new state is enabled, update the border
        if (!currentState) {
            updateWorldBorder();
        } else {
            removeWorldBorder();
        }
    }
    
    @Override
    public BorderColor getBorderColor() {
        return plugin.getPlayerDataManager().getBorderColor(uuid);
    }
    
    @Override
    public void setBorderColor(BorderColor color) {
        plugin.getPlayerDataManager().setBorderColor(uuid, color);
        
        // Update the border with the new color
        updateWorldBorder();
    }
    
    @Override
    public void updateWorldBorder(Realm realm) {
        if (realm != null && hasWorldBorderEnabled()) {
            // Only update if world borders are enabled globally
            if (plugin.getConfig().getBoolean("world-borders", true)) {
                // Use NMS to send world border packet
                sendWorldBorderPacket(realm);
            }
        } else {
            removeWorldBorder();
        }
    }
    
    @Override
    public void removeWorldBorder() {
        // Remove the world border for the player
        nmsWorldBorder.removeWorldBorder(player);
    }
    
    @Override
    public void updateWorldBorder() {
        // Get the current realm the player is in
        Realm currentRealm = getCurrentRealm();
        updateWorldBorder(currentRealm);
    }
    
    private void sendWorldBorderPacket(Realm realm) {
        org.bukkit.World bukkitWorld = realm.getBukkitWorld();
        if (bukkitWorld != null && player.getWorld().equals(bukkitWorld)) {
            // Get center coordinates from realm (these should be updated when world is loaded)
            double centerX = realm.getCenterX();
            double centerZ = realm.getCenterZ();
            
            // Get size from realm configuration
            double size = realm.getBorderSize(); // This would come from realm configuration
            
            // Update center if not set (first time loading)
            if (centerX == 0.0 && centerZ == 0.0) {
                org.bukkit.Location spawnLocation = bukkitWorld.getSpawnLocation();
                centerX = spawnLocation.getX();
                centerZ = spawnLocation.getZ();
                realm.setCenterX(centerX);
                realm.setCenterZ(centerZ);
            }
            
            // Get player's border color
            BorderColor color = getBorderColor();
            
            // Send the world border packet using NMS
            nmsWorldBorder.sendWorldBorder(player, bukkitWorld, centerX, centerZ, size, color);
        }
    }
    
    private Realm getCurrentRealm() {
        // Check if player is in a realm world
        String worldName = player.getWorld().getName();
        return plugin.getWorldDataManager().getRealm(worldName);
    }
}