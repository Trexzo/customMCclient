package dev.trexzo.custommc.platform.v1_8_9;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Pure original-source policy probes, never substitutes for Mojang PvP. */
final class Minecraft189AutoClickerRemoteUuidAcceptanceTest {
    private static final int LOCAL = Minecraft189WorldEntityKindState
            .LOCAL_PLAYER | Minecraft189WorldEntityKindState.PLAYER
            | Minecraft189WorldEntityKindState.LIVING;
    private static final int REMOTE = Minecraft189WorldEntityKindState.PLAYER
            | Minecraft189WorldEntityKindState.LIVING;
    private static final int MOB = Minecraft189WorldEntityKindState.LIVING;
    private static final UUID HOST = new UUID(0L, 1L);
    private static final UUID A = new UUID(0L, 2L);
    private static final UUID B = new UUID(0L, 3L);

    private static Minecraft189WorldEntityUuidState.Snapshot ids(UUID... u) {
        Minecraft189WorldEntityUuidState s = new Minecraft189WorldEntityUuidState();
        assertTrue(s.update(u));
        return s.snapshot();
    }
    private static Minecraft189WorldEntityKindState.Snapshot kinds(int... k) {
        Minecraft189WorldEntityKindState s = new Minecraft189WorldEntityKindState();
        s.update(k);
        return s.snapshot();
    }
    private static boolean admits(UUID expected, UUID actual,
            Minecraft189WorldEntityUuidState.Snapshot i,
            Minecraft189WorldEntityKindState.Snapshot k) {
        return Minecraft189AutoClickerRemoteUuidAcceptance.admits(
                expected, actual, i, k);
    }

    @Test void genuineExpectedRemoteLivingPlayerAndControllerArgumentPass() {
        assertTrue(admits(A,A,ids(HOST,A),kinds(LOCAL,REMOTE)));
        assertTrue(admits(A,A,ids(B,HOST,A),kinds(REMOTE,LOCAL,REMOTE)));
    }
    @Test void wrongRemoteUuidNeverQualifies() {
        assertFalse(admits(A,B,ids(HOST,A,B),kinds(LOCAL,REMOTE,REMOTE)));
        assertFalse(admits(B,A,ids(HOST,A,B),kinds(LOCAL,REMOTE,REMOTE)));
    }
    @Test void localOrMobWithMatchingUuidNeverQualifies() {
        assertFalse(admits(A,A,ids(A,HOST),kinds(LOCAL,REMOTE)));
        assertFalse(admits(A,A,ids(HOST,A),kinds(LOCAL,MOB)));
    }
    @Test void absentNativePlayerCannotBorrowExpectedUuid() {
        assertFalse(admits(A,A,ids(HOST,B),kinds(LOCAL,REMOTE)));
        assertFalse(admits(A,A,ids(),kinds()));
    }
    @Test void mismatchedAndMissingNativeSnapshotsFailClosed() {
        assertFalse(admits(A,A,ids(HOST,A),kinds(LOCAL)));
        assertFalse(admits(A,A,null,kinds(LOCAL,REMOTE)));
        assertFalse(admits(A,A,ids(HOST,A),null));
        Minecraft189WorldEntityUuidState absent = new Minecraft189WorldEntityUuidState();
        assertFalse(admits(A,A,absent.snapshot(),kinds()));
        Minecraft189WorldEntityKindState invalid = new Minecraft189WorldEntityKindState();
        assertFalse(admits(A,A,ids(HOST,A),invalid.snapshot()));
    }
    @Test void missingExpectedOrControllerUuidDoesNotQualify() {
        assertFalse(admits(null,A,ids(HOST,A),kinds(LOCAL,REMOTE)));
        assertFalse(admits(A,null,ids(HOST,A),kinds(LOCAL,REMOTE)));
    }
    @Test void reorderedOriginalNativeIndicesCannotChangeUuidOwnership() {
        assertTrue(admits(A,A,ids(A,HOST),kinds(REMOTE,LOCAL)));
        assertFalse(admits(A,B,ids(A,HOST,B),kinds(REMOTE,LOCAL,REMOTE)));
    }
}
