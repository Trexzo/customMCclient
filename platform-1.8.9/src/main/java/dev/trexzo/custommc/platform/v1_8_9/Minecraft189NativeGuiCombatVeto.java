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
        return blocksOriginalVerdict(
                Minecraft189ScaffoldScreenGate.inspect(minecraft));
    }

    static boolean blocksOriginalVerdict(
            final Minecraft189ScaffoldScreenGate.Verdict screen) {
        return screen != Minecraft189ScaffoldScreenGate.Verdict.CLEAR;
    }
}
