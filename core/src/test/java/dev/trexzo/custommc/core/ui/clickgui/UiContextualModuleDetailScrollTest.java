package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingBinding;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingDescriptor;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.SettingValueKind;
import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiClipCommand;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiPointerAction;
import dev.trexzo.custommc.core.ui.UiPointerButton;
import dev.trexzo.custommc.core.ui.UiPointerEvent;
import dev.trexzo.custommc.core.ui.UiRoundedRectCommand;
import dev.trexzo.custommc.core.ui.UiTextCommand;
import dev.trexzo.custommc.core.ui.UiThemes;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** S49 real source module context, independent of global Settings page. */
final class UiContextualModuleDetailScrollTest {
    private static final UiBounds PANEL =
            new UiBounds(215.0F, 33.0F, 580.0F, 376.0F);
    private static final ClickGuiPage PAGE =
            new ClickGuiPage("module-detail", "Module Details", 20);
    private static final ClickGuiSnapshot SNAPSHOT =
            new ClickGuiSnapshot(true, "module-detail", "",
                    Collections.singletonList(PAGE));

    @Test
    void longSourceModuleHasRealDraggableDetailRailWithoutEditingSettings() {
        final Fixture fixture = new Fixture();
        fixture.install("a", 24);
        fixture.install("b", 2);
        fixture.select("a");
        final List<UiDrawCommand> initial = fixture.compose();
        assertTrue(texts(initial).contains("24 settings"));
        final float firstY = thumbY(initial);
        assertEquals(PANEL.y() + 49.0F, firstY, .001F);

        final float railX = PANEL.x() + PANEL.width() - 9.0F;
        final float railBottom = PANEL.y() + PANEL.height() - 21.1F;
        assertTrue(fixture.pointer(railX, railBottom, UiPointerAction.PRESS));
        assertTrue(fixture.pointer(railX, railBottom - 12.0F,
                UiPointerAction.MOVE));
        assertTrue(thumbY(fixture.compose()) > firstY + 30.0F);
        assertTrue(fixture.pointer(railX + 70.0F, 850.0F,
                UiPointerAction.RELEASE));
        assertFalse(fixture.pointer(railX, railBottom - 20.0F,
                UiPointerAction.MOVE));
        for (Setting<?> setting : fixture.settings.snapshot()) {
            assertEquals(7, setting.get());
        }

        // Selecting another real module must not inherit A's midpage
        // scroll or any prior captured pointer. B fits without a rail.
        fixture.select("b");
        assertTrue(texts(fixture.compose()).contains("2 settings"));
        assertFalse(hasThumb(fixture.compose()));
        fixture.select("a");
        assertEquals(firstY, thumbY(fixture.compose()), .001F);
        assertEquals("a", fixture.selection.selectedModuleId());
    }

    @Test
    void changeSelectionWhileDraggingCancelsCaptureAndShowsEmptyDetailHint() {
        final Fixture fixture = new Fixture();
        fixture.install("many", 18);
        fixture.install("none", 0);
        fixture.select("many");
        final float x = PANEL.x() + PANEL.width() - 9.0F;
        final float y = PANEL.y() + PANEL.height() - 21.1F;
        assertTrue(fixture.pointer(x, y, UiPointerAction.PRESS));

        fixture.select("none");
        final List<UiDrawCommand> none = fixture.compose();
        assertTrue(texts(none).contains("0 settings"));
        assertTrue(texts(none).contains("No settings for this module"));
        assertFalse(hasThumb(none));
        assertFalse(fixture.pointer(x, y - 40.0F, UiPointerAction.MOVE));

        fixture.select("many");
        assertEquals(PANEL.y() + 49.0F,
                thumbY(fixture.compose()), .001F);
    }

    private static final class Fixture {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final ModulePresentationRegistry names =
                new ModulePresentationRegistry();
        final SettingRegistry settings = new SettingRegistry();
        final SettingPresentationRegistry settingsNames =
                new SettingPresentationRegistry();
        final ModuleSettingRegistry bindings =
                new ModuleSettingRegistry(modules, settings);
        final ModuleSelectionModel selection = new ModuleSelectionModel(modules);
        final ModuleDetailPageContent detail = new ModuleDetailPageContent(
                selection, controller, names, bindings, settings, settingsNames);

        void install(final String id, final int count) {
            modules.register(new Module() {
                @Override public String id() { return id; }
            });
            for (int i = 0; i < count; i++) {
                final String key = id + ".setting." + i;
                settings.register(new Setting<Integer>(
                        key, 7, value -> value >= 0));
                settingsNames.register(new SettingDescriptor(
                        key, "Setting " + i, SettingValueKind.INTEGER, i));
                bindings.register(new ModuleSettingBinding(id, key, i));
            }
        }

        void select(final String id) { selection.select(id); }

        List<UiDrawCommand> compose() {
            return detail.compose(new ClickGuiContentContext(
                    SNAPSHOT, PAGE, PANEL, UiThemes.graphite()));
        }

        boolean pointer(final float x, final float y,
                final UiPointerAction action) {
            return detail.pointer(new ClickGuiContentInputContext(
                    SNAPSHOT, PAGE, PANEL),
                    new UiPointerEvent(x, y, UiPointerButton.LEFT, action));
        }
    }

    private static List<String> texts(final List<UiDrawCommand> commands) {
        final List<String> out = new ArrayList<String>();
        for (UiDrawCommand command : commands) {
            if (command instanceof UiTextCommand) {
                out.add(((UiTextCommand) command).text());
            } else if (command instanceof UiClipCommand) {
                out.addAll(texts(((UiClipCommand) command).commands()));
            }
        }
        return out;
    }

    private static boolean hasThumb(final List<UiDrawCommand> commands) {
        for (UiDrawCommand command : commands) {
            if (command instanceof UiRoundedRectCommand) {
                final UiRoundedRectCommand r = (UiRoundedRectCommand) command;
                if (r.layer() == 6 && r.width() == 3.0F) return true;
            }
        }
        return false;
    }

    private static float thumbY(final List<UiDrawCommand> commands) {
        for (UiDrawCommand command : commands) {
            if (command instanceof UiRoundedRectCommand) {
                final UiRoundedRectCommand r = (UiRoundedRectCommand) command;
                if (r.layer() == 6 && r.width() == 3.0F) return r.y();
            }
        }
        throw new AssertionError("expected actual source rail thumb");
    }
}
