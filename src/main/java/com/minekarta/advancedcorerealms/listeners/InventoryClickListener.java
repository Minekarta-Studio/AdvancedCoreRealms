package com.minekarta.advancedcorerealms.listeners;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.menu.Menu;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class InventoryClickListener implements Listener {

    public InventoryClickListener(AdvancedCoreRealms plugin) {
        // Constructor can be used for dependency injection if needed later
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        InventoryHolder holder = e.getInventory().getHolder();

        // If the inventory has a custom holder that is a Menu, handle the click
        if (holder instanceof Menu) {
            e.setCancelled(true);
            Menu menu = (Menu) holder;
            menu.handleMenu(e);
        }
    }
}