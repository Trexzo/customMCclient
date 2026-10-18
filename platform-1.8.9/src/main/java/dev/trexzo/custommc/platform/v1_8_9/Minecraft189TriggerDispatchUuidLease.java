package dev.trexzo.custommc.platform.v1_8_9;

import java.util.UUID;

/**
 * One synthetic TriggerBot click may be spent only against the SAME
 * original loaded-player UUID that earned its earlier real World proof.
 * The lease never authorizes an attack by itself: the mapped original
 * raycast, native controller and vanilla server remain authoritative.
 */
final class Minecraft189TriggerDispatchUuidLease {
    private UUID expectedUuid;
    private long expectedModuleEpoch;
    private boolean pending;

    synchronized void clear() {
        pending = false;
        expectedUuid = null;
        expectedModuleEpoch = 0;
    }

    synchronized void arm(final UUID originalUuid, final long moduleEpoch) {
        clear();
        if (originalUuid != null) {
            expectedUuid = originalUuid;
            expectedModuleEpoch = moduleEpoch;
            pending = true;
        }
    }

    /** Consume exactly once, including for a rejected/changed native ray. */
    synchronized boolean consume(final UUID currentOriginalUuid,
            final long currentModuleEpoch, final boolean triggerEnabled,
            final boolean clickGuiOpen) {
        final boolean permitted = pending && expectedUuid != null
                && currentOriginalUuid != null
                && expectedUuid.equals(currentOriginalUuid)
                && expectedModuleEpoch == currentModuleEpoch
                && triggerEnabled && !clickGuiOpen;
        clear();
        return permitted;
    }
}
