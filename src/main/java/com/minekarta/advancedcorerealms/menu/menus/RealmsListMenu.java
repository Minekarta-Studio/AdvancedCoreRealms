package com.minekarta.advancedcorerealms.menu.menus;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.manager.RealmManager;
import com.minekarta.advancedcorerealms.menu.Menu;
import com.minekarta.advancedcorerealms.menu.MenuManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class RealmsListMenu extends Menu {

    private final AdvancedCoreRealms plugin;
    private final RealmManager realmManager;
    private final FileConfiguration menuConfig;
    private final MenuManager menuManager;
    private final boolean ownRealms;
    private final int page;
    private List<Realm> allRealms;
    private final Map<Integer, String> slotActions = new HashMap<>();
    private final Map<Integer, String> realmSlots = new HashMap<>();

    public RealmsListMenu(AdvancedCoreRealms plugin, Player player, FileConfiguration menuConfig, MenuManager menuManager, boolean ownRealms, int page) {
        super(plugin, player, menuConfig.getString(ownRealms ? "realms_list.my_realms_title" : "realms_list.accessible_realms_title", "Realms"), menuConfig.getInt("realms_list.size", 54));
        this.plugin = plugin;
        this.realmManager = plugin.getRealmManager();
        this.menuConfig = menuConfig;
        this.menuManager = menuManager;
        this.ownRealms = ownRealms;
        this.page = page;
        loadAndSetItems();
    }

    private void loadAndSetItems() {
        inventory.setItem(22, createGuiItem(Material.CLOCK, "&7Loading...")); // Centered loading item

        CompletableFuture<List<Realm>> realmsFuture = ownRealms
                ? realmManager.getRealmsByOwner(player.getUniqueId())
                : realmManager.getInvitedRealms(player.getUniqueId());

        realmsFuture.thenAccept(loadedRealms -> {
            this.allRealms = loadedRealms;
            Bukkit.getScheduler().runTask(plugin, this::setMenuItems);
        });
    }

    private void setMenuItems() {
        inventory.clear();
        ConfigurationSection elements = menuConfig.getConfigurationSection("realms_list.elements");
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
        List<Realm> pageRealms = allRealms.stream().skip(startIndex).limit(itemsPerPage).collect(Collectors.toList());

        int slot = 0;
        for (Realm realm : pageRealms) {
            while (inventory.getItem(slot) != null && slot < itemsPerPage) {
                slot++;
            }
            if (slot >= itemsPerPage) break;

            Material mat = realm.isFlat() ? Material.GRASS_BLOCK : Material.STONE;
            String name = "<green>" + realm.getName() + "</green>";
            String status = Bukkit.getWorld(realm.getWorldName()) != null ? "<gray>Status: <green>Loaded" : "<gray>Status: <red>Unloaded";
            inventory.setItem(slot, createGuiItem(mat, name, status));
            realmSlots.put(slot, realm.getName());
        }

        ConfigurationSection glassPaneConfig = elements.getConfigurationSection("glass_panes");
        if (glassPaneConfig != null && glassPaneConfig.getBoolean("fill_remaining", false)) {
            Material material = Material.matchMaterial(glassPaneConfig.getString("material", "BLACK_STAINED_GLASS_PANE"));
            if (material == null) material = Material.BLACK_STAINED_GLASS_PANE;
            String name = glassPaneConfig.getString("name", " ");
            fillWith(createGuiItem(material, name));
        }
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir() || allRealms == null) return;

        int clickedSlot = e.getSlot();

        String action = slotActions.get(clickedSlot);
        if (action != null) {
            switch (action) {
                case "back_button":
                    menuManager.openMainMenu(player);
                    break;
                case "previous_page":
                    if (page > 1) {
                        new RealmsListMenu(plugin, player, menuConfig, menuManager, ownRealms, page - 1).open();
                    }
                    break;
                case "next_page":
                    int itemsPerPage = 45;
                    int totalPages = (int) Math.ceil((double) allRealms.size() / itemsPerPage);
                    if (page < totalPages) {
                        new RealmsListMenu(plugin, player, menuConfig, menuManager, ownRealms, page + 1).open();
                    }
                    break;
            }
            return;
        }

        String realmName = realmSlots.get(clickedSlot);
        if (realmName != null) {
            menuManager.openRealmManagementMenu(player, realmName, ownRealms);
        }
    }
}