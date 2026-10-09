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

final class Minecraft189BunnyHopModuleTest {
    @Test
    void bunnyHopRearmsAirborneAndOwnsMovementOverAutoJumpAndStrafe() {
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
                            Minecraft189BunnyHopModule.ID));
            assertNotNull(
                    settings.find(
                            Minecraft189BunnyHopModule.SPEED_SETTING_ID));
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189BunnyHopModule.SMOOTH_ACCELERATION_SETTING_ID));
            assertEquals("50", settings.snapshotEncoded().get(
                    Minecraft189BunnyHopModule.ACCELERATION_PERCENT_SETTING_ID));
            assertEquals(
                    Minecraft189BunnyHopModule.DEFAULT_SPEED,
                    runtime.featureCatalog()
                            .bunnyHop()
                            .speedSetting()
                            .get()
                            .doubleValue(),
                    0.000000001D);

            final TestPlayer player =
                    new TestPlayer();
            runtime.playerMovementState()
                    .update(
                            true,
                            false,
                            false);
            runtime.playerRotationState()
                    .update(
                            0.0F);
            controller.enable(
                    Minecraft189BunnyHopModule.ID);
            assertTrue(
                    runtime.featureCatalog()
                            .bunnyHop()
                            .active());

            runtime.playerJumpControl(
                    player);
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    0,
                    player.jumpCalls);
            assertEquals(
                    0.0D,
                    player.motionX,
                    0.000000001D);
            assertEquals(
                    0.0D,
                    player.motionZ,
                    0.000000001D);

            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.W,
                            true);
            runtime.playerJumpControl(
                    player);
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    1,
                    player.jumpCalls);
            assertEquals(
                    0.0D,
                    player.motionX,
                    0.000000001D);
            assertEquals(
                    Minecraft189BunnyHopModule.DEFAULT_SPEED,
                    player.motionZ,
                    0.000000001D);

            runtime.playerJumpControl(
                    player);
            assertEquals(
                    1,
                    player.jumpCalls);

            runtime.playerMovementState()
                    .update(
                            false,
                            false,
                            false);
            runtime.playerJumpControl(
                    player);
            assertEquals(
                    1,
                    player.jumpCalls);

            runtime.playerMovementState()
                    .update(
                            true,
                            false,
                            false);
            runtime.playerJumpControl(
                    player);
            assertEquals(
                    2,
                    player.jumpCalls);

            runtime.featureCatalog()
                    .bunnyHop()
                    .speedSetting()
                    .set(
                            0.55D);
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
                    -0.55D,
                    player.motionX,
                    0.000000001D);
            assertEquals(
                    0.0D,
                    player.motionZ,
                    0.000000001D);

            controller.enable(
                    Minecraft189AutoJumpModule.ID);
            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.W,
                            false);
            runtime.playerMovementState()
                    .update(
                            false,
                            false,
                            false);
            runtime.playerJumpControl(
                    player);
            runtime.playerMovementState()
                    .update(
                            true,
                            false,
                            false);
            runtime.playerJumpControl(
                    player);
            assertEquals(
                    2,
                    player.jumpCalls);

            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.W,
                            true);
            controller.enable(
                    Minecraft189LongJumpModule.ID);
            runtime.playerMovementState()
                    .update(
                            false,
                            false,
                            false);
            runtime.playerJumpControl(
                    player);
            runtime.playerMovementState()
                    .update(
                            true,
                            false,
                            false);
            runtime.playerJumpControl(
                    player);
            assertEquals(
                    2,
                    player.jumpCalls);
            controller.disable(
                    Minecraft189LongJumpModule.ID);

            controller.enable(
                    Minecraft189FlightModule.ID);
            runtime.playerMovementState()
                    .update(
                            false,
                            false,
                            false);
            runtime.playerJumpControl(
                    player);
            runtime.playerMovementState()
                    .update(
                            true,
                            false,
                            false);
            runtime.playerJumpControl(
                    player);
            assertEquals(
                    2,
                    player.jumpCalls);
            controller.disable(
                    Minecraft189FlightModule.ID);

            controller.disable(
                    Minecraft189BunnyHopModule.ID);
            assertFalse(
                    runtime.featureCatalog()
                            .bunnyHop()
                            .active());
            runtime.playerJumpControl(
                    player);
            assertEquals(
                    3,
                    player.jumpCalls);

            runtime.playerJumpControl(
                    null);
            runtime.playerMotionControl(
                    null);
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189BunnyHopModule.ID));
        assertNull(
                settings.find(
                        Minecraft189BunnyHopModule.SPEED_SETTING_ID));
        assertNull(settings.find(
                Minecraft189BunnyHopModule.SMOOTH_ACCELERATION_SETTING_ID));
        assertNull(settings.find(
                Minecraft189BunnyHopModule.ACCELERATION_PERCENT_SETTING_ID));
        assertNull(
                categories.find(
                        Minecraft189FeatureCatalog.MOVEMENT_CATEGORY_ID));
    }

    @Test
    void bunnyHopSmoothAccelerationBlendsActualMotionAndPreservesJumpCadence() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189BunnyHopModule module = new Minecraft189BunnyHopModule(input);
        final Minecraft189PlayerRotationState rotation =
                new Minecraft189PlayerRotationState();
        final Minecraft189PlayerMovementState movement =
                new Minecraft189PlayerMovementState();
        final TestPlayer player = new TestPlayer();
        rotation.update(0.0F);
        movement.update(true, false, false);
        module.onEnable();
        input.key(LegacyKeyboardCodes.W, true);
        assertFalse(module.smoothAccelerationSetting().get().booleanValue());
        assertEquals(50, module.accelerationPercentSetting().get().intValue());
        assertTrue(module.applyMotion(player, rotation.snapshot(), false));
        assertEquals(0.38D, player.motionZ, 0.000000001D); // Instant default.

        player.motionZ = 0.0D;
        module.smoothAccelerationSetting().set(Boolean.TRUE);
        assertTrue(module.applyMotion(player, rotation.snapshot(), false));
        assertEquals(0.19D, player.motionZ, 0.000000001D);
        module.applyMotion(player, rotation.snapshot(), false);
        assertEquals(0.285D, player.motionZ, 0.000000001D);

        module.accelerationPercentSetting().set(100);
        module.applyMotion(player, rotation.snapshot(), false);
        assertEquals(0.38D, player.motionZ, 0.000000001D);
        module.speedSetting().set(0.60D);
        module.accelerationPercentSetting().set(50);
        module.applyMotion(player, rotation.snapshot(), false);
        assertEquals(0.49D, player.motionZ, 0.000000001D);

        // No horizontal writes during a higher-priority owner, and no
        // accumulated interpolation credit when movement is suspended.
        final double beforeSuspend = player.motionZ;
        module.applyMotion(player, rotation.snapshot(), true);
        assertEquals(beforeSuspend, player.motionZ, 0.000000001D);
        input.key(LegacyKeyboardCodes.W, false);
        assertFalse(module.applyMotion(player, rotation.snapshot(), false));
        assertEquals(beforeSuspend, player.motionZ, 0.000000001D);
        input.key(LegacyKeyboardCodes.S, true);
        module.applyMotion(player, rotation.snapshot(), false);
        assertEquals(-0.055D, player.motionZ, 0.000000001D);

        input.key(LegacyKeyboardCodes.S, false);
        input.key(LegacyKeyboardCodes.W, true);
        player.motionZ = Double.NaN;
        module.applyMotion(player, rotation.snapshot(), false);
        assertEquals(0.60D, player.motionZ, 0.000000001D);
        player.motionZ = Double.POSITIVE_INFINITY;
        module.applyMotion(player, rotation.snapshot(), false);
        assertEquals(0.60D, player.motionZ, 0.000000001D);
        assertEquals(0.60D, Minecraft189BunnyHopModule.interpolate(
                Double.NEGATIVE_INFINITY, 0.60D, 0.5D), 0.000000001D);
        assertEquals(0.30D, Minecraft189BunnyHopModule.interpolate(
                0.0D, 0.60D, 0.5D), 0.000000001D);
        assertEquals(0.60D, Minecraft189BunnyHopModule.interpolate(
                0.5999999D, 0.60D, 0.5D), 0.000000001D);

        // Jump rearm is unchanged by horizontal smoothing.
        module.applyJump(player, movement.snapshot(), false);
        assertEquals(1, player.jumpCalls);
        module.applyJump(player, movement.snapshot(), false);
        assertEquals(1, player.jumpCalls);
        movement.update(false, false, false);
        module.applyJump(player, movement.snapshot(), false);
        movement.update(true, false, false);
        module.applyJump(player, movement.snapshot(), false);
        assertEquals(2, player.jumpCalls);

        module.smoothAccelerationSetting().set(Boolean.FALSE);
        player.motionZ = 0.0D;
        module.applyMotion(player, rotation.snapshot(), false);
        assertEquals(0.60D, player.motionZ, 0.000000001D);
        module.onDisable();
        player.motionZ = 0.1D;
        assertFalse(module.applyMotion(player, rotation.snapshot(), false));
        assertEquals(0.1D, player.motionZ, 0.000000001D);
        assertThrows(IllegalArgumentException.class,
                () -> module.accelerationPercentSetting().set(9));
        assertThrows(IllegalArgumentException.class,
                () -> module.accelerationPercentSetting().set(101));
    }

    @Test
    void bunnyHopLandingDelayWaitsOnlyForEligibleGroundCallbacks() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189BunnyHopModule bunny = new Minecraft189BunnyHopModule(input);
        final Minecraft189PlayerMovementState movement = new Minecraft189PlayerMovementState();
        final TestPlayer player = new TestPlayer();
        input.key(LegacyKeyboardCodes.W, true);
        movement.update(true, false, false);
        bunny.onEnable();
        assertEquals(Integer.valueOf(0), bunny.landingDelayTicksSetting().get());
        bunny.applyJump(player, movement.snapshot(), false);
        assertEquals(1, player.jumpCalls); // Original immediate jump.
        bunny.applyJump(player, movement.snapshot(), false);
        assertEquals(1, player.jumpCalls); // No repeated ground contact.

        bunny.landingDelayTicksSetting().set(2);
        movement.update(false, false, false);
        bunny.applyJump(player, movement.snapshot(), false); // Rearm.
        movement.update(true, false, false);
        bunny.applyJump(player, movement.snapshot(), false);
        bunny.applyJump(player, movement.snapshot(), false);
        assertEquals(1, player.jumpCalls);
        bunny.applyJump(player, movement.snapshot(), false);
        assertEquals(2, player.jumpCalls);
        bunny.applyJump(player, movement.snapshot(), false);
        assertEquals(2, player.jumpCalls);

        movement.update(false, false, false);
        bunny.applyJump(player, movement.snapshot(), false);
        movement.update(true, false, false);
        bunny.applyJump(player, movement.snapshot(), false); // 1/2.
        input.key(LegacyKeyboardCodes.W, false);
        bunny.applyJump(player, movement.snapshot(), false); // Reset.
        input.key(LegacyKeyboardCodes.W, true);
        bunny.applyJump(player, movement.snapshot(), false); // 1/2.
        bunny.applyJump(player, movement.snapshot(), true); // Suspended reset.
        bunny.applyJump(player, movement.snapshot(), false);
        bunny.applyJump(player, movement.snapshot(), false);
        assertEquals(2, player.jumpCalls);
        bunny.applyJump(player, movement.snapshot(), false);
        assertEquals(3, player.jumpCalls);

        // Live delay edits lose credit. Unavailable state loses waiting,
        // while the existing horizontal-speed owner remains untouched.
        movement.update(false, false, false);
        bunny.applyJump(player, movement.snapshot(), false);
        movement.update(true, false, false);
        bunny.applyJump(player, movement.snapshot(), false); // 1/2.
        bunny.landingDelayTicksSetting().set(3);
        for (int i = 0; i < 3; i++) {
            bunny.applyJump(player, movement.snapshot(), false);
            assertEquals(3, player.jumpCalls);
        }
        bunny.applyJump(player, movement.snapshot(), false);
        assertEquals(4, player.jumpCalls);

        movement.update(false, false, false);
        bunny.applyJump(player, movement.snapshot(), false);
        movement.update(true, false, false);
        bunny.applyJump(player, movement.snapshot(), false);
        movement.clear();
        bunny.applyJump(player, movement.snapshot(), false);
        movement.update(true, false, false);
        for (int i = 0; i < 3; i++) {
            bunny.applyJump(player, movement.snapshot(), false);
            assertEquals(4, player.jumpCalls);
        }
        bunny.applyJump(player, movement.snapshot(), false);
        assertEquals(5, player.jumpCalls);

        bunny.landingDelayTicksSetting().set(0);
        movement.update(false, false, false);
        bunny.applyJump(player, movement.snapshot(), false);
        movement.update(true, false, false);
        bunny.applyJump(player, movement.snapshot(), false);
        assertEquals(6, player.jumpCalls);
        bunny.onDisable();
        bunny.applyJump(player, movement.snapshot(), false);
        assertEquals(6, player.jumpCalls);
        assertThrows(IllegalArgumentException.class,
                () -> bunny.landingDelayTicksSetting().set(-1));
        assertThrows(IllegalArgumentException.class,
                () -> bunny.landingDelayTicksSetting().set(11));
    }

    @Test
    void hostedBunnyHopLandingDelayPersistsAndCleansUp() {
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
            final Minecraft189BunnyHopModule bunny = runtime.featureCatalog().bunnyHop();
            assertEquals("0", settings.snapshotEncoded().get(
                    Minecraft189BunnyHopModule.LANDING_DELAY_SETTING_ID));
            bunny.landingDelayTicksSetting().set(2);
            assertEquals("2", settings.snapshotEncoded().get(
                    Minecraft189BunnyHopModule.LANDING_DELAY_SETTING_ID));
            controller.enable(Minecraft189BunnyHopModule.ID);
            final TestPlayer player = new TestPlayer();
            runtime.inputState().key(LegacyKeyboardCodes.W, true);
            runtime.playerMovementState().update(true, false, false);
            runtime.playerJumpControl(player);
            runtime.playerJumpControl(player);
            assertEquals(0, player.jumpCalls);
            runtime.playerJumpControl(player);
            assertEquals(1, player.jumpCalls);
            runtime.playerJumpControl(player);
            assertEquals(1, player.jumpCalls);
            runtime.playerMovementState().update(false, false, false);
            runtime.playerJumpControl(player);
            runtime.playerMovementState().update(true, false, false);
            runtime.playerJumpControl(player);
            runtime.playerJumpControl(player);
            assertEquals(1, player.jumpCalls);
            runtime.playerJumpControl(player);
            assertEquals(2, player.jumpCalls);
            controller.disable(Minecraft189BunnyHopModule.ID);
            runtime.playerJumpControl(player);
            assertEquals(2, player.jumpCalls);
        } finally {
            runtime.close();
        }
        assertNull(settings.find(Minecraft189BunnyHopModule.LANDING_DELAY_SETTING_ID));
        assertNull(modules.find(Minecraft189BunnyHopModule.ID));
    }

    private static final class TestPlayer
            implements Minecraft189PlayerJumpControl,
            Minecraft189PlayerMotionControl {
        private int jumpCalls;
        private double motionX;
        private double motionY;
        private double motionZ;

        @Override
        public void customMcJump() {
            jumpCalls++;
            motionY = 0.42D;
        }

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
