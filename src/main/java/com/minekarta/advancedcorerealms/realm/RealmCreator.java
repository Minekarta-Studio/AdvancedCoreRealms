package com.minekarta.advancedcorerealms.realm;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.config.RealmConfig;
import com.minekarta.advancedcorerealms.data.object.Realm;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class RealmCreator {

    private final AdvancedCoreRealms plugin;
    private final RealmConfig realmConfig;

    public RealmCreator(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.realmConfig = plugin.getRealmConfig();
    }

    public void createRealmAsync(Player owner, String realmName, String templateType) {
        File lockFile = new File(plugin.getDataFolder(), "creating_" + owner.getUniqueId() + ".lock");
        if (lockFile.exists()) {
            owner.sendMessage(Component.text("You are already creating a realm. Please wait.", NamedTextColor.RED));
            return;
        }

        try {
            lockFile.createNewFile();
        } catch (IOException e) {
            owner.sendMessage(Component.text("Failed to acquire creation lock. Please try again.", NamedTextColor.RED));
            plugin.getLogger().severe("Failed to create lock file for " + owner.getName() + ": " + e.getMessage());
            return;
        }

        owner.sendMessage(Component.text("Starting realm creation... this may take a moment.", NamedTextColor.GRAY));

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String sanitizedName = sanitizeName(realmName);
                String worldFolderName = generateWorldFolderName(owner, sanitizedName);

                Path templateDir = plugin.getDataFolder().toPath().resolve(realmConfig.getTemplatesFolder()).resolve(templateType);
                if (!Files.isDirectory(templateDir)) {
                    notifyFailure(owner, "Template '" + templateType + "' not found.", lockFile, null);
                    return;
                }

                Path targetDir = Bukkit.getWorldContainer().toPath().resolve(realmConfig.getServerRealmsFolder()).resolve(worldFolderName);
                if (Files.exists(targetDir)) {
                    notifyFailure(owner, "A realm with a similar name already exists.", lockFile, null);
                    return;
                }

                copyDirectory(templateDir, targetDir);

                // World copied, now load it on the main thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    loadAndFinalizeWorld(owner, realmName, worldFolderName, templateType, lockFile, targetDir);
                });

            } catch (IOException e) {
                plugin.getLogger().severe("Error copying realm template for " + owner.getName() + ": " + e.getMessage());
                notifyFailure(owner, "A file error occurred during creation.", lockFile, null);
            }
        });
    }

    private void loadAndFinalizeWorld(Player owner, String name, String worldFolderName, String templateType, File lockFile, Path worldPath) {
        WorldCreator creator = new WorldCreator(worldPath.toString());
        World world = Bukkit.createWorld(creator);

        if (world == null) {
            notifyFailure(owner, "Failed to load the new world.", lockFile, worldPath);
            return;
        }

        // Configure world
        world.setDifficulty(Difficulty.PEACEFUL);
        world.getWorldBorder().setCenter(0, 0);
        world.getWorldBorder().setSize(realmConfig.getDefaultBorderSize());

        // Persist metadata
        Realm realm = new Realm(name, owner.getUniqueId(), worldFolderName, templateType);
        plugin.getWorldDataManager().addRealm(realm);

        // Teleport and notify player
        owner.teleport(world.getSpawnLocation());
        Title title = Title.title(
                Component.text("Realm Created!", NamedTextColor.GOLD),
                Component.text(name),
                Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(3), Duration.ofSeconds(1))
        );
        owner.showTitle(title);
        owner.sendMessage(Component.text("Your new realm has been created successfully!", NamedTextColor.GREEN));

        lockFile.delete();
    }

    private void notifyFailure(Player owner, String message, File lockFile, Path worldPath) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            owner.sendMessage(Component.text("Realm creation failed: " + message, NamedTextColor.RED));
        });

        if (worldPath != null) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    deleteDirectory(worldPath);
                } catch (IOException e) {
                    plugin.getLogger().severe("Failed to clean up partial world directory: " + worldPath);
                }
            });
        }
        lockFile.delete();
    }

    private String sanitizeName(String input) {
        String allowedCharsRegex = realmConfig.getSanitizeAllowedRegex();
        String invertedRegex = "[^" + allowedCharsRegex.substring(1, allowedCharsRegex.length() - 1) + "]";
        String sanitized = input.toLowerCase().replaceAll(invertedRegex, "");
        int maxLength = realmConfig.getSanitizeMaxLength();
        if (sanitized.length() > maxLength) {
            sanitized = sanitized.substring(0, maxLength);
        }
        return sanitized.isEmpty() ? "realm" : sanitized;
    }

    private String generateWorldFolderName(Player owner, String sanitizedName) {
        String ownerUuid = owner.getUniqueId().toString().replace("-", "");
        String shortUuid = ownerUuid.substring(0, Math.min(ownerUuid.length(), 8));
        long timestamp = System.currentTimeMillis();
        return realmConfig.getWorldNameFormat()
                .replace("{owner}", shortUuid)
                .replace("{name}", sanitizedName)
                .replace("{ts}", String.valueOf(timestamp));
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        try (Stream<Path> stream = Files.walk(source)) {
            stream.forEach(sourcePath -> {
                try {
                    Path targetPath = target.resolve(source.relativize(sourcePath));
                    if (Files.isDirectory(sourcePath)) {
                        if (!Files.exists(targetPath)) {
                            Files.createDirectories(targetPath);
                        }
                    } else {
                        Files.copy(sourcePath, targetPath);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Failed to copy file: " + sourcePath, e);
                }
            });
        }
    }

    private void deleteDirectory(Path path) throws IOException {
        try (Stream<Path> walk = Files.walk(path)) {
            walk.sorted(java.util.Comparator.reverseOrder())
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                         plugin.getLogger().severe("Failed to delete path: " + p + " during cleanup.");
                    }
                });
        }
    }
}