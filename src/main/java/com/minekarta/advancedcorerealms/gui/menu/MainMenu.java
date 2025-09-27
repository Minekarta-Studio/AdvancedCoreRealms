package com.minekarta.advancedcorerealms.gui.menu;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.manager.WorldManager;
import com.minekarta.advancedcorerealms.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MainMenu {
    
    private final AdvancedCoreRealms plugin;
    private final WorldManager worldManager;
    
    public MainMenu(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.worldManager = plugin.getWorldManager();
    }
    
    public void openMainMenu(Player player) {
        int realmCount = plugin.getWorldDataManager().getPlayerRealms(player.getUniqueId()).size();
        int maxRealms = 1; // Default
        if (player.hasPermission("advancedcorerealms.limit.realms.3")) maxRealms = 3;
        if (player.hasPermission("advancedcorerealms.limit.realms.5")) maxRealms = 5;
        
        // Calculate needed inventory size (rows of 9 slots)
        int realmListSize = Math.max(9, (int) Math.ceil((realmCount + 3) / 9.0) * 9); // +3 for buttons
        Inventory inventory = Bukkit.createInventory(null, realmListSize, ChatColor.GOLD + "Your Realms");
        
        // Add player's realms
        List<Realm> playerRealms = plugin.getWorldDataManager().getPlayerRealms(player.getUniqueId());
        int slot = 0;
        for (Realm realm : playerRealms) {
            World world = Bukkit.getWorld(realm.getName());
            ItemStack item = new ItemStack(realm.isFlat() ? Material.GRASS_BLOCK : Material.SHORT_GRASS);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + realm.getName());
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Type: " + (realm.isFlat() ? "Flat" : "Normal"));
            lore.add(ChatColor.GRAY + "Owner: You");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click to teleport");
            
            if (world != null) {
                lore.add(ChatColor.GREEN + "Status: Loaded");
                lore.add(ChatColor.GRAY + "Players: " + world.getPlayers().size());
            } else {
                lore.add(ChatColor.RED + "Status: Unloaded");
            }
            
            meta.setLore(lore);
            item.setItemMeta(meta);
            
            inventory.setItem(slot, item);
            slot++;
        }
        
        // Add invited realms
        List<Realm> invitedRealms = plugin.getWorldDataManager().getPlayerInvitedRealms(player.getUniqueId());
        for (Realm realm : invitedRealms) {
            World world = Bukkit.getWorld(realm.getName());
            ItemStack item = new ItemStack(Material.BOOK);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + realm.getName() + " (Invited)");
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Type: " + (realm.isFlat() ? "Flat" : "Normal"));
            lore.add(ChatColor.GRAY + "Owner: " + Bukkit.getOfflinePlayer(realm.getOwner()).getName());
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click to teleport");
            
            if (world != null) {
                lore.add(ChatColor.GREEN + "Status: Loaded");
                lore.add(ChatColor.GRAY + "Players: " + world.getPlayers().size());
            } else {
                lore.add(ChatColor.RED + "Status: Unloaded");
            }
            
            meta.setLore(lore);
            item.setItemMeta(meta);
            
            if (slot < inventory.getSize()) {
                inventory.setItem(slot, item);
                slot++;
            }
        }
        
        // Add action buttons at the bottom
        addButtons(inventory, slot, player, maxRealms, realmCount);
        
        player.openInventory(inventory);
    }
    
    private void addButtons(Inventory inventory, int startSlot, Player player, int maxRealms, int currentCount) {
        // Create Realm button
        if (startSlot + 1 < inventory.getSize()) {
            ItemStack createItem = new ItemStack(currentCount >= maxRealms ? Material.GRAY_WOOL : Material.LIME_WOOL);
            ItemMeta createMeta = createItem.getItemMeta();
            createMeta.setDisplayName(ChatColor.GREEN + "Create New Realm");
            List<String> createLore = new ArrayList<>();
            createLore.add(ChatColor.GRAY + "Click to create a new private realm");
            createLore.add(ChatColor.GRAY + "Your limit: " + currentCount + "/" + maxRealms);
            if (currentCount >= maxRealms) {
                createLore.add(ChatColor.RED + "You've reached your realm limit!");
            }
            createMeta.setLore(createLore);
            createItem.setItemMeta(createMeta);
            inventory.setItem(startSlot, createItem);
        }
        
        // Refresh button
        if (startSlot + 2 < inventory.getSize()) {
            ItemStack refreshItem = new ItemStack(Material.COMPASS);
            ItemMeta refreshMeta = refreshItem.getItemMeta();
            refreshMeta.setDisplayName(ChatColor.AQUA + "Refresh");
            List<String> refreshLore = new ArrayList<>();
            refreshLore.add(ChatColor.GRAY + "Click to refresh the realm list");
            refreshMeta.setLore(refreshLore);
            refreshItem.setItemMeta(refreshMeta);
            inventory.setItem(startSlot + 1, refreshItem);
        }
        
        // Help button
        if (startSlot + 3 < inventory.getSize()) {
            ItemStack helpItem = new ItemStack(Material.KNOWLEDGE_BOOK);
            ItemMeta helpMeta = helpItem.getItemMeta();
            helpMeta.setDisplayName(ChatColor.YELLOW + "Help & Info");
            List<String> helpLore = new ArrayList<>();
            helpLore.add(ChatColor.GRAY + "Private worlds for your personal use");
            helpLore.add(ChatColor.GRAY + "Invite friends to join your realm");
            helpLore.add(ChatColor.GRAY + "Separate inventories per realm");
            helpMeta.setLore(helpLore);
            helpItem.setItemMeta(helpMeta);
            inventory.setItem(startSlot + 2, helpItem);
        }
    }
}