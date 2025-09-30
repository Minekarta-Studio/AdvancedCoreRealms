package com.minekarta.advancedcorerealms.manager;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.PlayerData;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.storage.StorageManager;
import org.bukkit.Location;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Manages all data persistence and caching for {@link Realm} objects using a JSON file-based storage system.
 * This class provides a fully asynchronous, non-blocking API for all file interactions
 * to ensure the server's main thread is never impacted. It uses an in-memory caching
 * strategy for high performance.
 *
 * <p><b>Caching Strategy:</b></p>
 * <ul>
 *     <li><b>Realms:</b> A Guava {@link Cache} is used for storing loaded realms, keyed by their unique `realmId`.</li>
 *     <li><b>Lookups:</b> Secondary caches map realm names and world folder names to the `realmId` for fast lookups.</li>
 * </ul>
 *
 * <p><b>Data Flow:</b></p>
 * <p>On plugin startup, all realms are loaded from their JSON files into the cache.
 * All subsequent operations (get, update, delete) are performed on the cached instances first,
 * with corresponding file operations being dispatched asynchronously.</p>
 */
public class RealmManager {

    private final AdvancedCoreRealms plugin;
    private final StorageManager storageManager;

    // Primary cache: UUID -> Realm
    private final Cache<UUID, Realm> realmByIdCache;
    // Lookup cache: String (name) -> UUID
    private final Cache<String, UUID> realmByNameCache;
    // Lookup cache: String (world folder) -> UUID
    private final Cache<String, UUID> worldFolderNameCache;

    public RealmManager(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.storageManager = plugin.getStorageManager();

        this.realmByIdCache = CacheBuilder.newBuilder()
                .maximumSize(500) // Adjust size as needed
                .expireAfterAccess(60, TimeUnit.MINUTES)
                .build();

        this.realmByNameCache = CacheBuilder.newBuilder()
                .maximumSize(500)
                .expireAfterAccess(60, TimeUnit.MINUTES)
                .build();

        this.worldFolderNameCache = CacheBuilder.newBuilder()
                .maximumSize(500)
                .expireAfterAccess(60, TimeUnit.MINUTES)
                .build();
    }

    /**
     * Initializes the RealmManager by loading all existing realms from storage into the cache.
     * This should be called during the plugin's onEnable sequence.
     */
    public void init() {
        storageManager.loadAllRealms().thenAccept(realms -> {
            plugin.getLogger().info("Loading " + realms.size() + " realms from storage...");
            for (Realm realm : realms) {
                cacheRealm(realm);
            }
            plugin.getLogger().info("Realm loading complete.");
        }).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize RealmManager and load realms", ex);
            return null;
        });
    }

    /**
     * Asynchronously saves a new realm and adds it to the cache.
     *
     * @param realm The {@link Realm} object to create and persist.
     * @return A {@link CompletableFuture} that completes when the operation is finished.
     */
    public CompletableFuture<Void> createRealm(Realm realm) {
        return storageManager.saveRealm(realm).thenRun(() -> {
            cacheRealm(realm);
        });
    }

    /**
     * Asynchronously updates an existing realm's data in both storage and the cache.
     *
     * @param realm The {@link Realm} object with updated information.
     * @return A {@link CompletableFuture} that completes when the update is finished.
     */
    public CompletableFuture<Void> updateRealm(Realm realm) {
        return storageManager.saveRealm(realm).thenRun(() -> {
            // Re-cache the realm to ensure data consistency
            cacheRealm(realm);
        });
    }

    /**
     * Asynchronously deletes a realm from storage and invalidates it from the cache.
     *
     * @param realm The realm to delete.
     * @return A {@link CompletableFuture} that completes when the deletion is finished.
     */
    public CompletableFuture<Void> deleteRealm(Realm realm) {
        return storageManager.deleteRealm(realm.getRealmId()).thenRun(() -> {
            invalidateCaches(realm);
        });
    }

    /**
     * Retrieves a realm by its unique ID.
     *
     * @param realmId The UUID of the realm.
     * @return An {@link Optional} containing the {@link Realm} if found in the cache.
     */
    public Optional<Realm> getRealm(UUID realmId) {
        return Optional.ofNullable(realmByIdCache.getIfPresent(realmId));
    }

    /**
     * Retrieves a realm by its unique, case-insensitive name.
     *
     * @param name The name of the realm.
     * @return An {@link Optional} containing the {@link Realm} if found.
     */
    public Optional<Realm> getRealmByName(String name) {
        UUID realmId = realmByNameCache.getIfPresent(name.toLowerCase());
        if (realmId == null) {
            return Optional.empty();
        }
        return getRealm(realmId);
    }

    /**
     * Retrieves a realm by its world folder name.
     *
     * @param worldFolderName The name of the world folder.
     * @return An {@link Optional} containing the {@link Realm} if found.
     */
    public Optional<Realm> getRealmByWorldFolderName(String worldFolderName) {
        UUID realmId = worldFolderNameCache.getIfPresent(worldFolderName);
        if (realmId == null) {
            return Optional.empty();
        }
        return getRealm(realmId);
    }

    /**
     * Checks if a realm with the given name already exists.
     *
     * @param name The name to check.
     * @return true if a realm with this name exists, false otherwise.
     */
    public boolean doesRealmExist(String name) {
        return realmByNameCache.getIfPresent(name.toLowerCase()) != null;
    }

    /**
     * Retrieves all cached realms owned by a specific player.
     *
     * @param ownerUuid The UUID of the owner.
     * @return A list of owned {@link Realm}s.
     */
    public List<Realm> getRealmsByOwner(UUID ownerUuid) {
        return realmByIdCache.asMap().values().stream()
                .filter(realm -> realm.getOwner().equals(ownerUuid))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all cached realms where the player is a member (but not necessarily the owner).
     *
     * @param playerUuid The UUID of the player.
     * @return A list of {@link Realm}s the player is a member of.
     */
    public List<Realm> getMemberRealms(UUID playerUuid) {
        return realmByIdCache.asMap().values().stream()
                .filter(realm -> realm.isMember(playerUuid))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all realms currently loaded in the cache.
     *
     * @return A concurrent map of all cached realms.
     */
    public ConcurrentMap<UUID, Realm> getAllCachedRealms() {
        return realmByIdCache.asMap();
    }

    /**
     * Adds or updates a realm in the cache system.
     *
     * @param realm The realm to cache.
     */
    private void cacheRealm(Realm realm) {
        realmByIdCache.put(realm.getRealmId(), realm);
        realmByNameCache.put(realm.getName().toLowerCase(), realm.getRealmId());
        worldFolderNameCache.put(realm.getWorldFolderName(), realm.getRealmId());
    }

    /**
     * Invalidates all cache entries associated with a specific realm.
     *
     * @param realm The realm whose caches should be invalidated.
     */
    public void invalidateCaches(Realm realm) {
        if (realm != null) {
            realmByIdCache.invalidate(realm.getRealmId());
            realmByNameCache.invalidate(realm.getName().toLowerCase());
            worldFolderNameCache.invalidate(realm.getWorldFolderName());
        }
    }

    // Note: PlayerData management has been removed from RealmManager to improve separation of concerns.
    // This will be handled by a dedicated PlayerDataManager class if needed, or within the listener classes
    // for simple data like previous location. For now, we remove the complexity.

    private final Cache<UUID, Location> previousLocationCache = CacheBuilder.newBuilder()
            .maximumSize(500)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();

    public void savePreviousLocation(UUID playerUUID, Location location) {
        previousLocationCache.put(playerUUID, location);
    }

    public Optional<Location> getPreviousLocation(UUID playerUUID) {
        return Optional.ofNullable(previousLocationCache.getIfPresent(playerUUID));
    }

    public void clearPreviousLocation(UUID playerUUID) {
        previousLocationCache.invalidate(playerUUID);
    }
}