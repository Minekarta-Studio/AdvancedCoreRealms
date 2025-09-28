package com.minekarta.advancedcorerealms.listeners;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryClickListener implements Listener {
    
    private final AdvancedCoreRealms plugin;
    
    public InventoryClickListener(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().startsWith("AdvancedCoreRealms") || 
            event.getView().getTitle().startsWith("Realms |")) {
            plugin.getGuiManager().handleInventoryClick(event, event.getView().getTitle());
        }
    }
}