package com.minekarta.advancedcorerealms.menu.menus;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.menu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class RealmCreationMenu extends Menu {

    public RealmCreationMenu(AdvancedCoreRealms plugin, Player player, FileConfiguration menuConfig) {
        super(plugin, player, menuConfig.getString("realm_creation.title", "Create Realm"), menuConfig.getInt("realm_creation.size", 27));
        setMenuItems(menuConfig);
        autoClose();
    }

    private void setMenuItems(FileConfiguration menuConfig) {
        ConfigurationSection elements = menuConfig.getConfigurationSection("realm_creation.elements");
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
        }
    }

    private void autoClose() {
        Bukkit.getScheduler().runTaskLater(plugin, (Runnable) player::closeInventory, 60L); // 3 seconds
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        // No interaction needed in this menu.
    }
}