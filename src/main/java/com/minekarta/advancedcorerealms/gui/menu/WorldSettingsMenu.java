package com.minekarta.advancedcorerealms.gui.menu;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WorldSettingsMenu {
    
    private final AdvancedCoreRealms plugin;
    private final String realmName;
    
    public WorldSettingsMenu(AdvancedCoreRealms plugin, String realmName) {
        this.plugin = plugin;
        this.realmName = realmName;
    }
    
    public void openWorldSettingsMenu(Player player) {
        Realm realm = plugin.getWorldDataManager().getRealm(realmName);
        if (realm == null) {
            player.sendMessage(ChatColor.RED + "Realm does not exist!");
            return;
        }
        
        // Check if player is the owner
        if (!realm.getOwner().equals(player.getUniqueId()) && !player.hasPermission("advancedcorerealms.admin.*")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to access this realm's settings!");
            return;
        }
        
        Inventory inventory = Bukkit.createInventory(null, 27, ChatColor.GOLD + realmName + " Settings");
        
        // Add realm info
        ItemStack infoItem = new ItemStack(Material.NAME_TAG);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName(ChatColor.AQUA + "Realm Information");
        
        List<String> infoLore = new ArrayList<>();
        infoLore.add(ChatColor.GRAY + "Name: " + realm.getName());
        infoLore.add(ChatColor.GRAY + "Type: " + (realm.isFlat() ? "Flat" : "Normal"));
        infoLore.add(ChatColor.GRAY + "Owner: " + Bukkit.getOfflinePlayer(realm.getOwner()).getName());
        infoLore.add(ChatColor.GRAY + "Creation Time: " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new java.util.Date(realm.getCreationTime())));
        infoLore.add("");
        infoLore.add(ChatColor.YELLOW + "Click to view realm info");
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        
        inventory.setItem(0, infoItem);
        
        // Invite members
        ItemStack inviteItem = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta inviteMeta = inviteItem.getItemMeta();
        inviteMeta.setDisplayName(ChatColor.GREEN + "Invite Members");
        
        List<String> inviteLore = new ArrayList<>();
        inviteLore.add(ChatColor.GRAY + "Invite players to this realm");
        inviteLore.add(ChatColor.GRAY + "Members will be able to teleport here");
        inviteLore.add("");
        inviteLore.add(ChatColor.YELLOW + "Click to open invite menu");
        inviteMeta.setLore(inviteLore);
        inviteItem.setItemMeta(inviteMeta);
        
        inventory.setItem(2, inviteItem);
        
        // Manage members
        ItemStack membersItem = new ItemStack(Material.BOOK);
        ItemMeta membersMeta = membersItem.getItemMeta();
        membersMeta.setDisplayName(ChatColor.YELLOW + "Manage Members");
        
        List<String> membersLore = new ArrayList<>();
        membersLore.add(ChatColor.GRAY + "Current members: " + realm.getMembers().size());
        for (UUID memberId : realm.getMembers()) {
            OfflinePlayer member = Bukkit.getOfflinePlayer(memberId);
            membersLore.add(ChatColor.GRAY + "- " + member.getName());
        }
        membersLore.add("");
        membersLore.add(ChatColor.YELLOW + "Click to manage members");
        membersMeta.setLore(membersLore);
        membersItem.setItemMeta(membersMeta);
        
        inventory.setItem(3, membersItem);
        
        // Realm actions
        ItemStack deleteItem = new ItemStack(Material.BARRIER);
        ItemMeta deleteMeta = deleteItem.getItemMeta();
        deleteMeta.setDisplayName(ChatColor.RED + "Delete Realm");
        
        List<String> deleteLore = new ArrayList<>();
        deleteLore.add(ChatColor.GRAY + "Permanently delete this realm");
        deleteLore.add(ChatColor.GRAY + "This action cannot be undone!");
        deleteLore.add("");
        deleteLore.add(ChatColor.RED + "Click to delete realm");
        deleteMeta.setLore(deleteLore);
        deleteItem.setItemMeta(deleteMeta);
        
        inventory.setItem(8, deleteItem);
        
        // Back button
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.WHITE + "Back to Main Menu");
        List<String> backLore = new ArrayList<>();
        backLore.add(ChatColor.GRAY + "Return to the main realms menu");
        backMeta.setLore(backLore);
        backItem.setItemMeta(backMeta);
        
        inventory.setItem(26, backItem);
        
        player.openInventory(inventory);
    }
}