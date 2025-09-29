package com.minekarta.advancedcorerealms.storage;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

/**
 * Defines the contract for a database provider, allowing for different database implementations
 * (e.g., SQLite, MongoDB) to be used interchangeably. This abstraction decouples the core plugin
 * logic from the specific details of a database implementation, promoting modularity and making
 * it easier to support multiple database types in the future.
 *
 * <p>Implementations of this interface are responsible for managing the entire lifecycle of the
 * database connection, including initialization, connection pooling, and shutdown.</p>
 */
public interface DatabaseProvider {

    /**
     * Initializes the database provider. This method should handle setting up the
     * connection pool, creating the physical database file if necessary, and preparing
     * the provider for use.
     *
     * @throws Exception if a critical error occurs during initialization that prevents
     *                   the provider from functioning, such as file permission errors.
     */
    void initialize() throws Exception;

    /**
     * Closes the database connection and releases all associated resources, such as
     * the connection pool. This method is critical for a graceful shutdown and should
     * be called when the plugin is disabled.
     */
    void close();

    /**
     * Retrieves a {@link Connection} from the database connection pool.
     * It is the caller's responsibility to close the connection in a try-with-resources
     * block to ensure it is returned to the pool.
     *
     * @return A database {@link Connection}.
     * @throws SQLException if a database access error occurs or if the pool is not initialized.
     */
    Connection getConnection() throws SQLException;

    /**
     * Asynchronously ensures that all necessary database tables and their schemas are created
     * or migrated to the latest version. This method handles both initial table creation
     * and any subsequent schema alterations (e.g., adding new columns).
     *
     * <p>This operation should be non-destructive and safe to run on every startup.</p>
     *
     * @return A {@link CompletableFuture<Void>} that completes when the operation is finished.
     *         The future will complete exceptionally if a critical error occurs during setup.
     */
    CompletableFuture<Void> setupTables();
}