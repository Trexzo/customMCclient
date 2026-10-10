package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189KillAuraTargetSelectorTest {
    @Test
    void switchesBetweenNearestAndCrosshairPriorityInsideConfiguredFov() {
        final Minecraft189KillAuraModule aura = new Minecraft189KillAuraModule();
        final Minecraft189PlayerPositionState local = new Minecraft189PlayerPositionState();
        final Minecraft189PlayerRotationState rotation = new Minecraft189PlayerRotationState();
        final Minecraft189WorldEntityPositionState positions = new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds = new Minecraft189WorldEntityKindState();
        final Minecraft189WorldEntityCombatState combat = new Minecraft189WorldEntityCombatState();
        final Minecraft189NearestPlayerTargetState selected =
                new Minecraft189NearestPlayerTargetState();
        local.update(0, 0, 0);
        rotation.update(0.0F, 0.0F); // North: +Z
        positions.update(new double[]{1, 0, 0, 0, 0, 3});
        kinds.update(new int[]{3, 3});
        combat.update(new int[]{1, 1});

        Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                combat.snapshot(), selected);
        assertTrue(selected.snapshot().found());
        assertEquals(0, selected.snapshot().entityIndex()); // nearer east

        aura.prioritizeCrosshairSetting().set(true);
        Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                combat.snapshot(), selected);
        assertEquals(1, selected.snapshot().entityIndex()); // aligned north

        aura.prioritizeCrosshairSetting().set(false);
        aura.fovSetting().set(45.0D);
        Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                combat.snapshot(), selected);
        assertEquals(1, selected.snapshot().entityIndex()); // east excluded
    }

    @Test
    void optionalTabEvidenceRejectsKnownMissingNetworkPlayer() {
        final Minecraft189KillAuraModule aura = new Minecraft189KillAuraModule();
        final Minecraft189AntiBotModule antiBot = new Minecraft189AntiBotModule();
        final Minecraft189PlayerPositionState local = new Minecraft189PlayerPositionState();
        final Minecraft189PlayerRotationState rotation = new Minecraft189PlayerRotationState();
        final Minecraft189WorldEntityPositionState positions = new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds = new Minecraft189WorldEntityKindState();
        final Minecraft189WorldEntityCombatState combat = new Minecraft189WorldEntityCombatState();
        final Minecraft189NearestPlayerTargetState selected =
                new Minecraft189NearestPlayerTargetState();
        local.update(0, 0, 0);
        rotation.update(0, 0);
        positions.update(new double[]{0, 0, 2, 0, 0, 3});
        kinds.update(new int[]{3, 3});
        combat.update(new int[]{1 | 512, 1 | 512 | 256});

        // Off by default: nearer player still wins.
        Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                combat.snapshot(), antiBot, selected);
        assertEquals(0, selected.snapshot().entityIndex());

        antiBot.onEnable();
        Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                combat.snapshot(), antiBot, selected);
        assertEquals(1, selected.snapshot().entityIndex());
        combat.update(new int[]{1 | 512, 1 | 512});
        Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                combat.snapshot(), antiBot, selected);
        assertFalse(selected.snapshot().found());
        antiBot.onDisable();
    }

    @Test
    void switchesFromHurtNearestToNextEligiblePlayerWithoutBankingTargets() {
        final Minecraft189KillAuraModule aura = new Minecraft189KillAuraModule();
        final Minecraft189PlayerPositionState local = new Minecraft189PlayerPositionState();
        final Minecraft189PlayerRotationState rotation = new Minecraft189PlayerRotationState();
        final Minecraft189WorldEntityPositionState positions = new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds = new Minecraft189WorldEntityKindState();
        final Minecraft189WorldEntityCombatState combat = new Minecraft189WorldEntityCombatState();
        final Minecraft189NearestPlayerTargetState selected =
                new Minecraft189NearestPlayerTargetState();
        local.update(0, 0, 0);
        rotation.update(0, 0);
        positions.update(new double[]{0, 0, 2, 0, 0, 3});
        kinds.update(new int[]{3, 3});
        combat.update(new int[]{(6 << 1) | 1, 1});

        // Default OFF preserves ordinary nearest selection.
        Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                combat.snapshot(), selected);
        assertEquals(0, selected.snapshot().entityIndex());

        aura.switchHurtTargetsSetting().set(true);
        Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                combat.snapshot(), selected);
        assertEquals(1, selected.snapshot().entityIndex());
        aura.maxSwitchHurtTicksSetting().set(6);
        Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                combat.snapshot(), selected);
        assertEquals(0, selected.snapshot().entityIndex());

        // When all nearby targets are hurt beyond threshold, no attacker
        // identity is fabricated and the selector reports no candidate.
        aura.maxSwitchHurtTicksSetting().set(0);
        combat.update(new int[]{(4 << 1) | 1, (3 << 1) | 1});
        Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                combat.snapshot(), selected);
        assertFalse(selected.snapshot().found());
        assertThrows(IllegalArgumentException.class,
                () -> aura.maxSwitchHurtTicksSetting().set(21));
    }


    @Test
    void optionalTargetLockRevalidatesEligibilityAndClearsOnWorldLoss() {
        final Minecraft189KillAuraModule aura = new Minecraft189KillAuraModule();
        final Minecraft189PlayerPositionState local = new Minecraft189PlayerPositionState();
        final Minecraft189PlayerRotationState rotation = new Minecraft189PlayerRotationState();
        final Minecraft189WorldEntityPositionState positions =
                new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds =
                new Minecraft189WorldEntityKindState();
        final Minecraft189WorldEntityCombatState combat =
                new Minecraft189WorldEntityCombatState();
        final Minecraft189NearestPlayerTargetState selected =
                new Minecraft189NearestPlayerTargetState();
        local.update(0, 0, 0);
        rotation.update(0, 0);
        positions.update(new double[]{0, 0, 2, 0, 0, 3});
        kinds.update(new int[]{3, 3});
        combat.update(new int[]{1, 1});
        aura.onEnable();
        aura.lockTargetSetting().set(true);
        try {
            Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                    rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                    combat.snapshot(), selected);
            assertEquals(0, selected.snapshot().entityIndex());

            // The originally selected enemy remains eligible, despite a
            // different opponent moving closer on a later snapshot.
            positions.update(new double[]{0, 0, 3, 0, 0, 2});
            Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                    rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                    combat.snapshot(), selected);
            assertEquals(0, selected.snapshot().entityIndex());

            // Dead/filtered targets are not retained as a stale target.
            combat.update(new int[]{0, 1});
            Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                    rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                    combat.snapshot(), selected);
            assertEquals(1, selected.snapshot().entityIndex());

            combat.update(new int[]{1, 1});
            positions.update(new double[]{0, 0, 1, 0, 0, 3});
            Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                    rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                    combat.snapshot(), selected);
            assertEquals(1, selected.snapshot().entityIndex());

            // World evidence loss destroys retained state immediately.
            combat.clear();
            Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                    rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                    combat.snapshot(), selected);
            assertFalse(selected.snapshot().available());
            combat.update(new int[]{1, 1});
            Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                    rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                    combat.snapshot(), selected);
            assertEquals(0, selected.snapshot().entityIndex());

            // Default/off behavior remains ordinary nearest-player targeting.
            aura.lockTargetSetting().set(false);
            positions.update(new double[]{0, 0, 3, 0, 0, 2});
            Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                    rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                    combat.snapshot(), selected);
            assertEquals(1, selected.snapshot().entityIndex());
        } finally {
            aura.onDisable();
        }
        assertEquals(-1, aura.lockedTargetIndex());
    }

    @Test
    void standaloneTeamGuardFiltersAuraWithoutAntiBotEnabled() {
        final Minecraft189KillAuraModule aura = new Minecraft189KillAuraModule();
        final Minecraft189AntiBotModule bot = new Minecraft189AntiBotModule();
        final Minecraft189TeamGuardModule guard = new Minecraft189TeamGuardModule();
        final Minecraft189PlayerPositionState local = new Minecraft189PlayerPositionState();
        final Minecraft189PlayerRotationState rotation = new Minecraft189PlayerRotationState();
        final Minecraft189WorldEntityPositionState pos = new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds = new Minecraft189WorldEntityKindState();
        final Minecraft189WorldEntityCombatState combat = new Minecraft189WorldEntityCombatState();
        final Minecraft189NearestPlayerTargetState selected =
                new Minecraft189NearestPlayerTargetState();
        local.update(0, 0, 0);
        rotation.update(0, 0);
        pos.update(new double[]{0, 0, 2, 0, 0, 3});
        kinds.update(new int[]{3, 3});
        combat.update(new int[]{1 | 2048 | 1024, 1 | 2048});
        Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                rotation.snapshot(), pos.snapshot(), kinds.snapshot(), combat.snapshot(),
                bot, guard, selected);
        assertEquals(0, selected.snapshot().entityIndex());
        guard.onEnable();
        try {
            assertFalse(bot.active());
            Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                    rotation.snapshot(), pos.snapshot(), kinds.snapshot(), combat.snapshot(),
                    bot, guard, selected);
            assertEquals(1, selected.snapshot().entityIndex());
            combat.update(new int[]{1 | 2048 | 1024, 1});
            Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                    rotation.snapshot(), pos.snapshot(), kinds.snapshot(), combat.snapshot(),
                    bot, guard, selected);
            assertFalse(selected.snapshot().found());
            guard.onDisable();
            Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                    rotation.snapshot(), pos.snapshot(), kinds.snapshot(), combat.snapshot(),
                    bot, guard, selected);
            assertEquals(0, selected.snapshot().entityIndex());
        } finally { guard.onDisable(); }
    }

    @Test
    void excludesDeadUnknownAndOutOfRangeAndClearsStaleInputs() {
        final Minecraft189KillAuraModule aura = new Minecraft189KillAuraModule();
        final Minecraft189PlayerPositionState local = new Minecraft189PlayerPositionState();
        final Minecraft189PlayerRotationState rotation = new Minecraft189PlayerRotationState();
        final Minecraft189WorldEntityPositionState positions = new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds = new Minecraft189WorldEntityKindState();
        final Minecraft189WorldEntityCombatState combat = new Minecraft189WorldEntityCombatState();
        final Minecraft189NearestPlayerTargetState selected =
                new Minecraft189NearestPlayerTargetState();
        local.update(0, 0, 0);
        rotation.update(0, 0);
        positions.update(new double[]{1, 0, 0, 0, 0, 3});
        kinds.update(new int[]{3, 3});
        combat.update(new int[]{0, 1});
        Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                combat.snapshot(), selected);
        assertEquals(1, selected.snapshot().entityIndex());

        aura.rangeSetting().set(1.5D);
        Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                combat.snapshot(), selected);
        assertFalse(selected.snapshot().found());
        aura.rangeSetting().set(6.0D);
        combat.update(new int[]{-1, -1});
        Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                combat.snapshot(), selected);
        assertFalse(selected.snapshot().found());
        combat.clear();
        Minecraft189KillAuraTargetSelector.select(aura, local.snapshot(),
                rotation.snapshot(), positions.snapshot(), kinds.snapshot(),
                combat.snapshot(), selected);
        assertFalse(selected.snapshot().available());
    }
}
