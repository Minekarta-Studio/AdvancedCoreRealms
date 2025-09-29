package com.minekarta.advancedcorerealms.commands.handlers;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.commands.base.SubCommand;
import com.minekarta.advancedcorerealms.manager.LanguageManager;
import com.minekarta.advancedcorerealms.manager.world.WorldManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TeleportCommand implements SubCommand {

    private final LanguageManager languageManager;
    private final WorldManager worldManager;

    public TeleportCommand(AdvancedCoreRealms plugin) {
        this.languageManager = plugin.getLanguageManager();
        this.worldManager = plugin.getWorldManager();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            languageManager.sendMessage(sender, "error.players_only");
            return;
        }

        if (args.length < 2) {
            languageManager.sendMessage(player, "error.usage.teleport");
            return;
        }

        String worldName = args[1];
        worldManager.teleportToRealm(player, worldName);
    }

    @Override
    public String getName() {
        // This handler will be registered for both "tp" and "teleport"
        return "teleport";
    }

    @Override
    public String getPermission() {
        return "advancedcorerealms.user.teleport";
    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        // Tab completion for realm names would be useful here, but for now, returning empty.
        return Collections.emptyList();
    }

    /**
     * Gets all aliases for this command.
     * @return A list of aliases.
     */
    public List<String> getAliases() {
        return Arrays.asList("tp");
    }
}