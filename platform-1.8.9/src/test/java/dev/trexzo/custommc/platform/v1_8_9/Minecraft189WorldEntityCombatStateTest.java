package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189WorldEntityCombatStateTest {
    @Test
    void protectsUnknownAndCopiesSourceAndSnapshots() {
        final Minecraft189WorldEntityCombatState state = new Minecraft189WorldEntityCombatState();
        assertFalse(state.snapshot().available());
        assertFalse(state.snapshot().known(0));
        assertEquals(-1, state.snapshot().hurtTime(0));
        final int[] evidence = {-1, 1, 11, 10};
        state.update(evidence);
        evidence[2] = 0;
        final Minecraft189WorldEntityCombatState.Snapshot old = state.snapshot();
        assertTrue(old.available());
        assertEquals(4, old.entityCount());
        assertFalse(old.known(0));
        assertTrue(old.known(1));
        assertTrue(old.alive(1));
        assertTrue(old.alive(2));
        assertFalse(old.alive(3));
        assertEquals(0, old.hurtTime(1));
        assertEquals(5, old.hurtTime(2));
        assertEquals(5, old.hurtTime(3));
        assertFalse(old.known(4));
        state.update(new int[] {0});
        assertEquals(4, old.entityCount());
        assertEquals(1, state.snapshot().entityCount());
        state.clear();
        assertFalse(state.snapshot().available());
    }

    @Test
    void failClosedValidationDoesNotReplacePriorSnapshot() {
        final Minecraft189WorldEntityCombatState state = new Minecraft189WorldEntityCombatState();
        state.update(new int[] {1});
        assertThrows(IllegalArgumentException.class,
                () -> state.update(new int[] {-2}));
        assertThrows(IllegalArgumentException.class,
                () -> state.update(new int[] {256}));
        assertThrows(NullPointerException.class,
                () -> state.update(null));
        assertEquals(1, state.snapshot().raw(0));
    }
}
