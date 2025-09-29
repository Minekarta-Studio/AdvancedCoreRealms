package com.minekarta.advancedcorerealms.upgrades.definitions;

/**
 * A marker interface for all upgrade definitions.
 * This provides a common type for different kinds of realm upgrades.
 */
public interface UpgradeDefinition {
    /**
     * Gets the unique identifier for this upgrade definition.
     * For tiered upgrades, this would be the tier ID.
     *
     * @return The unique ID.
     */
    String getId();

    /**
     * Gets the price of this upgrade.
     *
     * @return The price.
     */
    double getPrice();
}