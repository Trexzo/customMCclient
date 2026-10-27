package dev.trexzo.custommc.core.ui;

import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodec;
import dev.trexzo.custommc.core.setting.SettingRegistry;

import java.util.Objects;

/**
 * Shared, validated persisted theme choice. "default" deliberately
 * delegates to the caller's theme provider (including a custom host theme).
 */
public final class UiThemeSettings {
    public static final String ID = "ui.theme.preset";
    private static final String DEFAULT = "default";

    private static final SettingCodec<String> CODEC =
            new SettingCodec<String>() {
                @Override
                public String encode(final String value) {
                    return value;
                }
                @Override
                public String decode(final String saved) {
                    // Old profiles are migrated when loaded; no old theme
                    // name or color reaches the live settings UI.
                    return "nebula".equals(saved) ? "graphite" : saved;
                }
            };

    private UiThemeSettings() { }

    public static Setting<String> setting() {
        return new Setting<String>(ID, DEFAULT,
                UiThemeSettings::isValid, CODEC);
    }

    public static boolean isValid(final String id) {
        return DEFAULT.equals(id)
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

    public static String displayName(final String selected) {
        if ("default".equals(selected) || "graphite".equals(selected))
            return "Graphite";
        if ("blue".equals(selected)) return "Blue";
        if ("mono".equals(selected)) return "Mono";
        if ("frost".equals(selected)) return "Frost";
        return "Unknown";
    }

    public static boolean cycle(final Setting<?> setting,
            final boolean forwards) {
        if (setting == null || !ID.equals(setting.id())
                || !(setting.get() instanceof String)) return false;
        final String current = (String) setting.get();
        if (!isValid(current)) return false;
        final String[] choices = {
                "default", "graphite", "blue", "mono", "frost"};
        int index = 0;
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
