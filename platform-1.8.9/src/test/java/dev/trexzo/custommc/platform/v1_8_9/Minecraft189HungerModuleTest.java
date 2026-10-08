package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.event.EventBus;
import dev.trexzo.custommc.core.module.ModuleCategoryRegistry;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.module.ModuleState;
import dev.trexzo.custommc.core.render.RenderPipeline;
import dev.trexzo.custommc.core.service.ServiceRegistry;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.ui.UiFontHandle;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.platform.PlatformContext;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189HungerModuleTest {
    @Test
    void hungerHudReadsLiveValuesAndPersistsPosition() {
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

        final RecordingHost host =
                new RecordingHost();
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

        try {
            final Minecraft189HungerModule hunger =
                    runtime.featureCatalog()
                            .hunger();
            assertFalse(hunger.showMetersSetting().get().booleanValue());
            assertFalse(hunger.lowFoodAlertSetting().get().booleanValue());
            assertEquals(Integer.valueOf(6), hunger.lowFoodThresholdSetting().get());
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189HungerModule.SHOW_METERS_SETTING_ID));
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189HungerModule.LOW_FOOD_ALERT_SETTING_ID));
            assertEquals("6", settings.snapshotEncoded().get(
                    Minecraft189HungerModule.LOW_FOOD_THRESHOLD_SETTING_ID));

            assertEquals(
                    ModuleState.DISABLED,
                    controller.stateOf(
                            Minecraft189HungerModule.ID));

            hunger.xSetting().set(48);
            hunger.ySetting().set(228);
            runtime.playerHunger(
                    new Minecraft189PlayerHungerAccess() {
                        @Override
                        public int customMcFoodLevel() {
                            return 17;
                        }

                        @Override
                        public float customMcSaturationLevel() {
                            return 6.5F;
                        }
                    });

            controller.enable(
                    Minecraft189HungerModule.ID);
            assertTrue(
                    hunger.renderPassInstalled());

            runtime.renderHud(
                    0L,
                    0.0F);

            assertEquals(
                    "Hunger: 17/20 | Sat: 6.5",
                    host.lastText);
            assertEquals(0xFFFFFFFF, host.lastArgb);
            assertTrue(host.bars.isEmpty()); // Default OFF draw-call parity.
            assertEquals(
                    48.0F,
                    host.lastX);
            assertEquals(
                    228.0F,
                    host.lastY);
            assertEquals(
                    "48",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189HungerModule.X_SETTING_ID));
            assertEquals(
                    "228",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189HungerModule.Y_SETTING_ID));

            final Minecraft189PlayerHungerState.Snapshot snapshot =
                    runtime.playerHungerState()
                            .snapshot();
            assertTrue(
                    snapshot.available());
            assertEquals(
                    17,
                    snapshot.foodLevel());
            assertEquals(
                    6.5F,
                    snapshot.saturationLevel());

            // Two tracks plus independent food and saturation fills.
            hunger.showMetersSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189HungerModule.SHOW_METERS_SETTING_ID));
            runtime.renderHud(1L, 0.0F);
            assertEquals(4, host.bars.size());
            assertBar(host.bars.get(0), 48.0F, 240.0F, 100.0F, 3.0F, 0xFF303D4A);
            assertBar(host.bars.get(1), 48.0F, 246.0F, 100.0F, 3.0F, 0xFF303D4A);
            assertBar(host.bars.get(2), 48.0F, 240.0F, 85.0F, 3.0F, 0xFF7EC97A);
            assertBar(host.bars.get(3), 48.0F, 246.0F, 32.5F, 3.0F, 0xFF69BFE8);
            host.bars.clear();

            // Inclusive warning boundary; text and food fill change color.
            hunger.lowFoodAlertSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189HungerModule.LOW_FOOD_ALERT_SETTING_ID));
            runtime.playerHunger(hungerAccess(6, 1.5F));
            runtime.renderHud(2L, 0.0F);
            assertEquals("Hunger: 6/20 | Sat: 1.5", host.lastText);
            assertEquals(0xFFFFB65C, host.lastArgb);
            assertEquals(4, host.bars.size());
            assertBar(host.bars.get(2), 48.0F, 240.0F, 30.0F, 3.0F, 0xFFFFB65C);
            assertBar(host.bars.get(3), 48.0F, 246.0F, 7.5F, 3.0F, 0xFF69BFE8);

            host.bars.clear();
            runtime.playerHunger(hungerAccess(7, 1.5F));
            runtime.renderHud(3L, 0.0F);
            assertEquals(0xFFFFFFFF, host.lastArgb);
            assertBar(host.bars.get(2), 48.0F, 240.0F, 35.0F, 3.0F, 0xFF7EC97A);
            hunger.lowFoodThresholdSetting().set(8);
            assertEquals("8", settings.snapshotEncoded().get(
                    Minecraft189HungerModule.LOW_FOOD_THRESHOLD_SETTING_ID));
            host.bars.clear();
            runtime.renderHud(4L, 0.0F);
            assertEquals(0xFFFFB65C, host.lastArgb);
            assertBar(host.bars.get(2), 48.0F, 240.0F, 35.0F, 3.0F, 0xFFFFB65C);

            hunger.lowFoodAlertSetting().set(Boolean.FALSE);
            host.bars.clear();
            runtime.renderHud(5L, 0.0F);
            assertEquals(0xFFFFFFFF, host.lastArgb);
            assertBar(host.bars.get(2), 48.0F, 240.0F, 35.0F, 3.0F, 0xFF7EC97A);

            // Zero values never generate zero-width fill draw calls.
            runtime.playerHunger(hungerAccess(0, 0.0F));
            host.bars.clear();
            runtime.renderHud(6L, 0.0F);
            assertEquals(2, host.bars.size());
            hunger.showMetersSetting().set(Boolean.FALSE);
            host.bars.clear();
            runtime.renderHud(7L, 0.0F);
            assertTrue(host.bars.isEmpty());
            assertEquals("Hunger: 0/20 | Sat: 0.0", host.lastText);
            assertThrows(IllegalArgumentException.class,
                    () -> hunger.lowFoodThresholdSetting().set(0));
            assertThrows(IllegalArgumentException.class,
                    () -> hunger.lowFoodThresholdSetting().set(21));

            host.lastText = null;
            runtime.playerHunger(null);
            runtime.renderHud(
                    1L,
                    0.0F);
            assertNull(host.lastText);

            controller.disable(
                    Minecraft189HungerModule.ID);
            assertFalse(
                    hunger.renderPassInstalled());
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189HungerModule.ID));
        assertNull(
                settings.find(
                        Minecraft189HungerModule.X_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189HungerModule.Y_SETTING_ID));
        assertNull(settings.find(Minecraft189HungerModule.SHOW_METERS_SETTING_ID));
        assertNull(settings.find(Minecraft189HungerModule.LOW_FOOD_ALERT_SETTING_ID));
        assertNull(settings.find(Minecraft189HungerModule.LOW_FOOD_THRESHOLD_SETTING_ID));
    }

    @Test
    void hungerStateRejectsInvalidValues() {
        final Minecraft189PlayerHungerState state =
                new Minecraft189PlayerHungerState();

        assertThrows(
                IllegalArgumentException.class,
                () -> state.update(
                        -1,
                        1.0F));
        assertThrows(
                IllegalArgumentException.class,
                () -> state.update(
                        21,
                        1.0F));
        assertThrows(
                IllegalArgumentException.class,
                () -> state.update(
                        10,
                        Float.NaN));
        assertThrows(
                IllegalArgumentException.class,
                () -> state.update(
                        10,
                        20.5F));

        state.update(
                20,
                20.0F);
        assertEquals(
                20,
                state.snapshot()
                        .foodLevel());
    }

    @Test
    void meterFractionsClampAndFailClosedForNonfiniteSourceNumbers() {
        assertEquals(0.0F, Minecraft189HungerModule.meterFillWidth(-1.0D), 0.0001F);
        assertEquals(0.0F, Minecraft189HungerModule.meterFillWidth(Double.NaN), 0.0001F);
        assertEquals(0.0F, Minecraft189HungerModule.meterFillWidth(
                Double.POSITIVE_INFINITY), 0.0001F);
        assertEquals(0.0F, Minecraft189HungerModule.meterFillWidth(
                Double.NEGATIVE_INFINITY), 0.0001F);
        assertEquals(0.0F, Minecraft189HungerModule.meterFillWidth(0.0D), 0.0001F);
        assertEquals(25.0F, Minecraft189HungerModule.meterFillWidth(5.0D), 0.0001F);
        assertEquals(100.0F, Minecraft189HungerModule.meterFillWidth(20.0D), 0.0001F);
        assertEquals(100.0F, Minecraft189HungerModule.meterFillWidth(200.0D), 0.0001F);
        final Minecraft189PlayerHungerState state = new Minecraft189PlayerHungerState();
        assertFalse(Minecraft189HungerModule.belowFoodThreshold(
                state.snapshot(), 6));
        state.update(6, 2.0F);
        assertTrue(Minecraft189HungerModule.belowFoodThreshold(
                state.snapshot(), 6));
        assertFalse(Minecraft189HungerModule.belowFoodThreshold(
                state.snapshot(), 5));
    }

    private static Minecraft189PlayerHungerAccess hungerAccess(
            final int food, final float saturation) {
        return new Minecraft189PlayerHungerAccess() {
            @Override
            public int customMcFoodLevel() {
                return food;
            }

            @Override
            public float customMcSaturationLevel() {
                return saturation;
            }
        };
    }

    private static void assertBar(final Bar bar, final float x,
            final float y, final float width, final float height, final int argb) {
        assertEquals(x, bar.x, 0.0001F);
        assertEquals(y, bar.y, 0.0001F);
        assertEquals(width, bar.width, 0.0001F);
        assertEquals(height, bar.height, 0.0001F);
        assertEquals(argb, bar.argb);
    }

    private static final class Bar {
        private final float x;
        private final float y;
        private final float width;
        private final float height;
        private final int argb;

        private Bar(final float x, final float y,
                final float width, final float height, final int argb) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.argb = argb;
        }
    }

    private static final class RecordingHost
            implements LegacyUiHostCallbacks {
        private String lastText;
        private int lastArgb;
        private final java.util.List<Bar> bars = new java.util.ArrayList<Bar>();
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
            bars.add(new Bar(x, y, width, height, argb));
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
            lastArgb = argb;
            lastX = x;
            lastY = y;
        }

        @Override
        public void endUi() {
        }
    }
}
