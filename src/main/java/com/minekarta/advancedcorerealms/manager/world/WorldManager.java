package com.minekarta.advancedcorerealms.manager.world;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

/**
 * Enhanced WorldManager that uses the modular WorldPluginManager for world creation
 * This replaces the original WorldManager to support the new architecture
 */
public class WorldManager {
    
    private final AdvancedCoreRealms plugin;
    private final WorldPluginManager worldPluginManager;
    
    public WorldManager(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.worldPluginManager = new WorldPluginManager(plugin);
    }
    
    public void createWorldAsync(Player player, String worldName, String worldType) {
        // Run world creation asynchronously to avoid blocking the main thread
        CompletableFuture.runAsync(() -> {
            try {
                WorldCreator creator = WorldCreator.name(worldName);
                
                // Set world type based on input
                if (worldType.equalsIgnoreCase("FLAT")) {
                    creator.type(WorldType.FLAT);
                } else if (worldType.equalsIgnoreCase("NORMAL")) {
                    creator.type(WorldType.NORMAL);
                } else if (worldType.equalsIgnoreCase("AMPLIFIED")) {
                    creator.type(WorldType.AMPLIFIED);
                } else {
                    // Default to normal
                    creator.type(WorldType.NORMAL);
                }
                
                // Use the WorldPluginManager to create the world with fallback system
                World world = worldPluginManager.createWorldAsync(worldName, creator, player).join();
                
                if (world != null) {
                    // Create the realm data object
                    Realm realm = new Realm(worldName, player.getUniqueId(), worldType.equalsIgnoreCase("FLAT"));
                    realm.setWorldType(worldType);
                    
                    // Add player to members list to give them access to their own realm
                    realm.addMember(player.getUniqueId());
                    
                    // Set world properties based on the type
                    if (worldType.equalsIgnoreCase("FLAT")) {
                        realm.setCreativeMode(false); // Flat worlds default to survival
                        realm.setPeacefulMode(false);
                    } else if (worldType.equalsIgnoreCase("AMPLIFIED")) {
                        realm.setCreativeMode(true); // Amplified often used for creative builds
                        realm.setPeacefulMode(false);
                    } else {
                        realm.setCreativeMode(false); // Normal worlds default to survival
                        realm.setPeacefulMode(false);
                    }
                    
                    // Set the default max players limit
                    realm.setMaxPlayers(plugin.getConfig().getInt("default-max-players", 8));
                    
                    // Add the realm to the data manager
                    plugin.getWorldDataManager().addRealm(realm);
                    
                    // Save the world data
                    plugin.getWorldDataManager().saveData();
                    
                    // Teleport the player to the new world (back on main thread)
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        teleportToRealm(player, worldName);
                        player.sendMessage(ChatColor.GREEN + "Realms successfully created! Enjoy your home sweat home...");
                    });
                } else {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        player.sendMessage(ChatColor.RED + "Failed to create world: " + worldName + 
                                         " - All creation methods exhausted");
                    });
                }
            } catch (Exception e) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage(ChatColor.RED + "Error creating world: " + e.getMessage());
                    plugin.getLogger().severe("Error creating world: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }
    
    public void deleteWorld(Player player, String worldName) {
        World world = Bukkit.getWorld(worldName);
        
        if (world != null) {
            // Teleport all players out of the world first
            for (Player worldPlayer : world.getPlayers()) {
                // Teleport to spawn or to the player's last known location in main world
                org.bukkit.Location mainWorldSpawn = Bukkit.getWorlds().get(0).getSpawnLocation();
                worldPlayer.teleport(mainWorldSpawn);
                worldPlayer.sendMessage(ChatColor.RED + "The realm you were in has been deleted!");
            }
            
            // Unload the world
            boolean unloaded = Bukkit.unloadWorld(world, true);
            if (!unloaded) {
                player.sendMessage(ChatColor.RED + "Failed to unload world before deletion");
                return;
            }
        }
        
        // Delete the world folder from disk
        java.io.File worldFolder = new java.io.File(Bukkit.getWorldContainer(), worldName);
        if (worldFolder.exists() && worldFolder.isDirectory()) {
            deleteDirectory(worldFolder);
        }
        
        // Remove from data
        plugin.getWorldDataManager().removeRealm(worldName);
        MessageUtils.sendMessage(player, "world.deleted", "%world%", worldName);
    }
    
    public void teleportToRealm(Player player, String worldName) {
        // Check if player has access to the realm
        if (!hasAccessToRealm(player, worldName)) {
            player.sendMessage(ChatColor.RED + "You don't have access to this realm!");
            return;
        }
        
        World world = Bukkit.getWorld(worldName);
        
        if (world == null) {
            // Try to load the world
            world = Bukkit.createWorld(WorldCreator.name(worldName));
            if (world == null) {
                player.sendMessage(ChatColor.RED + "Could not load the realm: " + worldName);
                return;
            }
        }
        
        // Save player's current location for /realms back command
        plugin.getPlayerDataManager().savePreviousLocation(player.getUniqueId(), player.getLocation());
        
        // Teleport player to the world
        org.bukkit.Location destination = world.getSpawnLocation();
        player.teleport(destination);
        MessageUtils.sendMessage(player, "world.teleport", "%world%", worldName);
    }
    
    public WorldPluginManager getWorldPluginManager() {
        return worldPluginManager;
    }
    
    private boolean hasAccessToRealm(Player player, String worldName) {
        Realm realm = plugin.getWorldDataManager().getRealm(worldName);
        if (realm == null) {
            return false;
        }
        
        // Owner or member can access
        return realm.isMember(player.getUniqueId());
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