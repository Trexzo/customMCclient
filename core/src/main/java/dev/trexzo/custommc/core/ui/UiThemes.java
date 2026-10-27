package dev.trexzo.custommc.core.ui;

import java.util.EnumMap;

/**
 * Minecraft-first visual palette. Graphite is the default; it is not the
 * discontinued Nebula identity. Palette roles are shared with the 1.8.9
 * ClickGUI, ArrayList and TargetHUD.
 */
public final class UiThemes {
    private static final UiTheme GRAPHITE = create("graphite",
            0xFF101113, 0xFF191B1F, 0xFF23262B, 0xFF30343B,
            0xFFEE8755, 0xFFFFA171);
    private static final UiTheme BLUE = create("blue",
            0xFF101217, 0xFF191D25, 0xFF222936, 0xFF323B4C,
            0xFF5994EA, 0xFF79ADF2);
    private static final UiTheme FROST = create("frost",
            0xFF10181B, 0xFF18252A, 0xFF22333A, 0xFF34434B,
            0xFF4DC4CD, 0xFF7DDCE3);
    private static final UiTheme MONO = create("mono",
            0xFF101113, 0xFF191B1E, 0xFF25272C, 0xFF373B42,
            0xFFD6DCE3, 0xFFF7FAFC);
    // Historic API id retained so callers/tests using darkDefault() work,
    // but its actual colors are graphite, not the old purple/blue look.
    private static final UiTheme DARK_DEFAULT = create("dark-default",
            0xFF101113, 0xFF191B1F, 0xFF23262B, 0xFF30343B,
            0xFFEE8755, 0xFFFFA171);

    private UiThemes() { }

    public static UiTheme darkDefault() { return DARK_DEFAULT; }
    public static UiTheme graphite() { return GRAPHITE; }
    public static UiTheme blue() { return BLUE; }
    public static UiTheme frost() { return FROST; }
    public static UiTheme mono() { return MONO; }

    /**
     * Binary compatibility only for earlier UI prototypes. This is no
     * longer a user-selectable color scheme or an active design identity.
     */
    @Deprecated
    public static UiTheme nebula() { return GRAPHITE; }

    public static UiTheme byId(final String id) {
        if ("graphite".equals(id) || "nebula".equals(id)) return GRAPHITE;
        if ("blue".equals(id)) return BLUE;
        if ("frost".equals(id)) return FROST;
        if ("mono".equals(id)) return MONO;
        if ("dark-default".equals(id)) return DARK_DEFAULT;
        throw new IllegalArgumentException("unknown theme: " + id);
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
        colors.put(UiColorRole.TEXT_PRIMARY, 0xFFF2F4F6);
        colors.put(UiColorRole.TEXT_MUTED, 0xFF9EA5AE);
        colors.put(UiColorRole.ACCENT, accent);
        colors.put(UiColorRole.ACCENT_HOVER, hover);
        colors.put(UiColorRole.POSITIVE, 0xFF81CBAA);
        colors.put(UiColorRole.WARNING, 0xFFE9B869);
        colors.put(UiColorRole.DANGER, 0xFFE67B7D);
        return new UiTheme(id, colors);
    }
}
