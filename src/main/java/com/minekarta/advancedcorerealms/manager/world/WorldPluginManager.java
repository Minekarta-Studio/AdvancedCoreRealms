package com.minekarta.advancedcorerealms.manager.world;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Manager that handles world creation through various plugin hooks
 * Implements fallback system: Multiverse-Core -> MultiWorld -> Bukkit Native
 */
public class WorldPluginManager {
    
    private final AdvancedCoreRealms plugin;
    private final List<WorldPluginHook> hooks = new ArrayList<>();
    
    public WorldPluginManager(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        initializeHooks();
    }
    
    /**
     * Initializes all available world plugin hooks
     */
    private void initializeHooks() {
        // Add hooks in order of preference (fallback chain)
        hooks.add(new MultiverseCoreHook(plugin));
        hooks.add(new MultiWorldHook(plugin));
        hooks.add(new BukkitNativeHook(plugin));
        
        // Log which hooks are available
        plugin.getLogger().info("Initializing world management hooks...");
        for (WorldPluginHook hook : hooks) {
            if (hook.isAvailable()) {
                plugin.getLogger().info("  ✓ " + hook.getPluginName() + " integration available");
            } else {
                plugin.getLogger().info("  ✗ " + hook.getPluginName() + " integration not available");
            }
        }
    }
    
    /**
     * Creates a world using the available hooks in fallback order
     * @param worldName Name of the world to create
     * @param creator WorldCreator object with configuration
     * @param player Player creating the world
     * @return CompletableFuture that completes with the created World or null if all methods fail
     */
    public CompletableFuture<World> createWorldAsync(String worldName, WorldCreator creator, Player player) {
        CompletableFuture<World> result = new CompletableFuture<>();
        
        createWorldWithFallback(result, worldName, creator, player, 0);
        
        return result;
    }
    
    /**
     * Recursive method to try world creation with each hook in order
     */
    private void createWorldWithFallback(CompletableFuture<World> result, String worldName, WorldCreator creator, Player player, int hookIndex) {
        if (hookIndex >= hooks.size()) {
            // All hooks have been tried and failed
            plugin.getLogger().severe("All world creation methods failed for world: " + worldName);
            result.complete(null);
            return;
        }
        
        WorldPluginHook hook = hooks.get(hookIndex);
        
        if (!hook.isAvailable()) {
            plugin.getLogger().warning("Hook " + hook.getPluginName() + " is not available, skipping...");
            createWorldWithFallback(result, worldName, creator, player, hookIndex + 1);
            return;
        }
        
        // Try to create the world with this hook
        plugin.getLogger().info("Attempting to create world '" + worldName + "' using " + hook.getPluginName());
        
        CompletableFuture<World> hookResult = hook.createWorldAsync(worldName, creator, player);
        
        hookResult.whenComplete((world, throwable) -> {
            if (throwable != null) {
                plugin.getLogger().warning("World creation failed with " + hook.getPluginName() + 
                                         ": " + throwable.getMessage());
                // Try next hook in the chain
                createWorldWithFallback(result, worldName, creator, player, hookIndex + 1);
            } else if (world == null) {
                plugin.getLogger().warning("World creation returned null with " + hook.getPluginName());
                // Try next hook in the chain
                createWorldWithFallback(result, worldName, creator, player, hookIndex + 1);
            } else {
                // Success! World was created
                plugin.getLogger().info("Successfully created world '" + worldName + "' using " + hook.getPluginName());
                result.complete(world);
            }
        });
    }
    
    /**
     * Gets the first available hook that supports a specific world type
     * @param worldTypeName World type to check
     * @return Available hook that supports the type, or null if none available
     */
    public WorldPluginHook getAvailableHookForType(String worldTypeName) {
        for (WorldPluginHook hook : hooks) {
            if (hook.isAvailable() && hook.supportsWorldType(worldTypeName)) {
                return hook;
            }
        }
        return null;
    }
    
    /**
     * Gets all available hooks
     * @return List of available hooks
     */
    public List<WorldPluginHook> getAvailableHooks() {
        List<WorldPluginHook> available = new ArrayList<>();
        for (WorldPluginHook hook : hooks) {
            if (hook.isAvailable()) {
                available.add(hook);
            }
        }
        return available;
    }
    
    /**
     * Gets the first available hook
     * @return First available hook, or null if none available
     */
    public WorldPluginHook getFirstAvailableHook() {
        for (WorldPluginHook hook : hooks) {
            if (hook.isAvailable()) {
                return hook;
            }
        }
        return null;
    }
    
    public AdvancedCoreRealms getPlugin() {
        return plugin;
    }
}