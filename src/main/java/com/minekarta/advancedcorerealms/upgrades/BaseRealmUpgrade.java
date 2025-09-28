package com.minekarta.advancedcorerealms.upgrades;

import com.minekarta.advancedcorerealms.data.object.Realm;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Base implementation for realm upgrades
 */
public abstract class BaseRealmUpgrade implements RealmUpgrade {
    protected final String id;
    protected final String name;
    protected final String description;
    protected final int maxLevel;
    protected final Map<Integer, Double> prices;
    protected final Map<Integer, Object> effects;
    protected final String icon;
    
    public BaseRealmUpgrade(String id, String name, String description, int maxLevel, 
                           Map<Integer, Double> prices, Map<Integer, Object> effects, String icon) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.maxLevel = maxLevel;
        this.prices = new HashMap<>(prices);
        this.effects = new HashMap<>(effects);
        this.icon = icon;
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public int getMaxLevel() {
        return maxLevel;
    }
    
    @Override
    public int getLevel(Realm realm) {
        if (realm.getUpgradeLevels() == null) {
            // Initialize upgrade levels if not already set
            realm.setUpgradeLevels(new HashMap<>());
        }
        return realm.getUpgradeLevels().getOrDefault(id, 0);
    }
    
    @Override
    public void setLevel(Realm realm, int level) {
        if (realm.getUpgradeLevels() == null) {
            realm.setUpgradeLevels(new HashMap<>());
        }
        realm.getUpgradeLevels().put(id, level);
    }
    
    @Override
    public double getCost(int currentLevel) {
        return prices.getOrDefault(currentLevel + 1, 0.0);
    }
    
    @Override
    public Object getEffectValue(int level) {
        return effects.get(level);
    }
    
    @Override
    public boolean canApplyUpgrade(Player player, Realm realm) {
        // Check if player is the owner of the realm
        return realm.getOwner().equals(player.getUniqueId());
    }
    
    @Override
    public boolean isMaxLevel(Realm realm) {
        return getLevel(realm) >= getMaxLevel();
    }
    
    @Override
    public String getIcon() {
        return icon;
    }
}