package com.minekarta.advancedcorerealms.data;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
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
        // Load realms from file
        if (!worldsFile.exists()) {
            try {
                worldsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create worlds.yml file: " + e.getMessage());
                return;
            }
        }
        
        // Load all realm data
        Set<String> keys = worldsConfig.getKeys(false);
        for (String realmName : keys) {
            String ownerStr = worldsConfig.getString(realmName + ".owner");
            if (ownerStr == null) continue;
            
            UUID owner = UUID.fromString(ownerStr);
            boolean isFlat = worldsConfig.getBoolean(realmName + ".isFlat", true);
            
            List<String> memberStrings = worldsConfig.getStringList(realmName + ".members");
            List<UUID> members = new ArrayList<>();
            for (String memberStr : memberStrings) {
                try {
                    members.add(UUID.fromString(memberStr));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in members list for realm: " + realmName);
                }
            }
            
            List<String> transferableItems = worldsConfig.getStringList(realmName + ".transferable-items");
            
            Realm realm = new Realm(realmName, owner, isFlat);
            realm.setMembers(members);
            realm.setTransferableItems(transferableItems);
            realms.put(realmName, realm);
        }
        
        plugin.getLogger().info("Loaded " + realms.size() + " realms from data file.");
    }
    
    public void saveData() {
        // Save all realms to file
        for (Map.Entry<String, Realm> entry : realms.entrySet()) {
            String realmName = entry.getKey();
            Realm realm = entry.getValue();
            
            worldsConfig.set(realmName + ".owner", realm.getOwner().toString());
            worldsConfig.set(realmName + ".isFlat", realm.isFlat());
            
            List<String> memberStrings = new ArrayList<>();
            for (UUID member : realm.getMembers()) {
                memberStrings.add(member.toString());
            }
            worldsConfig.set(realmName + ".members", memberStrings);
            
            worldsConfig.set(realmName + ".transferable-items", realm.getTransferableItems());
        }
        
        try {
            worldsConfig.save(worldsFile);
            plugin.getLogger().info("Saved " + realms.size() + " realms to data file.");
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save worlds.yml file: " + e.getMessage());
        }
    }
    
    public void addRealm(Realm realm) {
        realms.put(realm.getName(), realm);
        saveData();
    }
    
    public void removeRealm(String realmName) {
        realms.remove(realmName);
        saveData();
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
            if (realm.getMembers().contains(playerId) && !realm.getOwner().equals(playerId)) {
                invitedRealms.add(realm);
            }
        }
        return invitedRealms;
    }
    
    public boolean isPlayerInRealm(UUID playerId, String realmName) {
        Realm realm = getRealm(realmName);
        return realm != null && realm.isMember(playerId);
    }
}