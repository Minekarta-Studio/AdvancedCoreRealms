package com.minekarta.advancedcorerealms.manager.world;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.manager.LanguageManager;
import com.minekarta.advancedcorerealms.manager.RealmManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
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
     * Asynchronously deletes a realm's world from the correct 'realms' subdirectory.
     *
     * @param realm The realm whose world is to be deleted.
     * @return A CompletableFuture that completes with true if successful, false otherwise.
     */
    public CompletableFuture<Boolean> deleteWorld(Realm realm) {
        final String worldPath = "realms/" + realm.getWorldFolderName();
        CompletableFuture<Boolean> unloadFuture = new CompletableFuture<>();

        // Unloading must happen on the main thread
        Bukkit.getScheduler().runTask(plugin, () -> {
            World world = Bukkit.getWorld(worldPath);
            if (world != null) {
                world.getPlayers().forEach(p -> {
                    p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                    languageManager.sendMessage(p, "realm.world_deleted_kick", "%world%", realm.getName());
                });

                if (Bukkit.unloadWorld(world, true)) {
                    plugin.getLogger().info("Successfully unloaded realm world: " + worldPath);
                    unloadFuture.complete(true);
                } else {
                    plugin.getLogger().severe("Failed to unload realm world: " + worldPath);
                    unloadFuture.complete(false);
                }
            } else {
                unloadFuture.complete(true);
            }
        });

        return unloadFuture.thenComposeAsync(unloaded -> {
            if (!unloaded) {
                return CompletableFuture.completedFuture(false);
            }

            // Delete world files from the correct path
            File worldFolder = new File(Bukkit.getWorldContainer(), worldPath);
            if (worldFolder.exists()) {
                try {
                    deleteDirectory(worldFolder.toPath());
                    plugin.getLogger().info("Successfully deleted world folder: " + worldPath);
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to delete world folder: " + worldPath, e);
                    return CompletableFuture.completedFuture(false);
                }
            }
            return CompletableFuture.completedFuture(true);
        });
    }

    /**
     * Teleports a player to a realm, loading the world from the correct 'realms' subdirectory if needed.
     *
     * @param player The player to teleport.
     * @param realmName The display name of the realm.
     */
    public void teleportToRealm(Player player, String realmName) {
        Optional<Realm> optionalRealm = realmManager.getRealmByName(realmName);

        if (optionalRealm.isEmpty()) {
            languageManager.sendMessage(player, "error.realm_not_found");
            return;
        }

        Realm realm = optionalRealm.get();
        final String worldPath = "realms/" + realm.getWorldFolderName();

        if (!realm.isMember(player.getUniqueId()) && !player.hasPermission("advancedcorerealms.admin.teleport.any")) {
            languageManager.sendMessage(player, "error.no_access_to_realm");
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            World world = Bukkit.getWorld(worldPath);
            if (world == null) {
                languageManager.sendMessage(player, "world.loading", "%world%", realm.getName());
                world = new WorldCreator(worldPath).createWorld();
                if (world == null) {
                    languageManager.sendMessage(player, "error.world_load_failed", "%world%", realm.getName());
                    return;
                }
            }

            realmManager.savePreviousLocation(player.getUniqueId(), player.getLocation());
            player.teleport(world.getSpawnLocation());
            languageManager.sendMessage(player, "world.teleport", "%world%", realm.getName());
            plugin.getRealmInventoryService().enterRealm(player, realm);
        });
    }

    public WorldPluginManager getWorldPluginManager() {
        return worldPluginManager;
    }

    private void deleteDirectory(Path path) throws IOException {
        if (Files.exists(path)) {
            try (var walk = Files.walk(path)) {
                walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                        plugin.getLogger().log(Level.SEVERE, "Failed to delete path: " + p, e);
                    }
                });
            }
        }
    }
}