package dev.trexzo.custommc.launcher.profile;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleKeyChord;
import dev.trexzo.custommc.core.module.ModuleKeybindAssignments;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleState;
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
    private static final String MODULE_PREFIX =
            "@module/";
    private static final String MODULE_ENABLED =
            "enabled";
    private static final String MODULE_DISABLED =
            "disabled";
    private static final String CHORD_VERSION =
            "v1";
    private static final Base64.Encoder ENCODER =
            Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER =
            Base64.getUrlDecoder();

    private final SettingRegistry settings;
    private final ModuleKeybindAssignments keybinds;
    private final ModuleRegistry modules;
    private final ModuleController moduleController;

    public LauncherProfileState(
            final SettingRegistry settings,
            final ModuleKeybindAssignments keybinds) {
        this(
                settings,
                keybinds,
                null,
                null);
    }

    public LauncherProfileState(
            final SettingRegistry settings,
            final ModuleKeybindAssignments keybinds,
            final ModuleRegistry modules,
            final ModuleController moduleController) {
        this.settings = Objects.requireNonNull(
                settings,
                "settings");
        this.keybinds = Objects.requireNonNull(
                keybinds,
                "keybinds");
        if ((modules == null) != (moduleController == null)) {
            throw new IllegalArgumentException(
                    "modules and moduleController must both be supplied or both omitted");
        }
        this.modules = modules;
        this.moduleController = moduleController;
    }

    public Map<String, String> snapshot() {
        final Map<String, String> values =
                new TreeMap<String, String>();

        for (Map.Entry<String, String> entry :
                settings.snapshotEncoded().entrySet()) {
            if (reservedProfileKey(
                    entry.getKey())) {
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

        if (modules != null) {
            for (Module module :
                    modules.snapshot()) {
                final ModuleState state =
                        moduleController.stateOf(
                                module.id());
                values.put(
                        MODULE_PREFIX + module.id(),
                        encodeModuleState(
                                module.id(),
                                state));
            }
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
        final Map<String, Boolean> moduleValues =
                new LinkedHashMap<String, Boolean>();

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

            if (key.startsWith(
                    KEYBIND_PREFIX)) {
                final String moduleId =
                        profileModuleId(
                                key,
                                KEYBIND_PREFIX,
                                "keybind");
                keybindValues.put(
                        moduleId,
                        decodeChord(value));
                continue;
            }

            if (key.startsWith(
                    MODULE_PREFIX)) {
                final String moduleId =
                        profileModuleId(
                                key,
                                MODULE_PREFIX,
                                "module state");
                moduleValues.put(
                        moduleId,
                        decodeModuleState(
                                value));
                continue;
            }

            settingValues.put(key, value);
        }

        preflightModuleValues(
                moduleValues,
                unknownSettingPolicy);

        final Map<String, String> previousSettings =
                settings.snapshotEncoded();
        final Map<String, ModuleKeyChord> previousKeybinds =
                new LinkedHashMap<String, ModuleKeyChord>(
                        keybinds.snapshotOwnedBindings());
        final Map<String, Boolean> previousModules =
                snapshotModuleStates();

        settings.applyEncoded(
                settingValues,
                unknownSettingPolicy);

        try {
            keybinds.replaceOwnedBindings(
                    keybindValues);
            applyModuleStates(
                    moduleValues,
                    unknownSettingPolicy);
        } catch (RuntimeException failure) {
            rollback(
                    previousSettings,
                    previousKeybinds,
                    previousModules,
                    failure);
            throw failure;
        }
    }

    private void preflightModuleValues(
            final Map<String, Boolean> moduleValues,
            final UnknownSettingPolicy unknownSettingPolicy) {
        if (moduleValues.isEmpty()) {
            return;
        }
        if (modules == null) {
            if (unknownSettingPolicy
                    == UnknownSettingPolicy.REJECT) {
                throw new IllegalArgumentException(
                        "module state profile entries require module state authority");
            }
            return;
        }

        for (String moduleId :
                moduleValues.keySet()) {
            if (modules.find(moduleId) == null
                    && unknownSettingPolicy
                    == UnknownSettingPolicy.REJECT) {
                throw new IllegalArgumentException(
                        "unknown module: " + moduleId);
            }
        }

        snapshotModuleStates();
    }

    private Map<String, Boolean> snapshotModuleStates() {
        final Map<String, Boolean> states =
                new LinkedHashMap<String, Boolean>();
        if (modules == null) {
            return states;
        }

        for (Module module :
                modules.snapshot()) {
            final ModuleState state =
                    moduleController.stateOf(
                            module.id());
            if (state == ModuleState.ENABLED) {
                states.put(
                        module.id(),
                        Boolean.TRUE);
            } else if (state == ModuleState.DISABLED) {
                states.put(
                        module.id(),
                        Boolean.FALSE);
            } else {
                throw new IllegalStateException(
                        "module state is not profile-stable: "
                                + module.id()
                                + "="
                                + state);
            }
        }
        return states;
    }

    private void applyModuleStates(
            final Map<String, Boolean> moduleValues,
            final UnknownSettingPolicy unknownSettingPolicy) {
        if (moduleValues.isEmpty()
                || modules == null) {
            return;
        }

        for (Map.Entry<String, Boolean> entry :
                moduleValues.entrySet()) {
            final String moduleId =
                    entry.getKey();
            if (modules.find(moduleId) == null) {
                if (unknownSettingPolicy
                        == UnknownSettingPolicy.IGNORE) {
                    continue;
                }
                throw new IllegalArgumentException(
                        "unknown module: " + moduleId);
            }
            setModuleState(
                    moduleId,
                    entry.getValue()
                            .booleanValue());
        }
    }

    private void restoreModuleStates(
            final Map<String, Boolean> states) {
        if (modules == null) {
            return;
        }

        for (Map.Entry<String, Boolean> entry :
                states.entrySet()) {
            if (modules.find(
                    entry.getKey()) == null) {
                continue;
            }
            setModuleState(
                    entry.getKey(),
                    entry.getValue()
                            .booleanValue());
        }
    }

    private void setModuleState(
            final String moduleId,
            final boolean enabled) {
        final ModuleState current =
                moduleController.stateOf(
                        moduleId);
        if (enabled) {
            if (current != ModuleState.ENABLED) {
                moduleController.enable(
                        moduleId);
            }
        } else if (current != ModuleState.DISABLED) {
            moduleController.disable(
                    moduleId);
        }
    }

    private void rollback(
            final Map<String, String> previousSettings,
            final Map<String, ModuleKeyChord> previousKeybinds,
            final Map<String, Boolean> previousModules,
            final RuntimeException failure) {
        try {
            restoreModuleStates(
                    previousModules);
        } catch (RuntimeException restoreFailure) {
            failure.addSuppressed(
                    restoreFailure);
        }

        try {
            keybinds.replaceOwnedBindings(
                    previousKeybinds);
        } catch (RuntimeException restoreFailure) {
            failure.addSuppressed(
                    restoreFailure);
        }

        try {
            settings.applyEncoded(
                    previousSettings,
                    UnknownSettingPolicy.REJECT);
        } catch (RuntimeException restoreFailure) {
            failure.addSuppressed(
                    restoreFailure);
        }
    }

    private static boolean reservedProfileKey(
            final String key) {
        return key.startsWith(
                KEYBIND_PREFIX)
                || key.startsWith(
                MODULE_PREFIX);
    }

    private static String profileModuleId(
            final String key,
            final String prefix,
            final String kind) {
        final String moduleId =
                key.substring(
                        prefix.length())
                        .trim();
        if (moduleId.isEmpty()) {
            throw new ProfileFormatException(
                    "blank module id in "
                            + kind
                            + " profile key");
        }
        return moduleId;
    }

    private static String encodeModuleState(
            final String moduleId,
            final ModuleState state) {
        if (state == ModuleState.ENABLED) {
            return MODULE_ENABLED;
        }
        if (state == ModuleState.DISABLED) {
            return MODULE_DISABLED;
        }
        throw new IllegalStateException(
                "module state is not profile-stable: "
                        + moduleId
                        + "="
                        + state);
    }

    private static boolean decodeModuleState(
            final String encoded) {
        if (MODULE_ENABLED.equals(encoded)) {
            return true;
        }
        if (MODULE_DISABLED.equals(encoded)) {
            return false;
        }
        throw new ProfileFormatException(
                "invalid module state encoding");
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
