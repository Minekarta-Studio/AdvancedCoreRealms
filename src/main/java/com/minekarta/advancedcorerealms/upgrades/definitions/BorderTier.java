package com.minekarta.advancedcorerealms.upgrades.definitions;

public class BorderTier implements UpgradeDefinition {
    private final String id;
    private final int size;
    private final double price;

    public BorderTier(String id, int size, double price) {
        this.id = id;
        this.size = size;
        this.price = price;
    }

    @Override
    public String getId() {
        return id;
    }

    public int getSize() {
        return size;
    }

    @Override
    public double getPrice() {
        return price;
    }
}