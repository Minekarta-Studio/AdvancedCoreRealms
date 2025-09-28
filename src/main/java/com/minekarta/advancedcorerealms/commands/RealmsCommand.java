package com.minekarta.advancedcorerealms.commands;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.api.AdvancedCorePlayer;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.manager.world.WorldManager;
import com.minekarta.advancedcorerealms.utils.MessageUtils;
import com.minekarta.advancedcorerealms.worldborder.BorderColor;
import com.minekarta.advancedcorerealms.worldborder.PlayerChangeBorderColorEvent;
import com.minekarta.advancedcorerealms.worldborder.PlayerToggleBorderEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
    
    public RealmsCommand(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.worldManager = plugin.getWorldManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            // Open GUI
            if (!player.hasPermission("advancedcorerealms.user.base")) {
                MessageUtils.sendMessage(player, "error.no-permission");
                return true;
            }
            
            plugin.getGuiManager().openMainMenu(player);
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
            case "debug":
            case "test":
                handleDebug(player);
                break;
            case "upgrade":
                handleUpgrade(player, args);
                break;
            case "admin":
                handleAdmin(player, args);
                break;
            default:
                MessageUtils.sendMessage(player, "command.help");
                break;
        }
        
        return true;
    }
    
    private void handleAdmin(Player player, String[] args) {
        if (!player.hasPermission("advancedcorerealms.admin.*")) {
            MessageUtils.sendMessage(player, "error.no-permission");
            return;
        }
        
        if (args.length < 3) {
            player.sendMessage(org.bukkit.ChatColor.RED + "Usage: /realms admin <upgrade/setupgrade> <player> <upgradeId> [level]");
            return;
        }
        
        String subCommand = args[1].toLowerCase();
        String targetPlayerName = args[2];
        
        org.bukkit.entity.Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer == null) {
            player.sendMessage(org.bukkit.ChatColor.RED + "Player is not online!");
            return;
        }
        
        com.minekarta.advancedcorerealms.data.object.Realm targetRealm = 
            plugin.getWorldDataManager().getPlayerRealms(targetPlayer.getUniqueId()).stream()
                .filter(r -> r.getOwner().equals(targetPlayer.getUniqueId()))
                .findFirst()
                .orElse(null);
                
        if (targetRealm == null) {
            player.sendMessage(org.bukkit.ChatColor.RED + "Target player doesn't own any realm!");
            return;
        }
        
        switch (subCommand) {
            case "upgrade":
                if (args.length < 4) {
                    player.sendMessage(org.bukkit.ChatColor.RED + "Usage: /realms admin upgrade <player> <upgradeId>");
                    return;
                }
                
                String upgradeId = args[3];
                boolean success = plugin.getUpgradeManager().upgradeRealm(targetRealm, upgradeId, targetPlayer);
                if (success) {
                    player.sendMessage(org.bukkit.ChatColor.GREEN + "Successfully upgraded " + upgradeId + " for " + targetPlayerName + "!");
                    targetPlayer.sendMessage(org.bukkit.ChatColor.GREEN + "An admin has upgraded " + upgradeId + " for your realm!");
                } else {
                    player.sendMessage(org.bukkit.ChatColor.RED + "Failed to upgrade " + upgradeId + ".");
                }
                break;
                
            case "setupgrade":
                if (args.length < 5) {
                    player.sendMessage(org.bukkit.ChatColor.RED + "Usage: /realms admin setupgrade <player> <upgradeId> <level>");
                    return;
                }
                
                try {
                    String upgradeId2 = args[3];
                    int level = Integer.parseInt(args[4]);
                    
                    com.minekarta.advancedcorerealms.upgrades.RealmUpgrade upgrade = 
                        plugin.getUpgradeManager().getUpgrade(upgradeId2);
                    if (upgrade == null) {
                        player.sendMessage(org.bukkit.ChatColor.RED + "Upgrade not found: " + upgradeId2);
                        return;
                    }
                    
                    if (level < 0 || level > upgrade.getMaxLevel()) {
                        player.sendMessage(org.bukkit.ChatColor.RED + "Level must be between 0 and " + upgrade.getMaxLevel());
                        return;
                    }
                    
                    upgrade.setLevel(targetRealm, level);
                    // Apply the upgrade effect
                    upgrade.applyUpgrade(targetRealm, level);
                    
                    player.sendMessage(org.bukkit.ChatColor.GREEN + "Set upgrade " + upgradeId2 + " to level " + level + " for " + targetPlayerName);
                    targetPlayer.sendMessage(org.bukkit.ChatColor.GREEN + "An admin has set " + upgradeId2 + " to level " + level + " for your realm!");
                } catch (NumberFormatException e) {
                    player.sendMessage(org.bukkit.ChatColor.RED + "Invalid level number!");
                }
                break;
                
            default:
                player.sendMessage(org.bukkit.ChatColor.RED + "Usage: /realms admin <upgrade/setupgrade> <player> <upgradeId> [level]");
                break;
        }
    }
    
    private void handleUpgrade(Player player, String[] args) {
        if (args.length == 1) {
            // Open upgrade menu
            plugin.getGuiManager().openUpgradeMenu(player);
            return;
        }
        
        if (args.length == 2) {
            String upgradeId = args[1].toLowerCase();
            
            // Try to upgrade the specific upgrade
            com.minekarta.advancedcorerealms.data.object.Realm currentRealm = 
                plugin.getWorldDataManager().getPlayerRealms(player.getUniqueId()).stream()
                    .filter(r -> r.getOwner().equals(player.getUniqueId()))
                    .findFirst()
                    .orElse(null);
                    
            if (currentRealm == null) {
                player.sendMessage(org.bukkit.ChatColor.RED + "You don't own any realm to upgrade!");
                return;
            }
            
            boolean success = plugin.getUpgradeManager().upgradeRealm(currentRealm, upgradeId, player);
            if (success) {
                player.sendMessage(org.bukkit.ChatColor.GREEN + "Successfully upgraded " + upgradeId + "!");
            } else {
                player.sendMessage(org.bukkit.ChatColor.RED + "Failed to upgrade " + upgradeId + ". Check your balance or level limits.");
            }
        } else {
            player.sendMessage(org.bukkit.ChatColor.RED + "Usage: /realms upgrade [type] or /realms upgrade to open menu");
        }
    }
    
    private void handleToggle(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /realms toggle <border>");
            return;
        }
        
        String toggleOption = args[1].toLowerCase();
        
        if ("border".equals(toggleOption)) {
            AdvancedCorePlayer advancedCorePlayer = plugin.getAdvancedCorePlayer(player);
            
            // Call the event
            PlayerToggleBorderEvent event = new PlayerToggleBorderEvent(player, !advancedCorePlayer.hasWorldBorderEnabled());
            Bukkit.getPluginManager().callEvent(event);
            
            if (event.isCancelled()) {
                return;
            }
            
            advancedCorePlayer.toggleWorldBorder();
            
            boolean isEnabled = advancedCorePlayer.hasWorldBorderEnabled();
            String status = isEnabled ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled";
            player.sendMessage(ChatColor.YELLOW + "World border has been " + status + ChatColor.YELLOW + ".");
        } else {
            player.sendMessage(ChatColor.RED + "Unknown toggle option. Available: border");
        }
    }
    
    private void handleBorder(Player player, String[] args) {
        AdvancedCorePlayer advancedCorePlayer = plugin.getAdvancedCorePlayer(player);
        
        if (args.length == 1) {
            // Open GUI for color selection
            plugin.getGuiManager().openBorderColorMenu(player);
            return;
        }
        
        if (args.length == 2) {
            String colorArg = args[1].toUpperCase();
            
            try {
                BorderColor newColor = BorderColor.valueOf(colorArg);
                
                // Call the event
                PlayerChangeBorderColorEvent event = new PlayerChangeBorderColorEvent(player, newColor);
                Bukkit.getPluginManager().callEvent(event);
                
                if (event.isCancelled()) {
                    return;
                }
                
                advancedCorePlayer.setBorderColor(newColor);
                player.sendMessage(ChatColor.YELLOW + "Border color set to " + ChatColor.RESET + newColor.name().toLowerCase() + ChatColor.YELLOW + ".");
            } catch (IllegalArgumentException e) {
                player.sendMessage(ChatColor.RED + "Invalid color. Available colors: BLUE, GREEN, RED");
            }
        } else {
            player.sendMessage(ChatColor.RED + "Usage: /realms border [color] or /realms border to open menu");
        }
    }
    
    private void handleHelp(Player player) {
        if (!player.hasPermission("advancedcorerealms.user.help")) {
            MessageUtils.sendMessage(player, "error.no-permission");
            return;
        }
        
        MessageUtils.sendMessage(player, "command.help");
    }
    
    private void handleCreate(Player player, String[] args) {
        // Check permissions and if player is running the command
        if (!player.hasPermission("advancedcorerealms.user.create")) {
            MessageUtils.sendMessage(player, "error.no-permission");
            return;
        }
        
        // Validate arguments - check if world name is provided
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /realms create <name> [type]");
            return;
        }
        
        String worldName = args[1];
        
        // Check if world already exists
        if (Bukkit.getWorld(worldName) != null || plugin.getWorldDataManager().getRealm(worldName) != null) {
            player.sendMessage(ChatColor.RED + "Realms with this name already exists. Please use another name!");
            return;
        }
        
        // Determine maximum number of realms based on permission
        int maxRealms = 1; // Default
        if (player.hasPermission("advancedcorerealms.limit.realms.3")) maxRealms = 3;
        if (player.hasPermission("advancedcorerealms.limit.realms.5")) maxRealms = 5;
        
        // Calculate current number of realms owned by player
        int playerRealms = plugin.getWorldDataManager().getPlayerRealms(player.getUniqueId()).size();
        
        // Check if player has reached maximum number of realms
        if (playerRealms >= maxRealms) {
            player.sendMessage(ChatColor.RED + "You have reached the maximum number of Realms");
            return;
        }
        
        // Process world type
        String worldType = plugin.getConfig().getString("default-world-type", "FLAT").toUpperCase();
        
        if (args.length >= 3) {
            String type = args[2].toUpperCase();
            if (type.equals("FLAT") || type.equals("NORMAL") || type.equals("AMPLIFIED")) {
                worldType = type;
            } else {
                player.sendMessage(ChatColor.RED + "Invalid world type! Valid types: FLAT, NORMAL, AMPLIFIED");
                return;
            }
        }
        
        // Save player's current location before creating realm
        plugin.getPlayerDataManager().savePreviousLocation(player.getUniqueId(), player.getLocation());
        
        // Send message to player while world is being created
        player.sendMessage(ChatColor.YELLOW + "Creating Realms, Please wait");
        
        // Create the world
        worldManager.createWorldAsync(player, worldName, worldType);
    }
    
    private void handleDelete(Player player, String[] args) {
        if (!player.hasPermission("advancedcorerealms.user.delete")) {
            MessageUtils.sendMessage(player, "error.no-permission");
            return;
        }
        
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /realms delete <world>");
            return;
        }
        
        String worldName = args[1];
        Realm realm = plugin.getWorldDataManager().getRealm(worldName);
        
        if (realm == null) {
            player.sendMessage(ChatColor.RED + "Realm does not exist!");
            return;
        }
        
        if (!realm.getOwner().equals(player.getUniqueId()) && !player.hasPermission("advancedcorerealms.admin.*")) {
            MessageUtils.sendMessage(player, "error.not-owner");
            return;
        }
        
        worldManager.deleteWorld(player, worldName);
    }
    
    private void handleTeleport(Player player, String[] args) {
        if (!player.hasPermission("advancedcorerealms.user.teleport")) {
            MessageUtils.sendMessage(player, "error.no-permission");
            return;
        }
        
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /realms tp <world>");
            return;
        }
        
        String worldName = args[1];
        worldManager.teleportToRealm(player, worldName);
    }
    
    private void handleList(Player player) {
        if (!player.hasPermission("advancedcorerealms.user.list")) {
            MessageUtils.sendMessage(player, "error.no-permission");
            return;
        }
        
        List<Realm> playerRealms = plugin.getWorldDataManager().getPlayerRealms(player.getUniqueId());
        List<Realm> invitedRealms = plugin.getWorldDataManager().getPlayerInvitedRealms(player.getUniqueId());
        
        player.sendMessage(ChatColor.GOLD + "=== Your Realms ===");
        for (Realm realm : playerRealms) {
            World world = Bukkit.getWorld(realm.getName());
            String status = (world != null) ? ChatColor.GREEN + "Loaded" : ChatColor.RED + "Unloaded";
            player.sendMessage(ChatColor.AQUA + "- " + realm.getName() + " (" + status + ")");
        }
        
        if (!invitedRealms.isEmpty()) {
            player.sendMessage(ChatColor.GOLD + "=== Invited Realms ===");
            for (Realm realm : invitedRealms) {
                World world = Bukkit.getWorld(realm.getName());
                String status = (world != null) ? ChatColor.GREEN + "Loaded" : ChatColor.RED + "Unloaded";
                player.sendMessage(ChatColor.AQUA + "- " + realm.getName() + " (Invited - " + status + ")");
            }
        }
    }
    
    private void handleInvite(Player player, String[] args) {
        if (!player.hasPermission("advancedcorerealms.user.invite")) {
            MessageUtils.sendMessage(player, "error.no-permission");
            return;
        }
        
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /realms invite <world> <player>");
            return;
        }
        
        String worldName = args[1];
        String targetPlayerName = args[2];
        
        Realm realm = plugin.getWorldDataManager().getRealm(worldName);
        if (realm == null) {
            player.sendMessage(ChatColor.RED + "Realm does not exist!");
            return;
        }
        
        if (!realm.getOwner().equals(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "error.not-owner");
            return;
        }
        
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer == null) {
            player.sendMessage(ChatColor.RED + "Player is not online!");
            return;
        }
        
        plugin.getInviteManager().sendInvite(player, targetPlayer, worldName);
    }
    
    private void handleAccept(Player player) {
        if (!player.hasPermission("advancedcorerealms.user.accept")) {
            MessageUtils.sendMessage(player, "error.no-permission");
            return;
        }
        
        plugin.getInviteManager().acceptInvite(player);
    }
    
    private void handleDeny(Player player) {
        if (!player.hasPermission("advancedcorerealms.user.deny")) {
            MessageUtils.sendMessage(player, "error.no-permission");
            return;
        }
        
        plugin.getInviteManager().denyInvite(player);
    }
    
    private void handleReload(Player player) {
        if (!player.hasPermission("advancedcorerealms.admin.reload")) {
            MessageUtils.sendMessage(player, "error.no-permission");
            return;
        }
        
        plugin.reloadConfig();
        plugin.getLanguageManager().loadLanguage();
        MessageUtils.sendMessage(player, "command.reloaded");
    }
    
    private void handleTransfer(Player player, String[] args) {
        if (!player.hasPermission("advancedcorerealms.admin.transfer")) {
            MessageUtils.sendMessage(player, "error.no-permission");
            return;
        }
        
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /realms transfer <world> <player>");
            return;
        }
        
        String worldName = args[1];
        String newOwnerName = args[2];
        
        Realm realm = plugin.getWorldDataManager().getRealm(worldName);
        if (realm == null) {
            player.sendMessage(ChatColor.RED + "Realm does not exist!");
            return;
        }
        
        Player newOwner = Bukkit.getPlayer(newOwnerName);
        if (newOwner == null) {
            player.sendMessage(ChatColor.RED + "Player is not online!");
            return;
        }
        
        realm.setOwner(newOwner.getUniqueId());
        plugin.getWorldDataManager().saveData();
        player.sendMessage(ChatColor.GREEN + "Realm ownership transferred successfully!");
    }
    
    private void handleBack(Player player) {
        if (!player.hasPermission("advancedcorerealms.user.back")) {
            MessageUtils.sendMessage(player, "error.no-permission");
            return;
        }
        
        org.bukkit.Location previousLocation = plugin.getPlayerDataManager().loadPreviousLocation(player.getUniqueId());
        if (previousLocation == null) {
            player.sendMessage(ChatColor.RED + "No previous location found!");
            return;
        }
        
        player.teleport(previousLocation);
        player.sendMessage(ChatColor.GREEN + "Teleported to your previous location.");
    }
    
    private void handleDebug(Player player) {
        if (!player.hasPermission("advancedcorerealms.admin.debug")) {
            MessageUtils.sendMessage(player, "error.no-permission");
            return;
        }
        
        // Set border color to blue
                com.minekarta.advancedcorerealms.api.AdvancedCorePlayer advancedCorePlayer = 
                    plugin.getAdvancedCorePlayer(player);
        
        player.sendMessage(ChatColor.GOLD + "Fallback system test initiated. Check console for results.");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("help", "create", "delete", "tp", "teleport", "list", 
                    "invite", "accept", "deny", "reload", "transfer", "back", "debug", "test", "toggle", "border", "upgrade", "admin");
            return filterByPrefix(subcommands, args[0]);
        } else if (args.length == 2) {
            String subcommand = args[0].toLowerCase();
            switch (subcommand) {
                case "delete":
                case "tp":
                case "teleport":
                case "invite":
                case "transfer":
                    // Return list of available worlds
                    List<String> worlds = new ArrayList<>();
                    for (World world : Bukkit.getWorlds()) {
                        worlds.add(world.getName());
                    }
                    // Also add realms from data
                    for (Realm realm : plugin.getWorldDataManager().getAllRealms()) {
                        if (!worlds.contains(realm.getName())) {
                            worlds.add(realm.getName());
                        }
                    }
                    return filterByPrefix(worlds, args[1]);
                case "create":
                    return Arrays.asList("FLAT", "NORMAL", "AMPLIFIED"); // Updated to include more world types
                case "toggle":
                    return Arrays.asList("border");
                case "border":
                    return Arrays.asList("BLUE", "GREEN", "RED");
                case "admin":
                    return Arrays.asList("upgrade", "setupgrade");
                default:
                    return new ArrayList<>();
            }
        } else if (args.length == 3) {
            String subcommand = args[0].toLowerCase();
            if (subcommand.equals("create")) {
                return Arrays.asList("FLAT", "NORMAL", "AMPLIFIED");
            } else if (subcommand.equals("invite") || subcommand.equals("transfer")) {
                // Return online player names
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (subcommand.equals("admin")) {
                // Return online player names
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
        } else if (args.length == 4) {
            String subcommand = args[0].toLowerCase();
            if (subcommand.equals("admin") && (args[1].equals("upgrade") || args[1].equals("setupgrade"))) {
                // Return list of available upgrades
                List<String> upgrades = new ArrayList<>();
                for (com.minekarta.advancedcorerealms.upgrades.RealmUpgrade upgrade : plugin.getUpgradeManager().getUpgrades()) {
                    upgrades.add(upgrade.getId());
                }
                return filterByPrefix(upgrades, args[3]);
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