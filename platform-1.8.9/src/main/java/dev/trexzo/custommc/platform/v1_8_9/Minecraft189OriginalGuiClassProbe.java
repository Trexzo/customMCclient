package dev.trexzo.custommc.platform.v1_8_9;

import java.lang.reflect.Field;

/**
 * Acceptance diagnostics ONLY: read the exact type of a vanilla GUI holding
 * Minecraft.currentScreen. The strict OPEN/CLEAR/UNAVAILABLE source gate
 * remains Minecraft189ScaffoldScreenGate; this helper never grants gameplay.
 *
 * Never call screen.toString(), never infer from X11 focus, never inject
 * player input, and never mutate the source original GuiScreen.
 */
final class Minecraft189OriginalGuiClassProbe {
    static final String UNAVAILABLE = "UNAVAILABLE";
    static final String CLEAR = "CLEAR";

    private Minecraft189OriginalGuiClassProbe() {
    }

    static String inspect(final Object originalMinecraft) {
        if (!(originalMinecraft instanceof Minecraft189ScaffoldNativeContextAccess)
                || !"ave".equals(originalMinecraft.getClass().getName()))
            return UNAVAILABLE;
        return inspectFieldOfType(originalMinecraft, "axu");
    }

    /** Test seam: preserves exact declared field-type matching and ambiguity veto. */
    static String inspectFieldOfType(final Object source,
            final String screenType) {
        if (source == null || screenType == null || screenType.isEmpty())
            return UNAVAILABLE;
        try {
            Field selected = null;
            for (Field field : source.getClass().getDeclaredFields()) {
                if (!screenType.equals(field.getType().getName()))
                    continue;
                if (selected != null) return UNAVAILABLE;
                selected = field;
            }
            if (selected == null) return UNAVAILABLE;
            selected.setAccessible(true);
            final Object screen = selected.get(source);
            return screen == null ? CLEAR : screen.getClass().getName();
        } catch (ReflectiveOperationException | RuntimeException failure) {
            return UNAVAILABLE;
        }
    }
}
