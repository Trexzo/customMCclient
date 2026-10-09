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
import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class Minecraft189NoGravityModuleTest {
    @Test
    void noGravityCancelsOnlyAirborneDescentAndOwnsFastFallAndGlide() {
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
                            Minecraft189FeatureCatalog.MOVEMENT_CATEGORY_ID));
            assertEquals(
                    ModuleState.DISABLED,
                    controller.stateOf(
                            Minecraft189NoGravityModule.ID));

            final TestPlayer player =
                    new TestPlayer();
            player.motionY = -0.30D;

            controller.enable(
                    Minecraft189NoGravityModule.ID);
            assertTrue(
                    runtime.featureCatalog()
                            .noGravity()
                            .active());

            runtime.playerMotionControl(
                    player);
            assertEquals(
                    -0.30D,
                    player.motionY,
                    0.000000001D);

            runtime.playerMovementState()
                    .update(
                            true,
                            false,
                            false);
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    -0.30D,
                    player.motionY,
                    0.000000001D);

            runtime.playerMovementState()
                    .update(
                            false,
                            false,
                            false);
            player.motionY = 0.20D;
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    0.20D,
                    player.motionY,
                    0.000000001D);

            player.motionY = -0.20D;
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    0.0D,
                    player.motionY,
                    0.000000001D);

            controller.enable(
                    Minecraft189FastFallModule.ID);
            controller.enable(
                    Minecraft189GlideModule.ID);
            player.motionY = -0.10D;
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    0.0D,
                    player.motionY,
                    0.000000001D);

            controller.enable(
                    Minecraft189FlightModule.ID);
            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.SPACE,
                            true);
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    Minecraft189FlightModule.ASCEND_MOTION_Y,
                    player.motionY,
                    0.000000001D);
            controller.disable(
                    Minecraft189FlightModule.ID);
            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.SPACE,
                            false);

            controller.disable(
                    Minecraft189NoGravityModule.ID);
            assertFalse(
                    runtime.featureCatalog()
                            .noGravity()
                            .active());
            player.motionY = -0.05D;
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    -Minecraft189FastFallModule.DEFAULT_FALL_SPEED,
                    player.motionY,
                    0.000000001D);

            controller.disable(
                    Minecraft189FastFallModule.ID);
            controller.disable(
                    Minecraft189GlideModule.ID);
            runtime.playerMotionControl(
                    null);
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189NoGravityModule.ID));
        assertNull(
                categories.find(
                        Minecraft189FeatureCatalog.MOVEMENT_CATEGORY_ID));
    }

    @Test
    void noGravityLiftSpeedRetainsOriginalZeroGravityAndRespectsPriority() {
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
            final Minecraft189NoGravityModule gravity =
                    runtime.featureCatalog().noGravity();
            final TestPlayer player = new TestPlayer();
            assertEquals(0.0D, gravity.liftSpeedSetting().get(), 0.000000001D);
            assertEquals("0.0", settings.snapshotEncoded().get(
                    Minecraft189NoGravityModule.LIFT_SPEED_SETTING_ID));
            controller.enable(Minecraft189NoGravityModule.ID);
            runtime.playerMovementState().update(false, false, false);
            player.motionY = -0.20D;
            runtime.playerMotionControl(player);
            assertEquals(0.0D, player.motionY, 0.000000001D);

            gravity.liftSpeedSetting().set(0.12D);
            assertEquals("0.12", settings.snapshotEncoded().get(
                    Minecraft189NoGravityModule.LIFT_SPEED_SETTING_ID));
            player.motionY = -0.20D;
            runtime.playerMotionControl(player);
            assertEquals(0.12D, player.motionY, 0.000000001D);
            player.motionY = 0.05D;
            runtime.playerMotionControl(player);
            assertEquals(0.12D, player.motionY, 0.000000001D);
            player.motionY = 0.25D;
            runtime.playerMotionControl(player);
            assertEquals(0.25D, player.motionY, 0.000000001D);

            // Ground or missing mapped authority always suspends lift.
            runtime.playerMovementState().update(true, false, false);
            player.motionY = -0.20D;
            runtime.playerMotionControl(player);
            assertEquals(-0.20D, player.motionY, 0.000000001D);
            runtime.playerMovementState().clear();
            runtime.playerMotionControl(player);
            assertEquals(-0.20D, player.motionY, 0.000000001D);

            runtime.playerMovementState().update(false, false, false);
            controller.enable(Minecraft189FlightModule.ID);
            runtime.playerMotionControl(player);
            assertEquals(Minecraft189FlightModule.HOVER_MOTION_Y,
                    player.motionY, 0.000000001D);
            controller.disable(Minecraft189FlightModule.ID);
            controller.enable(Minecraft189FastFallModule.ID);
            player.motionY = -0.05D;
            runtime.playerMotionControl(player);
            assertEquals(0.12D, player.motionY, 0.000000001D);

            // Runtime edits restore the exact default hovering cap.
            gravity.liftSpeedSetting().set(0.0D);
            player.motionY = -0.18D;
            runtime.playerMotionControl(player);
            assertEquals(0.0D, player.motionY, 0.000000001D);
            controller.disable(Minecraft189NoGravityModule.ID);
            player.motionY = -0.05D;
            runtime.playerMotionControl(player);
            assertEquals(-Minecraft189FastFallModule.DEFAULT_FALL_SPEED,
                    player.motionY, 0.000000001D);
            assertThrows(IllegalArgumentException.class,
                    () -> gravity.liftSpeedSetting().set(-0.01D));
            assertThrows(IllegalArgumentException.class,
                    () -> gravity.liftSpeedSetting().set(0.31D));
            assertThrows(IllegalArgumentException.class,
                    () -> gravity.liftSpeedSetting().set(Double.NaN));
            assertThrows(IllegalArgumentException.class,
                    () -> gravity.liftSpeedSetting().set(Double.POSITIVE_INFINITY));
        } finally {
            runtime.close();
        }
        assertNull(settings.find(Minecraft189NoGravityModule.LIFT_SPEED_SETTING_ID));
        assertNull(modules.find(Minecraft189NoGravityModule.ID));
    }

    @Test
    void optionalNoGravitySmoothLiftConvergesWithoutAccumulatedSuspensionCredit() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final ServiceRegistry services = new ServiceRegistry();
        services.register(RenderPipeline.class, new RenderPipeline());
        final Minecraft189Platform platform = new Minecraft189Platform();
        platform.attach(new PlatformContext(new EventBus(),
                modules, controller, services));
        final Minecraft189HostRuntime runtime = Minecraft189HostRuntime.install(
                platform, new ModulePresentationRegistry(),
                new ModuleCategoryRegistry(),
                new ModuleSettingRegistry(modules, settings),
                null, null, settings, new SettingPresentationRegistry(),
                new NoOpHost());
        try {
            final Minecraft189NoGravityModule gravity =
                    runtime.featureCatalog().noGravity();
            final TestPlayer player = new TestPlayer();
            assertFalse(gravity.smoothLiftSetting().get().booleanValue());
            assertEquals(0.10D, gravity.liftStepSetting().get().doubleValue(),
                    0.000000001D);
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189NoGravityModule.SMOOTH_LIFT_SETTING_ID));
            assertEquals("0.1", settings.snapshotEncoded().get(
                    Minecraft189NoGravityModule.LIFT_STEP_SETTING_ID));
            controller.enable(Minecraft189NoGravityModule.ID);
            gravity.liftSpeedSetting().set(0.20D);
            runtime.playerMovementState().update(false, false, false);
            player.motionY = -0.30D;
            runtime.playerMotionControl(player);
            assertEquals(0.20D, player.motionY, 0.000000001D); // Original snap.

            gravity.smoothLiftSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189NoGravityModule.SMOOTH_LIFT_SETTING_ID));
            player.motionY = -0.30D;
            for (int i = 1; i <= 5; i++) {
                runtime.playerMotionControl(player);
                assertEquals(-0.30D + 0.10D * i, player.motionY, 0.000000001D);
            }
            runtime.playerMotionControl(player);
            assertEquals(0.20D, player.motionY, 0.000000001D);

            // Live step changes are immediate, with real-motion source.
            gravity.liftStepSetting().set(0.05D);
            assertEquals("0.05", settings.snapshotEncoded().get(
                    Minecraft189NoGravityModule.LIFT_STEP_SETTING_ID));
            player.motionY = -0.20D;
            runtime.playerMotionControl(player);
            assertEquals(-0.15D, player.motionY, 0.000000001D);
            runtime.playerMovementState().update(true, false, false);
            runtime.playerMotionControl(player);
            assertEquals(-0.15D, player.motionY, 0.000000001D);
            runtime.playerMovementState().clear();
            runtime.playerMotionControl(player);
            assertEquals(-0.15D, player.motionY, 0.000000001D);
            runtime.playerMovementState().update(false, false, false);
            runtime.playerMotionControl(player);
            assertEquals(-0.10D, player.motionY, 0.000000001D);

            // Do not slow faster upward motion. Flight retains priority.
            player.motionY = 0.28D;
            runtime.playerMotionControl(player);
            assertEquals(0.28D, player.motionY, 0.000000001D);
            controller.enable(Minecraft189FlightModule.ID);
            runtime.playerMotionControl(player);
            assertEquals(Minecraft189FlightModule.HOVER_MOTION_Y,
                    player.motionY, 0.000000001D);
            controller.disable(Minecraft189FlightModule.ID);

            // Nonfinite mapped downwards input repairs to finite target.
            player.motionY = Double.NEGATIVE_INFINITY;
            runtime.playerMotionControl(player);
            assertEquals(0.20D, player.motionY, 0.000000001D);
            player.motionY = Double.NaN;
            runtime.playerMotionControl(player);
            assertTrue(Double.isNaN(player.motionY)); // Fail-closed as before.

            gravity.smoothLiftSetting().set(Boolean.FALSE);
            player.motionY = -0.40D;
            runtime.playerMotionControl(player);
            assertEquals(0.20D, player.motionY, 0.000000001D);
            controller.disable(Minecraft189NoGravityModule.ID);
            player.motionY = -0.20D;
            runtime.playerMotionControl(player);
            assertEquals(-0.20D, player.motionY, 0.000000001D);

            assertThrows(IllegalArgumentException.class,
                    () -> gravity.liftStepSetting().set(0.009D));
            assertThrows(IllegalArgumentException.class,
                    () -> gravity.liftStepSetting().set(0.51D));
            assertThrows(IllegalArgumentException.class,
                    () -> gravity.liftStepSetting().set(Double.NaN));
        } finally {
            runtime.close();
        }
        assertNull(settings.find(
                Minecraft189NoGravityModule.SMOOTH_LIFT_SETTING_ID));
        assertNull(settings.find(
                Minecraft189NoGravityModule.LIFT_STEP_SETTING_ID));
        assertNull(modules.find(Minecraft189NoGravityModule.ID));
    }

    private static final class TestPlayer
            implements Minecraft189PlayerMotionControl {
        private double motionX;
        private double motionY;
        private double motionZ;

        @Override
        public double customMcMotionX() {
            return motionX;
        }

        @Override
        public void customMcSetMotionX(
                final double motionX) {
            this.motionX = motionX;
        }

        @Override
        public double customMcMotionY() {
            return motionY;
        }

        @Override
        public void customMcSetMotionY(
                final double motionY) {
            this.motionY = motionY;
        }

        @Override
        public double customMcMotionZ() {
            return motionZ;
        }

        @Override
        public void customMcSetMotionZ(
                final double motionZ) {
            this.motionZ = motionZ;
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
