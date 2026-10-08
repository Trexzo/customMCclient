package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189NearestPlayerTargetStateTest {
    @Test
    void selectsNearestNonLocalPlayerWithStableIndexTieBreak() {
        final Minecraft189PlayerPositionState local =
                new Minecraft189PlayerPositionState();
        final Minecraft189WorldEntityPositionState positions =
                new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds =
                new Minecraft189WorldEntityKindState();
        final Minecraft189NearestPlayerTargetState target =
                new Minecraft189NearestPlayerTargetState();

        local.update(
                0.0D,
                64.0D,
                0.0D);
        positions.update(
                new double[]{
                        0.0D, 64.0D, 0.0D,
                        3.0D, 64.0D, 4.0D,
                        -3.0D, 64.0D, -4.0D,
                        1.0D, 64.0D, 1.0D
                });
        kinds.update(
                new int[]{
                        Minecraft189WorldEntityKindState.LIVING
                                | Minecraft189WorldEntityKindState.PLAYER
                                | Minecraft189WorldEntityKindState.LOCAL_PLAYER,
                        Minecraft189WorldEntityKindState.LIVING
                                | Minecraft189WorldEntityKindState.PLAYER,
                        Minecraft189WorldEntityKindState.LIVING
                                | Minecraft189WorldEntityKindState.PLAYER,
                        Minecraft189WorldEntityKindState.LIVING
                });

        target.update(
                local.snapshot(),
                positions.snapshot(),
                kinds.snapshot());

        final Minecraft189NearestPlayerTargetState.Snapshot snapshot =
                target.snapshot();
        assertTrue(snapshot.available());
        assertTrue(snapshot.found());
        assertEquals(
                1,
                snapshot.entityIndex());
        assertEquals(
                3.0D,
                snapshot.x());
        assertEquals(
                64.0D,
                snapshot.y());
        assertEquals(
                4.0D,
                snapshot.z());
        assertEquals(
                25.0D,
                snapshot.distanceSquared());
        assertEquals(
                5.0D,
                snapshot.distance());
    }

    @Test
    void representsNoCandidateWithoutRetainingStaleTarget() {
        final Minecraft189PlayerPositionState local =
                new Minecraft189PlayerPositionState();
        final Minecraft189WorldEntityPositionState positions =
                new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds =
                new Minecraft189WorldEntityKindState();
        final Minecraft189NearestPlayerTargetState target =
                new Minecraft189NearestPlayerTargetState();

        local.update(
                1.0D,
                2.0D,
                3.0D);
        positions.update(
                new double[]{
                        1.0D, 2.0D, 3.0D
                });
        kinds.update(
                new int[]{
                        Minecraft189WorldEntityKindState.LIVING
                                | Minecraft189WorldEntityKindState.PLAYER
                                | Minecraft189WorldEntityKindState.LOCAL_PLAYER
                });

        target.update(
                local.snapshot(),
                positions.snapshot(),
                kinds.snapshot());

        final Minecraft189NearestPlayerTargetState.Snapshot snapshot =
                target.snapshot();
        assertTrue(snapshot.available());
        assertFalse(snapshot.found());
        assertEquals(
                -1,
                snapshot.entityIndex());
        assertEquals(
                0.0D,
                snapshot.distanceSquared());
    }

    @Test
    void failsClosedWhenSourcesAreUnavailableOrMisaligned() {
        final Minecraft189PlayerPositionState local =
                new Minecraft189PlayerPositionState();
        final Minecraft189WorldEntityPositionState positions =
                new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds =
                new Minecraft189WorldEntityKindState();
        final Minecraft189NearestPlayerTargetState target =
                new Minecraft189NearestPlayerTargetState();

        local.update(
                0.0D,
                0.0D,
                0.0D);
        positions.update(
                new double[]{
                        1.0D, 0.0D, 0.0D
                });
        kinds.update(
                new int[0]);

        target.update(
                local.snapshot(),
                positions.snapshot(),
                kinds.snapshot());
        assertFalse(
                target.snapshot()
                        .available());

        kinds.update(
                new int[]{
                        Minecraft189WorldEntityKindState.LIVING
                                | Minecraft189WorldEntityKindState.PLAYER
                });
        positions.clear();
        target.update(
                local.snapshot(),
                positions.snapshot(),
                kinds.snapshot());
        assertFalse(
                target.snapshot()
                        .available());

        target.clear();
        assertFalse(
                target.snapshot()
                        .available());
    }

    @Test
    void rangeQualifiedSelectionSkipsCloserPlayersWithoutChangingLegacySelection() {
        final Minecraft189PlayerPositionState local = new Minecraft189PlayerPositionState();
        final Minecraft189WorldEntityPositionState positions =
                new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds =
                new Minecraft189WorldEntityKindState();
        final Minecraft189NearestPlayerTargetState target =
                new Minecraft189NearestPlayerTargetState();
        local.update(0.0D, 0.0D, 0.0D);
        positions.update(new double[]{
                0.0D, 0.0D, 0.0D,
                2.0D, 0.0D, 0.0D,
                0.0D, 0.0D, 7.0D,
                0.0D, 0.0D, 9.0D
        });
        kinds.update(new int[]{
                Minecraft189WorldEntityKindState.LIVING
                        | Minecraft189WorldEntityKindState.PLAYER
                        | Minecraft189WorldEntityKindState.LOCAL_PLAYER,
                Minecraft189WorldEntityKindState.LIVING
                        | Minecraft189WorldEntityKindState.PLAYER,
                Minecraft189WorldEntityKindState.LIVING
                        | Minecraft189WorldEntityKindState.PLAYER,
                Minecraft189WorldEntityKindState.LIVING
                        | Minecraft189WorldEntityKindState.PLAYER
        });
        target.update(local.snapshot(), positions.snapshot(), kinds.snapshot());
        assertEquals(1, target.snapshot().entityIndex());
        assertEquals(4.0D, target.snapshot().distanceSquared());

        target.update(local.snapshot(), positions.snapshot(), kinds.snapshot(),
                5.0D, 20.0D);
        assertTrue(target.snapshot().available());
        assertTrue(target.snapshot().found());
        assertEquals(2, target.snapshot().entityIndex());
        assertEquals(49.0D, target.snapshot().distanceSquared());
        assertEquals(7.0D, target.snapshot().distance());

        target.update(local.snapshot(), positions.snapshot(), kinds.snapshot(),
                7.0D, 7.0D);
        assertEquals(2, target.snapshot().entityIndex());
        target.update(local.snapshot(), positions.snapshot(), kinds.snapshot(),
                8.0D, 9.0D);
        assertEquals(3, target.snapshot().entityIndex());
        target.update(local.snapshot(), positions.snapshot(), kinds.snapshot(),
                3.0D, 6.0D);
        assertTrue(target.snapshot().available());
        assertFalse(target.snapshot().found());
        assertEquals(-1, target.snapshot().entityIndex());

        target.update(local.snapshot(), positions.snapshot(), kinds.snapshot(),
                10.0D, 9.0D);
        assertFalse(target.snapshot().available());
        assertFalse(target.snapshot().found());
        target.update(local.snapshot(), positions.snapshot(), kinds.snapshot(),
                Double.NaN, 20.0D);
        assertFalse(target.snapshot().available());
        target.update(local.snapshot(), positions.snapshot(), kinds.snapshot());
        assertTrue(target.snapshot().available());
        assertEquals(1, target.snapshot().entityIndex());
    }

    @Test
    void candidateFilterSelectsNextNearestAndFailsClosedWithoutMatches() {
        final Minecraft189PlayerPositionState local = new Minecraft189PlayerPositionState();
        final Minecraft189WorldEntityPositionState positions =
                new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds =
                new Minecraft189WorldEntityKindState();
        final Minecraft189NearestPlayerTargetState target =
                new Minecraft189NearestPlayerTargetState();
        local.update(0.0D, 0.0D, 0.0D);
        positions.update(new double[]{
                0.0D, 0.0D, 0.0D,
                2.0D, 0.0D, 0.0D,
                7.0D, 0.0D, 0.0D
        });
        kinds.update(new int[]{
                Minecraft189WorldEntityKindState.LIVING
                        | Minecraft189WorldEntityKindState.PLAYER
                        | Minecraft189WorldEntityKindState.LOCAL_PLAYER,
                Minecraft189WorldEntityKindState.LIVING
                        | Minecraft189WorldEntityKindState.PLAYER,
                Minecraft189WorldEntityKindState.LIVING
                        | Minecraft189WorldEntityKindState.PLAYER
        });
        target.update(local.snapshot(), positions.snapshot(), kinds.snapshot(),
                0.0D, 20.0D, candidate -> candidate.entityIndex() == 2);
        assertTrue(target.snapshot().available());
        assertTrue(target.snapshot().found());
        assertEquals(2, target.snapshot().entityIndex());
        assertEquals(49.0D, target.snapshot().distanceSquared());

        target.update(local.snapshot(), positions.snapshot(), kinds.snapshot(),
                0.0D, 20.0D, candidate -> false);
        assertTrue(target.snapshot().available());
        assertFalse(target.snapshot().found());
        assertEquals(-1, target.snapshot().entityIndex());

        target.update(local.snapshot(), positions.snapshot(), kinds.snapshot(),
                0.0D, 20.0D);
        assertEquals(1, target.snapshot().entityIndex());
        target.update(local.snapshot(), positions.snapshot(), kinds.snapshot());
        assertEquals(1, target.snapshot().entityIndex());
    }

}
