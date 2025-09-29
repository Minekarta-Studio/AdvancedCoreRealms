package com.minekarta.advancedcorerealms.api;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.PlayerData;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.manager.RealmManager;
import com.minekarta.advancedcorerealms.nms.NMSWorldBorder;
import com.minekarta.advancedcorerealms.worldborder.BorderColor;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AdvancedCorePlayerImpl implements AdvancedCorePlayer {

    private final AdvancedCoreRealms plugin;
    private final Player player;
    private final UUID uuid;
    private final NMSWorldBorder nmsWorldBorder;
    private final RealmManager realmManager;

    public AdvancedCorePlayerImpl(AdvancedCoreRealms plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.uuid = player.getUniqueId();
        this.nmsWorldBorder = NMSWorldBorder.getImplementation(plugin);
        this.realmManager = plugin.getRealmManager();
    }

    @Override
    public Player asPlayer() {
        return player;
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public boolean hasWorldBorderEnabled() {
        return realmManager.getPlayerData(uuid).isBorderEnabled();
    }

    @Override
    public void toggleWorldBorder() {
        PlayerData data = realmManager.getPlayerData(uuid);
        boolean newState = !data.isBorderEnabled();
        data.setBorderEnabled(newState);
        realmManager.savePlayerData(data); // Asynchronously save the change

        if (newState) {
            updateWorldBorder();
        } else {
            removeWorldBorder();
        }
    }

    @Override
    public BorderColor getBorderColor() {
        return realmManager.getPlayerData(uuid).getBorderColor();
    }

    @Override
    public void setBorderColor(BorderColor color) {
        PlayerData data = realmManager.getPlayerData(uuid);
        data.setBorderColor(color);
        realmManager.savePlayerData(data); // Asynchronously save the change
        updateWorldBorder();
    }

    @Override
    public void updateWorldBorder(Realm realm) {
        if (realm != null && hasWorldBorderEnabled()) {
            if (plugin.getConfig().getBoolean("world-borders", true)) {
                sendWorldBorderPacket(realm);
            }
        } else {
            removeWorldBorder();
        }
    }

    @Override
    public void removeWorldBorder() {
        nmsWorldBorder.removeWorldBorder(player);
    }

    @Override
    public void updateWorldBorder() {
        getCurrentRealmAsync().thenAccept(this::updateWorldBorder);
    }

    private void sendWorldBorderPacket(Realm realm) {
        org.bukkit.World bukkitWorld = realm.getBukkitWorld();
        if (bukkitWorld != null && player.getWorld().equals(bukkitWorld)) {
            double centerX = realm.getBorderCenterX();
            double centerZ = realm.getBorderCenterZ();
            double size = realm.getBorderSize();

            if (centerX == 0.0 && centerZ == 0.0) {
                org.bukkit.Location spawnLocation = bukkitWorld.getSpawnLocation();
                centerX = spawnLocation.getX();
                centerZ = spawnLocation.getZ();
                realm.setBorderCenterX(centerX);
                realm.setBorderCenterZ(centerZ);
            }

            BorderColor color = getBorderColor();
            nmsWorldBorder.sendWorldBorder(player, bukkitWorld, centerX, centerZ, size, color);
        }
    }

    private CompletableFuture<Realm> getCurrentRealmAsync() {
        String worldName = player.getWorld().getName();
        return realmManager.getRealmByWorldName(worldName);
    }
}