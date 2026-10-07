package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189TargetRotationStateTest {
    @Test
    void solvesMinecraftYawAndPitchConvention() {
        final Minecraft189PlayerPositionState local =
                new Minecraft189PlayerPositionState();
        final Minecraft189NearestPlayerTargetState target =
                new Minecraft189NearestPlayerTargetState();
        final Minecraft189TargetRotationState rotation =
                new Minecraft189TargetRotationState();

        local.update(
                0.0D,
                0.0D,
                0.0D);

        assertSolution(
                local,
                target,
                rotation,
                0.0D,
                0.0D,
                10.0D,
                0.0F,
                0.0F);
        assertSolution(
                local,
                target,
                rotation,
                -10.0D,
                0.0D,
                0.0D,
                90.0F,
                0.0F);
        assertSolution(
                local,
                target,
                rotation,
                10.0D,
                0.0D,
                0.0D,
                -90.0F,
                0.0F);
        assertSolution(
                local,
                target,
                rotation,
                0.0D,
                10.0D,
                10.0D,
                0.0F,
                -45.0F);
    }

    @Test
    void failsClosedWithoutTargetOrForVerticalOnlySolution() {
        final Minecraft189PlayerPositionState local =
                new Minecraft189PlayerPositionState();
        final Minecraft189NearestPlayerTargetState target =
                new Minecraft189NearestPlayerTargetState();
        final Minecraft189TargetRotationState rotation =
                new Minecraft189TargetRotationState();

        local.update(
                0.0D,
                0.0D,
                0.0D);
        rotation.update(
                local.snapshot(),
                target.snapshot());
        assertFalse(
                rotation.snapshot()
                        .available());

        selectSingleRemotePlayer(
                local,
                target,
                0.0D,
                5.0D,
                0.0D);
        rotation.update(
                local.snapshot(),
                target.snapshot());
        assertFalse(
                rotation.snapshot()
                        .available());

        rotation.clear();
        assertFalse(
                rotation.snapshot()
                        .available());
    }

    private static void assertSolution(
            final Minecraft189PlayerPositionState local,
            final Minecraft189NearestPlayerTargetState target,
            final Minecraft189TargetRotationState rotation,
            final double x,
            final double y,
            final double z,
            final float expectedYaw,
            final float expectedPitch) {
        selectSingleRemotePlayer(
                local,
                target,
                x,
                y,
                z);
        rotation.update(
                local.snapshot(),
                target.snapshot());

        final Minecraft189TargetRotationState.Snapshot snapshot =
                rotation.snapshot();
        assertTrue(snapshot.available());
        assertEquals(
                0,
                snapshot.entityIndex());
        assertEquals(
                expectedYaw,
                snapshot.yaw(),
                0.0001F);
        assertEquals(
                expectedPitch,
                snapshot.pitch(),
                0.0001F);
    }

    private static void selectSingleRemotePlayer(
            final Minecraft189PlayerPositionState local,
            final Minecraft189NearestPlayerTargetState target,
            final double x,
            final double y,
            final double z) {
        final Minecraft189WorldEntityPositionState positions =
                new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds =
                new Minecraft189WorldEntityKindState();
        positions.update(
                new double[]{
                        x,
                        y,
                        z
                });
        kinds.update(
                new int[]{
                        Minecraft189WorldEntityKindState.LIVING
                                | Minecraft189WorldEntityKindState.PLAYER
                });
        target.update(
                local.snapshot(),
                positions.snapshot(),
                kinds.snapshot());
        assertTrue(
                target.snapshot()
                        .found());
    }
}
