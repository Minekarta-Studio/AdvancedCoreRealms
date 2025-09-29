package com.minekarta.advancedcorerealms.menu.menus;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.menu.Menu;
import com.minekarta.advancedcorerealms.realm.Role;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;
import java.util.stream.Collectors;

public class ManageMembersMenu extends Menu {

    private final Realm realm;

    public ManageMembersMenu(AdvancedCoreRealms plugin, Player player, Realm realm) {
        super(plugin, player, "Manage Members: " + realm.getName(), 54);
        this.realm = realm;
        setMenuItems();
    }

    private void setMenuItems() {
        // Add member heads
        int slot = 0;
        for (UUID memberUuid : realm.getMembers().keySet()) {
            if (slot >= 45) break; // Max 45 members displayed

            OfflinePlayer member = Bukkit.getOfflinePlayer(memberUuid);
            Role role = realm.getRole(memberUuid);

            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta meta = (SkullMeta) playerHead.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(member);
                meta.setDisplayName(member.getName());
                meta.setLore(java.util.Arrays.asList(
                        "Role: " + role.name(),
                        "Click to promote/demote.",
                        "Shift-click to kick."
                ));
                playerHead.setItemMeta(meta);
            }

            inventory.setItem(slot++, playerHead);
        }

        // Add control buttons
        inventory.setItem(48, createGuiItem(Material.GREEN_WOOL, "Invite Player", "Click to invite a new player."));
        inventory.setItem(49, createGuiItem(Material.REDSTONE_BLOCK, "Transfer Ownership", "Click to transfer ownership."));
        inventory.setItem(50, createGuiItem(Material.BARRIER, "Back", "Return to the previous menu."));
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        if (e.getCurrentItem() == null) return;

        int slot = e.getSlot();
        Player player = (Player) e.getWhoClicked();

        if (slot < 45 && e.getCurrentItem().getType() == Material.PLAYER_HEAD) {
            // Member management
            SkullMeta meta = (SkullMeta) e.getCurrentItem().getItemMeta();
            if (meta == null || meta.getOwningPlayer() == null) return;

            UUID targetUuid = meta.getOwningPlayer().getUniqueId();
            Role targetRole = realm.getRole(targetUuid);
            Role actorRole = realm.getRole(player.getUniqueId());

            if (e.isShiftClick()) {
                // Kick player
                if (actorRole == Role.OWNER || (actorRole == Role.ADMIN && targetRole != Role.ADMIN && targetRole != Role.OWNER)) {
                    realm.removeMember(targetUuid);
                    player.sendMessage("Kicked " + meta.getOwningPlayer().getName());
                    setMenuItems(); // Refresh menu
                } else {
                    player.sendMessage("You don't have permission to kick this player.");
                }
            } else {
                // Promote/demote
                if (actorRole == Role.OWNER || (actorRole == Role.ADMIN && targetRole == Role.MEMBER)) {
                    Role newRole = (targetRole == Role.MEMBER) ? Role.ADMIN : Role.MEMBER;
                    realm.addMember(targetUuid, newRole);
                    player.sendMessage("Set " + meta.getOwningPlayer().getName() + "'s role to " + newRole.name());
                    setMenuItems(); // Refresh menu
                } else {
                    player.sendMessage("You don't have permission to change this player's role.");
                }
            }
        } else if (slot == 48) {
            // Invite player
            player.closeInventory();
            player.sendMessage("Please type the name of the player you want to invite.");
            // Here you would typically register a chat listener for the next message
        } else if (slot == 49) {
            // Transfer ownership
            player.closeInventory();
            player.sendMessage("Ownership transfer needs a confirmation GUI. Not yet implemented.");
        } else if (slot == 50) {
            // Back button
            // This should open the previous menu, e.g., RealmManagementMenu
            player.closeInventory();
        }
    }
}