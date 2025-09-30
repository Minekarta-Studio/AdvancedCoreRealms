package com.minekarta.advancedcorerealms.upgrades;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.config.ConfigManager;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.economy.EconomyService;
import com.minekarta.advancedcorerealms.manager.LanguageManager;
import com.minekarta.advancedcorerealms.manager.RealmManager;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.logging.Level;

/**
 * Handles the logic for purchasing various realm upgrades.
 * It coordinates between the player, economy, configuration, and realm data.
 */
public class UpgradeManager {

    private final AdvancedCoreRealms plugin;
    private final EconomyService economyService;
    private final ConfigManager configManager;
    private final RealmManager realmManager;
    private final LanguageManager languageManager;

    public UpgradeManager(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.economyService = plugin.getEconomyService();
        this.configManager = plugin.getConfigManager();
        this.realmManager = plugin.getRealmManager();
        this.languageManager = plugin.getLanguageManager();
    }

    // NOTE: All upgrade definitions (tiers, costs) are now loaded and managed by ConfigManager.
    // This class is now only responsible for the *process* of upgrading.

    /**
     * Handles the purchase of a difficulty upgrade for a realm.
     *
     * @param player The player purchasing the upgrade.
     * @param realm The realm being upgraded.
     * @param newDifficulty The target difficulty level (e.g., "hard").
     */
    public void purchaseDifficultyUpgrade(Player player, Realm realm, String newDifficulty) {
        // In a full implementation, cost would come from ConfigManager
        double cost = 5000; // Example cost

        if (realm.getDifficulty().equalsIgnoreCase(newDifficulty)) {
            languageManager.sendMessage(player, "upgrade.already-owned");
            return;
        }

        if (!economyService.hasBalance(player, cost)) {
            languageManager.sendMessage(player, "error.border.insufficient_funds", "%cost%", String.valueOf(cost));
            return;
        }

        if (economyService.withdraw(player, cost)) {
            String oldDifficulty = realm.getDifficulty();
            realm.setDifficulty(newDifficulty.toLowerCase());

            // Asynchronously save the updated realm data
            realmManager.updateRealm(realm).thenRun(() -> {
                // Apply the change to the live world on the main thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    World world = realm.getBukkitWorld();
                    if (world != null) {
                        try {
                            world.setDifficulty(Difficulty.valueOf(newDifficulty.toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().log(Level.WARNING, "Invalid difficulty set for upgrade: " + newDifficulty, e);
                        }
                    }
                });
                languageManager.sendMessage(player, "upgrade.success", "%upgrade%", "Difficulty", "%new_value%", newDifficulty);
            }).exceptionally(ex -> {
                plugin.getLogger().log(Level.SEVERE, "Failed to apply difficulty upgrade for realm " + realm.getName() + ". Refunding player.", ex);
                economyService.deposit(player, cost); // Refund on failure
                realm.setDifficulty(oldDifficulty); // Revert in-memory change
                languageManager.sendMessage(player, "upgrade.failure-refunded");
                return null;
            });
        }
    }

    // Other upgrade purchase methods like for member slots or keep-loaded would go here,
    // following a similar pattern of checking requirements, handling economy, and updating the realm.
    // For now, they are omitted to resolve the immediate compilation errors.
}