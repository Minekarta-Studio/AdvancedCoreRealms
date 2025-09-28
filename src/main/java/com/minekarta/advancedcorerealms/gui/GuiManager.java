package com.minekarta.advancedcorerealms.gui;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GUIManager {
    
    private final AdvancedCoreRealms plugin;
    private final MiniMessage miniMessage;
    
    public GUIManager(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }
    
    // Main Menu - Now uses configurable system
    public void openMainMenu(Player player) {
        plugin.getMenuManager().openMainMenu(player);
    }
    
    // Realms List Menu
    public void openRealmsList(Player player, boolean ownRealms) {
        openRealmsList(player, ownRealms, 1);
    }
    
    public void openRealmsList(Player player, boolean ownRealms, int page) {
        // Update player state with current page
        plugin.getPlayerStateManager().setRealmsListPage(player, ownRealms, page);
        // Use the new MenuManager method
        plugin.getMenuManager().openRealmsListMenu(player, ownRealms, page);
    }
    
    // Realm Management Menu
    public void openRealmManagement(Player player, String realmName) {
        // Use the new MenuManager method
        plugin.getMenuManager().openRealmManagementMenu(player, realmName);
    }
    
    // Realm Settings Menu
    public void openRealmSettings(Player player, String realmName) {
        // Use the new MenuManager method
        plugin.getMenuManager().openRealmSettingsMenu(player, realmName);
    }
    
    // Realm Players Menu
    public void openRealmPlayers(Player player, String realmName) {
        openRealmPlayers(player, realmName, 1);
    }
    
    public void openRealmPlayers(Player player, String realmName, int page) {
        // Update player state with current page
        plugin.getPlayerStateManager().setRealmPlayersPage(player, realmName, page);
        // Use the new MenuManager method
        plugin.getMenuManager().openRealmPlayersMenu(player, realmName, page);
    }
    
    // Realm Creation Menu
    public void openRealmCreation(Player player) {
        // Use the new MenuManager method
        plugin.getMenuManager().openRealmCreationMenu(player);
    }
    
    // Helper methods
    private ItemStack createGlassPane() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorUtils.processColors(" "));
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createItem(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        String formattedName = ColorUtils.processColors(name);
        meta.setDisplayName(formattedName);
        
        if (lore != null && !lore.isEmpty()) {
            List<String> loreList = new ArrayList<>();
            for (String line : lore.split("\n")) {
                loreList.add(ColorUtils.processColors(line));
            }
            meta.setLore(loreList);
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createBookItem(String title, String content) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        String formattedTitle = ColorUtils.processColors(title);
        meta.setTitle(formattedTitle);
        meta.setAuthor("AdvancedCoreRealms");
        
        List<String> pages = new ArrayList<>();
        StringBuilder page = new StringBuilder();
        
        // Split content into pages
        for (String line : content.split("\n")) {
            if (page.length() + line.length() > 200) { // If page is getting too long
                String formattedPage = ColorUtils.processColors(page.toString());
                pages.add(formattedPage);
                String formattedLine = ColorUtils.processColors(line);
                page = new StringBuilder(formattedLine + "\n");
            } else {
                String formattedLine = ColorUtils.processColors(line);
                page.append(formattedLine).append("\n");
            }
        }
        if (page.length() > 0) {
            String formattedPage = ColorUtils.processColors(page.toString());
            pages.add(formattedPage);
        }
        
        // Add pages to book
        for (String pageContent : pages) {
            meta.addPage(pageContent);
        }
        
        book.setItemMeta(meta);
        return book;
    }
    
    private ItemStack createPlayerHead(OfflinePlayer player, String name, String lore) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(player);
        String formattedName = ColorUtils.processColors(name);
        meta.setDisplayName(formattedName);
        
        if (lore != null && !lore.isEmpty()) {
            List<String> loreList = new ArrayList<>();
            for (String line : lore.split("\n")) {
                loreList.add(ColorUtils.processColors(line));
            }
            meta.setLore(loreList);
        }
        
        skull.setItemMeta(meta);
        return skull;
    }
    
    private String getOwnerName(UUID ownerId) {
        OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerId);
        return owner.getName();
    }
    
    // Open the border color selection menu
    public void openBorderColorMenu(Player player) {
        // Use the new MenuManager method
        plugin.getMenuManager().openBorderColorMenu(player);
    }
    
    // Open the upgrade menu
    public void openUpgradeMenu(Player player) {
        // Use the new MenuManager method
        plugin.getMenuManager().openUpgradeMenu(player);
    }
    
    // Handle inventory clicks
    public void handleInventoryClick(InventoryClickEvent event, String inventoryTitle) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }
        
        String title = event.getView().getTitle();
        
        // Cancel the event to prevent item movement
        event.setCancelled(true);
        
        // Determine the menu type based on the title
        if (title.equals("AdvancedCoreRealms") || title.contains("Main Menu")) {
            handleMainMenuClick(player, event);
        } else if (title.equals("My Realms") || title.equals("Accessible Realms") || 
                  title.contains("Realms List") || title.contains("Realms -")) {
            handleRealmsListClick(player, event);
        } else if (title.startsWith("Realms | Realm:") || title.contains("Management")) {
            handleRealmManagementClick(player, event);
        } else if (title.startsWith("Realms | Settings:") || title.contains("Settings")) {
            handleRealmSettingsClick(player, event);
        } else if (title.startsWith("Realms | Players:") || title.contains("Players")) {
            handleRealmPlayersClick(player, event);
        } else if (title.startsWith("Realms | Create") || title.contains("Create")) {
            handleRealmCreationClick(player, event);
        } else if (title.equals("Realms | Border Color") || title.contains("Border")) {
            handleBorderColorClick(player, event);
        } else if (title.equals("Realms | Upgrades") || title.contains("Upgrade")) {
            handleUpgradeClick(player, event);
        }
    }
    
    private void handleMainMenuClick(Player player, InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        
        String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
        
        switch (displayName) {
            case "My Realms":
                openRealmsList(player, true);
                break;
            case "Accessible Realms":
                openRealmsList(player, false);
                break;
            case "Create Realm":
            case "Create Realm (Donors Only)":
                if (player.hasPermission("advancedcorerealms.donor.create") || 
                    player.hasPermission("advancedcorerealms.admin.create") ||
                    player.hasPermission("advancedcorerealms.unlimited.create")) {
                    openRealmCreation(player);
                } else {
                    Component message = miniMessage.deserialize("<red>You need donor or admin privileges to create a realm!</red>");
                    player.sendMessage(message);
                }
                break;
            case "Get Items":
                player.closeInventory();
                // This feature will be implemented later based on transferable items
                Component message = miniMessage.deserialize("<yellow>Get items feature coming soon!</yellow>");
                player.sendMessage(message);
                break;
            case "Help":
                player.closeInventory();
                Component helpMessage = miniMessage.deserialize("<gold>Available commands:</gold>");
                player.sendMessage(helpMessage);
                player.sendMessage(miniMessage.deserialize("<aqua>/realms create <name> [type] - Create a realm</aqua>"));
                player.sendMessage(miniMessage.deserialize("<aqua>/realms list - List your realms</aqua>"));
                player.sendMessage(miniMessage.deserialize("<aqua>/realms tp <world> - Teleport to realm</aqua>"));
                player.sendMessage(miniMessage.deserialize("<aqua>/realms invite <world> <player> - Invite player</aqua>"));
                player.sendMessage(miniMessage.deserialize("<aqua>/realms back - Return to previous location</aqua>"));
                break;
            case "Border Color":
                openBorderColorMenu(player);
                break;
            case "Upgrades":
                openUpgradeMenu(player);
                break;
            case "Back":
            case "Close":
                player.closeInventory();
                break;
        }
    }
    
    private void handleRealmsListClick(Player player, InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        
        String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
        
        // Check if it's a realm item (has a specific material)
        if (clickedItem.getType() == Material.GRASS_BLOCK || clickedItem.getType() == Material.STONE ||
            clickedItem.getType() == Material.DIRT || clickedItem.getType() == Material.OAK_LOG) {
            // Realm item clicked - get the realm name from the display name
            String realmName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
            openRealmManagement(player, realmName);
        } else if (displayName.contains("Back") || displayName.contains("Close")) {
            openMainMenu(player);
        } else if (displayName.contains("Previous")) {
            // Get current page and realm type from player state
            boolean ownRealms = event.getView().getTitle().contains("My Realms") || 
                               event.getView().getTitle().contains("own");
            int currentPage = plugin.getPlayerStateManager().getRealmsListPage(player, ownRealms);
            if (currentPage > 1) {
                openRealmsList(player, ownRealms, currentPage - 1);
            }
        } else if (displayName.contains("Next")) {
            // Get current page and realm type from player state
            boolean ownRealms = event.getView().getTitle().contains("My Realms") || 
                               event.getView().getTitle().contains("own");
            int currentPage = plugin.getPlayerStateManager().getRealmsListPage(player, ownRealms);
            // Calculate max pages to determine if we can go to next page
            // For now, just increment the page number (proper implementation would check realm count)
            openRealmsList(player, ownRealms, currentPage + 1);
        }
    }
    
    private void handleRealmManagementClick(Player player, InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        
        String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
        String realmName = extractRealmName(event.getView().getTitle());
        
        switch (displayName) {
            case "Teleport":
                plugin.getWorldManager().teleportToRealm(player, realmName);
                player.closeInventory();
                break;
            case "Manage Players":
                openRealmPlayers(player, realmName);
                break;
            case "Realm Settings":
                openRealmSettings(player, realmName);
                break;
            case "Delete Realm":
                player.closeInventory();
                Component message = miniMessage.deserialize("<red>To delete this realm, use: /realms delete " + realmName + "</red>");
                player.sendMessage(message);
                break;
            case "Back":
            case "Close":
                boolean ownRealms = plugin.getWorldDataManager()
                    .getPlayerRealms(player.getUniqueId())
                    .stream()
                    .anyMatch(r -> r.getName().equals(realmName));
                openRealmsList(player, ownRealms);
                break;
        }
    }
    
    private void handleRealmSettingsClick(Player player, InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        
        String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
        String realmName = extractRealmName(event.getView().getTitle());
        
        switch (displayName) {
            case "Player Limit":
                handlePlayerLimitClick(player, event, realmName);
                break;
            case "Set Spawn Point":
                player.closeInventory();
                // Set spawn point to player's current location in the realm
                org.bukkit.World world = Bukkit.getWorld(realmName);
                if (world != null) {
                    world.setSpawnLocation(player.getLocation());
                    Component message = miniMessage.deserialize("<green>Spawn point set for realm " + realmName + "</green>");
                    player.sendMessage(message);
                } else {
                    Component message = miniMessage.deserialize("<red>Realm is not loaded. Please teleport there first.</red>");
                    player.sendMessage(message);
                }
                break;
            case "Back":
            case "Close":
                openRealmManagement(player, realmName);
                break;
        }
    }
    
    private void handlePlayerLimitClick(Player player, InventoryClickEvent event, String realmName) {
        Realm realm = plugin.getWorldDataManager().getRealm(realmName);
        if (realm == null) return;
        
        int currentLimit = realm.getMaxPlayers();
        int newLimit = currentLimit;
        
        // Check click type to modify limit
        if (event.isLeftClick()) {
            if (event.isShiftClick()) {
                newLimit += 5; // Shift + left click increases by 5
            } else {
                newLimit += 1; // Left click increases by 1
            }
        } else if (event.isRightClick()) {
            if (event.isShiftClick()) {
                newLimit -= 5; // Shift + right click decreases by 5
            } else {
                newLimit -= 1; // Right click decreases by 1
            }
        }
        
        // Limit min/max values
        newLimit = Math.max(1, newLimit); // Minimum of 1 player
        newLimit = Math.min(100, newLimit); // Maximum of 100 players (or whatever you set as max)
        
        // Update realm
        realm.setMaxPlayers(newLimit);
        plugin.getWorldDataManager().saveData();
        
        // Update the menu item to reflect the new value
        Inventory inv = event.getInventory();
        ItemStack playerLimit = createItem(Material.CLOCK, "Player Limit", 
            "Current: " + newLimit + " players\n" +
            "Left-click: Increase by 1\n" +
            "Right-click: Decrease by 1\n" +
            "Shift+Click: +/-5");
        inv.setItem(11, playerLimit);
        
        Component message = miniMessage.deserialize("<green>Player limit for " + realmName + " set to " + newLimit + "</green>");
        player.sendMessage(message);
    }
    
    private void handleRealmPlayersClick(Player player, InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        
        String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
        String realmName = extractRealmNameFromPlayerMenu(event.getView().getTitle());
        
        if (displayName.contains("Back") || displayName.contains("Close")) {
            openRealmManagement(player, realmName);
        } else if (displayName.contains("Invite Player")) {
            player.closeInventory();
            Component message = miniMessage.deserialize("<yellow>To invite a player, use: /realms invite " + realmName + " <player></yellow>");
            player.sendMessage(message);
        } else if (displayName.contains("Previous")) {
            int currentPage = plugin.getPlayerStateManager().getRealmPlayersPage(player, realmName);
            if (currentPage > 1) {
                openRealmPlayers(player, realmName, currentPage - 1);
            }
        } else if (displayName.contains("Next")) {
            int currentPage = plugin.getPlayerStateManager().getRealmPlayersPage(player, realmName);
            // For now, just increment the page number (proper implementation would check player count)
            openRealmPlayers(player, realmName, currentPage + 1);
        }
    }
    
    private void handleRealmCreationClick(Player player, InventoryClickEvent event) {
        // This menu auto-closes after 3 seconds, so no interaction needed
        player.closeInventory();
    }
    
    private void handleBorderColorClick(Player player, InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        
        String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
        
        switch (displayName) {
            case "Blue Border":
                // Set border color to blue
                com.minekarta.advancedcorerealms.api.AdvancedCorePlayer advancedCorePlayer = 
                    plugin.getAdvancedCorePlayer(player);
                
                // Call the event
                com.minekarta.advancedcorerealms.worldborder.PlayerChangeBorderColorEvent colorEvent = 
                    new com.minekarta.advancedcorerealms.worldborder.PlayerChangeBorderColorEvent(
                        player, com.minekarta.advancedcorerealms.worldborder.BorderColor.BLUE);
                Bukkit.getPluginManager().callEvent(colorEvent);
                
                if (!colorEvent.isCancelled()) {
                    advancedCorePlayer.setBorderColor(com.minekarta.advancedcorerealms.worldborder.BorderColor.BLUE);
                    player.sendMessage(ColorUtils.processColors("&aBorder color set to blue!"));
                    player.closeInventory();
                }
                break;
            case "Green Border":
                // Set border color to green
                advancedCorePlayer = plugin.getAdvancedCorePlayer(player);
                
                // Call the event
                colorEvent = new com.minekarta.advancedcorerealms.worldborder.PlayerChangeBorderColorEvent(
                    player, com.minekarta.advancedcorerealms.worldborder.BorderColor.GREEN);
                Bukkit.getPluginManager().callEvent(colorEvent);
                
                if (!colorEvent.isCancelled()) {
                    advancedCorePlayer.setBorderColor(com.minekarta.advancedcorerealms.worldborder.BorderColor.GREEN);
                    player.sendMessage(ColorUtils.processColors("&aBorder color set to green!"));
                    player.closeInventory();
                }
                break;
            case "Red Border":
                // Set border color to red
                advancedCorePlayer = plugin.getAdvancedCorePlayer(player);
                
                // Call the event
                colorEvent = new com.minekarta.advancedcorerealms.worldborder.PlayerChangeBorderColorEvent(
                    player, com.minekarta.advancedcorerealms.worldborder.BorderColor.RED);
                Bukkit.getPluginManager().callEvent(colorEvent);
                
                if (!colorEvent.isCancelled()) {
                    advancedCorePlayer.setBorderColor(com.minekarta.advancedcorerealms.worldborder.BorderColor.RED);
                    player.sendMessage(ColorUtils.processColors("&aBorder color set to red!"));
                    player.closeInventory();
                }
                break;
            case "Back":
            case "Close":
                openMainMenu(player);
                break;
        }
    }
    
    private void handleUpgradeClick(Player player, InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        
        String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
        
        if (displayName.contains("Back") || displayName.contains("Close")) {
            openMainMenu(player);
            return;
        }
        
        // Check if this is an upgrade item (doesn't start with "Back")
        if (event.getSlot() >= 9) { // Upgrade items start from slot 9
            // Extract upgrade name from display name (remove the level part)
            String upgradeName = displayName.split(" \\(")[0]; // Get text before " (Level"
            
            // Find the corresponding upgrade
            com.minekarta.advancedcorerealms.upgrades.RealmUpgrade upgrade = null;
            for (com.minekarta.advancedcorerealms.upgrades.RealmUpgrade u : plugin.getUpgradeManager().getUpgrades()) {
                if (u.getName().equals(upgradeName)) {
                    upgrade = u;
                    break;
                }
            }
            
            if (upgrade != null) {
                // Get the player's realm
                com.minekarta.advancedcorerealms.data.object.Realm currentRealm = 
                    plugin.getWorldDataManager().getPlayerRealms(player.getUniqueId()).stream()
                        .filter(r -> r.getOwner().equals(player.getUniqueId()))
                        .findFirst()
                        .orElse(null);
                        
                if (currentRealm != null) {
                    // Check if upgrade is at max level
                    int currentLevel = upgrade.getLevel(currentRealm);
                    if (currentLevel >= upgrade.getMaxLevel()) {
                        player.sendMessage(ChatColor.RED + "This upgrade is already at maximum level!");
                        return;
                    }
                    
                    // Check if player has enough money
                    double cost = upgrade.getCost(currentLevel);
                    if (!plugin.getUpgradeManager().hasEnoughMoney(player, cost)) {
                        player.sendMessage(ChatColor.RED + "You don't have enough money! Need: $" + String.format("%.2f", cost));
                        return;
                    }
                    
                    // Perform the upgrade
                    boolean success = plugin.getUpgradeManager().upgradeRealm(currentRealm, upgrade.getId(), player);
                    if (success) {
                        player.sendMessage(ChatColor.GREEN + "Successfully upgraded " + upgrade.getName() + " to level " + (currentLevel + 1) + "!");
                        
                        // Reopen the menu to show updated levels
                        openUpgradeMenu(player);
                    } else {
                        player.sendMessage(ChatColor.RED + "Failed to upgrade " + upgrade.getName() + ".");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You don't own any realm to upgrade!");
                }
            }
        }
    }
    
    private String extractRealmName(String title) {
        if (title.startsWith("Realms | Realm: ")) {
            return title.substring("Realms | Realm: ".length());
        } else if (title.startsWith("Realms | Settings: ")) {
            return title.substring("Realms | Settings: ".length());
        }
        return null;
    }
    
    private String extractRealmNameFromPlayerMenu(String title) {
        if (title.startsWith("Realms | Players: ")) {
            // Extract realm name from "Realms | Players: <realmName>"
            String realmPart = title.substring("Realms | Players: ".length());
            // Remove page info if present
            if (realmPart.contains(" (Page")) {
                return realmPart.substring(0, realmPart.indexOf(" (Page"));
            }
            return realmPart;
        }
        return null;
    }
}