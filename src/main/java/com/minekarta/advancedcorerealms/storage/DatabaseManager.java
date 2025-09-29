package com.minekarta.advancedcorerealms.storage;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

/**
 * Manages the plugin's database connection using HikariCP for efficient connection pooling.
 * This class handles the initialization of the SQLite database, table creation, and connection lifecycle.
 */
public class DatabaseManager {

    private final AdvancedCoreRealms plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
    }

    /**
     * Initializes the database connection pool.
     * Sets up the HikariCP configuration, creates the database file if it doesn't exist,
     * and asynchronously creates the necessary database tables.
     */
    public void initialize() {
        File dbFile = new File(plugin.getDataFolder(), "realms.db");
        if (!dbFile.exists()) {
            try {
                dbFile.getParentFile().mkdirs();
                if (!dbFile.createNewFile()) {
                    throw new IOException("Failed to create database file.");
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create database file: " + dbFile.getAbsolutePath(), e);
                return;
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

        this.dataSource = new HikariDataSource(config);
        plugin.getLogger().info("Database connection pool initialized.");

        createTables();
    }

    /**
     * Closes the database connection pool safely.
     * This should be called when the plugin is disabled to release all database resources.
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("Database connection pool closed.");
        }
    }

    /**
     * Gets a connection from the connection pool.
     *
     * @return A database {@link Connection}.
     * @throws SQLException if a database access error occurs.
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Asynchronously creates the database tables if they do not already exist.
     */
    private void createTables() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
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
                        "max_players INTEGER DEFAULT 8" +
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
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create database tables.", e);
            }
        });
    }
}