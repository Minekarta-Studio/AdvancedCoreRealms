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
import java.util.stream.Collectors;

public class ListCommand implements SubCommand {

    private final LanguageManager languageManager;
    private final RealmManager realmManager;

    public ListCommand(AdvancedCoreRealms plugin) {
        this.languageManager = plugin.getLanguageManager();
        this.realmManager = plugin.getRealmManager();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            languageManager.sendMessage(sender, "error.players_only");
            return;
        }

        List<Realm> ownRealms = realmManager.getRealmsByOwner(player.getUniqueId());
        List<Realm> invitedRealms = realmManager.getMemberRealms(player.getUniqueId()).stream()
                .filter(realm -> !realm.getOwner().equals(player.getUniqueId()))
                .collect(Collectors.toList());

        // Display owned realms
        languageManager.sendMessage(player, "realm.list.header_own");
        if (ownRealms.isEmpty()) {
            languageManager.sendMessage(player, "realm.list.none_own");
        } else {
            for (Realm realm : ownRealms) {
                String worldPath = "realms/" + realm.getWorldFolderName();
                String status = Bukkit.getWorld(worldPath) != null ? "<green>Loaded" : "<red>Unloaded";
                languageManager.sendMessage(player, "realm.list.entry", "%name%", realm.getName(), "%status%", status);
            }
        }

        // Display invited realms
        languageManager.sendMessage(player, "realm.list.header_invited");
        if (invitedRealms.isEmpty()) {
            languageManager.sendMessage(player, "realm.list.none_invited");
        } else {
            for (Realm realm : invitedRealms) {
                String worldPath = "realms/" + realm.getWorldFolderName();
                String status = Bukkit.getWorld(worldPath) != null ? "<green>Loaded" : "<red>Unloaded";
                languageManager.sendMessage(player, "realm.list.entry", "%name%", realm.getName(), "%status%", status);
            }
        }
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