package com.minekarta.advancedcorerealms.placeholder;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;

public class AdvancedCoreRealmsPlaceholder extends PlaceholderExpansion {

    private final AdvancedCoreRealms plugin;

    public AdvancedCoreRealmsPlaceholder(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean persist() {
        return true; // This is supposed to be the default, but it's better to return it anyway
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return "advancedcorerealms";
    }

    @Override
    public String getAuthor() {
        return String.join(",", plugin.getDescription().getAuthors());
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return null;
        }

        switch (identifier.toLowerCase()) {
            case "total_realms":
                return String.valueOf(plugin.getWorldDataManager().getAllRealms().size());
                
            case "player_realms_count":
                return String.valueOf(plugin.getWorldDataManager().getPlayerRealms(player.getUniqueId()).size());
                
            case "player_invited_realms_count":
                return String.valueOf(plugin.getWorldDataManager().getPlayerInvitedRealms(player.getUniqueId()).size());
                
            case "player_total_accessible_realms":
                List<Realm> ownedRealms = plugin.getWorldDataManager().getPlayerRealms(player.getUniqueId());
                List<Realm> invitedRealms = plugin.getWorldDataManager().getPlayerInvitedRealms(player.getUniqueId());
                return String.valueOf(ownedRealms.size() + invitedRealms.size());
                
            case "player_current_realm":
                org.bukkit.World currentWorld = player.getWorld();
                if (plugin.getWorldDataManager().getRealm(currentWorld.getName()) != null) {
                    return currentWorld.getName();
                }
                return "None";
                
            case "current_realm_owner":
                org.bukkit.World currentWorld2 = player.getWorld();
                Realm currentRealm = plugin.getWorldDataManager().getRealm(currentWorld2.getName());
                if (currentRealm != null) {
                    org.bukkit.OfflinePlayer owner = org.bukkit.Bukkit.getOfflinePlayer(currentRealm.getOwner());
                    return owner.getName() != null ? owner.getName() : "Unknown";
                }
                return "N/A";
                
            case "current_realm_player_count":
                org.bukkit.World currentWorld3 = player.getWorld();
                Realm currentRealm2 = plugin.getWorldDataManager().getRealm(currentWorld3.getName());
                if (currentRealm2 != null && currentRealm2.getBukkitWorld() != null) {
                    return String.valueOf(currentRealm2.getBukkitWorld().getPlayers().size());
                }
                return "0";
                
            case "current_realm_max_players":
                org.bukkit.World currentWorld4 = player.getWorld();
                Realm currentRealm3 = plugin.getWorldDataManager().getRealm(currentWorld4.getName());
                if (currentRealm3 != null) {
                    return String.valueOf(currentRealm3.getMaxPlayers());
                }
                return "8"; // Default max players
                
            case "current_realm_type":
                org.bukkit.World currentWorld5 = player.getWorld();
                Realm currentRealm4 = plugin.getWorldDataManager().getRealm(currentWorld5.getName());
                if (currentRealm4 != null) {
                    return currentRealm4.getWorldType();
                }
                return "N/A";
                
            default:
                if (identifier.toLowerCase().startsWith("player_realm_")) {
                    // Check if it's a realm existence check like "player_realm_{name}_exists"
                    if (identifier.toLowerCase().endsWith("_exists")) {
                        String realmName = identifier.toLowerCase().substring(13, identifier.length() - 7); // Remove "player_realm_" and "_exists"
                        Realm realm = plugin.getWorldDataManager().getRealm(realmName);
                        if (realm != null) {
                            // Check if player has access to this realm
                            if (realm.isMember(player.getUniqueId()) || 
                                plugin.getWorldDataManager().getPlayerRealms(player.getUniqueId()).contains(realm)) {
                                return "true";
                            }
                        }
                        return "false";
                    }
                }
                break;
        }

        return null; // Placeholder is unknown
    }
}