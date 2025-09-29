package com.minekarta.advancedcorerealms.placeholder;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.manager.RealmManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Optional;

public class AdvancedCoreRealmsPlaceholder extends PlaceholderExpansion {

    private final AdvancedCoreRealms plugin;
    private final RealmManager realmManager;

    public AdvancedCoreRealmsPlaceholder(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.realmManager = plugin.getRealmManager();
    }

    @Override
    public boolean persist() {
        return true;
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

        // Placeholders requiring a current realm context, using the fast cache
        Optional<Realm> currentRealmOpt = realmManager.getRealmFromCacheByWorld(player.getWorld().getName());

        if (identifier.startsWith("current_realm_")) {
            if (currentRealmOpt.isEmpty()) {
                return "N/A";
            }
            Realm currentRealm = currentRealmOpt.get();
            switch (identifier) {
                case "current_realm_name":
                    return currentRealm.getName();
                case "current_realm_owner":
                    OfflinePlayer owner = Bukkit.getOfflinePlayer(currentRealm.getOwner());
                    return owner.getName() != null ? owner.getName() : "Unknown";
                case "current_realm_player_count":
                    return currentRealm.getBukkitWorld() != null ? String.valueOf(currentRealm.getBukkitWorld().getPlayers().size()) : "0";
                case "current_realm_max_players":
                    return String.valueOf(currentRealm.getMaxPlayers());
                case "current_realm_type":
                    return currentRealm.getWorldType();
                default:
                    return null;
            }
        }

        // --- Disabled Placeholders ---
        // The following placeholders cannot be supported with the new asynchronous database system
        // without causing significant performance issues (lag) on the server's main thread.
        // They require iterating through all realms or all of a player's realms, which is
        // no longer feasible in a synchronous context like PlaceholderAPI.
        // We are disabling them to prioritize server performance and stability.
        switch (identifier.toLowerCase()) {
            case "total_realms":
            case "player_realms_count":
            case "player_invited_realms_count":
            case "player_total_accessible_realms":
                return "Unsupported"; // Return a clear indicator that this is no longer supported.
        }

        return null; // Placeholder is unknown
    }
}