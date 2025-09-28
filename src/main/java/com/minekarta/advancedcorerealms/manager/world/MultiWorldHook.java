package com.minekarta.advancedcorerealms.manager.world;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

/**
 * WorldPluginHook implementation for MultiWorld integration
 * Uses reflection to avoid compile-time dependency on MultiWorld
 */
public class MultiWorldHook implements WorldPluginHook {
    
    private final AdvancedCoreRealms plugin;
    private Object multiWorldPlugin;
    private boolean isAvailable = false;
    
    public MultiWorldHook(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        initializeMultiWorld();
    }
    
    private void initializeMultiWorld() {
        try {
            // Check if MultiWorld plugin is loaded
            org.bukkit.plugin.Plugin multiWorldBukkitPlugin = Bukkit.getServer().getPluginManager().getPlugin("MultiWorld");
            if (multiWorldBukkitPlugin != null && multiWorldBukkitPlugin.isEnabled()) {
                this.multiWorldPlugin = multiWorldBukkitPlugin;
                this.isAvailable = true;
                plugin.getLogger().info("MultiWorld integration initialized");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Could not initialize MultiWorld integration: " + e.getMessage());
            this.isAvailable = false;
        }
    }
    
    @Override
    public String getPluginName() {
        return "MultiWorld";
    }
    
    @Override
    public boolean isAvailable() {
        return isAvailable && multiWorldPlugin != null;
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
                // Use reflection to call MultiWorld's world creation methods
                Class<?> multiWorldClass = multiWorldPlugin.getClass();
                Object multiWorldInstance = multiWorldPlugin;
                
                // Try to find and call createWorld method
                // This is based on common MultiWorld API patterns
                boolean worldCreated = createWorldWithReflection(multiWorldInstance, worldName, creator);
                
                if (worldCreated) {
                    // Attempt to load the world after creation
                    World world = Bukkit.getWorld(worldName);
                    if (world == null) {
                        // If not loaded, try to load it
                        world = Bukkit.createWorld(WorldCreator.name(worldName));
                    }
                    future.complete(world);
                } else {
                    plugin.getLogger().warning("MultiWorld failed to create world: " + worldName);
                    future.complete(null);
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Error creating world with MultiWorld: " + e.getMessage());
                e.printStackTrace();
                future.completeExceptionally(e);
            }
        });
        
        return future;
    }
    
    /**
     * Attempt to create world using reflection on MultiWorld methods
     */
    private boolean createWorldWithReflection(Object multiWorldInstance, String worldName, WorldCreator creator) {
        try {
            // Look for various possible method signatures in MultiWorld
            Class<?>[] paramTypes = new Class<?>[] { String.class, String.class, String.class, String.class };
            
            // Common method pattern in MultiWorld: createWorld(String name, String seed, String generator, String type)
            String generatorName = (creator.generator() != null) ? creator.generator().toString() : "null";
            String environment = creator.environment().name();
            String type = creator.type().name();
            
            // Try different method signatures that MultiWorld might have
            return attemptWorldCreation(multiWorldInstance, worldName, creator, generatorName, environment, type);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Could not create world with MultiWorld via reflection: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Attempt world creation with different possible method patterns
     */
    private boolean attemptWorldCreation(Object multiWorldInstance, String worldName, WorldCreator creator, String generator, String environment, String type) {
        // Since MultiWorld implementations vary, we'll try to call a generic command
        // to create the world using Bukkit's command system if the plugin provides one
        boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
            "mw create " + worldName + " " + environment.toLowerCase() + " " + type.toLowerCase());
        
        if (success) {
            return true;
        }
        
        // If command didn't work, try direct API calls using reflection
        // Try common MultiWorld method names
        try {
            // Try getPlugin().getAPI().createWorld(...)
            Class<?> pluginClass = multiWorldInstance.getClass();
            Method getAPI = findMethod(pluginClass, "getAPI");
            if (getAPI != null) {
                Object api = getAPI.invoke(multiWorldInstance);
                if (api != null) {
                    // Try various createWorld method patterns in the API
                    Method createWorld = findMethod(api.getClass(), "createWorld", 
                        String.class, Long.TYPE, String.class, String.class);
                    if (createWorld != null) {
                        return (Boolean) createWorld.invoke(api, worldName, creator.seed(), 
                            creator.type().name(), 
                            (creator.generator() != null) ? creator.generator().toString() : "null");
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Could not use MultiWorld API directly: " + e.getMessage());
        }
        
        // If API methods don't work, try fallback to command
        return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
            "mw create " + worldName + " " + environment.toLowerCase());
    }
    
    /**
     * Helper method to find a method using reflection
     */
    private Method findMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        try {
            return clazz.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            // Try with different parameter combinations
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
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
        // MultiWorld typically supports standard world types
        return true; // In practice, you might want to be more specific based on the specific MultiWorld version
    }
}