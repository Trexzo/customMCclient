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

final class Minecraft189CpsModuleTest {
    @Test
    void cpsHudUsesAtomicRollingCountsAndConfiguredPosition() {
        final MutableClock clock =
                new MutableClock();
        final Minecraft189ClickRateTracker tracker =
                new Minecraft189ClickRateTracker(clock);
        final RenderPipeline pipeline =
                new RenderPipeline();
        final RecordingHost host =
                new RecordingHost();
        final Minecraft189CpsModule cps =
                new Minecraft189CpsModule(
                        tracker,
                        pipeline,
                        host);
        final ModuleRegistry modules =
                new ModuleRegistry();
        modules.register(cps);
        final ModuleController controller =
                new ModuleController(modules);

        cps.xSetting().set(52);
        cps.ySetting().set(164);
        controller.enable(
                Minecraft189CpsModule.ID);
        assertTrue(
                cps.renderPassInstalled());

        clock.now = 0L;
        tracker.recordPress(
                Minecraft189ClickRateTracker.LEFT_BUTTON);
        clock.now = 100_000_000L;
        tracker.recordPress(
                Minecraft189ClickRateTracker.LEFT_BUTTON);
        clock.now = 200_000_000L;
        tracker.recordPress(
                Minecraft189ClickRateTracker.RIGHT_BUTTON);

        pipeline.render(
                RenderStage.HUD,
                new RenderFrame(
                        0L,
                        0.0F));

        assertEquals(
                "CPS: L 2 | R 1",
                host.lastText);
        assertEquals(
                52.0F,
                host.lastX);
        assertEquals(
                164.0F,
                host.lastY);

        clock.now = 1_100_000_000L;
        pipeline.render(
                RenderStage.HUD,
                new RenderFrame(
                        1L,
                        0.0F));

        assertEquals(
                "CPS: L 0 | R 1",
                host.lastText);

        controller.disable(
                Minecraft189CpsModule.ID);
        assertFalse(
                cps.renderPassInstalled());
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
