package com.minekarta.advancedcorerealms.manager;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InviteManager {

    private final AdvancedCoreRealms plugin;
    private final LanguageManager languageManager;
    private final Map<UUID, Map<UUID, String>> pendingInvites;

    public InviteManager(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
        this.pendingInvites = new HashMap<>();
        
        // Start a repeating task to clean up expired invites
        Bukkit.getScheduler().runTaskTimer(plugin, this::cleanupExpiredInvites, 20*60, 20*60); // Every minute
    }

    public void sendInvite(Player sender, Player target, String realmName) {
        Realm realm = plugin.getWorldDataManager().getRealm(realmName);
        if (realm == null) {
            languageManager.sendMessage(sender, "error.realm_not_found");
            return;
        }

        if (!realm.getOwner().equals(sender.getUniqueId())) {
            languageManager.sendMessage(sender, "error.not-owner");
            return;
        }

        pendingInvites.computeIfAbsent(sender.getUniqueId(), k -> new HashMap<>())
                      .put(target.getUniqueId(), realmName);

        languageManager.sendMessage(target, "realm.invite_received", "%player%", sender.getName(), "%realm%", realmName);
        languageManager.sendMessage(target, "realm.invite_instructions");
        languageManager.sendMessage(sender, "realm.invite_sent", "%player%", target.getName());
    }

    public void acceptInvite(Player player) {
        UUID playerId = player.getUniqueId();

        for (Map.Entry<UUID, Map<UUID, String>> outerEntry : pendingInvites.entrySet()) {
            Map<UUID, String> innerMap = outerEntry.getValue();
            if (innerMap.containsKey(playerId)) {
                String realmName = innerMap.get(playerId);
                Realm realm = plugin.getWorldDataManager().getRealm(realmName);
                if (realm != null) {
                    if (!realm.getMembers().contains(playerId)) {
                        realm.getMembers().add(playerId);
                        plugin.getWorldDataManager().saveData();
                        languageManager.sendMessage(player, "realm.invite_accepted", "%realm%", realmName);
                        
                        innerMap.remove(playerId);
                        if (innerMap.isEmpty()) {
                            pendingInvites.remove(outerEntry.getKey());
                        }
                        
                        plugin.getWorldManager().teleportToRealm(player, realmName);
                        return;
                    }
                }
            }
        }
        languageManager.sendMessage(player, "error.no_pending_invites");
    }

    public void denyInvite(Player player) {
        UUID playerId = player.getUniqueId();

        for (Map.Entry<UUID, Map<UUID, String>> outerEntry : pendingInvites.entrySet()) {
            Map<UUID, String> innerMap = outerEntry.getValue();
            if (innerMap.containsKey(playerId)) {
                String realmName = innerMap.get(playerId);
                languageManager.sendMessage(player, "realm.invite_denied", "%realm%", realmName);
                
                innerMap.remove(playerId);
                if (innerMap.isEmpty()) {
                    pendingInvites.remove(outerEntry.getKey());
                }
                return;
            }
        }
        languageManager.sendMessage(player, "error.no_pending_invites");
    }

    private void cleanupExpiredInvites() {
        // Implementation for expiring invites would go here.
    }

    public boolean hasPendingInvite(Player player) {
        UUID playerId = player.getUniqueId();
        for (Map<UUID, String> invites : pendingInvites.values()) {
            if (invites.containsKey(playerId)) {
                return true;
            }
        }
        return false;
    }
}