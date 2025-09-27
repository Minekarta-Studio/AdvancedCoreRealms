package com.minekarta.advancedcorerealms.data;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlayerDataManager {
    
    private final AdvancedCoreRealms plugin;
    private final File playerDataFolder;
    
    public PlayerDataManager(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.playerDataFolder = new File(plugin.getDataFolder(), "playerdata");
        
        if (!playerDataFolder.exists()) {
            playerDataFolder.mkdirs();
        }
    }
    
    /**
     * Saves a player's inventory for a specific world
     */
    public void savePlayerInventory(UUID playerUUID, String worldName, ItemStack[] inventory) {
        File playerFile = getPlayerDataFile(playerUUID);
        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        
        // Save inventory contents
        List<Map<String, Object>> itemMapList = new ArrayList<>();
        for (int i = 0; i < inventory.length; i++) {
            ItemStack item = inventory[i];
            if (item != null) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("slot", i);
                itemMap.put("item", item);
                itemMapList.add(itemMap);
            }
        }
        
        config.set(worldName + ".inventory", itemMapList);
        
        try {
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save player inventory data: " + e.getMessage());
        }
    }
    
    /**
     * Loads a player's inventory for a specific world
     */
    public ItemStack[] loadPlayerInventory(UUID playerUUID, String worldName) {
        File playerFile = getPlayerDataFile(playerUUID);
        if (!playerFile.exists()) {
            // Return empty inventory if no data exists
            return new ItemStack[41]; // 36 inventory slots + 4 armor slots + 1 offhand
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemMapList = (List<Map<String, Object>>) config.getList(worldName + ".inventory");
        
        ItemStack[] inventory = new ItemStack[41];
        
        if (itemMapList != null) {
            for (Map<String, Object> itemMap : itemMapList) {
                Integer slot = (Integer) itemMap.get("slot");
                ItemStack item = (ItemStack) itemMap.get("item");
                
                if (slot != null && item != null) {
                    inventory[slot] = item;
                }
            }
        }
        
        return inventory;
    }
    
    /**
     * Saves a player's location before teleporting to a realm
     */
    public void savePreviousLocation(UUID playerUUID, Location location) {
        File playerFile = getPlayerDataFile(playerUUID);
        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        
        config.set("previous_location.world", location.getWorld().getName());
        config.set("previous_location.x", location.getX());
        config.set("previous_location.y", location.getY());
        config.set("previous_location.z", location.getZ());
        config.set("previous_location.yaw", location.getYaw());
        config.set("previous_location.pitch", location.getPitch());
        
        try {
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save player location data: " + e.getMessage());
        }
    }
    
    /**
     * Loads a player's previous location
     */
    public Location loadPreviousLocation(UUID playerUUID) {
        File playerFile = getPlayerDataFile(playerUUID);
        if (!playerFile.exists()) {
            return null;
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        String worldName = config.getString("previous_location.world");
        
        if (worldName == null) {
            return null;
        }
        
        Double x = config.getDouble("previous_location.x");
        Double y = config.getDouble("previous_location.y");
        Double z = config.getDouble("previous_location.z");
        Float yaw = (float) config.getDouble("previous_location.yaw", 0.0);
        Float pitch = (float) config.getDouble("previous_location.pitch", 0.0);
        
        org.bukkit.World world = plugin.getServer().getWorld(worldName);
        if (world == null) {
            return null;
        }
        
        return new Location(world, x, y, z, yaw, pitch);
    }
    
    /**
     * Gets the file for a specific player's data
     */
    private File getPlayerDataFile(UUID playerUUID) {
        return new File(playerDataFolder, playerUUID.toString() + ".yml");
    }
}