package com.minekarta.advancedcorerealms.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.utils.InstantAdapter;
import com.minekarta.advancedcorerealms.utils.UUIDAdapter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * Manages the persistence of Realm data using JSON files.
 * Each realm is stored as a separate .json file in the 'realms' directory.
 */
public class StorageManager {

    private final AdvancedCoreRealms plugin;
    private final File realmsDir;
    private final Gson gson;

    public StorageManager(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.realmsDir = new File(plugin.getDataFolder(), "realms");
        if (!realmsDir.exists() && !realmsDir.mkdirs()) {
            plugin.getLogger().severe("Could not create realms storage directory!");
        }

        this.gson = new GsonBuilder()
                .registerTypeAdapter(UUID.class, new UUIDAdapter())
                .registerTypeAdapter(Instant.class, new InstantAdapter())
                .setPrettyPrinting()
                .create();
    }

    /**
     * Asynchronously saves a realm to its corresponding JSON file.
     *
     * @param realm The realm to save.
     * @return A CompletableFuture that completes when the save operation is done.
     */
    public CompletableFuture<Void> saveRealm(Realm realm) {
        return CompletableFuture.runAsync(() -> {
            File realmFile = new File(realmsDir, realm.getRealmId().toString() + ".json");
            try (FileWriter writer = new FileWriter(realmFile)) {
                gson.toJson(realm, writer);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save realm " + realm.getName(), e);
                throw new RuntimeException("Failed to save realm", e);
            }
        });
    }

    /**
     * Asynchronously loads a single realm from its JSON file.
     *
     * @param realmId The UUID of the realm to load.
     * @return A CompletableFuture containing the loaded Realm, or null if not found.
     */
    public CompletableFuture<Realm> loadRealm(UUID realmId) {
        return CompletableFuture.supplyAsync(() -> {
            File realmFile = new File(realmsDir, realmId.toString() + ".json");
            if (!realmFile.exists()) {
                return null;
            }
            try (FileReader reader = new FileReader(realmFile)) {
                return gson.fromJson(reader, Realm.class);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load realm " + realmId, e);
                throw new RuntimeException("Failed to load realm", e);
            }
        });
    }

    /**
     * Asynchronously loads all realms from the storage directory.
     *
     * @return A CompletableFuture containing a list of all loaded realms.
     */
    public CompletableFuture<List<Realm>> loadAllRealms() {
        return CompletableFuture.supplyAsync(() -> {
            List<Realm> realms = new ArrayList<>();
            if (!realmsDir.exists()) {
                return realms;
            }
            try (Stream<Path> paths = Files.walk(realmsDir.toPath())) {
                paths.filter(Files::isRegularFile)
                     .filter(path -> path.toString().endsWith(".json"))
                     .forEach(path -> {
                         try (FileReader reader = new FileReader(path.toFile())) {
                             Realm realm = gson.fromJson(reader, Realm.class);
                             if (realm != null) {
                                 realms.add(realm);
                             }
                         } catch (IOException e) {
                             plugin.getLogger().log(Level.SEVERE, "Failed to load realm from file " + path, e);
                         }
                     });
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to walk realms directory", e);
            }
            return realms;
        });
    }

    /**
     * Asynchronously deletes a realm's JSON file.
     *
     * @param realmId The UUID of the realm to delete.
     * @return A CompletableFuture that completes when the deletion is done.
     */
    public CompletableFuture<Void> deleteRealm(UUID realmId) {
        return CompletableFuture.runAsync(() -> {
            File realmFile = new File(realmsDir, realmId.toString() + ".json");
            if (realmFile.exists()) {
                try {
                    Files.delete(realmFile.toPath());
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to delete realm file " + realmId, e);
                    throw new RuntimeException("Failed to delete realm file", e);
                }
            }
        });
    }
}