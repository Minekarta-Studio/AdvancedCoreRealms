package com.minekarta.advancedcorerealms.menu.menus;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.manager.RealmManager;
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

    private final AdvancedCoreRealms plugin;
    private final RealmManager realmManager;
    private final FileConfiguration menuConfig;
    private final MenuManager menuManager;
    private final String realmName;
    private final int page;
    private final boolean fromMyRealms;
    private List<UUID> allPlayers;
    private Realm realm;
    private final Map<Integer, String> slotActions = new HashMap<>();
    private final Map<Integer, UUID> playerSlots = new HashMap<>();

    public RealmPlayersMenu(AdvancedCoreRealms plugin, Player player, FileConfiguration menuConfig, MenuManager menuManager, String realmName, int page, boolean fromMyRealms) {
        super(plugin, player, menuConfig.getString("realm_players.title", "Realm Players").replace("[name]", realmName), menuConfig.getInt("realm_players.size", 54));
        this.plugin = plugin;
        this.realmManager = plugin.getRealmManager();
        this.menuConfig = menuConfig;
        this.menuManager = menuManager;
        this.realmName = realmName;
        this.page = page;
        this.fromMyRealms = fromMyRealms;
        loadAndSetItems();
    }

    private void loadAndSetItems() {
        inventory.setItem(22, createGuiItem(Material.CLOCK, "&7Loading players..."));
        realmManager.getRealmByName(realmName).thenAccept(loadedRealm -> {
            this.realm = loadedRealm;
            if (this.realm == null) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.closeInventory();
                    plugin.getLanguageManager().sendMessage(player, "error.realm_not_found");
                });
                return;
            }
            this.allPlayers = new ArrayList<>(realm.getMembers().keySet());
            Bukkit.getScheduler().runTask(plugin, this::setMenuItems);
        });
    }

    private void setMenuItems() {
        inventory.clear();
        ConfigurationSection elements = menuConfig.getConfigurationSection("realm_players.elements");
        if (elements == null) return;

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

        int itemsPerPage = 45;
        int startIndex = (page - 1) * itemsPerPage;
        List<UUID> pagePlayers = allPlayers.stream().distinct().sorted().skip(startIndex).limit(itemsPerPage).collect(Collectors.toList());

        int slot = 0;
        for (UUID playerId : pagePlayers) {
            while (inventory.getItem(slot) != null && slot < itemsPerPage) slot++;
            if (slot >= itemsPerPage) break;
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
            boolean isOwner = realm.getOwner().equals(playerId);
            inventory.setItem(slot, createPlayerHead(offlinePlayer, isOwner));
            playerSlots.put(slot, playerId);
        }

        ConfigurationSection glassPaneConfig = elements.getConfigurationSection("glass_panes");
        if (glassPaneConfig != null && glassPaneConfig.getBoolean("fill_remaining", false)) {
            Material material = Material.matchMaterial(glassPaneConfig.getString("material", "BLACK_STAINED_GLASS_PANE"));
            fillWith(createGuiItem(material == null ? Material.BLACK_STAINED_GLASS_PANE : material, " "));
        }
    }

    private ItemStack createPlayerHead(OfflinePlayer p, boolean isOwner) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta == null) return head;
        meta.setOwningPlayer(p);
        meta.displayName(miniMessage.deserialize("<gold>" + p.getName() + "</gold>"));
        List<Component> lore = new ArrayList<>();
        lore.add(miniMessage.deserialize(isOwner ? "<gray>Role: <red>Owner" : "<gray>Role: <aqua>Member"));
        lore.add(miniMessage.deserialize(p.isOnline() ? "<gray>Status: <green>Online" : "<gray>Status: <red>Offline"));
        if (!isOwner) {
            lore.add(Component.text(""));
            lore.add(miniMessage.deserialize("<red>Click to kick player"));
        }
        meta.lore(lore);
        head.setItemMeta(meta);
        return head;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        if (realm == null) return;
        String action = slotActions.get(e.getSlot());
        if (action != null) {
            handleStaticClick(action);
            return;
        }
        UUID targetId = playerSlots.get(e.getSlot());
        if (targetId != null) {
            handlePlayerKick(targetId);
        }
    }

    private void handleStaticClick(String action) {
        switch (action) {
            case "back_button" -> menuManager.openRealmManagementMenu(player, realmName, fromMyRealms);
            case "previous_page" -> {
                if (page > 1) menuManager.openRealmPlayersMenu(player, realmName, page - 1, fromMyRealms);
            }
            case "next_page" -> {
                int totalPages = (int) Math.ceil((double) allPlayers.size() / 45.0);
                if (page < totalPages) menuManager.openRealmPlayersMenu(player, realmName, page + 1, fromMyRealms);
            }
            case "invite_player" -> {
                player.closeInventory();
                plugin.getLanguageManager().sendMessage(player, "realm.invite_command_info", "%realm%", realmName);
            }
        }
    }

    private void handlePlayerKick(UUID targetId) {
        boolean isOwner = realm.getOwner().equals(player.getUniqueId());
        boolean isTargetOwner = realm.getOwner().equals(targetId);

        if (isOwner && !isTargetOwner) {
            // The removeMemberFromRealm method now handles cache invalidation internally.
            realmManager.removeMemberFromRealm(realm, targetId).thenRun(() -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    plugin.getLanguageManager().sendMessage(player, "realm.player_kicked", "%player%", Bukkit.getOfflinePlayer(targetId).getName());
                    // Refresh the menu to show the updated player list
                    new RealmPlayersMenu(plugin, player, menuConfig, menuManager, realmName, page, fromMyRealms).open();
                });
            });
        } else {
            plugin.getLanguageManager().sendMessage(player, "error.cannot_kick_player");
        }
    }
}