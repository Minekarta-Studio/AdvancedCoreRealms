package com.minekarta.advancedcorerealms.worldborder;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.config.ConfigManager;
import com.minekarta.advancedcorerealms.config.WorldBorderTier;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.economy.EconomyService;
import com.minekarta.advancedcorerealms.manager.LanguageManager;
import com.minekarta.advancedcorerealms.manager.RealmManager;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.logging.Level;

/**
 * Manages the logic for applying and upgrading world borders based on a tiered configuration.
 * This class orchestrates the interaction between configuration, economy, and the Bukkit world border API.
 */
public class WorldBorderManager {

    private final AdvancedCoreRealms plugin;
    private final ConfigManager configManager;
    private final RealmManager realmManager;
    private final EconomyService economyService;
    private final LanguageManager languageManager;
    private final WorldBorderService worldBorderService;

    public WorldBorderManager(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.realmManager = plugin.getRealmManager();
        this.economyService = plugin.getEconomyService();
        this.languageManager = plugin.getLanguageManager();
        this.worldBorderService = plugin.getWorldBorderService();
    }

    /**
     * Applies the world border to a realm based on its currently saved tier.
     *
     * @param realm The realm whose world border needs to be set or updated.
     */
    public void applyBorder(Realm realm) {
        if (realm == null) {
            plugin.getLogger().warning("Attempted to apply border for a null realm.");
            return;
        }

        WorldBorderTier tier = configManager.getTier(realm.getBorderTierId());
        if (tier == null) {
            plugin.getLogger().warning("Realm " + realm.getName() + " has an invalid border tier ID: '" + realm.getBorderTierId() + "'. Using default tier.");
            tier = configManager.getDefaultTier();
        }

        worldBorderService.applyWorldBorder(realm, tier.getSize());
    }

    /**
     * Attempts to upgrade a realm's world border to the next available tier.
     *
     * @param player The player initiating the upgrade.
     * @param realm The realm to upgrade.
     * @param targetTierId The ID of the desired new tier.
     */
    public void upgradeBorder(Player player, Realm realm, String targetTierId) {
        WorldBorderTier currentTier = configManager.getTier(realm.getBorderTierId());
        WorldBorderTier targetTier = configManager.getTier(targetTierId);

        if (targetTier == null) {
            languageManager.sendMessage(player, "error.border.invalid_tier");
            return;
        }

        if (currentTier != null && targetTier.getSize() <= currentTier.getSize()) {
            languageManager.sendMessage(player, "error.border.not_an_upgrade");
            return;
        }

        double cost = targetTier.getCost();
        if (!economyService.hasBalance(player, cost)) {
            languageManager.sendMessage(player, "error.border.insufficient_funds", "%cost%", String.valueOf(cost));
            return;
        }

        // Withdraw money and proceed with the upgrade
        if (economyService.withdraw(player, cost)) {
            realm.setBorderTierId(targetTier.getId());
            realm.setBorderSize(targetTier.getSize());

            realmManager.updateRealm(realm).thenRun(() -> {
                // Apply the new border and notify the player
                applyBorder(realm);
                languageManager.sendMessage(player, "success.border.upgraded", "%tier%", targetTier.getId(), "%size%", String.valueOf(targetTier.getSize()));

                // Notify other players in the world
                World world = realm.getBukkitWorld();
                if (world != null) {
                    String title = languageManager.getMessage("broadcast.border.title");
                    String subtitle = languageManager.getMessage("broadcast.border.subtitle")
                                                     .replace("%size%", String.valueOf(targetTier.getSize()));
                    world.getPlayers().forEach(p -> p.sendTitle(title, subtitle, 10, 70, 20));
                }
            }).exceptionally(ex -> {
                plugin.getLogger().log(Level.SEVERE, "Failed to save realm after border upgrade. Refunding player.", ex);
                economyService.deposit(player, cost); // Refund on failure
                languageManager.sendMessage(player, "error.border.upgrade_failed");
                return null;
            });
        } else {
            // This should theoretically not happen if the `has` check passed, but as a safeguard:
            languageManager.sendMessage(player, "error.border.economy_error");
        }
    }
}