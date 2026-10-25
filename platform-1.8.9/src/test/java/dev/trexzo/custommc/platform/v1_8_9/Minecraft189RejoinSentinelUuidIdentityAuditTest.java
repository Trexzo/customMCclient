package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Source-native identity audit: M417 requires both original World
 * net +4 nonplayers AND four distinct novel nonplayer UUIDs. Diagnostics,
 * server commands and reordering alone never authorize native acceptance.
 */
final class Minecraft189RejoinSentinelUuidIdentityAuditTest {
    private static UUID uuid(long n) { return new UUID(0L,n); }

    private static Minecraft189WorldEntityUuidState.Snapshot ids(long... values) {
        Minecraft189WorldEntityUuidState s=new Minecraft189WorldEntityUuidState();
        UUID[] a=new UUID[values.length];
        for(int i=0;i<a.length;i++)a[i]=uuid(values[i]);
        assertTrue(s.update(a));
        return s.snapshot();
    }

    private static Minecraft189WorldEntityKindState.Snapshot kinds(int... bits) {
        Minecraft189WorldEntityKindState s=new Minecraft189WorldEntityKindState();
        s.update(bits);
        return s.snapshot();
    }

    private static Set<UUID> baseline(
            Minecraft189WorldEntityUuidState.Snapshot i,
            Minecraft189WorldEntityKindState.Snapshot k) {
        return Minecraft189RejoinTargetAcceptance.m273BaselineNonplayerUuids(i,k);
    }

    private static int novel(Set<UUID> set,
            Minecraft189WorldEntityUuidState.Snapshot i,
            Minecraft189WorldEntityKindState.Snapshot k) {
        return Minecraft189RejoinTargetAcceptance.m273NovelNativeNonplayerCount(set,i,k);
    }

    private static final int PLAYER=Minecraft189WorldEntityKindState.PLAYER
            | Minecraft189WorldEntityKindState.LIVING;
    private static final int LOCAL=PLAYER
            | Minecraft189WorldEntityKindState.LOCAL_PLAYER;
    private static final int OTHER=0;

    @Test void baselineCopiesOriginalNonplayerUuidIdentityNotEntityIndex() {
        Set<UUID> baseline=baseline(ids(1,2,3),kinds(LOCAL,OTHER,PLAYER));
        assertNotNull(baseline);
        assertEquals(1,baseline.size());
        assertTrue(baseline.contains(uuid(2)));
        assertFalse(baseline.contains(uuid(3)));
    }

    @Test void genuineFourNewOriginalNonplayerUuidsAreDistinguishable() {
        Set<UUID> before=baseline(ids(1,2,3),kinds(LOCAL,OTHER,PLAYER));
        assertEquals(4,novel(before,
                ids(1,2,3,4,5,6,7),
                kinds(LOCAL,OTHER,PLAYER,OTHER,OTHER,OTHER,OTHER)));
    }

    @Test void changedListOrderMustNotForgeNewEntities() {
        Set<UUID> before=baseline(ids(1,2,3),kinds(LOCAL,OTHER,PLAYER));
        assertEquals(0,novel(before,ids(3,2,1),kinds(PLAYER,OTHER,LOCAL)));
    }

    @Test void fourNewEntitiesAndFourOldUnloadsHaveZeroNetIncreaseButFourDistinctBirths(){
        Set<UUID> before=baseline(ids(1,2,3,4,5,6),
                kinds(LOCAL,OTHER,OTHER,OTHER,OTHER,PLAYER));
        assertEquals(4,novel(before,ids(1,6,7,8,9,10),
                kinds(LOCAL,PLAYER,OTHER,OTHER,OTHER,OTHER)));
    }

    @Test void newRealPlayerCannotCountAsArmorStand() {
        Set<UUID> before=baseline(ids(1,2),kinds(LOCAL,OTHER));
        assertEquals(3,novel(before,ids(1,2,3,4,5,6),
                kinds(LOCAL,OTHER,OTHER,OTHER,OTHER,PLAYER)));
    }

    @Test void oneNewNativeNonplayerDoesNotMeetFourThreshold() {
        Set<UUID> before=baseline(ids(1,2),kinds(LOCAL,OTHER));
        assertEquals(1,novel(before,ids(1,2,3),kinds(LOCAL,OTHER,OTHER)));
    }

    @Test void twoNewNativeNonplayersDoNotMeetFourThreshold() {
        Set<UUID> before=baseline(ids(1,2),kinds(LOCAL,OTHER));
        assertEquals(2,novel(before,ids(1,2,3,4),
                kinds(LOCAL,OTHER,OTHER,OTHER)));
    }

    @Test void threeNewNativeNonplayersDoNotMeetFourThreshold() {
        Set<UUID> before=baseline(ids(1,2),kinds(LOCAL,OTHER));
        assertEquals(3,novel(before,ids(1,2,3,4,5),
                kinds(LOCAL,OTHER,OTHER,OTHER,OTHER)));
    }

    @Test void genuinelyNoChangesReportZeroDistinctBirths() {
        Set<UUID> before=baseline(ids(1,2),kinds(LOCAL,OTHER));
        assertEquals(0,novel(before,ids(1,2),kinds(LOCAL,OTHER)));
    }

    @Test void baselineIsFrozenAndCallerCannotRewriteHistoricalNativeEvidence() {
        Set<UUID> before=baseline(ids(1,2),kinds(LOCAL,OTHER));
        assertThrows(UnsupportedOperationException.class,()->before.add(uuid(3)));
        assertEquals(1,before.size());
    }

    @Test void unavailableBaselineNeverQualifies() {
        assertEquals(-1,novel(null,ids(1,2,3,4),kinds(OTHER,OTHER,OTHER,OTHER)));
    }

    @Test void inconsistentOriginalWorldUuidKindLengthsAreRejected() {
        assertNull(baseline(ids(1,2),kinds(OTHER)));
        Set<UUID> before=baseline(ids(1),kinds(OTHER));
        assertEquals(-1,novel(before,ids(1,2),kinds(OTHER)));
    }

    @Test void originalUuidStateMissingNeverQualifies() {
        Minecraft189WorldEntityUuidState unavailable=new Minecraft189WorldEntityUuidState();
        assertNull(baseline(unavailable.snapshot(),kinds()));
        assertEquals(-1,novel(baseline(ids(),kinds()),unavailable.snapshot(),kinds()));
    }

    @Test void originalKindsStateMissingNeverQualifies() {
        Minecraft189WorldEntityKindState unavailable=new Minecraft189WorldEntityKindState();
        assertNull(baseline(ids(),unavailable.snapshot()));
        assertEquals(-1,novel(baseline(ids(),kinds()),ids(),unavailable.snapshot()));
    }

    @Test void newRealLocalPlayerUuidIsNotANewNonplayer() {
        Set<UUID> before=baseline(ids(1,2),kinds(LOCAL,OTHER));
        assertEquals(0,novel(before,ids(1,2,3),kinds(LOCAL,OTHER,LOCAL)));
    }

    @Test void originalNonplayerCountCanShrinkWhileNovelUuidExists() {
        Set<UUID> before=baseline(ids(1,2,3,4),kinds(LOCAL,OTHER,OTHER,OTHER));
        assertEquals(1,novel(before,ids(1,5),kinds(LOCAL,OTHER)));
    }

    @Test void originalM417RequiresBothNetFourAndFourDistinctNewUuids() {
        assertTrue(Minecraft189RejoinTargetAcceptance
                .m309FourSourceNativeSentinelsAdmitted(2, 6, 4));
        assertTrue(Minecraft189RejoinTargetAcceptance
                .m309FourSourceNativeSentinelsAdmitted(10, 18, 5));
        // A net increase without distinct original source IDs is no proof.
        assertFalse(Minecraft189RejoinTargetAcceptance
                .m309FourSourceNativeSentinelsAdmitted(2, 6, 3));
        assertFalse(Minecraft189RejoinTargetAcceptance
                .m309FourSourceNativeSentinelsAdmitted(2, 6, 0));
        // Four distinct births cannot excuse a missing original net gain.
        assertFalse(Minecraft189RejoinTargetAcceptance
                .m309FourSourceNativeSentinelsAdmitted(2, 5, 4));
        assertFalse(Minecraft189RejoinTargetAcceptance
                .m309FourSourceNativeSentinelsAdmitted(2, 2, 4));
    }

    @Test void originalM417MissingOrOverflowedSourceFailsClosed() {
        assertFalse(Minecraft189RejoinTargetAcceptance
                .m309FourSourceNativeSentinelsAdmitted(-1, 4, 4));
        assertFalse(Minecraft189RejoinTargetAcceptance
                .m309FourSourceNativeSentinelsAdmitted(0, -1, 4));
        assertFalse(Minecraft189RejoinTargetAcceptance
                .m309FourSourceNativeSentinelsAdmitted(0, 4, -1));
        assertFalse(Minecraft189RejoinTargetAcceptance
                .m309FourSourceNativeSentinelsAdmitted(
                        Integer.MAX_VALUE - 2, Integer.MAX_VALUE, 4));
        assertTrue(Minecraft189RejoinTargetAcceptance
                .m309FourSourceNativeSentinelsAdmitted(
                        Integer.MAX_VALUE - 4, Integer.MAX_VALUE, 4));
    }

    @Test void originalM417ReorderAndPlayerArrivalsCannotForgeSentinels() {
        final Set<UUID> before = baseline(
                ids(1,2,3), kinds(LOCAL,OTHER,PLAYER));
        final int realFour = novel(before,
                ids(1,2,3,11,12,13,14),
                kinds(LOCAL,OTHER,PLAYER,OTHER,OTHER,OTHER,OTHER));
        assertTrue(Minecraft189RejoinTargetAcceptance
                .m309FourSourceNativeSentinelsAdmitted(1,5,realFour));
        final int reordered = novel(before,
                ids(3,2,1), kinds(PLAYER,OTHER,LOCAL));
        assertFalse(Minecraft189RejoinTargetAcceptance
                .m309FourSourceNativeSentinelsAdmitted(1,5,reordered));
        final int oneIsPlayer = novel(before,
                ids(1,2,3,11,12,13,14),
                kinds(LOCAL,OTHER,PLAYER,OTHER,OTHER,OTHER,PLAYER));
        assertFalse(Minecraft189RejoinTargetAcceptance
                .m309FourSourceNativeSentinelsAdmitted(1,5,oneIsPlayer));
        final int oldUnloads = novel(
                baseline(ids(1,2,3,4,5,6),
                        kinds(LOCAL,OTHER,OTHER,OTHER,OTHER,PLAYER)),
                ids(1,6,7,8,9,10),
                kinds(LOCAL,PLAYER,OTHER,OTHER,OTHER,OTHER));
        assertFalse(Minecraft189RejoinTargetAcceptance
                .m309FourSourceNativeSentinelsAdmitted(4,4,oldUnloads));
    }

    @Test void emptyWorldAtBaselineStillRequiresActualNativeUuidArrivals() {
        Set<UUID> before=baseline(ids(),kinds());
        assertNotNull(before);assertTrue(before.isEmpty());
        assertEquals(0,novel(before,ids(),kinds()));
        assertEquals(4,novel(before,ids(3,4,5,6),
                kinds(OTHER,OTHER,OTHER,OTHER)));
    }
}
