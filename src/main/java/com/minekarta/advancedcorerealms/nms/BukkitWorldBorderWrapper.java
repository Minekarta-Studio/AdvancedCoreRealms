package com.minekarta.advancedcorerealms.nms;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.worldborder.BorderColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Bukkit implementation of NMSWorldBorder for modern Minecraft versions
 */
public class BukkitWorldBorderWrapper implements NMSWorldBorder {
    
    private final AdvancedCoreRealms plugin;
    
    public BukkitWorldBorderWrapper(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void sendWorldBorder(Player player, World world, double centerX, double centerZ, double size, BorderColor color) {
        org.bukkit.WorldBorder worldBorder = player.getWorldBorder();

        // If the player doesn't have a custom world border, create a new one for them
        if (worldBorder == null) {
            // Create a new WorldBorder instance. We can't just do "new WorldBorder"
            // so we get it from the world and then set it for the player.
            // This gives the player their own border instance to be modified.
            worldBorder = world.getWorldBorder();
            player.setWorldBorder(worldBorder);
            // We need to re-fetch it after setting it to ensure we have the player's instance
            worldBorder = player.getWorldBorder();
        }
        
        // Now that we're sure worldBorder is not null, we can apply changes.
        if (worldBorder != null) {
            // Set center
            worldBorder.setCenter(centerX, centerZ);

            // Set size
            worldBorder.setSize(size);

            // Set warning distance based on color (different visual effects per color)
            switch (color) {
                case BLUE:
                    worldBorder.setWarningDistance(5);
                    worldBorder.setWarningTime(15);
                    break;
                case GREEN:
                    worldBorder.setWarningDistance(3);
                    worldBorder.setWarningTime(10);
                    break;
                case RED:
                    worldBorder.setWarningDistance(7);
                    worldBorder.setWarningTime(20);
                    break;
            }
        }
    }
    
    @Override
    public void removeWorldBorder(Player player) {
        // Reset to default world border
        player.setWorldBorder(null);
    }
    
    @Override
    public void sendAnimatedWorldBorder(Player player, World world, double centerX, double centerZ,
                                       double oldSize, double newSize, double duration, BorderColor color) {
        org.bukkit.WorldBorder worldBorder = player.getWorldBorder();

        // If the player doesn't have a custom world border, create one.
        if (worldBorder == null) {
            worldBorder = world.getWorldBorder();
            player.setWorldBorder(worldBorder);
            worldBorder = player.getWorldBorder(); // Re-fetch the player's instance
        }
        
        if (worldBorder != null) {
            // Set center
            worldBorder.setCenter(centerX, centerZ);

            // Animate size change - convert double to long for Bukkit API
            worldBorder.setSize(newSize, (long) duration); // duration in seconds

            // Set warning distance based on color
            switch (color) {
                case BLUE:
                    worldBorder.setWarningDistance(5);
                    worldBorder.setWarningTime(15);
                    break;
                case GREEN:
                    worldBorder.setWarningDistance(3);
                    worldBorder.setWarningTime(10);
                    break;
                case RED:
                    worldBorder.setWarningDistance(7);
                    worldBorder.setWarningTime(20);
                    break;
            }
        }
    }
}