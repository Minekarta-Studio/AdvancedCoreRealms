package com.minekarta.advancedcorerealms.upgrades;

import com.minekarta.advancedcorerealms.data.object.Realm;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Upgrade for increasing the number of warps in a realm
 */
public class RealmWarpUpgrade extends BaseRealmUpgrade {
    
    public RealmWarpUpgrade(String id, String name, String description, int maxLevel,
                            Map<Integer, Double> prices, Map<Integer, Object> effects, String icon) {
        super(id, name, description, maxLevel, prices, effects, icon);
    }
    
    @Override
    public void applyUpgrade(Realm realm, int newLevel) {
        // Get the new warp limit from the effect value
        Object effectValue = getEffectValue(newLevel);
        if (effectValue instanceof Number) {
            int newLimit = ((Number) effectValue).intValue();
            // For now, we'll store this as additional data in the realm
            // The actual warp system would need to be implemented separately
            // This could be a placeholder for future warp functionality
        }
    }
    
    @Override
    public boolean canApplyUpgrade(Player player, Realm realm) {
        // Check if player is the owner of the realm
        if (!realm.getOwner().equals(player.getUniqueId())) {
            return false;
        }
        
        return true;
    }
}