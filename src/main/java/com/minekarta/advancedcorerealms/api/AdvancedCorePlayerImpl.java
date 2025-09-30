package com.minekarta.advancedcorerealms.api;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.manager.RealmManager;
import com.minekarta.advancedcorerealms.nms.NMSWorldBorder;
import com.minekarta.advancedcorerealms.worldborder.BorderColor;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class AdvancedCorePlayerImpl implements AdvancedCorePlayer {

    private final AdvancedCoreRealms plugin;
    private final Player player;
    private final UUID uuid;
    private final RealmManager realmManager;
    // private final NMSWorldBorder nmsWorldBorder; // Temporarily commented out to fix compilation

    public AdvancedCorePlayerImpl(AdvancedCoreRealms plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.uuid = player.getUniqueId();
        this.realmManager = plugin.getRealmManager();
        // this.nmsWorldBorder = NMSWorldBorder.getImplementation(plugin);
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
    public void updateWorldBorder(Realm realm) {
        // This functionality for per-player borders seems deprecated in favor of server-side world borders.
        // If per-player cosmetic borders are desired, this can be re-implemented.
        // For now, this method does nothing to avoid compilation errors and conflicts with the main system.
    }

    @Override
    public void removeWorldBorder() {
        // if (nmsWorldBorder != null) {
        //     nmsWorldBorder.removeWorldBorder(player);
        // }
    }

    @Override
    public void updateWorldBorder() {
        getCurrentRealm().ifPresent(this::updateWorldBorder);
    }

    private Optional<Realm> getCurrentRealm() {
        // Worlds are now in "realms/<uuid>", but Bukkit reports the world name as the full path.
        String worldName = player.getWorld().getName();
        if (worldName.startsWith("realms/")) {
            String worldFolderName = worldName.substring("realms/".length());
            return realmManager.getRealmByWorldFolderName(worldFolderName);
        }
        return Optional.empty();
    }
}