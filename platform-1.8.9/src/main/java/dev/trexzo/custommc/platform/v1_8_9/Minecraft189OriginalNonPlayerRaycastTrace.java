package dev.trexzo.custommc.platform.v1_8_9;

/**
 * V288 opt-in genuine original nonplayer raycast diagnostic only.
 * Records existing decision branches without native getters, entity edits,
 * targeting, click scheduling, or changing acceptance gate thresholds.
 */
final class Minecraft189OriginalNonPlayerRaycastTrace {
    private int calls;
    private int validCallbacks;
    private String lastDecision = "not-observed";
    private int lastNativeIndex = -1;
    private int lastNativeKindBits = -1;
    private int lastNativeEntityCount = -1;
    private int lastSourceKindCount = -1;

    synchronized void reset() {
        calls = 0;
        validCallbacks = 0;
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
        if (accepted) validCallbacks++;
        lastDecision = decision;
        lastNativeIndex = index;
        lastNativeKindBits = bits;
        lastNativeEntityCount = nativeEntityCount;
        lastSourceKindCount = sourceKindCount;
    }

    synchronized Snapshot snapshot() {
        return new Snapshot(calls, validCallbacks, lastDecision,
                lastNativeIndex, lastNativeKindBits, lastNativeEntityCount,
                lastSourceKindCount);
    }

    static final class Snapshot {
        final int calls;
        final int validCallbacks;
        final String decision;
        final int nativeIndex;
        final int nativeKindBits;
        final int nativeEntityCount;
        final int sourceKindCount;

        Snapshot(final int calls, final int validCallbacks,
                final String decision, final int nativeIndex,
                final int nativeKindBits, final int nativeEntityCount,
                final int sourceKindCount) {
            this.calls = calls;
            this.validCallbacks = validCallbacks;
            this.decision = decision;
            this.nativeIndex = nativeIndex;
            this.nativeKindBits = nativeKindBits;
            this.nativeEntityCount = nativeEntityCount;
            this.sourceKindCount = sourceKindCount;
        }
    }
}
