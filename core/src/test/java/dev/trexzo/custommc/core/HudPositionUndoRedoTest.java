package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiFocusManager;
import dev.trexzo.custommc.core.ui.UiKey;
import dev.trexzo.custommc.core.ui.UiKeyAction;
import dev.trexzo.custommc.core.ui.UiKeyEvent;
import dev.trexzo.custommc.core.ui.UiKeys;
import dev.trexzo.custommc.core.ui.UiPointerAction;
import dev.trexzo.custommc.core.ui.UiPointerButton;
import dev.trexzo.custommc.core.ui.UiPointerEvent;
import dev.trexzo.custommc.core.ui.UiTextCommand;
import dev.trexzo.custommc.core.ui.UiThemes;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiContentContext;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiContentInputContext;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiContentRegistry;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiInputController;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiLayout;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiLayoutEngine;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiModel;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiPage;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiSnapshot;
import dev.trexzo.custommc.core.ui.clickgui.HudPositionPageContent;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class HudPositionUndoRedoTest {
    private static final UiViewport VIEWPORT = new UiViewport(1200, 800, 1F);
    private static final UiBounds CONTENT = new UiBounds(200F, 80F, 650F, 480F);
    private static final ClickGuiPage PAGE = new ClickGuiPage(
            "hud-layout", "HUD Layout", 15);

    @Test
    void placementAndResetAreUndoableViaShortcutsAndClickableControls() {
        final Fixture f = new Fixture();
        final UiBounds canvas = canvas();
        assertTrue(f.page.pointer(f.context(), press(
                canvas.x() + canvas.width() * .7F,
                canvas.y() + canvas.height() * .5F)));
        assertXY(f, 758, 372);
        assertTrue(f.page.pointer(f.context(), pressAt(
                HudPositionPageContent.resetBounds(canvas))));
        assertXY(f, 18, 210);
        assertTrue(f.page.key(ctrl("legacy-key-44")));
        assertXY(f, 758, 372);
        assertTrue(f.page.pointer(f.context(), pressAt(
                HudPositionPageContent.undoBounds(canvas))));
        assertXY(f, 18, 210);
        assertTrue(f.page.pointer(f.context(), pressAt(
                HudPositionPageContent.redoBounds(canvas))));
        assertXY(f, 758, 372);
        assertTrue(f.page.key(ctrlShift("legacy-key-44")));
        assertXY(f, 18, 210);
        assertTrue(f.page.key(ctrl("legacy-key-44")));
        assertXY(f, 758, 372);
        assertEquals("keep", f.settings.snapshotEncoded().get("ui.sentinel"));
        boolean drewUndo = false;
        boolean drewRedo = false;
        for (UiDrawCommand c : f.page.compose(f.renderContext())) {
            if (c instanceof UiTextCommand) {
                drewUndo |= "Undo".equals(((UiTextCommand) c).text());
                drewRedo |= "Redo".equals(((UiTextCommand) c).text());
            }
        }
        assertTrue(drewUndo && drewRedo);
    }

    @Test
    void heldMotionCoalescesIntoOneUndoAndReleaseStartsNewGesture() {
        final Fixture f = new Fixture();
        final UiBounds c = canvas();
        final float middleX = c.x() + c.width() * .5F;
        final float middleY = c.y() + c.height() * .5F;
        assertTrue(f.page.pointer(f.context(), press(middleX, middleY)));
        assertXY(f, 518, 372);
        assertTrue(f.page.pointer(f.context(), move(
                c.x() + c.width() * .75F,
                c.y() + c.height() * .25F)));
        assertXY(f, 818, 172);
        assertTrue(f.page.pointer(f.context(), move(
                c.x() + c.width() * .7F,
                c.y() + c.height() * .4F)));
        assertXY(f, 758, 292);
        assertFalse(f.page.pointer(f.context(), release(middleX, middleY)));
        assertTrue(f.page.key(ctrl("legacy-key-44")));
        assertXY(f, 18, 210);
        assertTrue(f.page.key(ctrl("legacy-key-21")));
        assertXY(f, 758, 292);

        // At the same spot a mouse down makes no change, yet the subsequent
        // move still needs its own real undo checkpoint.
        assertTrue(f.page.pointer(f.context(), press(
                c.x() + (758F + 82F) * c.width() / VIEWPORT.logicalWidth(),
                c.y() + (292F + 28F) * c.height() / VIEWPORT.logicalHeight())));
        assertTrue(f.page.pointer(f.context(), move(middleX, middleY)));
        assertXY(f, 518, 372);
        assertTrue(f.page.key(ctrl("legacy-key-44")));
        assertXY(f, 758, 292);
    }

    @Test
    void directExternalProfileChangeInvalidatesOldHistoryWithoutOverwrite() {
        final Fixture f = new Fixture();
        assertTrue(f.page.key(shift(UiKeys.RIGHT)));
        assertXY(f, 22, 210);
        f.x.set(335);
        f.y.set(404);
        assertFalse(f.page.key(ctrl("legacy-key-44")));
        assertXY(f, 335, 404);
        assertTrue(f.page.key(shift(UiKeys.RIGHT)));
        assertXY(f, 339, 404);
        assertTrue(f.page.key(ctrl("legacy-key-44")));
        assertXY(f, 335, 404);
        assertFalse(f.page.key(ctrl("legacy-key-44")));
    }

    @Test
    void historyKeepsOnlyTheMostRecent64Edits() {
        final Fixture f = new Fixture();
        for (int i = 0; i < 80; i++) {
            assertTrue(f.page.key(shift(UiKeys.RIGHT)));
        }
        assertXY(f, 338, 210);
        for (int i = 0; i < 64; i++) {
            assertTrue(f.page.key(ctrl("legacy-key-44")));
        }
        assertXY(f, 82, 210);
        assertFalse(f.page.key(ctrl("legacy-key-44")));
        assertXY(f, 82, 210);
    }

    @Test
    void focusedEditorCannotUndoAndReleaseOutsideCanvasTerminatesGesture() {
        final Fixture f = new Fixture();
        final ClickGuiModel model = new ClickGuiModel();
        model.register(PAGE);
        model.open();
        final ClickGuiContentRegistry pages = new ClickGuiContentRegistry();
        pages.register(PAGE.id(), f.page);
        final UiFocusManager focus = new UiFocusManager();
        try (ClickGuiInputController input =
                     new ClickGuiInputController(model, pages, focus)) {
            final UiBounds c = HudPositionPageContent.previewBounds(
                    new ClickGuiLayoutEngine().layout(VIEWPORT).content(),
                    VIEWPORT);
            assertTrue(input.pointer(press(c.x() + c.width() / 2,
                    c.y() + c.height() / 2), VIEWPORT));
            final int priorX = f.x.get();
            assertFalse(input.pointer(release(2F, 2F), VIEWPORT));
            assertTrue(input.key(ctrl("legacy-key-44")));
            assertEquals(18, f.x.get().intValue());
            // Ctrl+F focuses search and must own Ctrl+Z until escaped.
            assertTrue(input.key(ctrl("legacy-key-33")));
            assertFalse(input.key(ctrl("legacy-key-44")));
            assertEquals(18, f.x.get().intValue());
            assertTrue(input.key(new UiKeyEvent(
                    UiKeys.ESCAPE, UiKeyAction.PRESS, '\0',
                    false, false, false)));
            assertTrue(model.snapshot().open());
            assertTrue(priorX > 18);
        }
    }

    private static final class Fixture {
        final SettingRegistry settings = new SettingRegistry();
        final Setting<Integer> x = new Setting<Integer>(
                HudPositionPageContent.X_ID, 18,
                v -> v != null && v >= 0 && v <= 4096, SettingCodecs.INTEGER);
        final Setting<Integer> y = new Setting<Integer>(
                HudPositionPageContent.Y_ID, 210,
                v -> v != null && v >= 0 && v <= 4096, SettingCodecs.INTEGER);
        final HudPositionPageContent page;

        Fixture() {
            settings.register(x);
            settings.register(y);
            settings.register(new Setting<String>(
                    "ui.sentinel", "keep", v -> v != null, SettingCodecs.STRING));
            page = new HudPositionPageContent(settings);
        }

        ClickGuiContentInputContext context() {
            return new ClickGuiContentInputContext(snapshot(),
                    PAGE, CONTENT, null, null, VIEWPORT);
        }

        ClickGuiContentContext renderContext() {
            return new ClickGuiContentContext(snapshot(),
                    PAGE, CONTENT, UiThemes.darkDefault(), VIEWPORT);
        }

        void unused() { }
    }

    private static ClickGuiSnapshot snapshot() {
        return new ClickGuiSnapshot(true, PAGE.id(), "",
                Collections.singletonList(PAGE));
    }

    private static UiBounds canvas() {
        return HudPositionPageContent.previewBounds(CONTENT, VIEWPORT);
    }

    private static UiPointerEvent pressAt(final UiBounds bounds) {
        return press(bounds.x() + 4F, bounds.y() + 4F);
    }

    private static UiPointerEvent press(float x, float y) {
        return new UiPointerEvent(x, y,
                UiPointerButton.LEFT, UiPointerAction.PRESS);
    }

    private static UiPointerEvent move(float x, float y) {
        return new UiPointerEvent(x, y,
                UiPointerButton.LEFT, UiPointerAction.MOVE);
    }

    private static UiPointerEvent release(float x, float y) {
        return new UiPointerEvent(x, y,
                UiPointerButton.LEFT, UiPointerAction.RELEASE);
    }

    private static UiKeyEvent ctrl(final String id) {
        return new UiKeyEvent(new UiKey(id), UiKeyAction.PRESS,
                '\0', false, true, false);
    }

    private static UiKeyEvent ctrlShift(final String id) {
        return new UiKeyEvent(new UiKey(id), UiKeyAction.PRESS,
                '\0', true, true, false);
    }

    private static UiKeyEvent shift(final UiKey id) {
        return new UiKeyEvent(id, UiKeyAction.PRESS,
                '\0', true, false, false);
    }

    private static void assertXY(Fixture f, int x, int y) {
        assertEquals(x, f.x.get().intValue());
        assertEquals(y, f.y.get().intValue());
    }
}
