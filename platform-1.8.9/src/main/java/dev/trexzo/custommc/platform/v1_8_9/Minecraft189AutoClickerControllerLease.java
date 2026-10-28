package dev.trexzo.custommc.platform.v1_8_9;

import java.util.UUID;

/**
 * One source-UUID Auto Clicker trigger admission may reach one original
 * controller attack. No lease invents a hit, controller call or packet.
 *
 * Stage 1: HostRuntime verifies native World/raycast and arms a UUID/epoch.
 * Stage 2: the bridge consumes it exactly once before vanilla clickMouse.
 * Stage 3: the original controller's actual Entity UUID must still match.
 *
 * Manual clicks and ordinary hold-mode Auto Clicker never use this lease.
 */
final class Minecraft189AutoClickerControllerLease {
    static final class Intent {
        private final UUID originalTarget;
        private final long ownerEpoch;

        private Intent(final UUID originalTarget, final long ownerEpoch) {
            this.originalTarget = originalTarget;
            this.ownerEpoch = ownerEpoch;
        }

        boolean permits(final UUID actualControllerUuid, final long liveEpoch,
                final boolean triggerEnabled, final boolean otherOwnerActive,
                final boolean guiOpen) {
            return originalTarget != null && actualControllerUuid != null
                    && originalTarget.equals(actualControllerUuid)
                    && ownerEpoch == liveEpoch && triggerEnabled
                    && !otherOwnerActive && !guiOpen;
        }

        UUID targetUuid() {
            return originalTarget;
        }
    }

    private Intent pending;

    synchronized void clear() {
        pending = null;
    }

    synchronized void arm(final UUID originalTarget, final long ownerEpoch) {
        pending = originalTarget == null ? null :
                new Intent(originalTarget, ownerEpoch);
    }

    synchronized boolean pending() {
        return pending != null;
    }

    /** A failed or stale reservation is spent rather than replayed. */
    synchronized Intent consume(final UUID actualSourceUuid,
            final long currentEpoch, final boolean triggerEnabled,
            final boolean otherOwnerActive, final boolean guiOpen) {
        final Intent granted = pending;
        pending = null;
        return granted != null && granted.permits(actualSourceUuid,
                currentEpoch, triggerEnabled, otherOwnerActive, guiOpen)
                ? granted : null;
    }
}
