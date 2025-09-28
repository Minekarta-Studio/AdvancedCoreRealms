package com.minekarta.advancedcorerealms.api;

import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.worldborder.BorderColor;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Interface representing a player in the AdvancedCoreRealms system
 */
public interface AdvancedCorePlayer {
    
    /**
     * Get the Bukkit player
     */
    Player asPlayer();
    
    /**
     * Get the player's UUID
     */
    UUID getUniqueId();
    
    /**
     * Check if the player has world border enabled
     */
    boolean hasWorldBorderEnabled();
    
    /**
     * Toggle the player's world border state
     */
    void toggleWorldBorder();
    
    /**
     * Get the player's selected border color
     */
    BorderColor getBorderColor();
    
    /**
     * Set the player's border color
     */
    void setBorderColor(BorderColor color);
    
    /**
     * Update the world border for the player based on their current realm
     */
    void updateWorldBorder(Realm realm);
    
    /**
     * Remove the world border for the player
     */
    void removeWorldBorder();
    
    /**
     * Update the world border to match the current realm the player is in
     */
    void updateWorldBorder();
}