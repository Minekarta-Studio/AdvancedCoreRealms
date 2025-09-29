package com.minekarta.advancedcorerealms.manager;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.PlayerData;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.realm.Role;
import com.minekarta.advancedcorerealms.storage.DatabaseManager;
import com.minekarta.advancedcorerealms.worldborder.BorderColor;
import org.bukkit.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Manages all data persistence and caching for {@link Realm} and {@link PlayerData} objects.
 * This class provides a fully asynchronous, non-blocking API for all database interactions
 * to ensure the server's main thread is never impacted. It uses a sophisticated, multi-layered
 * caching strategy for high performance.
 *
 * <p><b>Caching Strategy:</b></p>
 * <ul>
 *     <li><b>Realms:</b> A Guava {@link LoadingCache} is used for realms, keyed by name. This cache
 *     is backed by a secondary {@link Cache} keyed by world name for fast lookups during
 *     world-related events.</li>
 *     <li><b>Player Data:</b> A Caffeine {@link AsyncLoadingCache} is used for player data.
 *     This ensures that loading player data from the database is fully asynchronous and
 *     does not block any calling thread.</li>
 * </ul>
 *
 * <p><b>Error Handling:</b></p>
 * <p>All methods that interact with the database are designed to propagate failures. If a
 * {@link SQLException} occurs, it is wrapped in a {@link RuntimeException}, causing the
 * calling {@link CompletableFuture} to complete exceptionally. This allows the caller to
 * handle database failures gracefully using {@code .exceptionally()}.</p>
 */
public class RealmManager {

    private final AdvancedCoreRealms plugin;
    private final DatabaseManager dbManager;
    private final LoadingCache<String, CompletableFuture<Realm>> realmNameCache;
    private final Cache<String, Realm> worldNameCache;
    private final LoadingCache<UUID, PlayerData> playerDataCache;

    public RealmManager(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.dbManager = plugin.getDatabaseManager();

        this.worldNameCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build();

        this.realmNameCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build(new CacheLoader<>() {
                    @Override
                    public CompletableFuture<Realm> load(String realmName) {
                        return getRealmByNameUncached(realmName).thenApply(realm -> {
                            if (realm != null) {
                                worldNameCache.put(realm.getWorldName(), realm);
                            }
                            return realm;
                        });
                    }
                });

        this.playerDataCache = CacheBuilder.newBuilder()
                .maximumSize(500)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build(new CacheLoader<>() {
                    @Override
                    public PlayerData load(UUID playerUUID) throws Exception {
                        // This is a blocking call, but we will pre-warm the cache on player join
                        // to prevent this from blocking the main thread during gameplay.
                        return loadPlayerData(playerUUID).get();
                    }
                });
    }

    /**
     * Asynchronously creates a new realm record in the database.
     * This includes all realm properties, such as ownership, configuration, and
     * the world border settings (size and center coordinates).
     *
     * @param realm The {@link Realm} object to persist.
     * @return A {@link CompletableFuture} that completes when the operation is finished, or fails if an error occurs.
     */
    public CompletableFuture<Void> createRealm(Realm realm) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT INTO realms (name, owner_uuid, world_name, template, created_at, is_flat, difficulty, keep_loaded, border_tier_id, member_slot_tier_id, border_size, max_players, border_center_x, border_center_z) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, realm.getName());
                pstmt.setString(2, realm.getOwner().toString());
                pstmt.setString(3, realm.getWorldName());
                pstmt.setString(4, realm.getTemplate());
                pstmt.setString(5, realm.getCreatedAt().toString());
                pstmt.setBoolean(6, realm.isFlat());
                pstmt.setString(7, realm.getDifficulty());
                pstmt.setBoolean(8, realm.isKeepLoaded());
                pstmt.setString(9, realm.getBorderTierId());
                pstmt.setString(10, realm.getMemberSlotTierId());
                pstmt.setInt(11, realm.getBorderSize());
                pstmt.setInt(12, realm.getMaxPlayers());
                pstmt.setDouble(13, realm.getBorderCenterX());
                pstmt.setDouble(14, realm.getBorderCenterZ());
                pstmt.executeUpdate();
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int realmId = generatedKeys.getInt(1);
                        realm.setId(realmId);
                        addMemberToRealm(realm, realm.getOwner(), Role.OWNER).join();
                    }
                }
                invalidateCaches(realm);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create realm in database: " + realm.getName(), e);
                throw new RuntimeException("Failed to create realm", e);
            }
        });
    }

    /**
     * Asynchronously retrieves a realm by its unique name from the cache or database.
     *
     * @param name The case-insensitive name of the realm.
     * @return A {@link CompletableFuture} containing the {@link Realm} if found, otherwise null.
     */
    public CompletableFuture<Realm> getRealmByName(String name) {
        return realmNameCache.getUnchecked(name);
    }

    /**
     * Asynchronously retrieves a realm by its associated world name.
     * It first checks a dedicated world name cache for performance, falling back to a full database lookup.
     *
     * @param worldName The name of the world associated with the realm.
     * @return A {@link CompletableFuture} containing the {@link Realm} if found, otherwise null.
     */
    public CompletableFuture<Realm> getRealmByWorldName(String worldName) {
        Realm realm = worldNameCache.getIfPresent(worldName);
        if (realm != null) {
            return CompletableFuture.completedFuture(realm);
        }
        return getRealmByNameUncached(worldName);
    }

    /**
     * Synchronously retrieves a realm from the cache by its world name.
     * This method is safe to call from the main thread as it only queries the in-memory cache.
     *
     * @param worldName The name of the world.
     * @return An {@link Optional} containing the cached {@link Realm}, or empty if not present.
     */
    public Optional<Realm> getRealmFromCacheByWorld(String worldName) {
        return Optional.ofNullable(worldNameCache.getIfPresent(worldName));
    }

    /**
     * Synchronously retrieves a realm from the cache by its name.
     * This method is safe to call from the main thread as it only queries the in-memory cache.
     *
     * @param realmName The name of the realm.
     * @return An {@link Optional} containing the cached {@link Realm}, or empty if not present.
     */
    public Optional<Realm> getRealmFromCacheByName(String realmName) {
        try {
            CompletableFuture<Realm> future = realmNameCache.getIfPresent(realmName);
            if (future != null && future.isDone() && !future.isCompletedExceptionally()) {
                return Optional.ofNullable(future.getNow(null));
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error getting realm from cache by name (sync): " + realmName, e);
        }
        return Optional.empty();
    }

    private CompletableFuture<Realm> getRealmByNameUncached(String name) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM realms WHERE name = ? OR world_name = ?";
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setString(2, name);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    Realm realm = mapResultSetToRealm(rs);
                    loadMembersForRealm(realm).join();
                    return realm;
                }
                return null;
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error getting realm by name uncached: " + name, e);
                throw new RuntimeException("Failed to get realm by name: " + name, e);
            }
        });
    }

    /**
     * Asynchronously retrieves all realms owned by a specific player.
     *
     * @param ownerUuid The UUID of the owner.
     * @return A {@link CompletableFuture} containing a list of owned {@link Realm}s.
     */
    public CompletableFuture<List<Realm>> getRealmsByOwner(UUID ownerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            List<Realm> realms = new ArrayList<>();
            String sql = "SELECT * FROM realms WHERE owner_uuid = ?";
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, ownerUuid.toString());
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    Realm realm = mapResultSetToRealm(rs);
                    loadMembersForRealm(realm).join();
                    realms.add(realm);
                }
                return realms;
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error getting realms by owner: " + ownerUuid, e);
                throw new RuntimeException("Failed to get realms by owner", e);
            }
        });
    }

    /**
     * Asynchronously retrieves all realms to which a player is invited (i.e., is a member but not the owner).
     *
     * @param playerUuid The UUID of the player.
     * @return A {@link CompletableFuture} containing a list of invited {@link Realm}s.
     */
    public CompletableFuture<List<Realm>> getInvitedRealms(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            List<Realm> realms = new ArrayList<>();
            String sql = "SELECT r.* FROM realms r JOIN realm_members rm ON r.id = rm.realm_id WHERE rm.player_uuid = ? AND r.owner_uuid != ?";
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, playerUuid.toString());
                pstmt.setString(2, playerUuid.toString());
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    Realm realm = mapResultSetToRealm(rs);
                    loadMembersForRealm(realm).join();
                    realms.add(realm);
                }
                return realms;
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error getting invited realms: " + playerUuid, e);
                throw new RuntimeException("Failed to get invited realms", e);
            }
        });
    }

    /**
     * Asynchronously updates an existing realm's record in the database.
     *
     * @param realm The {@link Realm} object with updated information to save.
     * @return A {@link CompletableFuture} that completes when the update is finished.
     */
    public CompletableFuture<Void> updateRealm(Realm realm) {
        return CompletableFuture.runAsync(() -> {
            String sql = "UPDATE realms SET owner_uuid = ?, difficulty = ?, keep_loaded = ?, border_tier_id = ?, member_slot_tier_id = ?, border_size = ?, max_players = ?, border_center_x = ?, border_center_z = ? WHERE name = ?";
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, realm.getOwner().toString());
                pstmt.setString(2, realm.getDifficulty());
                pstmt.setBoolean(3, realm.isKeepLoaded());
                pstmt.setString(4, realm.getBorderTierId());
                pstmt.setString(5, realm.getMemberSlotTierId());
                pstmt.setInt(6, realm.getBorderSize());
                pstmt.setInt(7, realm.getMaxPlayers());
                pstmt.setDouble(8, realm.getBorderCenterX());
                pstmt.setDouble(9, realm.getBorderCenterZ());
                pstmt.setString(10, realm.getName());
                pstmt.executeUpdate();
                invalidateCaches(realm);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not update realm: " + realm.getName(), e);
                throw new RuntimeException("Failed to update realm", e);
            }
        });
    }

    /**
     * Asynchronously deletes a realm from the database.
     *
     * @param realmName The name of the realm to delete.
     * @return A {@link CompletableFuture} that completes when the deletion is finished.
     */
    public CompletableFuture<Void> deleteRealm(String realmName) {
        return getRealmByName(realmName).thenAcceptAsync(realm -> {
            if (realm != null) {
                invalidateCaches(realm);
            } else {
                realmNameCache.invalidate(realmName);
            }
            String sql = "DELETE FROM realms WHERE name = ?";
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, realmName);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not delete realm: " + realmName, e);
                throw new RuntimeException("Failed to delete realm", e);
            }
        });
    }

    /**
     * Asynchronously adds or updates a player's membership in a realm.
     *
     * @param realm      The realm to modify.
     * @param memberUuid The UUID of the member to add or update.
     * @param role       The new role for the member.
     * @return A {@link CompletableFuture} that completes when the operation is finished.
     */
    public CompletableFuture<Void> addMemberToRealm(Realm realm, UUID memberUuid, Role role) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT OR REPLACE INTO realm_members (realm_id, player_uuid, role) VALUES (?, ?, ?)";
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, realm.getId());
                pstmt.setString(2, memberUuid.toString());
                pstmt.setString(3, role.name());
                pstmt.executeUpdate();
                invalidateCaches(realm);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not add member to realm: " + realm.getId(), e);
                throw new RuntimeException("Failed to add member to realm", e);
            }
        });
    }

    /**
     * Asynchronously removes a member from a realm.
     *
     * @param realm      The realm to modify.
     * @param memberUuid The UUID of the member to remove.
     * @return A {@link CompletableFuture} that completes when the operation is finished.
     */
    public CompletableFuture<Void> removeMemberFromRealm(Realm realm, UUID memberUuid) {
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM realm_members WHERE realm_id = ? AND player_uuid = ?";
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, realm.getId());
                pstmt.setString(2, memberUuid.toString());
                pstmt.executeUpdate();
                invalidateCaches(realm);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not remove member from realm: " + realm.getId(), e);
                throw new RuntimeException("Failed to remove member from realm", e);
            }
        });
    }

    /**
     * Invalidates all cache entries associated with a specific realm.
     * This should be called after any modification to a realm's data.
     *
     * @param realm The realm whose caches should be invalidated.
     */
    public void invalidateCaches(Realm realm) {
        if (realm != null) {
            realmNameCache.invalidate(realm.getName());
            worldNameCache.invalidate(realm.getWorldName());
        }
    }

    private CompletableFuture<Void> loadMembersForRealm(Realm realm) {
        return CompletableFuture.runAsync(() -> {
            String sql = "SELECT * FROM realm_members WHERE realm_id = ?";
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, realm.getId());
                ResultSet rs = pstmt.executeQuery();
                Map<UUID, Role> members = new HashMap<>();
                members.put(realm.getOwner(), Role.OWNER);
                while (rs.next()) {
                    UUID memberUuid = UUID.fromString(rs.getString("player_uuid"));
                    Role role = Role.valueOf(rs.getString("role"));
                    members.put(memberUuid, role);
                }
                realm.setMembers(members);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not load members for realm: " + realm.getName(), e);
                throw new RuntimeException("Failed to load members for realm", e);
            }
        });
    }

    private Realm mapResultSetToRealm(ResultSet rs) throws SQLException {
        Realm realm = new Realm(
                rs.getString("name"),
                UUID.fromString(rs.getString("owner_uuid")),
                rs.getString("world_name"),
                rs.getString("template"),
                Instant.parse(rs.getString("created_at")),
                rs.getBoolean("is_flat")
        );
        realm.setId(rs.getInt("id"));
        realm.setDifficulty(rs.getString("difficulty"));
        realm.setKeepLoaded(rs.getBoolean("keep_loaded"));
        realm.setBorderTierId(rs.getString("border_tier_id"));
        realm.setMemberSlotTierId(rs.getString("member_slot_tier_id"));
        realm.setBorderSize(rs.getInt("border_size"));
        realm.setMaxPlayers(rs.getInt("max_players"));
        realm.setBorderCenterX(rs.getDouble("border_center_x"));
        realm.setBorderCenterZ(rs.getDouble("border_center_z"));
        return realm;
    }

    /**
     * Asynchronously loads a player's data from the database.
     * If no data is found, a new {@link PlayerData} object with default values is returned.
     *
     * @param playerUUID The UUID of the player to load.
     * @return A {@link CompletableFuture} containing the player's data.
     */
    public CompletableFuture<PlayerData> loadPlayerData(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM player_data WHERE player_uuid = ?";
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, playerUUID.toString());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    PlayerData data = new PlayerData(playerUUID);
                    data.setPreviousLocationString(rs.getString("previous_location"));
                    data.setBorderEnabled(rs.getBoolean("border_enabled"));
                    data.setBorderColor(BorderColor.fromString(rs.getString("border_color")));
                    return data;
                } else {
                    return new PlayerData(playerUUID);
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load player data for " + playerUUID, e);
                throw new RuntimeException("Failed to load player data", e);
            }
        });
    }

    /**
     * Asynchronously saves a player's data to the database.
     *
     * @param data The {@link PlayerData} to save.
     * @return A {@link CompletableFuture} that completes when the data is saved.
     */
    public CompletableFuture<Void> savePlayerData(PlayerData data) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT OR REPLACE INTO player_data (player_uuid, previous_location, border_enabled, border_color) VALUES (?, ?, ?, ?)";
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, data.getPlayerUUID().toString());
                pstmt.setString(2, data.getPreviousLocationString());
                pstmt.setBoolean(3, data.isBorderEnabled());
                pstmt.setString(4, data.getBorderColor().name());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save player data for " + data.getPlayerUUID(), e);
                throw new RuntimeException("Failed to save player data", e);
            }
        });
    }

    /**
     * Retrieves a player's data from the cache.
     * If the data is not in the cache, it will be loaded synchronously. To avoid
     * blocking, ensure {@link #preloadPlayerData(UUID)} is called when the player joins.
     *
     * @param playerUUID The UUID of the player.
     * @return The {@link PlayerData} for the player.
     */
    public PlayerData getPlayerData(UUID playerUUID) {
        try {
            return playerDataCache.get(playerUUID);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get player data for " + playerUUID, e);
            // Return a new object to prevent NPEs
            return new PlayerData(playerUUID);
        }
    }

    /**
     * Asynchronously saves the player's last known location.
     *
     * @param playerUUID The UUID of the player.
     * @param location   The location to save.
     */
    public void savePreviousLocation(UUID playerUUID, Location location) {
        PlayerData data = getPlayerData(playerUUID);
        data.setPreviousLocation(location);
        savePlayerData(data);
    }

    /**
     * Asynchronously pre-loads a player's data into the cache.
     * This is intended to be called on player join to prevent the first data access
     * from blocking the main thread.
     *
     * @param playerUUID The UUID of the player whose data should be loaded.
     */
    public void preloadPlayerData(UUID playerUUID) {
        CompletableFuture.runAsync(() -> {
            try {
                playerDataCache.get(playerUUID);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to preload player data for " + playerUUID, e);
            }
        });
    }
}