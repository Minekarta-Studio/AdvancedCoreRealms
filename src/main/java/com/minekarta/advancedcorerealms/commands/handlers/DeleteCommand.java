package com.minekarta.advancedcorerealms.commands.handlers;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.commands.base.SubCommand;
import com.minekarta.advancedcorerealms.manager.LanguageManager;
import com.minekarta.advancedcorerealms.manager.RealmManager;
import com.minekarta.advancedcorerealms.manager.world.WorldManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class DeleteCommand implements SubCommand {

    private final AdvancedCoreRealms plugin;
    private final LanguageManager languageManager;
    private final RealmManager realmManager;
    private final WorldManager worldManager;

    public DeleteCommand(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
        this.realmManager = plugin.getRealmManager();
        this.worldManager = plugin.getWorldManager();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            languageManager.sendMessage(sender, "error.players_only");
            return;
        }

        if (args.length < 2) {
            languageManager.sendMessage(player, "error.usage.delete");
            return;
        }

        String realmName = args[1];

        realmManager.getRealmByName(realmName).thenCompose(realm -> {
            if (realm == null) {
                languageManager.sendMessage(player, "error.realm_not_found");
                return CompletableFuture.completedFuture(null); // Stop the chain
            }

            if (!realm.getOwner().equals(player.getUniqueId()) && !player.hasPermission("advancedcorerealms.admin.delete")) {
                languageManager.sendMessage(player, "error.not-owner");
                return CompletableFuture.completedFuture(null); // Stop the chain
            }

            languageManager.sendMessage(player, "realm.deletion_started", "%realm%", realmName);
            return worldManager.deleteWorld(realm);

        }).thenAccept(success -> {
            if (success == null) {
                // A prerequisite failed, message already sent.
                return;
            }
            if (success) {
                languageManager.sendMessage(player, "world.deleted", "%world%", realmName);
            } else {
                languageManager.sendMessage(player, "error.world_delete_failed", "%world%", realmName);
            }
        }).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, "An error occurred during realm deletion for command: " + realmName, ex);
            languageManager.sendMessage(player, "error.command_generic");
            return null;
        });
    }

    @Override
    public String getName() {
        return "delete";
    }

    @Override
    public String getPermission() {
        return "advancedcorerealms.user.delete";
    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        // Tab completion for realm names is complex with an async backend.
        // For now, we return an empty list to avoid blocking the server.
        return Collections.emptyList();
    }
}