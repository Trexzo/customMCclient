package dev.trexzo.custommc.platform.v1_8_9;

import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Offline original-shaped native entity source regressions for V308.
 * These tests do not certify a Mojang binary, World or vanilla server.
 */
final class Minecraft189RejoinNativeNovelSentinelGateTest {
    private static final int PLAYER =
            Minecraft189WorldEntityKindState.PLAYER
            | Minecraft189WorldEntityKindState.LIVING;
    private static final int LOCAL =
            PLAYER | Minecraft189WorldEntityKindState.LOCAL_PLAYER;
    private static final int NONPLAYER = 0;

    private static Minecraft189WorldEntityUuidState.Snapshot ids(
            final long... values) {
        final Minecraft189WorldEntityUuidState state =
                new Minecraft189WorldEntityUuidState();
        final UUID[] valuesAsIds = new UUID[values.length];
        for (int i=0; i<values.length; i++)
            valuesAsIds[i] = new UUID(0L, values[i]);
        assertTrue(state.update(valuesAsIds));
        return state.snapshot();
    }

    private static Minecraft189WorldEntityKindState.Snapshot kinds(
            final int... flags) {
        final Minecraft189WorldEntityKindState state =
                new Minecraft189WorldEntityKindState();
        state.update(flags);
        return state.snapshot();
    }

    private static Set<UUID> baseline(
            final Minecraft189WorldEntityUuidState.Snapshot sourceIds,
            final Minecraft189WorldEntityKindState.Snapshot sourceKinds) {
        return Minecraft189RejoinTargetAcceptance
                .m273BaselineNonplayerUuids(sourceIds, sourceKinds);
    }

    private static boolean qualifies(
            final int oldCount, final Set<UUID> oldUuids,
            final Minecraft189WorldEntityUuidState.Snapshot sourceIds,
            final Minecraft189WorldEntityKindState.Snapshot sourceKinds) {
        return Minecraft189RejoinTargetAcceptance
                .m308StrictOriginalNovelSentinelGate(
                        oldCount, oldUuids, sourceIds, sourceKinds);
    }

    @Test void fourGenuineNewOriginalNonplayerUuidsWithNetFourPass() {
        final Set<UUID> old=baseline(ids(1,2),kinds(LOCAL,NONPLAYER));
        assertTrue(qualifies(1, old, ids(1,2,3,4,5,6),
                kinds(LOCAL,NONPLAYER,NONPLAYER,NONPLAYER,
                        NONPLAYER,NONPLAYER)));
    }

    @Test void oneTwoOrThreeNewNativeNonplayersNeverQualify() {
        final Set<UUID> old=baseline(ids(1,2),kinds(LOCAL,NONPLAYER));
        assertFalse(qualifies(1,old,ids(1,2,3),
                kinds(LOCAL,NONPLAYER,NONPLAYER)));
        assertFalse(qualifies(1,old,ids(1,2,3,4),
                kinds(LOCAL,NONPLAYER,NONPLAYER,NONPLAYER)));
        assertFalse(qualifies(1,old,ids(1,2,3,4,5),
                kinds(LOCAL,NONPLAYER,NONPLAYER,NONPLAYER,NONPLAYER)));
    }

    @Test void fourNovelWithNoNetIncreaseCannotWeakenOldM417Gate() {
        final Set<UUID> old=baseline(ids(1,2,3,4,5,6),
                kinds(LOCAL,NONPLAYER,NONPLAYER,NONPLAYER,NONPLAYER,PLAYER));
        assertFalse(qualifies(4,old,ids(1,6,7,8,9,10),
                kinds(LOCAL,PLAYER,NONPLAYER,NONPLAYER,
                        NONPLAYER,NONPLAYER)));
    }

    @Test void extraPlayersCannotPretendToBeFourNonplayers() {
        final Set<UUID> old=baseline(ids(1,2),kinds(LOCAL,NONPLAYER));
        assertFalse(qualifies(1,old,ids(1,2,3,4,5,6),
                kinds(LOCAL,NONPLAYER,NONPLAYER,NONPLAYER,PLAYER,PLAYER)));
    }

    @Test void duplicatedNativeUuidWithPositiveCountCannotQualify() {
        final Set<UUID> old=baseline(ids(1,2),kinds(LOCAL,NONPLAYER));
        assertFalse(qualifies(1,old,ids(1,2,3,4,5,5),
                kinds(LOCAL,NONPLAYER,NONPLAYER,NONPLAYER,
                        NONPLAYER,NONPLAYER)));
    }

    @Test void reorderedBaselineUuidsCannotAppearNovel() {
        final Set<UUID> old=baseline(ids(1,2,3,4,5),
                kinds(LOCAL,NONPLAYER,NONPLAYER,NONPLAYER,NONPLAYER));
        assertFalse(qualifies(4,old,ids(5,4,3,2,1),
                kinds(NONPLAYER,NONPLAYER,NONPLAYER,NONPLAYER,LOCAL)));
    }

    @Test void absentAndMismatchedWorldSourcesFailClosed() {
        final Set<UUID> old=baseline(ids(1),kinds(NONPLAYER));
        assertFalse(qualifies(1,null,ids(1,2,3,4,5),
                kinds(NONPLAYER,NONPLAYER,NONPLAYER,NONPLAYER,NONPLAYER)));
        assertFalse(qualifies(1,old,null,
                kinds(NONPLAYER,NONPLAYER)));
        assertFalse(qualifies(1,old,ids(1,2),null));
        assertFalse(qualifies(1,old,ids(1,2,3),
                kinds(NONPLAYER,NONPLAYER)));
        assertFalse(qualifies(-1,old,ids(1,2,3),
                kinds(NONPLAYER,NONPLAYER,NONPLAYER)));
    }

    @Test void overflowingHypotheticalBaselineCountCannotForgeFour() {
        final Set<UUID> old=baseline(ids(),kinds());
        assertFalse(qualifies(Integer.MAX_VALUE-1,old,
                ids(2,3,4,5),kinds(NONPLAYER,NONPLAYER,
                        NONPLAYER,NONPLAYER)));
    }

    @Test void baselineWithZeroOtherEntitiesStillNeedsFourNewUuids() {
        final Set<UUID> old=baseline(ids(1),kinds(LOCAL));
        assertFalse(qualifies(0,old,ids(1,2,3,4),
                kinds(LOCAL,NONPLAYER,NONPLAYER,NONPLAYER)));
        assertTrue(qualifies(0,old,ids(1,2,3,4,5),
                kinds(LOCAL,NONPLAYER,NONPLAYER,NONPLAYER,NONPLAYER)));
    }
}
