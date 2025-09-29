package com.minekarta.advancedcorerealms.commands.handlers;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.commands.base.SubCommand;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.manager.LanguageManager;
import com.minekarta.advancedcorerealms.manager.RealmManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ListCommand implements SubCommand {

    private final AdvancedCoreRealms plugin;
    private final LanguageManager languageManager;
    private final RealmManager realmManager;

    public ListCommand(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
        this.realmManager = plugin.getRealmManager();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            languageManager.sendMessage(sender, "error.players_only");
            return;
        }

        CompletableFuture<List<Realm>> ownRealmsFuture = realmManager.getRealmsByOwner(player.getUniqueId());
        CompletableFuture<List<Realm>> invitedRealmsFuture = realmManager.getInvitedRealms(player.getUniqueId());

        CompletableFuture.allOf(ownRealmsFuture, invitedRealmsFuture).thenRun(() -> {
            List<Realm> ownRealms = ownRealmsFuture.join();
            List<Realm> invitedRealms = invitedRealmsFuture.join();

            // Display owned realms
            languageManager.sendMessage(player, "realm.list.header_own");
            if (ownRealms.isEmpty()) {
                languageManager.sendMessage(player, "realm.list.none_own");
            } else {
                for (Realm realm : ownRealms) {
                    String status = Bukkit.getWorld(realm.getWorldName()) != null ? "<green>Loaded" : "<red>Unloaded";
                    languageManager.sendMessage(player, "realm.list.entry", "%name%", realm.getName(), "%status%", status);
                }
            }

            // Display invited realms
            languageManager.sendMessage(player, "realm.list.header_invited");
            if (invitedRealms.isEmpty()) {
                languageManager.sendMessage(player, "realm.list.none_invited");
            } else {
                for (Realm realm : invitedRealms) {
                    String status = Bukkit.getWorld(realm.getWorldName()) != null ? "<green>Loaded" : "<red>Unloaded";
                    languageManager.sendMessage(player, "realm.list.entry", "%name%", realm.getName(), "%status%", status);
                }
            }

        }).exceptionally(ex -> {
            languageManager.sendMessage(player, "error.command_generic");
            plugin.getLogger().severe("Error fetching player realms: " + ex.getMessage());
            return null;
        });
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getPermission() {
        return "advancedcorerealms.user.list";
    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        return Collections.emptyList();
    }
}