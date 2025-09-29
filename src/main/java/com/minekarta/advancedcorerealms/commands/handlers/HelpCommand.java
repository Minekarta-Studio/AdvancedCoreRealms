package com.minekarta.advancedcorerealms.commands.handlers;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.commands.base.SubCommand;
import com.minekarta.advancedcorerealms.manager.LanguageManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class HelpCommand implements SubCommand {

    private final LanguageManager languageManager;

    public HelpCommand(AdvancedCoreRealms plugin) {
        this.languageManager = plugin.getLanguageManager();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        languageManager.sendMessage(sender, "command.help");
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getPermission() {
        return "advancedcorerealms.user.help";
    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        return Collections.emptyList();
    }
}