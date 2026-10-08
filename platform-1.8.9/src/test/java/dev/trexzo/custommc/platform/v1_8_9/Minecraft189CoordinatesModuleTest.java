package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
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
    void chunkCoordinatesAreOptInAndFloorNegativePositions() {
        final Minecraft189PlayerPositionState state =
                new Minecraft189PlayerPositionState();
        final RenderPipeline pipeline = new RenderPipeline();
        final RecordingHost host = new RecordingHost();
        final Minecraft189CoordinatesModule coordinates =
                new Minecraft189CoordinatesModule(state, pipeline, host);
        final ModuleRegistry modules = new ModuleRegistry();
        modules.register(coordinates);
        final ModuleController controller = new ModuleController(modules);
        controller.enable(Minecraft189CoordinatesModule.ID);

        state.update(-0.1D, 70.0D, -16.01D);
        pipeline.render(RenderStage.HUD, new RenderFrame(0L, 0.0F));
        assertEquals(1, host.texts.size());
        assertEquals("XYZ: -0.1 / 70.0 / -16.0", host.texts.get(0));
        assertFalse(coordinates.showChunkSetting().get().booleanValue());

        coordinates.showChunkSetting().set(Boolean.TRUE);
        host.texts.clear();
        host.textYs.clear();
        pipeline.render(RenderStage.HUD, new RenderFrame(1L, 0.0F));
        assertEquals(2, host.texts.size());
        assertEquals("CHUNK: -1 / -2", host.texts.get(1));
        assertEquals(160.0F, host.textYs.get(1));

        state.update(16.0D, 70.0D, 0.0D);
        host.texts.clear();
        pipeline.render(RenderStage.HUD, new RenderFrame(2L, 0.0F));
        assertEquals("CHUNK: 1 / 0", host.texts.get(1));
        assertEquals(0L, Minecraft189CoordinatesModule.chunkIndex(15.999D));
        assertEquals(1L, Minecraft189CoordinatesModule.chunkIndex(16.0D));
        assertEquals(-1L, Minecraft189CoordinatesModule.chunkIndex(-0.001D));
        assertEquals(-1L, Minecraft189CoordinatesModule.chunkIndex(-16.0D));
        assertEquals(-2L, Minecraft189CoordinatesModule.chunkIndex(-16.001D));

        coordinates.showChunkSetting().set(Boolean.FALSE);
        host.texts.clear();
        pipeline.render(RenderStage.HUD, new RenderFrame(3L, 0.0F));
        assertEquals(1, host.texts.size());

        state.clear();
        host.texts.clear();
        pipeline.render(RenderStage.HUD, new RenderFrame(4L, 0.0F));
        assertTrue(host.texts.isEmpty());
        controller.disable(Minecraft189CoordinatesModule.ID);
    }

    @Test
    void chunkSettingIsPersistedAndUnregisteredOnClose() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final ModulePresentationRegistry presentations =
                new ModulePresentationRegistry();
        final SettingRegistry settings = new SettingRegistry();
        final ModuleSettingRegistry moduleSettings =
                new ModuleSettingRegistry(modules, settings);
        final SettingPresentationRegistry settingPresentations =
                new SettingPresentationRegistry();
        final Minecraft189CoordinatesFeature feature =
                Minecraft189CoordinatesFeature.install(
                        modules, controller, presentations,
                        moduleSettings, settings, settingPresentations,
                        new Minecraft189PlayerPositionState(),
                        new RenderPipeline(), new RecordingHost());
        try {
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189CoordinatesModule.SHOW_CHUNK_SETTING_ID));
            feature.module().showChunkSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189CoordinatesModule.SHOW_CHUNK_SETTING_ID));
        } finally {
            feature.close();
        }
        assertNull(settings.find(
                Minecraft189CoordinatesModule.SHOW_CHUNK_SETTING_ID));
        assertNull(modules.find(Minecraft189CoordinatesModule.ID));
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
        private final java.util.List<String> texts =
                new java.util.ArrayList<String>();
        private final java.util.List<Float> textYs =
                new java.util.ArrayList<Float>();
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
            texts.add(text);
            textYs.add(y);
            lastText = text;
            lastX = x;
            lastY = y;
        }

        @Override
        public void endUi() {
        }
    }
}
