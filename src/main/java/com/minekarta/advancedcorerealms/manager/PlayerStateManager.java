package com.minekarta.advancedcorerealms.manager;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages player-specific states for menus, such as current page in paginated views
 */
public class PlayerStateManager {
    
    private final AdvancedCoreRealms plugin;
    private final Map<UUID, PlayerState> playerStates = new HashMap<>();
    
    public PlayerStateManager(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Gets or creates a player state for the given player
     * @param player The player
     * @return The player's state
     */
    public PlayerState getState(Player player) {
        return playerStates.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerState());
    }
    
    /**
     * Sets the current realm list page for a player
     * @param player The player
     * @param ownRealms Whether they're viewing their own realms or accessible realms
     * @param page The page number
     */
    public void setRealmsListPage(Player player, boolean ownRealms, int page) {
        PlayerState state = getState(player);
        if (ownRealms) {
            state.currentOwnRealmsPage = page;
        } else {
            state.currentAccessibleRealmsPage = page;
        }
    }
    
    /**
     * Gets the current realm list page for a player
     * @param player The player
     * @param ownRealms Whether they're viewing their own realms or accessible realms
     * @return The current page number
     */
    public int getRealmsListPage(Player player, boolean ownRealms) {
        PlayerState state = getState(player);
        if (ownRealms) {
            return state.currentOwnRealmsPage;
        } else {
            return state.currentAccessibleRealmsPage;
        }
    }
    
    /**
     * Sets the current players list page for a player in a specific realm
     * @param player The player
     * @param realmName The realm name
     * @param page The page number
     */
    public void setRealmPlayersPage(Player player, String realmName, int page) {
        PlayerState state = getState(player);
        state.realmPlayerPages.put(realmName, page);
    }
    
    /**
     * Gets the current players list page for a player in a specific realm
     * @param player The player
     * @param realmName The realm name
     * @return The current page number
     */
    public int getRealmPlayersPage(Player player, String realmName) {
        PlayerState state = getState(player);
        return state.realmPlayerPages.getOrDefault(realmName, 1);
    }
    
    /**
     * Clears all state for a player (should be called when they log out)
     * @param player The player
     */
    public void clearState(Player player) {
        playerStates.remove(player.getUniqueId());
    }
    
    /**
     * Internal class to hold player-specific state
     */
    public static class PlayerState {
        private int currentOwnRealmsPage = 1;
        private int currentAccessibleRealmsPage = 1;
        private final Map<String, Integer> realmPlayerPages = new HashMap<>();
    }
}