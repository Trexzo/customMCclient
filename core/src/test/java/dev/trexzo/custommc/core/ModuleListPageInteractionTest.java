package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleDescriptor;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleState;
import dev.trexzo.custommc.core.ui.UiFocusManager;
import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiTextCommand;
import dev.trexzo.custommc.core.ui.UiThemes;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiContentContext;
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
import dev.trexzo.custommc.core.ui.clickgui.ModuleListPageContent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ModuleListPageInteractionTest {
    @Test
    void presentationSearchStillTogglesStableModuleId() {
        final ModuleRegistry modules =
                new ModuleRegistry();
        modules.register(module("fly"));
        modules.register(module("esp.internal"));

        final ModulePresentationRegistry presentations =
                new ModulePresentationRegistry();
        presentations.register(
                new ModuleDescriptor(
                        "esp.internal",
                        "ESP Overlay",
                        "Highlights players and entities"));

        final ModuleController controller =
                new ModuleController(modules);
        final ClickGuiModel model =
                new ClickGuiModel();
        model.register(
                new ClickGuiPage(
                        "modules",
                        "Modules",
                        0));
        model.setSearchQuery("overlay");
        model.open();

        final ClickGuiContentRegistry contents =
                new ClickGuiContentRegistry();
        contents.register(
                "modules",
                new ModuleListPageContent(
                        modules,
                        controller,
                        presentations));

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
                             new UiFocusManager())) {
            final float x =
                    layout.content().x()
                            + 24.0F
                            + 4.0F;
            final float y =
                    layout.content().y()
                            + 24.0F
                            + 34.0F
                            + 4.0F;

            assertTrue(
                    input.pointer(
                            new UiPointerEvent(
                                    x,
                                    y,
                                    UiPointerButton.LEFT,
                                    UiPointerAction.PRESS),
                            viewport));

            assertEquals(
                    ModuleState.ENABLED,
                    controller.stateOf("esp.internal"));
            assertEquals(
                    ModuleState.DISABLED,
                    controller.stateOf("fly"));

            assertTrue(
                    input.pointer(
                            new UiPointerEvent(
                                    x,
                                    y,
                                    UiPointerButton.LEFT,
                                    UiPointerAction.PRESS),
                            viewport));

            assertEquals(
                    ModuleState.DISABLED,
                    controller.stateOf("esp.internal"));
        }
    }

    private static Module module(
            final String id) {
        return new Module() {
            @Override
            public String id() {
                return id;
            }
        };
    }
    @Test
    void unmatchedSearchShowsEmptyStateWithoutMutatingModules() {
        final ModuleRegistry modules = new ModuleRegistry();
        modules.register(module("combat.autoClicker"));
        final ModuleController controller = new ModuleController(modules);
        final ModuleListPageContent content =
                new ModuleListPageContent(modules, controller);
        final ClickGuiModel model = new ClickGuiModel();
        model.register(new ClickGuiPage("modules", "Modules", 0));
        model.setSearchQuery("impossible-query");
        model.open();
        final List<UiDrawCommand> commands = content.compose(
                new ClickGuiContentContext(
                        model.snapshot(),
                        model.snapshot().pages().get(0),
                        new UiBounds(0, 0, 500, 350),
                        UiThemes.darkDefault()));

        boolean sawEmpty = false;
        boolean sawCount = false;
        for (UiDrawCommand command : commands) {
            if (command instanceof UiTextCommand) {
                final String text = ((UiTextCommand) command).text();
                sawEmpty |= "No modules match your search".equals(text);
                sawCount |= "0 modules".equals(text);
            }
        }
        assertTrue(sawEmpty);
        assertTrue(sawCount);
        assertEquals(ModuleState.DISABLED,
                controller.stateOf("combat.autoClicker"));

        model.setSearchQuery("");
        final List<UiDrawCommand> unfiltered = content.compose(
                new ClickGuiContentContext(
                        model.snapshot(),
                        model.snapshot().pages().get(0),
                        new UiBounds(0, 0, 500, 350),
                        UiThemes.darkDefault()));
        for (UiDrawCommand command : unfiltered) {
            if (command instanceof UiTextCommand) {
                assertFalse("No modules match your search".equals(
                        ((UiTextCommand) command).text()));
            }
        }
    }

}
