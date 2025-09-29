package com.minekarta.advancedcorerealms.menu;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public abstract class Menu implements InventoryHolder {

    protected final AdvancedCoreRealms plugin;
    protected final Player player;
    protected final Inventory inventory;

    public Menu(AdvancedCoreRealms plugin, Player player, String title, int size) {
        this.plugin = plugin;
        this.player = player;
        Component inventoryTitle = ColorUtils.toComponent(title, player);
        this.inventory = Bukkit.createInventory(this, size, inventoryTitle);
    }

    public void open() {
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public abstract void handleMenu(InventoryClickEvent e);

    protected ItemStack createGuiItem(final Material material, final String name, final String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        meta.displayName(ColorUtils.toComponent(name, player));
        List<Component> loreList = new ArrayList<>();
        for (String line : lore) {
            loreList.add(ColorUtils.toComponent(line, player));
        }
        meta.lore(loreList);
        item.setItemMeta(meta);
        return item;
    }

    protected void fillWith(ItemStack item) {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, item);
            }
        }
    }
}