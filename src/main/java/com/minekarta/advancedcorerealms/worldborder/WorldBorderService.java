package com.minekarta.advancedcorerealms.worldborder;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.manager.LanguageManager;
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
 * Service responsible for all direct interactions with the Bukkit {@link WorldBorder} API.
 * This class applies border settings to game worlds, ensuring all API calls are thread-safe.
 */
public class WorldBorderService implements Listener {

    private final AdvancedCoreRealms plugin;
    private final LanguageManager lang;
    private final ConcurrentHashMap<String, Realm> pendingRealms = new ConcurrentHashMap<>();
    private static final long BORDER_TRANSITION_SECONDS = 10; // 10-second transition for all border changes

    public WorldBorderService(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLanguageManager();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Safely applies a world border of a given size to a realm's world.
     * If the world is loaded, it applies the changes immediately. If not, it queues the update.
     *
     * @param realm The realm object containing the world information.
     * @param newSize The target diameter of the world border.
     */
    public void applyWorldBorder(Realm realm, double newSize) {
        if (realm == null) {
            plugin.getLogger().warning("Attempted to apply world border with a null realm.");
            return;
        }

        String worldFolderName = realm.getWorldFolderName();
        World world = Bukkit.getWorld(worldFolderName);
        if (world != null) {
            plugin.getLogger().info("Applying world border size " + newSize + " to loaded world: " + worldFolderName);
            updateBorder(world, realm, newSize);
        } else {
            plugin.getLogger().info("World " + worldFolderName + " is not loaded. Queuing border update.");
            pendingRealms.put(worldFolderName, realm);
        }
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        String worldName = event.getWorld().getName();
        Realm realm = pendingRealms.remove(worldName);
        if (realm != null) {
            plugin.getLogger().info("Applying queued world border for newly loaded world: " + worldName);
            // Re-delegate to the manager to ensure the latest tier info is used
            plugin.getWorldBorderManager().applyBorder(realm);
        }
    }

    /**
     * Core private method that performs the actual border update on a given world.
     *
     * @param world The world where the border will be updated.
     * @param realm The realm data, used to get the center coordinates.
     * @param newSize The target size of the border.
     */
    private void updateBorder(World world, Realm realm, double newSize) {
        if (world == null || realm == null) {
            plugin.getLogger().warning("updateBorder called with null world or realm.");
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            WorldBorder border = world.getWorldBorder();
            border.setCenter(realm.getBorderCenterX(), realm.getBorderCenterZ());
            border.setWarningDistance(10); // Standard warning distance
            border.setWarningTime(15);   // Standard warning time

            if (newSize > 0) {
                border.setSize(newSize, BORDER_TRANSITION_SECONDS);
            } else {
                plugin.getLogger().warning("Invalid border size (" + newSize + ") for realm " + realm.getName() + ". No size change.");
            }

            plugin.getLogger().info("Successfully applied border to " + world.getName() +
                    ". New size: " + newSize + " (transitioning over " + BORDER_TRANSITION_SECONDS + "s)");

            long delayTicks = BORDER_TRANSITION_SECONDS * 20L;
            Bukkit.getScheduler().runTaskLater(plugin, () -> checkAndRelocatePlayers(world), delayTicks + 20L);
        });
    }

    /**
     * Iterates through all players in a given world and checks if they are outside
     * the world's border. If so, they are teleported to a safe location.
     *
     * @param world The world to check players in.
     */
    private void checkAndRelocatePlayers(World world) {
        if (world == null) return;

        WorldBorder border = world.getWorldBorder();
        Location safeLocation = border.getCenter().toLocation(world);
        safeLocation.setY(world.getHighestBlockYAt(safeLocation) + 1.5);

        for (Player player : world.getPlayers()) {
            if (!border.isInside(player.getLocation())) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.teleport(safeLocation);
                    plugin.getLogger().info("Relocated player " + player.getName() + " in world " + world.getName() + ".");
                    lang.sendMessage(player, "world_border.relocated");
                });
            }
        }
    }
}