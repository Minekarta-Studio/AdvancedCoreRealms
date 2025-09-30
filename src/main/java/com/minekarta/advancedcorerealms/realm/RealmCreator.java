package com.minekarta.advancedcorerealms.realm;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.manager.RealmManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class RealmCreator {

    private final AdvancedCoreRealms plugin;
    private final RealmManager realmManager;

    public RealmCreator(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.realmManager = plugin.getRealmManager();
    }

    /**
     * Asynchronously creates a new realm.
     * This process involves checking for name availability, copying a world template,
     * creating the world, saving the realm data, and teleporting the player.
     *
     * @param player The player creating the realm.
     * @param realmName The desired name for the realm.
     * @param templateType The template to use for the world.
     */
    public void createRealmAsync(Player player, String realmName, String templateType) {
        // 1. Check if realm name is already taken
        if (realmManager.doesRealmExist(realmName)) {
            plugin.getLanguageManager().sendMessage(player, "error.realm_name_taken");
            return;
        }

        // Use a lock file based on player UUID to prevent concurrent creation by the same player
        File lockFile = new File(plugin.getDataFolder(), "creating_" + player.getUniqueId().toString() + ".lock");
        try {
            if (!lockFile.createNewFile()) {
                plugin.getLanguageManager().sendMessage(player, "error.creation_in_progress");
                return;
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not create lock file for realm creation.", e);
            plugin.getLanguageManager().sendMessage(player, "error.command_generic");
            return;
        }

        plugin.getLanguageManager().sendMessage(player, "realm.creation_started");

        CompletableFuture.runAsync(() -> {
            try {
                // 2. Create the Realm object first to get the unique ID and folder name
                final Realm realm = new Realm(realmName, player.getUniqueId(), templateType);
                final String worldFolderName = realm.getWorldFolderName();
                final String worldPath = "realms/" + worldFolderName;

                // 3. Prepare directories and copy template
                File templateDir = new File(plugin.getDataFolder(), "templates/" + templateType);
                if (!templateDir.exists() || !templateDir.isDirectory()) {
                    plugin.getLanguageManager().sendMessage(player, "error.template_not_found", "%template%", templateType);
                    return;
                }

                File newWorldDir = new File(Bukkit.getWorldContainer(), worldPath);
                File realmsDir = newWorldDir.getParentFile();
                if (!realmsDir.exists()) {
                    realmsDir.mkdirs();
                }

                try {
                    copyDirectory(templateDir, newWorldDir);
                    // IMPORTANT: Delete the uid.dat file to prevent Bukkit from thinking it's the same world
                    File uidDat = new File(newWorldDir, "uid.dat");
                    if (uidDat.exists()) {
                        uidDat.delete();
                    }
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to copy template for new realm.", e);
                    plugin.getLanguageManager().sendMessage(player, "error.command_generic");
                    return;
                }

                // 4. Create and load the world on the main thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    WorldCreator creator = new WorldCreator(worldPath);
                    World world = creator.createWorld();

                    if (world != null) {
                        // Update realm object with final details from the created world
                        realm.setFlat(world.getWorldType() == org.bukkit.WorldType.FLAT);
                        realm.setBorderCenterX(world.getSpawnLocation().getX());
                        realm.setBorderCenterZ(world.getSpawnLocation().getZ());
                        realm.setDifficulty(world.getDifficulty().name().toLowerCase());

                        // 5. Save the realm data to JSON and teleport the player
                        realmManager.createRealm(realm).thenRun(() -> {
                            plugin.getLanguageManager().sendMessage(player, "world.created", "%world%", realmName);
                            // Teleport must also be on the main thread
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                plugin.getWorldManager().teleportToRealm(player, realmName);
                            });
                        }).exceptionally(ex -> {
                            plugin.getLogger().log(Level.SEVERE, "Failed to save new realm data after world creation.", ex);
                            plugin.getLanguageManager().sendMessage(player, "error.command_generic");
                            // Consider adding cleanup logic here
                            return null;
                        });
                    } else {
                        plugin.getLanguageManager().sendMessage(player, "error.world_creation_failed", "%world%", realmName);
                    }
                });
            } finally {
                // 6. Always release the lock
                lockFile.delete();
            }
        });
    }

    private void copyDirectory(File source, File destination) throws IOException {
        if (source.isDirectory()) {
            if (!destination.exists() && !destination.mkdirs()) {
                throw new IOException("Cannot create directory " + destination.getAbsolutePath());
            }
            String[] files = source.list();
            if (files != null) {
                for (String file : files) {
                    File srcFile = new File(source, file);
                    File destFile = new File(destination, file);
                    copyDirectory(srcFile, destFile);
                }
            }
        } else {
            Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}