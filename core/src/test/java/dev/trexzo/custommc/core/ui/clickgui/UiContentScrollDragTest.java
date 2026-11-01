package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleState;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.SettingValueKind;
import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiPointerAction;
import dev.trexzo.custommc.core.ui.UiPointerButton;
import dev.trexzo.custommc.core.ui.UiPointerEvent;
import dev.trexzo.custommc.core.ui.UiRoundedRectCommand;
import dev.trexzo.custommc.core.ui.UiThemes;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class UiContentScrollDragTest {
    // The actual 854x480 Minecraft 1.8.9 native screenshot's content panel.
    private static final UiBounds PANEL = new UiBounds(
            215.0F, 33.0F, 580.0F, 376.0F);

    @Test
    void nativeRailGrabIsStableAcrossMoveAndReleaseBeyondPanel() {
        final float total = 1800.0F;
        final ClickGuiContentScrollState scroll =
                new ClickGuiContentScrollState();
        final UiContentScrollDrag drag = new UiContentScrollDrag();
        final UiBounds track = UiContentScrollRail.trackBounds(PANEL, total);
        final UiBounds thumb = UiContentScrollRail.thumbBounds(PANEL, total, 0);
        assertNotNull(track);
        assertNotNull(thumb);
        final float x = track.x() + 1.0F;
        final float pressedY = thumb.y() + thumb.height() * .75F;
        assertTrue(drag.pointer(pointer(x, pressedY,
                UiPointerButton.LEFT, UiPointerAction.PRESS),
                PANEL, total, scroll));
        assertEquals(0.0F, scroll.offset(total, PANEL.height()), .001F);
        assertTrue(drag.dragging());

        // The exact grab point moves to the end without a thumb jump.
        final float endY = track.y() + track.height()
                - thumb.height() * .25F;
        assertTrue(drag.pointer(pointer(x, endY,
                UiPointerButton.LEFT, UiPointerAction.MOVE),
                PANEL, total, scroll));
        assertEquals(total - PANEL.height(),
                scroll.offset(total, PANEL.height()), .01F);
        assertTrue(drag.pointer(pointer(x + 80.0F, 900.0F,
                UiPointerButton.LEFT, UiPointerAction.RELEASE),
                PANEL, total, scroll));
        assertFalse(drag.dragging());
        assertFalse(drag.pointer(pointer(x, track.y(),
                UiPointerButton.LEFT, UiPointerAction.MOVE),
                PANEL, total, scroll));
        assertEquals(total - PANEL.height(),
                scroll.offset(total, PANEL.height()), .01F);
    }

    @Test
    void emptyTrackClickSeeksAndRailCannotReactWithoutOverflow() {
        final ClickGuiContentScrollState scroll =
                new ClickGuiContentScrollState();
        final UiContentScrollDrag drag = new UiContentScrollDrag();
        final float total = 1800.0F;
        final UiBounds track = UiContentScrollRail.trackBounds(PANEL, total);
        assertFalse(drag.pointer(pointer(track.x(), track.y() + 2,
                UiPointerButton.RIGHT, UiPointerAction.PRESS),
                PANEL, total, scroll));
        assertTrue(drag.pointer(pointer(track.x(), track.y() + track.height(),
                UiPointerButton.LEFT, UiPointerAction.PRESS),
                PANEL, total, scroll));
        assertEquals(total - PANEL.height(),
                scroll.offset(total, PANEL.height()), .001F);
        assertTrue(drag.pointer(pointer(track.x(), track.y(),
                UiPointerButton.LEFT, UiPointerAction.RELEASE),
                PANEL, total, scroll));
        assertFalse(drag.pointer(pointer(track.x(), track.y() + 2,
                UiPointerButton.LEFT, UiPointerAction.PRESS),
                PANEL, PANEL.height(), scroll));
        assertEquals(0.0F,
                scroll.offset(PANEL.height(), PANEL.height()), .001F);
        assertEquals(null, UiContentScrollRail.hitBounds(
                PANEL, PANEL.height()));
    }

    @Test
    void moduleListRailDoesNotToggleOrOpenAnySourceModule() {
        final ModuleRegistry modules = new ModuleRegistry();
        for (int n = 0; n < 30; n++) {
            final String id = String.format("test.module.%02d", n);
            modules.register(new Module() {
                @Override public String id() { return id; }
            });
        }
        final ModuleController controller = new ModuleController(modules);
        final ModuleListPageContent content =
                new ModuleListPageContent(modules, controller);
        final ClickGuiPage page = new ClickGuiPage("modules", "Modules", 0);
        final ClickGuiSnapshot snapshot = new ClickGuiSnapshot(
                true, "modules", "", Collections.singletonList(page));
        final ClickGuiContentInputContext input =
                new ClickGuiContentInputContext(snapshot, page, PANEL);
        final float x = PANEL.x() + PANEL.width() - 9.0F;
        final float bottom = PANEL.y() + PANEL.height() - 23.0F;
        assertTrue(content.pointer(input, pointer(x, bottom,
                UiPointerButton.LEFT, UiPointerAction.PRESS)));
        assertTrue(content.pointer(input, pointer(x, bottom - 50,
                UiPointerButton.LEFT, UiPointerAction.MOVE)));
        assertTrue(content.pointer(input, pointer(x, bottom,
                UiPointerButton.LEFT, UiPointerAction.RELEASE)));
        for (Module module : modules.snapshot()) {
            assertEquals(ModuleState.DISABLED, controller.stateOf(module.id()));
        }
        // Actual displayed rail thumb now follows the changed page offset.
        final List<UiDrawCommand> commands = content.compose(
                new ClickGuiContentContext(
                        snapshot, page, PANEL, UiThemes.graphite()));
        assertTrue(visibleThumbY(commands) > PANEL.y() + 49.0F);
    }

    @Test
    void settingsListRailDoesNotMutateOrFocusAnyRealSetting() {
        final SettingRegistry settings = new SettingRegistry();
        final SettingPresentationRegistry presentations =
                new SettingPresentationRegistry();
        for (int n = 0; n < 30; n++) {
            final String id = String.format("test.number.%02d", n);
            settings.register(new Setting<Integer>(id, 7, value -> value >= 0));
            presentations.register(new SettingDescriptor(
                    id, "Setting " + n, SettingValueKind.INTEGER, n));
        }
        final SettingListPageContent content =
                new SettingListPageContent(settings, presentations);
        final ClickGuiPage page = new ClickGuiPage("settings", "Settings", 0);
        final ClickGuiSnapshot snapshot = new ClickGuiSnapshot(
                true, "settings", "", Collections.singletonList(page));
        final ClickGuiContentInputContext input =
                new ClickGuiContentInputContext(snapshot, page, PANEL);
        final float x = PANEL.x() + PANEL.width() - 9.0F;
        final float bottom = PANEL.y() + PANEL.height() - 23.0F;
        assertTrue(content.pointer(input, pointer(x, bottom,
                UiPointerButton.LEFT, UiPointerAction.PRESS)));
        assertTrue(content.pointer(input, pointer(x, bottom - 70,
                UiPointerButton.LEFT, UiPointerAction.MOVE)));
        assertTrue(content.pointer(input, pointer(x, bottom,
                UiPointerButton.LEFT, UiPointerAction.RELEASE)));
        for (Setting<?> setting : settings.snapshot()) {
            assertEquals(7, setting.get());
        }
        final List<UiDrawCommand> commands = content.compose(
                new ClickGuiContentContext(
                        snapshot, page, PANEL, UiThemes.graphite()));
        assertTrue(visibleThumbY(commands) > PANEL.y() + 49.0F);
    }

    private static float visibleThumbY(final List<UiDrawCommand> commands) {
        for (UiDrawCommand item : commands) {
            if (item instanceof UiRoundedRectCommand) {
                final UiRoundedRectCommand rect = (UiRoundedRectCommand) item;
                if (rect.width() == 3.0F && rect.layer() == 6) {
                    return rect.y();
                }
            }
        }
        throw new AssertionError("native rail thumb not drawn");
    }

    private static UiPointerEvent pointer(
            final float x, final float y, final UiPointerButton button,
            final UiPointerAction action) {
        return new UiPointerEvent(x, y, button, action);
    }
}
