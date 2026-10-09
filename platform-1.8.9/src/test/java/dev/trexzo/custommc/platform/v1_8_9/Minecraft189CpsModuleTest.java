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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    void cpsBarsVisualizeMeasuredRollingRatesAndLiveScale() {
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
            assertFalse(cps.showBarsSetting().get().booleanValue());
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189CpsModule.SHOW_BARS_SETTING_ID));
            assertEquals("20", settings.snapshotEncoded().get(
                    Minecraft189CpsModule.BAR_SCALE_SETTING_ID));
            controller.enable(Minecraft189CpsModule.ID);
            clock.now = 0L;
            for (int i = 0; i < 5; i++) {
                tracker.recordPress(Minecraft189ClickRateTracker.LEFT_BUTTON);
            }
            for (int i = 0; i < 2; i++) {
                tracker.recordPress(Minecraft189ClickRateTracker.RIGHT_BUTTON);
            }
            pipeline.render(RenderStage.HUD, new RenderFrame(0L, 0.0F));
            assertEquals("CPS: L 5 | R 2", host.lastText);
            assertEquals(0, host.rectWidths.size()); // OFF: legacy text-only.

            cps.showBarsSetting().set(Boolean.TRUE);
            cps.barScaleSetting().set(10);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189CpsModule.SHOW_BARS_SETTING_ID));
            assertEquals("10", settings.snapshotEncoded().get(
                    Minecraft189CpsModule.BAR_SCALE_SETTING_ID));
            host.rectWidths.clear();
            pipeline.render(RenderStage.HUD, new RenderFrame(1L, 0.0F));
            assertEquals(4, host.rectWidths.size()); // Track/fill for L and R.
            assertEquals(84.0F, host.rectWidths.get(0), 0.0001F);
            assertEquals(42.0F, host.rectWidths.get(1), 0.0001F);
            assertEquals(84.0F, host.rectWidths.get(2), 0.0001F);
            assertEquals(16.8F, host.rectWidths.get(3), 0.0001F);
            assertEquals(Integer.valueOf(0xFF70C9E8), host.rectColors.get(1));
            assertEquals(Integer.valueOf(0xFFFFB65C), host.rectColors.get(3));
            assertEquals("R", host.lastText); // Bar row labels are drawn.

            cps.barScaleSetting().set(1);
            host.rectWidths.clear();
            pipeline.render(RenderStage.HUD, new RenderFrame(2L, 0.0F));
            assertEquals(84.0F, host.rectWidths.get(1), 0.0001F);
            assertEquals(84.0F, host.rectWidths.get(3), 0.0001F);
            cps.showBarsSetting().set(Boolean.FALSE);
            host.rectWidths.clear();
            pipeline.render(RenderStage.HUD, new RenderFrame(3L, 0.0F));
            assertEquals(0, host.rectWidths.size());
            assertEquals("CPS: L 5 | R 2", host.lastText);
            assertEquals(0.0F, Minecraft189CpsModule.barWidthFor(-10, 20), 0.0F);
            assertEquals(0.0F, Minecraft189CpsModule.barWidthFor(10, 0), 0.0F);
            assertEquals(84.0F,
                    Minecraft189CpsModule.barWidthFor(Integer.MAX_VALUE, 20),
                    0.0F);
            assertThrows(IllegalArgumentException.class,
                    () -> cps.barScaleSetting().set(0));
            assertThrows(IllegalArgumentException.class,
                    () -> cps.barScaleSetting().set(41));
            controller.disable(Minecraft189CpsModule.ID);
            host.rectWidths.clear();
            pipeline.render(RenderStage.HUD, new RenderFrame(4L, 0.0F));
            assertEquals(0, host.rectWidths.size());
        } finally {
            feature.close();
        }
        assertNull(settings.find(Minecraft189CpsModule.SHOW_BARS_SETTING_ID));
        assertNull(settings.find(Minecraft189CpsModule.BAR_SCALE_SETTING_ID));
        assertNull(modules.find(Minecraft189CpsModule.ID));
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
        private final List<Float> rectWidths = new ArrayList<Float>();
        private final List<Integer> rectColors = new ArrayList<Integer>();

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
            rectWidths.add(width);
            rectColors.add(argb);
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
