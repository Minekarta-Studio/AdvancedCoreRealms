package com.minekarta.advancedcorerealms.commands.handlers;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.commands.base.SubCommand;
import com.minekarta.advancedcorerealms.manager.InviteManager;
import com.minekarta.advancedcorerealms.manager.LanguageManager;
import com.minekarta.advancedcorerealms.manager.RealmManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InviteCommand implements SubCommand {

    private final AdvancedCoreRealms plugin;
    private final LanguageManager languageManager;
    private final RealmManager realmManager;
    private final InviteManager inviteManager;

    public InviteCommand(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
        this.realmManager = plugin.getRealmManager();
        this.inviteManager = plugin.getInviteManager();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            languageManager.sendMessage(sender, "error.players_only");
            return;
        }

        if (args.length < 3) {
            languageManager.sendMessage(player, "error.usage.invite");
            return;
        }

        String realmName = args[1];
        Player targetPlayer = Bukkit.getPlayer(args[2]);

        if (targetPlayer == null) {
            languageManager.sendMessage(player, "error.player_not_online");
            return;
        }

        realmManager.getRealmByName(realmName).thenAccept(realm -> {
            if (realm == null) {
                languageManager.sendMessage(player, "error.realm_not_found");
                return;
            }

            if (!realm.getOwner().equals(player.getUniqueId())) {
                languageManager.sendMessage(player, "error.not-owner");
                return;
            }

            // Ensure invite logic is run on the main thread if it interacts with Bukkit API
            Bukkit.getScheduler().runTask(plugin, () ->
                inviteManager.sendInvite(player, targetPlayer, realmName)
            );

        }).exceptionally(ex -> {
            plugin.getLogger().severe("Error during invite command: " + ex.getMessage());
            languageManager.sendMessage(player, "error.command_generic");
            return null;
        });
    }

    @Override
    public String getName() {
        return "invite";
    }

    @Override
    public String getPermission() {
        return "advancedcorerealms.user.invite";
    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        if (args.length == 2) {
            // Tab-completion for realm names cannot be done without blocking or a complex cache.
            // Returning an empty list is the safest option for server performance.
            return Collections.emptyList();
        }
        if (args.length == 3) {
            // Suggest online players, which is a non-blocking operation.
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}