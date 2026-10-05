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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ModuleSelectionModelTest {
    @Test
    void selectionUsesStableRegisteredModuleIdsAndCanClear() {
        final ModuleRegistry modules =
                new ModuleRegistry();
        modules.register(module("render.esp"));

        final ModuleSelectionModel selection =
                new ModuleSelectionModel(modules);

        assertFalse(selection.hasSelection());
        assertNull(selection.selectedModuleId());
        assertNull(selection.selectedModule());

        selection.select("render.esp");

        assertTrue(selection.hasSelection());
        assertEquals(
                "render.esp",
                selection.selectedModuleId());
        assertEquals(
                modules.find("render.esp"),
                selection.selectedModule());
        assertTrue(
                selection.isSelected(
                        "render.esp"));

        selection.clear();

        assertFalse(selection.hasSelection());
        assertNull(selection.selectedModuleId());

        assertThrows(
                IllegalArgumentException.class,
                () -> selection.select(
                        "missing.module"));
    }

    @Test
    void rightClickSelectsWithoutChangingLifecycleAndLeftClickStillToggles() {
        final ModuleRegistry modules =
                new ModuleRegistry();
        modules.register(module("fly"));
        modules.register(module("esp"));

        final ModuleController controller =
                new ModuleController(modules);
        final ModuleSelectionModel selection =
                new ModuleSelectionModel(modules);
        final ClickGuiModel model =
                new ClickGuiModel();
        model.register(
                new ClickGuiPage(
                        "modules",
                        "Modules",
                        0));
        model.setSearchQuery("esp");
        model.open();

        final ClickGuiContentRegistry contents =
                new ClickGuiContentRegistry();
        contents.register(
                "modules",
                new ModuleListPageContent(
                        modules,
                        controller,
                        new ModulePresentationRegistry(),
                        new ModuleCategoryRegistry(),
                        selection));

        final UiViewport viewport =
                new UiViewport(
                        1200,
                        800,
                        1.0F);
        final ClickGuiLayout layout =
                new ClickGuiLayoutEngine()
                        .layout(viewport);
        final float x =
                layout.content().x()
                        + 24.0F
                        + 4.0F;
        final float y =
                layout.content().y()
                        + 24.0F
                        + 34.0F
                        + 4.0F;

        try (ClickGuiInputController input =
                     new ClickGuiInputController(
                             model,
                             contents,
                             new UiFocusManager())) {
            assertTrue(
                    input.pointer(
                            new UiPointerEvent(
                                    x,
                                    y,
                                    UiPointerButton.RIGHT,
                                    UiPointerAction.PRESS),
                            viewport));

            assertEquals(
                    "esp",
                    selection.selectedModuleId());
            assertEquals(
                    ModuleState.DISABLED,
                    controller.stateOf("esp"));

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
                    controller.stateOf("esp"));
            assertEquals(
                    "esp",
                    selection.selectedModuleId());
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
