package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * V298 synthetic test of the recorder invoked by the existing original
 * transformed World return path. Only actual Mojang CI can certify the ray.
 */
final class Minecraft189OriginalScaffoldNativeRayReturnTraceTest {
    private static final String PROPERTY =
            "custommc.acceptance.requireNativeScaffoldRayTrace";
    private String previous;

    @BeforeEach void init() {
        previous = System.getProperty(PROPERTY);
        Minecraft189OriginalScaffoldNativeRayReturnTrace.reset();
    }

    @AfterEach void restore() {
        if (previous == null) System.clearProperty(PROPERTY);
        else System.setProperty(PROPERTY, previous);
        Minecraft189OriginalScaffoldNativeRayReturnTrace.reset();
    }

    private boolean original(int reason, boolean returned) {
        return Minecraft189OriginalScaffoldNativeRayReturnTrace.recordResult(
                reason, 1, 0, 99, 0, returned);
    }

    @Test void disabledOriginalSourceReportsNoDataAndPreservesFalse() {
        System.clearProperty(PROPERTY);
        assertFalse(original(Minecraft189OriginalScaffoldNativeRayReturnTrace.MISS, false));
        assertEquals(0, Minecraft189OriginalScaffoldNativeRayReturnTrace.snapshot().sourceCalls);
    }

    @Test void disabledOriginalSourcePreservesTrueEvenIfDiagnosticReasonFalse() {
        System.clearProperty(PROPERTY);
        assertTrue(original(Minecraft189OriginalScaffoldNativeRayReturnTrace.MISS, true));
        assertEquals(0, Minecraft189OriginalScaffoldNativeRayReturnTrace.snapshot().expectedUpCalls);
    }

    @Test void genuineExpectedNullRayIsNotAHit() {
        System.setProperty(PROPERTY, "true");
        assertFalse(original(Minecraft189OriginalScaffoldNativeRayReturnTrace.MISS, false));
        final Minecraft189OriginalScaffoldNativeRayReturnTrace.Snapshot s =
                Minecraft189OriginalScaffoldNativeRayReturnTrace.snapshot();
        assertEquals(1, s.miss);
        assertEquals(0, s.exactMatch);
        assertEquals(1, s.expectedUpCalls);
    }

    @Test void nativeNonBlockRayDoesNotBecomeWorldStone() {
        System.setProperty(PROPERTY, "true");
        assertFalse(original(Minecraft189OriginalScaffoldNativeRayReturnTrace.WRONG_TYPE, false));
        assertEquals(1, Minecraft189OriginalScaffoldNativeRayReturnTrace.snapshot().wrongType);
    }

    @Test void missingOriginalEnumFacingDoesNotBecomeUpFace() {
        System.setProperty(PROPERTY, "true");
        assertFalse(original(Minecraft189OriginalScaffoldNativeRayReturnTrace.MISSING_FACE, false));
        assertEquals(1, Minecraft189OriginalScaffoldNativeRayReturnTrace.snapshot().missingFace);
    }

    @Test void originalWorldWrongFacingIsDistinctFromMissingHit() {
        System.setProperty(PROPERTY, "true");
        assertFalse(original(Minecraft189OriginalScaffoldNativeRayReturnTrace.WRONG_FACE, false));
        assertEquals(1, Minecraft189OriginalScaffoldNativeRayReturnTrace.snapshot().wrongFace);
        assertEquals(0, Minecraft189OriginalScaffoldNativeRayReturnTrace.snapshot().miss);
    }

    @Test void originalBlockPosMismatchCannotBeAccepted() {
        System.setProperty(PROPERTY, "true");
        assertFalse(original(Minecraft189OriginalScaffoldNativeRayReturnTrace.WRONG_BLOCK, false));
        assertEquals(1, Minecraft189OriginalScaffoldNativeRayReturnTrace.snapshot().wrongBlock);
        assertEquals(0, Minecraft189OriginalScaffoldNativeRayReturnTrace.snapshot().exactMatch);
    }

    @Test void onlyActualNativeExactMatchCanRetainTrue() {
        System.setProperty(PROPERTY, "true");
        assertTrue(original(Minecraft189OriginalScaffoldNativeRayReturnTrace.EXACT_MATCH, true));
        assertEquals(1, Minecraft189OriginalScaffoldNativeRayReturnTrace.snapshot().exactMatch);
        assertEquals(6, Minecraft189OriginalScaffoldNativeRayReturnTrace.snapshot().lastCode);
    }

    @Test void wrongFaceNegativeFixtureMustNotPolluteExactUpRayHistogram() {
        System.setProperty(PROPERTY, "true");
        assertFalse(Minecraft189OriginalScaffoldNativeRayReturnTrace.recordResult(
                Minecraft189OriginalScaffoldNativeRayReturnTrace.WRONG_FACE,
                2, 0, 99, 0, false));
        assertEquals(0, Minecraft189OriginalScaffoldNativeRayReturnTrace.snapshot().sourceCalls);
    }

    @Test void wrongBlockNegativeFixtureMustNotPolluteExpectedBlock() {
        System.setProperty(PROPERTY, "true");
        assertFalse(Minecraft189OriginalScaffoldNativeRayReturnTrace.recordResult(
                Minecraft189OriginalScaffoldNativeRayReturnTrace.WRONG_BLOCK,
                1, 1, 99, 0, false));
        assertEquals(0, Minecraft189OriginalScaffoldNativeRayReturnTrace.snapshot().sourceCalls);
    }

    @Test void everyActualExpectedNativeReturnGoesIntoExactlyOneReason() {
        System.setProperty(PROPERTY, "true");
        for (int i=1; i<=6; i++) original(i, i==6);
        final Minecraft189OriginalScaffoldNativeRayReturnTrace.Snapshot s =
                Minecraft189OriginalScaffoldNativeRayReturnTrace.snapshot();
        assertEquals(6, s.sourceCalls);
        assertEquals(6, s.miss+s.wrongType+s.missingFace+s.wrongFace
                +s.wrongBlock+s.exactMatch);
    }

    @Test void actualReturnValueIsAlwaysPreservedWithoutAnyExtraSourceRead() {
        System.setProperty(PROPERTY, "true");
        assertTrue(original(Minecraft189OriginalScaffoldNativeRayReturnTrace.MISS, true));
        assertFalse(original(Minecraft189OriginalScaffoldNativeRayReturnTrace.EXACT_MATCH, false));
        assertEquals(2, Minecraft189OriginalScaffoldNativeRayReturnTrace.snapshot().sourceCalls);
    }

    @Test void resetRevokesOriginalWorldAndReasonCounts() {
        System.setProperty(PROPERTY, "true");
        original(Minecraft189OriginalScaffoldNativeRayReturnTrace.MISS,false);
        Minecraft189OriginalScaffoldNativeRayReturnTrace.reset();
        final Minecraft189OriginalScaffoldNativeRayReturnTrace.Snapshot s =
                Minecraft189OriginalScaffoldNativeRayReturnTrace.snapshot();
        assertEquals(0, s.sourceCalls);
        assertEquals(0, s.miss);
        assertEquals(0, s.lastCode);
    }

    @Test void oldSourceSnapshotCannotBeRewrittenByLaterReturn() {
        System.setProperty(PROPERTY, "true");
        original(Minecraft189OriginalScaffoldNativeRayReturnTrace.MISS,false);
        final Minecraft189OriginalScaffoldNativeRayReturnTrace.Snapshot before =
                Minecraft189OriginalScaffoldNativeRayReturnTrace.snapshot();
        original(Minecraft189OriginalScaffoldNativeRayReturnTrace.EXACT_MATCH,true);
        assertEquals(1, before.sourceCalls);
        assertEquals(0, before.exactMatch);
        assertEquals(1, Minecraft189OriginalScaffoldNativeRayReturnTrace.snapshot().exactMatch);
    }
}
