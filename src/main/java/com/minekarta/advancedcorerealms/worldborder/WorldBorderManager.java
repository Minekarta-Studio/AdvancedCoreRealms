package com.minekarta.advancedcorerealms.worldborder;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;

import java.util.logging.Level;

/**
 * Manages the logic for applying and upgrading world borders based on a tiered configuration.
 * This class acts as the central hub for all world border operations, separating the
 * business logic from the direct Bukkit API interactions handled by {@link WorldBorderService}.
 */
public class WorldBorderManager {

    private final AdvancedCoreRealms plugin;
    private final WorldBorderConfig borderConfig;

    /**
     * Constructs the WorldBorderManager.
     *
     * @param plugin The main plugin instance, used to access configuration and other services.
     */
    public WorldBorderManager(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.borderConfig = plugin.getWorldBorderConfig();
    }

    /**
     * Applies the appropriate world border to a realm based on its currently configured tier.
     * This method fetches the tier from the realm's data, finds the corresponding tier
     * configuration, and then instructs the WorldBorderService to apply it.
     *
     * @param realm The realm whose world border needs to be set or updated.
     */
    public void applyBorder(Realm realm) {
        if (realm == null) {
            plugin.getLogger().warning("Attempted to apply border for a null realm.");
            return;
        }

        WorldBorderTier tier = borderConfig.getTier(realm.getBorderTierId());
        if (tier == null) {
            plugin.getLogger().warning("Realm " + realm.getName() + " has an invalid border tier ID: '" + realm.getBorderTierId() + "'. Using default tier.");
            tier = borderConfig.getDefaultTier();
            if (tier == null) {
                plugin.getLogger().severe("No default world border tier is configured. Cannot apply border for realm " + realm.getName() + ".");
                return;
            }
        }

        // Delegate the actual border application to the service that handles Bukkit API calls
        plugin.getWorldBorderService().applyWorldBorder(realm, tier);
    }

    /**
     * Sets a realm's world border to a new tier, handling the full update process.
     * <p>
     * This method orchestrates the entire border change operation:
     * <ol>
     *     <li>Validates that the target tier exists.</li>
     *     <li>Updates the {@link Realm} object with the new tier ID.</li>
     *     <li>Saves the updated realm data asynchronously to the database.</li>
     *     <li>Delegates to the {@link WorldBorderService} to apply the physical border change.</li>
     *     <li>Notifies players in the world that a transition is beginning.</li>
     * </ol>
     *
     * @param realm        The realm whose border is to be changed.
     * @param targetTierId The unique identifier of the target {@link WorldBorderTier}.
     */
    public void setBorderTier(Realm realm, String targetTierId) {
        WorldBorderTier targetTier = borderConfig.getTier(targetTierId);
        if (targetTier == null) {
            plugin.getLogger().warning("Attempted to set border to a non-existent tier '" + targetTierId + "' for realm " + realm.getName());
            return;
        }

        // Update the realm's data to persist the new tier choice
        realm.setBorderTierId(targetTier.getId());

        // Asynchronously save the updated realm data. The RealmManager is designed
        // to handle database operations off the main thread.
        plugin.getRealmManager().updateRealm(realm);

        // Apply the new border
        applyBorder(realm);

        // Notify players that the transition is starting
        World world = realm.getBukkitWorld();
        if (world != null) {
            // These messages can be moved to the language file for full localization.
            String title = "§aWorld Border Changing";
            String subtitle = "§7New size: " + (int) targetTier.getSize() + " blocks. Transition: " + targetTier.getTransitionTime() + "s.";

            for (Player player : world.getPlayers()) {
                player.sendTitle(title, subtitle, 10, 70, 20); // fadeIn, stay, fadeOut in ticks
            }
        }
    }
}