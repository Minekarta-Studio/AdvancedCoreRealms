package com.minekarta.advancedcorerealms.menu.menus;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.menu.Menu;
import com.minekarta.advancedcorerealms.menu.MenuManager;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class RealmManagementMenu extends Menu {

    private final FileConfiguration menuConfig;
    private final MenuManager menuManager;
    private final String realmName;

    public RealmManagementMenu(AdvancedCoreRealms plugin, Player player, FileConfiguration menuConfig, MenuManager menuManager, String realmName) {
        super(plugin, player, menuConfig.getString("realm_management.title", "Realm Management").replace("[name]", realmName), menuConfig.getInt("realm_management.size", 36));
        this.menuConfig = menuConfig;
        this.menuManager = menuManager;
        this.realmName = realmName;
        setMenuItems();
    }

    private void setMenuItems() {
        ConfigurationSection elements = menuConfig.getConfigurationSection("realm_management.elements");
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
            String name = itemConfig.getString("name").replace("[name]", realmName);
            List<String> lore = itemConfig.getStringList("lore");
            lore.replaceAll(text -> text.replace("[name]", realmName));

            inventory.setItem(slot, createGuiItem(material, name, lore.toArray(new String[0])));
        }
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir() || clickedItem.getItemMeta() == null) return;

        String displayName = PlainTextComponentSerializer.plainText().serialize(clickedItem.getItemMeta().displayName());

        if (displayName.equalsIgnoreCase("Teleport")) {
            plugin.getWorldManager().teleportToRealm(player, realmName);
            player.closeInventory();
        } else if (displayName.equalsIgnoreCase("Manage Players")) {
            menuManager.openRealmPlayersMenu(player, realmName, 1);
        } else if (displayName.equalsIgnoreCase("Realm Settings")) {
            menuManager.openRealmSettingsMenu(player, realmName);
        } else if (displayName.equalsIgnoreCase("Delete Realm")) {
            player.closeInventory();
            player.sendMessage("<red>To delete this realm, use: /realms delete " + realmName);
        } else if (displayName.equalsIgnoreCase("Back")) {
            // This is a simplification. We need to know which list to go back to.
            menuManager.openRealmsListMenu(player, true);
        }
    }
}