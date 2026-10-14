package dev.trexzo.custommc.platform.v1_8_9;

import java.lang.reflect.Field;

/**
 * V35: source-original 1.8.9 vanilla GUI-screen state, read-only and fail-shut.
 *
 * 'ave' and the single declared 'Laxu;' screen field are pinned by the real
 * SHA1-verified Mojang bytecode test. The original runtime class-loader owns
 * the field type; no parent-owned vanilla GUI class may be assumed.
 *
 * This gate cannot invent a Minecraft action. It only rejects placement if a
 * screen is open, the source field is missing/ambiguous, or access is denied.
 */
final class Minecraft189ScaffoldScreenGate {
    enum Verdict { CLEAR, OPEN, UNAVAILABLE }

    private Minecraft189ScaffoldScreenGate() { }

    static Verdict inspect(final Object originalMinecraft) {
        if (!(originalMinecraft instanceof Minecraft189ScaffoldNativeContextAccess)
                || !"ave".equals(originalMinecraft.getClass().getName()))
            return Verdict.UNAVAILABLE;
        return inspectFieldOfType(originalMinecraft, "axu");
    }

    /** Package-private type injection permits deterministic tests without Mojang classes. */
    static Verdict inspectFieldOfType(final Object source, final String guiType) {
        if (source == null || guiType == null || guiType.isEmpty())
            return Verdict.UNAVAILABLE;
        try {
            Field selected = null;
            for (Field candidate : source.getClass().getDeclaredFields()) {
                if (!guiType.equals(candidate.getType().getName())) continue;
                if (selected != null) return Verdict.UNAVAILABLE;
                selected = candidate;
            }
            if (selected == null) return Verdict.UNAVAILABLE;
            selected.setAccessible(true);
            return selected.get(source) == null ? Verdict.CLEAR : Verdict.OPEN;
        } catch (ReflectiveOperationException | RuntimeException unavailable) {
            return Verdict.UNAVAILABLE;
        }
    }
}
