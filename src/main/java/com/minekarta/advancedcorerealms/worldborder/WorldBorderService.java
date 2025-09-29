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
 * <p>
 * This class acts as the final layer in the world border system, taking validated
 * {@link WorldBorderTier} objects and applying their properties to the game world.
 * Its key responsibilities include:
 * <ul>
 *     <li>Ensuring all Bukkit API calls related to world borders are executed on the main server thread via the scheduler.</li>
 *     <li>Implementing the smooth, timed transition for border size changes.</li>
 *     <li>Handling the queuing of border updates for worlds that are not yet loaded.</li>
 *     <li>Performing safety checks to relocate players who are outside a shrinking border.</li>
 * </ul>
 * It listens for {@link WorldLoadEvent} to apply queued updates reliably.
 */
public class WorldBorderService implements Listener {

    private final AdvancedCoreRealms plugin;
    private final LanguageManager lang;
    private final ConcurrentHashMap<String, Realm> pendingRealms = new ConcurrentHashMap<>();

    public WorldBorderService(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLanguageManager();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Safely applies the border settings from a {@link WorldBorderTier} object to a realm's world.
     * <p>
     * This is the primary entry point for all world border updates. It determines if the
     * world is loaded. If so, it applies the changes immediately. If not, it queues
     * the update to be applied when the world loads via {@link #onWorldLoad(WorldLoadEvent)}.
     *
     * @param realm The realm object containing the world information.
     * @param tier  The specific border tier configuration to apply.
     */
    public void applyWorldBorder(Realm realm, WorldBorderTier tier) {
        if (realm == null || tier == null) {
            plugin.getLogger().warning("Attempted to apply world border with a null realm or tier.");
            return;
        }

        World world = Bukkit.getWorld(realm.getWorldName());
        if (world != null) {
            plugin.getLogger().info("Applying world border tier '" + tier.getId() + "' to loaded world: " + realm.getWorldName());
            updateBorder(world, tier);
        } else {
            plugin.getLogger().info("World " + realm.getWorldName() + " is not loaded. Queuing border update.");
            // We only need the realm to identify the world later. The manager will re-fetch the tier.
            pendingRealms.put(realm.getWorldName(), realm);
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
     * Core private method that performs the actual border update on a given world using a tier's properties.
     * This method handles the smooth transition via {@link WorldBorder#setSize(double, long)}.
     *
     * @param world The world where the border will be updated.
     * @param tier  The tier object containing the new border settings.
     */
    private void updateBorder(World world, WorldBorderTier tier) {
        if (world == null || tier == null) {
            plugin.getLogger().warning("updateBorder called with null world or tier.");
            return;
        }

        // All Bukkit API calls should be on the main thread.
        Bukkit.getScheduler().runTask(plugin, () -> {
            WorldBorder border = world.getWorldBorder();

            // Set properties from the tier
            border.setCenter(tier.getCenterX(), tier.getCenterZ());
            border.setWarningDistance(tier.getWarningDistance());
            border.setWarningTime(tier.getWarningTime());

            // Validate and set size with a smooth transition
            if (tier.getSize() > 0) {
                border.setSize(tier.getSize(), tier.getTransitionTime());
            } else {
                plugin.getLogger().warning("Invalid border size (" + tier.getSize() + ") for tier " + tier.getId() + ". No size change.");
            }

            plugin.getLogger().info("Successfully applied border tier '" + tier.getId() + "' to " + world.getName() +
                    ". New size: " + tier.getSize() + " (transitioning over " + tier.getTransitionTime() + "s)");

            // Schedule completion notification and player relocation check to run after the transition
            long delayTicks = tier.getTransitionTime() * 20L; // Convert seconds to server ticks
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Notify players that the transition is complete
                // These messages can be moved to the language file for full localization.
                String title = "§aBorder Change Complete";
                String subtitle = "§7The world border is now " + (int) tier.getSize() + " blocks wide.";
                for (Player player : world.getPlayers()) {
                    player.sendTitle(title, subtitle, 10, 70, 20);
                }

                // Check for any players outside the new border
                checkAndRelocatePlayers(world);

            }, delayTicks + 20L); // Add 1s buffer
        });
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
        // Use the border's center as the guaranteed safe location.
        // We add a small offset to Y to ensure the player doesn't spawn underground.
        Location safeLocation = border.getCenter().toLocation(world);
        safeLocation.setY(world.getHighestBlockYAt(safeLocation) + 1.5);


        for (Player player : world.getPlayers()) {
            // The check itself can be done on the current thread, but the teleport should be on the main thread.
            if (!border.isInside(player.getLocation())) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.teleport(safeLocation);
                    plugin.getLogger().info("Relocated player " + player.getName() + " to the border's center in world " + world.getName() + ".");
                    // Send a message to the player, assuming the key "world_border.relocated" exists in the lang file.
                    lang.sendMessage(player, "world_border.relocated");
                });
            }
        }
    }
}