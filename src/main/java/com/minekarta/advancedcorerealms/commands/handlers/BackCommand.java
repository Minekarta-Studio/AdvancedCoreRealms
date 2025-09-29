package com.minekarta.advancedcorerealms.commands.handlers;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.commands.base.SubCommand;
import com.minekarta.advancedcorerealms.data.object.PlayerData;
import com.minekarta.advancedcorerealms.manager.LanguageManager;
import com.minekarta.advancedcorerealms.manager.RealmManager;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class BackCommand implements SubCommand {

    private final LanguageManager languageManager;
    private final RealmManager realmManager;

    public BackCommand(AdvancedCoreRealms plugin) {
        this.languageManager = plugin.getLanguageManager();
        this.realmManager = plugin.getRealmManager();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            languageManager.sendMessage(sender, "error.players_only");
            return;
        }

        PlayerData playerData = realmManager.getPlayerData(player.getUniqueId());
        Location previousLocation = playerData.getPreviousLocation();

        if (previousLocation == null) {
            languageManager.sendMessage(player, "error.no_previous_location");
            return;
        }

        player.teleport(previousLocation);
        languageManager.sendMessage(player, "realm.teleport_back_success");
    }

    @Override
    public String getName() {
        return "back";
    }

    @Override
    public String getPermission() {
        return "advancedcorerealms.user.back";
    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        return Collections.emptyList();
    }
}