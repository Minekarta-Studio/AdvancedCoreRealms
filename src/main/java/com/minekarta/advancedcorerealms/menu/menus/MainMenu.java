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

public class MainMenu extends Menu {

    private final FileConfiguration menuConfig;
    private final MenuManager menuManager;

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
        }
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir() || clickedItem.getItemMeta() == null) return;

        String displayName = PlainTextComponentSerializer.plainText().serialize(clickedItem.getItemMeta().displayName());

        if (displayName.contains("My Realms")) {
            menuManager.openRealmsListMenu(player, true);
        } else if (displayName.contains("Accessible Realms")) {
            menuManager.openRealmsListMenu(player, false);
        } else if (displayName.contains("Create Realm")) {
            ConfigurationSection createRealmConfig = menuConfig.getConfigurationSection("main_menu.elements.create_realm");
            if (createRealmConfig != null) {
                String permissions = createRealmConfig.getString("permission_required", "");
                boolean hasPerm = false;
                if (permissions.isEmpty()) {
                    hasPerm = true;
                } else {
                    for (String perm : permissions.split(",")) {
                        if (player.hasPermission(perm.trim())) {
                            hasPerm = true;
                            break;
                        }
                    }
                }

                if (hasPerm) {
                    menuManager.openRealmCreationMenu(player);
                } else {
                    player.sendMessage(miniMessage.deserialize("<red>You do not have permission to create a realm.</red>"));
                    player.closeInventory();
                }
            }
        } else if (displayName.contains("Border Color")) {
            menuManager.openBorderColorMenu(player);
        } else if (displayName.contains("Upgrades")) {
            menuManager.openUpgradeMenu(player);
        } else if (displayName.contains("Close")) {
            player.closeInventory();
        }
    }
}