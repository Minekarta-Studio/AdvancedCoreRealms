package com.minekarta.advancedcorerealms.upgrades.definitions;

public class DifficultyUpgrade implements UpgradeDefinition {
    private final String id;
    private final double price;

    public DifficultyUpgrade(String id, double price) {
        this.id = id;
        this.price = price;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public double getPrice() {
        return price;
    }
}