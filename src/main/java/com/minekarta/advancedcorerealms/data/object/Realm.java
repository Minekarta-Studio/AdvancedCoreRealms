package com.minekarta.advancedcorerealms.data.object;

import com.minekarta.advancedcorerealms.realm.Role;
import org.bukkit.World;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Realm {
    private int id; // Database ID
    private String name;
    private UUID owner;
    private Map<UUID, Role> members;
    private List<UUID> accessList;  // Additional access permissions beyond members
    private boolean isFlat;
    private Instant createdAt;
    private String worldName;
    private String template;
    private int maxPlayers;        // Max players allowed in the world
    private boolean isCreativeMode; // Whether the world is in creative or survival
    private boolean isPeacefulMode; // Whether the world is in peaceful mode
    private String worldType;      // The type of world (NORMAL, FLAT, AMPLIFIED, etc.)
    private List<String> transferableItems; // List of items that can be transferred out of the realm
    private int borderSize;        // Size of the world border for this realm (in blocks)
    /** The X-coordinate of the world border's center. */
    private double borderCenterX;
    /** The Z-coordinate of the world border's center. */
    private double borderCenterZ;

    // New upgrade fields
    private String difficulty;
    private boolean keepLoaded;
    private String borderTierId;
    private String memberSlotTierId;

    public Realm(String name, UUID owner, String worldName, String template) {
        this.name = name;
        this.owner = owner;
        this.worldName = worldName;
        this.template = template;
        this.members = new HashMap<>();
        this.members.put(owner, Role.OWNER); // Owner is always a member with OWNER role
        this.accessList = new ArrayList<>();
        this.isFlat = false; // This can be configured per-template later
        this.createdAt = Instant.now();
        this.maxPlayers = 8; // Default max players
        this.isCreativeMode = false; // Default to survival
        this.isPeacefulMode = false; // Default to normal mob spawning
        this.worldType = "NORMAL";
        this.transferableItems = new ArrayList<>();
        this.borderSize = 100; // Default border size
        this.borderCenterX = 0.0;
        this.borderCenterZ = 0.0;

        // Initialize new fields
        this.difficulty = "normal"; // Default difficulty
        this.keepLoaded = false;
        this.borderTierId = "tier_50"; // Default border tier
        this.memberSlotTierId = "tier_0"; // Default member slot tier
    }

    // This constructor is for loading from storage
    public Realm(String name, UUID owner, String worldName, String template, Instant createdAt, boolean isFlat) {
        this.name = name;
        this.owner = owner;
        this.worldName = worldName;
        this.template = template;
        this.createdAt = createdAt;
        this.isFlat = isFlat;
        this.members = new HashMap<>();
        this.members.put(owner, Role.OWNER); // Ensure owner is set
        this.accessList = new ArrayList<>();
        this.maxPlayers = 8;
        this.isCreativeMode = false;
        this.isPeacefulMode = false;
        this.worldType = "NORMAL";
        this.transferableItems = new ArrayList<>();
        this.borderSize = 100;
        this.borderCenterX = 0.0;
        this.borderCenterZ = 0.0;

        // Initialize new fields with defaults, will be overwritten by loader
        this.difficulty = "normal";
        this.keepLoaded = false;
        this.borderTierId = "tier_50";
        this.memberSlotTierId = "tier_0";
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public UUID getOwner() {
        return owner;
    }
    
    public void setOwner(UUID newOwner) {
        // Demote old owner to admin, promote new owner
        if (this.owner != null) {
            members.put(this.owner, Role.ADMIN);
        }
        this.owner = newOwner;
        members.put(newOwner, Role.OWNER);
    }
    
    public Map<UUID, Role> getMembers() {
        return members;
    }

    public void setMembers(Map<UUID, Role> members) {
        this.members = members;
        // Ensure owner always has OWNER role
        if (this.owner != null) {
            this.members.put(this.owner, Role.OWNER);
        }
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
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
        return members.containsKey(playerId);
    }

    public Role getRole(UUID playerId) {
        if (owner.equals(playerId)) {
            return Role.OWNER;
        }
        return members.getOrDefault(playerId, Role.VISITOR);
    }
    
    public boolean hasAccess(UUID playerId) {
        // Owner and members have access. Visitors do not.
        return isMember(playerId);
    }
    
    public void addMember(UUID playerId) {
        // Add player with default MEMBER role
        addMember(playerId, Role.MEMBER);
    }

    public void addMember(UUID playerId, Role role) {
        if (owner.equals(playerId)) return; // Cannot change owner's role here
        members.put(playerId, role);
    }
    
    public void removeMember(UUID playerId) {
        if (owner.equals(playerId)) return; // Cannot remove the owner
        members.remove(playerId);
        // Also remove from access list if there
        accessList.remove(playerId);
    }
    
    public void addToAccessList(UUID playerId) {
        if (!accessList.contains(playerId) && !isMember(playerId)) {
            accessList.add(playerId);
        }
    }
    
    public void removeFromAccessList(UUID playerId) {
        accessList.remove(playerId);
    }
    
    public World getBukkitWorld() {
        return org.bukkit.Bukkit.getWorld(this.worldName);
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
    
    public double getBorderCenterX() {
        return borderCenterX;
    }

    public void setBorderCenterX(double borderCenterX) {
        this.borderCenterX = borderCenterX;
    }

    public double getBorderCenterZ() {
        return borderCenterZ;
    }

    public void setBorderCenterZ(double borderCenterZ) {
        this.borderCenterZ = borderCenterZ;
    }
    
    public void updateCenterFromWorld() {
        World bukkitWorld = getBukkitWorld();
        if (bukkitWorld != null) {
            org.bukkit.Location spawnLocation = bukkitWorld.getSpawnLocation();
            this.borderCenterX = spawnLocation.getX();
            this.borderCenterZ = spawnLocation.getZ();
        }
    }
    
    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public boolean isKeepLoaded() {
        return keepLoaded;
    }

    public void setKeepLoaded(boolean keepLoaded) {
        this.keepLoaded = keepLoaded;
    }

    public String getBorderTierId() {
        return borderTierId;
    }

    public void setBorderTierId(String borderTierId) {
        this.borderTierId = borderTierId;
    }

    public String getMemberSlotTierId() {
        return memberSlotTierId;
    }

    public void setMemberSlotTierId(String memberSlotTierId) {
        this.memberSlotTierId = memberSlotTierId;
    }
}