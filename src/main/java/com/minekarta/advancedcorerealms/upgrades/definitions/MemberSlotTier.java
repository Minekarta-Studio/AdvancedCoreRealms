package com.minekarta.advancedcorerealms.upgrades.definitions;

public class MemberSlotTier implements UpgradeDefinition {
    private final String id;
    private final int additionalSlots;
    private final double price;

    public MemberSlotTier(String id, int additionalSlots, double price) {
        this.id = id;
        this.additionalSlots = additionalSlots;
        this.price = price;
    }

    @Override
    public String getId() {
        return id;
    }

    public int getAdditionalSlots() {
        return additionalSlots;
    }

    @Override
    public double getPrice() {
        return price;
    }
}