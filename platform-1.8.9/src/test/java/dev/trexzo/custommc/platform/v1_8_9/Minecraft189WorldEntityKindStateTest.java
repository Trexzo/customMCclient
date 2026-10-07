package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189WorldEntityKindStateTest {
    @Test
    void storesImmutableValidatedKindFlagsAndClears() {
        final Minecraft189WorldEntityKindState state =
                new Minecraft189WorldEntityKindState();
        final int[] source =
                new int[]{
                        Minecraft189WorldEntityKindState.LIVING
                                | Minecraft189WorldEntityKindState.PLAYER
                                | Minecraft189WorldEntityKindState.LOCAL_PLAYER,
                        Minecraft189WorldEntityKindState.LIVING,
                        0
                };

        state.update(source);
        source[0] = 0;

        final Minecraft189WorldEntityKindState.Snapshot snapshot =
                state.snapshot();
        assertTrue(snapshot.available());
        assertEquals(
                3,
                snapshot.entityCount());
        assertTrue(snapshot.living(0));
        assertTrue(snapshot.player(0));
        assertTrue(snapshot.localPlayer(0));
        assertTrue(snapshot.living(1));
        assertFalse(snapshot.player(1));
        assertFalse(snapshot.localPlayer(1));
        assertEquals(
                0,
                snapshot.kindBits(2));

        final int[] exported =
                snapshot.kindBits();
        exported[0] = 0;
        assertTrue(snapshot.localPlayer(0));

        state.clear();
        final Minecraft189WorldEntityKindState.Snapshot cleared =
                state.snapshot();
        assertFalse(cleared.available());
        assertEquals(
                0,
                cleared.entityCount());
    }

    @Test
    void rejectsImpossibleOrUnknownKindFlags() {
        final Minecraft189WorldEntityKindState state =
                new Minecraft189WorldEntityKindState();

        assertThrows(
                IllegalArgumentException.class,
                () -> state.update(
                        new int[]{
                                Minecraft189WorldEntityKindState.PLAYER
                        }));
        assertThrows(
                IllegalArgumentException.class,
                () -> state.update(
                        new int[]{
                                Minecraft189WorldEntityKindState.LOCAL_PLAYER
                        }));
        assertThrows(
                IllegalArgumentException.class,
                () -> state.update(
                        new int[]{
                                1 << 10
                        }));
    }
}
