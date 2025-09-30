package com.minekarta.advancedcorerealms.manager;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.realm.Role;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

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
        Optional<Realm> optionalRealm = realmManager.getRealmByName(realmName);

        if (optionalRealm.isEmpty()) {
            languageManager.sendMessage(sender, "error.realm_not_found");
            return;
        }

        Realm realm = optionalRealm.get();
        if (!realm.getOwner().equals(sender.getUniqueId())) {
            languageManager.sendMessage(sender, "error.not-owner");
            return;
        }

        pendingInvites.put(target.getUniqueId(), realmName);

        languageManager.sendMessage(target, "realm.invite_received", "%player%", sender.getName(), "%realm%", realmName);
        languageManager.sendMessage(target, "realm.invite_instructions");
        languageManager.sendMessage(sender, "realm.invite_sent", "%player%", target.getName());
    }

    public void acceptInvite(Player player) {
        UUID playerId = player.getUniqueId();
        String realmName = pendingInvites.remove(playerId);

        if (realmName == null) {
            languageManager.sendMessage(player, "error.no_pending_invites");
            return;
        }

        Optional<Realm> optionalRealm = realmManager.getRealmByName(realmName);
        if (optionalRealm.isEmpty()) {
            languageManager.sendMessage(player, "error.realm_not_found");
            return;
        }

        Realm realm = optionalRealm.get();
        if (realm.isMember(playerId)) {
            // Already a member, just teleport
            plugin.getWorldManager().teleportToRealm(player, realmName);
            return;
        }

        // Add the player as a member and save the realm
        realm.addMember(playerId, Role.MEMBER);
        realmManager.updateRealm(realm).thenRun(() -> {
            languageManager.sendMessage(player, "realm.invite_accepted", "%realm%", realmName);
            plugin.getWorldManager().teleportToRealm(player, realmName);
        }).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, "Failed to accept invite for player " + player.getName(), ex);
            languageManager.sendMessage(player, "error.command_generic");
            // Revert the local change if save failed, though it's not critical as it wasn't persisted
            realm.removeMember(playerId);
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