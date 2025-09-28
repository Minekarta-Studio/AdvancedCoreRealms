package com.minekarta.advancedcorerealms.menu.menus;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.menu.Menu;
import com.minekarta.advancedcorerealms.menu.MenuManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class RealmPlayersMenu extends Menu {

    private final FileConfiguration menuConfig;
    private final MenuManager menuManager;
    private final String realmName;
    private final int page;
    private final List<UUID> allPlayers;

    public RealmPlayersMenu(AdvancedCoreRealms plugin, Player player, FileConfiguration menuConfig, MenuManager menuManager, String realmName, int page) {
        super(plugin, player, menuConfig.getString("realm_players.title", "Realm Players").replace("[name]", realmName), menuConfig.getInt("realm_players.size", 54));
        this.menuConfig = menuConfig;
        this.menuManager = menuManager;
        this.realmName = realmName;
        this.page = page;

        Realm realm = plugin.getWorldDataManager().getRealm(realmName);
        this.allPlayers = new ArrayList<>();
        if (realm != null) {
            allPlayers.add(realm.getOwner());
            allPlayers.addAll(realm.getMembers());
        }

        setMenuItems();
    }

    private void setMenuItems() {
        ConfigurationSection elements = menuConfig.getConfigurationSection("realm_players.elements");
        if (elements == null) return;

        // Set static items like back button, next/prev page buttons
        for (String key : elements.getKeys(false)) {
            ConfigurationSection itemConfig = elements.getConfigurationSection(key);
            if (itemConfig == null || "glass_panes".equals(key)) continue;

            int itemSlot = itemConfig.getInt("slot", -1);
            if (itemSlot == -1) continue;

            Material material = Material.matchMaterial(itemConfig.getString("material"));
            if (material == null) material = Material.STONE;
            String name = itemConfig.getString("name");
            List<String> lore = itemConfig.getStringList("lore");
            inventory.setItem(itemSlot, createGuiItem(material, name, lore.toArray(new String[0])));
        }

        // Add player heads
        int itemsPerPage = 36; // slots 0-35 for players
        int startIndex = (page - 1) * itemsPerPage;
        List<UUID> pagePlayers = allPlayers.stream().distinct().skip(startIndex).limit(itemsPerPage).collect(Collectors.toList());

        int slot = 0;
        Realm realm = plugin.getWorldDataManager().getRealm(realmName);
        for (UUID playerId : pagePlayers) {
            while (inventory.getItem(slot) != null && slot < 45) { // Only fill up to slot 44
                slot++;
            }
            if (slot >= 45) break;

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
            boolean isOwner = realm.getOwner().equals(playerId);

            ItemStack playerHead = createPlayerHead(offlinePlayer, isOwner);
            inventory.setItem(slot, playerHead);
        }


        // Fill remaining slots
        ConfigurationSection glassPaneConfig = elements.getConfigurationSection("glass_panes");
        if (glassPaneConfig != null && glassPaneConfig.getBoolean("fill_remaining", false)) {
            Material material = Material.matchMaterial(glassPaneConfig.getString("material", "BLACK_STAINED_GLASS_PANE"));
            if (material == null) material = Material.BLACK_STAINED_GLASS_PANE;
            String name = glassPaneConfig.getString("name", " ");
            fillWith(createGuiItem(material, name));
        }
    }

    private ItemStack createPlayerHead(OfflinePlayer p, boolean isOwner) {
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
        if (skullMeta == null) return playerHead;

        skullMeta.setOwningPlayer(p);

        String name = "<gold>" + p.getName() + "</gold>";
        String role = isOwner ? "<gray>Role: <red>Owner" : "<gray>Role: <aqua>Member";
        String onlineStatus = p.isOnline() ? "<gray>Status: <green>Online" : "<gray>Status: <red>Offline";

        skullMeta.displayName(miniMessage.deserialize(name));

        List<Component> lore = new ArrayList<>();
        lore.add(miniMessage.deserialize(role));
        lore.add(miniMessage.deserialize(onlineStatus));
        if (!isOwner) {
            lore.add(Component.text(""));
            lore.add(miniMessage.deserialize("<red>Click to kick player"));
        }
        skullMeta.lore(lore);

        playerHead.setItemMeta(skullMeta);
        return playerHead;
    }


    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir() || clickedItem.getItemMeta() == null) return;

        String displayName = PlainTextComponentSerializer.plainText().serialize(clickedItem.getItemMeta().displayName());

        ConfigurationSection elements = menuConfig.getConfigurationSection("realm_players.elements");
        if (elements == null) return;

        String backButtonName = PlainTextComponentSerializer.plainText().serialize(miniMessage.deserialize(elements.getString("back_button.name", "Back")));
        String prevPageName = PlainTextComponentSerializer.plainText().serialize(miniMessage.deserialize(elements.getString("previous_page.name", "Previous Page")));
        String nextPageName = PlainTextComponentSerializer.plainText().serialize(miniMessage.deserialize(elements.getString("next_page.name", "Next Page")));
        String invitePlayerName = PlainTextComponentSerializer.plainText().serialize(miniMessage.deserialize(elements.getString("invite_player.name", "Invite Player")));

        if (displayName.equalsIgnoreCase(backButtonName)) {
            menuManager.openRealmManagementMenu(player, realmName);
        } else if (displayName.equalsIgnoreCase(prevPageName)) {
            if (page > 1) {
                menuManager.openRealmPlayersMenu(player, realmName, page - 1);
            }
        } else if (displayName.equalsIgnoreCase(nextPageName)) {
            int itemsPerPage = 36;
            int totalPages = (int) Math.ceil((double) allPlayers.stream().distinct().count() / itemsPerPage);
            if (page < totalPages) {
                menuManager.openRealmPlayersMenu(player, realmName, page + 1);
            }
        } else if (displayName.equalsIgnoreCase(invitePlayerName)) {
            player.closeInventory();
            player.sendMessage("<yellow>To invite a player, use: /realms invite " + realmName + " <player>");
        } else if (clickedItem.getType() == Material.PLAYER_HEAD) {
            // Handle kicking a player
            SkullMeta meta = (SkullMeta) clickedItem.getItemMeta();
            OfflinePlayer target = meta.getOwningPlayer();
            if (target != null) {
                Realm realm = plugin.getWorldDataManager().getRealm(realmName);
                if (realm != null && realm.getOwner().equals(player.getUniqueId()) && !realm.getOwner().equals(target.getUniqueId())) {
                    realm.getMembers().remove(target.getUniqueId());
                    plugin.getWorldDataManager().saveData();
                    player.sendMessage("<green>Kicked " + target.getName() + " from the realm.");
                    // Refresh the menu
                    menuManager.openRealmPlayersMenu(player, realmName, page);
                } else {
                    player.sendMessage("<red>You can only kick members from your own realm.");
                }
            }
        }
    }
}