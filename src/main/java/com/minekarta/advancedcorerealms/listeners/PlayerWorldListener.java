package com.minekarta.advancedcorerealms.listeners;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.api.AdvancedCorePlayer;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.manager.RealmManager;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Optional;

public class PlayerWorldListener implements Listener {

    private final AdvancedCoreRealms plugin;
    private final RealmManager realmManager;

    public PlayerWorldListener(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.realmManager = plugin.getRealmManager();
    }

    @EventHandler
    public void onPlayerWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World toWorld = player.getWorld();

        Optional<Realm> toRealmOpt = realmManager.getRealmFromCacheByWorld(toWorld.getName());

        // Handle GameMode changes when entering/leaving a realm
        if (toRealmOpt.isPresent()) {
            Realm toRealm = toRealmOpt.get();
            if (toRealm.isCreativeMode()) {
                player.setGameMode(GameMode.CREATIVE);
            } else {
                player.setGameMode(GameMode.SURVIVAL);
            }
        } else {
            // Player is leaving a realm and entering a non-realm world, set to default gamemode.
            // This prevents keeping creative mode from a creative realm.
            player.setGameMode(plugin.getServer().getDefaultGameMode());
        }

        // Update world border for the player based on their new location
        AdvancedCorePlayer advancedCorePlayer = plugin.getAdvancedCorePlayer(player);
        advancedCorePlayer.updateWorldBorder();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Optional<Realm> currentRealmOpt = realmManager.getRealmFromCacheByWorld(player.getWorld().getName());

        // Set game mode based on realm settings if they log into a realm
        currentRealmOpt.ifPresent(currentRealm -> {
            if (currentRealm.isCreativeMode()) {
                player.setGameMode(GameMode.CREATIVE);
            } else {
                player.setGameMode(GameMode.SURVIVAL);
            }
        });

        // Update world border based on player's current location
        AdvancedCorePlayer advancedCorePlayer = plugin.getAdvancedCorePlayer(event.getPlayer());
        advancedCorePlayer.updateWorldBorder();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Inventory saving is handled by RealmInventoryService.
        // We just need to handle things specific to the player's session.

        // Remove world border when player quits
        AdvancedCorePlayer advancedCorePlayer = plugin.getAdvancedCorePlayer(event.getPlayer());
        advancedCorePlayer.removeWorldBorder();
    }
}