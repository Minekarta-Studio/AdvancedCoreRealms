package com.minekarta.advancedcorerealms.upgrades;

import com.minekarta.advancedcorerealms.data.object.Realm;
import org.bukkit.entity.Player;

/**
 * Interface representing an upgrade that can be purchased with money
 */
public interface RealmUpgrade {
    
    /**
     * Get the unique identifier for this upgrade
     */
    String getId();
    
    /**
     * Get the display name of the upgrade
     */
    String getName();
    
    /**
     * Get the description of the upgrade
     */
    String getDescription();
    
    /**
     * Get the maximum level this upgrade can reach
     */
    int getMaxLevel();
    
    /**
     * Get the current level of the upgrade for a specific realm
     */
    int getLevel(Realm realm);
    
    /**
     * Set the level of the upgrade for a specific realm
     */
    void setLevel(Realm realm, int level);
    
    /**
     * Get the cost to upgrade to the next level
     */
    double getCost(int currentLevel);
    
    /**
     * Get the effect value of a specific upgrade level
     */
    Object getEffectValue(int level);
    
    /**
     * Check if this upgrade can be applied to the given realm
     */
    boolean canApplyUpgrade(Player player, Realm realm);
    
    /**
     * Apply the upgrade effect to the realm
     */
    void applyUpgrade(Realm realm, int newLevel);
    
    /**
     * Check if the upgrade has reached maximum level
     */
    boolean isMaxLevel(Realm realm);
    
    /**
     * Get the upgrade icon material name (for GUI display)
     */
    String getIcon();
}