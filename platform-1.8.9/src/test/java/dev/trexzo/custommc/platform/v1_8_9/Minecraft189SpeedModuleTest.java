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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189SpeedModuleTest {
    @Test
    void speedHudUsesHorizontalTickDistanceAndConfiguredLocation() {
        final Minecraft189MovementSpeedTracker tracker =
                new Minecraft189MovementSpeedTracker();
        final RenderPipeline pipeline =
                new RenderPipeline();
        final RecordingHost host =
                new RecordingHost();
        final Minecraft189SpeedModule speed =
                new Minecraft189SpeedModule(
                        tracker,
                        pipeline,
                        host);
        final ModuleRegistry modules =
                new ModuleRegistry();
        modules.register(speed);
        final ModuleController controller =
                new ModuleController(modules);

        speed.xSetting().set(44);
        speed.ySetting().set(172);
        controller.enable(
                Minecraft189SpeedModule.ID);
        assertTrue(
                speed.renderPassInstalled());

        tracker.sample(
                10.0D,
                20.0D);
        pipeline.render(
                RenderStage.HUD,
                new RenderFrame(
                        0L,
                        0.0F));
        assertNull(host.lastText);

        tracker.sample(
                13.0D,
                24.0D);
        pipeline.render(
                RenderStage.HUD,
                new RenderFrame(
                        1L,
                        0.0F));

        assertEquals(
                "Speed: 100.00 BPS",
                host.lastText);
        assertEquals(
                44.0F,
                host.lastX);
        assertEquals(
                172.0F,
                host.lastY);

        tracker.clear();
        host.lastText = null;
        pipeline.render(
                RenderStage.HUD,
                new RenderFrame(
                        2L,
                        0.0F));
        assertNull(host.lastText);

        controller.disable(
                Minecraft189SpeedModule.ID);
        assertFalse(
                speed.renderPassInstalled());
    }

    @Test
    void trackerRejectsNonFiniteSamples() {
        final Minecraft189MovementSpeedTracker tracker =
                new Minecraft189MovementSpeedTracker();

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> tracker.sample(
                        Double.NaN,
                        0.0D));
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> tracker.sample(
                        0.0D,
                        Double.NEGATIVE_INFINITY));
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
