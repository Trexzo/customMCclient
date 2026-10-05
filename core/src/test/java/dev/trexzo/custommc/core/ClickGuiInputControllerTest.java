package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.ui.UiFocusManager;
import dev.trexzo.custommc.core.ui.UiKey;
import dev.trexzo.custommc.core.ui.UiKeyAction;
import dev.trexzo.custommc.core.ui.UiKeyEvent;
import dev.trexzo.custommc.core.ui.UiKeys;
import dev.trexzo.custommc.core.ui.UiPointerAction;
import dev.trexzo.custommc.core.ui.UiPointerButton;
import dev.trexzo.custommc.core.ui.UiPointerEvent;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiInputController;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiLayout;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiLayoutEngine;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiMetrics;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiModel;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiPage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ClickGuiInputControllerTest {
    @Test
    void searchFocusEditsQueryAndEscapeUnfocusesBeforeClosing() {
        final ClickGuiModel model =
                model();
        final UiFocusManager focus =
                new UiFocusManager();
        final UiViewport viewport =
                new UiViewport(
                        1200,
                        800,
                        1.0F);
        final ClickGuiLayout layout =
                new ClickGuiLayoutEngine()
                        .layout(viewport);

        try (ClickGuiInputController input =
                     new ClickGuiInputController(
                             model,
                             focus)) {
            assertTrue(input.pointer(
                    press(
                            layout.search().x() + 2.0F,
                            layout.search().y() + 2.0F),
                    viewport));
            assertEquals(
                    ClickGuiInputController.SEARCH_FOCUS_ID,
                    focus.focusedId());

            assertTrue(input.key(character('e')));
            assertTrue(input.key(character('s')));
            assertTrue(input.key(character('p')));
            assertEquals(
                    "esp",
                    model.snapshot().searchQuery());

            assertTrue(input.key(key(UiKeys.BACKSPACE)));
            assertEquals(
                    "es",
                    model.snapshot().searchQuery());

            assertTrue(input.key(key(UiKeys.ESCAPE)));
            assertNull(focus.focusedId());
            assertTrue(model.snapshot().open());

            assertTrue(input.key(key(UiKeys.ESCAPE)));
            assertFalse(model.snapshot().open());
        }
    }

    @Test
    void navigationClickSelectsPageUsingSharedRowGeometry() {
        final ClickGuiModel model =
                model();
        final UiFocusManager focus =
                new UiFocusManager();
        final UiViewport viewport =
                new UiViewport(
                        1200,
                        800,
                        1.0F);
        final ClickGuiLayout layout =
                new ClickGuiLayoutEngine()
                        .layout(viewport);

        try (ClickGuiInputController input =
                     new ClickGuiInputController(
                             model,
                             focus)) {
            final float secondRowY =
                    layout.navigation().y()
                            + ClickGuiMetrics.PAGE_HEIGHT
                            + ClickGuiMetrics.PAGE_GAP
                            + 2.0F;

            assertTrue(input.pointer(
                    press(
                            layout.navigation().x() + 3.0F,
                            secondRowY),
                    viewport));

            assertEquals(
                    "render",
                    model.snapshot().selectedPageId());
            assertNull(focus.focusedId());
        }
    }

    @Test
    void closedGuiIgnoresInputAndCloseReleasesSearchFocus() {
        final ClickGuiModel model =
                model();
        final UiFocusManager focus =
                new UiFocusManager();
        final UiViewport viewport =
                new UiViewport(
                        1200,
                        800,
                        1.0F);
        final ClickGuiLayout layout =
                new ClickGuiLayoutEngine()
                        .layout(viewport);

        final ClickGuiInputController input =
                new ClickGuiInputController(
                        model,
                        focus);

        assertTrue(input.pointer(
                press(
                        layout.search().x() + 2.0F,
                        layout.search().y() + 2.0F),
                viewport));
        assertEquals(
                ClickGuiInputController.SEARCH_FOCUS_ID,
                focus.focusedId());

        input.close();
        assertNull(focus.focusedId());

        model.close();
        assertFalse(input.pointer(
                press(
                        layout.search().x() + 2.0F,
                        layout.search().y() + 2.0F),
                viewport));
        assertFalse(input.key(character('x')));
    }

    private static ClickGuiModel model() {
        final ClickGuiModel model =
                new ClickGuiModel();
        model.register(
                new ClickGuiPage(
                        "combat",
                        "Combat",
                        0));
        model.register(
                new ClickGuiPage(
                        "render",
                        "Render",
                        1));
        model.open();
        return model;
    }

    private static UiPointerEvent press(
            final float x,
            final float y) {
        return new UiPointerEvent(
                x,
                y,
                UiPointerButton.LEFT,
                UiPointerAction.PRESS);
    }

    private static UiKeyEvent character(
            final char character) {
        return new UiKeyEvent(
                new UiKey(
                        "character-" + character),
                UiKeyAction.PRESS,
                character,
                false,
                false,
                false);
    }

    private static UiKeyEvent key(
            final dev.trexzo.custommc.core.ui.UiKey key) {
        return new UiKeyEvent(
                key,
                UiKeyAction.PRESS,
                '\0',
                false,
                false,
                false);
    }
}
