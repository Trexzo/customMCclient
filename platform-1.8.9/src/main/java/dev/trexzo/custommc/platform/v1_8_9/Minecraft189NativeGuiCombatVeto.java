package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Source-only Minecraft 1.8.9 vanilla-GUI admission gate for automated
 * combat. The exact SHA-pinned original class is ave and the sole declared
 * GuiScreen field is of original type axu.
 *
 * No synthetic controller action may be issued from a genuine source with
 * OPEN or UNAVAILABLE GuiScreen. Test doubles remain under their separate
 * deterministic contracts; they cannot certify original screen authority.
 * This never changes manual mouse input, vanilla counters, GUI state or
 * server-owned attack/damage evidence.
 */
final class Minecraft189NativeGuiCombatVeto {
    private Minecraft189NativeGuiCombatVeto() { }

    static boolean blocks(final Object minecraft) {
        if (minecraft == null || !"ave".equals(minecraft.getClass().getName()))
            return false;
        return blocksOriginalVerdict(
                Minecraft189ScaffoldScreenGate.inspect(minecraft));
    }

    static boolean blocksOriginalVerdict(
            final Minecraft189ScaffoldScreenGate.Verdict screen) {
        return screen != Minecraft189ScaffoldScreenGate.Verdict.CLEAR;
    }
}
