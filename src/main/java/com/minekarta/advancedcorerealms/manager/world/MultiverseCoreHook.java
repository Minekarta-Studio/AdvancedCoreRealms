package com.minekarta.advancedcorerealms.manager.world;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

/**
 * WorldPluginHook implementation for Multiverse-Core integration
 * Uses reflection to avoid compile-time dependency on Multiverse-Core
 */
public class MultiverseCoreHook implements WorldPluginHook {
    
    private final AdvancedCoreRealms plugin;
    private Object multiverseCore;
    private boolean isAvailable = false;
    
    public MultiverseCoreHook(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        initializeMultiverseCore();
    }
    
    private void initializeMultiverseCore() {
        try {
            // Check if Multiverse-Core plugin is loaded
            org.bukkit.plugin.Plugin mvCore = Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");
            if (mvCore != null && mvCore.isEnabled()) {
                this.multiverseCore = mvCore;
                this.isAvailable = true;
                plugin.getLogger().info("Multiverse-Core integration initialized");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Could not initialize Multiverse-Core integration: " + e.getMessage());
            this.isAvailable = false;
        }
    }
    
    @Override
    public String getPluginName() {
        return "Multiverse-Core";
    }
    
    @Override
    public boolean isAvailable() {
        return isAvailable && multiverseCore != null;
    }
    
    @Override
    public CompletableFuture<World> createWorldAsync(String worldName, WorldCreator creator, Player player) {
        CompletableFuture<World> future = new CompletableFuture<>();
        
        if (!isAvailable()) {
            future.complete(null);
            return future;
        }
        
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                // Use reflection to call Multiverse-Core's world creation methods
                boolean worldCreated = createWorldWithReflection(worldName, creator);
                
                if (worldCreated) {
                    // Attempt to load the world after creation
                    World world = Bukkit.getWorld(worldName);
                    if (world == null) {
                        // If not loaded, try to load it using Bukkit's createWorld if needed
                        // But usually Multiverse will handle loading
                        world = Bukkit.getWorld(worldName); // Try again after delay
                    }
                    future.complete(world);
                } else {
                    plugin.getLogger().warning("Multiverse-Core failed to create world: " + worldName);
                    future.complete(null);
                }
            } catch (Exception e) {
                plugin.getLogger().log(java.util.logging.Level.SEVERE, "Error creating world with Multiverse-Core: " + e.getMessage(), e);
                future.completeExceptionally(e);
            }
        });
        
        return future;
    }
    
    /**
     * Attempt to create world using reflection on Multiverse-Core methods
     */
    private boolean createWorldWithReflection(String worldName, WorldCreator creator) {
        try {
            // Try to create world using Multiverse-Core commands
            // Multiverse-Core typically provides commands to create worlds
            String command = "mv create " + worldName + " " + creator.environment().name().toLowerCase();
            
            // Add seed if specified
            if (creator.seed() != 0) {
                command += " -s " + creator.seed();
            }
            
            // Add generator if specified
            if (creator.generator() != null) {
                command += " -g " + creator.generator().toString();
            }
            
            boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            return success;
            
        } catch (Exception e) {
            plugin.getLogger().warning("Could not create world with Multiverse-Core via command: " + e.getMessage());
            
            // Try using reflection to access API directly
            try {
                Class<?> mvCoreClass = multiverseCore.getClass();
                
                // Try to access MVWorldManager via reflection
                java.lang.reflect.Method getMVWorldManager = findMethod(mvCoreClass, "getMVWorldManager");
                if (getMVWorldManager != null) {
                    Object worldManager = getMVWorldManager.invoke(multiverseCore);
                    if (worldManager != null) {
                        // Try to call addWorld method with reflection
                        Class<?>[] paramTypes = {String.class, org.bukkit.World.Environment.class, 
                                               String.class, org.bukkit.WorldType.class, String.class, int.class};
                        java.lang.reflect.Method addWorld = findMethod(worldManager.getClass(), "addWorld", paramTypes);
                        
                        if (addWorld != null) {
                            return (Boolean) addWorld.invoke(worldManager, 
                                worldName, 
                                creator.environment(),
                                String.valueOf(creator.seed()),
                                creator.type(),
                                (creator.generator() != null) ? creator.generator().toString() : null,
                                1); // difficulty parameter
                        }
                    }
                }
            } catch (Exception apiException) {
                plugin.getLogger().warning("Could not create world with Multiverse-Core via API: " + apiException.getMessage());
            }
            
            return false;
        }
    }
    
    /**
     * Helper method to find a method using reflection
     */
    private java.lang.reflect.Method findMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        try {
            return clazz.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            // Try with different parameter combinations or method names
            java.lang.reflect.Method[] methods = clazz.getMethods();
            for (java.lang.reflect.Method method : methods) {
                if (method.getName().equalsIgnoreCase(methodName)) {
                    Class<?>[] methodParamTypes = method.getParameterTypes();
                    if (methodParamTypes.length == paramTypes.length) {
                        boolean match = true;
                        for (int i = 0; i < paramTypes.length; i++) {
                            if (!isAssignableFrom(methodParamTypes[i], paramTypes[i])) {
                                match = false;
                                break;
                            }
                        }
                        if (match) {
                            return method;
                        }
                    }
                }
            }
            return null;
        }
    }
    
    /**
     * Check if one class can be assigned from another (for reflection method matching)
     */
    private boolean isAssignableFrom(Class<?> superClass, Class<?> subClass) {
        if (superClass == subClass) {
            return true;
        }
        if (superClass.isAssignableFrom(subClass)) {
            return true;
        }
        // Handle primitive-wrapper relationships
        if (superClass.isPrimitive() && subClass.isAssignableFrom(getWrapperClass(superClass))) {
            return true;
        }
        if (subClass.isPrimitive() && superClass.isAssignableFrom(getWrapperClass(subClass))) {
            return true;
        }
        return false;
    }
    
    /**
     * Get wrapper class for primitive types
     */
    private Class<?> getWrapperClass(Class<?> primitive) {
        if (primitive == int.class) return Integer.class;
        if (primitive == long.class) return Long.class;
        if (primitive == double.class) return Double.class;
        if (primitive == float.class) return Float.class;
        if (primitive == boolean.class) return Boolean.class;
        if (primitive == byte.class) return Byte.class;
        if (primitive == char.class) return Character.class;
        if (primitive == short.class) return Short.class;
        return primitive;
    }
    
    @Override
    public boolean supportsWorldType(String worldTypeName) {
        // Multiverse-Core supports standard world types
        try {
            org.bukkit.WorldType.valueOf(worldTypeName.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}