package com.minekarta.advancedcorerealms.upgrades;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.economy.EconomyService;
import com.minekarta.advancedcorerealms.upgrades.definitions.BorderTier;
import com.minekarta.advancedcorerealms.upgrades.definitions.DifficultyUpgrade;
import com.minekarta.advancedcorerealms.upgrades.definitions.KeepLoadedUpgrade;
import com.minekarta.advancedcorerealms.upgrades.definitions.MemberSlotTier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import com.minekarta.advancedcorerealms.api.events.RealmUpgradeEvent;
import com.minekarta.advancedcorerealms.api.events.RealmUpgradedEvent;
import org.bukkit.Bukkit;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class UpgradeManager {

    private final AdvancedCoreRealms plugin;
    private final EconomyService economyService;
    private final Map<String, ReentrantLock> realmLocks = new ConcurrentHashMap<>();

    // Upgrade Definitions
    private List<BorderTier> borderTiers = new ArrayList<>();
    private List<MemberSlotTier> memberSlotTiers = new ArrayList<>();
    private Map<String, DifficultyUpgrade> difficultyUpgrades = new ConcurrentHashMap<>();
    private KeepLoadedUpgrade keepLoadedUpgrade;

    public UpgradeManager(AdvancedCoreRealms plugin) {
        this.plugin = plugin;
        this.economyService = plugin.getEconomyService();
    }

    public void loadUpgrades() {
        // Clear existing definitions
        borderTiers.clear();
        memberSlotTiers.clear();
        difficultyUpgrades.clear();

        ConfigurationSection upgradeConfig = plugin.getConfig().getConfigurationSection("upgrades");
        if (upgradeConfig == null) {
            plugin.getLogger().warning("Could not find 'upgrades' section in config.yml. No upgrades will be available.");
            return;
        }

        // Load Border Tiers
        ConfigurationSection borderTiersSection = upgradeConfig.getConfigurationSection("border_tiers");
        if (borderTiersSection != null) {
            for (String key : borderTiersSection.getKeys(false)) {
                ConfigurationSection tierSection = borderTiersSection.getConfigurationSection(key);
                if (tierSection != null) {
                    String id = tierSection.getString("id");
                    int size = tierSection.getInt("size");
                    double price = tierSection.getDouble("price");
                    borderTiers.add(new BorderTier(id, size, price));
                }
            }
            borderTiers.sort(Comparator.comparingInt(BorderTier::getSize));
            plugin.getLogger().info("Loaded " + borderTiers.size() + " border tiers.");
        }

        // Load Member Slot Tiers
        ConfigurationSection memberSlotsSection = upgradeConfig.getConfigurationSection("member_slots");
        if (memberSlotsSection != null) {
            for (String key : memberSlotsSection.getKeys(false)) {
                ConfigurationSection tierSection = memberSlotsSection.getConfigurationSection(key);
                if (tierSection != null) {
                    String id = "tier_" + key; // Generate an id
                    int additional = tierSection.getInt("additional");
                    double price = tierSection.getDouble("price");
                    memberSlotTiers.add(new MemberSlotTier(id, additional, price));
                }
            }
            memberSlotTiers.sort(Comparator.comparingInt(MemberSlotTier::getAdditionalSlots));
            plugin.getLogger().info("Loaded " + memberSlotTiers.size() + " member slot tiers.");
        }

        // Load Difficulty Upgrades
        ConfigurationSection difficultySection = upgradeConfig.getConfigurationSection("difficulty");
        if (difficultySection != null) {
            for (String difficulty : difficultySection.getKeys(false)) {
                double price = difficultySection.getDouble(difficulty);
                difficultyUpgrades.put(difficulty.toLowerCase(), new DifficultyUpgrade(difficulty, price));
            }
            plugin.getLogger().info("Loaded " + difficultyUpgrades.size() + " difficulty upgrades.");
        }
        
        // Load Keep Loaded Upgrade
        ConfigurationSection keepLoadedSection = upgradeConfig.getConfigurationSection("keep_loaded");
        if (keepLoadedSection != null) {
            double price = keepLoadedSection.getDouble("price_per_day"); // Assuming this is the intended key
            keepLoadedUpgrade = new KeepLoadedUpgrade(price);
            plugin.getLogger().info("Loaded keep_loaded upgrade with price: " + price);
        }
    }

    // Getters for upgrade definitions
    public List<BorderTier> getBorderTiers() {
        return borderTiers;
    }

    public List<MemberSlotTier> getMemberSlotTiers() {
        return memberSlotTiers;
    }

    public Map<String, DifficultyUpgrade> getDifficultyUpgrades() {
        return difficultyUpgrades;
    }

    public KeepLoadedUpgrade getKeepLoadedUpgrade() {
        return keepLoadedUpgrade;
    }

    // Logic to get next available upgrades
    public Optional<BorderTier> getNextBorderTier(Realm realm) {
        int currentSize = realm.getBorderSize();
        return borderTiers.stream()
                .filter(tier -> tier.getSize() > currentSize)
                .min(Comparator.comparingInt(BorderTier::getSize));
    }

    public Optional<MemberSlotTier> getNextMemberSlotTier(Realm realm) {
        // This logic assumes maxPlayers is base + additional
        int basePlayers = plugin.getRealmConfig().getBaseMaxPlayers(); // Assuming a base value exists
        int currentAdditional = realm.getMaxPlayers() - basePlayers;
        return memberSlotTiers.stream()
                .filter(tier -> tier.getAdditionalSlots() > currentAdditional)
                .min(Comparator.comparingInt(MemberSlotTier::getAdditionalSlots));
    }

    // Purchase logic will be added here
    public void purchaseBorderUpgrade(Player player, Realm realm, BorderTier tier) {
        ReentrantLock lock = realmLocks.computeIfAbsent(realm.getName(), k -> new ReentrantLock());
        lock.lock();
        try {
            // --- Prerequisite Checks ---
            if (realm.getBorderSize() >= tier.getSize()) {
                plugin.getLanguageManager().sendMessage(player, "upgrade.already-owned");
                return;
            }

            if (!economyService.isEnabled()) {
                plugin.getLanguageManager().sendMessage(player, "economy.disabled");
                return;
            }

            if (!economyService.hasBalance(player, tier.getPrice())) {
                plugin.getLanguageManager().sendMessage(player, "economy.not-enough-funds",
                        "%price%", economyService.format(tier.getPrice()),
                        "%balance%", economyService.format(economyService.getBalance(player)));
                return;
            }

            // --- Pre-Purchase Event ---
            RealmUpgradeEvent preEvent = new RealmUpgradeEvent(player, realm, "border", tier.getId(), tier.getPrice());
            plugin.getServer().getPluginManager().callEvent(preEvent);
            if (preEvent.isCancelled()) {
                plugin.getLanguageManager().sendMessage(player, "upgrade.cancelled-by-plugin");
                return;
            }

            // --- Atomic Transaction Start ---
            if (!economyService.withdraw(player, tier.getPrice())) {
                plugin.getLanguageManager().sendMessage(player, "economy.withdrawal-failed");
                return;
            }

            String oldTierId = realm.getBorderTierId();
            int oldBorderSize = realm.getBorderSize();

            try {
                // Apply change to Realm object
                realm.setBorderTierId(tier.getId());
                realm.setBorderSize(tier.getSize());

                // Apply change to the live world
                plugin.getWorldManager().updateWorldBorder(realm);

                // Persist the changes
                plugin.getWorldDataManager().saveDataAsync();

                // --- Success ---
                plugin.getLanguageManager().sendMessage(player, "upgrade.success", "%upgrade%", "Border Tier", "%new_value%", String.valueOf(tier.getSize()));

                // Fire post-purchase event
                RealmUpgradedEvent postEvent = new RealmUpgradedEvent(player, realm, "border", oldTierId, tier.getId(), tier.getPrice());
                plugin.getServer().getPluginManager().callEvent(postEvent);

                plugin.getTransactionLogger().log(realm.getName(), player.getUniqueId(), "border", oldTierId, tier.getId(), tier.getPrice());

            } catch (Exception e) {
                // --- Failure: Rollback and Refund ---
                plugin.getLogger().severe("Failed to apply or save border upgrade for realm " + realm.getName() + ". Refunding player " + player.getName());
                e.printStackTrace();

                // Refund player
                economyService.deposit(player, tier.getPrice());

                // Revert in-memory changes
                realm.setBorderTierId(oldTierId);
                realm.setBorderSize(oldBorderSize);

                // Attempt to revert live world change
                plugin.getWorldManager().updateWorldBorder(realm);

                plugin.getLanguageManager().sendMessage(player, "upgrade.failure-refunded");
            }

        } finally {
            lock.unlock();
        }
    }

    public void purchaseKeepLoadedUpgrade(Player player, Realm realm, KeepLoadedUpgrade upgrade) {
        ReentrantLock lock = realmLocks.computeIfAbsent(realm.getName(), k -> new ReentrantLock());
        lock.lock();
        try {
            if (realm.isKeepLoaded()) {
                plugin.getLanguageManager().sendMessage(player, "upgrade.already-owned");
                return;
            }

            if (!economyService.isEnabled() || !economyService.hasBalance(player, upgrade.getPrice())) {
                plugin.getLanguageManager().sendMessage(player, "economy.not-enough-funds",
                        "%price%", economyService.format(upgrade.getPrice()),
                        "%balance%", economyService.format(economyService.getBalance(player)));
                return;
            }

            if (!economyService.withdraw(player, upgrade.getPrice())) {
                plugin.getLanguageManager().sendMessage(player, "economy.withdrawal-failed");
                return;
            }

            try {
                realm.setKeepLoaded(true);
                // No direct world action needed, but maybe a placeholder for future logic
                // plugin.getWorldManager().updateKeepLoadedState(realm);
                plugin.getWorldDataManager().saveDataAsync();

                plugin.getLanguageManager().sendMessage(player, "upgrade.success", "%upgrade%", "Keep Loaded", "%new_value%", "Enabled");
                RealmUpgradedEvent postEvent = new RealmUpgradedEvent(player, realm, "keepLoaded", "false", "true", upgrade.getPrice());
                plugin.getServer().getPluginManager().callEvent(postEvent);

                plugin.getTransactionLogger().log(realm.getName(), player.getUniqueId(), "keepLoaded", "false", "true", upgrade.getPrice());

            } catch (Exception e) {
                plugin.getLogger().severe("Failed to apply keepLoaded upgrade for realm " + realm.getName() + ". Refunding player.");
                e.printStackTrace();
                economyService.deposit(player, upgrade.getPrice());
                realm.setKeepLoaded(false);
                plugin.getLanguageManager().sendMessage(player, "upgrade.failure-refunded");
            }
        } finally {
            lock.unlock();
        }
    }

    public void purchaseDifficultyUpgrade(Player player, Realm realm, DifficultyUpgrade upgrade) {
        ReentrantLock lock = realmLocks.computeIfAbsent(realm.getName(), k -> new ReentrantLock());
        lock.lock();
        try {
            if (realm.getDifficulty().equalsIgnoreCase(upgrade.getId())) {
                plugin.getLanguageManager().sendMessage(player, "upgrade.already-owned");
                return;
            }

            if (!economyService.isEnabled() || !economyService.hasBalance(player, upgrade.getPrice())) {
                plugin.getLanguageManager().sendMessage(player, "economy.not-enough-funds",
                        "%price%", economyService.format(upgrade.getPrice()),
                        "%balance%", economyService.format(economyService.getBalance(player)));
                return;
            }

            if (!economyService.withdraw(player, upgrade.getPrice())) {
                plugin.getLanguageManager().sendMessage(player, "economy.withdrawal-failed");
                return;
            }

            String oldDifficulty = realm.getDifficulty();

            try {
                realm.setDifficulty(upgrade.getId());
                plugin.getWorldManager().updateWorldDifficulty(realm);
                plugin.getWorldDataManager().saveDataAsync();

                plugin.getLanguageManager().sendMessage(player, "upgrade.success", "%upgrade%", "Difficulty", "%new_value%", upgrade.getId());
                RealmUpgradedEvent postEvent = new RealmUpgradedEvent(player, realm, "difficulty", oldDifficulty, upgrade.getId(), upgrade.getPrice());
                plugin.getServer().getPluginManager().callEvent(postEvent);

                plugin.getTransactionLogger().log(realm.getName(), player.getUniqueId(), "difficulty", oldDifficulty, upgrade.getId(), upgrade.getPrice());

            } catch (Exception e) {
                plugin.getLogger().severe("Failed to apply difficulty upgrade for realm " + realm.getName() + ". Refunding player.");
                e.printStackTrace();
                economyService.deposit(player, upgrade.getPrice());
                realm.setDifficulty(oldDifficulty);
                plugin.getWorldManager().updateWorldDifficulty(realm);
                plugin.getLanguageManager().sendMessage(player, "upgrade.failure-refunded");
            }
        } finally {
            lock.unlock();
        }
    }

    public void purchaseMemberSlotUpgrade(Player player, Realm realm, MemberSlotTier tier) {
        ReentrantLock lock = realmLocks.computeIfAbsent(realm.getName(), k -> new ReentrantLock());
        lock.lock();
        try {
            int basePlayers = plugin.getRealmConfig().getBaseMaxPlayers();
            int currentAdditional = realm.getMaxPlayers() - basePlayers;
            if (tier.getAdditionalSlots() <= currentAdditional) {
                plugin.getLanguageManager().sendMessage(player, "upgrade.already-owned");
                return;
            }

            if (!economyService.isEnabled() || !economyService.hasBalance(player, tier.getPrice())) {
                 plugin.getLanguageManager().sendMessage(player, "economy.not-enough-funds",
                        "%price%", economyService.format(tier.getPrice()),
                        "%balance%", economyService.format(economyService.getBalance(player)));
                return;
            }

            if (!economyService.withdraw(player, tier.getPrice())) {
                plugin.getLanguageManager().sendMessage(player, "economy.withdrawal-failed");
                return;
            }

            String oldTierId = realm.getMemberSlotTierId();
            int oldMaxPlayers = realm.getMaxPlayers();

            try {
                realm.setMemberSlotTierId(tier.getId());
                realm.setMaxPlayers(basePlayers + tier.getAdditionalSlots());
                plugin.getWorldDataManager().saveDataAsync();

                plugin.getLanguageManager().sendMessage(player, "upgrade.success", "%upgrade%", "Member Slots", "%new_value%", String.valueOf(realm.getMaxPlayers()));
                RealmUpgradedEvent postEvent = new RealmUpgradedEvent(player, realm, "members", oldTierId, tier.getId(), tier.getPrice());
                plugin.getServer().getPluginManager().callEvent(postEvent);

                plugin.getTransactionLogger().log(realm.getName(), player.getUniqueId(), "members", oldTierId, tier.getId(), tier.getPrice());

            } catch (Exception e) {
                plugin.getLogger().severe("Failed to apply member slot upgrade for realm " + realm.getName() + ". Refunding player.");
                e.printStackTrace();
                economyService.deposit(player, tier.getPrice());
                realm.setMemberSlotTierId(oldTierId);
                realm.setMaxPlayers(oldMaxPlayers);
                plugin.getLanguageManager().sendMessage(player, "upgrade.failure-refunded");
            }
        } finally {
            lock.unlock();
        }
    }
}