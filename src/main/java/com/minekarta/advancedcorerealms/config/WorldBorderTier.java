package com.minekarta.advancedcorerealms.config;

/**
 * Represents a specific tier of world border upgrade.
 * This is an immutable data class.
 */
public class WorldBorderTier {

    private final String id;
    private final int size;
    private final double cost;

    public WorldBorderTier(String id, int size, double cost) {
        this.id = id;
        this.size = size;
        this.cost = cost;
    }

    public String getId() {
        return id;
    }

    public int getSize() {
        return size;
    }

    public double getCost() {
        return cost;
    }
}