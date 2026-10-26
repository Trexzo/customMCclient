package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiPointerAction;
import dev.trexzo.custommc.core.ui.UiPointerButton;
import dev.trexzo.custommc.core.ui.UiPointerEvent;
import dev.trexzo.custommc.core.ui.UiTextCommand;
import dev.trexzo.custommc.core.ui.UiThemes;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiContentContext;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiContentInputContext;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiPage;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiSnapshot;
import dev.trexzo.custommc.core.ui.clickgui.HudPositionPageContent;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class HudPositionPageContentTest {
    private static final UiViewport VIEWPORT =
            new UiViewport(1200, 800, 1.0F);
    private static final UiBounds CONTENT =
            new UiBounds(200.0F, 80.0F, 650.0F, 480.0F);
    private static final ClickGuiPage PAGE =
            new ClickGuiPage("hud-layout", "HUD Layout", 15);

    @Test
    void clickPreviewSetsActualPersistedTargetHudCoordinatesAndResetRestoresDefaults() {
        final SettingRegistry settings = targetSettings();
        final HudPositionPageContent page = new HudPositionPageContent(settings);
        final UiBounds preview = HudPositionPageContent.previewBounds(
                CONTENT, VIEWPORT);
        assertTrue(preview != null);

        assertTrue(page.pointer(input(VIEWPORT),
                press(preview.x() + preview.width() * 0.70F,
                        preview.y() + preview.height() * 0.50F)));
        assertEquals("758", settings.snapshotEncoded().get(
                HudPositionPageContent.X_ID));
        assertEquals("372", settings.snapshotEncoded().get(
                HudPositionPageContent.Y_ID));
        assertEquals("sentinel", settings.snapshotEncoded().get("ui.sentinel"));

        final UiBounds reset = HudPositionPageContent.resetBounds(preview);
        assertTrue(page.pointer(input(VIEWPORT),
                press(reset.x() + 6.0F, reset.y() + 6.0F)));
        assertEquals("18", settings.snapshotEncoded().get(
                HudPositionPageContent.X_ID));
        assertEquals("210", settings.snapshotEncoded().get(
                HudPositionPageContent.Y_ID));
    }

    @Test
    void compactCoordinatesCardUsesCorrectNativeBoundingSizeAtUiScaleTwo() {
        final SettingRegistry settings = targetSettings();
        settings.register(new Setting<Boolean>(
                "render.targetHud.compact", true, v -> v != null,
                SettingCodecs.BOOLEAN));
        settings.register(new Setting<Boolean>(
                "render.targetHud.showCoordinates", true, v -> v != null,
                SettingCodecs.BOOLEAN));
        final HudPositionPageContent page = new HudPositionPageContent(settings);
        final UiViewport viewport = new UiViewport(1200, 800, 2.0F);
        final UiBounds preview = HudPositionPageContent.previewBounds(
                CONTENT, viewport);
        assertTrue(preview != null);
        assertTrue(page.pointer(input(viewport),
                press(preview.x() + preview.width() * 0.5F,
                        preview.y() + preview.height() * 0.5F)));
        // Logical framebuffer is 600 x 400, compact+XYZ card is 208 x 54.
        assertEquals("196", settings.snapshotEncoded().get(
                HudPositionPageContent.X_ID));
        assertEquals("173", settings.snapshotEncoded().get(
                HudPositionPageContent.Y_ID));

        final List<UiDrawCommand> commands =
                page.compose(render(viewport));
        boolean sawCoordinates = false;
        for (UiDrawCommand command : commands) {
            if (command instanceof UiTextCommand) {
                sawCoordinates |= ((UiTextCommand) command).text()
                        .contains("X: 196");
            }
        }
        assertTrue(sawCoordinates);
    }

    @Test
    void clicksOutsideCanvasRightButtonAndReleaseCannotReposition() {
        final SettingRegistry settings = targetSettings();
        final HudPositionPageContent page = new HudPositionPageContent(settings);
        final UiBounds preview = HudPositionPageContent.previewBounds(
                CONTENT, VIEWPORT);
        assertFalse(page.pointer(input(VIEWPORT),
                new UiPointerEvent(preview.x() + 8.0F,
                        preview.y() + 8.0F, UiPointerButton.RIGHT,
                        UiPointerAction.PRESS)));
        assertFalse(page.pointer(input(VIEWPORT),
                new UiPointerEvent(preview.x() + 8.0F,
                        preview.y() + 8.0F, UiPointerButton.LEFT,
                        UiPointerAction.RELEASE)));
        assertFalse(page.pointer(input(VIEWPORT),
                press(CONTENT.x() + 1.0F, CONTENT.y() + 1.0F)));
        assertEquals("18", settings.snapshotEncoded().get(
                HudPositionPageContent.X_ID));
        assertEquals("210", settings.snapshotEncoded().get(
                HudPositionPageContent.Y_ID));
    }

    @Test
    void missingTargetHudSettingsShowUnavailableAndDoNotCreateOrWriteThem() {
        final SettingRegistry settings = new SettingRegistry();
        settings.register(new Setting<Integer>(
                HudPositionPageContent.X_ID, 18,
                v -> v != null && v >= 0 && v <= 4096,
                SettingCodecs.INTEGER));
        final HudPositionPageContent page = new HudPositionPageContent(settings);
        boolean sawUnavailable = false;
        for (UiDrawCommand command : page.compose(render(VIEWPORT))) {
            if (command instanceof UiTextCommand) {
                sawUnavailable |= ((UiTextCommand) command).text()
                        .contains("unavailable");
            }
        }
        assertTrue(sawUnavailable);
        final UiBounds preview = HudPositionPageContent.previewBounds(
                CONTENT, VIEWPORT);
        assertFalse(page.pointer(input(VIEWPORT),
                press(preview.x() + 12.0F, preview.y() + 12.0F)));
        assertEquals("18", settings.snapshotEncoded().get(
                HudPositionPageContent.X_ID));
        assertEquals(null, settings.find(HudPositionPageContent.Y_ID));
    }

    @Test
    void narrowContentDoesNotWriteHiddenOrUnusableControls() {
        final SettingRegistry settings = targetSettings();
        final HudPositionPageContent page = new HudPositionPageContent(settings);
        final UiBounds narrow = new UiBounds(20, 20, 150, 160);
        assertEquals(null, HudPositionPageContent.previewBounds(
                narrow, VIEWPORT));
        final ClickGuiContentInputContext context =
                new ClickGuiContentInputContext(
                        snapshot(), PAGE, narrow, null, null, VIEWPORT);
        assertFalse(page.pointer(context, press(40.0F, 100.0F)));
        assertEquals("18", settings.snapshotEncoded().get(
                HudPositionPageContent.X_ID));
    }

    private static SettingRegistry targetSettings() {
        final SettingRegistry settings = new SettingRegistry();
        settings.register(new Setting<Integer>(
                HudPositionPageContent.X_ID, 18,
                v -> v != null && v >= 0 && v <= 4096,
                SettingCodecs.INTEGER));
        settings.register(new Setting<Integer>(
                HudPositionPageContent.Y_ID, 210,
                v -> v != null && v >= 0 && v <= 4096,
                SettingCodecs.INTEGER));
        settings.register(new Setting<String>(
                "ui.sentinel", "sentinel", v -> v != null,
                SettingCodecs.STRING));
        return settings;
    }

    private static ClickGuiSnapshot snapshot() {
        return new ClickGuiSnapshot(true, "hud-layout", "",
                Collections.singletonList(PAGE));
    }

    private static ClickGuiContentInputContext input(
            final UiViewport viewport) {
        return new ClickGuiContentInputContext(
                snapshot(), PAGE, CONTENT, null, null, viewport);
    }

    private static ClickGuiContentContext render(
            final UiViewport viewport) {
        return new ClickGuiContentContext(
                snapshot(), PAGE, CONTENT, UiThemes.darkDefault(), viewport);
    }

    private static UiPointerEvent press(final float x, final float y) {
        return new UiPointerEvent(x, y,
                UiPointerButton.LEFT, UiPointerAction.PRESS);
    }
}
