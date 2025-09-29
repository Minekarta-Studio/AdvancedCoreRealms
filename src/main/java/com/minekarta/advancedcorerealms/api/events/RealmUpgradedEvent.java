package com.minekarta.advancedcorerealms.api.events;

import com.minekarta.advancedcorerealms.data.object.Realm;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RealmUpgradedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final Realm realm;
    private final String upgradeType;
    private final String oldValue;
    private final String newValue;
    private final double cost;

    public RealmUpgradedEvent(Player player, Realm realm, String upgradeType, String oldValue, String newValue, double cost) {
        this.player = player;
        this.realm = realm;
        this.upgradeType = upgradeType;
        this.oldValue = oldValue;
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

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public double getCost() {
        return cost;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}