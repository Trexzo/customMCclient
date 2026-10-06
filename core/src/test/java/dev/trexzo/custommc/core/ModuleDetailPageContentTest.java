package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleDescriptor;
import dev.trexzo.custommc.core.module.ModuleKeyChord;
import dev.trexzo.custommc.core.module.ModuleKeybind;
import dev.trexzo.custommc.core.module.ModuleKeybindAssignments;
import dev.trexzo.custommc.core.module.ModuleKeybindRegistry;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingBinding;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.SettingValueKind;
import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiFocusManager;
import dev.trexzo.custommc.core.ui.UiKey;
import dev.trexzo.custommc.core.ui.UiKeyAction;
import dev.trexzo.custommc.core.ui.UiKeyEvent;
import dev.trexzo.custommc.core.ui.UiKeys;
import dev.trexzo.custommc.core.ui.UiPointerAction;
import dev.trexzo.custommc.core.ui.UiPointerButton;
import dev.trexzo.custommc.core.ui.UiPointerEvent;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiTextCommand;
import dev.trexzo.custommc.core.ui.UiThemes;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiContentContext;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiContentInputContext;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiPage;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiSnapshot;
import dev.trexzo.custommc.core.ui.clickgui.ModuleDetailPageContent;
import dev.trexzo.custommc.core.ui.clickgui.ModuleSelectionModel;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ModuleDetailPageContentTest {
    @Test
    void noSelectionShowsPromptWithoutLeakingSettings() {
        final Fixture fixture = new Fixture();

        final List<String> text =
                texts(
                        fixture.content.compose(
                                fixture.context()));

        assertTrue(
                text.contains(
                        "Select a module"));
        assertTrue(
                text.contains(
                        "Right-click a module to inspect its settings."));
        assertFalse(
                text.contains(
                        "Aura Enabled"));
    }

    @Test
    void selectedDetailProjectsLiveKeybindAuthority() {
        final Fixture fixture = new Fixture();

        fixture.selection.select(
                "combat.aura");

        final List<String> unbound =
                texts(
                        fixture.content.compose(
                                fixture.context()));
        assertTrue(
                unbound.contains(
                        "Bind: Unbound"));

        final ModuleKeybindRegistry.Registration registration =
                fixture.keybinds.register(
                        new ModuleKeybind(
                                "combat.aura",
                                new ModuleKeyChord(
                                        "legacy-key-37",
                                        false,
                                        true,
                                        false)));

        final List<String> bound =
                texts(
                        fixture.content.compose(
                                fixture.context()));
        assertTrue(
                bound.contains(
                        "Bind: CTRL+legacy-key-37"));
        assertFalse(
                bound.contains(
                        "Bind: Unbound"));

        registration.close();

        final List<String> released =
                texts(
                        fixture.content.compose(
                                fixture.context()));
        assertTrue(
                released.contains(
                        "Bind: Unbound"));
    }

    @Test
    void bindHeaderCapturesCancelsClearsAndPreservesOnConflict() {
        final Fixture fixture = new Fixture();
        fixture.selection.select(
                "combat.aura");

        final UiFocusManager focus =
                new UiFocusManager();
        final ClickGuiContentInputContext input =
                fixture.inputContext(focus);

        assertTrue(
                fixture.content.pointer(
                        input,
                        new UiPointerEvent(
                                130.0F,
                                170.0F,
                                UiPointerButton.LEFT,
                                UiPointerAction.PRESS)));
        assertTrue(
                texts(
                        fixture.content.compose(
                                fixture.context()))
                        .contains(
                                "Bind: Press a key..."));

        assertTrue(
                focus.dispatchKey(
                        new UiKeyEvent(
                                new UiKey(
                                        "legacy-key-37"),
                                UiKeyAction.PRESS,
                                'k',
                                false,
                                true,
                                false)));
        assertTrue(
                texts(
                        fixture.content.compose(
                                fixture.context()))
                        .contains(
                                "Bind: CTRL+legacy-key-37"));

        assertTrue(
                fixture.content.pointer(
                        input,
                        new UiPointerEvent(
                                130.0F,
                                170.0F,
                                UiPointerButton.LEFT,
                                UiPointerAction.PRESS)));
        assertTrue(
                focus.dispatchKey(
                        new UiKeyEvent(
                                UiKeys.ESCAPE,
                                UiKeyAction.PRESS,
                                '\0',
                                false,
                                false,
                                false)));
        assertTrue(
                texts(
                        fixture.content.compose(
                                fixture.context()))
                        .contains(
                                "Bind: CTRL+legacy-key-37"));

        fixture.assignments.bind(
                "render.esp",
                ModuleKeyChord.key(
                        "legacy-key-38"));

        assertTrue(
                fixture.content.pointer(
                        input,
                        new UiPointerEvent(
                                130.0F,
                                170.0F,
                                UiPointerButton.LEFT,
                                UiPointerAction.PRESS)));
        assertTrue(
                focus.dispatchKey(
                        new UiKeyEvent(
                                new UiKey(
                                        "legacy-key-38"),
                                UiKeyAction.PRESS,
                                '\0',
                                false,
                                false,
                                false)));

        final List<String> conflict =
                texts(
                        fixture.content.compose(
                                fixture.context()));
        assertTrue(
                conflict.contains(
                        "Bind: Key in use"));
        assertEquals(
                "legacy-key-37",
                fixture.keybinds
                        .findByModule(
                                "combat.aura")
                        .chord()
                        .keyId());

        assertTrue(
                focus.dispatchKey(
                        new UiKeyEvent(
                                UiKeys.DELETE,
                                UiKeyAction.PRESS,
                                '\0',
                                false,
                                false,
                                false)));
        assertTrue(
                texts(
                        fixture.content.compose(
                                fixture.context()))
                        .contains(
                                "Bind: Unbound"));
    }

    @Test
    void selectedDetailUsesOnlyOwnedPresentedSettingsAndLiveAuthorities() {
        final Fixture fixture = new Fixture();

        fixture.selection.select(
                "combat.aura");

        final List<String> before =
                texts(
                        fixture.content.compose(
                                fixture.context()));

        assertTrue(before.contains("Kill Aura"));
        assertTrue(
                before.contains(
                        "Targets nearby entities"));
        assertTrue(before.contains("DISABLED"));

        assertBefore(
                before,
                "Aura Enabled",
                "Attack Range");
        assertTrue(before.contains("false"));
        assertTrue(before.contains("3"));

        assertFalse(
                before.contains(
                        "Internal Threshold"));
        assertFalse(
                before.contains(
                        "ESP Color"));

        fixture.enabled.set(Boolean.TRUE);
        fixture.controller.enable(
                "combat.aura");

        final List<String> after =
                texts(
                        fixture.content.compose(
                                fixture.context()));

        assertTrue(after.contains("ENABLED"));
        assertTrue(after.contains("true"));
        assertFalse(after.contains("false"));
    }

    private static List<String> texts(
            final List<UiDrawCommand> commands) {
        final List<String> result =
                new ArrayList<String>();
        for (UiDrawCommand command : commands) {
            if (command instanceof UiTextCommand) {
                result.add(
                        ((UiTextCommand) command)
                                .text());
            }
        }
        return result;
    }

    private static void assertBefore(
            final List<String> text,
            final String left,
            final String right) {
        final int leftIndex =
                text.indexOf(left);
        final int rightIndex =
                text.indexOf(right);

        assertTrue(
                leftIndex >= 0,
                "missing text: " + left);
        assertTrue(
                rightIndex >= 0,
                "missing text: " + right);
        assertTrue(
                leftIndex < rightIndex,
                left + " should appear before " + right);
    }

    private static final class Fixture {
        private final ModuleRegistry modules =
                new ModuleRegistry();
        private final SettingRegistry settings =
                new SettingRegistry();
        private final ModuleController controller;
        private final ModuleSelectionModel selection;
        private final ModuleKeybindRegistry keybinds;
        private final ModuleKeybindAssignments assignments;
        private final ModuleDetailPageContent content;
        private final Setting<Boolean> enabled;

        Fixture() {
            modules.register(
                    module("combat.aura"));
            modules.register(
                    module("render.esp"));

            enabled =
                    new Setting<Boolean>(
                            "aura.enabled",
                            Boolean.FALSE,
                            value -> true,
                            SettingCodecs.BOOLEAN);
            final Setting<Integer> range =
                    new Setting<Integer>(
                            "aura.range",
                            3,
                            value -> value >= 1
                                    && value <= 6,
                            SettingCodecs.INTEGER);
            final Setting<Integer> internal =
                    new Setting<Integer>(
                            "aura.internal",
                            10,
                            value -> value >= 0,
                            SettingCodecs.INTEGER);
            final Setting<String> espColor =
                    new Setting<String>(
                            "esp.color",
                            "white",
                            value -> true,
                            SettingCodecs.STRING);

            settings.register(enabled);
            settings.register(range);
            settings.register(internal);
            settings.register(espColor);

            final ModulePresentationRegistry modulePresentations =
                    new ModulePresentationRegistry();
            modulePresentations.register(
                    new ModuleDescriptor(
                            "combat.aura",
                            "Kill Aura",
                            "Targets nearby entities"));

            final SettingPresentationRegistry settingPresentations =
                    new SettingPresentationRegistry();
            settingPresentations.register(
                    new SettingDescriptor(
                            "aura.enabled",
                            "Aura Enabled",
                            SettingValueKind.BOOLEAN,
                            100));
            settingPresentations.register(
                    new SettingDescriptor(
                            "aura.range",
                            "Attack Range",
                            SettingValueKind.INTEGER,
                            0));
            settingPresentations.register(
                    new SettingDescriptor(
                            "esp.color",
                            "ESP Color",
                            SettingValueKind.TEXT,
                            0));

            final ModuleSettingRegistry moduleSettings =
                    new ModuleSettingRegistry(
                            modules,
                            settings);
            moduleSettings.register(
                    new ModuleSettingBinding(
                            "combat.aura",
                            "aura.range",
                            20));
            moduleSettings.register(
                    new ModuleSettingBinding(
                            "combat.aura",
                            "aura.enabled",
                            10));
            moduleSettings.register(
                    new ModuleSettingBinding(
                            "combat.aura",
                            "aura.internal",
                            30));
            moduleSettings.register(
                    new ModuleSettingBinding(
                            "render.esp",
                            "esp.color",
                            0));

            controller =
                    new ModuleController(modules);
            selection =
                    new ModuleSelectionModel(modules);
            keybinds =
                    new ModuleKeybindRegistry(modules);
            assignments =
                    new ModuleKeybindAssignments(
                            keybinds);
            content =
                    new ModuleDetailPageContent(
                            selection,
                            controller,
                            modulePresentations,
                            moduleSettings,
                            keybinds,
                            assignments,
                            settings,
                            settingPresentations);
        }

        ClickGuiContentInputContext inputContext(
                final UiFocusManager focus) {
            final ClickGuiPage page =
                    new ClickGuiPage(
                            "module-detail",
                            "Module Details",
                            0);
            return new ClickGuiContentInputContext(
                    new ClickGuiSnapshot(
                            true,
                            "module-detail",
                            "",
                            Arrays.asList(page)),
                    page,
                    new UiBounds(
                            100.0F,
                            100.0F,
                            700.0F,
                            500.0F),
                    focus);
        }

        ClickGuiContentContext context() {
            final ClickGuiPage page =
                    new ClickGuiPage(
                            "module-detail",
                            "Module Details",
                            0);
            return new ClickGuiContentContext(
                    new ClickGuiSnapshot(
                            true,
                            "module-detail",
                            "",
                            Arrays.asList(page)),
                    page,
                    new UiBounds(
                            100.0F,
                            100.0F,
                            700.0F,
                            500.0F),
                    UiThemes.darkDefault());
        }
    }

    private static Module module(
            final String id) {
        return new Module() {
            @Override
            public String id() {
                return id;
            }
        };
    }
}
