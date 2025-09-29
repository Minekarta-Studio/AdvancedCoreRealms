package com.minekarta.advancedcorerealms.listeners;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.manager.RealmManager;
import com.minekarta.advancedcorerealms.realm.Role;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Optional;

public class RealmProtectionListener implements Listener {

    private final RealmManager realmManager;

    public RealmProtectionListener(AdvancedCoreRealms plugin) {
        this.realmManager = plugin.getRealmManager();
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        // Use the fast, synchronous cache lookup for the listener
        Optional<Realm> realmOptional = realmManager.getRealmFromCacheByWorld(player.getWorld().getName());

        if (realmOptional.isPresent()) {
            Realm realm = realmOptional.get();
            Role playerRole = realm.getRole(player.getUniqueId());

            if (playerRole == Role.VISITOR) {
                event.setCancelled(true);
                // Optionally send a message via LanguageManager if feedback is desired
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Optional<Realm> realmOptional = realmManager.getRealmFromCacheByWorld(player.getWorld().getName());

        if (realmOptional.isPresent()) {
            Realm realm = realmOptional.get();
            Role playerRole = realm.getRole(player.getUniqueId());

            if (playerRole == Role.VISITOR) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getClickedBlock() == null) return;

        Optional<Realm> realmOptional = realmManager.getRealmFromCacheByWorld(player.getWorld().getName());

        if (realmOptional.isPresent()) {
            Realm realm = realmOptional.get();
            Role playerRole = realm.getRole(player.getUniqueId());

            if (playerRole == Role.VISITOR) {
                switch (event.getClickedBlock().getType()) {
                    case CHEST, TRAPPED_CHEST, FURNACE, BLAST_FURNACE, SMOKER, BARREL,
                         SHULKER_BOX, ENDER_CHEST, ANVIL, CRAFTING_TABLE, HOPPER,
                         DROPPER, DISPENSER, BEACON, BREWING_STAND:
                        event.setCancelled(true);
                        break;
                    default:
                        // Allow interaction with simple blocks like buttons, levers, doors, etc.
                        break;
                }
            }
        }
    }
}