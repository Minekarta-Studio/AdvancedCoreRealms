package com.minekarta.advancedcorerealms.commands.base;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Represents a subcommand for the /realms command.
 */
public interface SubCommand {

    /**
     * Executes the subcommand.
     *
     * @param sender The CommandSender who issued the command.
     * @param args   The arguments provided with the command.
     */
    void execute(CommandSender sender, String[] args);

    /**
     * Gets the name of the subcommand.
     *
     * @return The name of the subcommand.
     */
    String getName();

    /**
     * Gets the permission required to execute this subcommand.
     *
     * @return The permission string, or null if no permission is required.
     */
    String getPermission();

    /**
     * Provides tab completions for the subcommand.
     *
     * @param player The player who is tab-completing.
     * @param args   The current command arguments.
     * @return A list of tab completions.
     */
    List<String> onTabComplete(Player player, String[] args);

    /**
     * Gets the aliases for the subcommand.
     *
     * @return A list of aliases.
     */
    default List<String> getAliases() {
        return java.util.Collections.emptyList();
    }
}