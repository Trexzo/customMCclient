package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiClipCommand;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiFocusManager;
import dev.trexzo.custommc.core.ui.UiKeyAction;
import dev.trexzo.custommc.core.ui.UiKeyEvent;
import dev.trexzo.custommc.core.ui.UiKeys;
import dev.trexzo.custommc.core.ui.UiPointerAction;
import dev.trexzo.custommc.core.ui.UiPointerButton;
import dev.trexzo.custommc.core.ui.UiPointerEvent;
import dev.trexzo.custommc.core.ui.UiTextCommand;
import dev.trexzo.custommc.core.ui.UiThemes;
import dev.trexzo.custommc.core.ui.UiViewport;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ClickGuiContextNavigationTest {
    private static ClickGuiModel model() {
        final ClickGuiModel m = new ClickGuiModel();
        m.register(new ClickGuiPage("modules", "Modules", 0));
        m.register(new ClickGuiPage("settings", "Settings", 10));
        m.register(new ClickGuiPage("hud-layout", "HUD Layout", 15));
        m.register(new ClickGuiPage("module-detail", "Module Details", 20));
        m.open();
        return m;
    }

    private static UiKeyEvent press(final dev.trexzo.custommc.core.ui.UiKey key) {
        return new UiKeyEvent(key, UiKeyAction.PRESS, '\0',
                false, false, false);
    }

    @Test
    void contextualDetailRegisteredButHiddenFromSidebar() {
        final ClickGuiModel model = model();
        assertEquals(4, model.snapshot().pages().size());
        final List<ClickGuiPage> visible =
                ClickGuiNavigationPages.visible(model.snapshot());
        assertEquals(3, visible.size());
        assertEquals("Modules,Settings,HUD Layout",
                visible.stream().map(ClickGuiPage::title)
                        .collect(Collectors.joining(",")));
        assertTrue(model.select("module-detail"));
        assertEquals("modules",
                ClickGuiNavigationPages.highlightedId(model.snapshot()));

        final UiViewport viewport = new UiViewport(854, 480, 1.0F);
        final ClickGuiLayout layout = new ClickGuiLayoutEngine().layout(viewport);
        final List<UiDrawCommand> commands = new ClickGuiComposer().compose(
                model.snapshot(), viewport, UiThemes.graphite());
        UiClipCommand nav = null;
        for (UiDrawCommand command : commands) {
            if (command instanceof UiClipCommand
                    && ((UiClipCommand) command).bounds().equals(layout.navigation())) {
                nav = (UiClipCommand) command;
                break;
            }
        }
        assertTrue(nav != null);
        assertFalse(nav.commands().stream()
                .filter(c -> c instanceof UiTextCommand)
                .map(c -> ((UiTextCommand) c).text())
                .anyMatch("Module Details"::equals));
    }

    @Test
    void keyboardAndPointerNeverLandOnHiddenRootEntry() {
        final ClickGuiModel model = model();
        final UiViewport viewport = new UiViewport(854, 480, 1.0F);
        final ClickGuiLayout layout = new ClickGuiLayoutEngine().layout(viewport);
        try (ClickGuiInputController input =
                new ClickGuiInputController(model, new UiFocusManager())) {
            assertTrue(input.key(press(UiKeys.DOWN)));
            assertEquals("settings", model.snapshot().selectedPageId());
            assertTrue(input.key(press(UiKeys.DOWN)));
            assertEquals("hud-layout", model.snapshot().selectedPageId());
            assertTrue(input.key(press(UiKeys.DOWN)));
            assertEquals("modules", model.snapshot().selectedPageId());
            assertTrue(model.select("module-detail"));
            // Esc returns from contextual details without closing the GUI.
            assertTrue(input.key(press(UiKeys.ESCAPE)));
            assertTrue(model.snapshot().open());
            assertEquals("modules", model.snapshot().selectedPageId());

            // Third phantom sidebar row used to be the Module Details
            // landing zone, but now falls within unoccupied navigation.
            final float y = layout.navigation().y()
                    + 3 * (ClickGuiMetrics.PAGE_HEIGHT + ClickGuiMetrics.PAGE_GAP)
                    + 10;
            assertTrue(input.pointer(new UiPointerEvent(
                    layout.navigation().x() + 10, y,
                    UiPointerButton.LEFT, UiPointerAction.PRESS), viewport));
            assertEquals("modules", model.snapshot().selectedPageId());

            assertTrue(input.key(press(UiKeys.ESCAPE)));
            assertFalse(model.snapshot().open());
        }
    }

    @Test
    void backLinkDrawAndHitTargetAreSameAndRequireWideView() {
        final UiBounds content = new UiBounds(215, 33, 580, 376);
        final List<UiDrawCommand> commands =
                UiModuleDetailBackLink.compose(content, UiThemes.graphite());
        assertEquals(2, commands.size());
        assertTrue(UiModuleDetailBackLink.hit(content, 685, 46));
        assertFalse(UiModuleDetailBackLink.hit(content, 660, 46));
        assertFalse(UiModuleDetailBackLink.hit(content, 685, 65));
        assertTrue(UiModuleDetailBackLink.compose(
                new UiBounds(1, 1, 120, 70), UiThemes.graphite()).isEmpty());
    }
}
