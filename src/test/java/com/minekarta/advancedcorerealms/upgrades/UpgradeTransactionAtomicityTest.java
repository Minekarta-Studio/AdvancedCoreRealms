package com.minekarta.advancedcorerealms.upgrades;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.data.WorldDataManager;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.economy.EconomyService;
import com.minekarta.advancedcorerealms.manager.LanguageManager;
import com.minekarta.advancedcorerealms.manager.world.WorldManager;
import com.minekarta.advancedcorerealms.upgrades.definitions.BorderTier;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpgradeTransactionAtomicityTest {

    @Mock private AdvancedCoreRealms plugin;
    @Mock private EconomyService economyService;
    @Mock private WorldDataManager worldDataManager;
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
        // Given: A test realm and a target upgrade tier
        testRealm = new Realm("TestRealm", UUID.randomUUID(), "world_test", "default");
        testRealm.setBorderSize(50);
        nextTier = new BorderTier("tier_100", 100, 5000);

        // And: The plugin's managers are mocked and available
        lenient().when(plugin.getEconomyService()).thenReturn(economyService);
        lenient().when(plugin.getWorldDataManager()).thenReturn(worldDataManager);
        lenient().when(plugin.getWorldManager()).thenReturn(worldManager);
        lenient().when(plugin.getLanguageManager()).thenReturn(languageManager);
        lenient().when(plugin.getServer()).thenReturn(server);
        lenient().when(server.getPluginManager()).thenReturn(pluginManager);

        lenient().when(plugin.getLogger()).thenReturn(logger);

        // Manually instantiate UpgradeManager after mocks are set up
        upgradeManager = new UpgradeManager(plugin);
    }

    @Test
    @DisplayName("Should refund player when persistence fails after withdrawal")
    void purchaseUpgrade_whenPersistenceFails_refundsPlayer() {
        // Given: The economy service successfully withdraws money
        when(economyService.isEnabled()).thenReturn(true);
        when(economyService.hasBalance(any(Player.class), anyDouble())).thenReturn(true);
        when(economyService.withdraw(any(Player.class), eq(nextTier.getPrice()))).thenReturn(true);

        // And: The persistence step (saveDataAsync) will throw an exception
        doThrow(new RuntimeException("Simulated database connection error"))
                .when(worldDataManager).saveDataAsync();

        // When: The purchase process is initiated
        upgradeManager.purchaseBorderUpgrade(player, testRealm, nextTier);

        // Then: A refund is issued
        verify(economyService, times(1)).deposit(player, nextTier.getPrice());

        // And: The realm's in-memory state is reverted
        assertEquals(50, testRealm.getBorderSize(), "Border size should be reverted to its original value");
        assertNotEquals(nextTier.getId(), testRealm.getBorderTierId(), "Border tier ID should be reverted");

        // And: The player is notified of the failure and refund
        verify(languageManager, times(1)).sendMessage(player, "upgrade.failure-refunded");
    }

    @Test
    @DisplayName("Should not apply upgrade if withdrawal fails")
    void purchaseUpgrade_whenWithdrawalFails_doesNotApplyUpgrade() {
        // Given: The economy service fails to withdraw money
        when(economyService.isEnabled()).thenReturn(true);
        when(economyService.hasBalance(any(Player.class), anyDouble())).thenReturn(true);
        when(economyService.withdraw(any(Player.class), eq(nextTier.getPrice()))).thenReturn(false);

        // When: The purchase process is initiated
        upgradeManager.purchaseBorderUpgrade(player, testRealm, nextTier);

        // Then: The realm's state is not changed
        assertEquals(50, testRealm.getBorderSize(), "Border size should remain unchanged");

        // And: No attempt is made to save the data
        verify(worldDataManager, never()).saveDataAsync();

        // And: No refund is issued (since no money was taken)
        verify(economyService, never()).deposit(any(Player.class), anyDouble());

        // And: The player is notified of the withdrawal failure
        verify(languageManager, times(1)).sendMessage(player, "economy.withdrawal-failed");
    }
}