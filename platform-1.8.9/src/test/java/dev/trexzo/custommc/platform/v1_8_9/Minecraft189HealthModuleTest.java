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

final class Minecraft189HealthModuleTest {
    @Test
    void healthHudReadsLiveHealthAndPersistsPosition() {
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
            final Minecraft189HealthModule health =
                    runtime.featureCatalog()
                            .health();

            assertEquals(
                    ModuleState.DISABLED,
                    controller.stateOf(
                            Minecraft189HealthModule.ID));

            health.xSetting().set(32);
            health.ySetting().set(196);
            runtime.playerHealth(
                    new Minecraft189PlayerHealthAccess() {
                        @Override
                        public float customMcHealth() {
                            return 17.5F;
                        }

                        @Override
                        public float customMcMaxHealth() {
                            return 20.0F;
                        }
                    });

            controller.enable(
                    Minecraft189HealthModule.ID);
            assertTrue(
                    health.renderPassInstalled());

            runtime.renderHud(
                    0L,
                    0.0F);

            assertEquals(
                    "Health: 17.5 / 20.0",
                    host.lastText);
            assertEquals(0xFFFFFFFF, host.lastArgb);
            assertFalse(health.showPercentSetting().get().booleanValue());
            assertFalse(health.lowHealthAlertSetting().get().booleanValue());
            assertEquals(Integer.valueOf(30), health.lowHealthThresholdSetting().get());
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189HealthModule.SHOW_PERCENT_SETTING_ID));
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189HealthModule.LOW_HEALTH_ALERT_SETTING_ID));
            assertEquals("30", settings.snapshotEncoded().get(
                    Minecraft189HealthModule.LOW_HEALTH_THRESHOLD_SETTING_ID));
            assertEquals(
                    32.0F,
                    host.lastX);
            assertEquals(
                    196.0F,
                    host.lastY);
            assertEquals(
                    "32",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189HealthModule.X_SETTING_ID));
            assertEquals(
                    "196",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189HealthModule.Y_SETTING_ID));

            // The percent display never alters existing position or color.
            health.showPercentSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189HealthModule.SHOW_PERCENT_SETTING_ID));
            runtime.renderHud(1L, 0.0F);
            assertEquals("Health: 17.5 / 20.0 (88%)", host.lastText);
            assertEquals(0xFFFFFFFF, host.lastArgb);

            // Warning threshold is inclusive and uses the unrounded fraction:
            // 6/20 is exactly 30%; 6.01/20 is above 30% despite 30% display.
            health.lowHealthAlertSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189HealthModule.LOW_HEALTH_ALERT_SETTING_ID));
            runtime.playerHealth(healthAccess(6.0F, 20.0F));
            runtime.renderHud(2L, 0.0F);
            assertEquals("Health: 6.0 / 20.0 (30%)", host.lastText);
            assertEquals(0xFFFF6969, host.lastArgb);
            runtime.playerHealth(healthAccess(6.01F, 20.0F));
            runtime.renderHud(3L, 0.0F);
            assertEquals(0xFFFFFFFF, host.lastArgb);
            health.lowHealthThresholdSetting().set(31);
            assertEquals("31", settings.snapshotEncoded().get(
                    Minecraft189HealthModule.LOW_HEALTH_THRESHOLD_SETTING_ID));
            runtime.renderHud(4L, 0.0F);
            assertEquals(0xFFFF6969, host.lastArgb);

            // Max-health changes must recompute percentage and warning.
            runtime.playerHealth(healthAccess(6.0F, 40.0F));
            runtime.renderHud(5L, 0.0F);
            assertEquals("Health: 6.0 / 40.0 (15%)", host.lastText);
            assertEquals(0xFFFF6969, host.lastArgb);
            health.lowHealthAlertSetting().set(Boolean.FALSE);
            runtime.renderHud(6L, 0.0F);
            assertEquals(0xFFFFFFFF, host.lastArgb);
            health.showPercentSetting().set(Boolean.FALSE);
            runtime.renderHud(7L, 0.0F);
            assertEquals("Health: 6.0 / 40.0", host.lastText);

            assertThrows(IllegalArgumentException.class,
                    () -> health.lowHealthThresholdSetting().set(0));
            assertThrows(IllegalArgumentException.class,
                    () -> health.lowHealthThresholdSetting().set(101));

            host.lastText = null;
            runtime.playerHealth(null);
            runtime.renderHud(
                    8L,
                    0.0F);
            assertNull(host.lastText);

            controller.disable(
                    Minecraft189HealthModule.ID);
            assertFalse(
                    health.renderPassInstalled());
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189HealthModule.ID));
        assertNull(
                settings.find(
                        Minecraft189HealthModule.X_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189HealthModule.Y_SETTING_ID));
        assertNull(settings.find(Minecraft189HealthModule.SHOW_PERCENT_SETTING_ID));
        assertNull(settings.find(Minecraft189HealthModule.LOW_HEALTH_ALERT_SETTING_ID));
        assertNull(settings.find(Minecraft189HealthModule.LOW_HEALTH_THRESHOLD_SETTING_ID));
    }

    @Test
    void healthStateRejectsInvalidValues() {
        final Minecraft189PlayerHealthState state =
                new Minecraft189PlayerHealthState();

        assertThrows(
                IllegalArgumentException.class,
                () -> state.update(
                        Float.NaN,
                        20.0F));
        assertThrows(
                IllegalArgumentException.class,
                () -> state.update(
                        10.0F,
                        Float.POSITIVE_INFINITY));
        assertThrows(
                IllegalArgumentException.class,
                () -> state.update(
                        10.0F,
                        0.0F));
    }

    @Test
    void healthBarDisplaysClampedLiveFractionAndUsesLowHealthWarningColor() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final ServiceRegistry services = new ServiceRegistry();
        services.register(RenderPipeline.class, new RenderPipeline());
        final Minecraft189Platform platform = new Minecraft189Platform();
        platform.attach(new PlatformContext(new EventBus(), modules,
                controller, services));
        final RecordingHost host = new RecordingHost();
        final Minecraft189HostRuntime runtime = Minecraft189HostRuntime.install(
                platform, new ModulePresentationRegistry(),
                new ModuleCategoryRegistry(),
                new ModuleSettingRegistry(modules, settings),
                null, null, settings, new SettingPresentationRegistry(), host);
        try {
            final Minecraft189HealthModule hud = runtime.featureCatalog().health();
            assertFalse(hud.showBarSetting().get().booleanValue());
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189HealthModule.SHOW_BAR_SETTING_ID));
            hud.xSetting().set(40);
            hud.ySetting().set(210);
            controller.enable(Minecraft189HealthModule.ID);
            runtime.playerHealth(healthAccess(15.0F, 20.0F));
            runtime.renderHud(0L, 0.0F);
            assertEquals("Health: 15.0 / 20.0", host.lastText);
            assertEquals(0, host.healthBarTracks);
            assertEquals(0, host.healthBarFills);

            hud.showBarSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189HealthModule.SHOW_BAR_SETTING_ID));
            runtime.renderHud(1L, 0.0F);
            assertEquals(1, host.healthBarTracks);
            assertEquals(1, host.healthBarFills);
            assertEquals(90.0F, host.lastBarFillWidth, 0.00001F);
            assertEquals(222.0F, host.lastBarY, 0.00001F);
            assertEquals(0xFF70C9E8, host.lastBarColor);
            assertEquals("Health: 15.0 / 20.0", host.lastText);
            assertEquals(40.0F, host.lastX, 0.00001F);

            hud.lowHealthAlertSetting().set(Boolean.TRUE);
            runtime.playerHealth(healthAccess(6.0F, 20.0F));
            runtime.renderHud(2L, 0.0F);
            assertEquals(36.0F, host.lastBarFillWidth, 0.00001F);
            assertEquals(0xFFFF6969, host.lastBarColor);
            assertEquals(0xFFFF6969, host.lastArgb);
            runtime.playerHealth(healthAccess(6.01F, 20.0F));
            runtime.renderHud(3L, 0.0F);
            assertEquals(0xFF70C9E8, host.lastBarColor);
            assertEquals(0xFFFFFFFF, host.lastArgb);

            runtime.playerHealth(healthAccess(30.0F, 20.0F));
            runtime.renderHud(4L, 0.0F);
            assertEquals(120.0F, host.lastBarFillWidth, 0.00001F);
            runtime.playerHealth(healthAccess(0.0F, 20.0F));
            final int fillsBeforeZero = host.healthBarFills;
            runtime.renderHud(5L, 0.0F);
            assertEquals(fillsBeforeZero, host.healthBarFills);
            assertEquals(5, host.healthBarTracks);
            runtime.playerHealth(healthAccess(-5.0F, 20.0F));
            runtime.renderHud(6L, 0.0F);
            assertEquals(fillsBeforeZero, host.healthBarFills);
            assertEquals(6, host.healthBarTracks);

            hud.showBarSetting().set(Boolean.FALSE);
            runtime.playerHealth(healthAccess(10.0F, 20.0F));
            runtime.renderHud(7L, 0.0F);
            assertEquals(6, host.healthBarTracks);
            assertEquals("Health: 10.0 / 20.0", host.lastText);
            runtime.playerHealth(null);
            runtime.renderHud(8L, 0.0F);
            assertEquals(6, host.healthBarTracks);
            controller.disable(Minecraft189HealthModule.ID);
            runtime.playerHealth(healthAccess(10.0F, 20.0F));
            runtime.renderHud(9L, 0.0F);
            assertEquals(6, host.healthBarTracks);
        } finally {
            runtime.close();
        }
        assertNull(settings.find(Minecraft189HealthModule.SHOW_BAR_SETTING_ID));
        assertNull(modules.find(Minecraft189HealthModule.ID));
    }

    @Test
    void healthBarWidthAdjustsTrackAndFillWithLivePersistence() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final ServiceRegistry services = new ServiceRegistry();
        services.register(RenderPipeline.class, new RenderPipeline());
        final Minecraft189Platform platform = new Minecraft189Platform();
        platform.attach(new PlatformContext(new EventBus(), modules,
                controller, services));
        final RecordingHost host = new RecordingHost();
        final Minecraft189HostRuntime runtime = Minecraft189HostRuntime.install(
                platform, new ModulePresentationRegistry(),
                new ModuleCategoryRegistry(),
                new ModuleSettingRegistry(modules, settings),
                null, null, settings, new SettingPresentationRegistry(),
                host);
        try {
            final Minecraft189HealthModule hud = runtime.featureCatalog().health();
            assertEquals(Integer.valueOf(120), hud.barWidthSetting().get());
            assertEquals("120", settings.snapshotEncoded().get(
                    Minecraft189HealthModule.BAR_WIDTH_SETTING_ID));
            controller.enable(Minecraft189HealthModule.ID);
            runtime.playerHealth(healthAccess(15.0F, 20.0F));
            runtime.renderHud(0L, 0.0F);
            assertEquals(0, host.healthBarTracks); // Default text-only parity.

            hud.showBarSetting().set(Boolean.TRUE);
            runtime.renderHud(1L, 0.0F);
            assertEquals(120.0F, host.lastBarTrackWidth, 0.00001F);
            assertEquals(90.0F, host.lastBarFillWidth, 0.00001F);

            hud.barWidthSetting().set(200);
            assertEquals("200", settings.snapshotEncoded().get(
                    Minecraft189HealthModule.BAR_WIDTH_SETTING_ID));
            runtime.renderHud(2L, 0.0F);
            assertEquals(200.0F, host.lastBarTrackWidth, 0.00001F);
            assertEquals(150.0F, host.lastBarFillWidth, 0.00001F);
            assertEquals("Health: 15.0 / 20.0", host.lastText);

            hud.lowHealthAlertSetting().set(Boolean.TRUE);
            runtime.playerHealth(healthAccess(4.0F, 20.0F));
            runtime.renderHud(3L, 0.0F);
            assertEquals(200.0F, host.lastBarTrackWidth, 0.00001F);
            assertEquals(40.0F, host.lastBarFillWidth, 0.00001F);
            assertEquals(0xFFFF6969, host.lastBarColor);

            hud.barWidthSetting().set(40);
            runtime.playerHealth(healthAccess(30.0F, 20.0F));
            runtime.renderHud(4L, 0.0F);
            assertEquals(40.0F, host.lastBarTrackWidth, 0.00001F);
            assertEquals(40.0F, host.lastBarFillWidth, 0.00001F);
            hud.barWidthSetting().set(240);
            runtime.playerHealth(healthAccess(10.0F, 20.0F));
            runtime.renderHud(5L, 0.0F);
            assertEquals(240.0F, host.lastBarTrackWidth, 0.00001F);
            assertEquals(120.0F, host.lastBarFillWidth, 0.00001F);
            assertThrows(IllegalArgumentException.class,
                    () -> hud.barWidthSetting().set(39));
            assertThrows(IllegalArgumentException.class,
                    () -> hud.barWidthSetting().set(241));
            hud.showBarSetting().set(Boolean.FALSE);
            final int tracks = host.healthBarTracks;
            runtime.renderHud(6L, 0.0F);
            assertEquals(tracks, host.healthBarTracks);
            controller.disable(Minecraft189HealthModule.ID);
            runtime.renderHud(7L, 0.0F);
            assertEquals(tracks, host.healthBarTracks);
        } finally {
            runtime.close();
        }
        assertNull(settings.find(Minecraft189HealthModule.BAR_WIDTH_SETTING_ID));
        assertNull(modules.find(Minecraft189HealthModule.ID));
    }

    private static Minecraft189PlayerHealthAccess healthAccess(
            final float health, final float maxHealth) {
        return new Minecraft189PlayerHealthAccess() {
            @Override
            public float customMcHealth() {
                return health;
            }

            @Override
            public float customMcMaxHealth() {
                return maxHealth;
            }
        };
    }

    private static final class RecordingHost
            implements LegacyUiHostCallbacks {
        private String lastText;
        private int lastArgb;
        private float lastX;
        private float lastY;
        private int healthBarTracks;
        private float lastBarTrackWidth;
        private int healthBarFills;
        private float lastBarFillWidth;
        private float lastBarY;
        private int lastBarColor;

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
            if (argb == 0xBB26303A) {
                healthBarTracks++;
                lastBarTrackWidth = width;
            }
            if (argb == 0xFF70C9E8 || argb == 0xFFFF6969) {
                healthBarFills++;
                lastBarFillWidth = width;
                lastBarY = y;
                lastBarColor = argb;
            }
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
