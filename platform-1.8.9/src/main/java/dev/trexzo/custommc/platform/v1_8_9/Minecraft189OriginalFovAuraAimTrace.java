package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Opt-in original FOV acceptance diagnostics: records the INPUTS and return
 * value of the unmodified production Aura aim call. It never owns rotation,
 * a World snapshot, player control, attack authorization or source reads.
 *
 * An eligible aim return does not prove that a guarded yaw setter succeeded.
 * Conversely, ineligible aim callbacks do not prove the vanilla client could
 * not rotate by mouse, server teleport, or another upstream mechanism.
 */
final class Minecraft189OriginalFovAuraAimTrace {
    private int callbacks;
    private int eligibleCallbacks;
    private float lastPreAimYaw = Float.NaN;
    private float lastEligiblePreAimYaw = Float.NaN;
    private float lastEligibleSelectedYaw = Float.NaN;
    private int lastEligibleSelectedIndex = -1;

    synchronized void reset() {
        callbacks = 0;
        eligibleCallbacks = 0;
        lastPreAimYaw = Float.NaN;
        lastEligiblePreAimYaw = Float.NaN;
        lastEligibleSelectedYaw = Float.NaN;
        lastEligibleSelectedIndex = -1;
    }

    synchronized void observe(final boolean playerYawAvailable,
            final float preAimYaw, final boolean selectedAvailable,
            final int selectedIndex, final float selectedYaw,
            final boolean originalAuraAimReturnedTrue) {
        callbacks++;
        lastPreAimYaw = playerYawAvailable && Float.isFinite(preAimYaw)
                ? preAimYaw : Float.NaN;
        if (originalAuraAimReturnedTrue) {
            eligibleCallbacks++;
            lastEligiblePreAimYaw = lastPreAimYaw;
            lastEligibleSelectedYaw = selectedAvailable
                    && Float.isFinite(selectedYaw)
                    ? selectedYaw : Float.NaN;
            lastEligibleSelectedIndex = selectedAvailable
                    ? selectedIndex : -1;
        }
    }

    synchronized Snapshot snapshot() {
        return new Snapshot(callbacks, eligibleCallbacks, lastPreAimYaw,
                lastEligiblePreAimYaw, lastEligibleSelectedYaw,
                lastEligibleSelectedIndex);
    }

    static final class Snapshot {
        final int callbacks;
        final int eligibleCallbacks;
        final float lastPreAimYaw;
        final float lastEligiblePreAimYaw;
        final float lastEligibleSelectedYaw;
        final int lastEligibleSelectedIndex;

        private Snapshot(final int total, final int eligible,
                final float sourceYaw, final float eligibleYaw,
                final float selectedYaw, final int index) {
            callbacks = total;
            eligibleCallbacks = eligible;
            lastPreAimYaw = sourceYaw;
            lastEligiblePreAimYaw = eligibleYaw;
            lastEligibleSelectedYaw = selectedYaw;
            lastEligibleSelectedIndex = index;
        }
    }
}
