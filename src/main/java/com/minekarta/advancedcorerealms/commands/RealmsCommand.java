package com.minekarta.advancedcorerealms.commands;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.commands.base.SubCommand;
import com.minekarta.advancedcorerealms.commands.handlers.*;
import com.minekarta.advancedcorerealms.manager.LanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Main command executor for the /realms command.
 * This class acts as a dispatcher, routing subcommands to their respective
 * {@link SubCommand} handlers. It is responsible for registering all subcommands
 * and performing initial permission checks.
 */
public class RealmsCommand implements CommandExecutor, TabCompleter {

    private final AdvancedCoreRealms plugin;
    private final LanguageManager languageManager;
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public RealmsCommand(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();

        registerSubCommands();
    }

    private void registerSubCommands() {
        registerSubCommand(new CreateCommand(plugin));
        registerSubCommand(new DeleteCommand(plugin));
        registerSubCommand(new TeleportCommand(plugin));
        registerSubCommand(new ListCommand(plugin));
        registerSubCommand(new InviteCommand(plugin));
        registerSubCommand(new AcceptCommand(plugin));
        registerSubCommand(new DenyCommand(plugin));
        registerSubCommand(new HelpCommand(plugin));
        registerSubCommand(new ReloadCommand(plugin));
        registerSubCommand(new TransferCommand(plugin));
        registerSubCommand(new BackCommand(plugin));
        registerSubCommand(new UpgradeCommand(plugin));
        registerSubCommand(new BorderCommand(plugin));
    }

    private void registerSubCommand(SubCommand subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
        for (String alias : subCommand.getAliases()) {
            subCommands.put(alias.toLowerCase(), subCommand);
        }
    }

    /**
     * Handles the execution of the /realms command and its subcommands.
     *
     * @param sender  The entity who sent the command.
     * @param command The command that was executed.
     * @param label   The alias of the command that was used.
     * @param args    The arguments passed to the command.
     * @return true if the command was handled, false otherwise.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            languageManager.sendMessage(sender, "error.players_only");
            return true;
        }

        if (args.length == 0) {
            if (!player.hasPermission("advancedcorerealms.user.base")) {
                languageManager.sendMessage(player, "error.no-permission");
                return true;
            }
            plugin.getMenuManager().openMainMenu(player);
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand == null) {
            languageManager.sendMessage(player, "command.help");
            return true;
        }

        if (subCommand.getPermission() != null && !player.hasPermission(subCommand.getPermission())) {
            languageManager.sendMessage(player, "error.no-permission");
            return true;
        }

        subCommand.execute(player, args);
        return true;
    }

    /**
     * Provides tab completions for the /realms command.
     *
     * @param sender The entity who is tab-completing.
     * @param command The command being tab-completed.
     * @param alias   The alias of the command being used.
     * @param args    The current arguments.
     * @return A list of tab completions.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            // Suggest subcommands the player has permission for
            return subCommands.keySet().stream()
                    .filter(name -> {
                        SubCommand sub = subCommands.get(name);
                        return sub.getPermission() == null || player.hasPermission(sub.getPermission());
                    })
                    .map(String::toLowerCase)
                    .filter(name -> name.startsWith(args[0].toLowerCase()))
                    .distinct()
                    .collect(Collectors.toList());
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand != null && (subCommand.getPermission() == null || player.hasPermission(subCommand.getPermission()))) {
            return subCommand.onTabComplete(player, args);
        }

        return new ArrayList<>();
    }
}