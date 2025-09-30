package com.minekarta.advancedcorerealms.listeners;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.manager.LanguageManager;
import com.minekarta.advancedcorerealms.manager.RealmManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class InventoryListener implements Listener {

    private final AdvancedCoreRealms plugin;
    private final RealmManager realmManager;
    private final LanguageManager languageManager;

    public InventoryListener(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.realmManager = plugin.getRealmManager();
        this.languageManager = plugin.getLanguageManager();
    }

    private Optional<Realm> getRealmFromPlayer(Player player) {
        String worldName = player.getWorld().getName();
        if (worldName.startsWith("realms/")) {
            String worldFolderName = worldName.substring("realms/".length());
            return realmManager.getRealmByWorldFolderName(worldFolderName);
        }
        return Optional.empty();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // This listener primarily handles GUI interactions, which are cancelled within the Menu system.
        // The logic for item transfer restrictions is better handled on world-change events.
        // This keeps the listener clean and focused.
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Optional<Realm> currentRealmOpt = getRealmFromPlayer(player);

        if (currentRealmOpt.isEmpty()) {
            return; // Not in a realm, no restrictions
        }

        Realm currentRealm = currentRealmOpt.get();
        ItemStack droppedItem = event.getItemDrop().getItemStack();

        // Prevent dropping non-transferable items inside a realm.
        // This is a simple safeguard against dropping items for others to bypass inventory separation.
        if (!currentRealm.isItemTransferable(droppedItem.getType().name())) {
            event.setCancelled(true);
            languageManager.sendMessage(player, "error.item_not_transferable");
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        // As with onInventoryClick, this is primarily for GUIs, which are handled elsewhere.
    }
}