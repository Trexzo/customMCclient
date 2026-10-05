package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.module.ModuleCategoryRegistry;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleState;
import dev.trexzo.custommc.core.ui.UiFocusManager;
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
import dev.trexzo.custommc.core.ui.clickgui.ModuleSelectionModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ModuleDetailNavigationTest {
    @Test
    void rightClickSelectsStableModuleAndNavigatesToRegisteredDetailPage() {
        final Fixture fixture =
                new Fixture(true);

        try (ClickGuiInputController input =
                     fixture.input()) {
            assertTrue(
                    input.pointer(
                            fixture.press(
                                    UiPointerButton.RIGHT),
                            fixture.viewport));
        }

        assertEquals(
                "esp",
                fixture.selection.selectedModuleId());
        assertEquals(
                "module-detail",
                fixture.model.snapshot()
                        .selectedPageId());
        assertEquals(
                ModuleState.DISABLED,
                fixture.controller.stateOf("esp"));
    }

    @Test
    void missingDetailPageKeepsCurrentPageButRetainsSelection() {
        final Fixture fixture =
                new Fixture(false);

        try (ClickGuiInputController input =
                     fixture.input()) {
            assertTrue(
                    input.pointer(
                            fixture.press(
                                    UiPointerButton.RIGHT),
                            fixture.viewport));
        }

        assertEquals(
                "esp",
                fixture.selection.selectedModuleId());
        assertEquals(
                "modules",
                fixture.model.snapshot()
                        .selectedPageId());
    }

    @Test
    void leftClickTogglesLifecycleWithoutPromotingDetailPage() {
        final Fixture fixture =
                new Fixture(true);

        try (ClickGuiInputController input =
                     fixture.input()) {
            assertTrue(
                    input.pointer(
                            fixture.press(
                                    UiPointerButton.LEFT),
                            fixture.viewport));
        }

        assertEquals(
                ModuleState.ENABLED,
                fixture.controller.stateOf("esp"));
        assertEquals(
                "modules",
                fixture.model.snapshot()
                        .selectedPageId());
    }

    private static final class Fixture {
        private final ModuleRegistry modules =
                new ModuleRegistry();
        private final ModuleController controller;
        private final ModuleSelectionModel selection;
        private final ClickGuiModel model =
                new ClickGuiModel();
        private final ClickGuiContentRegistry contents =
                new ClickGuiContentRegistry();
        private final UiViewport viewport =
                new UiViewport(
                        1200,
                        800,
                        1.0F);
        private final ClickGuiLayout layout;

        Fixture(final boolean registerDetailPage) {
            modules.register(module("fly"));
            modules.register(module("esp"));
            controller =
                    new ModuleController(modules);
            selection =
                    new ModuleSelectionModel(modules);

            model.register(
                    new ClickGuiPage(
                            "modules",
                            "Modules",
                            0));
            if (registerDetailPage) {
                model.register(
                        new ClickGuiPage(
                                "module-detail",
                                "Module Details",
                                1));
            }
            model.setSearchQuery("esp");
            model.open();

            contents.register(
                    "modules",
                    new ModuleListPageContent(
                            modules,
                            controller,
                            new ModulePresentationRegistry(),
                            new ModuleCategoryRegistry(),
                            selection,
                            "module-detail"));

            layout =
                    new ClickGuiLayoutEngine()
                            .layout(viewport);
        }

        ClickGuiInputController input() {
            return new ClickGuiInputController(
                    model,
                    contents,
                    new UiFocusManager());
        }

        UiPointerEvent press(
                final UiPointerButton button) {
            return new UiPointerEvent(
                    layout.content().x()
                            + 24.0F
                            + 4.0F,
                    layout.content().y()
                            + 24.0F
                            + 34.0F
                            + 4.0F,
                    button,
                    UiPointerAction.PRESS);
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
}
