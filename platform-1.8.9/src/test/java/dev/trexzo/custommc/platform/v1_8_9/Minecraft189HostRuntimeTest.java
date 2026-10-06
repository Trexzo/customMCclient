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
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiInputController;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiLayout;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiLayoutEngine;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiModel;
import dev.trexzo.custommc.platform.PlatformContext;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189HostRuntimeTest {
    @Test
    void unifiedHostRuntimeOwnsClickGuiHooksWithoutOwningPlatform() {
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

        final RecordingHostCallbacks host =
                new RecordingHostCallbacks();

        final Minecraft189HostRuntime runtime =
                Minecraft189HostRuntime.install(
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

        assertTrue(platform.attached());
        assertFalse(runtime.closed());
        assertTrue(
                services.contains(
                        ClickGuiModel.class));
        assertTrue(
                services.contains(
                        ClickGuiInputController.class));

        runtime.clickGuiRuntime()
                .coreRuntime()
                .model()
                .open();

        runtime.renderHud(
                0L,
                0.0F);

        assertEquals(
                1,
                host.beginCalls.get());
        assertEquals(
                1,
                host.endCalls.get());
        assertTrue(
                host.textCalls.get() > 0);

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

        assertTrue(
                runtime.pointerButton(
                        pixelX,
                        pixelYFromBottom,
                        0,
                        true));
        assertTrue(
                runtime.key(
                        30,
                        'e',
                        true,
                        false,
                        false,
                        false,
                        false));

        assertEquals(
                "e",
                runtime.clickGuiRuntime()
                        .coreRuntime()
                        .model()
                        .snapshot()
                        .searchQuery());

        runtime.close();

        assertTrue(runtime.closed());
        assertTrue(platform.attached());
        assertFalse(
                services.contains(
                        ClickGuiModel.class));
        assertFalse(
                services.contains(
                        ClickGuiInputController.class));

        assertThrows(
                IllegalStateException.class,
                () -> runtime.renderHud(
                        1L,
                        0.0F));
        assertThrows(
                IllegalStateException.class,
                runtime::clickGuiRuntime);

        runtime.close();
        assertTrue(runtime.closed());
    }

    private static final class RecordingHostCallbacks
            implements LegacyUiHostCallbacks {
        private int width = 1280;
        private int height = 720;
        private float scale = 1.0F;

        private final AtomicInteger beginCalls =
                new AtomicInteger();
        private final AtomicInteger endCalls =
                new AtomicInteger();
        private final AtomicInteger textCalls =
                new AtomicInteger();

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
        public void endUi() {
            endCalls.incrementAndGet();
        }
    }
}
