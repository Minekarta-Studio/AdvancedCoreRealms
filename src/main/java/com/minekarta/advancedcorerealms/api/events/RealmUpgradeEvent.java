package com.minekarta.advancedcorerealms.api.events;

import com.minekarta.advancedcorerealms.data.object.Realm;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RealmUpgradeEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private final Realm realm;
    private final String upgradeType;
    private final String newValue;
    private final double cost;

    public RealmUpgradeEvent(Player player, Realm realm, String upgradeType, String newValue, double cost) {
        this.player = player;
        this.realm = realm;
        this.upgradeType = upgradeType;
        this.newValue = newValue;
        this.cost = cost;
    }

    public Player getPlayer() {
        return player;
    }

    public Realm getRealm() {
        return realm;
    }

    public String getUpgradeType() {
        return upgradeType;
    }

    public String getNewValue() {
        return newValue;
    }

    public double getCost() {
        return cost;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}