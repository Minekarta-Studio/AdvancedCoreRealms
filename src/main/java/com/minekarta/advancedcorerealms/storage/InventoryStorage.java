package com.minekarta.advancedcorerealms.storage;

import com.minekarta.advancedcorerealms.data.object.SerializedInventory;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * An interface for storing and retrieving player inventories for realms.
 */
public interface InventoryStorage {

    /**
     * Saves a player's serialized inventory for a specific realm.
     *
     * @param realmId      The ID of the realm.
     * @param playerUuid   The UUID of the player.
     * @param data         The serialized inventory data.
     * @return A CompletableFuture that completes when the save operation is finished.
     */
    CompletableFuture<Void> savePlayerInventory(UUID realmId, UUID playerUuid, SerializedInventory data);

    /**
     * Loads a player's serialized inventory for a specific realm.
     *
     * @param realmId      The ID of the realm.
     * @param playerUuid   The UUID of the player.
     * @return A CompletableFuture that will contain the serialized inventory, if it exists.
     */
    CompletableFuture<Optional<SerializedInventory>> loadPlayerInventory(UUID realmId, UUID playerUuid);

    /**
     * Deletes a player's inventory for a specific realm.
     *
     * @param realmId      The ID of the realm.
     * @param playerUuid   The UUID of the player.
     * @return A CompletableFuture that completes when the deletion is finished.
     */
    CompletableFuture<Void> deletePlayerInventory(UUID realmId, UUID playerUuid);
}