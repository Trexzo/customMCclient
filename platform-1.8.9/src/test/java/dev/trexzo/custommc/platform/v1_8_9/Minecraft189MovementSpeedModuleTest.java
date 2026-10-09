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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class Minecraft189MovementSpeedModuleTest {
    @Test
    void movementSpeedOwnsGroundMotionAboveStrafeAndBelowFlight() {
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
            assertNotEquals(
                    Minecraft189SpeedModule.ID,
                    Minecraft189MovementSpeedModule.ID);
            assertEquals(
                    "render.speed",
                    Minecraft189SpeedModule.ID);
            assertEquals(
                    "movement.speed",
                    Minecraft189MovementSpeedModule.ID);
            assertNotNull(
                    modules.find(
                            Minecraft189SpeedModule.ID));
            assertNotNull(
                    modules.find(
                            Minecraft189MovementSpeedModule.ID));
            assertEquals(
                    ModuleState.DISABLED,
                    controller.stateOf(
                            Minecraft189MovementSpeedModule.ID));
            assertNotNull(
                    settings.find(
                            Minecraft189MovementSpeedModule.SPEED_SETTING_ID));

            final TestPlayer player =
                    new TestPlayer();
            player.motionX = 0.12D;
            player.motionZ = -0.13D;
            runtime.playerRotationState()
                    .update(
                            0.0F);
            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.W,
                            true);

            controller.enable(
                    Minecraft189MovementSpeedModule.ID);
            assertTrue(
                    runtime.featureCatalog()
                            .movementSpeed()
                            .active());

            runtime.playerMotionControl(
                    player);
            assertEquals(
                    0.12D,
                    player.motionX,
                    0.000000001D);
            assertEquals(
                    -0.13D,
                    player.motionZ,
                    0.000000001D);

            runtime.playerMovementState()
                    .update(
                            false,
                            false,
                            false);
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    0.12D,
                    player.motionX,
                    0.000000001D);
            assertEquals(
                    -0.13D,
                    player.motionZ,
                    0.000000001D);

            runtime.playerMovementState()
                    .update(
                            true,
                            false,
                            false);
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    0.0D,
                    player.motionX,
                    0.000000001D);
            assertEquals(
                    Minecraft189MovementSpeedModule.DEFAULT_SPEED,
                    player.motionZ,
                    0.000000001D);

            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.A,
                            true);
            runtime.playerMotionControl(
                    player);
            final double diagonal =
                    Minecraft189MovementSpeedModule.DEFAULT_SPEED
                            / Math.sqrt(2.0D);
            assertEquals(
                    diagonal,
                    player.motionX,
                    0.000000001D);
            assertEquals(
                    diagonal,
                    player.motionZ,
                    0.000000001D);

            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.A,
                            false);
            runtime.featureCatalog()
                    .movementSpeed()
                    .speedSetting()
                    .set(
                            0.65D);
            runtime.playerRotationState()
                    .update(
                            90.0F);
            controller.enable(
                    Minecraft189StrafeModule.ID);
            runtime.featureCatalog()
                    .strafe()
                    .speedSetting()
                    .set(
                            0.30D);
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    -0.65D,
                    player.motionX,
                    0.000000001D);
            assertEquals(
                    0.0D,
                    player.motionZ,
                    0.000000001D);

            controller.enable(
                    Minecraft189FlightModule.ID);
            runtime.playerRotationState()
                    .update(
                            0.0F);
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    0.0D,
                    player.motionX,
                    0.000000001D);
            assertEquals(
                    Minecraft189FlightModule.HORIZONTAL_MOTION,
                    player.motionZ,
                    0.000000001D);
            controller.disable(
                    Minecraft189FlightModule.ID);

            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.W,
                            false);
            player.motionX = 0.21D;
            player.motionZ = -0.22D;
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    0.21D,
                    player.motionX,
                    0.000000001D);
            assertEquals(
                    -0.22D,
                    player.motionZ,
                    0.000000001D);

            controller.disable(
                    Minecraft189MovementSpeedModule.ID);
            assertFalse(
                    runtime.featureCatalog()
                            .movementSpeed()
                            .active());
            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.W,
                            true);
            player.motionX = 0.31D;
            player.motionZ = 0.32D;
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    0.0D,
                    player.motionX,
                    0.000000001D);
            assertEquals(
                    0.30D,
                    player.motionZ,
                    0.000000001D);

            controller.disable(
                    Minecraft189StrafeModule.ID);
            runtime.playerMotionControl(
                    null);
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189MovementSpeedModule.ID));
        assertNull(
                settings.find(
                        Minecraft189MovementSpeedModule.SPEED_SETTING_ID));
        assertNull(
                categories.find(
                        Minecraft189FeatureCatalog.MOVEMENT_CATEGORY_ID));
    }

    @Test
    void groundSpeedSmoothAccelerationBlendsOnlyWhileGrounded() {
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
            final Minecraft189MovementSpeedModule speed =
                    runtime.featureCatalog().movementSpeed();
            final TestPlayer player = new TestPlayer();
            assertFalse(speed.smoothAccelerationSetting().get().booleanValue());
            assertEquals(50, speed.accelerationPercentSetting().get().intValue());
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189MovementSpeedModule.SMOOTH_ACCELERATION_SETTING_ID));
            assertEquals("50", settings.snapshotEncoded().get(
                    Minecraft189MovementSpeedModule.ACCELERATION_PERCENT_SETTING_ID));
            speed.speedSetting().set(0.60D);
            controller.enable(Minecraft189MovementSpeedModule.ID);
            runtime.inputState().key(LegacyKeyboardCodes.W, true);
            runtime.playerRotationState().update(0.0F);
            runtime.playerMovementState().update(true, false, false);
            runtime.playerMotionControl(player);
            assertEquals(0.60D, player.motionZ, 0.000000001D);
            player.motionZ = 0.0D;
            speed.smoothAccelerationSetting().set(Boolean.TRUE);
            runtime.playerMotionControl(player);
            assertEquals(0.30D, player.motionZ, 0.000000001D);
            runtime.playerMotionControl(player);
            assertEquals(0.45D, player.motionZ, 0.000000001D);
            speed.accelerationPercentSetting().set(25);
            assertEquals("25", settings.snapshotEncoded().get(
                    Minecraft189MovementSpeedModule.ACCELERATION_PERCENT_SETTING_ID));
            runtime.playerMotionControl(player);
            assertEquals(0.4875D, player.motionZ, 0.000000001D);

            // Air/unknown movement state does not write or accumulate.
            runtime.playerMovementState().update(false, false, false);
            runtime.playerMotionControl(player);
            assertEquals(0.4875D, player.motionZ, 0.000000001D);
            runtime.playerMovementState().clear();
            runtime.playerMotionControl(player);
            assertEquals(0.4875D, player.motionZ, 0.000000001D);
            runtime.playerMovementState().update(true, false, false);
            runtime.playerMotionControl(player);
            assertEquals(0.515625D, player.motionZ, 0.000000001D);

            runtime.inputState().key(LegacyKeyboardCodes.W, false);
            runtime.playerMotionControl(player);
            assertEquals(0.515625D, player.motionZ, 0.000000001D);
            runtime.inputState().key(LegacyKeyboardCodes.S, true);
            runtime.playerMotionControl(player);
            assertEquals(0.23671875D, player.motionZ, 0.000000001D);
            speed.accelerationPercentSetting().set(100);
            runtime.playerMotionControl(player);
            assertEquals(-0.60D, player.motionZ, 0.000000001D);

            player.motionX = Double.NaN;
            player.motionZ = Double.POSITIVE_INFINITY;
            runtime.playerMotionControl(player);
            assertEquals(0.0D, player.motionX, 0.000000001D);
            assertEquals(-0.60D, player.motionZ, 0.000000001D);
            speed.smoothAccelerationSetting().set(Boolean.FALSE);
            runtime.inputState().key(LegacyKeyboardCodes.S, false);
            runtime.inputState().key(LegacyKeyboardCodes.A, true);
            runtime.playerMotionControl(player);
            assertEquals(0.60D, player.motionX, 0.000000001D);
            assertEquals(0.0D, player.motionZ, 0.000000001D);

            controller.enable(Minecraft189FlightModule.ID);
            runtime.inputState().key(LegacyKeyboardCodes.A, false);
            runtime.inputState().key(LegacyKeyboardCodes.W, true);
            runtime.playerMotionControl(player);
            assertEquals(Minecraft189FlightModule.HORIZONTAL_MOTION,
                    player.motionZ, 0.000000001D);
            controller.disable(Minecraft189FlightModule.ID);

            controller.disable(Minecraft189MovementSpeedModule.ID);
            player.motionX = 0.20D;
            runtime.playerMotionControl(player);
            assertEquals(0.20D, player.motionX, 0.000000001D);
            assertThrows(IllegalArgumentException.class,
                    () -> speed.accelerationPercentSetting().set(9));
            assertThrows(IllegalArgumentException.class,
                    () -> speed.accelerationPercentSetting().set(101));
        } finally {
            runtime.close();
        }
        assertNull(settings.find(
                Minecraft189MovementSpeedModule.SMOOTH_ACCELERATION_SETTING_ID));
        assertNull(settings.find(
                Minecraft189MovementSpeedModule.ACCELERATION_PERCENT_SETTING_ID));
        assertNull(modules.find(Minecraft189MovementSpeedModule.ID));
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
