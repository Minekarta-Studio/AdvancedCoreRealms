package com.minekarta.advancedcorerealms.upgrades.definitions;

public class KeepLoadedUpgrade implements UpgradeDefinition {
    private final String id = "keepLoaded";
    private final double price;

    public KeepLoadedUpgrade(double price) {
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