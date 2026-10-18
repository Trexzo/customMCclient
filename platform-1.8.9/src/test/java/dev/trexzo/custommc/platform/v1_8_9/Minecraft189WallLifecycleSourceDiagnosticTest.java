package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Reject-only reasons for actual source-native Wall Check phase 2. Mocked
 * booleans do NOT supply original-world occlusion or server damage proof.
 */
final class Minecraft189WallLifecycleSourceDiagnosticTest {
    private static String classify(int index,
            boolean kindsAvailable, boolean combatAvailable,
            boolean visibilityAvailable, int kindsSize, int combatSize,
            int visibilitySize, boolean player, boolean living,
            boolean local, boolean alive, boolean occluded,
            boolean aura, boolean wall, boolean nearest, boolean rotation) {
        return Minecraft189RuntimeBridge.originalWallLifecycleSourceReason(
                index, kindsAvailable, combatAvailable, visibilityAvailable,
                kindsSize, combatSize, visibilitySize, player, living, local,
                alive, occluded, aura, wall, nearest, rotation);
    }

    private static String healthy(boolean selected, boolean rotation) {
        return classify(1, true, true, true, 2, 2, 2, true, true,
                false, true, true, true, true, selected, rotation);
    }

    @Test void actualOccludedRemoteSourceRequiresBothTargetSnapshotsCleared() {
        assertEquals("real-occluded-and-both-target-snapshots-cleared",
                healthy(false, false));
        assertEquals("stale-original-target-still-selected",
                healthy(true, false),
                "native nearest-found alone resets actual 30-tick gate");
        assertEquals("stale-original-target-still-selected",
                healthy(false, true),
                "native rotation-available alone also resets 30-tick gate");
        assertEquals("stale-original-target-still-selected",
                healthy(true, true));
    }

    @Test void sourceEntityCannotDisappearOrChangeKindWithoutRejecting() {
        assertEquals("no-original-target-index",
                classify(-1, true, true, true, 2, 2, 2,
                        true, true, false, true, true, true, true, false, false));
        assertEquals("source-index-outside-original-world",
                classify(2, true, true, true, 2, 2, 2,
                        true, true, false, true, true, true, true, false, false));
        assertEquals("source-index-outside-original-world",
                classify(1, true, true, true, 2, 1, 2,
                        true, true, false, true, true, true, true, false, false));
        assertEquals("source-entity-no-longer-remote-living-player",
                classify(1, true, true, true, 2, 2, 2,
                        false, true, false, true, true, true, true, false, false));
        assertEquals("source-entity-no-longer-remote-living-player",
                classify(1, true, true, true, 2, 2, 2,
                        true, true, true, true, true, true, true, false, false));
        assertEquals("source-entity-no-longer-remote-living-player",
                classify(1, true, true, true, 2, 2, 2,
                        true, true, false, false, true, true, true, false, false));
    }

    @Test void unavailableOriginalSnapshotsAreNotEvidenceOfHiddenTarget() {
        assertEquals("native-world-snapshot-unavailable",
                classify(1, false, true, true, 2, 2, 2,
                        true, true, false, true, true, true, true, false, false));
        assertEquals("native-world-snapshot-unavailable",
                classify(1, true, false, true, 2, 2, 2,
                        true, true, false, true, true, true, true, false, false));
        assertEquals("native-world-snapshot-unavailable",
                classify(1, true, true, false, 2, 2, 2,
                        true, true, false, true, true, true, true, false, false));
    }

    @Test void nativeLineOfSightClearingCannotMasqueradeAsOccluded() {
        assertEquals("original-source-line-of-sight-no-longer-occluded",
                classify(1, true, true, true, 2, 2, 2,
                        true, true, false, true, false, true, true, false, false));
    }

    @Test void originalModuleDisableNeverCountsAsValidRecoveryVeto() {
        assertEquals("source-aura-disabled",
                classify(1, true, true, true, 2, 2, 2,
                        true, true, false, true, true, false, true, false, false));
        assertEquals("source-wall-check-disabled",
                classify(1, true, true, true, 2, 2, 2,
                        true, true, false, true, true, true, false, false, false));
    }

    @Test void originalStrictWallLifecycleThirtyTickWitnessIsStillRequired() {
        // This classifier is never used to increment any counter. Read
        // the source to guard accidental "always PASS" acceptance later.
        final String result = healthy(false, false);
        assertNotNull(result);
        assertFalse(result.contains("_PASS="));
        assertFalse(result.contains("30"));
        assertEquals("real-occluded-and-both-target-snapshots-cleared", result);
    }
}
