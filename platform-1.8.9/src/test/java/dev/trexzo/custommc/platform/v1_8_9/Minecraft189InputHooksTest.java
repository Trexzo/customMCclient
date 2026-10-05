package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.event.EventBus;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.service.ServiceRegistry;
import dev.trexzo.custommc.core.ui.UiFocusManager;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiInputController;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiLayout;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiLayoutEngine;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiModel;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiPage;
import dev.trexzo.custommc.platform.PlatformContext;
import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189InputHooksTest {
    @Test
    void legacyPointerAndKeysReachRetainedClickGui() {
        final ClickGuiModel model =
                new ClickGuiModel();
        model.register(
                new ClickGuiPage(
                        "combat",
                        "Combat",
                        0));
        model.open();

        final UiFocusManager focus =
                new UiFocusManager();
        final ClickGuiInputController input =
                new ClickGuiInputController(
                        model,
                        focus);

        try {
            final Minecraft189InputHooks hooks =
                    hooks(input);
            final UiViewport viewport =
                    new UiViewport(
                            1200,
                            800,
                            1.0F);
            final ClickGuiLayout layout =
                    new ClickGuiLayoutEngine()
                            .layout(viewport);

            final int pixelX =
                    Math.round(layout.search().x() + 2.0F);
            final int logicalY =
                    Math.round(layout.search().y() + 2.0F);
            final int pixelYFromBottom =
                    viewport.pixelHeight()
                            - 1
                            - logicalY;

            assertTrue(
                    hooks.pointerButton(
                            viewport.pixelWidth(),
                            viewport.pixelHeight(),
                            viewport.scale(),
                            pixelX,
                            pixelYFromBottom,
                            0,
                            true));
            assertEquals(
                    ClickGuiInputController.SEARCH_FOCUS_ID,
                    focus.focusedId());

            assertTrue(
                    hooks.key(
                            30,
                            'e',
                            true,
                            false,
                            false,
                            false,
                            false));
            assertEquals(
                    "e",
                    model.snapshot().searchQuery());

            assertTrue(
                    hooks.key(
                            LegacyKeyboardCodes.ESCAPE,
                            '\0',
                            true,
                            false,
                            false,
                            false,
                            false));
            assertNull(focus.focusedId());
            assertTrue(model.snapshot().open());

            assertTrue(
                    hooks.key(
                            LegacyKeyboardCodes.ESCAPE,
                            '\0',
                            true,
                            false,
                            false,
                            false,
                            false));
            assertFalse(model.snapshot().open());
        } finally {
            input.close();
        }
    }

    @Test
    void unsupportedPointerButtonDoesNotRequireInputService() {
        final Minecraft189Platform platform =
                platform(new ServiceRegistry());
        final Minecraft189InputHooks hooks =
                new Minecraft189InputHooks(platform);

        assertFalse(
                hooks.pointerButton(
                        1200,
                        800,
                        1.0F,
                        100,
                        100,
                        4,
                        true));
    }

    private static Minecraft189InputHooks hooks(
            final ClickGuiInputController input) {
        final ServiceRegistry services =
                new ServiceRegistry();
        services.register(
                ClickGuiInputController.class,
                input);
        return new Minecraft189InputHooks(
                platform(services));
    }

    private static Minecraft189Platform platform(
            final ServiceRegistry services) {
        final ModuleRegistry modules =
                new ModuleRegistry();
        final Minecraft189Platform platform =
                new Minecraft189Platform();
        platform.attach(
                new PlatformContext(
                        new EventBus(),
                        modules,
                        new ModuleController(modules),
                        services));
        return platform;
    }
}
