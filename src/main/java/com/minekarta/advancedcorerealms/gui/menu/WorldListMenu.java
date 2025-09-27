package com.minekarta.advancedcorerealms.gui.menu;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
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

public class WorldListMenu {
    
    private final AdvancedCoreRealms plugin;
    
    public WorldListMenu(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
    }
    
    public void openWorldListMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.GOLD + "All Realms");
        
        List<Realm> allRealms = plugin.getWorldDataManager().getAllRealms();
        int slot = 0;
        
        for (Realm realm : allRealms) {
            // Only show realms the player has access to
            if (!realm.isMember(player.getUniqueId())) {
                continue;
            }
            
            World world = Bukkit.getWorld(realm.getName());
            ItemStack item = new ItemStack(realm.isFlat() ? Material.GRASS_BLOCK : Material.SHORT_GRASS);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + realm.getName());
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Type: " + (realm.isFlat() ? "Flat" : "Normal"));
            if (realm.getOwner().equals(player.getUniqueId())) {
                lore.add(ChatColor.GRAY + "Owner: You");
            } else {
                lore.add(ChatColor.GRAY + "Owner: " + Bukkit.getOfflinePlayer(realm.getOwner()).getName());
            }
            lore.add("");
            
            if (realm.isMember(player.getUniqueId()) && !realm.getOwner().equals(player.getUniqueId())) {
                lore.add(ChatColor.LIGHT_PURPLE + "Member");
            }
            
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
        
        // Add navigation or info items at the end
        if (slot < inventory.getSize() - 1) {
            ItemStack infoItem = new ItemStack(Material.PAPER);
            ItemMeta infoMeta = infoItem.getItemMeta();
            infoMeta.setDisplayName(ChatColor.WHITE + "Information");
            List<String> infoLore = new ArrayList<>();
            infoLore.add(ChatColor.GRAY + "This menu shows all realms");
            infoLore.add(ChatColor.GRAY + "you have access to.");
            infoLore.add("");
            infoLore.add(ChatColor.YELLOW + "Green: Loaded worlds");
            infoLore.add(ChatColor.RED + "Red: Unloaded worlds");
            infoMeta.setLore(infoLore);
            infoItem.setItemMeta(infoMeta);
            inventory.setItem(inventory.getSize() - 1, infoItem);
        }
        
        player.openInventory(inventory);
    }
}