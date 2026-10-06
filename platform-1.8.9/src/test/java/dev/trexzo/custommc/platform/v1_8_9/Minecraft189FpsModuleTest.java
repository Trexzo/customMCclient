package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.render.RenderFrame;
import dev.trexzo.custommc.core.render.RenderPipeline;
import dev.trexzo.custommc.core.render.RenderStage;
import dev.trexzo.custommc.core.ui.UiFontHandle;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;
import org.junit.jupiter.api.Test;

import java.util.function.LongSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189FpsModuleTest {
    @Test
    void fpsHudReadsRollingFrameAuthorityAndConfiguredPosition() {
        final MutableClock clock =
                new MutableClock();
        final Minecraft189FrameRateTracker tracker =
                new Minecraft189FrameRateTracker(clock);
        final RenderPipeline pipeline =
                new RenderPipeline();
        final RecordingHost host =
                new RecordingHost();
        final Minecraft189FpsModule fps =
                new Minecraft189FpsModule(
                        tracker,
                        pipeline,
                        host);
        final ModuleRegistry modules =
                new ModuleRegistry();
        modules.register(fps);
        final ModuleController controller =
                new ModuleController(modules);

        fps.xSetting().set(32);
        fps.ySetting().set(144);
        controller.enable(
                Minecraft189FpsModule.ID);
        assertTrue(
                fps.renderPassInstalled());

        clock.now = 0L;
        tracker.frameStarted();
        clock.now = 200_000_000L;
        tracker.frameStarted();
        clock.now = 400_000_000L;
        tracker.frameStarted();

        pipeline.render(
                RenderStage.HUD,
                new RenderFrame(
                        2L,
                        0.5F));

        assertEquals(
                "FPS: 3",
                host.lastText);
        assertEquals(
                32.0F,
                host.lastX);
        assertEquals(
                144.0F,
                host.lastY);

        clock.now = 1_100_000_000L;
        pipeline.render(
                RenderStage.HUD,
                new RenderFrame(
                        3L,
                        0.0F));

        assertEquals(
                "FPS: 2",
                host.lastText);

        controller.disable(
                Minecraft189FpsModule.ID);
        assertFalse(
                fps.renderPassInstalled());
    }

    private static final class MutableClock
            implements LongSupplier {
        private long now;

        @Override
        public long getAsLong() {
            return now;
        }
    }

    private static final class RecordingHost
            implements LegacyUiHostCallbacks {
        private String lastText;
        private float lastX;
        private float lastY;

        @Override
        public int framebufferWidth() {
            return 1280;
        }

        @Override
        public int framebufferHeight() {
            return 720;
        }

        @Override
        public float uiScale() {
            return 1.0F;
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
            lastText = text;
            lastX = x;
            lastY = y;
        }

        @Override
        public void endUi() {
        }
    }
}
