package dev.trexzo.custommc.launcher.profile;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleKeyChord;
import dev.trexzo.custommc.core.module.ModuleState;
import dev.trexzo.custommc.core.module.ModuleKeybindAssignments;
import dev.trexzo.custommc.core.module.ModuleKeybindRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.UnknownSettingPolicy;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LauncherProfileStateTest {
    @Test
    void snapshotAndApplyRoundTripSettingsAndOwnedKeybinds() {
        final ModuleRegistry modules =
                modules(
                        "combat.aura",
                        "render.esp");
        final SettingRegistry settings =
                settings();
        final ModuleKeybindAssignments assignments =
                new ModuleKeybindAssignments(
                        new ModuleKeybindRegistry(
                                modules));

        settings.find("combat.range");
        @SuppressWarnings("unchecked")
        final Setting<Integer> range =
                (Setting<Integer>) settings.find(
                        "combat.range");
        range.set(5);

        assignments.bind(
                "combat.aura",
                new ModuleKeyChord(
                        "legacy-key-37",
                        true,
                        false,
                        true));

        final LauncherProfileState profile =
                new LauncherProfileState(
                        settings,
                        assignments);
        final Map<String, String> snapshot =
                profile.snapshot();

        range.set(2);
        assignments.unbind(
                "combat.aura");
        assignments.bind(
                "render.esp",
                ModuleKeyChord.key(
                        "legacy-key-38"));

        profile.apply(
                snapshot,
                UnknownSettingPolicy.REJECT);

        assertEquals(
                Integer.valueOf(5),
                range.get());
        assertEquals(
                new ModuleKeyChord(
                        "legacy-key-37",
                        true,
                        false,
                        true),
                assignments.binding(
                        "combat.aura")
                        .chord());
        assertEquals(
                null,
                assignments.binding(
                        "render.esp"));
    }

    @Test
    void snapshotAndApplyRoundTripStableModuleStates() {
        final ModuleRegistry modules =
                modules(
                        "render.watermark",
                        "render.fps");
        final ModuleController controller =
                new ModuleController(modules);
        final SettingRegistry settings =
                settings();
        final ModuleKeybindAssignments assignments =
                new ModuleKeybindAssignments(
                        new ModuleKeybindRegistry(
                                modules));

        controller.enable(
                "render.watermark");

        final LauncherProfileState profile =
                new LauncherProfileState(
                        settings,
                        assignments,
                        modules,
                        controller);
        final Map<String, String> snapshot =
                profile.snapshot();

        assertEquals(
                "enabled",
                snapshot.get(
                        "@module/render.watermark"));
        assertEquals(
                "disabled",
                snapshot.get(
                        "@module/render.fps"));

        controller.disable(
                "render.watermark");
        controller.enable(
                "render.fps");

        profile.apply(
                snapshot,
                UnknownSettingPolicy.REJECT);

        assertEquals(
                ModuleState.ENABLED,
                controller.stateOf(
                        "render.watermark"));
        assertEquals(
                ModuleState.DISABLED,
                controller.stateOf(
                        "render.fps"));
    }

    @Test
    void legacyProfileWithoutModuleEntriesLeavesCurrentStatesUntouched() {
        final ModuleRegistry modules =
                modules(
                        "render.watermark");
        final ModuleController controller =
                new ModuleController(modules);
        final SettingRegistry settings =
                settings();
        final ModuleKeybindAssignments assignments =
                new ModuleKeybindAssignments(
                        new ModuleKeybindRegistry(
                                modules));
        final LauncherProfileState profile =
                new LauncherProfileState(
                        settings,
                        assignments,
                        modules,
                        controller);

        controller.enable(
                "render.watermark");

        final Map<String, String> legacy =
                new LinkedHashMap<String, String>();
        legacy.put(
                "combat.range",
                "5");

        profile.apply(
                legacy,
                UnknownSettingPolicy.REJECT);

        assertEquals(
                ModuleState.ENABLED,
                controller.stateOf(
                        "render.watermark"));
    }

    @Test
    void unknownModuleStateFollowsUnknownPolicyBeforeMutation() {
        final ModuleRegistry modules =
                modules(
                        "render.watermark");
        final ModuleController controller =
                new ModuleController(modules);
        final SettingRegistry settings =
                settings();
        final ModuleKeybindAssignments assignments =
                new ModuleKeybindAssignments(
                        new ModuleKeybindRegistry(
                                modules));
        final LauncherProfileState profile =
                new LauncherProfileState(
                        settings,
                        assignments,
                        modules,
                        controller);

        @SuppressWarnings("unchecked")
        final Setting<Integer> range =
                (Setting<Integer>) settings.find(
                        "combat.range");

        final Map<String, String> values =
                new LinkedHashMap<String, String>();
        values.put(
                "combat.range",
                "6");
        values.put(
                "@module/missing",
                "enabled");

        assertThrows(
                IllegalArgumentException.class,
                () -> profile.apply(
                        values,
                        UnknownSettingPolicy.REJECT));
        assertEquals(
                Integer.valueOf(3),
                range.get());

        profile.apply(
                values,
                UnknownSettingPolicy.IGNORE);
        assertEquals(
                Integer.valueOf(6),
                range.get());
        assertEquals(
                ModuleState.DISABLED,
                controller.stateOf(
                        "render.watermark"));
    }

    @Test
    void moduleEnableFailureRollsSettingsKeybindsAndStatesBack() {
        final ModuleRegistry modules =
                new ModuleRegistry();
        modules.register(
                new Module() {
                    @Override
                    public String id() {
                        return "render.stable";
                    }
                });
        modules.register(
                new Module() {
                    @Override
                    public String id() {
                        return "render.failing";
                    }

                    @Override
                    public void onEnable() {
                        throw new IllegalStateException(
                                "boom");
                    }
                });
        final ModuleController controller =
                new ModuleController(modules);
        final SettingRegistry settings =
                settings();
        final ModuleKeybindAssignments assignments =
                new ModuleKeybindAssignments(
                        new ModuleKeybindRegistry(
                                modules));

        assignments.bind(
                "render.stable",
                ModuleKeyChord.key(
                        "legacy-key-37"));
        controller.enable(
                "render.stable");

        final LauncherProfileState profile =
                new LauncherProfileState(
                        settings,
                        assignments,
                        modules,
                        controller);

        @SuppressWarnings("unchecked")
        final Setting<Integer> range =
                (Setting<Integer>) settings.find(
                        "combat.range");

        final Map<String, String> values =
                new LinkedHashMap<String, String>();
        values.put(
                "combat.range",
                "6");
        values.put(
                "@module/render.stable",
                "disabled");
        values.put(
                "@module/render.failing",
                "enabled");

        assertThrows(
                RuntimeException.class,
                () -> profile.apply(
                        values,
                        UnknownSettingPolicy.REJECT));

        assertEquals(
                Integer.valueOf(3),
                range.get());
        assertEquals(
                ModuleState.ENABLED,
                controller.stateOf(
                        "render.stable"));
        assertEquals(
                ModuleState.DISABLED,
                controller.stateOf(
                        "render.failing"));
        assertEquals(
                ModuleKeyChord.key(
                        "legacy-key-37"),
                assignments.binding(
                        "render.stable")
                        .chord());
    }

    @Test
    void malformedModuleStateIsRejectedBeforeSettingMutation() {
        final ModuleRegistry modules =
                modules(
                        "render.watermark");
        final ModuleController controller =
                new ModuleController(modules);
        final SettingRegistry settings =
                settings();
        final ModuleKeybindAssignments assignments =
                new ModuleKeybindAssignments(
                        new ModuleKeybindRegistry(
                                modules));
        final LauncherProfileState profile =
                new LauncherProfileState(
                        settings,
                        assignments,
                        modules,
                        controller);

        @SuppressWarnings("unchecked")
        final Setting<Integer> range =
                (Setting<Integer>) settings.find(
                        "combat.range");

        final Map<String, String> values =
                new LinkedHashMap<String, String>();
        values.put(
                "combat.range",
                "6");
        values.put(
                "@module/render.watermark",
                "maybe");

        assertThrows(
                ProfileFormatException.class,
                () -> profile.apply(
                        values,
                        UnknownSettingPolicy.REJECT));

        assertEquals(
                Integer.valueOf(3),
                range.get());
        assertEquals(
                ModuleState.DISABLED,
                controller.stateOf(
                        "render.watermark"));
    }

    @Test
    void malformedKeybindIsRejectedBeforeSettingMutation() {
        final SettingRegistry settings =
                settings();
        final ModuleKeybindAssignments assignments =
                new ModuleKeybindAssignments(
                        new ModuleKeybindRegistry(
                                modules("combat.aura")));
        final LauncherProfileState profile =
                new LauncherProfileState(
                        settings,
                        assignments);

        @SuppressWarnings("unchecked")
        final Setting<Integer> range =
                (Setting<Integer>) settings.find(
                        "combat.range");

        final Map<String, String> values =
                new LinkedHashMap<String, String>();
        values.put(
                "combat.range",
                "6");
        values.put(
                "@keybind/combat.aura",
                "broken");

        assertThrows(
                ProfileFormatException.class,
                () -> profile.apply(
                        values,
                        UnknownSettingPolicy.REJECT));

        assertEquals(
                Integer.valueOf(3),
                range.get());
        assertFalse(
                assignments.owns(
                        "combat.aura"));
    }

    @Test
    void keybindApplyFailureRollsSettingsBack() {
        final ModuleRegistry modules =
                modules(
                        "combat.aura");
        final SettingRegistry settings =
                settings();
        final ModuleKeybindAssignments assignments =
                new ModuleKeybindAssignments(
                        new ModuleKeybindRegistry(
                                modules));
        final LauncherProfileState profile =
                new LauncherProfileState(
                        settings,
                        assignments);

        @SuppressWarnings("unchecked")
        final Setting<Integer> range =
                (Setting<Integer>) settings.find(
                        "combat.range");

        final Map<String, String> values =
                new LinkedHashMap<String, String>();
        values.put(
                "combat.range",
                "6");

        final String validEncoded =
                profileWithBinding(
                        settings,
                        assignments,
                        "combat.aura",
                        ModuleKeyChord.key(
                                "legacy-key-37"));
        values.put(
                "@keybind/missing.module",
                validEncoded);

        assertThrows(
                IllegalArgumentException.class,
                () -> profile.apply(
                        values,
                        UnknownSettingPolicy.REJECT));

        assertEquals(
                Integer.valueOf(3),
                range.get());
        assertFalse(
                assignments.owns(
                        "combat.aura"));
    }

    @Test
    void reservedKeybindNamespaceCannotBeUsedByPersistentSetting() {
        final SettingRegistry settings =
                new SettingRegistry();
        settings.register(
                new Setting<String>(
                        "@keybind/combat.aura",
                        "x",
                        value -> true,
                        SettingCodecs.STRING));

        final ModuleKeybindAssignments assignments =
                new ModuleKeybindAssignments(
                        new ModuleKeybindRegistry(
                                modules("combat.aura")));

        assertThrows(
                IllegalStateException.class,
                () -> new LauncherProfileState(
                        settings,
                        assignments)
                        .snapshot());
    }

    @Test
    void reservedModuleNamespaceCannotBeUsedByPersistentSetting() {
        final SettingRegistry settings =
                new SettingRegistry();
        settings.register(
                new Setting<String>(
                        "@module/render.watermark",
                        "x",
                        value -> true,
                        SettingCodecs.STRING));

        final ModuleRegistry modules =
                modules("render.watermark");
        final ModuleKeybindAssignments assignments =
                new ModuleKeybindAssignments(
                        new ModuleKeybindRegistry(
                                modules));

        assertThrows(
                IllegalStateException.class,
                () -> new LauncherProfileState(
                        settings,
                        assignments,
                        modules,
                        new ModuleController(modules))
                        .snapshot());
    }

    private static String profileWithBinding(
            final SettingRegistry settings,
            final ModuleKeybindAssignments assignments,
            final String moduleId,
            final ModuleKeyChord chord) {
        assignments.bind(
                moduleId,
                chord);
        final String encoded =
                new LauncherProfileState(
                        settings,
                        assignments)
                        .snapshot()
                        .get(
                                "@keybind/" + moduleId);
        assignments.unbind(
                moduleId);
        return encoded;
    }

    private static SettingRegistry settings() {
        final SettingRegistry settings =
                new SettingRegistry();
        settings.register(
                new Setting<Integer>(
                        "combat.range",
                        3,
                        value -> value >= 1
                                && value <= 6,
                        SettingCodecs.INTEGER));
        settings.register(
                new Setting<Boolean>(
                        "render.esp",
                        Boolean.FALSE,
                        value -> true,
                        SettingCodecs.BOOLEAN));
        return settings;
    }

    private static ModuleRegistry modules(
            final String... ids) {
        final ModuleRegistry modules =
                new ModuleRegistry();
        for (String id : ids) {
            modules.register(
                    new Module() {
                        @Override
                        public String id() {
                            return id;
                        }
                    });
        }
        return modules;
    }
}
