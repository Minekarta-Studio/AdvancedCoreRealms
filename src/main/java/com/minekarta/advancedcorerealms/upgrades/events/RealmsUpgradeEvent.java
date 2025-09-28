package com.minekarta.advancedcorerealms.upgrades.events;

import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.upgrades.RealmUpgrade;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event called when a realm upgrade is attempted
 */
public class RealmsUpgradeEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Realm realm;
    private final RealmUpgrade upgrade;
    private final int newLevel;
    private final int previousLevel;
    private boolean cancelled;

    public RealmsUpgradeEvent(Player player, Realm realm, RealmUpgrade upgrade, int newLevel, int previousLevel) {
        this.player = player;
        this.realm = realm;
        this.upgrade = upgrade;
        this.newLevel = newLevel;
        this.previousLevel = previousLevel;
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

    public int getNewLevel() {
        return newLevel;
    }

    public int getPreviousLevel() {
        return previousLevel;
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