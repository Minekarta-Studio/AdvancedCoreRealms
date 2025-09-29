package com.minekarta.advancedcorerealms.realm;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.manager.world.WorldManager;
import com.minekarta.advancedcorerealms.manager.world.WorldPluginManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class RealmCreator implements Listener {

    private final AdvancedCoreRealms plugin;
    private final WorldManager worldManager;
    private final WorldPluginManager worldPluginManager;

    public RealmCreator(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.worldManager = plugin.getWorldManager();
        this.worldPluginManager = worldManager.getWorldPluginManager();
    }

    public void createRealmAsync(Player player, String realmName, String templateType) {
        File lockFile = new File(plugin.getDataFolder(), "creating_" + realmName + ".lock");
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

        CompletableFuture.runAsync(() -> {
            try {
                WorldCreator creator = new WorldCreator(realmName);

                File templateDir = new File(plugin.getDataFolder() + "/" + plugin.getRealmConfig().getTemplatesFolder(), templateType);
                if (!templateDir.exists() || !templateDir.isDirectory()) {
                    plugin.getLanguageManager().sendMessage(player, "error.template_not_found", "%template%", templateType);
                    return;
                }

                File worldContainer = Bukkit.getWorldContainer();
                File newWorldDir = new File(worldContainer, realmName);

                try {
                    copyDirectory(templateDir, newWorldDir);
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to copy template for new realm.", e);
                    plugin.getLanguageManager().sendMessage(player, "error.command_generic");
                    return;
                }

                World world = worldPluginManager.createWorldAsync(realmName, creator, player).join();

                if (world != null) {
                    Realm realm = new Realm(realmName, player.getUniqueId(), realmName, templateType);
                    realm.setFlat(world.getWorldType() == WorldType.FLAT);

                    plugin.getRealmManager().createRealm(realm).thenRun(() -> {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            worldManager.teleportToRealm(player, realmName);
                            plugin.getLanguageManager().sendMessage(player, "world.created", "%world%", realmName);
                        });
                    });
                } else {
                    plugin.getLanguageManager().sendMessage(player, "error.world_creation_failed", "%world%", realmName);
                }
            } finally {
                lockFile.delete();
            }
        });
    }

    private void copyDirectory(File source, File destination) throws IOException {
        if (source.isDirectory()) {
            if (!destination.exists()) {
                destination.mkdir();
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
            java.nio.file.Files.copy(source.toPath(), destination.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }
}