package dev.trexzo.custommc.platform.v1_8_9;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Genuine snapshot *policy* tests; no synthetic samples certify a server. */
final class Minecraft189OriginalNonPlayerArrivalWitnessTest {
    private static UUID u(final int number) {
        return new UUID(0L, (long)number);
    }

    private static Minecraft189WorldEntityUuidState.Snapshot ids(int... n) {
        final UUID[] all = new UUID[n.length];
        for (int i=0; i<n.length; i++) all[i] = u(n[i]);
        final Minecraft189WorldEntityUuidState state =
                new Minecraft189WorldEntityUuidState();
        assertTrue(state.update(all));
        return state.snapshot();
    }

    private static Minecraft189WorldEntityKindState.Snapshot kinds(
            int... entityKinds) {
        final Minecraft189WorldEntityKindState state =
                new Minecraft189WorldEntityKindState();
        state.update(entityKinds);
        return state.snapshot();
    }

    private static final int PLAYER = Minecraft189WorldEntityKindState.PLAYER
            | Minecraft189WorldEntityKindState.LIVING;

    @Test void fourGenuineNewNonPlayerUuidsSurviveUnrelatedDespawn() {
        final Minecraft189OriginalNonPlayerArrivalWitness witness =
                new Minecraft189OriginalNonPlayerArrivalWitness();
        assertTrue(witness.captureBaseline(
                ids(1, 2, 10, 11, 12),
                kinds(PLAYER, PLAYER, 0, 0, 0)));
        assertEquals(3, witness.baselineSize());
        // Old fixture: before=3, after=6. +3 would falsely block even
        // though *four new actual nonplayer IDs* arrived (20..23).
        assertEquals(4, witness.countNewNativeNonPlayers(
                ids(2, 11, 20, 21, 22, 23, 1),
                kinds(PLAYER, 0, 0, 0, 0, 0, PLAYER)));
        assertEquals(0, witness.countNewNativeNonPlayers(
                ids(12, 11, 10, 2, 1),
                kinds(0, 0, 0, PLAYER, PLAYER)),
                "entity-list index reorder never invents arrivals");
    }

    @Test void playerUuidNeverCountsTowardFourSentinels() {
        final Minecraft189OriginalNonPlayerArrivalWitness witness =
                new Minecraft189OriginalNonPlayerArrivalWitness();
        assertTrue(witness.captureBaseline(ids(1, 2, 10),
                kinds(PLAYER, PLAYER, 0)));
        assertEquals(3, witness.countNewNativeNonPlayers(
                ids(1, 2, 3, 10, 20, 21, 22),
                kinds(PLAYER, PLAYER, PLAYER, 0, 0, 0, 0)));
    }

    @Test void baselineCannotBeMadeFromMissingOrMismatchedSnapshots() {
        final Minecraft189OriginalNonPlayerArrivalWitness witness =
                new Minecraft189OriginalNonPlayerArrivalWitness();
        assertFalse(witness.captureBaseline(ids(1, 2),
                kinds(PLAYER)));
        assertFalse(witness.captured());
        assertEquals(-1, witness.countNewNativeNonPlayers(
                ids(1, 2), kinds(PLAYER, PLAYER)));
    }

    @Test void invalidOrDuplicateSnapshotCannotClaimArrival() {
        final Minecraft189OriginalNonPlayerArrivalWitness witness =
                new Minecraft189OriginalNonPlayerArrivalWitness();
        assertTrue(witness.captureBaseline(
                ids(1, 10), kinds(PLAYER, 0)));
        final Minecraft189WorldEntityUuidState nativeIds =
                new Minecraft189WorldEntityUuidState();
        assertFalse(nativeIds.update(new UUID[]{u(1), u(1)}));
        assertEquals(-1, witness.countNewNativeNonPlayers(
                nativeIds.snapshot(), kinds(PLAYER, 0)));
        assertEquals(-1, witness.countNewNativeNonPlayers(
                ids(1, 10, 20), kinds(PLAYER, 0)));
    }

    @Test void freshActivationMustReCaptureBaseline() {
        final Minecraft189OriginalNonPlayerArrivalWitness witness =
                new Minecraft189OriginalNonPlayerArrivalWitness();
        assertTrue(witness.captureBaseline(ids(1, 10),
                kinds(PLAYER, 0)));
        assertEquals(1, witness.countNewNativeNonPlayers(
                ids(1, 10, 20), kinds(PLAYER, 0, 0)));
        witness.reset();
        assertFalse(witness.captured());
        assertEquals(-1, witness.countNewNativeNonPlayers(
                ids(1, 10, 20), kinds(PLAYER, 0, 0)));
        assertTrue(witness.captureBaseline(ids(1, 10, 20),
                kinds(PLAYER, 0, 0)));
        assertEquals(0, witness.countNewNativeNonPlayers(
                ids(20, 10, 1), kinds(0, 0, PLAYER)));
    }
}
