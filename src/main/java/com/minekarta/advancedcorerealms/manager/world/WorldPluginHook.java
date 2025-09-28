package com.minekarta.advancedcorerealms.manager.world;

import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for integrating with external world management plugins
 */
public interface WorldPluginHook {
    
    /**
     * Gets the name of the plugin this hook integrates with
     * @return Plugin name
     */
    String getPluginName();
    
    /**
     * Checks if the required plugin is available on the server
     * @return true if plugin is available and loaded
     */
    boolean isAvailable();
    
    /**
     * Creates a world using this plugin's API
     * @param worldName Name of the world to create
     * @param creator WorldCreator object with configuration
     * @param player Player creating the world (for logging/permissions)
     * @return CompletableFuture that completes with the created World or null if failed
     */
    CompletableFuture<World> createWorldAsync(String worldName, WorldCreator creator, Player player);
    
    /**
     * Checks if this hook supports a specific world type
     * @param worldTypeName World type to check
     * @return true if supported
     */
    boolean supportsWorldType(String worldTypeName);
}