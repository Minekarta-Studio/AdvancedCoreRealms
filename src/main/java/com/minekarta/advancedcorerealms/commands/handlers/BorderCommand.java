package com.minekarta.advancedcorerealms.commands.handlers;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.commands.base.SubCommand;
import com.minekarta.advancedcorerealms.config.ConfigManager;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.manager.LanguageManager;
import com.minekarta.advancedcorerealms.manager.RealmManager;
import com.minekarta.advancedcorerealms.worldborder.WorldBorderManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BorderCommand implements SubCommand {

    private final LanguageManager lang;
    private final WorldBorderManager worldBorderManager;
    private final RealmManager realmManager;
    private final ConfigManager configManager;

    public BorderCommand(AdvancedCoreRealms plugin) {
        this.lang = plugin.getLanguageManager();
        this.worldBorderManager = plugin.getWorldBorderManager();
        this.realmManager = plugin.getRealmManager();
        this.configManager = plugin.getConfigManager();
    }

    @Override
    public String getName() {
        return "border";
    }

    @Override
    public String getPermission() {
        return "advancedcorerealms.user.border.upgrade";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            lang.sendMessage(sender, "error.players_only");
            return;
        }

        if (args.length < 3) {
            lang.sendMessage(player, "error.usage.border", "%usage%", "/realms border <realm_name> <tier_id>");
            return;
        }

        String realmName = args[1];
        String targetTierId = args[2];

        Optional<Realm> optionalRealm = realmManager.getRealmByName(realmName);

        if (optionalRealm.isEmpty()) {
            lang.sendMessage(player, "error.realm_not_found", "%name%", realmName);
            return;
        }

        Realm realm = optionalRealm.get();
        if (!realm.getOwner().equals(player.getUniqueId())) {
            lang.sendMessage(player, "error.not_owner");
            return;
        }

        // Delegate the entire upgrade logic to the manager
        worldBorderManager.upgradeBorder(player, realm, targetTierId);
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
        if (args.length == 3) {
            // Suggest available tier IDs from the config
            return configManager.getWorldBorderTiers().keySet().stream()
                    .filter(tierId -> !tierId.equalsIgnoreCase("default")) // Don't suggest the default tier
                    .filter(tierId -> tierId.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}