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
        
        // Load configurations
        loadConfig("main_menu.yml", "main_menu");
        loadConfig("realms_list.yml", "realms_list");
        loadConfig("realm_management.yml", "realm_management");
        loadConfig("realm_settings.yml", "realm_settings");
        loadConfig("realm_players.yml", "realm_players");
        loadConfig("realm_creation.yml", "realm_creation");
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
        if (fileName.equals("realms_list.yml")) {
            config.set("realms_list.title", "<gradient:#6677EE:#99FFCC>My Realms</gradient>");
            config.set("realms_list.size", 54);
            config.set("realms_list.elements.info_item.slot", 4);
            config.set("realms_list.elements.info_item.material", "WRITTEN_BOOK");
            config.set("realms_list.elements.info_item.name", "<gradient:#6677EE:#99FFCC>Realms Info</gradient>");
            config.set("realms_list.elements.info_item.lore", List.of("<gradient:#6677EE:#99FFCC>These are the Realms you own</gradient>"));
            config.set("realms_list.elements.glass_panes.material", "BLACK_STAINED_GLASS_PANE");
            config.set("realms_list.elements.glass_panes.name", " ");
            config.set("realms_list.elements.glass_panes.fill_remaining", true);
        } else if (fileName.equals("realm_management.yml")) {
            config.set("realm_management.title", "<gradient:#6677EE:#99FFCC>Realms | Realm: [name]</gradient>");
            config.set("realm_management.size", 36);
            config.set("realm_management.elements.info_item.slot", 4);
            config.set("realm_management.elements.info_item.material", "WRITTEN_BOOK");
            config.set("realm_management.elements.info_item.name", "<gradient:#6677EE:#99FFCC>Realm Info</gradient>");
            config.set("realm_management.elements.glass_panes.material", "BLACK_STAINED_GLASS_PANE");
            config.set("realm_management.elements.glass_panes.name", " ");
            config.set("realm_management.elements.glass_panes.fill_remaining", true);
        } else if (fileName.equals("realm_settings.yml")) {
            config.set("realm_settings.title", "<gradient:#6677EE:#99FFCC>Realms | Settings: [name]</gradient>");
            config.set("realm_settings.size", 36);
            config.set("realm_settings.elements.info_item.slot", 4);
            config.set("realm_settings.elements.info_item.material", "WRITTEN_BOOK");
            config.set("realm_settings.elements.info_item.name", "<gradient:#6677EE:#99FFCC>Settings Info</gradient>");
            config.set("realm_settings.elements.glass_panes.material", "BLACK_STAINED_GLASS_PANE");
            config.set("realm_settings.elements.glass_panes.name", " ");
            config.set("realm_settings.elements.glass_panes.fill_remaining", true);
        } else if (fileName.equals("realm_players.yml")) {
            config.set("realm_players.title", "<gradient:#6677EE:#99FFCC>Realms | Players: [name]</gradient>");
            config.set("realm_players.size", 54);
            config.set("realm_players.elements.info_item.slot", 4);
            config.set("realm_players.elements.info_item.material", "WRITTEN_BOOK");
            config.set("realm_players.elements.info_item.name", "<gradient:#6677EE:#99FFCC>Player Management</gradient>");
            config.set("realm_players.elements.glass_panes.material", "BLACK_STAINED_GLASS_PANE");
            config.set("realm_players.elements.glass_panes.name", " ");
            config.set("realm_players.elements.glass_panes.fill_remaining", true);
        } else if (fileName.equals("realm_creation.yml")) {
            config.set("realm_creation.title", "<gradient:#6677EE:#99FFCC>Realms | Create Realm</gradient>");
            config.set("realm_creation.size", 27);
            config.set("realm_creation.elements.info_item.slot", 13);
            config.set("realm_creation.elements.info_item.material", "WRITTEN_BOOK");
            config.set("realm_creation.elements.info_item.name", "<gradient:#6677EE:#99FFCC>Create Realm</gradient>");
            config.set("realm_creation.elements.glass_panes.material", "BLACK_STAINED_GLASS_PANE");
            config.set("realm_creation.elements.glass_panes.name", " ");
            config.set("realm_creation.elements.glass_panes.fill_remaining", true);
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
                createAndSetElement(inventory, elementKey, player, null);
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
    
    private FileConfiguration getCurrentConfig(String elementKey) {
        // This is a simplified approach - in a real implementation you'd want to determine
        // which menu configuration is currently active
        return mainMenuConfig; // Default to main menu config
    }
    
    private String getCurrentConfigPath(String elementKey) {
        // This is a simplified approach - in a real implementation you'd want to determine
        // which menu configuration path is currently active
        return "main_menu.elements." + elementKey;
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
    
    // Placeholder methods for other menu types - to be implemented as needed
    public void openRealmsListMenu(Player player) {
        // Implementation will follow similar pattern
    }
    
    public void openRealmManagementMenu(Player player, String realmName) {
        // Implementation will follow similar pattern
    }
    
    public void openRealmSettingsMenu(Player player, String realmName) {
        // Implementation will follow similar pattern
    }
    
    public void openRealmPlayersMenu(Player player, String realmName) {
        // Implementation will follow similar pattern
    }
    
    public void openRealmCreationMenu(Player player) {
        // Implementation will follow similar pattern
    }
}