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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189TimerSpeedModuleTest {
    @Test
    void timerUsesConfiguredMultiplierAndRestoresVanillaWhenDisabled() {
        final ModuleRegistry modules =
                new ModuleRegistry();
        final ModuleController controller =
                new ModuleController(modules);
        final SettingRegistry settings =
                new SettingRegistry();
        final ModuleCategoryRegistry categories =
                new ModuleCategoryRegistry();
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

        final Minecraft189HostRuntime runtime =
                Minecraft189HostRuntime.install(
                        platform,
                        new ModulePresentationRegistry(),
                        categories,
                        new ModuleSettingRegistry(
                                modules,
                                settings),
                        null,
                        null,
                        settings,
                        new SettingPresentationRegistry(),
                        new NoOpHost());

        try {
            assertNotNull(
                    categories.find(
                            Minecraft189FeatureCatalog.PLAYER_CATEGORY_ID));
            assertEquals(
                    ModuleState.DISABLED,
                    controller.stateOf(
                            Minecraft189TimerSpeedModule.ID));

            final TestTimer timer =
                    new TestTimer();
            timer.speed = 1.25F;
            runtime.timerSpeedControl(
                    timer);
            assertEquals(
                    1.0F,
                    timer.speed,
                    0.000001F);

            controller.enable(
                    Minecraft189TimerSpeedModule.ID);
            assertTrue(
                    runtime.featureCatalog()
                            .timerSpeed()
                            .active());

            runtime.featureCatalog()
                    .timerSpeed()
                    .speedPercentSetting()
                    .set(150);
            assertEquals(
                    "150",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189TimerSpeedModule.SPEED_SETTING_ID));

            runtime.timerSpeedControl(
                    timer);
            assertEquals(
                    1.5F,
                    timer.speed,
                    0.000001F);

            runtime.featureCatalog()
                    .timerSpeed()
                    .speedPercentSetting()
                    .set(50);
            runtime.timerSpeedControl(
                    timer);
            assertEquals(
                    0.5F,
                    timer.speed,
                    0.000001F);

            controller.disable(
                    Minecraft189TimerSpeedModule.ID);
            assertFalse(
                    runtime.featureCatalog()
                            .timerSpeed()
                            .active());
            runtime.timerSpeedControl(
                    timer);
            assertEquals(
                    1.0F,
                    timer.speed,
                    0.000001F);

            runtime.timerSpeedControl(
                    null);
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189TimerSpeedModule.ID));
        assertNull(
                settings.find(
                        Minecraft189TimerSpeedModule.SPEED_SETTING_ID));
        assertNull(settings.find(
                Minecraft189TimerSpeedModule.AIRBORNE_OVERRIDE_SETTING_ID));
        assertNull(settings.find(
                Minecraft189TimerSpeedModule.AIRBORNE_SPEED_SETTING_ID));
        assertNull(
                categories.find(
                        Minecraft189FeatureCatalog.PLAYER_CATEGORY_ID));
    }

    @Test
    void airborneOverrideSwitchesOnlyWithKnownAirborneMovementAndRestoresOnDisable() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final ServiceRegistry services = new ServiceRegistry();
        services.register(RenderPipeline.class, new RenderPipeline());
        final Minecraft189Platform platform = new Minecraft189Platform();
        platform.attach(new PlatformContext(new EventBus(), modules,
                controller, services));
        final Minecraft189HostRuntime runtime = Minecraft189HostRuntime.install(
                platform, new ModulePresentationRegistry(),
                new ModuleCategoryRegistry(),
                new ModuleSettingRegistry(modules, settings),
                null, null, settings, new SettingPresentationRegistry(),
                new NoOpHost());
        try {
            final Minecraft189TimerSpeedModule timerModule =
                    runtime.featureCatalog().timerSpeed();
            final TestTimer timer = new TestTimer();
            assertFalse(timerModule.airborneOverrideSetting().get().booleanValue());
            assertEquals(Integer.valueOf(100),
                    timerModule.airborneSpeedPercentSetting().get());
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189TimerSpeedModule.AIRBORNE_OVERRIDE_SETTING_ID));
            assertEquals("100", settings.snapshotEncoded().get(
                    Minecraft189TimerSpeedModule.AIRBORNE_SPEED_SETTING_ID));
            controller.enable(Minecraft189TimerSpeedModule.ID);
            timerModule.speedPercentSetting().set(150);
            timerModule.airborneSpeedPercentSetting().set(65);

            // Default OFF retains configured speed even with airborne authority.
            runtime.playerMovementState().update(false, false, false);
            runtime.timerSpeedControl(timer);
            assertEquals(1.5F, timer.speed, 0.000001F);
            assertEquals(1, timer.writes);

            timerModule.airborneOverrideSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189TimerSpeedModule.AIRBORNE_OVERRIDE_SETTING_ID));
            assertEquals("65", settings.snapshotEncoded().get(
                    Minecraft189TimerSpeedModule.AIRBORNE_SPEED_SETTING_ID));
            runtime.timerSpeedControl(timer);
            assertEquals(0.65F, timer.speed, 0.000001F);
            assertEquals(2, timer.writes);

            // Repeated callbacks must not produce redundant writes.
            runtime.timerSpeedControl(timer);
            assertEquals(2, timer.writes);

            runtime.playerMovementState().update(true, false, false);
            runtime.timerSpeedControl(timer);
            assertEquals(1.5F, timer.speed, 0.000001F);
            runtime.playerMovementState().update(false, false, true);
            runtime.timerSpeedControl(timer);
            assertEquals(0.65F, timer.speed, 0.000001F);

            timerModule.airborneSpeedPercentSetting().set(125);
            runtime.timerSpeedControl(timer);
            assertEquals(1.25F, timer.speed, 0.000001F);
            runtime.playerMovementState().clear();
            runtime.timerSpeedControl(timer);
            assertEquals(1.5F, timer.speed, 0.000001F);

            // A nonfinite current timer must be repaired on next update.
            timer.speed = Float.NaN;
            runtime.timerSpeedControl(timer);
            assertEquals(1.5F, timer.speed, 0.000001F);
            timer.speed = Float.POSITIVE_INFINITY;
            runtime.timerSpeedControl(timer);
            assertEquals(1.5F, timer.speed, 0.000001F);

            timerModule.airborneOverrideSetting().set(Boolean.FALSE);
            runtime.playerMovementState().update(false, true, false);
            runtime.timerSpeedControl(timer);
            assertEquals(1.5F, timer.speed, 0.000001F);
            assertThrows(IllegalArgumentException.class,
                    () -> timerModule.airborneSpeedPercentSetting().set(9));
            assertThrows(IllegalArgumentException.class,
                    () -> timerModule.airborneSpeedPercentSetting().set(301));
            assertThrows(IllegalArgumentException.class,
                    () -> timerModule.airborneSpeedPercentSetting().set(null));

            controller.disable(Minecraft189TimerSpeedModule.ID);
            runtime.timerSpeedControl(timer);
            assertEquals(1.0F, timer.speed, 0.000001F);
            final int writeCount = timer.writes;
            runtime.timerSpeedControl(timer);
            assertEquals(writeCount, timer.writes);
        } finally {
            runtime.close();
        }
        assertNull(settings.find(
                Minecraft189TimerSpeedModule.AIRBORNE_OVERRIDE_SETTING_ID));
        assertNull(settings.find(
                Minecraft189TimerSpeedModule.AIRBORNE_SPEED_SETTING_ID));
        assertNull(modules.find(Minecraft189TimerSpeedModule.ID));
    }

    @Test
    void legacyTimerApplyFallbackNeverAssumesAirborneAuthority() {
        final Minecraft189TimerSpeedModule module = new Minecraft189TimerSpeedModule();
        final Minecraft189PlayerMovementState movement =
                new Minecraft189PlayerMovementState();
        final TestTimer timer = new TestTimer();
        module.speedPercentSetting().set(170);
        module.airborneSpeedPercentSetting().set(55);
        module.airborneOverrideSetting().set(Boolean.TRUE);
        module.onEnable();
        module.apply(timer);
        assertEquals(1.7F, timer.speed, 0.000001F);
        movement.update(false, false, false);
        module.apply(timer, movement.snapshot());
        assertEquals(0.55F, timer.speed, 0.000001F);
        module.apply(timer);
        assertEquals(1.7F, timer.speed, 0.000001F);
        module.onDisable();
        module.apply(timer, movement.snapshot());
        assertEquals(1.0F, timer.speed, 0.000001F);
        module.apply(null, movement.snapshot());
    }

    private static final class TestTimer
            implements Minecraft189TimerSpeedControl {
        private float speed = 1.0F;
        private int writes;

        @Override
        public float customMcTimerSpeed() {
            return speed;
        }

        @Override
        public void customMcSetTimerSpeed(
                final float speed) {
            writes++;
            this.speed = speed;
        }
    }

    private static final class NoOpHost
            implements LegacyUiHostCallbacks {
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
        }

        @Override
        public void endUi() {
        }
    }
}
