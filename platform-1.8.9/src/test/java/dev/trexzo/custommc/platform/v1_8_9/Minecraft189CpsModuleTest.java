package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.render.RenderFrame;
import dev.trexzo.custommc.core.render.RenderPipeline;
import dev.trexzo.custommc.core.render.RenderStage;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.ui.UiFontHandle;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;
import org.junit.jupiter.api.Test;

import java.util.function.LongSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
        assertFalse(cps.compactSetting().get().booleanValue());
        assertFalse(cps.showTotalSetting().get().booleanValue());

        cps.showTotalSetting().set(Boolean.TRUE);
        pipeline.render(RenderStage.HUD, new RenderFrame(0L, 0.0F));
        assertEquals("CPS: L 2 | R 1 | T 3", host.lastText);
        cps.compactSetting().set(Boolean.TRUE);
        pipeline.render(RenderStage.HUD, new RenderFrame(0L, 0.0F));
        assertEquals("CPS: L2 R1 T3", host.lastText);
        cps.showTotalSetting().set(Boolean.FALSE);
        pipeline.render(RenderStage.HUD, new RenderFrame(0L, 0.0F));
        assertEquals("CPS: L2 R1", host.lastText);
        cps.compactSetting().set(Boolean.FALSE);
        pipeline.render(RenderStage.HUD, new RenderFrame(0L, 0.0F));
        assertEquals("CPS: L 2 | R 1", host.lastText);
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

    @Test
    void cpsDisplaySettingsPersistAndUnregisterWithoutChangingCounts() {
        final MutableClock clock = new MutableClock();
        final Minecraft189ClickRateTracker tracker =
                new Minecraft189ClickRateTracker(clock);
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final RenderPipeline pipeline = new RenderPipeline();
        final RecordingHost host = new RecordingHost();
        final Minecraft189CpsFeature feature = Minecraft189CpsFeature.install(
                modules, controller, new ModulePresentationRegistry(),
                new ModuleSettingRegistry(modules, settings), settings,
                new SettingPresentationRegistry(), tracker, pipeline, host);
        try {
            final Minecraft189CpsModule cps = feature.module();
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189CpsModule.COMPACT_SETTING_ID));
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189CpsModule.SHOW_TOTAL_SETTING_ID));
            cps.compactSetting().set(Boolean.TRUE);
            cps.showTotalSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189CpsModule.COMPACT_SETTING_ID));
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189CpsModule.SHOW_TOTAL_SETTING_ID));
            controller.enable(Minecraft189CpsModule.ID);
            clock.now = 0L;
            tracker.recordPress(Minecraft189ClickRateTracker.LEFT_BUTTON);
            tracker.recordPress(Minecraft189ClickRateTracker.RIGHT_BUTTON);
            pipeline.render(RenderStage.HUD, new RenderFrame(0L, 0.0F));
            assertEquals("CPS: L1 R1 T2", host.lastText);
            cps.compactSetting().set(Boolean.FALSE);
            pipeline.render(RenderStage.HUD, new RenderFrame(0L, 0.0F));
            assertEquals("CPS: L 1 | R 1 | T 2", host.lastText);
        } finally {
            feature.close();
        }
        assertNull(settings.find(Minecraft189CpsModule.COMPACT_SETTING_ID));
        assertNull(settings.find(Minecraft189CpsModule.SHOW_TOTAL_SETTING_ID));
        assertNull(modules.find(Minecraft189CpsModule.ID));
    }

    @Test
    void cpsTotalUsesLongAdditionForLargeSyntheticCounters() {
        assertEquals("CPS: L2147483647 R2147483647 T4294967294",
                Minecraft189CpsModule.textFor(
                        Integer.MAX_VALUE, Integer.MAX_VALUE, true, true));
        assertEquals("CPS: L 2 | R 1",
                Minecraft189CpsModule.textFor(2, 1, false, false));
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
