package com.minekarta.advancedcorerealms.commands.handlers;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.commands.base.SubCommand;
import com.minekarta.advancedcorerealms.manager.LanguageManager;
import com.minekarta.advancedcorerealms.manager.RealmManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class CreateCommand implements SubCommand {

    private final AdvancedCoreRealms plugin;
    private final LanguageManager languageManager;
    private final RealmManager realmManager;

    public CreateCommand(AdvancedCoreRealms plugin) {
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

        if (args.length < 2) {
            languageManager.sendMessage(player, "error.usage.create");
            return;
        }

        String realmName = args[1];
        String templateType = (args.length >= 3) ? args[2] : "default";

        // Perform checks synchronously
        if (realmManager.doesRealmExist(realmName)) {
            languageManager.sendMessage(player, "error.realm_name_taken");
            return;
        }

        int maxRealms = 1; // Default limit
        if (player.hasPermission("advancedcorerealms.limit.realms.5")) {
            maxRealms = 5;
        } else if (player.hasPermission("advancedcorerealms.limit.realms.3")) {
            maxRealms = 3;
        }

        if (realmManager.getRealmsByOwner(player.getUniqueId()).size() >= maxRealms) {
            languageManager.sendMessage(player, "error.max_realms_reached");
            return;
        }

        // All checks passed, proceed with creation
        realmManager.savePreviousLocation(player.getUniqueId(), player.getLocation());
        plugin.getRealmCreator().createRealmAsync(player, realmName, templateType);
    }

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getPermission() {
        return "advancedcorerealms.user.create";
    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        if (args.length == 3) {
            // Suggest template types from the templates folder
            File templatesFolder = new File(plugin.getDataFolder(), "templates");
            if (templatesFolder.isDirectory()) {
                String[] templateDirs = templatesFolder.list((current, name) -> new File(current, name).isDirectory());
                if (templateDirs != null) {
                    return List.of(templateDirs);
                }
            }
        }
        return Collections.emptyList();
    }
}