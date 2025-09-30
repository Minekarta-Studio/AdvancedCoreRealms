package com.minekarta.advancedcorerealms.commands.handlers;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.commands.base.SubCommand;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.manager.LanguageManager;
import com.minekarta.advancedcorerealms.manager.RealmManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class TransferCommand implements SubCommand {

    private final AdvancedCoreRealms plugin;
    private final LanguageManager languageManager;
    private final RealmManager realmManager;

    public TransferCommand(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
        this.realmManager = plugin.getRealmManager();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            languageManager.sendMessage(sender, "error.players_only");
            return;
        }

        if (args.length < 3) {
            languageManager.sendMessage(player, "error.usage.transfer");
            return;
        }

        String realmName = args[1];
        Player newOwner = Bukkit.getPlayer(args[2]);

        if (newOwner == null) {
            languageManager.sendMessage(player, "error.player_not_online");
            return;
        }

        Optional<Realm> optionalRealm = realmManager.getRealmByName(realmName);
        if (optionalRealm.isEmpty()) {
            languageManager.sendMessage(player, "error.realm_not_found");
            return;
        }

        Realm realm = optionalRealm.get();
        if (!realm.getOwner().equals(player.getUniqueId())) {
            languageManager.sendMessage(player, "error.not_owner");
            return;
        }

        realm.setOwner(newOwner.getUniqueId());
        realmManager.updateRealm(realm).thenRun(() -> {
            languageManager.sendMessage(player, "realm.transfer_success", "%player%", newOwner.getName());
            languageManager.sendMessage(newOwner, "realm.transfer_received", "%realm%", realm.getName(), "%player%", player.getName());
        }).exceptionally(ex -> {
            languageManager.sendMessage(player, "error.command_generic");
            plugin.getLogger().log(Level.SEVERE, "Error during realm transfer", ex);
            return null;
        });
    }

    @Override
    public String getName() {
        return "transfer";
    }

    @Override
    public String getPermission() {
        return "advancedcorerealms.user.transfer";
    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        if (args.length == 2) {
            return realmManager.getRealmsByOwner(player.getUniqueId()).stream()
                    .map(Realm::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 3) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}