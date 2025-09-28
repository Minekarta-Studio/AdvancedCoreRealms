package com.minekarta.advancedcorerealms.upgrades;

import com.minekarta.advancedcorerealms.data.object.Realm;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Upgrade for increasing the member limit in a realm
 */
public class RealmMembersUpgrade extends BaseRealmUpgrade {
    
    public RealmMembersUpgrade(String id, String name, String description, int maxLevel,
                               Map<Integer, Double> prices, Map<Integer, Object> effects, String icon) {
        super(id, name, description, maxLevel, prices, effects, icon);
    }
    
    @Override
    public void applyUpgrade(Realm realm, int newLevel) {
        // Get the new member limit from the effect value
        Object effectValue = getEffectValue(newLevel);
        if (effectValue instanceof Number) {
            int newLimit = ((Number) effectValue).intValue();
            // Note: This might require updating the actual member management system
            // For now, we're storing it as part of the realm data
            realm.setMaxPlayers(newLimit); // Using maxPlayers as the team member limit for now
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