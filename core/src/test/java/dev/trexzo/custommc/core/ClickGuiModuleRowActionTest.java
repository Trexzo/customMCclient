package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.module.ModuleCategoryRegistry;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleState;
import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiClipCommand;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiPointerAction;
import dev.trexzo.custommc.core.ui.UiPointerButton;
import dev.trexzo.custommc.core.ui.UiPointerEvent;
import dev.trexzo.custommc.core.ui.UiTextCommand;
import dev.trexzo.custommc.core.ui.UiThemes;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiContentContext;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiContentInputContext;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiNavigator;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiPage;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiSnapshot;
import dev.trexzo.custommc.core.ui.clickgui.ModuleListPageContent;
import dev.trexzo.custommc.core.ui.clickgui.ModuleSelectionModel;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ClickGuiModuleRowActionTest {
    private static final UiBounds CONTENT_BOUNDS =
            new UiBounds(200.0F, 100.0F, 500.0F, 320.0F);

    @Test
    void settingsButtonOpensDetailsWithoutEnablingAndNameIsClipped() {
        final String id =
                "very_long_module_name_that_must_not_paint_over_the_settings_control";
        final ModuleRegistry registry = registryWith(id);
        final ModuleController controller = new ModuleController(registry);
        final ModuleSelectionModel selection =
                new ModuleSelectionModel(registry);
        final ModuleListPageContent content =
                new ModuleListPageContent(
                        registry, controller,
                        new ModulePresentationRegistry(),
                        new ModuleCategoryRegistry(),
                        selection, "details");
        final ClickGuiPage page =
                new ClickGuiPage("modules", "Modules", 0);
        final ClickGuiSnapshot snapshot =
                new ClickGuiSnapshot(true, "modules", "",
                        Collections.singletonList(page));

        final List<UiDrawCommand> commands =
                content.compose(new ClickGuiContentContext(
                        snapshot, page, CONTENT_BOUNDS,
                        UiThemes.darkDefault()));
        UiClipCommand nameClip = null;
        boolean hasSettingsAffordance = false;
        for (UiDrawCommand command : commands) {
            if (command instanceof UiClipCommand) {
                final UiClipCommand clip = (UiClipCommand) command;
                for (UiDrawCommand child : clip.commands()) {
                    if (child instanceof UiTextCommand
                            && id.equals(((UiTextCommand) child).text())) {
                        nameClip = clip;
                    }
                }
            }
            if (command instanceof UiTextCommand
                    && "...".equals(((UiTextCommand) command).text())) {
                hasSettingsAffordance = true;
            }
        }
        assertNotNull(nameClip);
        assertTrue(hasSettingsAffordance);
        // Text ends before the separate toggle / details hit regions.
        assertTrue(nameClip.bounds().x() + nameClip.bounds().width()
                <= CONTENT_BOUNDS.x() + CONTENT_BOUNDS.width() - 90.0F);

        final AtomicReference<String> navigated = new AtomicReference<String>();
        final ClickGuiNavigator navigator = idOfPage -> {
            navigated.set(idOfPage);
            return true;
        };
        final ClickGuiContentInputContext input =
                new ClickGuiContentInputContext(
                        snapshot, page, CONTENT_BOUNDS, null, navigator);

        // First row: x=218..682, y=148..177; details in final 26px.
        assertTrue(content.pointer(input,
                press(670.0F, 160.0F, UiPointerButton.LEFT)));
        assertEquals(id, selection.selectedModuleId());
        assertEquals("details", navigated.get());
        assertEquals(ModuleState.DISABLED, controller.stateOf(id));

        // Left press elsewhere on the row still toggles it.
        assertTrue(content.pointer(input,
                press(230.0F, 160.0F, UiPointerButton.LEFT)));
        assertEquals(ModuleState.ENABLED, controller.stateOf(id));

        // Right-click shortcut remains available without changing state.
        assertTrue(content.pointer(input,
                press(250.0F, 160.0F, UiPointerButton.RIGHT)));
        assertEquals(ModuleState.ENABLED, controller.stateOf(id));
    }

    @Test
    void standaloneListHasNoDeadSettingsHotspot() {
        final ModuleRegistry registry = registryWith("simple");
        final ModuleController controller = new ModuleController(registry);
        final ModuleListPageContent content =
                new ModuleListPageContent(registry, controller);
        final ClickGuiPage page =
                new ClickGuiPage("modules", "Modules", 0);
        final ClickGuiSnapshot snapshot =
                new ClickGuiSnapshot(true, "modules", "",
                        Collections.singletonList(page));
        final ClickGuiContentInputContext input =
                new ClickGuiContentInputContext(
                        snapshot, page, CONTENT_BOUNDS);

        assertFalse(containsDetailsLabel(
                content.compose(new ClickGuiContentContext(
                        snapshot, page, CONTENT_BOUNDS,
                        UiThemes.darkDefault()))));
        assertTrue(content.pointer(input,
                press(670.0F, 160.0F, UiPointerButton.LEFT)));
        assertEquals(ModuleState.ENABLED, controller.stateOf("simple"));
        assertFalse(content.pointer(input,
                press(230.0F, 160.0F, UiPointerButton.RIGHT)));
    }

    private static boolean containsDetailsLabel(
            final List<UiDrawCommand> commands) {
        for (UiDrawCommand command : commands) {
            if (command instanceof UiTextCommand
                    && "...".equals(((UiTextCommand) command).text())) {
                return true;
            }
        }
        return false;
    }

    private static UiPointerEvent press(
            final float x,
            final float y,
            final UiPointerButton button) {
        return new UiPointerEvent(x, y, button, UiPointerAction.PRESS);
    }

    private static ModuleRegistry registryWith(final String id) {
        final ModuleRegistry registry = new ModuleRegistry();
        registry.register(new Module() {
            @Override
            public String id() {
                return id;
            }
        });
        return registry;
    }
}
