package dev.trexzo.custommc.launcher.profile;

import dev.trexzo.custommc.core.module.ModuleKeyChord;
import dev.trexzo.custommc.core.module.ModuleKeybindAssignments;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.UnknownSettingPolicy;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public final class LauncherProfileState {
    private static final String KEYBIND_PREFIX =
            "@keybind/";
    private static final String CHORD_VERSION =
            "v1";
    private static final Base64.Encoder ENCODER =
            Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER =
            Base64.getUrlDecoder();

    private final SettingRegistry settings;
    private final ModuleKeybindAssignments keybinds;

    public LauncherProfileState(
            final SettingRegistry settings,
            final ModuleKeybindAssignments keybinds) {
        this.settings = Objects.requireNonNull(
                settings,
                "settings");
        this.keybinds = Objects.requireNonNull(
                keybinds,
                "keybinds");
    }

    public Map<String, String> snapshot() {
        final Map<String, String> values =
                new TreeMap<String, String>();

        for (Map.Entry<String, String> entry :
                settings.snapshotEncoded().entrySet()) {
            if (entry.getKey().startsWith(
                    KEYBIND_PREFIX)) {
                throw new IllegalStateException(
                        "setting id uses reserved profile namespace: "
                                + entry.getKey());
            }
            values.put(
                    entry.getKey(),
                    entry.getValue());
        }

        for (Map.Entry<String, ModuleKeyChord> entry :
                keybinds.snapshotOwnedBindings().entrySet()) {
            values.put(
                    KEYBIND_PREFIX + entry.getKey(),
                    encodeChord(entry.getValue()));
        }

        return Collections.unmodifiableMap(values);
    }

    public void apply(
            final Map<String, String> values,
            final UnknownSettingPolicy unknownSettingPolicy) {
        Objects.requireNonNull(values, "values");
        Objects.requireNonNull(
                unknownSettingPolicy,
                "unknownSettingPolicy");

        final Map<String, String> settingValues =
                new LinkedHashMap<String, String>();
        final Map<String, ModuleKeyChord> keybindValues =
                new LinkedHashMap<String, ModuleKeyChord>();

        for (Map.Entry<String, String> entry :
                values.entrySet()) {
            final String key =
                    Objects.requireNonNull(
                            entry.getKey(),
                            "profile key");
            final String value =
                    Objects.requireNonNull(
                            entry.getValue(),
                            "profile value");

            if (!key.startsWith(
                    KEYBIND_PREFIX)) {
                settingValues.put(key, value);
                continue;
            }

            final String moduleId =
                    key.substring(
                            KEYBIND_PREFIX.length())
                            .trim();
            if (moduleId.isEmpty()) {
                throw new ProfileFormatException(
                        "blank module id in keybind profile key");
            }

            keybindValues.put(
                    moduleId,
                    decodeChord(value));
        }

        final Map<String, String> previousSettings =
                settings.snapshotEncoded();

        settings.applyEncoded(
                settingValues,
                unknownSettingPolicy);

        try {
            keybinds.replaceOwnedBindings(
                    keybindValues);
        } catch (RuntimeException failure) {
            try {
                settings.applyEncoded(
                        previousSettings,
                        UnknownSettingPolicy.REJECT);
            } catch (RuntimeException restoreFailure) {
                failure.addSuppressed(
                        restoreFailure);
            }
            throw failure;
        }
    }

    private static String encodeChord(
            final ModuleKeyChord chord) {
        final int mask =
                (chord.shift() ? 1 : 0)
                        | (chord.control() ? 2 : 0)
                        | (chord.alt() ? 4 : 0);
        return CHORD_VERSION
                + ":"
                + ENCODER.encodeToString(
                chord.keyId().getBytes(
                        StandardCharsets.UTF_8))
                + ":"
                + mask;
    }

    private static ModuleKeyChord decodeChord(
            final String encoded) {
        final String[] parts =
                encoded.split(":", -1);
        if (parts.length != 3
                || !CHORD_VERSION.equals(
                parts[0])) {
            throw new ProfileFormatException(
                    "unsupported keybind encoding");
        }

        final String keyId;
        try {
            keyId =
                    new String(
                            DECODER.decode(
                                    parts[1]),
                            StandardCharsets.UTF_8);
        } catch (IllegalArgumentException invalid) {
            throw new ProfileFormatException(
                    "invalid keybind key encoding",
                    invalid);
        }

        final int mask;
        try {
            mask = Integer.parseInt(
                    parts[2]);
        } catch (NumberFormatException invalid) {
            throw new ProfileFormatException(
                    "invalid keybind modifier mask",
                    invalid);
        }

        if (mask < 0 || mask > 7) {
            throw new ProfileFormatException(
                    "invalid keybind modifier mask");
        }

        try {
            return new ModuleKeyChord(
                    keyId,
                    (mask & 1) != 0,
                    (mask & 2) != 0,
                    (mask & 4) != 0);
        } catch (IllegalArgumentException invalid) {
            throw new ProfileFormatException(
                    "invalid keybind chord",
                    invalid);
        }
    }
}
