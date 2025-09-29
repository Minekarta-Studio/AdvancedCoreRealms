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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Manages all data persistence and caching for {@link Realm} and {@link PlayerData} objects.
 * This class provides a fully asynchronous API for database interactions to ensure the
 * server's main thread is never blocked. It uses a dual-caching strategy for high performance.
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
                        return loadPlayerData(playerUUID).get();
                    }
                });
    }

    public CompletableFuture<Void> createRealm(Realm realm) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT INTO realms (name, owner_uuid, world_name, template, created_at, is_flat, difficulty, keep_loaded, border_tier_id, member_slot_tier_id, border_size, max_players) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
                pstmt.executeUpdate();
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int realmId = generatedKeys.getInt(1);
                        realm.setId(realmId);
                        addMemberToRealm(realm, realm.getOwner(), Role.OWNER).join(); // This is acceptable here as we are already on an async thread.
                    }
                }
                invalidateCaches(realm);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create realm in database", e);
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Realm> getRealmByName(String name) {
        return realmNameCache.getUnchecked(name);
    }

    public CompletableFuture<Realm> getRealmByWorldName(String worldName) {
        Realm realm = worldNameCache.getIfPresent(worldName);
        if (realm != null) {
            return CompletableFuture.completedFuture(realm);
        }
        return getRealmByNameUncached(worldName); // Fallback to DB if not in world cache
    }

    public Optional<Realm> getRealmFromCacheByWorld(String worldName) {
        return Optional.ofNullable(worldNameCache.getIfPresent(worldName));
    }

    public Optional<Realm> getRealmFromCacheByName(String realmName) {
        try {
            CompletableFuture<Realm> future = realmNameCache.getIfPresent(realmName);
            if (future != null && future.isDone() && !future.isCompletedExceptionally()) {
                return Optional.ofNullable(future.getNow(null));
            }
        } catch (Exception e) {
            // This shouldn't happen with getNow, but we log just in case.
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
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error getting realm by name uncached: " + name, e);
            }
            return null;
        });
    }

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
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error getting realms by owner: " + ownerUuid, e);
            }
            return realms;
        });
    }

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
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error getting invited realms: " + playerUuid, e);
            }
            return realms;
        });
    }

    public CompletableFuture<Void> updateRealm(Realm realm) {
        return CompletableFuture.runAsync(() -> {
            String sql = "UPDATE realms SET owner_uuid = ?, difficulty = ?, keep_loaded = ?, border_tier_id = ?, member_slot_tier_id = ?, border_size = ?, max_players = ? WHERE name = ?";
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, realm.getOwner().toString());
                pstmt.setString(2, realm.getDifficulty());
                pstmt.setBoolean(3, realm.isKeepLoaded());
                pstmt.setString(4, realm.getBorderTierId());
                pstmt.setString(5, realm.getMemberSlotTierId());
                pstmt.setInt(6, realm.getBorderSize());
                pstmt.setInt(7, realm.getMaxPlayers());
                pstmt.setString(8, realm.getName());
                pstmt.executeUpdate();
                invalidateCaches(realm);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not update realm: " + realm.getName(), e);
            }
        });
    }

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
            }
        });
    }

    public CompletableFuture<Void> addMemberToRealm(Realm realm, UUID memberUuid, Role role) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT OR REPLACE INTO realm_members (realm_id, player_uuid, role) VALUES (?, ?, ?)";
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, realm.getId());
                pstmt.setString(2, memberUuid.toString());
                pstmt.setString(3, role.name());
                pstmt.executeUpdate();
                invalidateCaches(realm); // Invalidate cache after modification
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not add member to realm: " + realm.getId(), e);
            }
        });
    }

    public CompletableFuture<Void> removeMemberFromRealm(Realm realm, UUID memberUuid) {
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM realm_members WHERE realm_id = ? AND player_uuid = ?";
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, realm.getId());
                pstmt.setString(2, memberUuid.toString());
                pstmt.executeUpdate();
                invalidateCaches(realm); // Invalidate cache after modification
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not remove member from realm: " + realm.getId(), e);
            }
        });
    }

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
        return realm;
    }

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
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load player data for " + playerUUID, e);
            }
            return new PlayerData(playerUUID);
        });
    }

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
            }
        });
    }

    public PlayerData getPlayerData(UUID playerUUID) {
        try {
            return playerDataCache.get(playerUUID);
        } catch (ExecutionException e) {
            plugin.getLogger().log(Level.SEVERE, "Error retrieving player data from cache for " + playerUUID, e);
            return new PlayerData(playerUUID);
        }
    }

    public void savePreviousLocation(UUID playerUUID, Location location) {
        try {
            PlayerData data = playerDataCache.get(playerUUID);
            data.setPreviousLocation(location);
            savePlayerData(data);
        } catch (ExecutionException e) {
            plugin.getLogger().log(Level.SEVERE, "Error saving previous location for " + playerUUID, e);
        }
    }
}