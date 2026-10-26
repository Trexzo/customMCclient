package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.ui.UiFocusManager;
import dev.trexzo.custommc.core.ui.UiKey;
import dev.trexzo.custommc.core.ui.UiKeyAction;
import dev.trexzo.custommc.core.ui.UiKeyEvent;
import dev.trexzo.custommc.core.ui.UiKeys;
import dev.trexzo.custommc.core.ui.UiPointerAction;
import dev.trexzo.custommc.core.ui.UiPointerButton;
import dev.trexzo.custommc.core.ui.UiPointerEvent;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiContentRegistry;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiInputController;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiLayout;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiLayoutEngine;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiModel;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiPage;
import dev.trexzo.custommc.core.ui.clickgui.HudPositionPageContent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ClickGuiHudKeyboardNudgeTest {
    @Test
    void shiftArrowsNudgeLiveSettingsAndControlShiftUsesSinglePixel() {
        final Fixture fixture = new Fixture();
        try (ClickGuiInputController input = fixture.controller()) {
            assertTrue(input.key(key(UiKeys.RIGHT, true, false, false)));
            assertEquals(Integer.valueOf(22), fixture.x.get());
            assertEquals(Integer.valueOf(210), fixture.y.get());
            assertTrue(input.key(key(UiKeys.UP, true, true, false)));
            assertEquals(Integer.valueOf(209), fixture.y.get());
            assertTrue(input.key(key(UiKeys.LEFT, true, false, false)));
            assertEquals(Integer.valueOf(18), fixture.x.get());
            assertEquals("18", fixture.settings.snapshotEncoded().get(
                    HudPositionPageContent.X_ID));
            assertFalse(input.key(new UiKeyEvent(
                    UiKeys.RIGHT, UiKeyAction.REPEAT,
                    '\0', true, false, false)));
            assertFalse(input.key(key(UiKeys.RIGHT, true, false, true)));
            assertEquals(Integer.valueOf(18), fixture.x.get());
            assertTrue(fixture.model.snapshot().open());
        }
    }

    @Test
    void textSearchAndOtherPagesCannotStealHudPositionShortcuts() {
        final Fixture fixture = new Fixture();
        try (ClickGuiInputController input = fixture.controller()) {
            final UiViewport viewport = new UiViewport(1200, 800, 1.0F);
            final ClickGuiLayout layout = new ClickGuiLayoutEngine()
                    .layout(viewport);
            assertTrue(input.pointer(new UiPointerEvent(
                    layout.search().x() + 4.0F,
                    layout.search().y() + 4.0F,
                    UiPointerButton.LEFT, UiPointerAction.PRESS),
                    viewport));
            assertFalse(input.key(key(UiKeys.RIGHT, true, false, false)));
            assertEquals(Integer.valueOf(18), fixture.x.get());
            assertTrue(input.key(key(UiKeys.ESCAPE, false, false, false)));
            assertTrue(input.key(key(UiKeys.RIGHT, true, false, false)));
            assertEquals(Integer.valueOf(22), fixture.x.get());

            assertTrue(fixture.model.select("modules"));
            assertFalse(input.key(key(UiKeys.RIGHT, true, false, false)));
            assertEquals(Integer.valueOf(22), fixture.x.get());
            fixture.model.close();
            assertFalse(input.key(key(UiKeys.RIGHT, true, false, false)));
            assertEquals(Integer.valueOf(22), fixture.x.get());
        }
    }

    @Test
    void nudgeClampsToPersistentRangeAndNeverChangesOtherSettings() {
        final Fixture fixture = new Fixture();
        fixture.x.set(4096);
        fixture.y.set(0);
        try (ClickGuiInputController input = fixture.controller()) {
            assertTrue(input.key(key(UiKeys.RIGHT, true, false, false)));
            assertTrue(input.key(key(UiKeys.UP, true, true, false)));
            assertEquals(Integer.valueOf(4096), fixture.x.get());
            assertEquals(Integer.valueOf(0), fixture.y.get());
            assertEquals("keep", fixture.settings.snapshotEncoded().get(
                    "ui.sentinel"));
        }
    }

    private static UiKeyEvent key(
            final UiKey value,
            final boolean shift,
            final boolean control,
            final boolean alt) {
        return new UiKeyEvent(value, UiKeyAction.PRESS,
                '\0', shift, control, alt);
    }

    private static final class Fixture {
        final SettingRegistry settings = new SettingRegistry();
        final Setting<Integer> x = new Setting<Integer>(
                HudPositionPageContent.X_ID, 18,
                value -> value != null && value >= 0 && value <= 4096,
                SettingCodecs.INTEGER);
        final Setting<Integer> y = new Setting<Integer>(
                HudPositionPageContent.Y_ID, 210,
                value -> value != null && value >= 0 && value <= 4096,
                SettingCodecs.INTEGER);
        final ClickGuiModel model = new ClickGuiModel();
        final ClickGuiContentRegistry contents = new ClickGuiContentRegistry();

        Fixture() {
            settings.register(x);
            settings.register(y);
            settings.register(new Setting<String>(
                    "ui.sentinel", "keep", value -> value != null,
                    SettingCodecs.STRING));
            model.register(new ClickGuiPage("modules", "Modules", 0));
            model.register(new ClickGuiPage("hud-layout", "HUD Layout", 15));
            contents.register("hud-layout",
                    new HudPositionPageContent(settings));
            model.open();
            model.select("hud-layout");
        }

        ClickGuiInputController controller() {
            return new ClickGuiInputController(
                    model, contents, new UiFocusManager());
        }
    }
}
