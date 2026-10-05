package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.event.EventBus;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.service.ServiceRegistry;
import dev.trexzo.custommc.core.ui.UiFocusManager;
import dev.trexzo.custommc.core.ui.UiScrollEvent;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiInputController;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiLayout;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiLayoutEngine;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiMetrics;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiModel;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiPage;
import dev.trexzo.custommc.platform.PlatformContext;
import dev.trexzo.custommc.platform.v1_8_9.input.LegacyInputTranslator;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LegacyScrollInputTest {
    @Test
    void legacyWheelUsesTopLeftLogicalCoordinatesAndStableDirection() {
        final LegacyInputTranslator translator =
                new LegacyInputTranslator();
        final UiViewport viewport =
                new UiViewport(
                        800,
                        600,
                        2.0F);

        final Optional<UiScrollEvent> down =
                translator.scrollEvent(
                        viewport,
                        200,
                        99,
                        -120);
        final Optional<UiScrollEvent> up =
                translator.scrollEvent(
                        viewport,
                        200,
                        99,
                        120);

        assertTrue(down.isPresent());
        assertEquals(100.0F, down.get().x());
        assertEquals(250.0F, down.get().y());
        assertEquals(-1.0F, down.get().deltaY());
        assertEquals(1.0F, up.get().deltaY());
        assertFalse(
                translator.scrollEvent(
                        viewport,
                        0,
                        0,
                        0)
                        .isPresent());
    }

    @Test
    void inputHookRoutesWheelToRetainedNavigationScroll() {
        final ClickGuiModel model =
                new ClickGuiModel();
        for (int i = 0; i < 20; i++) {
            model.register(
                    new ClickGuiPage(
                            "page-" + i,
                            "Page " + i,
                            i));
        }
        model.open();

        final ClickGuiInputController input =
                new ClickGuiInputController(
                        model,
                        new UiFocusManager());

        try {
            final ServiceRegistry services =
                    new ServiceRegistry();
            services.register(
                    ClickGuiInputController.class,
                    input);

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

            final Minecraft189InputHooks hooks =
                    new Minecraft189InputHooks(platform);
            final UiViewport viewport =
                    new UiViewport(
                            1200,
                            800,
                            1.0F);
            final ClickGuiLayout layout =
                    new ClickGuiLayoutEngine()
                            .layout(viewport);

            final int pixelX =
                    Math.round(
                            layout.navigation().x()
                                    + 4.0F);
            final int logicalY =
                    Math.round(
                            layout.navigation().y()
                                    + 4.0F);
            final int pixelYFromBottom =
                    viewport.pixelHeight()
                            - 1
                            - logicalY;

            assertTrue(
                    hooks.scroll(
                            viewport.pixelWidth(),
                            viewport.pixelHeight(),
                            viewport.scale(),
                            pixelX,
                            pixelYFromBottom,
                            -120));
            assertEquals(
                    ClickGuiMetrics.NAVIGATION_SCROLL_STEP,
                    model.snapshot().navigationScroll());

            assertFalse(
                    hooks.scroll(
                            viewport.pixelWidth(),
                            viewport.pixelHeight(),
                            viewport.scale(),
                            pixelX,
                            pixelYFromBottom,
                            0));
            assertEquals(
                    ClickGuiMetrics.NAVIGATION_SCROLL_STEP,
                    model.snapshot().navigationScroll());
        } finally {
            input.close();
        }
    }
}
