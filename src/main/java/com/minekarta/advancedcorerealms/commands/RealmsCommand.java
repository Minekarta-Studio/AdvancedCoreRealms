package com.minekarta.advancedcorerealms.commands;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.api.AdvancedCorePlayer;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.manager.LanguageManager;
import com.minekarta.advancedcorerealms.manager.world.WorldManager;
import com.minekarta.advancedcorerealms.worldborder.BorderColor;
import com.minekarta.advancedcorerealms.worldborder.PlayerChangeBorderColorEvent;
import com.minekarta.advancedcorerealms.worldborder.PlayerToggleBorderEvent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RealmsCommand implements CommandExecutor, TabCompleter {

    private final AdvancedCoreRealms plugin;
    private final WorldManager worldManager;
    private final LanguageManager languageManager;

    public RealmsCommand(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.worldManager = plugin.getWorldManager();
        this.languageManager = plugin.getLanguageManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            languageManager.sendMessage(sender, "error.players_only");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            if (!player.hasPermission("advancedcorerealms.user.base")) {
                languageManager.sendMessage(player, "error.no-permission");
                return true;
            }
            plugin.getMenuManager().openMainMenu(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                handleHelp(player);
                break;
            case "create":
                handleCreate(player, args);
                break;
            case "delete":
                handleDelete(player, args);
                break;
            case "tp":
            case "teleport":
                handleTeleport(player, args);
                break;
            case "list":
                handleList(player);
                break;
            case "invite":
                handleInvite(player, args);
                break;
            case "accept":
                handleAccept(player);
                break;
            case "deny":
                handleDeny(player);
                break;
            case "reload":
                handleReload(player);
                break;
            case "transfer":
                handleTransfer(player, args);
                break;
            case "back":
                handleBack(player);
                break;
            case "upgrade":
                handleUpgrade(player, args);
                break;
            default:
                languageManager.sendMessage(player, "command.help");
                break;
        }

        return true;
    }

    private void handleHelp(Player player) {
        if (!player.hasPermission("advancedcorerealms.user.help")) {
            languageManager.sendMessage(player, "error.no-permission");
            return;
        }
        languageManager.sendMessage(player, "command.help");
    }

    private void handleCreate(Player player, String[] args) {
        if (!player.hasPermission("advancedcorerealms.user.create")) {
            languageManager.sendMessage(player, "error.no-permission");
            return;
        }

        if (args.length < 2) {
            languageManager.sendMessage(player, "error.usage.create");
            return;
        }

        String realmName = args[1];

        if (plugin.getWorldDataManager().getRealm(realmName) != null) {
            languageManager.sendMessage(player, "error.world-exists");
            return;
        }

        int maxRealms = 1;
        if (player.hasPermission("advancedcorerealms.limit.realms.5")) maxRealms = 5;
        else if (player.hasPermission("advancedcorerealms.limit.realms.3")) maxRealms = 3;

        if (plugin.getWorldDataManager().getPlayerRealms(player.getUniqueId()).size() >= maxRealms) {
            languageManager.sendMessage(player, "error.max_realms_reached");
            return;
        }

        // The template type is the second argument (optional), defaults to "default"
        String templateType = "default";
        if (args.length >= 3) {
            templateType = args[2];
        }

        plugin.getPlayerDataManager().savePreviousLocation(player.getUniqueId(), player.getLocation());
        plugin.getRealmCreator().createRealmAsync(player, realmName, templateType);
    }

    private void handleDelete(Player player, String[] args) {
        if (!player.hasPermission("advancedcorerealms.user.delete")) {
            languageManager.sendMessage(player, "error.no-permission");
            return;
        }

        if (args.length < 2) {
            languageManager.sendMessage(player, "error.usage.delete");
            return;
        }

        String worldName = args[1];
        Realm realm = plugin.getWorldDataManager().getRealm(worldName);

        if (realm == null) {
            languageManager.sendMessage(player, "error.realm_not_found");
            return;
        }

        if (!realm.getOwner().equals(player.getUniqueId()) && !player.hasPermission("advancedcorerealms.admin.*")) {
            languageManager.sendMessage(player, "error.not-owner");
            return;
        }

        worldManager.deleteWorld(player, worldName);
    }

    private void handleTeleport(Player player, String[] args) {
        if (!player.hasPermission("advancedcorerealms.user.teleport")) {
            languageManager.sendMessage(player, "error.no-permission");
            return;
        }

        if (args.length < 2) {
            languageManager.sendMessage(player, "error.usage.teleport");
            return;
        }

        String worldName = args[1];
        worldManager.teleportToRealm(player, worldName);
    }

    private void handleList(Player player) {
        if (!player.hasPermission("advancedcorerealms.user.list")) {
            languageManager.sendMessage(player, "error.no-permission");
            return;
        }

        languageManager.sendMessage(player, "realm.list.header_own");
        for (Realm realm : plugin.getWorldDataManager().getPlayerRealms(player.getUniqueId())) {
            String status = Bukkit.getWorld(realm.getName()) != null ? "<green>Loaded" : "<red>Unloaded";
            languageManager.sendMessage(player, "realm.list.entry", "%name%", realm.getName(), "%status%", status);
        }

        List<Realm> invitedRealms = plugin.getWorldDataManager().getPlayerInvitedRealms(player.getUniqueId());
        if (!invitedRealms.isEmpty()) {
            languageManager.sendMessage(player, "realm.list.header_invited");
            for (Realm realm : invitedRealms) {
                String status = Bukkit.getWorld(realm.getName()) != null ? "<green>Loaded" : "<red>Unloaded";
                languageManager.sendMessage(player, "realm.list.entry", "%name%", realm.getName(), "%status%", status);
            }
        }
    }

    private void handleInvite(Player player, String[] args) {
        if (!player.hasPermission("advancedcorerealms.user.invite")) {
            languageManager.sendMessage(player, "error.no-permission");
            return;
        }

        if (args.length < 3) {
            languageManager.sendMessage(player, "error.usage.invite");
            return;
        }

        String worldName = args[1];
        Realm realm = plugin.getWorldDataManager().getRealm(worldName);
        if (realm == null) {
            languageManager.sendMessage(player, "error.realm_not_found");
            return;
        }

        if (!realm.getOwner().equals(player.getUniqueId())) {
            languageManager.sendMessage(player, "error.not-owner");
            return;
        }

        Player targetPlayer = Bukkit.getPlayer(args[2]);
        if (targetPlayer == null) {
            languageManager.sendMessage(player, "error.player_not_online");
            return;
        }

        plugin.getInviteManager().sendInvite(player, targetPlayer, worldName);
    }

    private void handleAccept(Player player) {
        if (!player.hasPermission("advancedcorerealms.user.accept")) {
            languageManager.sendMessage(player, "error.no-permission");
            return;
        }
        plugin.getInviteManager().acceptInvite(player);
    }

    private void handleDeny(Player player) {
        if (!player.hasPermission("advancedcorerealms.user.deny")) {
            languageManager.sendMessage(player, "error.no-permission");
            return;
        }
        plugin.getInviteManager().denyInvite(player);
    }

    private void handleReload(Player player) {
        if (!player.hasPermission("advancedcorerealms.admin.reload")) {
            languageManager.sendMessage(player, "error.no-permission");
            return;
        }
        plugin.reloadConfig();
        plugin.getLanguageManager().loadLanguage();
        languageManager.sendMessage(player, "command.reloaded");
    }

    private void handleTransfer(Player player, String[] args) {
        if (!player.hasPermission("advancedcorerealms.admin.transfer")) {
            languageManager.sendMessage(player, "error.no-permission");
            return;
        }

        if (args.length < 3) {
            languageManager.sendMessage(player, "error.usage.transfer");
            return;
        }

        String worldName = args[1];
        Realm realm = plugin.getWorldDataManager().getRealm(worldName);
        if (realm == null) {
            languageManager.sendMessage(player, "error.realm_not_found");
            return;
        }

        Player newOwner = Bukkit.getPlayer(args[2]);
        if (newOwner == null) {
            languageManager.sendMessage(player, "error.player_not_online");
            return;
        }

        realm.setOwner(newOwner.getUniqueId());
        plugin.getWorldDataManager().saveData();
        languageManager.sendMessage(player, "realm.transfer_success");
    }

    private void handleBack(Player player) {
        if (!player.hasPermission("advancedcorerealms.user.back")) {
            languageManager.sendMessage(player, "error.no-permission");
            return;
        }

        org.bukkit.Location previousLocation = plugin.getPlayerDataManager().loadPreviousLocation(player.getUniqueId());
        if (previousLocation == null) {
            languageManager.sendMessage(player, "error.no_previous_location");
            return;
        }

        player.teleport(previousLocation);
        languageManager.sendMessage(player, "realm.teleport_back_success");
    }
    
    private void handleUpgrade(Player player, String[] args) {
        // This command now just opens the menu.
        plugin.getMenuManager().openUpgradeMenu(player);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("help", "create", "delete", "tp", "teleport", "list",
                    "invite", "accept", "deny", "reload", "transfer", "back", "upgrade");
            return filterByPrefix(subcommands, args[0]);
        } else if (args.length == 2) {
            String subcommand = args[0].toLowerCase();
            switch (subcommand) {
                case "delete":
                case "tp":
                case "teleport":
                case "invite":
                case "transfer":
                    List<String> worlds = new ArrayList<>();
                    for (World world : Bukkit.getWorlds()) {
                        worlds.add(world.getName());
                    }
                    for (Realm realm : plugin.getWorldDataManager().getAllRealms()) {
                        if (!worlds.contains(realm.getName())) {
                            worlds.add(realm.getName());
                        }
                    }
                    return filterByPrefix(worlds, args[1]);
                case "create":
                    // No suggestions for realm name, it's a free-form argument
                    return new ArrayList<>();
                default:
                    return new ArrayList<>();
            }
        } else if (args.length == 3) {
            String subcommand = args[0].toLowerCase();
            if (subcommand.equals("create")) {
                // Suggest template types from the templates folder
                java.io.File templatesFolder = new java.io.File(plugin.getDataFolder(), plugin.getRealmConfig().getTemplatesFolder());
                if (templatesFolder.isDirectory()) {
                    String[] templateDirs = templatesFolder.list((current, name) -> new java.io.File(current, name).isDirectory());
                    if (templateDirs != null) {
                        return filterByPrefix(Arrays.asList(templateDirs), args[2]);
                    }
                }
                return new ArrayList<>();
            } else if (subcommand.equals("invite") || subcommand.equals("transfer")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }

    private List<String> filterByPrefix(List<String> options, String prefix) {
        return options.stream()
                .filter(option -> option.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }
}