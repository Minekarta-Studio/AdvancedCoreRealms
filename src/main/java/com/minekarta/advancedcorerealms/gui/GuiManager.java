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
        String title = ownRealms ? "My Realms" : "Accessible Realms";
        Inventory inventory = Bukkit.createInventory(null, 54, title);
        
        // Fill with black glass panes
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, createGlassPane());
        }
        
        // Info Item (Slot 4)
        String infoText = ownRealms ? 
            "These are the Realms you own" : 
            "These are the Realms you can access";
        ItemStack infoItem = createBookItem("Realms Info", infoText);
        inventory.setItem(4, infoItem);
        
        // Get realms based on type
        List<Realm> realms = ownRealms ? 
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
            
            Realm realm = realms.get(i);
            Material worldMaterial = realm.isFlat() ? Material.GRASS_BLOCK : Material.STONE;
            
            String realmName = realm.getName();
            String worldStatus = realm.getBukkitWorld() != null ? "Loaded" : "Unloaded";
            int playerCount = realm.getBukkitWorld() != null ? realm.getBukkitWorld().getPlayers().size() : 0;
            
            ItemStack realmItem = createItem(worldMaterial, realmName, 
                "Players: " + playerCount + "\nStatus: " + worldStatus);
            inventory.setItem(slot, realmItem);
            slot++;
        }
        
        // Back button (Slot 49)
        ItemStack backItem = createItem(Material.BARRIER, "Back", "Return to main menu");
        inventory.setItem(49, backItem);
        
        // Pagination buttons (Slots 45 and 53)
        if (page > 1) {
            ItemStack prevPage = createItem(Material.ARROW, "Previous Page", "Go to page " + (page - 1));
            inventory.setItem(45, prevPage);
        }
        
        if (page < totalPages) {
            ItemStack nextPage = createItem(Material.ARROW, "Next Page", "Go to page " + (page + 1));
            inventory.setItem(53, nextPage);
        }
        
        player.openInventory(inventory);
    }
    
    // Realm Management Menu
    public void openRealmManagement(Player player, String realmName) {
        Inventory inventory = Bukkit.createInventory(null, 36, "Realms | Realm: " + realmName);
        
        // Fill with black glass panes
        for (int i = 0; i < 36; i++) {
            inventory.setItem(i, createGlassPane());
        }
        
        // Info Item (Slot 4)
        Realm realm = plugin.getWorldDataManager().getRealm(realmName);
        if (realm == null) {
            player.sendMessage(miniMessage.deserialize("<red>Realm not found!</red>"));
            return;
        }
        
        String worldStatus = realm.getBukkitWorld() != null ? "Loaded" : "Unloaded";
        int playerCount = realm.getBukkitWorld() != null ? realm.getBukkitWorld().getPlayers().size() : 0;
        
        ItemStack infoItem = createBookItem("Realm Info", 
            "Name: " + realm.getName() + "\n" +
            "Type: " + realm.getWorldType() + "\n" +
            "Status: " + worldStatus + "\n" +
            "Players: " + playerCount + "\n" +
            "Owner: " + (getOwnerName(realm.getOwner()) != null ? getOwnerName(realm.getOwner()) : "Unknown"));
        inventory.setItem(4, infoItem);
        
        // Teleport (Slot 10)
        ItemStack teleport = createItem(Material.ENDER_PEARL, "Teleport", "Teleport to this Realm");
        inventory.setItem(10, teleport);
        
        // Manage Players (Slot 12) - Only for owner
        if (realm.getOwner().equals(player.getUniqueId())) {
            ItemStack managePlayers = createPlayerHead(player, "Manage Players", 
                "Manage access for this Realm");
            inventory.setItem(12, managePlayers);
        }
        
        // Realm Settings (Slot 14) - Only for owner
        if (realm.getOwner().equals(player.getUniqueId())) {
            ItemStack settings = createItem(Material.COMPARATOR, "Realm Settings", 
                "Change settings for this Realm");
            inventory.setItem(14, settings);
        }
        
        // Delete Realm (Slot 16) - Only for owner
        if (realm.getOwner().equals(player.getUniqueId())) {
            ItemStack delete = createItem(Material.TNT, "Delete Realm", 
                "Delete this Realm (Cannot be undone!)");
            inventory.setItem(16, delete);
        }
        
        // Back Button (Slot 31)
        ItemStack backItem = createItem(Material.BARRIER, "Back", "Return to Realms list");
        inventory.setItem(31, backItem);
        
        player.openInventory(inventory);
    }
    
    // Realm Settings Menu
    public void openRealmSettings(Player player, String realmName) {
        Inventory inventory = Bukkit.createInventory(null, 36, "Realms | Settings: " + realmName);
        
        // Fill with black glass panes
        for (int i = 0; i < 36; i++) {
            inventory.setItem(i, createGlassPane());
        }
        
        // Info Item (Slot 4)
        ItemStack infoItem = createBookItem("Settings Info", 
            "Realm settings for: " + realmName);
        inventory.setItem(4, infoItem);
        
        // Player Limit (Slot 11)
        Realm realm = plugin.getWorldDataManager().getRealm(realmName);
        if (realm != null) {
            int currentLimit = realm.getMaxPlayers();
            ItemStack playerLimit = createItem(Material.CLOCK, "Player Limit", 
                "Current: " + currentLimit + " players\n" +
                "Left-click: Increase by 1\n" +
                "Right-click: Decrease by 1\n" +
                "Shift+Click: +/-5");
            inventory.setItem(11, playerLimit);
        }
        
        // Set Spawn Point (Slot 15)
        ItemStack setSpawn = createItem(Material.COMPASS, "Set Spawn Point", 
            "Set the spawn point for this Realm");
        inventory.setItem(15, setSpawn);
        
        // Back Button (Slot 31)
        ItemStack backItem = createItem(Material.BARRIER, "Back", "Return to Realm Management");
        inventory.setItem(31, backItem);
        
        player.openInventory(inventory);
    }
    
    // Realm Players Menu
    public void openRealmPlayers(Player player, String realmName) {
        openRealmPlayers(player, realmName, 1);
    }
    
    public void openRealmPlayers(Player player, String realmName, int page) {
        Inventory inventory = Bukkit.createInventory(null, 54, "Realms | Players: " + realmName + " (Page " + page + "/" + 1 + ")");
        
        // Fill with black glass panes
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, createGlassPane());
        }
        
        // Info Item (Slot 4)
        ItemStack infoItem = createBookItem("Player Management", 
            "Manage players for realm: " + realmName);
        inventory.setItem(4, infoItem);
        
        // Get realm and players
        Realm realm = plugin.getWorldDataManager().getRealm(realmName);
        if (realm == null) {
            player.sendMessage(miniMessage.deserialize("<red>Realm not found!</red>"));
            return;
        }
        
        List<UUID> allPlayers = new ArrayList<>();
        allPlayers.add(realm.getOwner()); // Add owner first
        for (UUID member : realm.getMembers()) {
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
            if (slot == 45) { // Skip back button slot
                slot++;
            }
            if (slot > 44) break; // Don't exceed player slots
            
            UUID playerId = allPlayers.get(i);
            boolean isOwner = playerId.equals(realm.getOwner());
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
            
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
        
        // Invite Player (Slot 49)
        ItemStack invitePlayer = createItem(Material.WRITABLE_BOOK, "Invite Player", 
            "Invite a player to this Realm");
        inventory.setItem(49, invitePlayer);
        
        // Back Button (Slot 45)
        ItemStack backItem = createItem(Material.BARRIER, "Back", "Return to Realm Management");
        inventory.setItem(45, backItem);
        
        // Pagination buttons for future use
        // Currently only showing one page
        
        player.openInventory(inventory);
    }
    
    // Realm Creation Menu
    public void openRealmCreation(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, "Realms | Create Realm");
        
        // Fill with black glass panes
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, createGlassPane());
        }
        
        // Info Item (Slot 13)
        ItemStack infoItem = createBookItem("Create Realm", 
            "To create a realm:\n" +
            "1. Close this menu\n" +
            "2. Type /realms create <name> [type]\n" +
            "In chat with the name you want.\n\n" +
            "Valid types: FLAT, NORMAL, AMPLIFIED\n" +
            "Names can only contain letters, numbers, and underscores.");
        inventory.setItem(13, infoItem);
        
        player.openInventory(inventory);
        
        // Auto-close after 3 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.getOpenInventory().getTitle().equals("Realms | Create Realm")) {
                player.closeInventory();
            }
        }, 60L); // 60 ticks = 3 seconds
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
    
    // Handle inventory clicks
    public void handleInventoryClick(InventoryClickEvent event, String inventoryTitle) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }
        
        String title = event.getView().getTitle();
        
        if (title.equals("AdvancedCoreRealms")) {
            handleMainMenuClick(player, event);
        } else if (title.equals("My Realms") || title.equals("Accessible Realms")) {
            handleRealmsListClick(player, event);
        } else if (title.startsWith("Realms | Realm:")) {
            handleRealmManagementClick(player, event);
        } else if (title.startsWith("Realms | Settings:")) {
            handleRealmSettingsClick(player, event);
        } else if (title.startsWith("Realms | Players:")) {
            handleRealmPlayersClick(player, event);
        } else if (title.equals("Realms | Create Realm")) {
            handleRealmCreationClick(player, event);
        }
    }
    
    private void handleMainMenuClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
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
            case "Back":
                openMainMenu(player);
                break;
        }
    }
    
    private void handleRealmsListClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        
        String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
        
        if (event.getSlot() >= 9 && event.getSlot() <= 53) {
            // Realm item clicked
            if (clickedItem.getType() == Material.GRASS_BLOCK || clickedItem.getType() == Material.STONE) {
                String realmName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
                openRealmManagement(player, realmName);
            }
        } else if (displayName.equals("Back")) {
            openMainMenu(player);
        } else if (displayName.equals("Previous Page")) {
            // Handle pagination - need to extract current page and realm type from state
            // For now, just reopen with page - 1
            boolean ownRealms = event.getView().getTitle().equals("My Realms");
            // This would need to track the current page in a map or similar
            // For now we'll implement a basic version
        } else if (displayName.equals("Next Page")) {
            // Handle pagination - need to extract current page and realm type from state
            // For now, just reopen with page + 1
            boolean ownRealms = event.getView().getTitle().equals("My Realms");
            // This would need to track the current page in a map or similar
        }
    }
    
    private void handleRealmManagementClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
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
                boolean ownRealms = plugin.getWorldDataManager()
                    .getPlayerRealms(player.getUniqueId())
                    .stream()
                    .anyMatch(r -> r.getName().equals(realmName));
                openRealmsList(player, ownRealms);
                break;
        }
    }
    
    private void handleRealmSettingsClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
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
        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        
        String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
        String realmName = extractRealmNameFromPlayerMenu(event.getView().getTitle());
        
        if (displayName.equals("Back")) {
            openRealmManagement(player, realmName);
        } else if (displayName.equals("Invite Player")) {
            player.closeInventory();
            Component message = miniMessage.deserialize("<yellow>To invite a player, use: /realms invite " + realmName + " <player></yellow>");
            player.sendMessage(message);
        }
    }
    
    private void handleRealmCreationClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        // This menu auto-closes after 3 seconds, so no interaction needed
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