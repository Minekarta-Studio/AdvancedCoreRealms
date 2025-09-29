package com.minekarta.advancedcorerealms.worldborder;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Represents a specific, immutable tier or level of a world border configuration.
 * <p>
 * This class acts as a data holder (POJO) for the properties of a single world
 * border tier as defined in {@code world_borders.yml}. Instances of this class are
 * created by the {@link WorldBorderConfig} loader and are used throughout the plugin
 * to define border characteristics.
 */
public class WorldBorderTier {
    private final String id;
    private final double size;
    private final double centerX;
    private final double centerZ;
    private final int warningDistance;
    private final int warningTime;
    private final int transitionTime;
    private final double costToUpgrade;

    /**
     * Constructs a new WorldBorderTier.
     *
     * @param id               The unique identifier for this tier (e.g., "tier_1").
     * @param size             The diameter of the world border in blocks.
     * @param centerX          The default X-coordinate of the border's center.
     * @param centerZ          The default Z-coordinate of the border's center.
     * @param warningDistance  The distance from the border at which a warning is shown.
     * @param warningTime      The time a player has to return to the safe zone before taking damage.
     * @param transitionTime   The duration in seconds for the border to resize smoothly.
     * @param costToUpgrade    The cost to upgrade to this tier.
     */
    /**
     * Constructs a new WorldBorderTier from a configuration section.
     *
     * @param section The ConfigurationSection to load data from.
     */
    public WorldBorderTier(ConfigurationSection section) {
        this.id = section.getString("id");
        this.size = section.getDouble("size");
        this.centerX = section.getDouble("center.x", 0.0);
        this.centerZ = section.getDouble("center.z", 0.0);
        this.warningDistance = section.getInt("warning.distance", 10);
        this.warningTime = section.getInt("warning.time", 15);
        this.transitionTime = section.getInt("transition_time", 10);
        this.costToUpgrade = section.getDouble("price");
    }

    public WorldBorderTier(String id, double size, double centerX, double centerZ, int warningDistance, int warningTime, int transitionTime, double costToUpgrade) {
        this.id = id;
        this.size = size;
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.warningDistance = warningDistance;
        this.warningTime = warningTime;
        this.transitionTime = transitionTime;
        this.costToUpgrade = costToUpgrade;
    }

    // --- Getters ---

    /**
     * Gets the price to upgrade to this tier. This is an alias for getCostToUpgrade().
     * @return The price of the upgrade.
     */
    public double getPrice() {
        return costToUpgrade;
    }

    public String getId() {
        return id;
    }

    public double getSize() {
        return size;
    }

    public double getCenterX() {
        return centerX;
    }

    public double getCenterZ() {
        return centerZ;
    }

    public int getWarningDistance() {
        return warningDistance;
    }

    public int getWarningTime() {
        return warningTime;
    }

    public int getTransitionTime() {
        return transitionTime;
    }

    public double getCostToUpgrade() {
        return costToUpgrade;
    }
}