package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
import dev.trexzo.custommc.core.setting.SettingNumericSpec;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.SettingValueKind;
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
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiMetrics;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiModel;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiPage;
import dev.trexzo.custommc.core.ui.clickgui.SettingListPageContent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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


    @Test
    void contentRightPressReachesNumericSettingEditor() {
        final ClickGuiModel model =
                new ClickGuiModel();
        model.register(
                new ClickGuiPage(
                        "settings",
                        "Settings",
                        0));
        model.open();

        final SettingRegistry settings =
                new SettingRegistry();
        final Setting<Integer> range =
                new Setting<Integer>(
                        "combat.range",
                        3,
                        value -> value >= 1 && value <= 6,
                        SettingCodecs.INTEGER);
        settings.register(range);

        final SettingPresentationRegistry presentations =
                new SettingPresentationRegistry();
        presentations.register(
                new SettingDescriptor(
                        "combat.range",
                        "Attack Range",
                        SettingValueKind.INTEGER,
                        0,
                        new SettingNumericSpec(
                                1.0D,
                                6.0D,
                                1.0D)));

        final ClickGuiContentRegistry contents =
                new ClickGuiContentRegistry();
        contents.register(
                "settings",
                new SettingListPageContent(
                        settings,
                        presentations));

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
                             contents,
                             focus)) {
            assertTrue(
                    input.pointer(
                            press(
                                    layout.content().x() + 30.0F,
                                    layout.content().y() + 60.0F,
                                    UiPointerButton.RIGHT),
                            viewport));
        }

        assertEquals(
                Integer.valueOf(2),
                range.get());
    }

    @Test
    void nonLeftNavigationPressIsConsumedWithoutChangingPage() {
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
        final float secondRowY =
                layout.navigation().y()
                        + ClickGuiMetrics.PAGE_HEIGHT
                        + ClickGuiMetrics.PAGE_GAP
                        + 2.0F;

        try (ClickGuiInputController input =
                     new ClickGuiInputController(
                             model,
                             focus)) {
            assertTrue(
                    input.pointer(
                            press(
                                    layout.navigation().x() + 3.0F,
                                    secondRowY,
                                    UiPointerButton.RIGHT),
                            viewport));
        }

        assertEquals(
                "combat",
                model.snapshot().selectedPageId());
    }


    @Test
    void textSettingDraftCommitsOnlyOnEnter() {
        final ClickGuiModel model =
                new ClickGuiModel();
        model.register(
                new ClickGuiPage(
                        "settings",
                        "Settings",
                        0));
        model.open();

        final SettingRegistry settings =
                new SettingRegistry();
        final Setting<String> name =
                new Setting<String>(
                        "profile.name",
                        "abc",
                        value -> value.length() <= 8,
                        SettingCodecs.STRING);
        settings.register(name);

        final SettingPresentationRegistry presentations =
                new SettingPresentationRegistry();
        presentations.register(
                new SettingDescriptor(
                        "profile.name",
                        "Profile Name",
                        SettingValueKind.TEXT,
                        0));

        final ClickGuiContentRegistry contents =
                new ClickGuiContentRegistry();
        contents.register(
                "settings",
                new SettingListPageContent(
                        settings,
                        presentations));

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
                             contents,
                             focus)) {
            assertTrue(
                    input.pointer(
                            press(
                                    layout.content().x() + 30.0F,
                                    layout.content().y() + 60.0F),
                            viewport));
            assertNotNull(focus.focusedId());

            assertTrue(input.key(character('x')));
            assertEquals(
                    "abc",
                    name.get());

            assertTrue(input.key(key(UiKeys.BACKSPACE)));
            assertEquals(
                    "abc",
                    name.get());

            assertTrue(input.key(character('z')));
            assertEquals(
                    "abc",
                    name.get());

            assertTrue(input.key(key(UiKeys.ENTER)));
            assertEquals(
                    "abcz",
                    name.get());
            assertNull(focus.focusedId());
        }
    }

    @Test
    void rejectedTextCommitStaysFocusedUntilEscapeCancelsDraft() {
        final ClickGuiModel model =
                new ClickGuiModel();
        model.register(
                new ClickGuiPage(
                        "settings",
                        "Settings",
                        0));
        model.open();

        final SettingRegistry settings =
                new SettingRegistry();
        final Setting<String> code =
                new Setting<String>(
                        "profile.code",
                        "ok",
                        value -> value.length() <= 2,
                        SettingCodecs.STRING);
        settings.register(code);

        final SettingPresentationRegistry presentations =
                new SettingPresentationRegistry();
        presentations.register(
                new SettingDescriptor(
                        "profile.code",
                        "Profile Code",
                        SettingValueKind.TEXT,
                        0));

        final ClickGuiContentRegistry contents =
                new ClickGuiContentRegistry();
        contents.register(
                "settings",
                new SettingListPageContent(
                        settings,
                        presentations));

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
                             contents,
                             focus)) {
            assertTrue(
                    input.pointer(
                            press(
                                    layout.content().x() + 30.0F,
                                    layout.content().y() + 60.0F),
                            viewport));
            assertTrue(input.key(character('x')));

            final String textFocusId =
                    focus.focusedId();
            assertNotNull(textFocusId);

            assertTrue(input.key(key(UiKeys.ENTER)));
            assertEquals(
                    "ok",
                    code.get());
            assertEquals(
                    textFocusId,
                    focus.focusedId());

            assertTrue(input.key(key(UiKeys.ESCAPE)));
            assertNull(focus.focusedId());
            assertEquals(
                    "ok",
                    code.get());
            assertTrue(model.snapshot().open());
        }
    }

    @Test
    void controllerCloseCancelsActiveTextDraft() {
        final ClickGuiModel model =
                new ClickGuiModel();
        model.register(
                new ClickGuiPage(
                        "settings",
                        "Settings",
                        0));
        model.open();

        final SettingRegistry settings =
                new SettingRegistry();
        final Setting<String> name =
                new Setting<String>(
                        "profile.name",
                        "abc",
                        value -> true,
                        SettingCodecs.STRING);
        settings.register(name);

        final SettingPresentationRegistry presentations =
                new SettingPresentationRegistry();
        presentations.register(
                new SettingDescriptor(
                        "profile.name",
                        "Profile Name",
                        SettingValueKind.TEXT,
                        0));

        final ClickGuiContentRegistry contents =
                new ClickGuiContentRegistry();
        contents.register(
                "settings",
                new SettingListPageContent(
                        settings,
                        presentations));

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
                        contents,
                        focus);

        assertTrue(
                input.pointer(
                        press(
                                layout.content().x() + 30.0F,
                                layout.content().y() + 60.0F),
                        viewport));
        assertTrue(input.key(character('x')));
        assertNotNull(focus.focusedId());

        input.close();

        assertNull(focus.focusedId());
        assertEquals(
                "abc",
                name.get());
    }


    @Test
    void capturedNumericScrubReceivesOutsideMovesAndUnownedHoverIsVetoed() {
        final ClickGuiModel model = new ClickGuiModel();
        model.register(new ClickGuiPage("settings", "Settings", 0));
        model.open();
        final SettingRegistry settings = new SettingRegistry();
        final Setting<Integer> range = new Setting<Integer>(
                "ui.outsideDrag", 40,
                value -> value != null && value >= 0 && value <= 100,
                SettingCodecs.INTEGER);
        settings.register(range);
        final SettingPresentationRegistry presentations =
                new SettingPresentationRegistry();
        presentations.register(new SettingDescriptor(
                "ui.outsideDrag", "Outside Drag",
                SettingValueKind.INTEGER, 0,
                new SettingNumericSpec(0.0D, 100.0D, 5.0D)));
        final ClickGuiContentRegistry contents =
                new ClickGuiContentRegistry();
        final SettingListPageContent page =
                new SettingListPageContent(settings, presentations);
        contents.register("settings", page);
        final UiViewport viewport = new UiViewport(1200, 800, 1.0F);
        final ClickGuiLayout layout =
                new ClickGuiLayoutEngine().layout(viewport);
        final float outsideRight = layout.content().x()
                + layout.content().width() + 100.0F;
        final float outsideLeft = layout.content().x() - 100.0F;
        final float rowY = layout.content().y() + 68.0F;
        final float wellX = layout.content().x()
                + layout.content().width() - 155.0F;
        try (ClickGuiInputController input = new ClickGuiInputController(
                model, contents, new UiFocusManager())) {
            // No owner: outside content motion remains rejected.
            assertFalse(input.pointer(new UiPointerEvent(
                    outsideRight, rowY, UiPointerButton.LEFT,
                    UiPointerAction.MOVE), viewport));
            assertEquals(Integer.valueOf(40), range.get());
            assertTrue(input.pointer(press(wellX, rowY), viewport));
            assertTrue(page.capturesPointerDrag());
            // The old S51 controller dropped both events at this boundary;
            // the underlying scrub itself already clamps correctly.
            assertTrue(input.pointer(new UiPointerEvent(
                    outsideRight, rowY, UiPointerButton.LEFT,
                    UiPointerAction.MOVE), viewport));
            assertEquals(Integer.valueOf(100), range.get());
            assertTrue(input.pointer(new UiPointerEvent(
                    outsideLeft, rowY, UiPointerButton.LEFT,
                    UiPointerAction.MOVE), viewport));
            assertEquals(Integer.valueOf(0), range.get());
            assertTrue(input.pointer(new UiPointerEvent(
                    outsideRight, rowY, UiPointerButton.LEFT,
                    UiPointerAction.RELEASE), viewport));
            assertFalse(page.capturesPointerDrag());
            assertFalse(input.pointer(new UiPointerEvent(
                    outsideRight, rowY, UiPointerButton.LEFT,
                    UiPointerAction.MOVE), viewport));
            assertEquals(Integer.valueOf(0), range.get());
            assertEquals("settings", model.snapshot().selectedPageId());
        }
    }


    @Test
    void dragCaptureCannotResumeAfterHiddenPageSwitchOrGuiReopen() {
        final ClickGuiModel model = new ClickGuiModel();
        model.register(new ClickGuiPage("settings", "Settings", 0));
        model.register(new ClickGuiPage("other", "Other", 10));
        model.open();
        final SettingRegistry settings = new SettingRegistry();
        final Setting<Integer> range = new Setting<Integer>(
                "ui.sessionRange", 40, x -> x != null && x >= 0 && x <= 100,
                SettingCodecs.INTEGER);
        settings.register(range);
        final SettingPresentationRegistry descriptions =
                new SettingPresentationRegistry();
        descriptions.register(new SettingDescriptor(
                "ui.sessionRange", "Session Range", SettingValueKind.INTEGER,
                0, new SettingNumericSpec(0, 100, 5)));
        final SettingListPageContent page =
                new SettingListPageContent(settings, descriptions);
        final ClickGuiContentRegistry contents =
                new ClickGuiContentRegistry();
        contents.register("settings", page);
        final UiViewport viewport = new UiViewport(1200, 800, 1.0F);
        final ClickGuiLayout layout =
                new ClickGuiLayoutEngine().layout(viewport);
        final float x = layout.content().x() + layout.content().width() - 155;
        final float y = layout.content().y() + 68;
        final float outside = layout.content().x()
                + layout.content().width() + 100;
        final UiPointerEvent outsideMove = new UiPointerEvent(
                outside, y, UiPointerButton.LEFT, UiPointerAction.MOVE);
        final ClickGuiInputController input = new ClickGuiInputController(
                model, contents, new UiFocusManager());
        try {
            assertTrue(input.pointer(press(x, y), viewport));
            assertTrue(page.capturesPointerDrag());
            final int firstValue = range.get();

            // No pointer events between A -> B -> A: comparing page name
            // alone is not enough to reject an older captured gesture.
            model.select("other");
            model.select("settings");
            assertFalse(input.pointer(outsideMove, viewport));
            assertFalse(page.capturesPointerDrag());
            assertEquals(firstValue, range.get());

            assertTrue(input.pointer(press(x, y), viewport));
            assertTrue(page.capturesPointerDrag());
            model.close();
            model.open();
            assertFalse(input.pointer(outsideMove, viewport));
            assertFalse(page.capturesPointerDrag());

            // Unchanged context retains active S52 outside-drag behavior.
            assertTrue(input.pointer(press(x, y), viewport));
            assertTrue(input.pointer(outsideMove, viewport));
            assertEquals(Integer.valueOf(100), range.get());
            assertTrue(input.pointer(new UiPointerEvent(outside, y,
                    UiPointerButton.LEFT, UiPointerAction.RELEASE), viewport));
            assertFalse(page.capturesPointerDrag());

            assertTrue(input.pointer(press(x, y), viewport));
            assertTrue(page.capturesPointerDrag());
        } finally {
            input.close();
        }
        assertFalse(page.capturesPointerDrag());
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
        return press(
                x,
                y,
                UiPointerButton.LEFT);
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
    @Test
    void clearButtonRemovesOnlySearchQueryAndPreservesSearchFocus() {
        final ClickGuiModel model = model();
        model.setSearchQuery("render");
        final UiFocusManager focus = new UiFocusManager();
        final UiViewport viewport = new UiViewport(1200, 800, 1.0F);
        final ClickGuiLayout layout = new ClickGuiLayoutEngine().layout(viewport);
        final float x = layout.search().x() + layout.search().width() - 8.0F;
        final float y = layout.search().y() + 8.0F;

        try (ClickGuiInputController input =
                     new ClickGuiInputController(model, focus)) {
            assertTrue(input.pointer(
                    press(x, y, UiPointerButton.RIGHT), viewport));
            assertEquals("render", model.snapshot().searchQuery());
            assertNull(focus.focusedId());

            assertTrue(input.pointer(
                    press(x, y, UiPointerButton.LEFT), viewport));
            assertEquals("", model.snapshot().searchQuery());
            assertEquals(ClickGuiInputController.SEARCH_FOCUS_ID,
                    focus.focusedId());
            assertTrue(model.snapshot().open());
            assertEquals("combat", model.snapshot().selectedPageId());
        }
    }

    @Test
    void keyboardNavigationCyclesPagesWithoutStealingFocusedSearch() {
        final ClickGuiModel model = model();
        final UiFocusManager focus = new UiFocusManager();
        final UiViewport viewport = new UiViewport(1200, 800, 1.0F);
        final ClickGuiLayout layout = new ClickGuiLayoutEngine().layout(viewport);
        try (ClickGuiInputController input =
                     new ClickGuiInputController(model, focus)) {
            assertEquals("combat", model.snapshot().selectedPageId());
            assertTrue(input.key(key(UiKeys.UP)));
            assertEquals("render", model.snapshot().selectedPageId());
            assertTrue(input.key(key(UiKeys.DOWN)));
            assertEquals("combat", model.snapshot().selectedPageId());
            assertTrue(input.key(key(UiKeys.END)));
            assertEquals("render", model.snapshot().selectedPageId());
            assertTrue(input.key(key(UiKeys.HOME)));
            assertEquals("combat", model.snapshot().selectedPageId());
            assertNull(focus.focusedId());

            assertTrue(input.pointer(press(layout.search().x() + 3.0F,
                    layout.search().y() + 3.0F), viewport));
            assertEquals(ClickGuiInputController.SEARCH_FOCUS_ID,
                    focus.focusedId());
            assertFalse(input.key(key(UiKeys.DOWN)));
            assertEquals("combat", model.snapshot().selectedPageId());
            assertTrue(input.key(key(UiKeys.ESCAPE)));
            assertNull(focus.focusedId());
            assertTrue(input.key(key(UiKeys.DOWN)));
            assertEquals("render", model.snapshot().selectedPageId());
            assertTrue(model.snapshot().open());
        }
    }

    @Test
    void navigationRejectsModifiersRepeatAndEmptyOrClosedModels() {
        final ClickGuiModel model = model();
        try (ClickGuiInputController input =
                     new ClickGuiInputController(model, new UiFocusManager())) {
            assertFalse(input.key(new UiKeyEvent(
                    UiKeys.DOWN, UiKeyAction.REPEAT, '\0',
                    false, false, false)));
            assertFalse(input.key(new UiKeyEvent(
                    UiKeys.DOWN, UiKeyAction.PRESS, '\0',
                    false, true, false)));
            assertFalse(input.key(new UiKeyEvent(
                    UiKeys.DOWN, UiKeyAction.PRESS, '\0',
                    false, false, true)));
            assertEquals("combat", model.snapshot().selectedPageId());
            model.close();
            assertFalse(input.key(key(UiKeys.DOWN)));
        }
        final ClickGuiModel empty = new ClickGuiModel();
        empty.open();
        try (ClickGuiInputController input =
                     new ClickGuiInputController(empty, new UiFocusManager())) {
            assertFalse(input.key(key(UiKeys.UP)));
            assertNull(empty.snapshot().selectedPageId());
        }
    }
}
