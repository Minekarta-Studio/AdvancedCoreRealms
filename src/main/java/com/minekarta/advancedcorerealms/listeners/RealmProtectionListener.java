package com.minekarta.advancedcorerealms.listeners;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.WorldDataManager;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.realm.Role;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Optional;

public class RealmProtectionListener implements Listener {

    private final AdvancedCoreRealms plugin;
    private final WorldDataManager worldDataManager;

    public RealmProtectionListener(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.worldDataManager = plugin.getWorldDataManager();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Optional<Realm> realmOptional = worldDataManager.getRealmByWorldName(player.getWorld().getName());

        if (realmOptional.isPresent()) {
            Realm realm = realmOptional.get();
            Role playerRole = realm.getRole(player.getUniqueId());

            // Only members, admins, and owners can place blocks
            if (playerRole == Role.VISITOR) {
                event.setCancelled(true);
                // Optionally send a message
                // player.sendMessage("You do not have permission to build here.");
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Optional<Realm> realmOptional = worldDataManager.getRealmByWorldName(player.getWorld().getName());

        if (realmOptional.isPresent()) {
            Realm realm = realmOptional.get();
            Role playerRole = realm.getRole(player.getUniqueId());

            // Only members, admins, and owners can break blocks
            if (playerRole == Role.VISITOR) {
                event.setCancelled(true);
                // Optionally send a message
                // player.sendMessage("You do not have permission to break blocks here.");
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getClickedBlock() == null) return; // Ignore interactions with air

        Optional<Realm> realmOptional = worldDataManager.getRealmByWorldName(player.getWorld().getName());

        if (realmOptional.isPresent()) {
            Realm realm = realmOptional.get();
            Role playerRole = realm.getRole(player.getUniqueId());

            // More granular checks can be added here, for now, visitors can't interact with most things
            if (playerRole == Role.VISITOR) {
                // Allow interaction with non-storage blocks (buttons, levers) but not chests, doors, etc.
                switch (event.getClickedBlock().getType()) {
                    case CHEST:
                    case TRAPPED_CHEST:
                    case FURNACE:
                    case BLAST_FURNACE:
                    case SMOKER:
                    case BARREL:
                    case SHULKER_BOX:
                    case ENDER_CHEST:
                    case ANVIL:
                    case CRAFTING_TABLE:
                    case HOPPER:
                    case DROPPER:
                    case DISPENSER:
                        event.setCancelled(true);
                        // player.sendMessage("You cannot interact with this block.");
                        break;
                    default:
                        // Allow other interactions
                        break;
                }
            }
        }
    }
}