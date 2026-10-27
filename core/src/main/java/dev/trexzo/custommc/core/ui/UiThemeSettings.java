package dev.trexzo.custommc.core.ui;

import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
import dev.trexzo.custommc.core.setting.SettingRegistry;

import java.util.Objects;

/**
 * Shared, validated persisted theme choice. "default" deliberately
 * delegates to the caller's theme provider (including a custom host theme).
 */
public final class UiThemeSettings {
    public static final String ID = "ui.theme.preset";
    private static final String DEFAULT = "default";

    private UiThemeSettings() { }

    public static Setting<String> setting() {
        return new Setting<String>(ID, DEFAULT,
                UiThemeSettings::isValid, SettingCodecs.STRING);
    }

    public static boolean isValid(final String id) {
        // Legacy "nebula" values remain readable but never appear in UI
        // choices. They resolve to graphite rather than restoring its colors.
        return DEFAULT.equals(id) || "nebula".equals(id)
                || "graphite".equals(id) || "blue".equals(id)
                || "frost".equals(id) || "mono".equals(id);
    }

    public static UiTheme resolve(final SettingRegistry settings,
            final UiTheme fallback) {
        Objects.requireNonNull(settings, "settings");
        Objects.requireNonNull(fallback, "fallback");
        final Setting<?> setting = settings.find(ID);
        if (setting == null) return fallback;
        final Object selected = setting.get();
        if (!(selected instanceof String) || !isValid((String) selected)
                || DEFAULT.equals(selected)) return fallback;
        return UiThemes.byId((String) selected);
    }

    public static boolean cycle(final Setting<?> setting,
            final boolean forwards) {
        if (setting == null || !ID.equals(setting.id())
                || !(setting.get() instanceof String)) return false;
        final String current = (String) setting.get();
        if (!isValid(current)) return false;
        final String[] choices = {
                "default", "graphite", "blue", "mono", "frost"};
        // A saved retired preset starts at the nearest modern equivalent.
        int index = "nebula".equals(current) ? 1 : 0;
        for (int i = 0; i < choices.length; i++) {
            if (choices[i].equals(current)) { index = i; break; }
        }
        final int next = (index + (forwards ? 1 : choices.length - 1))
                % choices.length;
        @SuppressWarnings("unchecked")
        final Setting<String> typed = (Setting<String>) setting;
        typed.set(choices[next]);
        return true;
    }
}
