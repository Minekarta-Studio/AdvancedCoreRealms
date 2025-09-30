package com.minekarta.advancedcorerealms.config;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages all plugin configurations from config.yml.
 * Provides a centralized, type-safe way to access configuration values.
 */
public class ConfigManager {

    private final AdvancedCoreRealms plugin;
    private FileConfiguration config;

    // GUI Settings
    private String guiTitle;
    private String guiItemName;
    private List<String> guiItemLore;

    // WorldBorder Tiers
    private final Map<String, WorldBorderTier> worldBorderTiers = new HashMap<>();

    public ConfigManager(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        plugin.saveDefaultConfig();
    }

    /**
     * Loads all configuration values from the config.yml file.
     */
    public void load() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();

        loadGuiSettings();
        loadWorldBorderTiers();
    }

    private void loadGuiSettings() {
        ConfigurationSection guiSection = config.getConfigurationSection("gui");
        if (guiSection != null) {
            this.guiTitle = guiSection.getString("title", "<green>My Realms</green>");
            this.guiItemName = guiSection.getString("item_name", "<yellow>%realm_name%</yellow>");
            this.guiItemLore = guiSection.getStringList("item_lore");
        } else {
            // Defaults if section is missing
            this.guiTitle = "<green>My Realms</green>";
            this.guiItemName = "<yellow>%realm_name%</yellow>";
            this.guiItemLore = List.of("<gray>Owner: %realm_owner%", "<gray>Click to enter!</gray>");
        }
    }

    private void loadWorldBorderTiers() {
        worldBorderTiers.clear();
        ConfigurationSection tiersSection = config.getConfigurationSection("world_border_tiers");
        if (tiersSection != null) {
            for (String tierId : tiersSection.getKeys(false)) {
                ConfigurationSection tierSection = tiersSection.getConfigurationSection(tierId);
                if (tierSection != null) {
                    int size = tierSection.getInt("size");
                    double cost = tierSection.getDouble("cost", 0.0);
                    worldBorderTiers.put(tierId, new WorldBorderTier(tierId, size, cost));
                }
            }
        }
        // Ensure a default tier exists
        if (!worldBorderTiers.containsKey("default")) {
            worldBorderTiers.put("default", new WorldBorderTier("default", 100, 0));
        }
    }

    public String getGuiTitle() {
        return guiTitle;
    }

    public String getGuiItemName() {
        return guiItemName;
    }

    public List<String> getGuiItemLore() {
        return guiItemLore;
    }

    public Map<String, WorldBorderTier> getWorldBorderTiers() {
        return worldBorderTiers;
    }

    public WorldBorderTier getTier(String tierId) {
        return worldBorderTiers.get(tierId);
    }

    public WorldBorderTier getDefaultTier() {
        return worldBorderTiers.get("default");
    }
}