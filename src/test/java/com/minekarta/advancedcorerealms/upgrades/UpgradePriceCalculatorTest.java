package com.minekarta.advancedcorerealms.upgrades;

import com.minekarta.advancedcorerealms.AdvancedCoreRealms;
import com.minekarta.advancedcorerealms.config.RealmConfig;
import com.minekarta.advancedcorerealms.data.object.Realm;
import com.minekarta.advancedcorerealms.worldborder.WorldBorderTier;
import com.minekarta.advancedcorerealms.upgrades.definitions.MemberSlotTier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpgradePriceCalculatorTest {

    @Mock
    private AdvancedCoreRealms plugin;

    @Mock
    private RealmConfig realmConfig;

    @InjectMocks
    private UpgradeManager upgradeManager;

    private Realm testRealm;

    @BeforeEach
    void setUp() {
        // Given: A list of border and member tiers is loaded into the UpgradeManager
        List<WorldBorderTier> borderTiers = List.of(
                new WorldBorderTier("tier_50", 50, 0, 0, 0, 0, 0, 0),
                new WorldBorderTier("tier_100", 100, 0, 0, 0, 0, 0, 5000),
                new WorldBorderTier("tier_150", 150, 0, 0, 0, 0, 0, 12000)
        );
        List<MemberSlotTier> memberSlotTiers = List.of(
                new MemberSlotTier("tier_0", 0, 0),
                new MemberSlotTier("tier_5", 5, 2500),
                new MemberSlotTier("tier_10", 10, 6000)
        );
        upgradeManager.getBorderTiers().addAll(borderTiers);
        upgradeManager.getMemberSlotTiers().addAll(memberSlotTiers);

        // And: A test realm object
        testRealm = new Realm("TestRealm", UUID.randomUUID(), "world_test", "default");

        // And: The RealmConfig provides a base value for max players
        lenient().when(plugin.getRealmConfig()).thenReturn(realmConfig);
        lenient().when(realmConfig.getBaseMaxPlayers()).thenReturn(8);
    }

    @Test
    @DisplayName("Should return the next border tier when not at max")
    void getNextBorderTier_whenNotAtMax_returnsNextTier() {
        // Given: The realm has a border size of 100
        testRealm.setBorderSize(100);

        // When: We request the next border tier
        Optional<WorldBorderTier> nextTier = upgradeManager.getNextBorderTier(testRealm);

        // Then: The correct next tier is returned
        assertTrue(nextTier.isPresent(), "Next tier should be present");
        assertEquals(150, nextTier.get().getSize(), "Next tier size should be 150");
        assertEquals(12000, nextTier.get().getPrice(), "Next tier price should be 12000");
    }

    @Test
    @DisplayName("Should return empty when at the maximum border tier")
    void getNextBorderTier_whenAtMax_returnsEmpty() {
        // Given: The realm has the maximum border size
        testRealm.setBorderSize(150);

        // When: We request the next border tier
        Optional<WorldBorderTier> nextTier = upgradeManager.getNextBorderTier(testRealm);

        // Then: An empty optional is returned
        assertFalse(nextTier.isPresent(), "No next tier should be available");
    }

    @Test
    @DisplayName("Should return the next member slot tier when not at max")
    void getNextMemberSlotTier_whenNotAtMax_returnsNextTier() {
        // Given: The realm has the base number of players (8) + 5 additional slots
        testRealm.setMaxPlayers(8 + 5);

        // When: We request the next member slot tier
        Optional<MemberSlotTier> nextTier = upgradeManager.getNextMemberSlotTier(testRealm);

        // Then: The correct next tier is returned
        assertTrue(nextTier.isPresent(), "Next member slot tier should be present");
        assertEquals(10, nextTier.get().getAdditionalSlots(), "Next tier additional slots should be 10");
        assertEquals(6000, nextTier.get().getPrice(), "Next tier price should be 6000");
    }

    @Test
    @DisplayName("Should return empty when at the maximum member slot tier")
    void getNextMemberSlotTier_whenAtMax_returnsEmpty() {
        // Given: The realm has the maximum number of additional member slots
        testRealm.setMaxPlayers(8 + 10);

        // When: We request the next member slot tier
        Optional<MemberSlotTier> nextTier = upgradeManager.getNextMemberSlotTier(testRealm);

        // Then: An empty optional is returned
        assertFalse(nextTier.isPresent(), "No next member slot tier should be available");
    }
}