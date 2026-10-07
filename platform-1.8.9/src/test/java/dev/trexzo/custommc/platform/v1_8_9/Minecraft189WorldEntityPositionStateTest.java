package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189WorldEntityPositionStateTest {
    @Test
    void storesImmutableFiniteXyzTriplesAndClears() {
        final Minecraft189WorldEntityPositionState state =
                new Minecraft189WorldEntityPositionState();
        final double[] source =
                new double[]{
                        1.0D,
                        2.0D,
                        3.0D,
                        -4.5D,
                        64.0D,
                        8.25D
                };

        state.update(source);
        source[0] = 999.0D;

        final Minecraft189WorldEntityPositionState.Snapshot snapshot =
                state.snapshot();
        assertTrue(snapshot.available());
        assertEquals(
                2,
                snapshot.entityCount());
        assertEquals(
                1.0D,
                snapshot.x(0));
        assertEquals(
                2.0D,
                snapshot.y(0));
        assertEquals(
                3.0D,
                snapshot.z(0));
        assertEquals(
                -4.5D,
                snapshot.x(1));
        assertEquals(
                64.0D,
                snapshot.y(1));
        assertEquals(
                8.25D,
                snapshot.z(1));

        final double[] exported =
                snapshot.packedPositions();
        exported[0] = -1000.0D;
        assertEquals(
                1.0D,
                snapshot.x(0));

        state.clear();
        final Minecraft189WorldEntityPositionState.Snapshot cleared =
                state.snapshot();
        assertFalse(cleared.available());
        assertEquals(
                0,
                cleared.entityCount());
    }

    @Test
    void rejectsMalformedOrNonFiniteSnapshots() {
        final Minecraft189WorldEntityPositionState state =
                new Minecraft189WorldEntityPositionState();

        assertThrows(
                IllegalArgumentException.class,
                () -> state.update(
                        new double[]{
                                1.0D,
                                2.0D
                        }));
        assertThrows(
                IllegalArgumentException.class,
                () -> state.update(
                        new double[]{
                                Double.NaN,
                                2.0D,
                                3.0D
                        }));
        assertThrows(
                IllegalArgumentException.class,
                () -> state.update(
                        new double[]{
                                1.0D,
                                Double.POSITIVE_INFINITY,
                                3.0D
                        }));
    }
}
