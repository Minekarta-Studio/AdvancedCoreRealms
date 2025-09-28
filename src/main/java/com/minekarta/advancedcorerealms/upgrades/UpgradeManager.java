package com.minekarta.advancedcorerealms.upgrades;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.upgrades.events.RealmsUpgradeEvent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.File;
import java.util.*;

/**
 * Manager class for handling all realm upgrades
 */
public class UpgradeManager {
    private final AdvancedCoreRealms plugin;
    private final Map<String, RealmUpgrade> upgrades;
    private final File upgradesDirectory;
    private Economy economy;
    
    public UpgradeManager(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.upgrades = new HashMap<>();
        this.upgradesDirectory = new File(plugin.getDataFolder(), "modules" + File.separator + "upgrades");
        
        if (!upgradesDirectory.exists()) {
            upgradesDirectory.mkdirs();
        }
    }
    
    /**
     * Initialize the economy system
     */
    public void initializeEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault plugin not found! Economy features will be disabled.");
            return;
        }
        
        try {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                plugin.getLogger().warning("No economy plugin found! Economy features will be disabled.");
                return;
            }
            this.economy = rsp.getProvider();
            plugin.getLogger().info("Vault economy system loaded successfully.");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to initialize economy system: " + e.getMessage());
        }
    }
    
    /**
     * Load all upgrade configurations from files
     */
    public void loadUpgrades() {
        upgrades.clear();
        
        // Load upgrade files from the upgrades directory
        File[] files = upgradesDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
        
        if (files != null) {
            for (File file : files) {
                loadUpgradeFromFile(file);
            }
        }
        
        if (upgrades.isEmpty()) {
            // If no upgrades were loaded from files, create default ones
            createDefaultUpgrades();
        }
        
        plugin.getLogger().info("Loaded " + upgrades.size() + " realm upgrades");
    }
    
    private void loadUpgradeFromFile(File file) {
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            
            String id = file.getName().replace(".yml", "").replace(".yaml", "");
            String name = config.getString("name", id);
            String description = config.getString("description", "");
            int maxLevel = config.getInt("max-level", 1);
            String icon = config.getString("icon", "STONE");
            
            // Load prices and effects
            Map<Integer, Double> prices = new HashMap<>();
            Map<Integer, Object> effects = new HashMap<>();
            
            ConfigurationSection priceSection = config.getConfigurationSection("prices");
            if (priceSection != null) {
                for (String levelStr : priceSection.getKeys(false)) {
                    try {
                        int level = Integer.parseInt(levelStr);
                        double price = config.getDouble("prices." + levelStr);
                        prices.put(level, price);
                    } catch (NumberFormatException e) {
                        plugin.getLogger().warning("Invalid level in prices section for " + id + ": " + levelStr);
                    }
                }
            }
            
            ConfigurationSection effectSection = config.getConfigurationSection("effects");
            if (effectSection != null) {
                for (String levelStr : effectSection.getKeys(false)) {
                    try {
                        int level = Integer.parseInt(levelStr);
                        Object effect = config.get("effects." + levelStr);
                        effects.put(level, effect);
                    } catch (NumberFormatException e) {
                        plugin.getLogger().warning("Invalid level in effects section for " + id + ": " + levelStr);
                    }
                }
            }
            
            // Create the appropriate upgrade based on the ID
            RealmUpgrade upgrade = createUpgrade(id, name, description, maxLevel, prices, effects, icon);
            if (upgrade != null) {
                upgrades.put(id, upgrade);
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("Error loading upgrade from file: " + file.getName());
            e.printStackTrace();
        }
    }
    
    private RealmUpgrade createUpgrade(String id, String name, String description, int maxLevel,
                                      Map<Integer, Double> prices, Map<Integer, Object> effects, String icon) {
        // Create specific upgrade instances based on the ID
        switch (id.toLowerCase()) {
            case "realmsizeupgrade":
            case "realmssizeupgrade":
                return new RealmSizeUpgrade(id, name, description, maxLevel, prices, effects, icon);
            case "realmsmembersupgrade":
            case "realmsteamlimitupgrade":
                return new RealmMembersUpgrade(id, name, description, maxLevel, prices, effects, icon);
            case "realmsdifficultyupgrade":
            case "realmsdifficulty":
                return new RealmDifficultyUpgrade(id, name, description, maxLevel, prices, effects, icon);
            case "realmsetwarpupgrade":
            case "realmsetwarp":
                return new RealmWarpUpgrade(id, name, description, maxLevel, prices, effects, icon);
            default:
                // Default to base upgrade if no specific type is found
                return new BaseRealmUpgrade(id, name, description, maxLevel, prices, effects, icon) {
                    @Override
                    public void applyUpgrade(Realm realm, int newLevel) {
                        // Default implementation - could be customized per upgrade
                    }
                };
        }
    }
    
    private void createDefaultUpgrades() {
        // Create default upgrades if no files are found
        plugin.getLogger().info("Creating default upgrades...");
        
        // Realms Size Upgrade (default)
        Map<Integer, Double> sizePrices = new HashMap<>();
        Map<Integer, Object> sizeEffects = new HashMap<>();
        for (int i = 1; i <= 10; i++) {
            sizePrices.put(i, Math.pow(2, i) * 50); // Exponential pricing
            sizeEffects.put(i, 50 + (i * 50)); // Increase size by 50 per level
        }
        upgrades.put("default_size", new RealmSizeUpgrade("default_size", "Realms Size", 
            "Increase the size of your realm borders", 10, sizePrices, sizeEffects, "GRASS_BLOCK"));
        
        // Realms Members Upgrade (default)
        Map<Integer, Double> memberPrices = new HashMap<>();
        Map<Integer, Object> memberEffects = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            memberPrices.put(i, 200.0 * i * i); // Quadratic pricing
            memberEffects.put(i, 4 * i); // 4, 8, 12, 16, 20 members
        }
        upgrades.put("default_members", new RealmMembersUpgrade("default_members", "Team Limits", 
            "Increase the maximum number of team members", 5, memberPrices, memberEffects, "PLAYER_HEAD"));
    }
    
    /**
     * Get a specific upgrade by ID
     */
    public RealmUpgrade getUpgrade(String upgradeId) {
        return upgrades.get(upgradeId.toLowerCase());
    }
    
    /**
     * Get all registered upgrades
     */
    public Collection<RealmUpgrade> getUpgrades() {
        return upgrades.values();
    }
    
    /**
     * Upgrade a realm's specific upgrade level
     */
    public boolean upgradeRealm(Realm realm, String upgradeId, Player player) {
        RealmUpgrade upgrade = getUpgrade(upgradeId);
        if (upgrade == null) {
            return false;
        }
        
        int currentLevel = upgrade.getLevel(realm);
        if (currentLevel >= upgrade.getMaxLevel()) {
            // Already at max level
            return false;
        }
        
        // Calculate cost for upgrading to next level
        double cost = upgrade.getCost(currentLevel);
        
        // Check if player has enough money
        if (economy != null && !economy.has(player, cost)) {
            return false;
        }
        
        // Call the pre-check event
        RealmsUpgradeEvent upgradeEvent = new RealmsUpgradeEvent(player, realm, upgrade, currentLevel + 1, currentLevel);
        Bukkit.getPluginManager().callEvent(upgradeEvent);
        
        if (upgradeEvent.isCancelled()) {
            return false;
        }
        
        // Deduct money from player if economy is available
        if (economy != null) {
            economy.withdrawPlayer(player, cost);
        }
        
        // Set the new level
        int newLevel = currentLevel + 1;
        upgrade.setLevel(realm, newLevel);
        
        // Apply the upgrade effect
        upgrade.applyUpgrade(realm, newLevel);
        
        return true;
    }
    
    /**
     * Check if player has enough money for the upgrade
     */
    public boolean hasEnoughMoney(Player player, double amount) {
        if (economy == null) {
            return true; // If no economy, allow everything
        }
        return economy.has(player, amount);
    }
    
    /**
     * Get the player's balance
     */
    public double getPlayerBalance(Player player) {
        if (economy == null) {
            return 0.0;
        }
        return economy.getBalance(player);
    }
    
    /**
     * Get the cost for upgrading to the next level
     */
    public double getUpgradeCost(Realm realm, String upgradeId) {
        RealmUpgrade upgrade = getUpgrade(upgradeId);
        if (upgrade == null) {
            return -1;
        }
        
        int currentLevel = upgrade.getLevel(realm);
        if (currentLevel >= upgrade.getMaxLevel()) {
            return -1; // Already at max level
        }
        
        return upgrade.getCost(currentLevel);
    }
}