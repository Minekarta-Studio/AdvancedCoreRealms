package com.minekarta.advancedcorerealms.manager.world;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

/**
 * WorldPluginHook implementation for native Bukkit world creation
 * Used as fallback when other plugins are not available
 */
public class BukkitNativeHook implements WorldPluginHook {
    
    private final AdvancedCoreRealms plugin;
    
    public BukkitNativeHook(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getPluginName() {
        return "Bukkit Native";
    }
    
    @Override
    public boolean isAvailable() {
        // Bukkit is always available since we're running on Bukkit/Spigot/Paper
        return true;
    }
    
    @Override
    public CompletableFuture<World> createWorldAsync(String worldName, WorldCreator creator, Player player) {
        CompletableFuture<World> future = new CompletableFuture<>();
        
        // Schedule the world creation on the main thread
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                // Configure the WorldCreator with the provided parameters
                WorldCreator worldCreator = WorldCreator.name(worldName)
                        .environment(creator.environment())
                        .type(creator.type())
                        .seed(creator.seed());

                if (creator.generator() != null) {
                    worldCreator.generator(creator.generator());
                }

                // Create the world using Bukkit API
                World world = worldCreator.createWorld();

                if (world != null) {
                    plugin.getLogger().info("Successfully created world '" + worldName + "' using Bukkit native API");
                    future.complete(world);
                } else {
                    plugin.getLogger().warning("Bukkit native API failed to create world: " + worldName);
                    future.complete(null);
                }
            } catch (Exception e) {
                plugin.getLogger().log(java.util.logging.Level.SEVERE, "Error creating world with Bukkit native API: " + e.getMessage(), e);
                future.completeExceptionally(e);
            }
        });
        
        return future;
    }
    
    @Override
    public boolean supportsWorldType(String worldTypeName) {
        // Bukkit supports standard world types
        try {
            org.bukkit.WorldType.valueOf(worldTypeName.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}