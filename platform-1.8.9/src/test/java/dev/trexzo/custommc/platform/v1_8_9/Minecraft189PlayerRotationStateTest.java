package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189PlayerRotationStateTest {
    @Test
    void rotationStateCarriesYawAndPitchAndClearsAtomically() {
        final Minecraft189PlayerRotationState state =
                new Minecraft189PlayerRotationState();

        Minecraft189PlayerRotationState.Snapshot snapshot =
                state.snapshot();
        assertFalse(snapshot.available());
        assertEquals(0.0F, snapshot.yaw());
        assertEquals(0.0F, snapshot.pitch());

        state.update(91.25F, -17.5F);
        snapshot = state.snapshot();
        assertTrue(snapshot.available());
        assertEquals(91.25F, snapshot.yaw());
        assertEquals(-17.5F, snapshot.pitch());

        assertThrows(
                IllegalArgumentException.class,
                () -> state.update(Float.NaN, 0.0F));
        assertThrows(
                IllegalArgumentException.class,
                () -> state.update(0.0F, Float.POSITIVE_INFINITY));

        snapshot = state.snapshot();
        assertTrue(snapshot.available());
        assertEquals(91.25F, snapshot.yaw());
        assertEquals(-17.5F, snapshot.pitch());

        state.clear();
        snapshot = state.snapshot();
        assertFalse(snapshot.available());
        assertEquals(0.0F, snapshot.yaw());
        assertEquals(0.0F, snapshot.pitch());
    }
}
