package com.minekarta.advancedcorerealms.commands.handlers;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.commands.base.SubCommand;
import com.minekarta.advancedcorerealms.manager.InviteManager;
import com.minekarta.advancedcorerealms.manager.LanguageManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class DenyCommand implements SubCommand {

    private final LanguageManager languageManager;
    private final InviteManager inviteManager;

    public DenyCommand(AdvancedCoreRealms plugin) {
        this.languageManager = plugin.getLanguageManager();
        this.inviteManager = plugin.getInviteManager();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            languageManager.sendMessage(sender, "error.players_only");
            return;
        }

        inviteManager.denyInvite(player);
    }

    @Override
    public String getName() {
        return "deny";
    }

    @Override
    public String getPermission() {
        return "advancedcorerealms.user.deny";
    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        return Collections.emptyList();
    }
}