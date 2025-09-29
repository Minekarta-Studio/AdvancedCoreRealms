package com.minekarta.advancedcorerealms.worldborder;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages all world border and world-related settings for realms.
 * This service centralizes logic for applying world border dimensions, centers,
 * and game difficulty. It handles cases where worlds are not yet loaded by
 * queueing changes and applying them upon the {@link WorldLoadEvent}, ensuring
 * reliability and preventing main thread lag by using schedulers where appropriate.
 */
public class WorldBorderService implements Listener {

    private final AdvancedCoreRealms plugin;
    private final ConcurrentHashMap<String, Realm> pendingBorders = new ConcurrentHashMap<>();

    /**
     * Constructs the WorldBorderService and registers it as an event listener.
     *
     * @param plugin The main plugin instance.
     */
    public WorldBorderService(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Safely applies the border settings from a {@link Realm} object to its corresponding world.
     * <p>
     * This method is the primary entry point for all world border updates. It checks if the
     * target world is currently loaded.
     * <ul>
     *     <li>If the world is loaded, it applies the border changes immediately.</li>
     *     <li>If the world is not loaded, it adds the realm's border configuration to a
     *     pending queue. The changes will be applied later when the world loads, triggered
     *     by the {@link #onWorldLoad(WorldLoadEvent)} listener.</li>
     * </ul>
     * This ensures that border settings are never lost, even if a realm is modified while
     * its world is inactive.
     *
     * @param realm The realm object containing the border configuration to apply. Must not be null.
     */
    public void applyWorldBorder(Realm realm) {
        if (realm == null) {
            plugin.getLogger().warning("Attempted to apply world border for a null realm.");
            return;
        }

        World world = Bukkit.getWorld(realm.getWorldName());
        if (world != null) {
            // World is loaded, apply immediately
            plugin.getLogger().info("Applying world border for loaded world: " + realm.getWorldName());
            updateBorder(world, realm);
        } else {
            // World is not loaded, queue for later
            plugin.getLogger().info("World " + realm.getWorldName() + " is not loaded. Queuing border update.");
            pendingBorders.put(realm.getWorldName(), realm);
        }
    }

    /**
     * Event handler that triggers when a world is loaded. It checks if there are any
     * pending border updates queued for the newly loaded world and applies them.
     *
     * @param event The world load event.
     */
    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        String worldName = event.getWorld().getName();
        Realm realm = pendingBorders.remove(worldName);
        if (realm != null) {
            plugin.getLogger().info("Applying queued world border for newly loaded world: " + worldName);
            updateBorder(event.getWorld(), realm);
        }
    }

    /**
     * Core private method that performs the actual border update on a given world.
     * It sets the border's center and size, with validation and logging for robustness.
     * It also triggers the player relocation check after a successful update.
     *
     * @param world The world where the border will be updated.
     * @param realm The realm object containing the new border settings.
     */
    private void updateBorder(World world, Realm realm) {
        if (world == null || realm == null) {
            plugin.getLogger().warning("updateBorder called with null world or realm.");
            return;
        }

        WorldBorder border = world.getWorldBorder();

        // Validate and set center
        try {
            border.setCenter(realm.getBorderCenterX(), realm.getBorderCenterZ());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().log(Level.SEVERE, "Invalid border center coordinates for realm " + realm.getName() +
                    ": (" + realm.getBorderCenterX() + ", " + realm.getBorderCenterZ() + "). Defaulting to spawn location.", e);
            border.setCenter(world.getSpawnLocation());
        }

        // Validate and set size
        if (realm.getBorderSize() > 0) {
            border.setSize(realm.getBorderSize());
        } else {
            plugin.getLogger().warning("Invalid border size (" + realm.getBorderSize() + ") for realm " + realm.getName() +
                    ". The border size must be positive. No changes were made to the size.");
        }

        plugin.getLogger().info("Successfully updated border for " + world.getName() +
                " to size " + border.getSize() + // Log the actual applied size
                " at (" + border.getCenter().getX() + ", " + border.getCenter().getZ() + ")");

        // After updating the border, check for any players outside of it
        checkAndRelocatePlayers(world);
    }

    /**
     * Safely applies the difficulty setting from a {@link Realm} to its world.
     * The update is performed on the main server thread via the Bukkit scheduler to
     * ensure thread safety. If the world is not loaded, Bukkit will handle setting
     * the difficulty from the world's `level.dat` file upon load.
     *
     * @param realm The realm object with the target difficulty. Must not be null.
     */
    public void applyWorldDifficulty(Realm realm) {
        if (realm == null) {
            plugin.getLogger().warning("Attempted to apply difficulty for a null realm.");
            return;
        }

        // Run on main thread to ensure API safety
        Bukkit.getScheduler().runTask(plugin, () -> {
            World world = Bukkit.getWorld(realm.getWorldName());
            if (world != null) {
                try {
                    org.bukkit.Difficulty difficulty = org.bukkit.Difficulty.valueOf(realm.getDifficulty().toUpperCase());
                    world.setDifficulty(difficulty);
                    plugin.getLogger().info("Set difficulty for world '" + world.getName() + "' to " + difficulty.name());
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid difficulty string '" + realm.getDifficulty() + "' for realm " + realm.getName() + ".");
                }
            }
        });
    }

    /**
     * Iterates through all players in a given world and checks if they are outside
     * the world's border. If a player is found outside, they are teleported to a safe
     * location (the world's spawn point) to prevent them from being stuck.
     * <p>
     * This operation is scheduled for the next server tick to avoid potential conflicts
     * during the border change itself.
     *
     * @param world The world to check players in. Must not be null.
     */
    private void checkAndRelocatePlayers(World world) {
        if (world == null) return;

        WorldBorder border = world.getWorldBorder();
        Location safeLocation = world.getSpawnLocation(); // Define a safe fallback location

        for (Player player : world.getPlayers()) {
            if (!border.isInside(player.getLocation())) {
                // The player is outside the new border, teleport them to safety.
                // We run this on the next tick to ensure all server processes are stable.
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.teleport(safeLocation);
                    plugin.getLogger().info("Relocated player " + player.getName() + " to a safe location in world " + world.getName() + ".");
                    // Optionally, send a message to the player
                    // player.sendMessage("You were moved to a safe location as the world border changed.");
                });
            }
        }
    }
}