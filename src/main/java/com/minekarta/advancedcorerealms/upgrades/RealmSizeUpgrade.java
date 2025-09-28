package com.minekarta.advancedcorerealms.upgrades;

import com.minekarta.advancedcorerealms.data.object.Realm;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Upgrade for increasing the size of the realm border
 */
public class RealmSizeUpgrade extends BaseRealmUpgrade {
    
    public RealmSizeUpgrade(String id, String name, String description, int maxLevel,
                           Map<Integer, Double> prices, Map<Integer, Object> effects, String icon) {
        super(id, name, description, maxLevel, prices, effects, icon);
    }
    
    @Override
    public void applyUpgrade(Realm realm, int newLevel) {
        // Get the new border size from the effect value
        Object effectValue = getEffectValue(newLevel);
        if (effectValue instanceof Number) {
            int newSize = ((Number) effectValue).intValue();
            realm.setBorderSize(newSize);
            
            // Update the world border for all players currently in the realm
            World bukkitWorld = realm.getBukkitWorld();
            if (bukkitWorld != null) {
                for (Player player : bukkitWorld.getPlayers()) {
                    // Update the player's world border to reflect the new size
                    // This would use the AdvancedCorePlayer system to update the border
                }
            }
        }
    }
    
    @Override
    public boolean canApplyUpgrade(Player player, Realm realm) {
        // Check if player is the owner of the realm
        if (!realm.getOwner().equals(player.getUniqueId())) {
            return false;
        }
        
        // Additional checks can be added here if needed
        return true;
    }
}