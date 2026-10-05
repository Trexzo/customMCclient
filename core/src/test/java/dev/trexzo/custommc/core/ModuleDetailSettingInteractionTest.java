package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingBinding;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
import dev.trexzo.custommc.core.setting.SettingNumericSpec;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.SettingValueKind;
import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiFocusManager;
import dev.trexzo.custommc.core.ui.UiKeyAction;
import dev.trexzo.custommc.core.ui.UiKeyEvent;
import dev.trexzo.custommc.core.ui.UiKeys;
import dev.trexzo.custommc.core.ui.UiPointerAction;
import dev.trexzo.custommc.core.ui.UiPointerButton;
import dev.trexzo.custommc.core.ui.UiPointerEvent;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiContentInputContext;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiPage;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiSnapshot;
import dev.trexzo.custommc.core.ui.clickgui.ModuleDetailPageContent;
import dev.trexzo.custommc.core.ui.clickgui.ModuleSelectionModel;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ModuleDetailSettingInteractionTest {
    @Test
    void detailRowsUseSharedValidatorOwnedEditors() {
        final ModuleRegistry modules =
                new ModuleRegistry();
        modules.register(module("combat.aura"));

        final SettingRegistry settings =
                new SettingRegistry();
        final Setting<Boolean> enabled =
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
        final Setting<String> mode =
                new Setting<String>(
                        "aura.mode",
                        "single",
                        value -> value.length() <= 16,
                        SettingCodecs.STRING);
        settings.register(enabled);
        settings.register(range);
        settings.register(mode);

        final SettingPresentationRegistry presentations =
                new SettingPresentationRegistry();
        presentations.register(
                new SettingDescriptor(
                        "aura.enabled",
                        "Aura Enabled",
                        SettingValueKind.BOOLEAN,
                        0));
        presentations.register(
                new SettingDescriptor(
                        "aura.range",
                        "Attack Range",
                        SettingValueKind.INTEGER,
                        0,
                        new SettingNumericSpec(
                                1.0D,
                                6.0D,
                                1.0D)));
        presentations.register(
                new SettingDescriptor(
                        "aura.mode",
                        "Mode",
                        SettingValueKind.TEXT,
                        0));

        final ModuleSettingRegistry ownership =
                new ModuleSettingRegistry(
                        modules,
                        settings);
        ownership.register(
                new ModuleSettingBinding(
                        "combat.aura",
                        "aura.enabled",
                        0));
        ownership.register(
                new ModuleSettingBinding(
                        "combat.aura",
                        "aura.range",
                        10));
        ownership.register(
                new ModuleSettingBinding(
                        "combat.aura",
                        "aura.mode",
                        20));

        final ModuleSelectionModel selection =
                new ModuleSelectionModel(modules);
        selection.select("combat.aura");

        final ModuleDetailPageContent content =
                new ModuleDetailPageContent(
                        selection,
                        new ModuleController(modules),
                        new ModulePresentationRegistry(),
                        ownership,
                        settings,
                        presentations);

        final ClickGuiPage page =
                new ClickGuiPage(
                        "module-detail",
                        "Module Details",
                        0);
        final ClickGuiSnapshot snapshot =
                new ClickGuiSnapshot(
                        true,
                        "module-detail",
                        "",
                        Arrays.asList(page));
        final UiBounds bounds =
                new UiBounds(
                        100.0F,
                        100.0F,
                        700.0F,
                        500.0F);
        final UiFocusManager focus =
                new UiFocusManager();
        final ClickGuiContentInputContext context =
                new ClickGuiContentInputContext(
                        snapshot,
                        page,
                        bounds,
                        focus);

        assertTrue(
                content.pointer(
                        context,
                        press(
                                130.0F,
                                210.0F,
                                UiPointerButton.LEFT)));
        assertEquals(
                Boolean.TRUE,
                enabled.get());

        assertTrue(
                content.pointer(
                        context,
                        press(
                                130.0F,
                                255.0F,
                                UiPointerButton.LEFT)));
        assertEquals(
                Integer.valueOf(4),
                range.get());

        assertTrue(
                content.pointer(
                        context,
                        press(
                                130.0F,
                                255.0F,
                                UiPointerButton.RIGHT)));
        assertEquals(
                Integer.valueOf(3),
                range.get());

        assertTrue(
                content.pointer(
                        context,
                        press(
                                130.0F,
                                300.0F,
                                UiPointerButton.LEFT)));
        assertEquals(
                "clickgui.setting-text.module-detail.aura.mode",
                focus.focusedId());

        assertTrue(
                focus.dispatchKey(
                        new UiKeyEvent(
                                UiKeys.SPACE,
                                UiKeyAction.PRESS,
                                'x',
                                false,
                                false,
                                false)));
        assertEquals(
                "single",
                mode.get());

        assertTrue(
                focus.dispatchKey(
                        new UiKeyEvent(
                                UiKeys.ENTER,
                                UiKeyAction.PRESS,
                                '\0',
                                false,
                                false,
                                false)));
        assertEquals(
                "singlex",
                mode.get());
        assertNull(
                focus.focusedId());
    }

    private static UiPointerEvent press(
            final float x,
            final float y,
            final UiPointerButton button) {
        return new UiPointerEvent(
                x,
                y,
                button,
                UiPointerAction.PRESS);
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
