package com.minekarta.advancedcorerealms.data.object;

import com.minekarta.advancedcorerealms.worldborder.BorderColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

/**
 * A data object to hold player-specific settings.
 */
public class PlayerData {
    private final UUID playerUUID;
    private String previousLocation;
    private boolean borderEnabled;
    private BorderColor borderColor;

    public PlayerData(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.borderEnabled = true; // Default value
        this.borderColor = BorderColor.BLUE; // Default value
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getPreviousLocationString() {
        return previousLocation;
    }

    public void setPreviousLocationString(String previousLocation) {
        this.previousLocation = previousLocation;
    }

    public Location getPreviousLocation() {
        return deserialize(this.previousLocation);
    }

    public void setPreviousLocation(Location location) {
        this.previousLocation = serialize(location);
    }

    public boolean isBorderEnabled() {
        return borderEnabled;
    }

    public void setBorderEnabled(boolean borderEnabled) {
        this.borderEnabled = borderEnabled;
    }

    public BorderColor getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(BorderColor borderColor) {
        this.borderColor = borderColor;
    }

    public static String serialize(Location loc) {
        if (loc == null) return null;
        return loc.getWorld().getName() + ";" +
               loc.getX() + ";" +
               loc.getY() + ";" +
               loc.getZ() + ";" +
               loc.getYaw() + ";" +
               loc.getPitch();
    }

    public static Location deserialize(String s) {
        if (s == null || s.isEmpty()) return null;
        String[] parts = s.split(";");
        if (parts.length != 6) return null;

        try {
            World world = Bukkit.getWorld(parts[0]);
            if (world == null) return null;
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = Float.parseFloat(parts[4]);
            float pitch = Float.parseFloat(parts[5]);
            return new Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}