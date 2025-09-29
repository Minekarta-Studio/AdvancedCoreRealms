package com.minekarta.advancedcorerealms.data;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;

public class WorldDataManager {
    
    private final AdvancedCoreRealms plugin;
    private final File worldsFile;
    private final FileConfiguration worldsConfig;
    private final Map<String, Realm> realms;
    
    public WorldDataManager(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.worldsFile = new File(plugin.getDataFolder(), "worlds.yml");
        this.worldsConfig = YamlConfiguration.loadConfiguration(worldsFile);
        this.realms = new HashMap<>();
    }
    
    public void loadData() {
        if (!worldsFile.exists()) {
            plugin.saveResource("worlds.yml", false);
        }

        Set<String> realmIds = worldsConfig.getConfigurationSection("realms").getKeys(false);
        if (realmIds == null) {
            plugin.getLogger().info("No realms found in data file.");
            return;
        }

        for (String realmId : realmIds) {
            String path = "realms." + realmId;
            String name = worldsConfig.getString(path + ".name");
            UUID owner = UUID.fromString(worldsConfig.getString(path + ".owner"));
            String worldName = worldsConfig.getString(path + ".worldName");
            String template = worldsConfig.getString(path + ".template");
            Instant createdAt = Instant.parse(worldsConfig.getString(path + ".createdAt"));
            boolean isFlat = worldsConfig.getBoolean(path + ".isFlat", false);

            Realm realm = new Realm(name, owner, worldName, template, createdAt, isFlat);

            // Load new upgrade fields
            realm.setDifficulty(worldsConfig.getString(path + ".difficulty", "normal"));
            realm.setKeepLoaded(worldsConfig.getBoolean(path + ".keepLoaded", false));
            realm.setBorderTierId(worldsConfig.getString(path + ".borderTierId", "tier_50"));
            realm.setMemberSlotTierId(worldsConfig.getString(path + ".memberSlotTierId", "tier_0"));
            realm.setBorderSize(worldsConfig.getInt(path + ".borderSize", 100));
            realm.setMaxPlayers(worldsConfig.getInt(path + ".maxPlayers", 8));

            // Load members
            if (worldsConfig.isConfigurationSection(path + ".members")) {
                for (String memberUuid : worldsConfig.getConfigurationSection(path + ".members").getKeys(false)) {
                    try {
                        com.minekarta.advancedcorerealms.realm.Role role = com.minekarta.advancedcorerealms.realm.Role.valueOf(worldsConfig.getString(path + ".members." + memberUuid));
                        realm.addMember(UUID.fromString(memberUuid), role);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid role for member " + memberUuid + " in realm " + name);
                    }
                }
            }

            realms.put(name, realm);
        }
        plugin.getLogger().info("Loaded " + realms.size() + " realms from data file.");
    }
    
    public void saveData() {
        // Clear the existing realms section to ensure deleted realms are removed
        worldsConfig.set("realms", null);

        // Save all realms to file
        for (Realm realm : realms.values()) {
            String path = "realms." + realm.getName(); // Using name as the key
            worldsConfig.set(path + ".name", realm.getName());
            worldsConfig.set(path + ".owner", realm.getOwner().toString());
            worldsConfig.set(path + ".worldName", realm.getWorldName());
            worldsConfig.set(path + ".template", realm.getTemplate());
            worldsConfig.set(path + ".createdAt", realm.getCreatedAt().toString());
            worldsConfig.set(path + ".isFlat", realm.isFlat());

            // Save new upgrade fields
            worldsConfig.set(path + ".difficulty", realm.getDifficulty());
            worldsConfig.set(path + ".keepLoaded", realm.isKeepLoaded());
            worldsConfig.set(path + ".borderTierId", realm.getBorderTierId());
            worldsConfig.set(path + ".memberSlotTierId", realm.getMemberSlotTierId());
            worldsConfig.set(path + ".borderSize", realm.getBorderSize());
            worldsConfig.set(path + ".maxPlayers", realm.getMaxPlayers());

            // Save members
            for (Map.Entry<UUID, com.minekarta.advancedcorerealms.realm.Role> entry : realm.getMembers().entrySet()) {
                worldsConfig.set(path + ".members." + entry.getKey().toString(), entry.getValue().name());
            }

            worldsConfig.set(path + ".transferable-items", realm.getTransferableItems());
        }
        
        try {
            worldsConfig.save(worldsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save worlds.yml file: " + e.getMessage());
        }
    }

    public void saveDataAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            saveData();
            plugin.getLogger().info("Saved " + realms.size() + " realms to data file asynchronously.");
        });
    }
    
    public void addRealm(Realm realm) {
        realms.put(realm.getName(), realm);
        saveDataAsync();
    }
    
    public void removeRealm(String realmName) {
        realms.remove(realmName);
        saveDataAsync();
    }
    
    public Realm getRealm(String realmName) {
        return realms.get(realmName);
    }
    
    public List<Realm> getAllRealms() {
        return new ArrayList<>(realms.values());
    }
    
    public List<Realm> getPlayerRealms(UUID playerId) {
        List<Realm> playerRealms = new ArrayList<>();
        for (Realm realm : realms.values()) {
            if (realm.getOwner().equals(playerId)) {
                playerRealms.add(realm);
            }
        }
        return playerRealms;
    }
    
    public List<Realm> getPlayerInvitedRealms(UUID playerId) {
        List<Realm> invitedRealms = new ArrayList<>();
        for (Realm realm : realms.values()) {
            if (realm.getMembers().containsKey(playerId) && !realm.getOwner().equals(playerId)) {
                invitedRealms.add(realm);
            }
        }
        return invitedRealms;
    }
    
    public boolean isPlayerInRealm(UUID playerId, String realmName) {
        Realm realm = getRealm(realmName);
        return realm != null && realm.isMember(playerId);
    }

    public Optional<Realm> getRealmByWorldName(String worldName) {
        return realms.values().stream()
                .filter(realm -> worldName.equals(realm.getWorldName()))
                .findFirst();
    }
}