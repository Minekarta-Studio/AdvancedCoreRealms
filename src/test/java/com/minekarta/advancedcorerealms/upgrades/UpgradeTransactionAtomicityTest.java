package com.minekarta.advancedcorerealms.upgrades;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.economy.EconomyService;
import com.minekarta.advancedcorerealms.manager.LanguageManager;
import com.minekarta.advancedcorerealms.manager.RealmManager;
import com.minekarta.advancedcorerealms.worldborder.WorldBorderService;
import com.minekarta.advancedcorerealms.worldborder.WorldBorderTier;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpgradeTransactionAtomicityTest {

    @Mock private AdvancedCoreRealms plugin;
    @Mock private EconomyService economyService;
    @Mock private RealmManager realmManager;
    @Mock private WorldBorderService worldBorderService;
    @Mock private LanguageManager languageManager;
    @Mock private Player player;
    @Mock private PluginManager pluginManager;
    @Mock private org.bukkit.Server server;
    @Mock private java.util.logging.Logger logger;

    private UpgradeManager upgradeManager;

    private Realm testRealm;
    private WorldBorderTier nextTier;
    private WorldBorderTier initialTier;

    @BeforeEach
    void setUp() {
        // Initialize Realm and Tiers
        testRealm = new Realm("TestRealm", UUID.randomUUID(), "world_test", "default");
        initialTier = new WorldBorderTier("tier_50", 50, 0, 0, 10, 15, 0, 0);
        nextTier = new WorldBorderTier("tier_100", 100, 0, 0, 10, 15, 10, 5000);

        testRealm.setBorderTierId(initialTier.getId());
        testRealm.setBorderSize((int) initialTier.getSize());

        // Setup Mocks
        lenient().when(plugin.getEconomyService()).thenReturn(economyService);
        lenient().when(plugin.getRealmManager()).thenReturn(realmManager);
        lenient().when(plugin.getWorldBorderService()).thenReturn(worldBorderService);
        lenient().when(plugin.getLanguageManager()).thenReturn(languageManager);
        lenient().when(plugin.getServer()).thenReturn(server);
        lenient().when(server.getPluginManager()).thenReturn(pluginManager);
        lenient().when(plugin.getLogger()).thenReturn(logger);

        // Initialize UpgradeManager and load it with test data
        upgradeManager = new UpgradeManager(plugin);
        upgradeManager.getBorderTiers().addAll(List.of(initialTier, nextTier));
    }

    @Test
    @DisplayName("Should refund player when persistence fails after withdrawal")
    void purchaseUpgrade_whenPersistenceFails_refundsPlayer() {
        when(economyService.isEnabled()).thenReturn(true);
        when(economyService.hasBalance(any(Player.class), anyDouble())).thenReturn(true);
        when(economyService.withdraw(any(Player.class), eq(nextTier.getPrice()))).thenReturn(true);

        // Mock the persistence step to return a failed future
        when(realmManager.updateRealm(any(Realm.class)))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Simulated database connection error")));

        upgradeManager.purchaseBorderUpgrade(player, testRealm, nextTier);

        verify(economyService, times(1)).deposit(player, nextTier.getPrice());
        assertEquals(initialTier.getSize(), testRealm.getBorderSize(), "Border size should be reverted to its original value");
        assertEquals(initialTier.getId(), testRealm.getBorderTierId(), "Border tier ID should be reverted");
        verify(languageManager, times(1)).sendMessage(player, "upgrade.failure-refunded");
    }

    @Test
    @DisplayName("Should not apply upgrade if withdrawal fails")
    void purchaseUpgrade_whenWithdrawalFails_doesNotApplyUpgrade() {
        when(economyService.isEnabled()).thenReturn(true);
        when(economyService.hasBalance(any(Player.class), anyDouble())).thenReturn(true);
        when(economyService.withdraw(any(Player.class), eq(nextTier.getPrice()))).thenReturn(false);

        upgradeManager.purchaseBorderUpgrade(player, testRealm, nextTier);

        assertEquals(initialTier.getSize(), testRealm.getBorderSize(), "Border size should remain unchanged");
        verify(realmManager, never()).updateRealm(any(Realm.class));
        verify(economyService, never()).deposit(any(Player.class), anyDouble());
        verify(languageManager, times(1)).sendMessage(player, "economy.withdrawal-failed");
    }
}