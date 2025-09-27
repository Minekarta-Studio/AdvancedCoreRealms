package com.minekarta.advancedcorerealms.manager;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.utils.MessageUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class WorldManager {
    
    private final AdvancedCoreRealms plugin;
    
    public WorldManager(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
    }
    
    public void createWorldAsync(Player player, String worldName, String worldType) {
        // Run world creation asynchronously to avoid blocking the main thread
        CompletableFuture.runAsync(() -> {
            try {
                WorldCreator creator = WorldCreator.name(worldName);
                
                if (worldType.equalsIgnoreCase("FLAT")) {
                    creator.type(WorldType.FLAT);
                } else {
                    creator.type(WorldType.NORMAL);
                }
                
                World world = creator.createWorld();
                
                if (world != null) {
                    // Create the realm data object
                    Realm realm = new Realm(worldName, player.getUniqueId(), worldType.equalsIgnoreCase("FLAT"));
                    plugin.getWorldDataManager().addRealm(realm);
                    
                    // Send success message to player (back on main thread)
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        MessageUtils.sendMessage(player, "world.created", "%world%", worldName);
                    });
                } else {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        player.sendMessage(ChatColor.RED + "Failed to create world: " + worldName);
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
                Location mainWorldSpawn = Bukkit.getWorlds().get(0).getSpawnLocation();
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
        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
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
        Location destination = world.getSpawnLocation();
        player.teleport(destination);
        MessageUtils.sendMessage(player, "world.teleport", "%world%", worldName);
    }
    
    private boolean hasAccessToRealm(Player player, String worldName) {
        Realm realm = plugin.getWorldDataManager().getRealm(worldName);
        if (realm == null) {
            return false;
        }
        
        // Owner or member can access
        return realm.isMember(player.getUniqueId());
    }
    
    private boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
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