package com.minekarta.advancedcorerealms.placeholder;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.manager.RealmManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Optional;

public class AdvancedCoreRealmsPlaceholder extends PlaceholderExpansion {

    private final AdvancedCoreRealms plugin;
    private final RealmManager realmManager;

    public AdvancedCoreRealmsPlaceholder(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.realmManager = plugin.getRealmManager();
    }

    private Optional<Realm> getRealmFromWorld(World world) {
        if (world == null) return Optional.empty();
        String worldName = world.getName();
        if (worldName.startsWith("realms/")) {
            String worldFolderName = worldName.substring("realms/".length());
            return realmManager.getRealmByWorldFolderName(worldFolderName);
        }
        return Optional.empty();
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

        // Placeholders requiring a current realm context
        Optional<Realm> currentRealmOpt = getRealmFromWorld(player.getWorld());

        if (identifier.startsWith("current_realm_")) {
            if (currentRealmOpt.isEmpty()) {
                return "N/A"; // Or some other default value
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

        // General placeholders not dependent on the current realm
        switch (identifier.toLowerCase()) {
            case "total_realms":
                return String.valueOf(realmManager.getAllCachedRealms().size());
            case "player_realms_count":
                return String.valueOf(realmManager.getRealmsByOwner(player.getUniqueId()).size());
            case "player_invited_realms_count":
                long invitedCount = realmManager.getMemberRealms(player.getUniqueId()).stream()
                        .filter(realm -> !realm.getOwner().equals(player.getUniqueId()))
                        .count();
                return String.valueOf(invitedCount);
            case "player_total_accessible_realms":
                 long totalCount = realmManager.getMemberRealms(player.getUniqueId()).size();
                return String.valueOf(totalCount);
        }

        return null; // Placeholder is unknown
    }
}