package com.minekarta.advancedcorerealms.data.object;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Realm {
    private String name;
    private UUID owner;
    private List<UUID> members;
    private boolean isFlat;
    private long creationTime;
    
    public Realm(String name, UUID owner, boolean isFlat) {
        this.name = name;
        this.owner = owner;
        this.members = new ArrayList<>();
        this.isFlat = isFlat;
        this.creationTime = System.currentTimeMillis();
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
    
    public boolean isFlat() {
        return isFlat;
    }
    
    public void setFlat(boolean flat) {
        isFlat = flat;
    }
    
    public long getCreationTime() {
        return creationTime;
    }
    
    public boolean isMember(UUID playerId) {
        return members.contains(playerId) || owner.equals(playerId);
    }
}