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

final class Minecraft189CoordinatesModuleTest {
    @Test
    void coordinatesHudReadsLivePositionAndConfiguredLocation() {
        final Minecraft189PlayerPositionState state =
                new Minecraft189PlayerPositionState();
        final RenderPipeline pipeline =
                new RenderPipeline();
        final RecordingHost host =
                new RecordingHost();
        final Minecraft189CoordinatesModule coordinates =
                new Minecraft189CoordinatesModule(
                        state,
                        pipeline,
                        host);
        final ModuleRegistry modules =
                new ModuleRegistry();
        modules.register(coordinates);
        final ModuleController controller =
                new ModuleController(modules);

        coordinates.xSetting().set(36);
        coordinates.ySetting().set(156);
        controller.enable(
                Minecraft189CoordinatesModule.ID);
        assertTrue(
                coordinates.renderPassInstalled());

        pipeline.render(
                RenderStage.HUD,
                new RenderFrame(
                        0L,
                        0.0F));
        assertNull(host.lastText);

        state.update(
                123.25D,
                64.5D,
                -42.75D);
        pipeline.render(
                RenderStage.HUD,
                new RenderFrame(
                        1L,
                        0.5F));

        assertEquals(
                "XYZ: 123.3 / 64.5 / -42.8",
                host.lastText);
        assertEquals(
                36.0F,
                host.lastX);
        assertEquals(
                156.0F,
                host.lastY);

        host.lastText = null;
        state.clear();
        pipeline.render(
                RenderStage.HUD,
                new RenderFrame(
                        2L,
                        0.0F));
        assertNull(host.lastText);

        controller.disable(
                Minecraft189CoordinatesModule.ID);
        assertFalse(
                coordinates.renderPassInstalled());
    }

    @Test
    void positionStateRejectsNonFiniteCoordinates() {
        final Minecraft189PlayerPositionState state =
                new Minecraft189PlayerPositionState();

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> state.update(
                        Double.NaN,
                        0.0D,
                        0.0D));
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> state.update(
                        0.0D,
                        Double.POSITIVE_INFINITY,
                        0.0D));
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
