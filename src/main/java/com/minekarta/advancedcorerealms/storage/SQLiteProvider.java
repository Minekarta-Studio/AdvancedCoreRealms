package com.minekarta.advancedcorerealms.storage;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * An implementation of {@link DatabaseProvider} for SQLite.
 * This class handles all SQLite-specific logic, including:
 * <ul>
 *     <li>Creating the {@code realms.db} database file.</li>
 *     <li>Configuring and managing a {@link HikariDataSource} for efficient connection pooling.</li>
 *     <li>Executing the SQL statements required to create and migrate the database schema.</li>
 * </ul>
 * All potentially long-running operations, such as table setup, are executed asynchronously.
 */
public class SQLiteProvider implements DatabaseProvider {

    private final AdvancedCoreRealms plugin;
    private HikariDataSource dataSource;

    public SQLiteProvider(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() throws IOException {
        File dbFile = new File(plugin.getDataFolder(), "realms.db");
        if (!dbFile.exists()) {
            dbFile.getParentFile().mkdirs();
            if (!dbFile.createNewFile()) {
                throw new IOException("Failed to create database file: " + dbFile.getAbsolutePath());
            }
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        config.setPoolName("AdvancedCoreRealms-Pool");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        // Recommended settings for SQLite
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.dataSource = new HikariDataSource(config);
        plugin.getLogger().info("SQLite database connection pool initialized.");
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("SQLite database connection pool closed.");
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Database source is not initialized.");
        }
        return dataSource.getConnection();
    }

    @Override
    public CompletableFuture<Void> setupTables() {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
                // Realms Table
                stmt.execute("CREATE TABLE IF NOT EXISTS realms (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT NOT NULL UNIQUE," +
                        "owner_uuid TEXT NOT NULL," +
                        "world_name TEXT NOT NULL UNIQUE," +
                        "template TEXT NOT NULL," +
                        "created_at TEXT NOT NULL," +
                        "is_flat BOOLEAN NOT NULL," +
                        "difficulty TEXT DEFAULT 'normal'," +
                        "keep_loaded BOOLEAN DEFAULT FALSE," +
                        "border_tier_id TEXT DEFAULT 'tier_50'," +
                        "member_slot_tier_id TEXT DEFAULT 'tier_0'," +
                        "border_size INTEGER DEFAULT 100," +
                        "max_players INTEGER DEFAULT 8," +
                        "border_center_x REAL DEFAULT 0.0," +
                        "border_center_z REAL DEFAULT 0.0" +
                        ");");

                // Members Table
                stmt.execute("CREATE TABLE IF NOT EXISTS realm_members (" +
                        "realm_id INTEGER NOT NULL," +
                        "player_uuid TEXT NOT NULL," +
                        "role TEXT NOT NULL," +
                        "PRIMARY KEY (realm_id, player_uuid)," +
                        "FOREIGN KEY (realm_id) REFERENCES realms(id) ON DELETE CASCADE" +
                        ");");

                // Player Data Table
                stmt.execute("CREATE TABLE IF NOT EXISTS player_data (" +
                        "player_uuid TEXT PRIMARY KEY," +
                        "previous_location TEXT," +
                        "border_enabled BOOLEAN DEFAULT TRUE," +
                        "border_color TEXT DEFAULT 'BLUE'" +
                        ");");

                plugin.getLogger().info("Database tables created or verified successfully.");
                updateSchema(conn);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create or verify database tables.", e);
                throw new RuntimeException("Failed to set up database tables", e);
            }
        }, runnable -> plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable));
    }

    /**
     * Applies non-destructive schema updates to an existing database.
     * This method is responsible for bringing older database schemas up to date
     * without losing data by adding new columns if they do not already exist.
     *
     * @param conn The active database connection.
     * @throws SQLException if a database error occurs.
     */
    private void updateSchema(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            if (!columnExists(conn, "realms", "border_center_x")) {
                stmt.execute("ALTER TABLE realms ADD COLUMN border_center_x REAL DEFAULT 0.0;");
                plugin.getLogger().info("Schema updated: Added border_center_x to realms table.");
            }
            if (!columnExists(conn, "realms", "border_center_z")) {
                stmt.execute("ALTER TABLE realms ADD COLUMN border_center_z REAL DEFAULT 0.0;");
                plugin.getLogger().info("Schema updated: Added border_center_z to realms table.");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to update database schema.", e);
            // We re-throw here to ensure the setup future completes exceptionally
            throw e;
        }
    }

    /**
     * Checks if a specific column exists within a given table.
     *
     * @param conn       The active database connection.
     * @param tableName  The name of the table to check.
     * @param columnName The name of the column to check for.
     * @return {@code true} if the column exists, {@code false} otherwise.
     * @throws SQLException if a database access error occurs.
     */
    private boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, tableName, columnName)) {
            return rs.next();
        }
    }
}