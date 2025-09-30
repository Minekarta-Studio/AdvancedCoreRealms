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

    public String getGuiTitle() {
        return guiTitle;
    }

    public String getGuiItemName() {
        return guiItemName;
    }

    public List<String> getGuiItemLore() {
        return guiItemLore;
    }
}