package com.minekarta.advancedcorerealms.commands.handlers;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.commands.base.SubCommand;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.manager.LanguageManager;
import com.minekarta.advancedcorerealms.worldborder.WorldBorderManager;
import com.minekarta.advancedcorerealms.worldborder.WorldBorderTier;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the {@code /realms border} subcommand, allowing administrators to manage
 * the world border of a specific realm.
 * <p>
 * This command currently supports one primary action: {@code upgrade}.
 * <p>
 * <b>Usage:</b> {@code /realms border upgrade <world_name> <tier_id>}
 * <p>
 * This command performs several validation checks:
 * <ul>
 *     <li>Ensures the sender has the required permission ({@code advancedcorerealms.admin.border}).</li>
 *     <li>Verifies that the specified realm and target tier exist.</li>
 *     <li>Prevents setting a realm to the tier it already has.</li>
 * </ul>
 * Upon successful execution, it delegates the border change to the {@link WorldBorderManager}.
 */
public class BorderCommand extends SubCommand {

    private final AdvancedCoreRealms plugin;
    private final LanguageManager lang;
    private final WorldBorderManager worldBorderManager;

    public BorderCommand(AdvancedCoreRealms plugin) {
        super("border", "Manages realm world borders.", "/realms border upgrade <world> <tier>", "advancedcorerealms.admin.border", "b");
        this.plugin = plugin;
        this.lang = plugin.getLanguageManager();
        this.worldBorderManager = plugin.getWorldBorderManager();
    }

    /**
     * Executes the border management command.
     *
     * @param sender The command sender.
     * @param args   The arguments provided with the command. Expects: [border, upgrade, world_name, tier_id].
     */
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 4 || !args[1].equalsIgnoreCase("upgrade")) {
            lang.sendMessage(sender, "command.border.usage", s -> s.replace("%usage%", getUsage()));
            return;
        }

        String worldName = args[2];
        String targetTierId = args[3];

        plugin.getRealmManager().getRealmByWorldName(worldName).thenAcceptAsync(realm -> {
            if (realm == null) {
                lang.sendMessage(sender, "error.realm-not-found", s -> s.replace("%name%", worldName));
                return;
            }

            WorldBorderTier targetTier = plugin.getWorldBorderConfig().getTier(targetTierId);
            if (targetTier == null) {
                lang.sendMessage(sender, "command.border.tier-not-found", s -> s.replace("%tier%", targetTierId));
                return;
            }

            if (realm.getBorderTierId().equalsIgnoreCase(targetTierId)) {
                lang.sendMessage(sender, "command.border.already-on-tier", s -> s.replace("%tier%", targetTierId));
                return;
            }

            // Execute the upgrade
            worldBorderManager.setBorderTier(realm, targetTierId);

            // Notify the command sender
            lang.sendMessage(sender, "command.border.upgrade-success", s -> s
                    .replace("%world%", worldName)
                    .replace("%tier%", targetTierId));

        }).exceptionally(ex -> {
            sender.sendMessage("§cAn unexpected error occurred while fetching the realm.");
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Failed to get realm for border command", ex);
            return null;
        });
    }

    /**
     * Provides tab completions for the border subcommand.
     * It suggests "upgrade", world names, and available tier IDs based on the current argument.
     *
     * @param sender The command sender.
     * @param args   The current command arguments.
     * @return A list of suggested completions.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return List.of("upgrade");
        }
        if (args.length == 3) {
            // Suggest world names
            return Bukkit.getWorlds().stream()
                    .map(World::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 4) {
            // Suggest available tier IDs
            return plugin.getWorldBorderConfig().getAllTiers().keySet().stream()
                    .filter(tierId -> tierId.toLowerCase().startsWith(args[3].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}