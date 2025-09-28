package com.minekarta.advancedcorerealms.manager.world;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * Test class to validate the fallback system functionality
 */
public class FallbackSystemTester {
    
    private final AdvancedCoreRealms plugin;
    private final Logger logger;
    private final WorldPluginManager worldPluginManager;
    
    public FallbackSystemTester(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.worldPluginManager = plugin.getWorldManager().getWorldPluginManager();
    }
    
    /**
     * Runs tests to validate the fallback system
     */
    public void runFallbackTests(Player player) {
        logger.info("Starting fallback system tests...");
        
        // Test 1: Check available hooks
        testAvailableHooks();
        
        // Test 2: Try to create a world using the fallback system
        testWorldCreationFallback(player);
    }
    
    private void testAvailableHooks() {
        logger.info("Testing available hooks...");
        
        var availableHooks = worldPluginManager.getAvailableHooks();
        logger.info("Available hooks: " + availableHooks.size());
        
        for (WorldPluginHook hook : availableHooks) {
            logger.info("  - " + hook.getPluginName() + ": AVAILABLE");
        }
        
        // Check if the first available hook is correctly identified
        WorldPluginHook firstHook = worldPluginManager.getFirstAvailableHook();
        if (firstHook != null) {
            logger.info("First available hook: " + firstHook.getPluginName());
        } else {
            logger.warning("No hooks available!");
        }
    }
    
    private void testWorldCreationFallback(Player player) {
        logger.info("Testing world creation fallback...");
        
        String testName = "test_fallback_" + System.currentTimeMillis();
        
        // Create a world creator with basic settings
        WorldCreator creator = WorldCreator.name(testName);
        
        // Use the fallback system to create the world
        CompletableFuture<World> future = worldPluginManager.createWorldAsync(testName, creator, player);
        
        future.whenComplete((world, throwable) -> {
            if (throwable != null) {
                logger.severe("World creation failed with error: " + throwable.getMessage());
                throwable.printStackTrace();
                if (player != null) {
                    player.sendMessage(ChatColor.RED + "Fallback test failed: " + throwable.getMessage());
                }
            } else if (world == null) {
                logger.warning("World creation returned null");
                if (player != null) {
                    player.sendMessage(ChatColor.YELLOW + "Fallback test: World creation returned null");
                }
            } else {
                logger.info("World creation successful: " + world.getName());
                if (player != null) {
                    player.sendMessage(ChatColor.GREEN + "Fallback test successful! World '" + 
                                     world.getName() + "' created using " + 
                                     getHookUsedForWorld(world.getName()));
                }
                
                // Clean up: delete the test world
                cleanupTestWorld(world.getName());
            }
        });
    }
    
    private String getHookUsedForWorld(String worldName) {
        // This is a simple way to determine which hook was likely used
        // In a real implementation, you might track this differently
        if (Bukkit.getWorld(worldName) != null) {
            return "Active world - created by one of the available hooks";
        }
        return "Unknown";
    }
    
    private void cleanupTestWorld(String worldName) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                // Attempt to unload the world
                boolean unloaded = Bukkit.unloadWorld(world, true);
                if (unloaded) {
                    logger.info("Cleaned up test world: " + worldName);
                } else {
                    logger.warning("Failed to unload test world: " + worldName);
                }
                
                // Delete the world folder from disk if it exists
                java.io.File worldFolder = new java.io.File(Bukkit.getWorldContainer(), worldName);
                if (worldFolder.exists() && worldFolder.isDirectory()) {
                    deleteDirectory(worldFolder);
                }
            }
        }, 20L * 5); // Wait 5 seconds before cleanup
    }
    
    private boolean deleteDirectory(java.io.File directory) {
        if (directory.exists()) {
            java.io.File[] files = directory.listFiles();
            if (files != null) {
                for (java.io.File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            return directory.delete();
        }
        return false;
    }
}