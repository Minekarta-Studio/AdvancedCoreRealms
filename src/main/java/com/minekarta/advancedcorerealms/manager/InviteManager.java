package com.minekarta.advancedcorerealms.manager;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.realm.Role;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InviteManager {

    private final AdvancedCoreRealms plugin;
    private final LanguageManager languageManager;
    private final RealmManager realmManager;
    private final Map<UUID, String> pendingInvites = new HashMap<>(); // Player UUID -> Realm Name

    public InviteManager(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
        this.realmManager = plugin.getRealmManager();
    }

    public void sendInvite(Player sender, Player target, String realmName) {
        realmManager.getRealmByName(realmName).thenAccept(realm -> {
            if (realm == null) {
                languageManager.sendMessage(sender, "error.realm_not_found");
                return;
            }

            if (!realm.getOwner().equals(sender.getUniqueId())) {
                languageManager.sendMessage(sender, "error.not-owner");
                return;
            }

            pendingInvites.put(target.getUniqueId(), realmName);

            languageManager.sendMessage(target, "realm.invite_received", "%player%", sender.getName(), "%realm%", realmName);
            languageManager.sendMessage(target, "realm.invite_instructions");
            languageManager.sendMessage(sender, "realm.invite_sent", "%player%", target.getName());
        });
    }

    public void acceptInvite(Player player) {
        UUID playerId = player.getUniqueId();
        String realmName = pendingInvites.remove(playerId);

        if (realmName == null) {
            languageManager.sendMessage(player, "error.no_pending_invites");
            return;
        }

        realmManager.getRealmByName(realmName).thenCompose(realm -> {
            if (realm == null) {
                languageManager.sendMessage(player, "error.realm_not_found");
                return null;
            }

            if (realm.isMember(playerId)) {
                // Already a member, just teleport
                plugin.getWorldManager().teleportToRealm(player, realmName);
                return null;
            }

            // Add the player as a member to the local object for the teleport check
            realm.addMember(playerId, Role.MEMBER);

            // Save the new member to the database, which will also handle cache invalidation
            return realmManager.addMemberToRealm(realm, playerId, Role.MEMBER);

        }).thenRun(() -> {
            languageManager.sendMessage(player, "realm.invite_accepted", "%realm%", realmName);
            plugin.getWorldManager().teleportToRealm(player, realmName);
        }).exceptionally(ex -> {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Failed to accept invite for player " + player.getName(), ex);
            languageManager.sendMessage(player, "error.command_generic");
            return null;
        });
    }

    public void denyInvite(Player player) {
        UUID playerId = player.getUniqueId();
        String realmName = pendingInvites.remove(playerId);

        if (realmName == null) {
            languageManager.sendMessage(player, "error.no_pending_invites");
            return;
        }

        languageManager.sendMessage(player, "realm.invite_denied", "%realm%", realmName);
    }
}