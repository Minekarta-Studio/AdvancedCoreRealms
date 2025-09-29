package com.minekarta.advancedcorerealms.data.object;

import java.time.Instant;
import java.util.UUID;

/**
 * A data class to hold the serialized contents of a player's inventory.
 */
public class SerializedInventory {

    private final String mainContents;
    private final String armorContents;
    private final String offhandContents;
    private final String enderChestContents; // Optional
    private final Instant savedAt;
    private final UUID sourceRealmId; // Can be null for global inventory

    public SerializedInventory(String mainContents, String armorContents, String offhandContents, String enderChestContents, UUID sourceRealmId) {
        this.mainContents = mainContents;
        this.armorContents = armorContents;
        this.offhandContents = offhandContents;
        this.enderChestContents = enderChestContents;
        this.sourceRealmId = sourceRealmId;
        this.savedAt = Instant.now();
    }

    // Getters
    public String getMainContents() {
        return mainContents;
    }

    public String getArmorContents() {
        return armorContents;
    }

    public String getOffhandContents() {
        return offhandContents;
    }

    public String getEnderChestContents() {
        return enderChestContents;
    }

    public Instant getSavedAt() {
        return savedAt;
    }

    public UUID getSourceRealmId() {
        return sourceRealmId;
    }
}