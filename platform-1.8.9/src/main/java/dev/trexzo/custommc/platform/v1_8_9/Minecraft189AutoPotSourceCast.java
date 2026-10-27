package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Single synchronous source-owned original potion use action. Encapsulating
 * the actual right-click callback makes the precise pitch/rollback boundary
 * executable in deterministic tests without an invented network packet.
 */
final class Minecraft189AutoPotSourceCast {
    private Minecraft189AutoPotSourceCast() {}

    static boolean execute(
            final Minecraft189HostRuntime host,
            final int originalSlot,
            final Runnable nativeRightClick) {
        return execute(host, originalSlot, nativeRightClick, () -> true);
    }

    /**
     * Optional extra source admission occurs AFTER mapped pitch/item getters
     * and immediately BEFORE vanilla rightClickMouse. Rejecting this gate
     * must never commit a fabricated potion-use result.
     */
    static boolean execute(
            final Minecraft189HostRuntime host,
            final int originalSlot,
            final Runnable nativeRightClick,
            final java.util.function.BooleanSupplier nativeAdmission) {
        if (host == null || nativeRightClick == null || originalSlot < 0
                || nativeAdmission == null)
            return false;
        final Minecraft189AutoPotPitchLease aim =
                host.beginAutoPotDownwardPitch(originalSlot);
        try {
            if (!host.mayExecuteAutoPotAtPitch(aim, originalSlot))
                return false;
            // Acceptance-only read-only source witnesses bracket the
            // EXACT production rightClickMouse. The pre-click hook runs
            // while the Survival ItemStack still exists; the post-click
            // hook runs only if the actual native callback returned.
            if (Boolean.getBoolean(
                    "custommc.acceptance.requireNativeRodPotionDiscovery"))
                Minecraft189OriginalCombatItemDiscoveryAcceptance
                        .beforeNativeAutoPotUse(host, originalSlot);
            if (!nativeAdmission.getAsBoolean()) return false;
            nativeRightClick.run();
            host.commitAutoPotUseAttempt(originalSlot);
            if (Boolean.getBoolean(
                    "custommc.acceptance.requireNativeRodPotionDiscovery"))
                Minecraft189OriginalCombatItemDiscoveryAcceptance
                        .afterNativeAutoPotUse(host);
            return true;
        } finally {
            if (aim != null) aim.restore();
        }
    }
}
