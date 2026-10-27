package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.SettingValueKind;
import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiPointerAction;
import dev.trexzo.custommc.core.ui.UiPointerButton;
import dev.trexzo.custommc.core.ui.UiPointerEvent;
import dev.trexzo.custommc.core.ui.UiThemeSettings;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiContentInputContext;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiPage;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiSnapshot;
import dev.trexzo.custommc.core.ui.clickgui.SettingListPageContent;
import org.junit.jupiter.api.Test;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SettingThemePresetClickTest {
    @Test
    void clickOnRealSettingsRowCyclesOnlyPresetAndPersistsSelection() {
        final SettingRegistry registry = new SettingRegistry();
        final Setting<String> theme = UiThemeSettings.setting();
        registry.register(theme);
        final SettingPresentationRegistry descriptions =
                new SettingPresentationRegistry();
        descriptions.register(new SettingDescriptor(
                UiThemeSettings.ID, "Visual Theme", SettingValueKind.TEXT, -100));
        final SettingListPageContent page =
                new SettingListPageContent(registry, descriptions);
        final ClickGuiPage selected =
                new ClickGuiPage("settings", "Settings", 0);
        final ClickGuiContentInputContext context =
                new ClickGuiContentInputContext(
                        new ClickGuiSnapshot(true, "settings", "",
                                Collections.singletonList(selected)),
                        selected, new UiBounds(200, 100, 600, 400));
        final UiPointerEvent left = new UiPointerEvent(240, 170,
                UiPointerButton.LEFT, UiPointerAction.PRESS);
        final UiPointerEvent right = new UiPointerEvent(240, 170,
                UiPointerButton.RIGHT, UiPointerAction.PRESS);
        assertTrue(page.pointer(context, left));
        assertEquals("graphite", theme.get());
        assertEquals("graphite", registry.snapshotEncoded().get(UiThemeSettings.ID));
        assertTrue(page.pointer(context, right));
        assertEquals("default", theme.get());
        assertFalse(page.pointer(context, new UiPointerEvent(240, 170,
                UiPointerButton.LEFT, UiPointerAction.RELEASE)));
        assertEquals("default", theme.get());
    }
}
