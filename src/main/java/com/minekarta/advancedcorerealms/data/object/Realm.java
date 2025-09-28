package com.minekarta.advancedcorerealms.data.object;

import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Realm {
    private String name;
    private UUID owner;
    private List<UUID> members;
    private List<UUID> accessList;  // Additional access permissions beyond members
    private boolean isFlat;
    private long creationTime;
    private int maxPlayers;        // Max players allowed in the world
    private boolean isCreativeMode; // Whether the world is in creative or survival
    private boolean isPeacefulMode; // Whether the world is in peaceful mode
    private String worldType;      // The type of world (NORMAL, FLAT, AMPLIFIED, etc.)
    private List<String> transferableItems; // List of items that can be transferred out of the realm
    private int borderSize;        // Size of the world border for this realm (in blocks)
    private double centerX;        // Center X coordinate of the realm border
    private double centerZ;        // Center Z coordinate of the realm border
    private java.util.Map<String, Integer> upgradeLevels; // Map of upgrade IDs to their levels
    
    public Realm(String name, UUID owner, boolean isFlat) {
        this.name = name;
        this.owner = owner;
        this.members = new ArrayList<>();
        this.accessList = new ArrayList<>();
        this.isFlat = isFlat;
        this.creationTime = System.currentTimeMillis();
        this.maxPlayers = 8; // Default max players
        this.isCreativeMode = false; // Default to survival
        this.isPeacefulMode = false; // Default to normal mob spawning
        this.worldType = isFlat ? "FLAT" : "NORMAL";
        this.transferableItems = new ArrayList<>(); // Default to no transferable items
        this.borderSize = 100; // Default border size
        // Initialize center coordinates to 0,0 (will be updated when world is created/loaded)
        this.centerX = 0.0;
        this.centerZ = 0.0;
        this.upgradeLevels = new java.util.HashMap<>(); // Initialize upgrade levels
    }
    
    // Getters and setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public UUID getOwner() {
        return owner;
    }
    
    public void setOwner(UUID owner) {
        this.owner = owner;
    }
    
    public List<UUID> getMembers() {
        return members;
    }
    
    public void setMembers(List<UUID> members) {
        this.members = members;
    }
    
    public List<UUID> getAccessList() {
        return accessList;
    }
    
    public void setAccessList(List<UUID> accessList) {
        this.accessList = accessList;
    }
    
    public boolean isFlat() {
        return isFlat;
    }
    
    public void setFlat(boolean flat) {
        isFlat = flat;
    }
    
    public long getCreationTime() {
        return creationTime;
    }
    
    public int getMaxPlayers() {
        return maxPlayers;
    }
    
    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }
    
    public boolean isCreativeMode() {
        return isCreativeMode;
    }
    
    public void setCreativeMode(boolean creativeMode) {
        isCreativeMode = creativeMode;
    }
    
    public boolean isPeacefulMode() {
        return isPeacefulMode;
    }
    
    public void setPeacefulMode(boolean peacefulMode) {
        isPeacefulMode = peacefulMode;
    }
    
    public String getWorldType() {
        return worldType;
    }
    
    public void setWorldType(String worldType) {
        this.worldType = worldType;
    }
    
    public boolean isMember(UUID playerId) {
        return members.contains(playerId) || owner.equals(playerId);
    }
    
    public boolean hasAccess(UUID playerId) {
        // Owner, members, and those in access list can access
        return owner.equals(playerId) || members.contains(playerId) || accessList.contains(playerId);
    }
    
    public void addMember(UUID playerId) {
        if (!members.contains(playerId)) {
            members.add(playerId);
        }
    }
    
    public void removeMember(UUID playerId) {
        members.remove(playerId);
        // Also remove from access list if there
        accessList.remove(playerId);
    }
    
    public void addToAccessList(UUID playerId) {
        if (!accessList.contains(playerId) && !owner.equals(playerId) && !members.contains(playerId)) {
            accessList.add(playerId);
        }
    }
    
    public void removeFromAccessList(UUID playerId) {
        accessList.remove(playerId);
    }
    
    /**
     * Gets the actual Bukkit world object if loaded
     * @return The loaded World object or null if not currently loaded
     */
    public World getBukkitWorld() {
        return org.bukkit.Bukkit.getWorld(name);
    }
    
    public List<String> getTransferableItems() {
        return transferableItems;
    }
    
    public void setTransferableItems(List<String> transferableItems) {
        this.transferableItems = transferableItems;
    }
    
    public boolean isItemTransferable(String materialName) {
        return transferableItems.contains(materialName.toUpperCase());
    }
    
    public void addTransferableItem(String materialName) {
        if (!transferableItems.contains(materialName.toUpperCase())) {
            transferableItems.add(materialName.toUpperCase());
        }
    }
    
    public void removeTransferableItem(String materialName) {
        transferableItems.remove(materialName.toUpperCase());
    }
    
    public int getBorderSize() {
        return borderSize;
    }
    
    public void setBorderSize(int borderSize) {
        this.borderSize = borderSize;
    }
    
    public double getCenterX() {
        return centerX;
    }
    
    public void setCenterX(double centerX) {
        this.centerX = centerX;
    }
    
    public double getCenterZ() {
        return centerZ;
    }
    
    public void setCenterZ(double centerZ) {
        this.centerZ = centerZ;
    }
    
    /**
     * Updates the center coordinates based on the world's spawn location
     * This should be called when the realm world is first created or loaded
     */
    public void updateCenterFromWorld() {
        World bukkitWorld = getBukkitWorld();
        if (bukkitWorld != null) {
            org.bukkit.Location spawnLocation = bukkitWorld.getSpawnLocation();
            this.centerX = spawnLocation.getX();
            this.centerZ = spawnLocation.getZ();
        }
    }
    
    public java.util.Map<String, Integer> getUpgradeLevels() {
        return upgradeLevels;
    }
    
    public void setUpgradeLevels(java.util.Map<String, Integer> upgradeLevels) {
        this.upgradeLevels = upgradeLevels;
    }
    
    public int getUpgradeLevel(String upgradeId) {
        if (upgradeLevels == null) {
            upgradeLevels = new java.util.HashMap<>();
        }
        return upgradeLevels.getOrDefault(upgradeId, 0);
    }
    
    public void setUpgradeLevel(String upgradeId, int level) {
        if (upgradeLevels == null) {
            upgradeLevels = new java.util.HashMap<>();
        }
        upgradeLevels.put(upgradeId, level);
    }
}