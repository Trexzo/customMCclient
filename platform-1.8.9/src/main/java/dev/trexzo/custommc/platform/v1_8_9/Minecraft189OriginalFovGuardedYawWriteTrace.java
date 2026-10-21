package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Diagnostic evidence of original guarded yaw setters that returned normally.
 * Has no access to a native player, World, rotation control, or attack gate.
 * A successful wrapper return does not independently prove a remote server
 * observed the yaw; it proves the native setter call completed in this JVM.
 */
final class Minecraft189OriginalFovGuardedYawWriteTrace {
    private int completedWrites;
    private int auraAttributableWrites;
    private float lastCompletedYaw = Float.NaN;
    private float lastAuraCompletedYaw = Float.NaN;

    synchronized void reset() {
        completedWrites = 0;
        auraAttributableWrites = 0;
        lastCompletedYaw = Float.NaN;
        lastAuraCompletedYaw = Float.NaN;
    }

    synchronized void afterOriginalNativeSetter(final float requestedYaw) {
        completedWrites++;
        lastCompletedYaw = requestedYaw;
    }

    synchronized int completedWrites() {
        return completedWrites;
    }

    synchronized void afterAuraAim(final int completedBeforeAim) {
        // Only an actual original setter completed INSIDE the same Aura
        // callback window can be attributed to Aura. No counting of a
        // hypothetical, rejected, or previously completed native setter.
        if (completedBeforeAim < 0 || completedBeforeAim > completedWrites)
            return;
        final int newWrites = completedWrites - completedBeforeAim;
        if (newWrites > 0) {
            auraAttributableWrites += newWrites;
            lastAuraCompletedYaw = lastCompletedYaw;
        }
    }

    synchronized Snapshot snapshot() {
        return new Snapshot(completedWrites, auraAttributableWrites,
                lastCompletedYaw, lastAuraCompletedYaw);
    }

    static final class Snapshot {
        final int completedWrites;
        final int auraAttributableWrites;
        final float lastCompletedYaw;
        final float lastAuraCompletedYaw;

        Snapshot(final int completed, final int auraWrites,
                final float yaw, final float auraYaw) {
            completedWrites = completed;
            auraAttributableWrites = auraWrites;
            lastCompletedYaw = yaw;
            lastAuraCompletedYaw = auraYaw;
        }
    }
}
