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
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        assertFalse(speed.showPeakSetting().get().booleanValue());
        assertFalse(speed.blocksPerTickSetting().get().booleanValue());

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

        // Tick-sampled peak is independent of how frequently HUD renders.
        speed.showPeakSetting().set(Boolean.TRUE);
        tracker.sample(13.0D, 25.0D);
        pipeline.render(RenderStage.HUD, new RenderFrame(2L, 0.0F));
        assertEquals("Speed: 20.00 BPS | Peak: 100.00 BPS", host.lastText);

        speed.blocksPerTickSetting().set(Boolean.TRUE);
        pipeline.render(RenderStage.HUD, new RenderFrame(3L, 0.0F));
        assertEquals("Speed: 1.00 BPT | Peak: 5.00 BPT", host.lastText);
        speed.showPeakSetting().set(Boolean.FALSE);
        pipeline.render(RenderStage.HUD, new RenderFrame(4L, 0.0F));
        assertEquals("Speed: 1.00 BPT", host.lastText);

        // A cleared tracker must discard both live and peak measurements.
        tracker.clear();
        assertFalse(tracker.snapshot().available());
        assertEquals(0.0D, tracker.snapshot().peakBlocksPerSecond());
        host.lastText = null;
        pipeline.render(
                RenderStage.HUD,
                new RenderFrame(
                        5L,
                        0.0F));
        assertNull(host.lastText);

        controller.disable(
                Minecraft189SpeedModule.ID);
        assertFalse(
                speed.renderPassInstalled());
    }

    @Test
    void horizontalTrackerDoesNotCountVerticalOnlyMovement() {
        final Minecraft189MovementSpeedTracker tracker =
                new Minecraft189MovementSpeedTracker();

        tracker.sample(
                12.0D,
                -8.0D);
        tracker.sample(
                12.0D,
                -8.0D);

        final Minecraft189MovementSpeedTracker.Snapshot speed =
                tracker.snapshot();
        assertTrue(
                speed.available());
        assertEquals(
                0.0D,
                speed.blocksPerSecond());
        assertEquals(0.0D, speed.peakBlocksPerSecond());
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

    @Test
    void peakIsCapturedPerSampleNotPerRenderAndSurvivesSpeedChanges() {
        final Minecraft189MovementSpeedTracker tracker =
                new Minecraft189MovementSpeedTracker();
        tracker.sample(0.0D, 0.0D);
        assertFalse(tracker.snapshot().available());
        tracker.sample(0.0D, 2.0D);
        assertEquals(40.0D, tracker.snapshot().peakBlocksPerSecond());
        tracker.sample(0.0D, 2.5D);
        assertEquals(10.0D, tracker.snapshot().blocksPerSecond());
        assertEquals(40.0D, tracker.snapshot().peakBlocksPerSecond());
        tracker.sample(3.0D, 6.5D);
        assertEquals(100.0D, tracker.snapshot().blocksPerSecond());
        assertEquals(100.0D, tracker.snapshot().peakBlocksPerSecond());
        tracker.sample(3.0D, 6.5D);
        assertEquals(0.0D, tracker.snapshot().blocksPerSecond());
        assertEquals(100.0D, tracker.snapshot().peakBlocksPerSecond());
        tracker.clear();
        assertFalse(tracker.snapshot().available());
        assertEquals(0.0D, tracker.snapshot().peakBlocksPerSecond());
        tracker.sample(5.0D, 10.0D);
        assertFalse(tracker.snapshot().available());
        tracker.sample(5.0D, 11.0D);
        assertEquals(20.0D, tracker.snapshot().peakBlocksPerSecond());
    }

    @Test
    void speedSettingsPersistAndReleaseAllRegistrations() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final ModuleSettingRegistry moduleSettings =
                new ModuleSettingRegistry(modules, settings);
        final SettingPresentationRegistry settingPresentations =
                new SettingPresentationRegistry();
        final Minecraft189SpeedFeature feature = Minecraft189SpeedFeature.install(
                modules,
                controller,
                new ModulePresentationRegistry(),
                moduleSettings,
                settings,
                settingPresentations,
                new Minecraft189MovementSpeedTracker(),
                new RenderPipeline(),
                new RecordingHost());
        try {
            assertNotNull(settings.find(Minecraft189SpeedModule.SHOW_PEAK_SETTING_ID));
            assertNotNull(settings.find(
                    Minecraft189SpeedModule.BLOCKS_PER_TICK_SETTING_ID));
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189SpeedModule.SHOW_PEAK_SETTING_ID));
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189SpeedModule.BLOCKS_PER_TICK_SETTING_ID));
            feature.module().showPeakSetting().set(Boolean.TRUE);
            feature.module().blocksPerTickSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189SpeedModule.SHOW_PEAK_SETTING_ID));
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189SpeedModule.BLOCKS_PER_TICK_SETTING_ID));
            controller.enable(Minecraft189SpeedModule.ID);
            assertTrue(feature.module().renderPassInstalled());
        } finally {
            feature.close();
        }
        assertNull(modules.find(Minecraft189SpeedModule.ID));
        assertNull(settings.find(Minecraft189SpeedModule.X_SETTING_ID));
        assertNull(settings.find(Minecraft189SpeedModule.Y_SETTING_ID));
        assertNull(settings.find(Minecraft189SpeedModule.SHOW_PEAK_SETTING_ID));
        assertNull(settings.find(Minecraft189SpeedModule.BLOCKS_PER_TICK_SETTING_ID));
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
