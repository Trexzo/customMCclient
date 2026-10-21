package dev.trexzo.custommc.platform.v1_8_9;

/**
 * V288 opt-in genuine original nonplayer raycast diagnostic only.
 * Records existing decision branches without native getters, entity edits,
 * targeting, click scheduling, or changing acceptance gate thresholds.
 */
final class Minecraft189OriginalNonPlayerRaycastTrace {
    private int calls;
    private int validCallbacks;
    // Raw consecutive raycast acceptances are NOT a native-identity
    // continuity witness; only the original bridge retains that authority.
    private int currentAcceptedRayStreak;
    private int maximumAcceptedRayStreak;
    private int missRayCount;
    private int wrongRayTypeCount;
    private int worldMismatchCount;
    private int identityMissingCount;
    private int kindIndexMismatchCount;
    private int invalidKindCount;
    private int reflectionUnavailableCount;
    private int unavailableSourceCount;
    private int otherRejections;
    private String lastDecision = "not-observed";
    private int lastNativeIndex = -1;
    private int lastNativeKindBits = -1;
    private int lastNativeEntityCount = -1;
    private int lastSourceKindCount = -1;

    synchronized void reset() {
        calls = 0;
        validCallbacks = 0;
        currentAcceptedRayStreak = 0;
        maximumAcceptedRayStreak = 0;
        missRayCount = 0;
        wrongRayTypeCount = 0;
        worldMismatchCount = 0;
        identityMissingCount = 0;
        kindIndexMismatchCount = 0;
        invalidKindCount = 0;
        reflectionUnavailableCount = 0;
        unavailableSourceCount = 0;
        otherRejections = 0;
        lastDecision = "not-observed";
        lastNativeIndex = -1;
        lastNativeKindBits = -1;
        lastNativeEntityCount = -1;
        lastSourceKindCount = -1;
    }

    synchronized void observe(final String decision, final int index,
            final int bits, final int nativeEntityCount,
            final int sourceKindCount, final boolean accepted) {
        if (decision == null || decision.isEmpty())
            throw new IllegalArgumentException("nonplayer diagnostic decision missing");
        calls++;
        if (accepted) {
            validCallbacks++;
            currentAcceptedRayStreak++;
            if (currentAcceptedRayStreak > maximumAcceptedRayStreak)
                maximumAcceptedRayStreak = currentAcceptedRayStreak;
        } else {
            currentAcceptedRayStreak = 0;
            switch (decision) {
                case "original-crosshair-no-ray":
                    missRayCount++;
                    break;
                case "original-crosshair-not-entity-hit":
                    wrongRayTypeCount++;
                    break;
                case "original-world-identity-changed":
                    worldMismatchCount++;
                    break;
                case "original-ray-not-in-loaded-entities":
                    identityMissingCount++;
                    break;
                case "original-ray-index-exceeds-kinds":
                    kindIndexMismatchCount++;
                    break;
                case "original-kind-not-living-nonplayer":
                    invalidKindCount++;
                    break;
                case "original-ray-reflection-unavailable":
                    reflectionUnavailableCount++;
                    break;
                case "native-input-or-kind-source-unavailable":
                case "original-world-source-not-observed":
                    unavailableSourceCount++;
                    break;
                default:
                    otherRejections++;
                    break;
            }
        }
        lastDecision = decision;
        lastNativeIndex = index;
        lastNativeKindBits = bits;
        lastNativeEntityCount = nativeEntityCount;
        lastSourceKindCount = sourceKindCount;
    }

    synchronized Snapshot snapshot() {
        return new Snapshot(calls, validCallbacks, lastDecision,
                lastNativeIndex, lastNativeKindBits, lastNativeEntityCount,
                lastSourceKindCount, currentAcceptedRayStreak,
                maximumAcceptedRayStreak, missRayCount, wrongRayTypeCount,
                worldMismatchCount, identityMissingCount,
                kindIndexMismatchCount, invalidKindCount,
                reflectionUnavailableCount, unavailableSourceCount,
                otherRejections);
    }

    static final class Snapshot {
        final int calls;
        final int validCallbacks;
        final String decision;
        final int nativeIndex;
        final int nativeKindBits;
        final int nativeEntityCount;
        final int sourceKindCount;
        final int currentAcceptedRayStreak;
        final int maximumAcceptedRayStreak;
        final int missRayCount;
        final int wrongRayTypeCount;
        final int worldMismatchCount;
        final int identityMissingCount;
        final int kindIndexMismatchCount;
        final int invalidKindCount;
        final int reflectionUnavailableCount;
        final int unavailableSourceCount;
        final int otherRejections;

        Snapshot(final int calls, final int validCallbacks,
                final String decision, final int nativeIndex,
                final int nativeKindBits, final int nativeEntityCount,
                final int sourceKindCount,
                final int currentAcceptedRayStreak,
                final int maximumAcceptedRayStreak,
                final int missRayCount, final int wrongRayTypeCount,
                final int worldMismatchCount, final int identityMissingCount,
                final int kindIndexMismatchCount, final int invalidKindCount,
                final int reflectionUnavailableCount,
                final int unavailableSourceCount, final int otherRejections) {
            this.calls = calls;
            this.validCallbacks = validCallbacks;
            this.decision = decision;
            this.nativeIndex = nativeIndex;
            this.nativeKindBits = nativeKindBits;
            this.nativeEntityCount = nativeEntityCount;
            this.sourceKindCount = sourceKindCount;
            this.currentAcceptedRayStreak = currentAcceptedRayStreak;
            this.maximumAcceptedRayStreak = maximumAcceptedRayStreak;
            this.missRayCount = missRayCount;
            this.wrongRayTypeCount = wrongRayTypeCount;
            this.worldMismatchCount = worldMismatchCount;
            this.identityMissingCount = identityMissingCount;
            this.kindIndexMismatchCount = kindIndexMismatchCount;
            this.invalidKindCount = invalidKindCount;
            this.reflectionUnavailableCount = reflectionUnavailableCount;
            this.unavailableSourceCount = unavailableSourceCount;
            this.otherRejections = otherRejections;
        }
    }
}
