package dev.trexzo.custommc.core.ui;

import java.util.EnumMap;

public final class UiThemes {
    private static final UiTheme DARK_DEFAULT =
            createDarkDefault();

    private UiThemes() {
    }

    public static UiTheme darkDefault() {
        return DARK_DEFAULT;
    }

    private static UiTheme createDarkDefault() {
        final EnumMap<UiColorRole, Integer> colors =
                new EnumMap<UiColorRole, Integer>(
                        UiColorRole.class);

        colors.put(UiColorRole.BACKGROUND, 0xFF0E1116);
        colors.put(UiColorRole.SURFACE, 0xFF151A21);
        colors.put(UiColorRole.SURFACE_RAISED, 0xFF1C232D);
        colors.put(UiColorRole.BORDER, 0xFF2A3442);
        colors.put(UiColorRole.TEXT_PRIMARY, 0xFFF5F7FA);
        colors.put(UiColorRole.TEXT_MUTED, 0xFF9AA8B8);
        colors.put(UiColorRole.ACCENT, 0xFF7C5CFC);
        colors.put(UiColorRole.ACCENT_HOVER, 0xFF9176FF);
        colors.put(UiColorRole.POSITIVE, 0xFF4DD4A8);
        colors.put(UiColorRole.WARNING, 0xFFFFC857);
        colors.put(UiColorRole.DANGER, 0xFFFF6B6B);

        return new UiTheme(
                "dark-default",
                colors);
    }
}
