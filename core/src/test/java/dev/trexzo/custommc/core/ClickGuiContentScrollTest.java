package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleState;
import dev.trexzo.custommc.core.ui.UiClipCommand;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiFocusManager;
import dev.trexzo.custommc.core.ui.UiPointerAction;
import dev.trexzo.custommc.core.ui.UiPointerButton;
import dev.trexzo.custommc.core.ui.UiPointerEvent;
import dev.trexzo.custommc.core.ui.UiScrollEvent;
import dev.trexzo.custommc.core.ui.UiTextCommand;
import dev.trexzo.custommc.core.ui.UiThemes;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiComposer;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ClickGuiContentScrollTest {
    @Test
    void contentWheelOffsetIsSharedByRenderingAndHitTesting() {
        final ModuleRegistry modules =
                populatedModules(20);
        final ModuleController controller =
                new ModuleController(modules);
        final ModuleListPageContent content =
                new ModuleListPageContent(
                        modules,
                        controller);

        final ClickGuiContentRegistry contents =
                new ClickGuiContentRegistry();
        contents.register(
                "modules",
                content);

        final ClickGuiModel model =
                new ClickGuiModel();
        model.register(
                new ClickGuiPage(
                        "modules",
                        "Modules",
                        0));
        model.open();

        final UiViewport viewport =
                new UiViewport(
                        1200,
                        800,
                        1.0F);
        final ClickGuiLayout layout =
                new ClickGuiLayoutEngine()
                        .layout(viewport);
        final ClickGuiComposer composer =
                new ClickGuiComposer(contents);

        final float before =
                textY(
                        composer.compose(
                                model.snapshot(),
                                viewport,
                                UiThemes.darkDefault()),
                        "module-00");

        try (ClickGuiInputController input =
                     new ClickGuiInputController(
                             model,
                             contents,
                             new UiFocusManager())) {
            final float wheelX =
                    layout.content().x() + 8.0F;
            final float wheelY =
                    layout.content().y() + 8.0F;

            assertTrue(
                    input.scroll(
                            new UiScrollEvent(
                                    wheelX,
                                    wheelY,
                                    -1.0F),
                            viewport));
            assertTrue(
                    input.scroll(
                            new UiScrollEvent(
                                    wheelX,
                                    wheelY,
                                    -1.0F),
                            viewport));

            final float after =
                    textY(
                            composer.compose(
                                    model.snapshot(),
                                    viewport,
                                    UiThemes.darkDefault()),
                            "module-00");
            assertEquals(
                    before - 56.0F,
                    after);

            assertTrue(
                    input.pointer(
                            new UiPointerEvent(
                                    layout.content().x() + 30.0F,
                                    layout.content().y() + 60.0F,
                                    UiPointerButton.LEFT,
                                    UiPointerAction.PRESS),
                            viewport));
        }

        assertEquals(
                ModuleState.DISABLED,
                controller.stateOf("module-00"));
        assertEquals(
                ModuleState.ENABLED,
                controller.stateOf("module-01"));
    }

    @Test
    void rightPressOnModuleRowIsConsumedWithoutTogglingModule() {
        final ModuleRegistry modules =
                populatedModules(1);
        final ModuleController controller =
                new ModuleController(modules);
        final ClickGuiContentRegistry contents =
                new ClickGuiContentRegistry();
        contents.register(
                "modules",
                new ModuleListPageContent(
                        modules,
                        controller));

        final ClickGuiModel model =
                new ClickGuiModel();
        model.register(
                new ClickGuiPage(
                        "modules",
                        "Modules",
                        0));
        model.open();

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
            assertTrue(
                    input.pointer(
                            new UiPointerEvent(
                                    layout.content().x() + 30.0F,
                                    layout.content().y() + 60.0F,
                                    UiPointerButton.RIGHT,
                                    UiPointerAction.PRESS),
                            viewport));
        }

        assertEquals(
                ModuleState.DISABLED,
                controller.stateOf("module-00"));
    }

    private static ModuleRegistry populatedModules(
            final int count) {
        final ModuleRegistry modules =
                new ModuleRegistry();
        for (int i = 0; i < count; i++) {
            final String id =
                    String.format(
                            "module-%02d",
                            i);
            modules.register(
                    new Module() {
                        @Override
                        public String id() {
                            return id;
                        }
                    });
        }
        return modules;
    }

    private static float textY(
            final List<UiDrawCommand> commands,
            final String expectedText) {
        for (UiDrawCommand command : commands) {
            if (command instanceof UiTextCommand) {
                final UiTextCommand text =
                        (UiTextCommand) command;
                if (expectedText.equals(
                        text.text())) {
                    return text.y();
                }
            }

            if (command instanceof UiClipCommand) {
                try {
                    return textY(
                            ((UiClipCommand) command)
                                    .commands(),
                            expectedText);
                } catch (IllegalArgumentException missing) {
                    // Keep searching siblings.
                }
            }
        }

        throw new IllegalArgumentException(
                "missing text command: " + expectedText);
    }
}
