package dev.trexzo.custommc.platform.v1_8_9;

/**
 * V333: independent original-vanilla GUI veto for automatic melee.
 *
 * The production target is the SHA-pinned, transformed 1.8.9 'ave' class.
 * We never trust X11 foreground ownership or CustomMC's own GUI state as
 * proof that Minecraft.currentScreen is null. Unknown original screen
 * evidence fails shut; no source counter or mouse state is changed.
 *
 * Non-original unit-test objects remain under their existing fake-host
 * contracts and cannot masquerade as genuine source evidence.
 */
final class Minecraft189NativeGuiCombatVeto {
    private Minecraft189NativeGuiCombatVeto() {
    }

    static boolean blocks(final Object minecraft) {
        if (minecraft == null
                || !"ave".equals(minecraft.getClass().getName())) {
            return false;
        }
        final Minecraft189ScaffoldScreenGate.Verdict verdict =
                Minecraft189ScaffoldScreenGate.inspect(minecraft);
        if (verdict != Minecraft189ScaffoldScreenGate.Verdict.CLEAR
                && (Boolean.getBoolean(
                        "custommc.acceptance.requireAutoClickerExactRemoteUuid")
                    || Boolean.getBoolean(
                        "custommc.acceptance.reportKillAuraRejoin"))) {
            final String nativeClass =
                    Minecraft189OriginalGuiClassProbe.inspect(minecraft);
            reportSourceGuiOnce(verdict, nativeClass);
        }
        return blocksOriginalVerdict(verdict);
    }

    // Read-only original-client log, never input, UUID or attack authority.
    private static final java.util.Set<String> reportedClasses =
            new java.util.LinkedHashSet<String>();

    private static void reportSourceGuiOnce(
            final Minecraft189ScaffoldScreenGate.Verdict verdict,
            final String nativeClass) {
        final String safe = nativeClass == null ? "UNAVAILABLE"
                : nativeClass.substring(0, Math.min(128, nativeClass.length()));
        final String observation = verdict.name() + " class=" + safe;
        synchronized (reportedClasses) {
            if (reportedClasses.size() >= 4
                    || !reportedClasses.add(observation)) return;
        }
        System.out.println("CUSTOMMC_189_V335_NATIVE_GUI_VETO_SOURCE "
                + observation);
        System.out.flush();
    }

    static boolean blocksOriginalVerdict(
            final Minecraft189ScaffoldScreenGate.Verdict screen) {
        return screen != Minecraft189ScaffoldScreenGate.Verdict.CLEAR;
    }
}
