package com.minekarta.advancedcorerealms.manager;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.utils.ColorUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    
    private final MiniMessage miniMessage;
    
    public MenuManager(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        loadMenuConfigurations();
    }
    
    public void loadMenuConfigurations() {
        File menuDir = new File(plugin.getDataFolder(), "menu");
        if (!menuDir.exists()) {
            menuDir.mkdirs();
        }
        
        // Copy default configurations from resources if they don't exist
        copyDefaultConfigIfNotExists("main_menu.yml");
        copyDefaultConfigIfNotExists("realms_list.yml");
        copyDefaultConfigIfNotExists("realm_management.yml");
        copyDefaultConfigIfNotExists("realm_settings.yml");
        copyDefaultConfigIfNotExists("realm_players.yml");
        copyDefaultConfigIfNotExists("realm_creation.yml");
        copyDefaultConfigIfNotExists("border_color.yml");
        copyDefaultConfigIfNotExists("upgrade_menu.yml");
        
        // Load configurations
        loadConfig("main_menu.yml", "main_menu");
        loadConfig("realms_list.yml", "realms_list");
        loadConfig("realm_management.yml", "realm_management");
        loadConfig("realm_settings.yml", "realm_settings");
        loadConfig("realm_players.yml", "realm_players");
        loadConfig("realm_creation.yml", "realm_creation");
        loadConfig("border_color.yml", "border_color");
        loadConfig("upgrade_menu.yml", "upgrade_menu");
    }
    
    private void copyDefaultConfigIfNotExists(String fileName) {
        File configFile = new File(plugin.getDataFolder(), "menu/" + fileName);
        if (!configFile.exists()) {
            try {
                // Try to copy from resources first
                plugin.saveResource("menu/" + fileName, false);
                
                // If the resource wasn't found or file is empty, create default content
                if (!configFile.exists() || configFile.length() == 0) {
                    configFile.getParentFile().mkdirs(); // Ensure directory exists
                    createDefaultMenuFile(configFile, fileName);
                }
            } catch (Exception e) {
                // If saveResource fails, create default content
                try {
                    configFile.getParentFile().mkdirs(); // Ensure directory exists
                    createDefaultMenuFile(configFile, fileName);
                } catch (Exception ex) {
                    plugin.getLogger().severe("Could not create default menu config file: " + ex.getMessage());
                }
            }
        }
        // If file already exists, silently continue (no error)
    }
    
    private void createDefaultMenuFile(File file, String fileName) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (fileName.equals("main_menu.yml")) {
            config.set("main_menu.title", "<gradient:#6677EE:#99FFCC>AdvancedCoreRealms</gradient>");
            config.set("main_menu.size", 27);
            config.set("main_menu.elements.info_item.slot", 4);
            config.set("main_menu.elements.info_item.material", "WRITTEN_BOOK");
            config.set("main_menu.elements.info_item.name", "<gradient:#6677EE:#99FFCC>AdvancedCoreRealms</gradient>");
            config.set("main_menu.elements.info_item.lore", List.of(
                "<gradient:#6677EE:#99FFCC>Manage your personal realms</gradient>",
                "<gradient:#6677EE:#99FFCC>with advanced features!</gradient>"
            ));
            config.set("main_menu.elements.my_realms.slot", 11);
            config.set("main_menu.elements.my_realms.material", "GRASS_BLOCK");
            config.set("main_menu.elements.my_realms.name", "<gradient:#6677EE:#99FFCC>My Realms</gradient>");
            config.set("main_menu.elements.my_realms.lore", List.of(
                "<gradient:#6677EE:#99FFCC>View and manage</gradient>",
                "<gradient:#6677EE:#99FFCC>your owned realms</gradient>"
            ));
            config.set("main_menu.elements.accessible_realms.slot", 13);
            config.set("main_menu.elements.accessible_realms.material", "OAK_LOG");
            config.set("main_menu.elements.accessible_realms.name", "<gradient:#6677EE:#99FFCC>Accessible Realms</gradient>");
            config.set("main_menu.elements.accessible_realms.lore", List.of(
                "<gradient:#6677EE:#99FFCC>View realms you have access to</gradient>",
                "<gradient:#6677EE:#99FFCC>that you don't own</gradient>"
            ));
            config.set("main_menu.elements.create_realm.slot", 15);
            config.set("main_menu.elements.create_realm.material", "CRAFTING_TABLE");
            config.set("main_menu.elements.create_realm.name", "<gradient:#6677EE:#99FFCC>Create Realm</gradient>");
            config.set("main_menu.elements.create_realm.lore", List.of(
                "<gradient:#6677EE:#99FFCC>Create a new personal realm!</gradient>",
                "<gradient:#6677EE:#99FFCC>Permissions required:</gradient>",
                "<gradient:#6677EE:#99FFCC>advancedcorerealms.create</gradient>"
            ));
            config.set("main_menu.elements.create_realm.permission_required", "advancedcorerealms.donor.create,advancedcorerealms.admin.create,advancedcorerealms.unlimited.create");
            config.set("main_menu.elements.create_realm.no_permission_material", "BARRIER");
            config.set("main_menu.elements.create_realm.no_permission_name", "<red>Locked: Create Realm</red>");
            config.set("main_menu.elements.create_realm.no_permission_lore", List.of(
                "<red>You don't have permission</red>",
                "<red>to create a realm!</red>",
                "<red>Required: donor or admin</red>"
            ));
            config.set("main_menu.elements.border_color.slot", 20);
            config.set("main_menu.elements.border_color.material", "BLUE_DYE");
            config.set("main_menu.elements.border_color.name", "<gradient:#6677EE:#99FFCC>Border Color</gradient>");
            config.set("main_menu.elements.border_color.lore", List.of(
                "<gradient:#6677EE:#99FFCC>Set your personal</gradient>",
                "<gradient:#6677EE:#99FFCC>world border color</gradient>"
            ));
            config.set("main_menu.elements.upgrades.slot", 22);
            config.set("main_menu.elements.upgrades.material", "EMERALD");
            config.set("main_menu.elements.upgrades.name", "<gradient:#6677EE:#99FFCC>Upgrades</gradient>");
            config.set("main_menu.elements.upgrades.lore", List.of(
                "<gradient:#6677EE:#99FFCC>View and purchase</gradient>",
                "<gradient:#6677EE:#99FFCC>realm upgrades</gradient>"
            ));
            config.set("main_menu.elements.glass_panes.material", "BLACK_STAINED_GLASS_PANE");
            config.set("main_menu.elements.glass_panes.name", " ");
            config.set("main_menu.elements.glass_panes.fill_remaining", true);
        } else if (fileName.equals("realms_list.yml")) {
            config.set("realms_list.title", "<gradient:#6677EE:#99FFCC>My Realms</gradient>");
            config.set("realms_list.size", 54);
            config.set("realms_list.my_realms_title", "<gradient:#6677EE:#99FFCC>My Realms</gradient>");
            config.set("realms_list.accessible_realms_title", "<gradient:#6677EE:#99FFCC>Accessible Realms</gradient>");
            config.set("realms_list.elements.info_item.slot", 4);
            config.set("realms_list.elements.info_item.material", "WRITTEN_BOOK");
            config.set("realms_list.elements.info_item.name", "<gradient:#6677EE:#99FFCC>Realms Info</gradient>");
            config.set("realms_list.elements.info_item.lore", List.of("<gradient:#6677EE:#99FFCC>These are the Realms you own</gradient>"));
            config.set("realms_list.elements.back_button.slot", 49);
            config.set("realms_list.elements.back_button.material", "BARRIER");
            config.set("realms_list.elements.back_button.name", "<red>Back</red>");
            config.set("realms_list.elements.back_button.lore", List.of("<red>Return to main menu</red>"));
            config.set("realms_list.elements.previous_page.slot", 45);
            config.set("realms_list.elements.previous_page.material", "ARROW");
            config.set("realms_list.elements.previous_page.name", "<gradient:#6677EE:#99FFCC>Previous Page</gradient>");
            config.set("realms_list.elements.previous_page.lore", List.of("<gradient:#6677EE:#99FFCC>Go to previous page</gradient>"));
            config.set("realms_list.elements.next_page.slot", 53);
            config.set("realms_list.elements.next_page.material", "ARROW");
            config.set("realms_list.elements.next_page.name", "<gradient:#6677EE:#99FFCC>Next Page</gradient>");
            config.set("realms_list.elements.next_page.lore", List.of("<gradient:#6677EE:#99FFCC>Go to next page</gradient>"));
            config.set("realms_list.elements.glass_panes.material", "BLACK_STAINED_GLASS_PANE");
            config.set("realms_list.elements.glass_panes.name", " ");
            config.set("realms_list.elements.glass_panes.fill_remaining", true);
        } else if (fileName.equals("realm_management.yml")) {
            config.set("realm_management.title", "<gradient:#6677EE:#99FFCC>Realms | Realm: [name]</gradient>");
            config.set("realm_management.size", 36);
            config.set("realm_management.elements.info_item.slot", 4);
            config.set("realm_management.elements.info_item.material", "WRITTEN_BOOK");
            config.set("realm_management.elements.info_item.name", "<gradient:#6677EE:#99FFCC>Realm Info</gradient>");
            config.set("realm_management.elements.teleport.slot", 10);
            config.set("realm_management.elements.teleport.material", "ENDER_PEARL");
            config.set("realm_management.elements.teleport.name", "<gradient:#6677EE:#99FFCC>Teleport</gradient>");
            config.set("realm_management.elements.teleport.lore", List.of("<gradient:#6677EE:#99FFCC>Teleport to this Realm</gradient>"));
            config.set("realm_management.elements.manage_players.slot", 12);
            config.set("realm_management.elements.manage_players.material", "PLAYER_HEAD");
            config.set("realm_management.elements.manage_players.name", "<gradient:#6677EE:#99FFCC>Manage Players</gradient>");
            config.set("realm_management.elements.manage_players.lore", List.of("<gradient:#6677EE:#99FFCC>Manage access for this Realm</gradient>"));
            config.set("realm_management.elements.manage_players.permission_required", "realm.owner");
            config.set("realm_management.elements.realm_settings.slot", 14);
            config.set("realm_management.elements.realm_settings.material", "COMPARATOR");
            config.set("realm_management.elements.realm_settings.name", "<gradient:#6677EE:#99FFCC>Realm Settings</gradient>");
            config.set("realm_management.elements.realm_settings.lore", List.of("<gradient:#6677EE:#99FFCC>Change settings for this Realm</gradient>"));
            config.set("realm_management.elements.realm_settings.permission_required", "realm.owner");
            config.set("realm_management.elements.delete_realm.slot", 16);
            config.set("realm_management.elements.delete_realm.material", "TNT");
            config.set("realm_management.elements.delete_realm.name", "<red>Delete Realm</red>");
            config.set("realm_management.elements.delete_realm.lore", List.of(
                "<red>Delete this Realm</red>",
                "<red>(Cannot be undone!)</red>"
            ));
            config.set("realm_management.elements.delete_realm.permission_required", "realm.owner");
            config.set("realm_management.elements.back_button.slot", 31);
            config.set("realm_management.elements.back_button.material", "BARRIER");
            config.set("realm_management.elements.back_button.name", "<red>Back</red>");
            config.set("realm_management.elements.back_button.lore", List.of("<red>Return to Realms list</red>"));
            config.set("realm_management.elements.glass_panes.material", "BLACK_STAINED_GLASS_PANE");
            config.set("realm_management.elements.glass_panes.name", " ");
            config.set("realm_management.elements.glass_panes.fill_remaining", true);
        } else if (fileName.equals("realm_settings.yml")) {
            config.set("realm_settings.title", "<gradient:#6677EE:#99FFCC>Realms | Settings: [name]</gradient>");
            config.set("realm_settings.size", 36);
            config.set("realm_settings.elements.info_item.slot", 4);
            config.set("realm_settings.elements.info_item.material", "WRITTEN_BOOK");
            config.set("realm_settings.elements.info_item.name", "<gradient:#6677EE:#99FFCC>Settings Info</gradient>");
            config.set("realm_settings.elements.player_limit.slot", 11);
            config.set("realm_settings.elements.player_limit.material", "CLOCK");
            config.set("realm_settings.elements.player_limit.name", "<gradient:#6677EE:#99FFCC>Player Limit</gradient>");
            config.set("realm_settings.elements.player_limit.lore", List.of(
                "<gradient:#6677EE:#99FFCC>Current: [limit] players</gradient>",
                "<gradient:#6677EE:#99FFCC>Left-click: Increase by 1</gradient>",
                "<gradient:#6677EE:#99FFCC>Right-click: Decrease by 1</gradient>",
                "<gradient:#6677EE:#99FFCC>Shift+Click: +/-5</gradient>"
            ));
            config.set("realm_settings.elements.set_spawn.slot", 15);
            config.set("realm_settings.elements.set_spawn.material", "COMPASS");
            config.set("realm_settings.elements.set_spawn.name", "<gradient:#6677EE:#99FFCC>Set Spawn Point</gradient>");
            config.set("realm_settings.elements.set_spawn.lore", List.of("<gradient:#6677EE:#99FFCC>Set the spawn point for this Realm</gradient>"));
            config.set("realm_settings.elements.back_button.slot", 31);
            config.set("realm_settings.elements.back_button.material", "BARRIER");
            config.set("realm_settings.elements.back_button.name", "<red>Back</red>");
            config.set("realm_settings.elements.back_button.lore", List.of("<red>Return to Realm Management</red>"));
            config.set("realm_settings.elements.glass_panes.material", "BLACK_STAINED_GLASS_PANE");
            config.set("realm_settings.elements.glass_panes.name", " ");
            config.set("realm_settings.elements.glass_panes.fill_remaining", true);
        } else if (fileName.equals("realm_players.yml")) {
            config.set("realm_players.title", "<gradient:#6677EE:#99FFCC>Realms | Players: [name]</gradient>");
            config.set("realm_players.size", 54);
            config.set("realm_players.elements.info_item.slot", 4);
            config.set("realm_players.elements.info_item.material", "WRITTEN_BOOK");
            config.set("realm_players.elements.info_item.name", "<gradient:#6677EE:#99FFCC>Player Management</gradient>");
            config.set("realm_players.elements.info_item.lore", List.of("<gradient:#6677EE:#99FFCC>Manage players for realm: [name]</gradient>"));
            config.set("realm_players.elements.back_button.slot", 45);
            config.set("realm_players.elements.back_button.material", "BARRIER");
            config.set("realm_players.elements.back_button.name", "<red>Back</red>");
            config.set("realm_players.elements.back_button.lore", List.of("<red>Return to Realm Management</red>"));
            config.set("realm_players.elements.invite_player.slot", 49);
            config.set("realm_players.elements.invite_player.material", "WRITABLE_BOOK");
            config.set("realm_players.elements.invite_player.name", "<gradient:#6677EE:#99FFCC>Invite Player</gradient>");
            config.set("realm_players.elements.invite_player.lore", List.of("<gradient:#6677EE:#99FFCC>Invite a player to this Realm</gradient>"));
            config.set("realm_players.elements.previous_page.slot", 46);
            config.set("realm_players.elements.previous_page.material", "ARROW");
            config.set("realm_players.elements.previous_page.name", "<gradient:#6677EE:#99FFCC>Previous Page</gradient>");
            config.set("realm_players.elements.previous_page.lore", List.of("<gradient:#6677EE:#99FFCC>Go to previous page</gradient>"));
            config.set("realm_players.elements.next_page.slot", 52);
            config.set("realm_players.elements.next_page.material", "ARROW");
            config.set("realm_players.elements.next_page.name", "<gradient:#6677EE:#99FFCC>Next Page</gradient>");
            config.set("realm_players.elements.next_page.lore", List.of("<gradient:#6677EE:#99FFCC>Go to next page</gradient>"));
            config.set("realm_players.elements.glass_panes.material", "BLACK_STAINED_GLASS_PANE");
            config.set("realm_players.elements.glass_panes.name", " ");
            config.set("realm_players.elements.glass_panes.fill_remaining", true);
        } else if (fileName.equals("realm_creation.yml")) {
            config.set("realm_creation.title", "<gradient:#6677EE:#99FFCC>Realms | Create Realm</gradient>");
            config.set("realm_creation.size", 27);
            config.set("realm_creation.elements.info_item.slot", 13);
            config.set("realm_creation.elements.info_item.material", "WRITTEN_BOOK");
            config.set("realm_creation.elements.info_item.name", "<gradient:#6677EE:#99FFCC>Create Realm</gradient>");
            config.set("realm_creation.elements.info_item.lore", List.of(
                "<gradient:#6677EE:#99FFCC>To create a realm:</gradient>",
                "<gradient:#6677EE:#99FFCC>1. Close this menu</gradient>",
                "<gradient:#6677EE:#99FFCC>2. Type /realms create <name> [type]</gradient>",
                "<gradient:#6677EE:#99FFCC>In chat with the name you want.</gradient>",
                " ",
                "<gradient:#6677EE:#99FFCC>Valid types: FLAT, NORMAL, AMPLIFIED</gradient>",
                "<gradient:#6677EE:#99FFCC>Names can only contain letters, numbers, and underscores.</gradient>"
            ));
            config.set("realm_creation.elements.glass_panes.material", "BLACK_STAINED_GLASS_PANE");
            config.set("realm_creation.elements.glass_panes.name", " ");
            config.set("realm_creation.elements.glass_panes.fill_remaining", true);
        } else if (fileName.equals("border_color.yml")) {
            config.set("border_color.title", "<gradient:#6677EE:#99FFCC>Realms | Border Color</gradient>");
            config.set("border_color.size", 27);
            config.set("border_color.elements.info_item.slot", 13);
            config.set("border_color.elements.info_item.material", "WRITTEN_BOOK");
            config.set("border_color.elements.info_item.name", "<gradient:#6677EE:#99FFCC>Border Color Selection</gradient>");
            config.set("border_color.elements.info_item.lore", List.of(
                "<gradient:#6677EE:#99FFCC>Select a color for your world border:</gradient>",
                " ",
                "<gradient:#6677EE:#99FFCC>BLUE: Standard world border</gradient>",
                "<gradient:#6677EE:#99FFCC>GREEN: Smaller warning area</gradient>",
                "<gradient:#6677EE:#99FFCC>RED: Larger warning area</gradient>"
            ));
            config.set("border_color.elements.blue_border.slot", 10);
            config.set("border_color.elements.blue_border.material", "BLUE_DYE");
            config.set("border_color.elements.blue_border.name", "<blue>Blue Border</blue>");
            config.set("border_color.elements.blue_border.lore", List.of("<blue>Click to set border color to blue</blue>"));
            config.set("border_color.elements.green_border.slot", 12);
            config.set("border_color.elements.green_border.material", "GREEN_DYE");
            config.set("border_color.elements.green_border.name", "<green>Green Border</green>");
            config.set("border_color.elements.green_border.lore", List.of("<green>Click to set border color to green</green>"));
            config.set("border_color.elements.red_border.slot", 14);
            config.set("border_color.elements.red_border.material", "RED_DYE");
            config.set("border_color.elements.red_border.name", "<red>Red Border</red>");
            config.set("border_color.elements.red_border.lore", List.of("<red>Click to set border color to red</red>"));
            config.set("border_color.elements.back_button.slot", 22);
            config.set("border_color.elements.back_button.material", "BARRIER");
            config.set("border_color.elements.back_button.name", "<red>Back</red>");
            config.set("border_color.elements.back_button.lore", List.of("<red>Return to main menu</red>"));
            config.set("border_color.elements.glass_panes.material", "BLACK_STAINED_GLASS_PANE");
            config.set("border_color.elements.glass_panes.name", " ");
            config.set("border_color.elements.glass_panes.fill_remaining", true);
        } else if (fileName.equals("upgrade_menu.yml")) {
            config.set("upgrade_menu.title", "<gradient:#6677EE:#99FFCC>Realms | Upgrades</gradient>");
            config.set("upgrade_menu.size", 54);
            config.set("upgrade_menu.elements.info_item.slot", 4);
            config.set("upgrade_menu.elements.info_item.material", "WRITTEN_BOOK");
            config.set("upgrade_menu.elements.info_item.name", "<gradient:#6677EE:#99FFCC>Upgrade Information</gradient>");
            config.set("upgrade_menu.elements.info_item.lore", List.of(
                "<gradient:#6677EE:#99FFCC>Here you can upgrade various aspects of your realm.</gradient>",
                " ",
                "<gradient:#6677EE:#99FFCC>Each upgrade has different levels with increasing costs.</gradient>",
                "<gradient:#6677EE:#99FFCC>You can only upgrade aspects of realms you own.</gradient>"
            ));
            config.set("upgrade_menu.elements.back_button.slot", 53);
            config.set("upgrade_menu.elements.back_button.material", "BARRIER");
            config.set("upgrade_menu.elements.back_button.name", "<red>Back</red>");
            config.set("upgrade_menu.elements.back_button.lore", List.of("<red>Return to main menu</red>"));
            config.set("upgrade_menu.elements.glass_panes.material", "BLACK_STAINED_GLASS_PANE");
            config.set("upgrade_menu.elements.glass_panes.name", " ");
            config.set("upgrade_menu.elements.glass_panes.fill_remaining", true);
        }
        
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save default menu config: " + e.getMessage());
        }
    }
    
    private void loadConfig(String fileName, String configName) {
        File file = new File(plugin.getDataFolder(), "menu/" + fileName);
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        switch (configName) {
            case "main_menu":
                mainMenuConfig = config;
                break;
            case "realms_list":
                realmsListConfig = config;
                break;
            case "realm_management":
                realmManagementConfig = config;
                break;
            case "realm_settings":
                realmSettingsConfig = config;
                break;
            case "realm_players":
                realmPlayersConfig = config;
                break;
            case "realm_creation":
                realmCreationConfig = config;
                break;
            case "border_color":
                borderColorConfig = config;
                break;
            case "upgrade_menu":
                upgradeConfig = config;
                break;
        }
    }
    
    public void openMainMenu(Player player) {
        String title = processPlaceholders(mainMenuConfig.getString("main_menu.title"), player, null);
        title = ColorUtils.processColors(title, player);
        int size = mainMenuConfig.getInt("main_menu.size", 27);
        Inventory inventory = Bukkit.createInventory(null, size, title);
        
        // Create and set elements
        Set<String> elementKeys = mainMenuConfig.getConfigurationSection("main_menu.elements").getKeys(false);
        for (String elementKey : elementKeys) {
            if (!elementKey.equals("glass_panes")) {
                createAndSetElement(inventory, elementKey, player, null, "main_menu");
            }
        }
        
        // Handle glass panes fill
        if (mainMenuConfig.getBoolean("main_menu.elements.glass_panes.fill_remaining", false)) {
            String material = mainMenuConfig.getString("main_menu.elements.glass_panes.material", "BLACK_STAINED_GLASS_PANE");
            String name = processPlaceholders(mainMenuConfig.getString("main_menu.elements.glass_panes.name", " "), player, null);
            name = ColorUtils.processColors(name, player);
            
            for (int i = 0; i < size; i++) {
                if (inventory.getItem(i) == null) {
                    ItemStack item = createItem(Material.getMaterial(material), name, null);
                    inventory.setItem(i, item);
                }
            }
        }
        
        player.openInventory(inventory);
    }
    
    private void createAndSetElement(Inventory inventory, String elementKey, Player player, String realmName) {
        FileConfiguration config = getCurrentConfig(elementKey);
        int slot = config.getInt(getCurrentConfigPath(elementKey) + ".slot", -1);
        if (slot < 0) return;
        
        String material = config.getString(getCurrentConfigPath(elementKey) + ".material");
        String name = config.getString(getCurrentConfigPath(elementKey) + ".name");
        name = processPlaceholders(name, player, realmName);
        name = ColorUtils.processColors(name, player);
        
        List<String> lore = null;
        if (config.isList(getCurrentConfigPath(elementKey) + ".lore")) {
            List<String> rawLore = config.getStringList(getCurrentConfigPath(elementKey) + ".lore");
            lore = new ArrayList<>();
            for (String loreLine : rawLore) {
                loreLine = processPlaceholders(loreLine, player, realmName);
                loreLine = ColorUtils.processColors(loreLine, player);
                lore.add(loreLine);
            }
        }
        
        // Check permission if required
        String permissionRequired = config.getString(getCurrentConfigPath(elementKey) + ".permission_required");
        if (permissionRequired != null && !permissionRequired.isEmpty()) {
            boolean hasPermission = false;
            for (String perm : permissionRequired.split(",")) {
                // Special handling for realm.owner permission
                if (perm.trim().equals("realm.owner")) {
                    if (realmName != null) {
                        com.minekarta.advancedcorerealms.data.object.Realm realm = 
                            plugin.getWorldDataManager().getRealm(realmName);
                        if (realm != null && realm.getOwner().equals(player.getUniqueId())) {
                            hasPermission = true;
                            break;
                        }
                    }
                } else if (player.hasPermission(perm.trim())) {
                    hasPermission = true;
                    break;
                }
            }
            
            if (!hasPermission) {
                // Use no_permission versions if available
                material = config.getString(getCurrentConfigPath(elementKey) + ".no_permission_material", material);
                String noPermName = config.getString(getCurrentConfigPath(elementKey) + ".no_permission_name", name);
                noPermName = processPlaceholders(noPermName, player, realmName);
                name = ColorUtils.processColors(noPermName, player);
                
                if (config.isList(getCurrentConfigPath(elementKey) + ".no_permission_lore")) {
                    List<String> rawNoPermLore = config.getStringList(getCurrentConfigPath(elementKey) + ".no_permission_lore");
                    lore = new ArrayList<>();
                    for (String loreLine : rawNoPermLore) {
                        loreLine = processPlaceholders(loreLine, player, realmName);
                        loreLine = ColorUtils.processColors(loreLine, player);
                        lore.add(loreLine);
                    }
                }
            }
        }
        
        if (material != null) {
            ItemStack item = createItem(Material.getMaterial(material), name, lore);
            inventory.setItem(slot, item);
        }
    }
    
    private FileConfiguration getCurrentConfig(String configName) {
        switch (configName) {
            case "main_menu":
                return mainMenuConfig;
            case "realms_list":
                return realmsListConfig;
            case "realm_management":
                return realmManagementConfig;
            case "realm_settings":
                return realmSettingsConfig;
            case "realm_players":
                return realmPlayersConfig;
            case "realm_creation":
                return realmCreationConfig;
            case "border_color":
                return borderColorConfig;
            case "upgrade_menu":
                return upgradeConfig;
            default:
                return mainMenuConfig;
        }
    }
    
    private String getCurrentConfigPath(String configName, String elementKey) {
        return configName + ".elements." + elementKey;
    }
    
    private void createAndSetElement(Inventory inventory, String elementKey, Player player, String realmName, String configName) {
        FileConfiguration config = getCurrentConfig(configName);
        int slot = config.getInt(getCurrentConfigPath(configName, elementKey) + ".slot", -1);
        if (slot < 0) return;
        
        String material = config.getString(getCurrentConfigPath(configName, elementKey) + ".material");
        String name = config.getString(getCurrentConfigPath(configName, elementKey) + ".name");
        name = processPlaceholders(name, player, realmName);
        name = ColorUtils.processColors(name, player);
        
        List<String> lore = null;
        if (config.isList(getCurrentConfigPath(configName, elementKey) + ".lore")) {
            List<String> rawLore = config.getStringList(getCurrentConfigPath(configName, elementKey) + ".lore");
            lore = new ArrayList<>();
            for (String loreLine : rawLore) {
                loreLine = processPlaceholders(loreLine, player, realmName);
                loreLine = ColorUtils.processColors(loreLine, player);
                lore.add(loreLine);
            }
        }
        
        // Check permission if required
        String permissionRequired = config.getString(getCurrentConfigPath(configName, elementKey) + ".permission_required");
        if (permissionRequired != null && !permissionRequired.isEmpty()) {
            boolean hasPermission = false;
            for (String perm : permissionRequired.split(",")) {
                // Special handling for realm.owner permission
                if (perm.trim().equals("realm.owner")) {
                    if (realmName != null) {
                        com.minekarta.advancedcorerealms.data.object.Realm realm = 
                            plugin.getWorldDataManager().getRealm(realmName);
                        if (realm != null && realm.getOwner().equals(player.getUniqueId())) {
                            hasPermission = true;
                            break;
                        }
                    }
                } else if (player.hasPermission(perm.trim())) {
                    hasPermission = true;
                    break;
                }
            }
            
            if (!hasPermission) {
                // Use no_permission versions if available
                material = config.getString(getCurrentConfigPath(configName, elementKey) + ".no_permission_material", material);
                String noPermName = config.getString(getCurrentConfigPath(configName, elementKey) + ".no_permission_name", name);
                noPermName = processPlaceholders(noPermName, player, realmName);
                name = ColorUtils.processColors(noPermName, player);
                
                if (config.isList(getCurrentConfigPath(configName, elementKey) + ".no_permission_lore")) {
                    List<String> rawNoPermLore = config.getStringList(getCurrentConfigPath(configName, elementKey) + ".no_permission_lore");
                    lore = new ArrayList<>();
                    for (String loreLine : rawNoPermLore) {
                        loreLine = processPlaceholders(loreLine, player, realmName);
                        loreLine = ColorUtils.processColors(loreLine, player);
                        lore.add(loreLine);
                    }
                }
            }
        }
        
        if (material != null) {
            ItemStack item = createItem(Material.getMaterial(material), name, lore);
            inventory.setItem(slot, item);
        }
    }
    
    /**
     * Process placeholders in a string
     * @param input The string to process
     * @param player The player for PAPI placeholders
     * @param realmName The realm name for [name] placeholder
     * @return The processed string
     */
    private String processPlaceholders(String input, Player player, String realmName) {
        if (input == null) return null;
        
        // Replace built-in placeholders
        if (realmName != null) {
            input = input.replace("[name]", realmName);
        }
        
        // Process PlaceholderAPI placeholders if available and player is provided
        if (player != null && Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            input = PlaceholderAPI.setPlaceholders(player, input);
        }
        
        return input;
    }
    
    private ItemStack createItem(Material material, String name, List<String> lore) {
        if (material == null) {
            material = Material.BARRIER; // Default fallback
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (name != null) {
            // Convert MiniMessage formatted string to Minecraft text component
            Component nameComponent = miniMessage.deserialize(name);
            String formattedName = miniMessage.serialize(nameComponent);
            meta.setDisplayName(formattedName);
        }
        
        if (lore != null && !lore.isEmpty()) {
            List<String> formattedLore = new ArrayList<>();
            for (String loreLine : lore) {
                Component loreComponent = miniMessage.deserialize(loreLine);
                String formattedLine = miniMessage.serialize(loreComponent);
                formattedLore.add(formattedLine);
            }
            meta.setLore(formattedLore);
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createPlayerHead(OfflinePlayer player, String name, List<String> lore) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(player);
        
        if (name != null) {
            Component nameComponent = miniMessage.deserialize(name);
            String formattedName = miniMessage.serialize(nameComponent);
            meta.setDisplayName(formattedName);
        }
        
        if (lore != null && !lore.isEmpty()) {
            List<String> formattedLore = new ArrayList<>();
            for (String loreLine : lore) {
                Component loreComponent = miniMessage.deserialize(loreLine);
                String formattedLine = miniMessage.serialize(loreComponent);
                formattedLore.add(formattedLine);
            }
            meta.setLore(formattedLore);
        }
        
        skull.setItemMeta(meta);
        return skull;
    }
    
    // Implement all menu methods
    public void openRealmsListMenu(Player player, boolean ownRealms, int page) {
        String titleTemplate = ownRealms ? 
            realmsListConfig.getString("realms_list.my_realms_title", "<gradient:#6677EE:#99FFCC>My Realms</gradient>") :
            realmsListConfig.getString("realms_list.accessible_realms_title", "<gradient:#6677EE:#99FFCC>Accessible Realms</gradient>");
        
        String title = processPlaceholders(titleTemplate, player, null);
        title = ColorUtils.processColors(title, player);
        int size = realmsListConfig.getInt("realms_list.size", 54);
        Inventory inventory = Bukkit.createInventory(null, size, title);
        
        // Create and set elements
        Set<String> elementKeys = realmsListConfig.getConfigurationSection("realms_list.elements").getKeys(false);
        for (String elementKey : elementKeys) {
            if (!elementKey.equals("glass_panes") && !elementKey.equals("realm_items")) {
                createAndSetElement(inventory, elementKey, player, null, "realms_list");
            }
        }
        
        // Handle glass panes fill
        if (realmsListConfig.getBoolean("realms_list.elements.glass_panes.fill_remaining", false)) {
            String material = realmsListConfig.getString("realms_list.elements.glass_panes.material", "BLACK_STAINED_GLASS_PANE");
            String name = processPlaceholders(realmsListConfig.getString("realms_list.elements.glass_panes.name", " "), player, null);
            name = ColorUtils.processColors(name, player);
            
            for (int i = 0; i < size; i++) {
                if (inventory.getItem(i) == null) {
                    ItemStack item = createItem(Material.getMaterial(material), name, null);
                    inventory.setItem(i, item);
                }
            }
        }
        
        // Add realm items dynamically based on the realm type (own or accessible)
        List<com.minekarta.advancedcorerealms.data.object.Realm> realms = ownRealms ? 
            plugin.getWorldDataManager().getPlayerRealms(player.getUniqueId()) :
            plugin.getWorldDataManager().getPlayerInvitedRealms(player.getUniqueId());
        
        // Calculate pagination
        int itemsPerPage = 45; // Slots 9-53 (excluding navigation buttons)
        int totalPages = (int) Math.ceil((double) realms.size() / itemsPerPage);
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, realms.size());
        
        // Add realm items (slots 9-53, excluding navigation buttons)
        int slot = 9; // Start from slot 9 (first row, second column)
        for (int i = startIndex; i < endIndex; i++) {
            if (slot == 45 || slot == 53) { // Skip navigation slots
                slot++;
            }
            if (slot > 53) break; // Don't exceed inventory size
            
            com.minekarta.advancedcorerealms.data.object.Realm realm = realms.get(i);
            Material worldMaterial = realm.isFlat() ? Material.GRASS_BLOCK : Material.STONE;
            
            String realmName = realm.getName();
            String worldStatus = realm.getBukkitWorld() != null ? "Loaded" : "Unloaded";
            int playerCount = realm.getBukkitWorld() != null ? realm.getBukkitWorld().getPlayers().size() : 0;
            
            ItemStack realmItem = createItem(worldMaterial, realmName, 
                "Players: " + playerCount + "
Status: " + worldStatus);
            inventory.setItem(slot, realmItem);
            slot++;
        }
        
        // Add pagination buttons if needed
        int currentSlot = 45; // Previous page button
        if (page > 1) {
            String prevName = processPlaceholders(realmsListConfig.getString("realms_list.elements.previous_page.name", "<gradient:#6677EE:#99FFCC>Previous Page</gradient>"), player, null);
            prevName = ColorUtils.processColors(prevName, player);
            List<String> prevLore = ColorUtils.processColorList(
                realmsListConfig.getStringList("realms_list.elements.previous_page.lore"), player);
            ItemStack prevPage = createItem(
                Material.getMaterial(realmsListConfig.getString("realms_list.elements.previous_page.material", "ARROW")), 
                prevName, 
                prevLore
            );
            inventory.setItem(currentSlot, prevPage);
        }
        
        currentSlot = 53; // Next page button
        if (page < totalPages) {
            String nextName = processPlaceholders(realmsListConfig.getString("realms_list.elements.next_page.name", "<gradient:#6677EE:#99FFCC>Next Page</gradient>"), player, null);
            nextName = ColorUtils.processColors(nextName, player);
            List<String> nextLore = ColorUtils.processColorList(
                realmsListConfig.getStringList("realms_list.elements.next_page.lore"), player);
            ItemStack nextPage = createItem(
                Material.getMaterial(realmsListConfig.getString("realms_list.elements.next_page.material", "ARROW")), 
                nextName, 
                nextLore
            );
            inventory.setItem(currentSlot, nextPage);
        }
        
        // For now we'll just open the inventory and let the dynamic content be handled elsewhere
        
        player.openInventory(inventory);
    }
    
    public void openRealmManagementMenu(Player player, String realmName) {
        String title = processPlaceholders(realmManagementConfig.getString("realm_management.title"), player, realmName);
        title = ColorUtils.processColors(title, player);
        int size = realmManagementConfig.getInt("realm_management.size", 36);
        Inventory inventory = Bukkit.createInventory(null, size, title);
        
        // Create and set elements
        Set<String> elementKeys = realmManagementConfig.getConfigurationSection("realm_management.elements").getKeys(false);
        for (String elementKey : elementKeys) {
            if (!elementKey.equals("glass_panes")) {
                createAndSetElement(inventory, elementKey, player, realmName, "realm_management");
            }
        }
        
        // Handle glass panes fill
        if (realmManagementConfig.getBoolean("realm_management.elements.glass_panes.fill_remaining", false)) {
            String material = realmManagementConfig.getString("realm_management.elements.glass_panes.material", "BLACK_STAINED_GLASS_PANE");
            String name = processPlaceholders(realmManagementConfig.getString("realm_management.elements.glass_panes.name", " "), player, realmName);
            name = ColorUtils.processColors(name, player);
            
            for (int i = 0; i < size; i++) {
                if (inventory.getItem(i) == null) {
                    ItemStack item = createItem(Material.getMaterial(material), name, null);
                    inventory.setItem(i, item);
                }
            }
        }
        
        player.openInventory(inventory);
    }
    
    public void openRealmSettingsMenu(Player player, String realmName) {
        String title = processPlaceholders(realmSettingsConfig.getString("realm_settings.title"), player, realmName);
        title = ColorUtils.processColors(title, player);
        int size = realmSettingsConfig.getInt("realm_settings.size", 36);
        Inventory inventory = Bukkit.createInventory(null, size, title);
        
        // Create and set elements
        Set<String> elementKeys = realmSettingsConfig.getConfigurationSection("realm_settings.elements").getKeys(false);
        for (String elementKey : elementKeys) {
            if (!elementKey.equals("glass_panes")) {
                // Special handling for player limit to include current limit
                if (elementKey.equals("player_limit")) {
                    // For player limit, we need to get the realm to show current limit
                    com.minekarta.advancedcorerealms.data.object.Realm realm = 
                        plugin.getWorldDataManager().getRealm(realmName);
                    if (realm != null) {
                        // Process with placeholder for limit
                        createAndSetElementWithPlaceholders(inventory, elementKey, player, realmName, "realm_settings", 
                            "[limit]", String.valueOf(realm.getMaxPlayers()));
                    } else {
                        createAndSetElement(inventory, elementKey, player, realmName, "realm_settings");
                    }
                } else {
                    createAndSetElement(inventory, elementKey, player, realmName, "realm_settings");
                }
            }
        }
        
        // Handle glass panes fill
        if (realmSettingsConfig.getBoolean("realm_settings.elements.glass_panes.fill_remaining", false)) {
            String material = realmSettingsConfig.getString("realm_settings.elements.glass_panes.material", "BLACK_STAINED_GLASS_PANE");
            String name = processPlaceholders(realmSettingsConfig.getString("realm_settings.elements.glass_panes.name", " "), player, realmName);
            name = ColorUtils.processColors(name, player);
            
            for (int i = 0; i < size; i++) {
                if (inventory.getItem(i) == null) {
                    ItemStack item = createItem(Material.getMaterial(material), name, null);
                    inventory.setItem(i, item);
                }
            }
        }
        
        player.openInventory(inventory);
    }
    
    private void createAndSetElementWithPlaceholders(Inventory inventory, String elementKey, Player player, String realmName, String configName, String placeholder, String replacement) {
        FileConfiguration config = getCurrentConfig(configName);
        int slot = config.getInt(getCurrentConfigPath(configName, elementKey) + ".slot", -1);
        if (slot < 0) return;
        
        String material = config.getString(getCurrentConfigPath(configName, elementKey) + ".material");
        String name = config.getString(getCurrentConfigPath(configName, elementKey) + ".name");
        // Replace the custom placeholder first
        name = name.replace(placeholder, replacement);
        name = processPlaceholders(name, player, realmName);
        name = ColorUtils.processColors(name, player);
        
        List<String> lore = null;
        if (config.isList(getCurrentConfigPath(configName, elementKey) + ".lore")) {
            List<String> rawLore = config.getStringList(getCurrentConfigPath(configName, elementKey) + ".lore");
            lore = new ArrayList<>();
            for (String loreLine : rawLore) {
                // Replace the custom placeholder first
                loreLine = loreLine.replace(placeholder, replacement);
                loreLine = processPlaceholders(loreLine, player, realmName);
                loreLine = ColorUtils.processColors(loreLine, player);
                lore.add(loreLine);
            }
        }
        
        // Check permission if required
        String permissionRequired = config.getString(getCurrentConfigPath(configName, elementKey) + ".permission_required");
        if (permissionRequired != null && !permissionRequired.isEmpty()) {
            boolean hasPermission = false;
            for (String perm : permissionRequired.split(",")) {
                // Special handling for realm.owner permission
                if (perm.trim().equals("realm.owner")) {
                    if (realmName != null) {
                        com.minekarta.advancedcorerealms.data.object.Realm realm = 
                            plugin.getWorldDataManager().getRealm(realmName);
                        if (realm != null && realm.getOwner().equals(player.getUniqueId())) {
                            hasPermission = true;
                            break;
                        }
                    }
                } else if (player.hasPermission(perm.trim())) {
                    hasPermission = true;
                    break;
                }
            }
            
            if (!hasPermission) {
                // Use no_permission versions if available
                material = config.getString(getCurrentConfigPath(configName, elementKey) + ".no_permission_material", material);
                String noPermName = config.getString(getCurrentConfigPath(configName, elementKey) + ".no_permission_name", name);
                noPermName = noPermName.replace(placeholder, replacement);
                noPermName = processPlaceholders(noPermName, player, realmName);
                name = ColorUtils.processColors(noPermName, player);
                
                if (config.isList(getCurrentConfigPath(configName, elementKey) + ".no_permission_lore")) {
                    List<String> rawNoPermLore = config.getStringList(getCurrentConfigPath(configName, elementKey) + ".no_permission_lore");
                    lore = new ArrayList<>();
                    for (String loreLine : rawNoPermLore) {
                        loreLine = loreLine.replace(placeholder, replacement);
                        loreLine = processPlaceholders(loreLine, player, realmName);
                        loreLine = ColorUtils.processColors(loreLine, player);
                        lore.add(loreLine);
                    }
                }
            }
        }
        
        if (material != null) {
            ItemStack item = createItem(Material.getMaterial(material), name, lore);
            inventory.setItem(slot, item);
        }
    }
    
    public void openRealmPlayersMenu(Player player, String realmName, int page) {
        String title = processPlaceholders(realmPlayersConfig.getString("realm_players.title"), player, realmName);
        title = ColorUtils.processColors(title, player);
        int size = realmPlayersConfig.getInt("realm_players.size", 54);
        Inventory inventory = Bukkit.createInventory(null, size, title);
        
        // Create and set elements
        Set<String> elementKeys = realmPlayersConfig.getConfigurationSection("realm_players.elements").getKeys(false);
        for (String elementKey : elementKeys) {
            if (!elementKey.equals("glass_panes") && !elementKey.equals("player_items")) {
                // Special handling for info item to include realm name
                if (elementKey.equals("info_item")) {
                    createAndSetElementWithPlaceholders(inventory, elementKey, player, realmName, "realm_players", 
                        "[name]", realmName);
                } else {
                    createAndSetElement(inventory, elementKey, player, realmName, "realm_players");
                }
            }
        }
        
        // Handle glass panes fill
        if (realmPlayersConfig.getBoolean("realm_players.elements.glass_panes.fill_remaining", false)) {
            String material = realmPlayersConfig.getString("realm_players.elements.glass_panes.material", "BLACK_STAINED_GLASS_PANE");
            String name = processPlaceholders(realmPlayersConfig.getString("realm_players.elements.glass_panes.name", " "), player, realmName);
            name = ColorUtils.processColors(name, player);
            
            for (int i = 0; i < size; i++) {
                if (inventory.getItem(i) == null) {
                    ItemStack item = createItem(Material.getMaterial(material), name, null);
                    inventory.setItem(i, item);
                }
            }
        }
        
        // Add player items dynamically
        com.minekarta.advancedcorerealms.data.object.Realm realm = plugin.getWorldDataManager().getRealm(realmName);
        if (realm != null) {
            List<java.util.UUID> allPlayers = new ArrayList<>();
            allPlayers.add(realm.getOwner()); // Add owner first
            for (java.util.UUID member : realm.getMembers()) {
                if (!member.equals(realm.getOwner())) { // Don't duplicate owner
                    allPlayers.add(member);
                }
            }
            
            // Calculate pagination
            int itemsPerPage = 36; // Slots 9-44 (excluding navigation buttons)
            int totalPages = (int) Math.ceil((double) allPlayers.size() / itemsPerPage);
            int startIndex = (page - 1) * itemsPerPage;
            int endIndex = Math.min(startIndex + itemsPerPage, allPlayers.size());
            
            // Add player items (slots 9-44, excluding navigation buttons)
            int slot = 9; // Start from slot 9
            for (int i = startIndex; i < endIndex; i++) {
                if (slot == 45) { // Skip back button slot if needed
                    slot++;
                }
                if (slot > 44) break; // Don't exceed player slots
                
                java.util.UUID playerId = allPlayers.get(i);
                boolean isOwner = playerId.equals(realm.getOwner());
                org.bukkit.OfflinePlayer offlinePlayer = org.bukkit.Bukkit.getOfflinePlayer(playerId);
                
                String playerName = offlinePlayer.getName() != null ? offlinePlayer.getName() : "Unknown Player";
                String status = isOwner ? "Owner" : "Member";
                Material itemMaterial = isOwner ? Material.GOLDEN_HELMET : Material.PLAYER_HEAD;
                
                ItemStack playerItem;
                if (isOwner) {
                    playerItem = createItem(itemMaterial, playerName, "Status: " + status);
                } else {
                    playerItem = createPlayerHead(offlinePlayer, playerName, "Status: " + status);
                }
                
                inventory.setItem(slot, playerItem);
                slot++;
            }
            
            // Add pagination buttons if needed
            int currentSlot = 46; // Previous page button
            if (page > 1) {
                String prevName = processPlaceholders(realmPlayersConfig.getString("realm_players.elements.previous_page.name", "<gradient:#6677EE:#99FFCC>Previous Page</gradient>"), player, realmName);
                prevName = ColorUtils.processColors(prevName, player);
                List<String> prevLore = ColorUtils.processColorList(
                    realmPlayersConfig.getStringList("realm_players.elements.previous_page.lore"), player);
                ItemStack prevPage = createItem(
                    Material.getMaterial(realmPlayersConfig.getString("realm_players.elements.previous_page.material", "ARROW")), 
                    prevName, 
                    prevLore
                );
                inventory.setItem(currentSlot, prevPage);
            }
            
            currentSlot = 52; // Next page button
            if (page < totalPages) {
                String nextName = processPlaceholders(realmPlayersConfig.getString("realm_players.elements.next_page.name", "<gradient:#6677EE:#99FFCC>Next Page</gradient>"), player, realmName);
                nextName = ColorUtils.processColors(nextName, player);
                List<String> nextLore = ColorUtils.processColorList(
                    realmPlayersConfig.getStringList("realm_players.elements.next_page.lore"), player);
                ItemStack nextPage = createItem(
                    Material.getMaterial(realmPlayersConfig.getString("realm_players.elements.next_page.material", "ARROW")), 
                    nextName, 
                    nextLore
                );
                inventory.setItem(currentSlot, nextPage);
            }
        }
        
        player.openInventory(inventory);
    }
    
    public void openRealmCreationMenu(Player player) {
        String title = processPlaceholders(realmCreationConfig.getString("realm_creation.title"), player, null);
        title = ColorUtils.processColors(title, player);
        int size = realmCreationConfig.getInt("realm_creation.size", 27);
        Inventory inventory = Bukkit.createInventory(null, size, title);
        
        // Create and set elements
        Set<String> elementKeys = realmCreationConfig.getConfigurationSection("realm_creation.elements").getKeys(false);
        for (String elementKey : elementKeys) {
            if (!elementKey.equals("glass_panes")) {
                createAndSetElement(inventory, elementKey, player, null, "realm_creation");
            }
        }
        
        // Handle glass panes fill
        if (realmCreationConfig.getBoolean("realm_creation.elements.glass_panes.fill_remaining", false)) {
            String material = realmCreationConfig.getString("realm_creation.elements.glass_panes.material", "BLACK_STAINED_GLASS_PANE");
            String name = processPlaceholders(realmCreationConfig.getString("realm_creation.elements.glass_panes.name", " "), player, null);
            name = ColorUtils.processColors(name, player);
            
            for (int i = 0; i < size; i++) {
                if (inventory.getItem(i) == null) {
                    ItemStack item = createItem(Material.getMaterial(material), name, null);
                    inventory.setItem(i, item);
                }
            }
        }
        
        player.openInventory(inventory);
        
        // Auto-close after 3 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.getOpenInventory().getTitle().equals(title)) {
                player.closeInventory();
            }
        }, 60L); // 60 ticks = 3 seconds
    }
    
    public void openBorderColorMenu(Player player) {
        String title = processPlaceholders(borderColorConfig.getString("border_color.title"), player, null);
        title = ColorUtils.processColors(title, player);
        int size = borderColorConfig.getInt("border_color.size", 27);
        Inventory inventory = Bukkit.createInventory(null, size, title);
        
        // Create and set elements
        Set<String> elementKeys = borderColorConfig.getConfigurationSection("border_color.elements").getKeys(false);
        for (String elementKey : elementKeys) {
            if (!elementKey.equals("glass_panes")) {
                createAndSetElement(inventory, elementKey, player, null, "border_color");
            }
        }
        
        // Handle glass panes fill
        if (borderColorConfig.getBoolean("border_color.elements.glass_panes.fill_remaining", false)) {
            String material = borderColorConfig.getString("border_color.elements.glass_panes.material", "BLACK_STAINED_GLASS_PANE");
            String name = processPlaceholders(borderColorConfig.getString("border_color.elements.glass_panes.name", " "), player, null);
            name = ColorUtils.processColors(name, player);
            
            for (int i = 0; i < size; i++) {
                if (inventory.getItem(i) == null) {
                    ItemStack item = createItem(Material.getMaterial(material), name, null);
                    inventory.setItem(i, item);
                }
            }
        }
        
        player.openInventory(inventory);
    }
    
    public void openUpgradeMenu(Player player) {
        String title = processPlaceholders(upgradeConfig.getString("upgrade_menu.title"), player, null);
        title = ColorUtils.processColors(title, player);
        int size = upgradeConfig.getInt("upgrade_menu.size", 54);
        Inventory inventory = Bukkit.createInventory(null, size, title);
        
        // Create and set elements
        Set<String> elementKeys = upgradeConfig.getConfigurationSection("upgrade_menu.elements").getKeys(false);
        for (String elementKey : elementKeys) {
            if (!elementKey.equals("glass_panes")) {
                createAndSetElement(inventory, elementKey, player, null, "upgrade_menu");
            }
        }
        
        // Handle glass panes fill
        if (upgradeConfig.getBoolean("upgrade_menu.elements.glass_panes.fill_remaining", false)) {
            String material = upgradeConfig.getString("upgrade_menu.elements.glass_panes.material", "BLACK_STAINED_GLASS_PANE");
            String name = processPlaceholders(upgradeConfig.getString("upgrade_menu.elements.glass_panes.name", " "), player, null);
            name = ColorUtils.processColors(name, player);
            
            for (int i = 0; i < size; i++) {
                if (inventory.getItem(i) == null) {
                    ItemStack item = createItem(Material.getMaterial(material), name, null);
                    inventory.setItem(i, item);
                }
            }
        }
        
        player.openInventory(inventory);
    }
}