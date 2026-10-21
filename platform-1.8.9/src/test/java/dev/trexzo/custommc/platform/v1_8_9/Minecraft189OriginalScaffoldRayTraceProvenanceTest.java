package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Original V19 source-only diagnostics, never authentic Mojang ray proof. */
final class Minecraft189OriginalScaffoldRayTraceProvenanceTest {
    private static final int STONE =
            Minecraft189ScaffoldWorldReadAccess.FULL_SOLID;

    @Test void initialStateIsNotFabricatedAsOriginalRayAcceptance() {
        final Minecraft189OriginalScaffoldRayTraceProvenance.Snapshot s =
                new Minecraft189OriginalScaffoldRayTraceProvenance().snapshot();
        assertEquals(0, s.sourceCallbacks);
        assertEquals(0, s.correctFaceHits);
        assertEquals(0, s.fullSolidCells);
        assertEquals("not-observed", s.lastDecision);
        assertEquals(Integer.MIN_VALUE, s.lastCellCode);
    }

    @Test void absentRealWorldCellInterfaceCannotCountAsAStone() {
        final Minecraft189OriginalScaffoldRayTraceProvenance t =
                new Minecraft189OriginalScaffoldRayTraceProvenance();
        t.observeInterfaces(false, true);
        final Minecraft189OriginalScaffoldRayTraceProvenance.Snapshot s =
                t.snapshot();
        assertEquals(1, s.sourceCallbacks);
        assertEquals(1, s.missingInterfaces);
        assertEquals(0, s.worldCellChecks);
        assertEquals("original-world-cell-interface-unavailable", s.lastDecision);
    }

    @Test void absentRayInterfaceCannotClaimRealUpFace() {
        final Minecraft189OriginalScaffoldRayTraceProvenance t =
                new Minecraft189OriginalScaffoldRayTraceProvenance();
        t.observeInterfaces(true, false);
        assertEquals("original-world-ray-interface-unavailable",
                t.snapshot().lastDecision);
        assertEquals(0, t.snapshot().correctFaceRayCalls);
    }

    @Test void presentTransformedInterfacesAreNotThemselvesRayEvidence() {
        final Minecraft189OriginalScaffoldRayTraceProvenance t =
                new Minecraft189OriginalScaffoldRayTraceProvenance();
        t.observeInterfaces(true, true);
        assertEquals(1, t.snapshot().sourceCallbacks);
        assertEquals(0, t.snapshot().missingInterfaces);
        assertEquals(0, t.snapshot().correctFaceRayCalls);
    }

    @Test void originalStoneCellCodeMustExactlyMatchFullSolid() {
        final Minecraft189OriginalScaffoldRayTraceProvenance t =
                new Minecraft189OriginalScaffoldRayTraceProvenance();
        t.observeInterfaces(true, true);
        t.observeCell(STONE, STONE);
        final Minecraft189OriginalScaffoldRayTraceProvenance.Snapshot s =
                t.snapshot();
        assertEquals(1, s.worldCellChecks);
        assertEquals(1, s.fullSolidCells);
        assertEquals(0, s.unavailableOrOtherCells);
        assertEquals(STONE, s.lastCellCode);
        assertEquals(0, s.correctFaceRayCalls);
    }

    @Test void unloadedOrAirWorldCellCannotBecomeOriginalStoneProof() {
        final Minecraft189OriginalScaffoldRayTraceProvenance t =
                new Minecraft189OriginalScaffoldRayTraceProvenance();
        t.observeCell(-1, STONE);
        t.observeCell(STONE == 0 ? 1 : 0, STONE);
        assertEquals(0, t.snapshot().fullSolidCells);
        assertEquals(2, t.snapshot().unavailableOrOtherCells);
        assertEquals("original-client-cell-not-full-solid",
                t.snapshot().lastDecision);
    }

    @Test void aFalseCorrectFaceRemainsARealNativeRayMiss() {
        final Minecraft189OriginalScaffoldRayTraceProvenance t =
                new Minecraft189OriginalScaffoldRayTraceProvenance();
        t.observeInterfaces(true, true);
        t.observeCell(STONE, STONE);
        t.observeRayResults(false, false, false);
        final Minecraft189OriginalScaffoldRayTraceProvenance.Snapshot s =
                t.snapshot();
        assertEquals(1, s.correctFaceRayCalls);
        assertEquals(0, s.correctFaceHits);
        assertEquals(0, s.wrongFaceHits);
        assertEquals(0, s.wrongBlockHits);
        assertEquals("original-correct-ray-up-face-missed", s.lastDecision);
    }

    @Test void correctNativeSourceFaceRetainsPositiveButNotMarker() {
        final Minecraft189OriginalScaffoldRayTraceProvenance t =
                new Minecraft189OriginalScaffoldRayTraceProvenance();
        t.observeRayResults(true, false, false);
        assertEquals(1, t.snapshot().correctFaceHits);
        assertEquals("original-correct-ray-up-face", t.snapshot().lastDecision);
        assertFalse(t.snapshot().toString().contains("PASS=YES"));
    }

    @Test void wrongFaceAndWrongBlockAreBothVisibleAndNotAuthorized() {
        final Minecraft189OriginalScaffoldRayTraceProvenance t =
                new Minecraft189OriginalScaffoldRayTraceProvenance();
        t.observeRayResults(true, true, true);
        final Minecraft189OriginalScaffoldRayTraceProvenance.Snapshot s =
                t.snapshot();
        assertEquals(1, s.wrongFaceHits);
        assertEquals(1, s.wrongBlockHits);
        assertEquals("original-negative-ray-accepted-unexpectedly",
                s.lastDecision);
    }

    @Test void multipleOriginalSourceCallbacksRetainAccounting() {
        final Minecraft189OriginalScaffoldRayTraceProvenance t =
                new Minecraft189OriginalScaffoldRayTraceProvenance();
        t.observeInterfaces(false, true);
        t.observeInterfaces(true, true);
        t.observeCell(-1, STONE);
        t.observeInterfaces(true, true);
        t.observeCell(STONE, STONE);
        t.observeRayResults(false, false, false);
        final Minecraft189OriginalScaffoldRayTraceProvenance.Snapshot s =
                t.snapshot();
        assertEquals(3, s.sourceCallbacks);
        assertEquals(1, s.missingInterfaces);
        assertEquals(2, s.worldCellChecks);
        assertEquals(1, s.fullSolidCells);
        assertEquals(1, s.unavailableOrOtherCells);
        assertEquals(1, s.correctFaceRayCalls);
        assertEquals(0, s.correctFaceHits);
    }

    @Test void oldOriginalSourceSnapshotIsNotOverwrittenByLaterCalls() {
        final Minecraft189OriginalScaffoldRayTraceProvenance t =
                new Minecraft189OriginalScaffoldRayTraceProvenance();
        t.observeInterfaces(true, true);
        final Minecraft189OriginalScaffoldRayTraceProvenance.Snapshot old =
                t.snapshot();
        t.observeCell(STONE, STONE);
        assertEquals(0, old.worldCellChecks);
        assertEquals(1, t.snapshot().worldCellChecks);
    }

    @Test void resetRevokesAllOriginalWorldSourceCounters() {
        final Minecraft189OriginalScaffoldRayTraceProvenance t =
                new Minecraft189OriginalScaffoldRayTraceProvenance();
        t.observeInterfaces(true, true);
        t.observeCell(STONE, STONE);
        t.observeRayResults(true, false, false);
        t.reset();
        final Minecraft189OriginalScaffoldRayTraceProvenance.Snapshot s =
                t.snapshot();
        assertEquals(0, s.sourceCallbacks);
        assertEquals(0, s.correctFaceHits);
        assertEquals(0, s.worldCellChecks);
        assertEquals("not-observed", s.lastDecision);
    }
}
