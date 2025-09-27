package com.minekarta.advancedcorerealms.gui;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.gui.menu.MainMenu;
import com.minekarta.advancedcorerealms.gui.menu.WorldListMenu;
import com.minekarta.advancedcorerealms.gui.menu.WorldSettingsMenu;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class GuiManager implements Listener {
    
    private final AdvancedCoreRealms plugin;
    private final MainMenu mainMenu;
    private final WorldListMenu worldListMenu;
    
    public GuiManager(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.mainMenu = new MainMenu(plugin);
        this.worldListMenu = new WorldListMenu(plugin);
    }
    
    public void openMainMenu(Player player) {
        mainMenu.openMainMenu(player);
    }
    
    public void openWorldListMenu(Player player) {
        worldListMenu.openWorldListMenu(player);
    }
    
    public void openWorldSettingsMenu(Player player, String realmName) {
        WorldSettingsMenu settingsMenu = new WorldSettingsMenu(plugin, realmName);
        settingsMenu.openWorldSettingsMenu(player);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        
        String inventoryTitle = event.getView().getTitle();
        Player player = (Player) event.getWhoClicked();
        
        // Handle main menu clicks
        if (inventoryTitle.equals(ChatColor.GOLD + "Your Realms")) {
            event.setCancelled(true);
            
            if (event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) {
                return;
            }
            
            String itemName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
            
            // Handle realm teleportation
            if (event.getCurrentItem().getType() == org.bukkit.Material.GRASS_BLOCK || 
                event.getCurrentItem().getType() == org.bukkit.Material.SHORT_GRASS ||
                event.getCurrentItem().getType() == org.bukkit.Material.BOOK) {
                
                // Extract realm name from item name (removing "(Invited)" suffix if present)
                String realmName = itemName;
                if (realmName.endsWith(" (Invited)")) {
                    realmName = realmName.substring(0, realmName.length() - 9); // Remove " (Invited)"
                }
                
                plugin.getWorldManager().teleportToRealm(player, realmName);
                player.closeInventory();
                return;
            }
            
            // Handle buttons
            if (itemName.equals("Create New Realm")) {
                // Check if player has reached limit
                int realmCount = plugin.getWorldDataManager().getPlayerRealms(player.getUniqueId()).size();
                int maxRealms = 1; // Default
                if (player.hasPermission("advancedcorerealms.limit.realms.3")) maxRealms = 3;
                if (player.hasPermission("advancedcorerealms.limit.realms.5")) maxRealms = 5;
                
                if (realmCount >= maxRealms) {
                    player.sendMessage(ChatColor.RED + "You have reached your realm limit!");
                    player.closeInventory();
                    return;
                }
                
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "To create a new realm, use /realms create <name> [FLAT/NORMAL]");
            } else if (itemName.equals("Refresh")) {
                player.closeInventory();
                mainMenu.openMainMenu(player);
            } else if (itemName.equals("Help & Info")) {
                // Help button - just keep the inventory open for now
            }
        }
        
        // Handle world list menu clicks
        else if (inventoryTitle.equals(ChatColor.GOLD + "All Realms")) {
            event.setCancelled(true);
            
            if (event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) {
                return;
            }
            
            String itemName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
            
            // Handle realm teleportation
            if (event.getCurrentItem().getType() == org.bukkit.Material.GRASS_BLOCK || 
                event.getCurrentItem().getType() == org.bukkit.Material.SHORT_GRASS) {
                
                plugin.getWorldManager().teleportToRealm(player, itemName);
                player.closeInventory();
                return;
            }
        }
        
        // Handle world settings menu clicks
        else if (inventoryTitle.endsWith(" Settings")) {
            event.setCancelled(true);
            
            if (event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) {
                return;
            }
            
            String itemName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
            String realmName = inventoryTitle.replace(" Settings", "");
            realmName = ChatColor.stripColor(realmName);
            
            if (itemName.equals("Delete Realm")) {
                player.closeInventory();
                // Send confirmation message and execute delete command
                player.performCommand("realms delete " + realmName);
            } else if (itemName.equals("Back to Main Menu")) {
                player.closeInventory();
                mainMenu.openMainMenu(player);
            } else if (itemName.equals("Invite Members")) {
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "To invite a player to this realm, use /realms invite " + realmName + " <player>");
            }
        }
    }
}