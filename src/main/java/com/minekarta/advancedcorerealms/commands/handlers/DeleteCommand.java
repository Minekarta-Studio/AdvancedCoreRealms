package com.minekarta.advancedcorerealms.commands.handlers;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.commands.base.SubCommand;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.manager.LanguageManager;
import com.minekarta.advancedcorerealms.manager.RealmManager;
import com.minekarta.advancedcorerealms.manager.world.WorldManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.stream.Collectors;

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
        Optional<Realm> optionalRealm = realmManager.getRealmByName(realmName);

        if (optionalRealm.isEmpty()) {
            languageManager.sendMessage(player, "error.realm_not_found");
            return;
        }

        Realm realm = optionalRealm.get();
        if (!realm.getOwner().equals(player.getUniqueId()) && !player.hasPermission("advancedcorerealms.admin.delete")) {
            languageManager.sendMessage(player, "error.not_owner");
            return;
        }

        languageManager.sendMessage(player, "realm.deletion_started", "%realm%", realmName);

        // First, delete the world files and unload the world.
        CompletableFuture<Boolean> worldDeletionFuture = worldManager.deleteWorld(realm);

        // Then, delete the realm data file.
        CompletableFuture<Void> dataDeletionFuture = realmManager.deleteRealm(realm);

        // Wait for both to complete
        CompletableFuture.allOf(worldDeletionFuture, dataDeletionFuture).thenRun(() -> {
            if (worldDeletionFuture.join()) { // Check if world deletion was successful
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
        if (args.length == 2) {
            // Suggest realms owned by the player
            return realmManager.getRealmsByOwner(player.getUniqueId()).stream()
                    .map(Realm::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}