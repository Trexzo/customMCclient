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
