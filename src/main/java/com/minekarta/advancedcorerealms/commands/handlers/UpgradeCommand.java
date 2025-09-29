package com.minekarta.advancedcorerealms.commands.handlers;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.commands.base.SubCommand;
import com.minekarta.advancedcorerealms.menu.MenuManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class UpgradeCommand implements SubCommand {

    private final MenuManager menuManager;

    public UpgradeCommand(AdvancedCoreRealms plugin) {
        this.menuManager = plugin.getMenuManager();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            // Although the menu is for players, we check just in case.
            // The main command handler already filters for players.
            return;
        }
        menuManager.openUpgradeMenu(player);
    }

    @Override
    public String getName() {
        return "upgrade";
    }

    @Override
    public String getPermission() {
        // The menu itself should handle permissions, so we can leave this null
        // or set a base permission if desired.
        return "advancedcorerealms.user.upgrade";
    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        return Collections.emptyList();
    }
}