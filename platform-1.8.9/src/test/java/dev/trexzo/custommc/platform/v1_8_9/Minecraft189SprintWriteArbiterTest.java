package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Exactly one real EntityPlayerSP sprint writer per callback;
 * WTap owns a setter attempt even if a native reentry revokes apply().
 */
final class Minecraft189SprintWriteArbiterTest {
    private static final class Player implements Minecraft189PlayerSprintControl {
        int trueWrites, falseWrites;
        Runnable onWrite;
        boolean throwOnWrite;
        @Override public void customMcSetSprinting(final boolean sprint) {
            if (sprint) trueWrites++; else falseWrites++;
            if (onWrite != null) onWrite.run();
            if (throwOnWrite) throw new IllegalStateException("native setter failed");
        }
    }

    @Test
    void wTapActualWriteBlocksOppositeAutoSprintEvenAfterReentry() {
        final Minecraft189SprintWriteArbiter arbiter =
                new Minecraft189SprintWriteArbiter();
        final Player original = new Player();
        arbiter.enter();
        try {
            final Minecraft189PlayerSprintControl tap =
                    arbiter.guardedPlayer(original,
                            Minecraft189SprintWriteArbiter.Owner.W_TAP);
            final Minecraft189PlayerSprintControl sprint =
                    arbiter.guardedPlayer(original,
                            Minecraft189SprintWriteArbiter.Owner.AUTO_SPRINT);
            assertFalse(arbiter.ownedBy(
                    Minecraft189SprintWriteArbiter.Owner.W_TAP));
            original.onWrite = () -> sprint.customMcSetSprinting(true);
            tap.customMcSetSprinting(false);
            assertTrue(arbiter.ownedBy(
                    Minecraft189SprintWriteArbiter.Owner.W_TAP));
            assertEquals(1, original.falseWrites);
            assertEquals(0, original.trueWrites);
            sprint.customMcSetSprinting(true);
            assertEquals(0, original.trueWrites);
        } finally {
            arbiter.exit();
        }
    }

    @Test
    void wTapWithoutNativeSetterYieldsToAutoSprint() {
        final Minecraft189SprintWriteArbiter arbiter =
                new Minecraft189SprintWriteArbiter();
        final Player original = new Player();
        arbiter.enter();
        final Minecraft189PlayerSprintControl sprint =
                arbiter.guardedPlayer(original,
                        Minecraft189SprintWriteArbiter.Owner.AUTO_SPRINT);
        assertFalse(arbiter.ownedBy(
                Minecraft189SprintWriteArbiter.Owner.W_TAP));
        sprint.customMcSetSprinting(true);
        assertEquals(1, original.trueWrites);
        assertTrue(arbiter.ownedBy(
                Minecraft189SprintWriteArbiter.Owner.AUTO_SPRINT));
        // No later WTap setter can steal AutoSprint's native callback.
        arbiter.guardedPlayer(original, Minecraft189SprintWriteArbiter.Owner.W_TAP)
                .customMcSetSprinting(false);
        assertEquals(0, original.falseWrites);
        arbiter.exit();
    }

    @Test
    void oldCallbackCannotUseRearmedWriterInLaterGeneration() {
        final Minecraft189SprintWriteArbiter arbiter =
                new Minecraft189SprintWriteArbiter();
        final Player old = new Player(), fresh = new Player();
        arbiter.enter();
        final Minecraft189PlayerSprintControl stale =
                arbiter.guardedPlayer(old, Minecraft189SprintWriteArbiter.Owner.W_TAP);
        arbiter.exit();
        stale.customMcSetSprinting(false);
        assertEquals(0, old.falseWrites);
        arbiter.enter();
        final Minecraft189PlayerSprintControl newWriter =
                arbiter.guardedPlayer(fresh,
                        Minecraft189SprintWriteArbiter.Owner.AUTO_SPRINT);
        stale.customMcSetSprinting(false);
        assertEquals(0, old.falseWrites);
        newWriter.customMcSetSprinting(true);
        assertEquals(1, fresh.trueWrites);
        arbiter.exit();
    }

    @Test
    void throwingOriginalWriteDoesNotOpenNewAutoSprintLane() {
        final Minecraft189SprintWriteArbiter arbiter =
                new Minecraft189SprintWriteArbiter();
        final Player original = new Player();
        original.throwOnWrite = true;
        arbiter.enter();
        final Minecraft189PlayerSprintControl tap =
                arbiter.guardedPlayer(original,
                        Minecraft189SprintWriteArbiter.Owner.W_TAP);
        assertThrows(IllegalStateException.class,
                () -> tap.customMcSetSprinting(false));
        assertTrue(arbiter.ownedBy(Minecraft189SprintWriteArbiter.Owner.W_TAP));
        // Calling a blocked writer cannot provoke another native throw.
        arbiter.guardedPlayer(original,
                Minecraft189SprintWriteArbiter.Owner.AUTO_SPRINT)
                .customMcSetSprinting(true);
        assertEquals(0, original.trueWrites);
        arbiter.exit();
    }
}
