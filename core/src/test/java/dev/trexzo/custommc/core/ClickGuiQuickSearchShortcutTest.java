package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.ui.UiFocusManager;
import dev.trexzo.custommc.core.ui.UiKey;
import dev.trexzo.custommc.core.ui.UiKeyAction;
import dev.trexzo.custommc.core.ui.UiKeyEvent;
import dev.trexzo.custommc.core.ui.UiKeys;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiInputController;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiModel;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiPage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ClickGuiQuickSearchShortcutTest {
    @Test
    void ctrlFFocusesSearchAndEnterExitsWithoutClosingOrClearing() {
        final ClickGuiModel model = new ClickGuiModel();
        model.register(new ClickGuiPage("modules", "Modules", 0));
        model.open();
        final UiFocusManager focus = new UiFocusManager();
        try (ClickGuiInputController input =
                     new ClickGuiInputController(model, focus)) {
            assertTrue(input.key(key(new UiKey("legacy-key-33"),
                    true, false, UiKeyAction.PRESS)));
            assertEquals(ClickGuiInputController.SEARCH_FOCUS_ID,
                    focus.focusedId());
            assertTrue(input.key(new UiKeyEvent(
                    new UiKey("legacy-key-18"), UiKeyAction.PRESS,
                    'e', false, false, false)));
            assertEquals("e", model.snapshot().searchQuery());
            assertTrue(input.key(key(UiKeys.ENTER, false,
                    false, UiKeyAction.PRESS)));
            assertNull(focus.focusedId());
            assertTrue(model.snapshot().open());
            assertEquals("e", model.snapshot().searchQuery());
        }
    }

    @Test
    void closedGuiRepeatAndAltCannotActivateQuickSearch() {
        final ClickGuiModel model = new ClickGuiModel();
        model.register(new ClickGuiPage("modules", "Modules", 0));
        final UiFocusManager focus = new UiFocusManager();
        try (ClickGuiInputController input =
                     new ClickGuiInputController(model, focus)) {
            assertFalse(input.key(key(new UiKey("legacy-key-33"),
                    true, false, UiKeyAction.PRESS)));
            model.open();
            assertFalse(input.key(key(new UiKey("legacy-key-33"),
                    true, false, UiKeyAction.REPEAT)));
            assertFalse(input.key(key(new UiKey("legacy-key-33"),
                    true, true, UiKeyAction.PRESS)));
            assertNull(focus.focusedId());
        }
    }

    private static UiKeyEvent key(final UiKey key,
            final boolean control, final boolean alt,
            final UiKeyAction action) {
        return new UiKeyEvent(key, action, '\0',
                false, control, alt);
    }
}
