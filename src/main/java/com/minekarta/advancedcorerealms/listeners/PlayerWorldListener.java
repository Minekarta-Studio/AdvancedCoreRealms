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

    private Optional<Realm> getRealmFromWorld(World world) {
        String worldName = world.getName();
        if (worldName.startsWith("realms/")) {
            String worldFolderName = worldName.substring("realms/".length());
            return realmManager.getRealmByWorldFolderName(worldFolderName);
        }
        return Optional.empty();
    }

    @EventHandler
    public void onPlayerWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World toWorld = player.getWorld();

        Optional<Realm> toRealmOpt = getRealmFromWorld(toWorld);

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
            player.setGameMode(plugin.getServer().getDefaultGameMode());
        }

        // Update world border for the player based on their new location
        AdvancedCorePlayer advancedCorePlayer = plugin.getAdvancedCorePlayer(player);
        advancedCorePlayer.updateWorldBorder();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Optional<Realm> currentRealmOpt = getRealmFromWorld(player.getWorld());

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
        // Remove world border when player quits
        AdvancedCorePlayer advancedCorePlayer = plugin.getAdvancedCorePlayer(event.getPlayer());
        advancedCorePlayer.removeWorldBorder();
    }
}