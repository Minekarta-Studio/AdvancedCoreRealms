package com.minekarta.advancedcorerealms.commands.handlers;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.commands.base.SubCommand;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.manager.LanguageManager;
import com.minekarta.advancedcorerealms.worldborder.WorldBorderConfig;
import com.minekarta.advancedcorerealms.worldborder.WorldBorderManager;
import com.minekarta.advancedcorerealms.worldborder.WorldBorderTier;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BorderCommand implements SubCommand {

    private final AdvancedCoreRealms plugin;
    private final LanguageManager lang;
    private final WorldBorderManager worldBorderManager;
    private final WorldBorderConfig worldBorderConfig;

    public BorderCommand(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLanguageManager();
        this.worldBorderManager = plugin.getWorldBorderManager();
        this.worldBorderConfig = plugin.getWorldBorderConfig();
    }

    @Override
    public String getName() {
        return "border";
    }

    @Override
    public String getPermission() {
        return "advancedcorerealms.admin.border";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 3 || !args[0].equalsIgnoreCase("upgrade")) {
            lang.sendMessage(sender, "command.border.usage", "%usage%", "/realms border upgrade <world> <tier>");
            return;
        }

        String worldName = args[1];
        String targetTierId = args[2];

        plugin.getRealmManager().getRealmByWorldName(worldName).thenAcceptAsync(realm -> {
            if (realm == null) {
                lang.sendMessage(sender, "error.realm-not-found", "%name%", worldName);
                return;
            }

            WorldBorderTier targetTier = worldBorderConfig.getTier(targetTierId);
            if (targetTier == null) {
                lang.sendMessage(sender, "command.border.tier-not-found", "%tier%", targetTierId);
                return;
            }

            if (realm.getBorderTierId().equalsIgnoreCase(targetTierId)) {
                lang.sendMessage(sender, "command.border.already-on-tier", "%tier%", targetTierId);
                return;
            }

            // Execute the upgrade by applying the new tier
            worldBorderManager.setBorderTier(realm, targetTierId);

            // Notify the command sender
            lang.sendMessage(sender, "command.border.upgrade-success", "%world%", worldName, "%tier%", targetTierId);

        }).exceptionally(ex -> {
            sender.sendMessage("§cAn unexpected error occurred while fetching the realm.");
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Failed to get realm for border command", ex);
            return null;
        });
    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        if (args.length == 1) {
            return List.of("upgrade");
        }
        if (args.length == 2) {
            // Suggest world names
            return Bukkit.getWorlds().stream()
                    .map(World::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 3) {
            // Suggest available tier IDs
            return worldBorderConfig.getAllTiers().keySet().stream()
                    .filter(tierId -> tierId.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}