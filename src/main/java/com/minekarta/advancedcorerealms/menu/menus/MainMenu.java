package com.minekarta.advancedcorerealms.menu.menus;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.menu.Menu;
import com.minekarta.advancedcorerealms.menu.MenuManager;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainMenu extends Menu {

    private final FileConfiguration menuConfig;
    private final MenuManager menuManager;
    private final Map<Integer, String> slotActions = new HashMap<>();

    public MainMenu(AdvancedCoreRealms plugin, Player player, FileConfiguration menuConfig, MenuManager menuManager) {
        super(plugin, player, menuConfig.getString("main_menu.title", "Main Menu"), menuConfig.getInt("main_menu.size", 27));
        this.menuConfig = menuConfig;
        this.menuManager = menuManager;
        setMenuItems();
    }

    private void setMenuItems() {
        ConfigurationSection elements = menuConfig.getConfigurationSection("main_menu.elements");
        if (elements == null) return;

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
            String name = itemConfig.getString("name");
            List<String> lore = itemConfig.getStringList("lore");

            inventory.setItem(slot, createGuiItem(material, name, lore.toArray(new String[0])));
            slotActions.put(slot, key); // Map the slot to the config key (e.g., "my_realms")
        }
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        String action = slotActions.get(e.getSlot());
        if (action == null) return;

        switch (action) {
            case "my_realms":
                menuManager.openRealmsListMenu(player, true);
                break;
            case "accessible_realms":
                menuManager.openRealmsListMenu(player, false);
                break;
            case "create_realm":
                handleCreateRealmClick();
                break;
            case "border_color": // Assuming key is "border_color" in YML
                menuManager.openBorderColorMenu(player);
                break;
            case "upgrades": // Assuming key is "upgrades" in YML
                menuManager.openUpgradeMenu(player);
                break;
            case "close": // Assuming key is "close" in YML
                player.closeInventory();
                break;
        }
    }

    private void handleCreateRealmClick() {
        ConfigurationSection createRealmConfig = menuConfig.getConfigurationSection("main_menu.elements.create_realm");
        if (createRealmConfig == null) return;

        String permissions = createRealmConfig.getString("permission_required", "");
        if (permissions.isEmpty() || checkPermissions(permissions)) {
            menuManager.openRealmCreationMenu(player);
        } else {
            plugin.getLanguageManager().sendMessage(player, "error.no-permission-create");
            player.closeInventory();
        }
    }

    private boolean checkPermissions(String permissions) {
        for (String perm : permissions.split(",")) {
            if (player.hasPermission(perm.trim())) {
                return true;
            }
        }
        return false;
    }
}