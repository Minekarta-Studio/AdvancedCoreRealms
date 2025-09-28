package com.minekarta.advancedcorerealms.upgrades.events;

import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.upgrades.RealmUpgrade;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event called before a realm upgrade check is performed
 */
public class RealmsUpgradePreCheckEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Realm realm;
    private final RealmUpgrade upgrade;
    private boolean cancelled;

    public RealmsUpgradePreCheckEvent(Player player, Realm realm, RealmUpgrade upgrade) {
        this.player = player;
        this.realm = realm;
        this.upgrade = upgrade;
        this.cancelled = false;
    }

    public Player getPlayer() {
        return player;
    }

    public Realm getRealm() {
        return realm;
    }

    public RealmUpgrade getUpgrade() {
        return upgrade;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}