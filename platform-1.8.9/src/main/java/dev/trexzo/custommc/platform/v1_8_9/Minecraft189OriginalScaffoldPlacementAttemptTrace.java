package dev.trexzo.custommc.platform.v1_8_9;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Acceptance-only original V29 provenance; stores names of already taken
 * production guard branches, never obtains an original World/player/controller
 * or participates in actual permission to place.
 */
final class Minecraft189OriginalScaffoldPlacementAttemptTrace {
    private int originalCalls;
    private int refused;
    private int completedControllerCalls;
    private int controllerAccepted;
    private String lastReason = "not-observed";
    private final TreeMap<String, Integer> reasons = new TreeMap<String, Integer>();

    synchronized void reset() {
        originalCalls = refused = completedControllerCalls = controllerAccepted = 0;
        lastReason = "not-observed";
        reasons.clear();
    }

    synchronized void started() {
        originalCalls++;
    }

    synchronized void rejected(final String reason) {
        if (reason == null || reason.isEmpty())
            throw new IllegalArgumentException("native refusal reason missing");
        refused++;
        lastReason = reason;
        final Integer old = reasons.get(reason);
        reasons.put(reason, old == null ? 1 : old + 1);
    }

    synchronized void completed(final boolean originalControllerAccepted) {
        completedControllerCalls++;
        if (originalControllerAccepted) controllerAccepted++;
        lastReason = originalControllerAccepted
                ? "original-controller-accepted"
                : "original-controller-rejected";
    }

    synchronized Snapshot snapshot() {
        return new Snapshot(originalCalls, refused, completedControllerCalls,
                controllerAccepted, lastReason,
                Collections.unmodifiableMap(new TreeMap<String, Integer>(reasons)));
    }

    static final class Snapshot {
        final int originalCalls;
        final int refused;
        final int completedControllerCalls;
        final int controllerAccepted;
        final String lastReason;
        final Map<String, Integer> reasons;

        Snapshot(int calls, int refused, int controllerCalls,
                int accepted, String reason, Map<String, Integer> reasons) {
            originalCalls = calls;
            this.refused = refused;
            completedControllerCalls = controllerCalls;
            controllerAccepted = accepted;
            lastReason = reason;
            this.reasons = reasons;
        }
    }
}
