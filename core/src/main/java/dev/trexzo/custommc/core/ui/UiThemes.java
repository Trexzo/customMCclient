package dev.trexzo.custommc.core.ui;

import java.util.EnumMap;

public final class UiThemes {
    private static final UiTheme DARK_DEFAULT = createDarkDefault();
    private static final UiTheme NEBULA = create("nebula", 0xFF0B0E14,
            0xFF11161F, 0xFF1B2230, 0xFF273042, 0xFF59A7FF, 0xFF7C6CFF);
    private static final UiTheme FROST = create("frost", 0xFF08131A,
            0xFF10212A, 0xFF1A303B, 0xFF28505D, 0xFF48D5DE, 0xFF83F4ED);
    private static final UiTheme MONO = create("mono", 0xFF0D0F11,
            0xFF16191D, 0xFF22272C, 0xFF3A4149, 0xFFD6DCE4, 0xFFFFFFFF);

    private UiThemes() {
    }

    public static UiTheme darkDefault() {
        return DARK_DEFAULT;
    }

    public static UiTheme nebula() { return NEBULA; }
    public static UiTheme frost() { return FROST; }
    public static UiTheme mono() { return MONO; }

    public static UiTheme byId(final String id) {
        if ("frost".equals(id)) return FROST;
        if ("mono".equals(id)) return MONO;
        if ("nebula".equals(id)) return NEBULA;
        if ("dark-default".equals(id)) return DARK_DEFAULT;
        throw new IllegalArgumentException("unknown theme: " + id);
    }

    private static UiTheme createDarkDefault() {
        return create("dark-default", 0xFF0B0E14, 0xFF11161F,
                0xFF1B2230, 0xFF273042, 0xFF59A7FF, 0xFF7C6CFF);
    }

    private static UiTheme create(final String id,
            final int background, final int surface, final int raised,
            final int border, final int accent, final int hover) {
        final EnumMap<UiColorRole, Integer> colors =
                new EnumMap<UiColorRole, Integer>(UiColorRole.class);
        colors.put(UiColorRole.BACKGROUND, background);
        colors.put(UiColorRole.SURFACE, surface);
        colors.put(UiColorRole.SURFACE_RAISED, raised);
        colors.put(UiColorRole.BORDER, border);
        colors.put(UiColorRole.TEXT_PRIMARY, 0xFFF3F6FB);
        colors.put(UiColorRole.TEXT_MUTED, 0xFF9EAEC4);
        colors.put(UiColorRole.ACCENT, accent);
        colors.put(UiColorRole.ACCENT_HOVER, hover);
        colors.put(UiColorRole.POSITIVE, 0xFF52D7AF);
        colors.put(UiColorRole.WARNING, 0xFFFFC76C);
        colors.put(UiColorRole.DANGER, 0xFFFF7489);
        return new UiTheme(id, colors);
    }
}
