package com.minekarta.advancedcorerealms.menu.menus;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.api.AdvancedCorePlayer;
import com.minekarta.advancedcorerealms.menu.Menu;
import com.minekarta.advancedcorerealms.menu.MenuManager;
import com.minekarta.advancedcorerealms.worldborder.BorderColor;
import com.minekarta.advancedcorerealms.worldborder.PlayerChangeBorderColorEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class BorderColorMenu extends Menu {

    private final MenuManager menuManager;

    public BorderColorMenu(AdvancedCoreRealms plugin, Player player, FileConfiguration menuConfig, MenuManager menuManager) {
        super(plugin, player, menuConfig.getString("border_color.title", "Border Color"), menuConfig.getInt("border_color.size", 27));
        this.menuManager = menuManager;
        setMenuItems(menuConfig);
    }

    private void setMenuItems(FileConfiguration menuConfig) {
        ConfigurationSection elements = menuConfig.getConfigurationSection("border_color.elements");
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

        if (displayName.contains("Blue")) {
            setBorderColor(BorderColor.BLUE);
        } else if (displayName.contains("Green")) {
            setBorderColor(BorderColor.GREEN);
        } else if (displayName.contains("Red")) {
            setBorderColor(BorderColor.RED);
        } else if (displayName.equalsIgnoreCase("Back")) {
            menuManager.openMainMenu(player);
        }
    }

    private void setBorderColor(BorderColor color) {
        AdvancedCorePlayer advancedCorePlayer = plugin.getAdvancedCorePlayer(player);
        PlayerChangeBorderColorEvent colorEvent = new PlayerChangeBorderColorEvent(player, color);
        Bukkit.getPluginManager().callEvent(colorEvent);

        if (!colorEvent.isCancelled()) {
            advancedCorePlayer.setBorderColor(color);
            player.sendMessage(miniMessage.deserialize("<green>Border color set to " + color.name().toLowerCase() + "!</green>"));
            player.closeInventory();
        }
    }
}