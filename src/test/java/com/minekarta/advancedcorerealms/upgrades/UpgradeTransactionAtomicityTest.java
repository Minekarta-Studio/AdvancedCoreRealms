package com.minekarta.advancedcorerealms.upgrades;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.economy.EconomyService;
import com.minekarta.advancedcorerealms.manager.LanguageManager;
import com.minekarta.advancedcorerealms.manager.RealmManager;
import com.minekarta.advancedcorerealms.manager.world.WorldManager;
import com.minekarta.advancedcorerealms.upgrades.definitions.BorderTier;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    @Mock private WorldManager worldManager;
    @Mock private LanguageManager languageManager;
    @Mock private Player player;
    @Mock private PluginManager pluginManager;
    @Mock private org.bukkit.Server server;
    @Mock private java.util.logging.Logger logger;

    private UpgradeManager upgradeManager;

    private Realm testRealm;
    private BorderTier nextTier;

    @BeforeEach
    void setUp() {
        testRealm = new Realm("TestRealm", UUID.randomUUID(), "world_test", "default");
        testRealm.setBorderSize(50);
        nextTier = new BorderTier("tier_100", 100, 5000);

        lenient().when(plugin.getEconomyService()).thenReturn(economyService);
        lenient().when(plugin.getRealmManager()).thenReturn(realmManager);
        lenient().when(plugin.getWorldManager()).thenReturn(worldManager);
        lenient().when(plugin.getLanguageManager()).thenReturn(languageManager);
        lenient().when(plugin.getServer()).thenReturn(server);
        lenient().when(server.getPluginManager()).thenReturn(pluginManager);
        lenient().when(plugin.getLogger()).thenReturn(logger);

        upgradeManager = new UpgradeManager(plugin);
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
        assertEquals(50, testRealm.getBorderSize(), "Border size should be reverted to its original value");
        assertNotEquals(nextTier.getId(), testRealm.getBorderTierId(), "Border tier ID should be reverted");
        verify(languageManager, times(1)).sendMessage(player, "upgrade.failure-refunded");
    }

    @Test
    @DisplayName("Should not apply upgrade if withdrawal fails")
    void purchaseUpgrade_whenWithdrawalFails_doesNotApplyUpgrade() {
        when(economyService.isEnabled()).thenReturn(true);
        when(economyService.hasBalance(any(Player.class), anyDouble())).thenReturn(true);
        when(economyService.withdraw(any(Player.class), eq(nextTier.getPrice()))).thenReturn(false);

        upgradeManager.purchaseBorderUpgrade(player, testRealm, nextTier);

        assertEquals(50, testRealm.getBorderSize(), "Border size should remain unchanged");
        verify(realmManager, never()).updateRealm(any(Realm.class));
        verify(economyService, never()).deposit(any(Player.class), anyDouble());
        verify(languageManager, times(1)).sendMessage(player, "economy.withdrawal-failed");
    }
}