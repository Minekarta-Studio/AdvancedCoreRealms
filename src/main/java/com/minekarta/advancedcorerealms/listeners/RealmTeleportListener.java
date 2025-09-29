package com.minekarta.advancedcorerealms.listeners;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.WorldDataManager;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.realm.RealmInventoryService;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Optional;

public class RealmTeleportListener implements Listener {

    private final RealmInventoryService realmInventoryService;
    private final WorldDataManager worldDataManager;

    public RealmTeleportListener(AdvancedCoreRealms plugin) {
        this.realmInventoryService = plugin.getRealmInventoryService();
        this.worldDataManager = plugin.getWorldDataManager();
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        World fromWorld = event.getFrom().getWorld();
        World toWorld = event.getTo().getWorld();

        if (fromWorld.equals(toWorld)) {
            return; // No world change, no inventory swap needed
        }

        Optional<Realm> fromRealm = worldDataManager.getRealmByWorldName(fromWorld.getName());
        Optional<Realm> toRealm = worldDataManager.getRealmByWorldName(toWorld.getName());

        // Case 1: Leaving a realm to a non-realm world (or another realm, handled by enterRealm)
        if (fromRealm.isPresent() && toRealm.isEmpty()) {
            realmInventoryService.exitRealm(event.getPlayer(), fromRealm.get());
        }

        // enterRealm is handled by the teleport command for controlled teleports.
        // This listener primarily handles EXITS to ensure inventory is restored
        // no matter how the player leaves the realm (e.g. /spawn, /home).
    }
}