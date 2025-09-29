package com.minekarta.advancedcorerealms.listeners;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.manager.RealmManager;
import com.minekarta.advancedcorerealms.realm.RealmInventoryService;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Optional;

public class RealmTeleportListener implements Listener {

    private final RealmInventoryService realmInventoryService;
    private final RealmManager realmManager;

    public RealmTeleportListener(AdvancedCoreRealms plugin) {
        this.realmInventoryService = plugin.getRealmInventoryService();
        this.realmManager = plugin.getRealmManager();
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        World fromWorld = event.getFrom().getWorld();
        World toWorld = event.getTo().getWorld();

        if (fromWorld.equals(toWorld)) {
            return; // No world change, no inventory swap needed
        }

        // Use the fast, synchronous cache lookup
        Optional<Realm> fromRealm = realmManager.getRealmFromCacheByWorld(fromWorld.getName());
        Optional<Realm> toRealm = realmManager.getRealmFromCacheByWorld(toWorld.getName());

        // Case 1: Leaving a realm to a non-realm world
        if (fromRealm.isPresent() && toRealm.isEmpty()) {
            realmInventoryService.exitRealm(event.getPlayer(), fromRealm.get());
        }

        // Case 2: Entering a realm from a non-realm world (less common, but good to handle)
        if (fromRealm.isEmpty() && toRealm.isPresent()) {
             realmInventoryService.enterRealm(event.getPlayer(), toRealm.get());
        }

        // Case 3: Moving between two different realms
        if (fromRealm.isPresent() && toRealm.isPresent() && !fromRealm.get().getName().equals(toRealm.get().getName())) {
            // The `enterRealm` logic handles saving the old inventory and loading the new one.
            realmInventoryService.enterRealm(event.getPlayer(), toRealm.get());
        }
    }
}