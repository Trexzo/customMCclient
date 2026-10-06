package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.event.EventBus;
import dev.trexzo.custommc.core.module.ModuleCategoryRegistry;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.render.RenderFrame;
import dev.trexzo.custommc.core.render.RenderPipeline;
import dev.trexzo.custommc.core.service.ServiceRegistry;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.ui.UiFontHandle;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiInputController;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiLayout;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiLayoutEngine;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiModel;
import dev.trexzo.custommc.platform.PlatformContext;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiGraphics;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiViewportSource;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189ClickGuiRuntimeTest {
    @Test
    void installerWiresAttachedPlatformHooksToCoreClickGuiRuntime() {
        final ModuleRegistry modules =
                new ModuleRegistry();
        final ModuleController controller =
                new ModuleController(modules);
        final SettingRegistry settings =
                new SettingRegistry();
        final ServiceRegistry services =
                new ServiceRegistry();
        final RenderPipeline pipeline =
                new RenderPipeline();
        services.register(
                RenderPipeline.class,
                pipeline);

        final Minecraft189Platform platform =
                new Minecraft189Platform();
        platform.attach(
                new PlatformContext(
                        new EventBus(),
                        modules,
                        controller,
                        services));

        final MutableViewportSource viewportSource =
                new MutableViewportSource(
                        1200,
                        800,
                        1.0F);
        final RecordingGraphics graphics =
                new RecordingGraphics();

        final Minecraft189ClickGuiRuntime runtime =
                Minecraft189ClickGuiRuntime.install(
                        platform,
                        new ModulePresentationRegistry(),
                        new ModuleCategoryRegistry(),
                        new ModuleSettingRegistry(
                                modules,
                                settings),
                        settings,
                        new SettingPresentationRegistry(),
                        viewportSource,
                        graphics);

        assertSame(
                runtime.coreRuntime().model(),
                services.require(
                        ClickGuiModel.class));
        assertSame(
                runtime.coreRuntime().input(),
                services.require(
                        ClickGuiInputController.class));

        final UiViewport initial =
                runtime.viewportProvider()
                        .viewport(
                                new RenderFrame(
                                        0L,
                                        0.0F));
        assertEquals(
                1200,
                initial.pixelWidth());
        assertEquals(
                800,
                initial.pixelHeight());
        assertEquals(
                1.0F,
                initial.scale());

        viewportSource.width = 1600;
        viewportSource.height = 900;
        viewportSource.scale = 2.0F;

        final UiViewport updated =
                runtime.viewportProvider()
                        .viewport(
                                new RenderFrame(
                                        1L,
                                        0.0F));
        assertEquals(
                1600,
                updated.pixelWidth());
        assertEquals(
                900,
                updated.pixelHeight());
        assertEquals(
                2.0F,
                updated.scale());

        runtime.coreRuntime().model().open();
        new Minecraft189Hooks(platform)
                .renderHud(
                        2L,
                        0.5F);

        assertEquals(
                1,
                graphics.beginCalls.get());
        assertEquals(
                1,
                graphics.endCalls.get());
        assertTrue(
                graphics.textCalls.get() > 0);

        final UiViewport inputViewport =
                new UiViewport(
                        1600,
                        900,
                        2.0F);
        final ClickGuiLayout layout =
                new ClickGuiLayoutEngine()
                        .layout(inputViewport);
        final int pixelX =
                Math.round(
                        layout.search().x()
                                * inputViewport.scale()
                                + 2.0F);
        final int logicalY =
                Math.round(
                        layout.search().y()
                                + 2.0F);
        final int pixelYFromBottom =
                inputViewport.pixelHeight()
                        - 1
                        - Math.round(
                        logicalY
                                * inputViewport.scale());

        final Minecraft189InputHooks inputHooks =
                new Minecraft189InputHooks(
                        platform);
        assertTrue(
                inputHooks.pointerButton(
                        inputViewport.pixelWidth(),
                        inputViewport.pixelHeight(),
                        inputViewport.scale(),
                        pixelX,
                        pixelYFromBottom,
                        0,
                        true));
        assertTrue(
                inputHooks.key(
                        30,
                        'e',
                        true,
                        false,
                        false,
                        false,
                        false));
        assertEquals(
                "e",
                runtime.coreRuntime()
                        .model()
                        .snapshot()
                        .searchQuery());

        runtime.close();

        assertTrue(runtime.closed());
        assertFalse(
                services.contains(
                        ClickGuiModel.class));
        assertFalse(
                services.contains(
                        ClickGuiInputController.class));

        new Minecraft189Hooks(platform)
                .renderHud(
                        3L,
                        0.0F);
        assertEquals(
                1,
                graphics.beginCalls.get());

        runtime.close();
        assertTrue(runtime.closed());
    }

    private static final class MutableViewportSource
            implements LegacyUiViewportSource {
        private int width;
        private int height;
        private float scale;

        MutableViewportSource(
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
    }

    private static final class RecordingGraphics
            implements LegacyUiGraphics {
        private final AtomicInteger beginCalls =
                new AtomicInteger();
        private final AtomicInteger endCalls =
                new AtomicInteger();
        private final AtomicInteger textCalls =
                new AtomicInteger();

        @Override
        public void begin(
                final UiViewport viewport) {
            beginCalls.incrementAndGet();
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
            textCalls.incrementAndGet();
        }

        @Override
        public void end() {
            endCalls.incrementAndGet();
        }
    }
}
