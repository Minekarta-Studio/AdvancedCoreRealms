package com.minekarta.advancedcorerealms.worldborder;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * Manages the loading, parsing, and in-memory storage of world border tier
 * configurations from the {@code world_borders.yml} file.
 * <p>
 * This class is responsible for:
 * <ul>
 *     <li>Ensuring a default {@code world_borders.yml} exists in the plugin's data folder.</li>
 *     <li>Loading the YAML file and parsing its contents into a collection of {@link WorldBorderTier} objects.</li>
 *     <li>Providing safe, cached access to the loaded tiers for other parts of the plugin.</li>
 *     <li>Handling and logging errors gracefully if the configuration file is malformed.</li>
 * </ul>
 */
public class WorldBorderConfig {

    private final AdvancedCoreRealms plugin;
    private final Map<String, WorldBorderTier> borderTiers = new HashMap<>();
    private String defaultTierId;

    /**
     * Constructs the configuration loader for world border tiers.
     *
     * @param plugin The main plugin instance.
     */
    public WorldBorderConfig(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads the world border tiers from the {@code world_borders.yml} file.
     * It first saves the default configuration if it doesn't exist, then loads
     * and parses the tier data into memory.
     */
    public void load() {
        // Clear previous data
        borderTiers.clear();

        File configFile = new File(plugin.getDataFolder(), "world_borders.yml");
        if (!configFile.exists()) {
            plugin.saveResource("world_borders.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Load the default tier ID
        this.defaultTierId = config.getString("default_tier", "tier_1");

        // Load all tiers
        ConfigurationSection tiersSection = config.getConfigurationSection("tiers");
        if (tiersSection == null) {
            plugin.getLogger().warning("Could not find 'tiers' section in world_borders.yml. No border tiers will be loaded.");
            return;
        }

        for (String tierId : tiersSection.getKeys(false)) {
            ConfigurationSection tierSection = tiersSection.getConfigurationSection(tierId);
            if (tierSection != null) {
                try {
                    WorldBorderTier tier = new WorldBorderTier(
                            tierId,
                            tierSection.getDouble("size"),
                            tierSection.getDouble("center_x", 0.0),
                            tierSection.getDouble("center_z", 0.0),
                            tierSection.getInt("warning_distance", 10),
                            tierSection.getInt("warning_time", 15),
                            tierSection.getInt("transition_time", 10),
                            tierSection.getDouble("cost_to_upgrade", 0.0)
                    );
                    borderTiers.put(tierId.toLowerCase(), tier);
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to parse world border tier '" + tierId + "'. Please check the configuration.", e);
                }
            }
        }
        plugin.getLogger().info("Successfully loaded " + borderTiers.size() + " world border tiers.");
    }

    /**
     * Retrieves a specific world border tier by its unique ID.
     *
     * @param tierId The case-insensitive ID of the tier to retrieve.
     * @return The {@link WorldBorderTier} object, or {@code null} if not found.
     */
    public WorldBorderTier getTier(String tierId) {
        if (tierId == null) return null;
        return borderTiers.get(tierId.toLowerCase());
    }

    /**
     * Retrieves the default world border tier as specified in the configuration.
     *
     * @return The default {@link WorldBorderTier}, or {@code null} if the default
     *         ID is invalid or no tiers are loaded.
     */
    public WorldBorderTier getDefaultTier() {
        return getTier(defaultTierId);
    }

    /**
     * Returns an unmodifiable map of all loaded border tiers.
     *
     * @return A map of tier IDs to {@link WorldBorderTier} objects.
     */
    public Map<String, WorldBorderTier> getAllTiers() {
        return Collections.unmodifiableMap(borderTiers);
    }
}