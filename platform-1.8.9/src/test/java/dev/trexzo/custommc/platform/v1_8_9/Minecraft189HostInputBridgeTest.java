package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.event.EventBus;
import dev.trexzo.custommc.core.module.ModuleCategoryRegistry;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.render.RenderPipeline;
import dev.trexzo.custommc.core.service.ServiceRegistry;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.ui.UiFontHandle;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiLayout;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiLayoutEngine;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiModel;
import dev.trexzo.custommc.platform.PlatformContext;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189HostInputBridgeTest {
    @Test
    void sharedHostViewportDrivesPointerAndKeyRouting() {
        final ModuleRegistry modules =
                new ModuleRegistry();
        final ModuleController controller =
                new ModuleController(modules);
        final SettingRegistry settings =
                new SettingRegistry();
        final ServiceRegistry services =
                new ServiceRegistry();
        services.register(
                RenderPipeline.class,
                new RenderPipeline());

        final Minecraft189Platform platform =
                new Minecraft189Platform();
        platform.attach(
                new PlatformContext(
                        new EventBus(),
                        modules,
                        controller,
                        services));

        final MutableHostCallbacks host =
                new MutableHostCallbacks(
                        1200,
                        800,
                        1.0F);

        final Minecraft189ClickGuiRuntime runtime =
                Minecraft189ClickGuiRuntime.install(
                        platform,
                        new ModulePresentationRegistry(),
                        new ModuleCategoryRegistry(),
                        new ModuleSettingRegistry(
                                modules,
                                settings),
                        null,
                        null,
                        settings,
                        new SettingPresentationRegistry(),
                        host);

        final ClickGuiModel model =
                runtime.coreRuntime()
                        .model();
        model.open();

        final Minecraft189HostInputBridge input =
                new Minecraft189HostInputBridge(
                        platform,
                        host);

        assertTrue(
                clickSearch(
                        input,
                        host));

        assertTrue(
                input.key(
                        30,
                        'e',
                        true,
                        false,
                        false,
                        false,
                        false));
        assertEquals(
                "e",
                model.snapshot()
                        .searchQuery());

        host.width = 2000;
        host.height = 1200;
        host.scale = 2.0F;

        assertTrue(
                clickSearch(
                        input,
                        host));

        runtime.close();
    }

    private static boolean clickSearch(
            final Minecraft189HostInputBridge input,
            final MutableHostCallbacks host) {
        final UiViewport viewport =
                new UiViewport(
                        host.width,
                        host.height,
                        host.scale);
        final ClickGuiLayout layout =
                new ClickGuiLayoutEngine()
                        .layout(viewport);

        final int pixelX =
                Math.round(
                        (layout.search().x()
                                + 2.0F)
                                * viewport.scale());
        final int logicalY =
                Math.round(
                        layout.search().y()
                                + 2.0F);
        final int pixelYFromBottom =
                viewport.pixelHeight()
                        - 1
                        - Math.round(
                        logicalY
                                * viewport.scale());

        return input.pointerButton(
                pixelX,
                pixelYFromBottom,
                0,
                true);
    }

    private static final class MutableHostCallbacks
            implements LegacyUiHostCallbacks {
        private int width;
        private int height;
        private float scale;

        MutableHostCallbacks(
                final int width,
                final int height,
                final float scale) {
            this.width = width;
            this.height = height;
            this.scale = scale;
        }

        @Override
        public int framebufferWidth() {
            return width;
        }

        @Override
        public int framebufferHeight() {
            return height;
        }

        @Override
        public float uiScale() {
            return scale;
        }

        @Override
        public void beginUi(
                final UiViewport viewport) {
        }

        @Override
        public void fillRect(
                final float x,
                final float y,
                final float width,
                final float height,
                final int argb) {
        }

        @Override
        public void fillRoundedRect(
                final float x,
                final float y,
                final float width,
                final float height,
                final float radius,
                final int argb) {
        }

        @Override
        public void strokeRect(
                final float x,
                final float y,
                final float width,
                final float height,
                final float thickness,
                final int argb) {
        }

        @Override
        public void pushClip(
                final float x,
                final float y,
                final float width,
                final float height) {
        }

        @Override
        public void popClip() {
        }

        @Override
        public void drawText(
                final UiFontHandle font,
                final float x,
                final float y,
                final String text,
                final int argb) {
        }

        @Override
        public void endUi() {
        }
    }
}
