package com.minekarta.advancedcorerealms.menu.menus;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.menu.Menu;
import com.minekarta.advancedcorerealms.menu.MenuManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RealmSettingsMenu extends Menu {

    private final FileConfiguration menuConfig;
    private final MenuManager menuManager;
    private final String realmName;
    private final boolean fromMyRealms;
    private final Map<Integer, String> slotActions = new HashMap<>();

    public RealmSettingsMenu(AdvancedCoreRealms plugin, Player player, FileConfiguration menuConfig, MenuManager menuManager, String realmName, boolean fromMyRealms) {
        super(plugin, player, menuConfig.getString("realm_settings.title", "Realm Settings").replace("[name]", realmName), menuConfig.getInt("realm_settings.size", 36));
        this.menuConfig = menuConfig;
        this.menuManager = menuManager;
        this.realmName = realmName;
        this.fromMyRealms = fromMyRealms;
        setMenuItems();
    }

    private void setMenuItems() {
        ConfigurationSection elements = menuConfig.getConfigurationSection("realm_settings.elements");
        if (elements == null) return;

        Realm realm = plugin.getWorldDataManager().getRealm(realmName);
        if (realm == null) return;

        for (String key : elements.getKeys(false)) {
            ConfigurationSection itemConfig = elements.getConfigurationSection(key);
            if (itemConfig == null) continue;

            if ("glass_panes".equals(key)) {
                if (itemConfig.getBoolean("fill_remaining", false)) {
                    Material material = Material.matchMaterial(itemConfig.getString("material", "BLACK_STAINED_GLASS_PANE"));
                    if (material == null) material = Material.BLACK_STAINED_GLASS_PANE;
                    String name = itemConfig.getString("name", " ");
                    fillWith(createGuiItem(material, name));
                }
                continue;
            }

            int slot = itemConfig.getInt("slot");
            Material material = Material.matchMaterial(itemConfig.getString("material"));
            if (material == null) material = Material.STONE;

            String name = itemConfig.getString("name", "").replace("[name]", realmName).replace("[limit]", String.valueOf(realm.getMaxPlayers()));
            List<String> lore = itemConfig.getStringList("lore").stream()
                    .map(line -> line.replace("[name]", realmName).replace("[limit]", String.valueOf(realm.getMaxPlayers())))
                    .collect(Collectors.toList());

            inventory.setItem(slot, createGuiItem(material, name, lore.toArray(new String[0])));
            slotActions.put(slot, key);
        }
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        String action = slotActions.get(e.getSlot());
        if (action == null) return;

        switch (action) {
            case "set_spawn_point":
                handleSetSpawn();
                break;
            case "player_limit":
                handlePlayerLimitClick(e);
                break;
            case "back_button":
                menuManager.openRealmManagementMenu(player, realmName, fromMyRealms);
                break;
        }
    }

    private void handleSetSpawn() {
        World world = Bukkit.getWorld(realmName);
        if (world != null && world.equals(player.getWorld())) {
            world.setSpawnLocation(player.getLocation());
            plugin.getLanguageManager().sendMessage(player, "realm.spawn_set", "%realm%", realmName);
            player.closeInventory();
        } else {
            plugin.getLanguageManager().sendMessage(player, "error.must_be_in_realm");
            player.closeInventory();
        }
    }

    private void handlePlayerLimitClick(InventoryClickEvent e) {
        Realm realm = plugin.getWorldDataManager().getRealm(realmName);
        if (realm == null) return;

        int currentLimit = realm.getMaxPlayers();
        int newLimit = currentLimit;

        if (e.isLeftClick()) {
            newLimit += e.isShiftClick() ? 5 : 1;
        } else if (e.isRightClick()) {
            newLimit -= e.isShiftClick() ? 5 : 1;
        }

        newLimit = Math.max(1, Math.min(100, newLimit)); // Clamp between 1 and 100

        if (newLimit != currentLimit) {
            realm.setMaxPlayers(newLimit);
            plugin.getWorldDataManager().saveData();
            plugin.getLanguageManager().sendMessage(player, "realm.player_limit_set", "%realm%", realmName, "%limit%", String.valueOf(newLimit));
            // Re-open the menu to reflect the change
            new RealmSettingsMenu(plugin, player, menuConfig, menuManager, realmName, fromMyRealms).open();
        }
    }
}