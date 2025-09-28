package com.minekarta.advancedcorerealms.nms;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.worldborder.BorderColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Interface for NMS world border operations
 */
public interface NMSWorldBorder {
    
    /**
     * Send a world border packet to the player
     */
    void sendWorldBorder(Player player, World world, double centerX, double centerZ, double size, BorderColor color);
    
    /**
     * Remove/reset the world border for the player
     */
    void removeWorldBorder(Player player);
    
    /**
     * Send a world border packet with animation to the player
     */
    void sendAnimatedWorldBorder(Player player, World world, double centerX, double centerZ, 
                                double oldSize, double newSize, double duration, BorderColor color);
    
    /**
     * Get the NMS world border implementation for the current server version
     */
    static NMSWorldBorder getImplementation(AdvancedCoreRealms plugin) {
        // For simplicity, we'll use the Bukkit API implementation since modern versions support it
        // In a full implementation, this would check the version and return appropriate NMS implementation
        return new BukkitWorldBorderWrapper(plugin);
    }
}