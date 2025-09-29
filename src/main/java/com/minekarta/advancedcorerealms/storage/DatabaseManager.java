package com.minekarta.advancedcorerealms.storage;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * Manages the plugin's database operations by delegating to a specific {@link DatabaseProvider}.
 * This class acts as a high-level controller, responsible for selecting the appropriate database
 * provider based on the plugin's configuration and managing its lifecycle. It abstracts away the
 * specific database implementation from the rest of the plugin.
 *
 * <p><b>Lifecycle and Error Handling:</b></p>
 * <p>During plugin startup, the {@link #initialize()} method selects and initializes a
 * {@link DatabaseProvider}. If a critical failure occurs during this process (e.g., inability
 * to connect to the database or create tables), this manager will log the error and
 * automatically disable the plugin to prevent data corruption or unexpected behavior.
 * This fail-fast approach ensures the server runs in a stable state.</p>
 *
 * <p>Currently, it defaults to {@link SQLiteProvider}. In the future, this class can be
 * expanded to support other providers like MongoDB by adding a selection logic in
 * {@code initialize()}.</p>
 */
public class DatabaseManager {

    private final AdvancedCoreRealms plugin;
    private DatabaseProvider provider;

    public DatabaseManager(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
    }

    /**
     * Initializes the database connection by selecting and setting up the appropriate provider.
     * It also triggers the asynchronous creation and migration of database tables. If this
     * process fails, the plugin will be disabled.
     */
    public void initialize() {
        // In a future update, this section can be expanded to select a provider
        // based on the plugin's configuration file (e.g., "sqlite", "mongodb").
        this.provider = new SQLiteProvider(plugin);

        try {
            provider.initialize();
            plugin.getLogger().info("Database provider initialized successfully.");

            // Asynchronously set up tables and handle critical failure.
            provider.setupTables().exceptionally(ex -> {
                plugin.getLogger().log(Level.SEVERE, "CRITICAL: Could not set up database tables. The plugin will be disabled.", ex);
                plugin.getServer().getPluginManager().disablePlugin(plugin);
                return null;
            });
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "CRITICAL: Failed to initialize the database provider. The plugin will be disabled.", e);
            this.provider = null; // Ensure provider is null if initialization fails
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }

    /**
     * Closes the active database connection pool safely by delegating to the provider.
     * This should be called when the plugin is disabled to release all database resources.
     */
    public void close() {
        if (provider != null) {
            provider.close();
            plugin.getLogger().info("Database provider closed.");
        }
    }

    /**
     * Gets a connection from the active provider's connection pool.
     *
     * @return A database {@link Connection}.
     * @throws SQLException if the provider is not initialized or a database access error occurs.
     */
    public Connection getConnection() throws SQLException {
        if (provider == null) {
            throw new SQLException("Database provider is not initialized or failed to initialize.");
        }
        return provider.getConnection();
    }

    /**
     * Gets the currently active database provider.
     *
     * @return The active {@link DatabaseProvider} instance, or {@code null} if initialization failed.
     */
    public DatabaseProvider getProvider() {
        return provider;
    }
}