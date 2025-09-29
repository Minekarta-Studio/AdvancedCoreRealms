package com.minekarta.advancedcorerealms.menu.menus;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.menu.Menu;
import com.minekarta.advancedcorerealms.menu.MenuManager;
import net.kyori.adventure.text.Component;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class RealmPlayersMenu extends Menu {

    private final FileConfiguration menuConfig;
    private final MenuManager menuManager;
    private final String realmName;
    private final int page;
    private final boolean fromMyRealms;
    private final List<UUID> allPlayers;
    private final Map<Integer, String> slotActions = new HashMap<>();

    public RealmPlayersMenu(AdvancedCoreRealms plugin, Player player, FileConfiguration menuConfig, MenuManager menuManager, String realmName, int page, boolean fromMyRealms) {
        super(plugin, player, menuConfig.getString("realm_players.title", "Realm Players").replace("[name]", realmName), menuConfig.getInt("realm_players.size", 54));
        this.menuConfig = menuConfig;
        this.menuManager = menuManager;
        this.realmName = realmName;
        this.page = page;
        this.fromMyRealms = fromMyRealms;

        Realm realm = plugin.getWorldDataManager().getRealm(realmName);
        this.allPlayers = new ArrayList<>();
        if (realm != null) {
            allPlayers.add(realm.getOwner());
            allPlayers.addAll(realm.getMembers().keySet());
        }

        setMenuItems();
    }

    private void setMenuItems() {
        ConfigurationSection elements = menuConfig.getConfigurationSection("realm_players.elements");
        if (elements == null) return;

        // Set static items
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
            slotActions.put(itemSlot, key);
        }

        // Add player heads
        int itemsPerPage = 45; // Max player heads
        int startIndex = (page - 1) * itemsPerPage;
        List<UUID> pagePlayers = allPlayers.stream().distinct().skip(startIndex).limit(itemsPerPage).collect(Collectors.toList());

        int slot = 0;
        Realm realm = plugin.getWorldDataManager().getRealm(realmName);
        for (UUID playerId : pagePlayers) {
            while (inventory.getItem(slot) != null && slot < itemsPerPage) {
                slot++;
            }
            if (slot >= itemsPerPage) break;

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
            boolean isOwner = realm.getOwner().equals(playerId);
            ItemStack playerHead = createPlayerHead(offlinePlayer, isOwner);
            inventory.setItem(slot, playerHead);
        }

        // Fill remaining empty slots with glass panes
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
        skullMeta.displayName(miniMessage.deserialize("<gold>" + p.getName() + "</gold>"));

        List<Component> lore = new ArrayList<>();
        lore.add(miniMessage.deserialize(isOwner ? "<gray>Role: <red>Owner" : "<gray>Role: <aqua>Member"));
        lore.add(miniMessage.deserialize(p.isOnline() ? "<gray>Status: <green>Online" : "<gray>Status: <red>Offline"));
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
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        String action = slotActions.get(e.getSlot());
        if (action != null) {
            switch (action) {
                case "back_button":
                    menuManager.openRealmManagementMenu(player, realmName, fromMyRealms);
                    break;
                case "previous_page":
                    if (page > 1) {
                        menuManager.openRealmPlayersMenu(player, realmName, page - 1, fromMyRealms);
                    }
                    break;
                case "next_page":
                    int itemsPerPage = 45;
                    int totalPages = (int) Math.ceil((double) allPlayers.stream().distinct().count() / itemsPerPage);
                    if (page < totalPages) {
                        menuManager.openRealmPlayersMenu(player, realmName, page + 1, fromMyRealms);
                    }
                    break;
                case "invite_player":
                    player.closeInventory();
                    plugin.getLanguageManager().sendMessage(player, "realm.invite_command_info", "%realm%", realmName);
                    break;
            }
        } else if (clickedItem.getType() == Material.PLAYER_HEAD) {
            handlePlayerKick(clickedItem);
        }
    }

    private void handlePlayerKick(ItemStack clickedItem) {
        SkullMeta meta = (SkullMeta) clickedItem.getItemMeta();
        OfflinePlayer target = meta.getOwningPlayer();
        if (target == null) return;

        Realm realm = plugin.getWorldDataManager().getRealm(realmName);
        if (realm == null) return;

        boolean isOwner = realm.getOwner().equals(player.getUniqueId());
        boolean isTargetOwner = realm.getOwner().equals(target.getUniqueId());

        if (isOwner && !isTargetOwner) {
            realm.getMembers().remove(target.getUniqueId());
            plugin.getWorldDataManager().saveData();
            plugin.getLanguageManager().sendMessage(player, "realm.player_kicked", "%player%", target.getName());
            // Refresh the menu
            menuManager.openRealmPlayersMenu(player, realmName, page, fromMyRealms);
        } else {
            plugin.getLanguageManager().sendMessage(player, "error.cannot_kick_player");
        }
    }
}