package com.minekarta.advancedcorerealms.manager.world;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.manager.LanguageManager;
import com.minekarta.advancedcorerealms.manager.RealmManager;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class WorldManager {

    private final AdvancedCoreRealms plugin;
    private final WorldPluginManager worldPluginManager;
    private final RealmManager realmManager;
    private final LanguageManager languageManager;

    public WorldManager(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.worldPluginManager = new WorldPluginManager(plugin);
        this.realmManager = plugin.getRealmManager();
        this.languageManager = plugin.getLanguageManager();
    }

    /**
     * Asynchronously deletes a realm, including teleporting players, unloading the world,
     * deleting world files, and removing the database entry.
     *
     * @param realm The realm to be deleted.
     * @return A CompletableFuture that completes with true if successful, false otherwise.
     */
    public CompletableFuture<Boolean> deleteWorld(Realm realm) {
        return CompletableFuture.supplyAsync(() -> {
            // Step 1: Unload the world (must run on main thread)
            try {
                return Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                    World world = Bukkit.getWorld(realm.getWorldName());
                    if (world != null) {
                        world.getPlayers().forEach(p -> {
                            p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                            languageManager.sendMessage(p, "realm.world_deleted_kick", "%world%", realm.getName());
                        });
                        return Bukkit.unloadWorld(world, true);
                    }
                    return true; // World wasn't loaded, so it's "successfully" unloaded
                }).get();
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Exception while unloading world " + realm.getWorldName(), e);
                return false;
            }
        }).thenComposeAsync(unloaded -> {
            if (!unloaded) {
                return CompletableFuture.completedFuture(false);
            }

            // Step 2: Delete world files (async)
            try {
                File worldFolder = new File(Bukkit.getWorldContainer(), realm.getWorldName());
                if (worldFolder.exists()) {
                    deleteDirectory(worldFolder);
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to delete world folder: " + realm.getWorldName(), e);
                return CompletableFuture.completedFuture(false);
            }

            // Step 3: Delete database record (async)
            return realmManager.deleteRealm(realm.getName()).thenApply(v -> true);
        });
    }


    public void teleportToRealm(Player player, String worldName) {
        realmManager.getRealmByWorldName(worldName).thenAccept(realm -> {
            if (realm == null) {
                languageManager.sendMessage(player, "error.realm_not_found");
                return;
            }

            if (!realm.isMember(player.getUniqueId()) && !player.hasPermission("advancedcorerealms.admin.teleport.any")) {
                languageManager.sendMessage(player, "error.no_access_to_realm");
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    languageManager.sendMessage(player, "world.loading", "%world%", worldName);
                    world = Bukkit.createWorld(new WorldCreator(worldName));
                    if (world == null) {
                        languageManager.sendMessage(player, "error.world_load_failed", "%world%", worldName);
                        return;
                    }
                }

                realmManager.savePreviousLocation(player.getUniqueId(), player.getLocation());
                player.teleport(world.getSpawnLocation());
                languageManager.sendMessage(player, "world.teleport", "%world%", worldName);
                plugin.getRealmInventoryService().enterRealm(player, realm);
            });
        });
    }

    public WorldPluginManager getWorldPluginManager() {
        return worldPluginManager;
    }

    private void deleteDirectory(File directory) throws IOException {
        if (!directory.exists()) return;
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        if (!directory.delete()) {
            throw new IOException("Failed to delete " + directory);
        }
    }

}