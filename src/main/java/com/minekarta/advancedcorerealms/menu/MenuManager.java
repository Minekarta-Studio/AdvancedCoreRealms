package com.minekarta.advancedcorerealms.menu;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.menu.menus.BorderColorMenu;
import com.minekarta.advancedcorerealms.menu.menus.MainMenu;
import com.minekarta.advancedcorerealms.menu.menus.RealmCreationMenu;
import com.minekarta.advancedcorerealms.menu.menus.RealmManagementMenu;
import com.minekarta.advancedcorerealms.menu.menus.RealmPlayersMenu;
import com.minekarta.advancedcorerealms.menu.menus.RealmSettingsMenu;
import com.minekarta.advancedcorerealms.menu.menus.RealmsListMenu;
import com.minekarta.advancedcorerealms.menu.menus.UpgradeMenu;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

public class MenuManager {

    private final AdvancedCoreRealms plugin;
    private FileConfiguration mainMenuConfig;
    private FileConfiguration realmsListConfig;
    private FileConfiguration realmManagementConfig;
    private FileConfiguration realmSettingsConfig;
    private FileConfiguration realmPlayersConfig;
    private FileConfiguration realmCreationConfig;
    private FileConfiguration borderColorConfig;
    private FileConfiguration upgradeConfig;


    public MenuManager(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        loadMenuConfigs();
    }

    private void loadMenuConfigs() {
        mainMenuConfig = loadConfig("menu/main_menu.yml");
        realmsListConfig = loadConfig("menu/realms_list.yml");
        realmManagementConfig = loadConfig("menu/realm_management.yml");
        realmSettingsConfig = loadConfig("menu/realm_settings.yml");
        realmPlayersConfig = loadConfig("menu/realm_players.yml");
        realmCreationConfig = loadConfig("menu/realm_creation.yml");
        borderColorConfig = loadConfig("menu/border_color.yml");
        upgradeConfig = loadConfig("menu/upgrade_menu.yml");
    }

    private FileConfiguration loadConfig(String resourcePath) {
        File file = new File(plugin.getDataFolder(), resourcePath);
        if (!file.exists()) {
            plugin.saveResource(resourcePath, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public void openMainMenu(Player player) {
        new MainMenu(plugin, player, mainMenuConfig, this).open();
    }

    public void openRealmsListMenu(Player player, boolean ownRealms) {
        new RealmsListMenu(plugin, player, realmsListConfig, this, ownRealms, 1).open();
    }

    public void openRealmManagementMenu(Player player, String realmName) {
        new RealmManagementMenu(plugin, player, realmManagementConfig, this, realmName).open();
    }

    public void openRealmSettingsMenu(Player player, String realmName) {
        new RealmSettingsMenu(plugin, player, realmSettingsConfig, this, realmName).open();
    }

    public void openRealmPlayersMenu(Player player, String realmName, int page) {
        new RealmPlayersMenu(plugin, player, realmPlayersConfig, this, realmName, page).open();
    }

    public void openRealmCreationMenu(Player player) {
        new RealmCreationMenu(plugin, player, realmCreationConfig).open();
    }

    public void openBorderColorMenu(Player player) {
        new BorderColorMenu(plugin, player, borderColorConfig, this).open();
    }

    public void openUpgradeMenu(Player player) {
        new UpgradeMenu(plugin, player, upgradeConfig, this).open();
    }
}