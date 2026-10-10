package dev.trexzo.custommc.platform.v1_8_9;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189WorldEntityUuidStateTest {
    @Test void tracksExactIdentityUnderReorderAndRejectsUnknownEvidence() {
        final Minecraft189WorldEntityUuidState state = new Minecraft189WorldEntityUuidState();
        final UUID a = UUID.randomUUID();
        final UUID b = UUID.randomUUID();
        assertFalse(state.snapshot().available());
        assertTrue(state.update(new UUID[]{a, b}));
        final Minecraft189WorldEntityUuidState.Snapshot earlier = state.snapshot();
        assertEquals(a, earlier.at(0));
        assertEquals(b, earlier.at(1));
        assertTrue(earlier.matches(new UUID[]{a, b}));
        assertFalse(earlier.matches(new UUID[]{b, a}));
        assertFalse(earlier.matches(new UUID[]{a}));
        assertFalse(earlier.matches(null));
        assertTrue(state.update(new UUID[]{b, a}));
        assertEquals(a, state.snapshot().at(1));
        assertEquals(a, earlier.at(0)); // Defensive immutable snapshot
        assertFalse(state.update(new UUID[]{b, null}));
        assertFalse(state.snapshot().available());
        assertFalse(state.update(new UUID[]{a, a}));
        assertFalse(state.update(null));
        assertNull(state.snapshot().at(0));
        assertTrue(state.update(new UUID[0]));
        assertTrue(state.snapshot().available());
        assertEquals(0, state.snapshot().entityCount());
        state.clear();
        assertFalse(state.snapshot().available());
    }
}
