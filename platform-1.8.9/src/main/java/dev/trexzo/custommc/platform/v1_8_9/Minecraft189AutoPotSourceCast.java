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
        if (host == null || nativeRightClick == null || originalSlot < 0)
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
            nativeRightClick.run();
            host.commitAutoPotUseAttempt();
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
