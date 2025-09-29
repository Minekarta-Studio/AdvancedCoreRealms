package com.minekarta.advancedcorerealms.commands.handlers;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.commands.base.SubCommand;
import com.minekarta.advancedcorerealms.manager.LanguageManager;
import com.minekarta.advancedcorerealms.manager.RealmManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TransferCommand implements SubCommand {

    private final AdvancedCoreRealms plugin;
    private final LanguageManager languageManager;
    private final RealmManager realmManager;

    public TransferCommand(AdvancedCoreRealms plugin) {
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

        if (args.length < 3) {
            languageManager.sendMessage(player, "error.usage.transfer");
            return;
        }

        String realmName = args[1];
        Player newOwner = Bukkit.getPlayer(args[2]);

        if (newOwner == null) {
            languageManager.sendMessage(player, "error.player_not_online");
            return;
        }

        realmManager.getRealmByName(realmName).thenCompose(realm -> {
            if (realm == null) {
                languageManager.sendMessage(player, "error.realm_not_found");
                return null;
            }

            realm.setOwner(newOwner.getUniqueId());
            return realmManager.updateRealm(realm);

        }).thenRun(() -> {
            languageManager.sendMessage(player, "realm.transfer_success");
        }).exceptionally(ex -> {
            languageManager.sendMessage(player, "error.command_generic");
            plugin.getLogger().severe("Error during realm transfer: " + ex.getMessage());
            return null;
        });
    }

    @Override
    public String getName() {
        return "transfer";
    }

    @Override
    public String getPermission() {
        return "advancedcorerealms.admin.transfer";
    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        if (args.length == 2) {
            // Suggest all realm names for admins
            // Note: This could be slow on large servers, but it's an admin command.
            // A more optimized solution would involve a cached list of realm names.
            return Collections.emptyList();
        }
        if (args.length == 3) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}