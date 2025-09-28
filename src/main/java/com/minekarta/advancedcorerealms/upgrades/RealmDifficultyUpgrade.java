package com.minekarta.advancedcorerealms.upgrades;

import com.minekarta.advancedcorerealms.data.object.Realm;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Upgrade for changing the difficulty of a realm
 */
public class RealmDifficultyUpgrade extends BaseRealmUpgrade {
    
    public RealmDifficultyUpgrade(String id, String name, String description, int maxLevel,
                                  Map<Integer, Double> prices, Map<Integer, Object> effects, String icon) {
        super(id, name, description, maxLevel, prices, effects, icon);
    }
    
    @Override
    public void applyUpgrade(Realm realm, int newLevel) {
        // Get the new difficulty from the effect value
        Object effectValue = getEffectValue(newLevel);
        if (effectValue instanceof String) {
            String difficultyStr = (String) effectValue;
            try {
                Difficulty difficulty = Difficulty.valueOf(difficultyStr.toUpperCase());
                
                // Set the difficulty for the realm's world
                World bukkitWorld = realm.getBukkitWorld();
                if (bukkitWorld != null) {
                    bukkitWorld.setDifficulty(difficulty);
                }
                
                // Update the realm's peaceful mode flag based on difficulty
                realm.setPeacefulMode(difficulty == Difficulty.PEACEFUL);
            } catch (IllegalArgumentException e) {
                // Invalid difficulty setting, log error
                System.err.println("Invalid difficulty value for upgrade: " + difficultyStr);
            }
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