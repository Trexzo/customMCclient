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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class Minecraft189FastFallModuleTest {
    @Test
    void fastFallAcceleratesOnlyGentleDescentAndOwnsGlide() {
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
                            Minecraft189FastFallModule.ID));
            assertNotNull(
                    settings.find(
                            Minecraft189FastFallModule.FALL_SPEED_SETTING_ID));
            assertNotNull(settings.find(Minecraft189FastFallModule.PROGRESSIVE_SETTING_ID));
            assertNotNull(settings.find(Minecraft189FastFallModule.RAMP_STEP_SETTING_ID));
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189FastFallModule.PROGRESSIVE_SETTING_ID));
            assertEquals("0.05", settings.snapshotEncoded().get(
                    Minecraft189FastFallModule.RAMP_STEP_SETTING_ID));

            final TestPlayer player =
                    new TestPlayer();
            runtime.playerMovementState()
                    .update(
                            false,
                            false,
                            false);

            controller.enable(
                    Minecraft189FastFallModule.ID);
            assertTrue(
                    runtime.featureCatalog()
                            .fastFall()
                            .active());

            player.motionY = -0.05D;
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    -Minecraft189FastFallModule.DEFAULT_FALL_SPEED,
                    player.motionY,
                    0.000000001D);

            player.motionY = 0.20D;
            final int beforeRise =
                    player.verticalSetCalls;
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    0.20D,
                    player.motionY,
                    0.000000001D);
            assertEquals(
                    beforeRise,
                    player.verticalSetCalls);

            player.motionY = -0.60D;
            final int beforeFastDescent =
                    player.verticalSetCalls;
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    -0.60D,
                    player.motionY,
                    0.000000001D);
            assertEquals(
                    beforeFastDescent,
                    player.verticalSetCalls);

            runtime.featureCatalog()
                    .fastFall()
                    .fallSpeedSetting()
                    .set(
                            0.45D);
            player.motionY = -0.10D;
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    -0.45D,
                    player.motionY,
                    0.000000001D);

            controller.enable(
                    Minecraft189GlideModule.ID);
            runtime.featureCatalog()
                    .glide()
                    .fallSpeedSetting()
                    .set(
                            0.08D);
            player.motionY = -0.10D;
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    -0.45D,
                    player.motionY,
                    0.000000001D);

            controller.enable(
                    Minecraft189FlightModule.ID);
            player.motionY = -0.40D;
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    Minecraft189FlightModule.HOVER_MOTION_Y,
                    player.motionY,
                    0.000000001D);

            controller.disable(
                    Minecraft189FlightModule.ID);
            controller.disable(
                    Minecraft189FastFallModule.ID);
            assertFalse(
                    runtime.featureCatalog()
                            .fastFall()
                            .active());

            player.motionY = -0.40D;
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    -0.08D,
                    player.motionY,
                    0.000000001D);

            runtime.playerMovementState()
                    .update(
                            true,
                            false,
                            false);
            player.motionY = -0.40D;
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    -0.40D,
                    player.motionY,
                    0.000000001D);

            runtime.playerMotionControl(
                    null);
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189FastFallModule.ID));
        assertNull(
                settings.find(
                        Minecraft189FastFallModule.FALL_SPEED_SETTING_ID));
        assertNull(settings.find(Minecraft189FastFallModule.PROGRESSIVE_SETTING_ID));
        assertNull(settings.find(Minecraft189FastFallModule.RAMP_STEP_SETTING_ID));
        assertNull(
                categories.find(
                        Minecraft189FeatureCatalog.MOVEMENT_CATEGORY_ID));
    }

    @Test
    void progressiveFastFallRampsWithoutOvershootAndHonorsMovementPriority() {
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
            final Minecraft189FastFallModule fall = runtime.featureCatalog().fastFall();
            final TestPlayer player = new TestPlayer();
            assertFalse(fall.progressiveSetting().get().booleanValue());
            assertEquals(Double.valueOf(0.05D), fall.rampStepSetting().get());
            controller.enable(Minecraft189FastFallModule.ID);
            runtime.playerMovementState().update(false, false, false);

            // Default OFF preserves the existing immediate target.
            player.motionY = -0.05D;
            runtime.playerMotionControl(player);
            assertEquals(-0.30D, player.motionY, 0.000000001D);
            assertEquals(1, player.verticalSetCalls);

            fall.progressiveSetting().set(Boolean.TRUE);
            fall.rampStepSetting().set(0.08D);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189FastFallModule.PROGRESSIVE_SETTING_ID));
            assertEquals("0.08", settings.snapshotEncoded().get(
                    Minecraft189FastFallModule.RAMP_STEP_SETTING_ID));
            player.motionY = -0.05D;
            runtime.playerMotionControl(player);
            assertEquals(-0.13D, player.motionY, 0.000000001D);
            runtime.playerMotionControl(player);
            assertEquals(-0.21D, player.motionY, 0.000000001D);
            runtime.playerMotionControl(player);
            assertEquals(-0.29D, player.motionY, 0.000000001D);
            runtime.playerMotionControl(player);
            assertEquals(-0.30D, player.motionY, 0.000000001D);
            final int writesAtTarget = player.verticalSetCalls;
            runtime.playerMotionControl(player);
            assertEquals(writesAtTarget, player.verticalSetCalls);

            // Lowering the speed limit does not slow a faster native fall.
            player.motionY = -0.60D;
            runtime.playerMotionControl(player);
            assertEquals(-0.60D, player.motionY, 0.000000001D);
            assertEquals(writesAtTarget, player.verticalSetCalls);

            player.motionY = 0.20D;
            runtime.playerMotionControl(player);
            assertEquals(0.20D, player.motionY, 0.000000001D);
            player.motionY = 0.0D;
            runtime.playerMotionControl(player);
            assertEquals(0.0D, player.motionY, 0.000000001D);
            player.motionY = Double.NaN;
            runtime.playerMotionControl(player);
            assertTrue(Double.isNaN(player.motionY));
            player.motionY = Double.NEGATIVE_INFINITY;
            runtime.playerMotionControl(player);
            assertEquals(Double.NEGATIVE_INFINITY, player.motionY);
            assertEquals(writesAtTarget, player.verticalSetCalls);

            // Live speed edits change the cap without introducing state.
            fall.fallSpeedSetting().set(0.45D);
            fall.rampStepSetting().set(0.20D);
            player.motionY = -0.10D;
            runtime.playerMotionControl(player);
            assertEquals(-0.30D, player.motionY, 0.000000001D);
            runtime.playerMotionControl(player);
            assertEquals(-0.45D, player.motionY, 0.000000001D);
            final int writesAtNewTarget = player.verticalSetCalls;

            // Fail closed on ground or when mapped movement is unavailable.
            runtime.playerMovementState().update(true, false, false);
            player.motionY = -0.10D;
            runtime.playerMotionControl(player);
            assertEquals(-0.10D, player.motionY, 0.000000001D);
            runtime.playerMovementState().clear();
            runtime.playerMotionControl(player);
            assertEquals(-0.10D, player.motionY, 0.000000001D);
            assertEquals(writesAtNewTarget, player.verticalSetCalls);

            // Flight retains its higher-priority vertical motion ownership.
            runtime.playerMovementState().update(false, false, false);
            controller.enable(Minecraft189FlightModule.ID);
            player.motionY = -0.10D;
            runtime.playerMotionControl(player);
            assertEquals(Minecraft189FlightModule.HOVER_MOTION_Y,
                    player.motionY, 0.000000001D);
            controller.disable(Minecraft189FlightModule.ID);

            // Disabled module never writes synthetic downward movement.
            controller.disable(Minecraft189FastFallModule.ID);
            player.motionY = -0.10D;
            final int writesBeforeDisabled = player.verticalSetCalls;
            runtime.playerMotionControl(player);
            assertEquals(-0.10D, player.motionY, 0.000000001D);
            assertEquals(writesBeforeDisabled, player.verticalSetCalls);

            assertThrows(IllegalArgumentException.class,
                    () -> fall.rampStepSetting().set(0.009D));
            assertThrows(IllegalArgumentException.class,
                    () -> fall.rampStepSetting().set(0.501D));
            assertThrows(IllegalArgumentException.class,
                    () -> fall.rampStepSetting().set(Double.NaN));
            assertThrows(IllegalArgumentException.class,
                    () -> fall.rampStepSetting().set(Double.POSITIVE_INFINITY));

            // Turning progressive mode OFF restores immediate Fast Fall.
            fall.progressiveSetting().set(Boolean.FALSE);
            controller.enable(Minecraft189FastFallModule.ID);
            player.motionY = -0.10D;
            runtime.playerMotionControl(player);
            assertEquals(-0.45D, player.motionY, 0.000000001D);
            controller.disable(Minecraft189FastFallModule.ID);
        } finally {
            runtime.close();
        }
        assertNull(modules.find(Minecraft189FastFallModule.ID));
        assertNull(settings.find(Minecraft189FastFallModule.FALL_SPEED_SETTING_ID));
        assertNull(settings.find(Minecraft189FastFallModule.PROGRESSIVE_SETTING_ID));
        assertNull(settings.find(Minecraft189FastFallModule.RAMP_STEP_SETTING_ID));
    }

    @Test
    void suspendedProgressiveFastFallNeverWritesAndDoesNotAccumulateCredit() {
        final Minecraft189FastFallModule fall = new Minecraft189FastFallModule();
        final Minecraft189PlayerMovementState movement =
                new Minecraft189PlayerMovementState();
        final TestPlayer player = new TestPlayer();
        fall.onEnable();
        fall.progressiveSetting().set(Boolean.TRUE);
        fall.rampStepSetting().set(0.10D);
        movement.update(false, false, false);
        player.motionY = -0.05D;
        fall.apply(player, movement.snapshot(), true);
        assertEquals(-0.05D, player.motionY, 0.000000001D);
        assertEquals(0, player.verticalSetCalls);
        fall.apply(player, movement.snapshot(), false);
        assertEquals(-0.15D, player.motionY, 0.000000001D);
        assertEquals(1, player.verticalSetCalls);
        fall.onDisable();
        fall.apply(player, movement.snapshot(), false);
        assertEquals(1, player.verticalSetCalls);
    }

    @Test
    void fastFallActivationDelayRequiresConsecutiveDescendingCallbacks() {
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
            final Minecraft189FastFallModule fall = runtime.featureCatalog().fastFall();
            final TestPlayer player = new TestPlayer();
            assertEquals(0, fall.activationDelayTicksSetting().get().intValue());
            assertEquals("0", settings.snapshotEncoded().get(
                    Minecraft189FastFallModule.ACTIVATION_DELAY_SETTING_ID));
            controller.enable(Minecraft189FastFallModule.ID);
            runtime.playerMovementState().update(false, false, false);
            player.motionY = -0.05D;
            runtime.playerMotionControl(player);
            assertEquals(-0.30D, player.motionY, 0.000000001D); // Default instant.

            fall.activationDelayTicksSetting().set(2);
            assertEquals("2", settings.snapshotEncoded().get(
                    Minecraft189FastFallModule.ACTIVATION_DELAY_SETTING_ID));
            player.motionY = -0.05D;
            final int initialWrites = player.verticalSetCalls;
            runtime.playerMotionControl(player);
            runtime.playerMotionControl(player);
            assertEquals(initialWrites, player.verticalSetCalls);
            runtime.playerMotionControl(player);
            assertEquals(-0.30D, player.motionY, 0.000000001D);
            assertEquals(initialWrites + 1, player.verticalSetCalls);

            // Ground contact cancels the countdown without writing.
            runtime.playerMovementState().update(true, false, false);
            player.motionY = -0.05D;
            runtime.playerMotionControl(player);
            runtime.playerMovementState().update(false, false, false);
            runtime.playerMotionControl(player);
            assertEquals(-0.05D, player.motionY, 0.000000001D);
            runtime.playerMotionControl(player);
            assertEquals(-0.05D, player.motionY, 0.000000001D);
            runtime.playerMotionControl(player);
            assertEquals(-0.30D, player.motionY, 0.000000001D);

            // Rising, zero and malformed velocity each restart delay.
            player.motionY = -0.05D;
            runtime.playerMotionControl(player); // Prior descent was eligible.
            player.motionY = 0.10D;
            runtime.playerMotionControl(player); // Reset.
            player.motionY = -0.05D;
            runtime.playerMotionControl(player);
            assertEquals(-0.05D, player.motionY, 0.000000001D);
            player.motionY = Double.NaN;
            runtime.playerMotionControl(player); // Reset without write.
            player.motionY = -0.05D;
            runtime.playerMotionControl(player);
            runtime.playerMotionControl(player);
            assertEquals(-0.05D, player.motionY, 0.000000001D);
            runtime.playerMotionControl(player);
            assertEquals(-0.30D, player.motionY, 0.000000001D);

            // Live delay edit restarts, and progressive ramp remains independent.
            fall.progressiveSetting().set(Boolean.TRUE);
            fall.rampStepSetting().set(0.10D);
            fall.activationDelayTicksSetting().set(1);
            player.motionY = -0.05D;
            runtime.playerMotionControl(player);
            assertEquals(-0.05D, player.motionY, 0.000000001D);
            runtime.playerMotionControl(player);
            assertEquals(-0.15D, player.motionY, 0.000000001D);

            fall.activationDelayTicksSetting().set(0);
            player.motionY = -0.05D;
            runtime.playerMotionControl(player);
            assertEquals(-0.15D, player.motionY, 0.000000001D);
            controller.disable(Minecraft189FastFallModule.ID);
            player.motionY = -0.05D;
            final int beforeDisable = player.verticalSetCalls;
            runtime.playerMotionControl(player);
            assertEquals(beforeDisable, player.verticalSetCalls);
            assertThrows(IllegalArgumentException.class,
                    () -> fall.activationDelayTicksSetting().set(-1));
            assertThrows(IllegalArgumentException.class,
                    () -> fall.activationDelayTicksSetting().set(11));
        } finally {
            runtime.close();
        }
        assertNull(settings.find(Minecraft189FastFallModule.ACTIVATION_DELAY_SETTING_ID));
        assertNull(modules.find(Minecraft189FastFallModule.ID));
    }

    @Test
    void suspendedFastFallResetsPendingActivationDelay() {
        final Minecraft189FastFallModule fall = new Minecraft189FastFallModule();
        final Minecraft189PlayerMovementState movement =
                new Minecraft189PlayerMovementState();
        final TestPlayer player = new TestPlayer();
        fall.activationDelayTicksSetting().set(2);
        fall.onEnable();
        movement.update(false, false, false);
        player.motionY = -0.05D;
        fall.apply(player, movement.snapshot(), false);
        assertEquals(0, player.verticalSetCalls);
        fall.apply(player, movement.snapshot(), true); // Reset countdown.
        fall.apply(player, movement.snapshot(), false);
        fall.apply(player, movement.snapshot(), false);
        assertEquals(0, player.verticalSetCalls);
        fall.apply(player, movement.snapshot(), false);
        assertEquals(-0.30D, player.motionY, 0.000000001D);
        assertEquals(1, player.verticalSetCalls);
        fall.onDisable();
    }

    @Test
    void fastFallSneakPausePreservesMotionAndResetsActivationDelay() {
        final Minecraft189FastFallModule fall = new Minecraft189FastFallModule();
        final Minecraft189PlayerMovementState movement =
                new Minecraft189PlayerMovementState();
        final TestPlayer player = new TestPlayer();
        fall.activationDelayTicksSetting().set(2);
        fall.onEnable();
        assertFalse(fall.pauseWhileSneakingSetting().get().booleanValue());
        movement.update(false, true, false);
        player.motionY = -0.05D;
        fall.apply(player, movement.snapshot(), false);
        fall.apply(player, movement.snapshot(), false);
        fall.apply(player, movement.snapshot(), false);
        assertEquals(-0.30D, player.motionY, 0.000000001D);
        assertEquals(1, player.verticalSetCalls); // Opt-in OFF parity.

        fall.pauseWhileSneakingSetting().set(Boolean.TRUE);
        player.motionY = -0.05D;
        fall.apply(player, movement.snapshot(), false);
        fall.apply(player, movement.snapshot(), false);
        assertEquals(-0.05D, player.motionY, 0.000000001D);
        assertEquals(1, player.verticalSetCalls); // No synthetic pause writes.
        movement.update(false, false, false);
        fall.apply(player, movement.snapshot(), false); // Eligible 1/2.
        movement.update(false, true, false);
        fall.apply(player, movement.snapshot(), false); // Reset 0/2.
        movement.update(false, false, false);
        fall.apply(player, movement.snapshot(), false);
        fall.apply(player, movement.snapshot(), false);
        assertEquals(-0.05D, player.motionY, 0.000000001D);
        fall.apply(player, movement.snapshot(), false);
        assertEquals(-0.30D, player.motionY, 0.000000001D);
        assertEquals(2, player.verticalSetCalls);

        // Progressive acceleration also honors the live guard. Suspension
        // and missing state never allow delayed acceleration credit.
        fall.progressiveSetting().set(Boolean.TRUE);
        fall.rampStepSetting().set(0.05D);
        player.motionY = -0.05D;
        fall.apply(player, movement.snapshot(), false); // 1/2.
        movement.clear();
        fall.apply(player, movement.snapshot(), false); // Reset.
        movement.update(false, false, false);
        fall.apply(player, movement.snapshot(), false);
        fall.apply(player, movement.snapshot(), false);
        assertEquals(-0.05D, player.motionY, 0.000000001D);
        fall.apply(player, movement.snapshot(), false);
        assertEquals(-0.10D, player.motionY, 0.000000001D);
        player.motionY = -0.05D;
        fall.apply(player, movement.snapshot(), true); // Priority reset.
        movement.update(false, true, false);
        fall.apply(player, movement.snapshot(), false); // Sneak reset.
        movement.update(false, false, false);
        fall.apply(player, movement.snapshot(), false);
        fall.apply(player, movement.snapshot(), false);
        assertEquals(-0.05D, player.motionY, 0.000000001D);
        fall.apply(player, movement.snapshot(), false);
        assertEquals(-0.10D, player.motionY, 0.000000001D);
        fall.onDisable();
        player.motionY = -0.05D;
        int prior = player.verticalSetCalls;
        fall.apply(player, movement.snapshot(), false);
        assertEquals(prior, player.verticalSetCalls);
    }

    @Test
    void fastFallSneakPauseSettingsPersistAndCleanlyUnregister() {
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
            final Minecraft189FastFallModule fall = runtime.featureCatalog().fastFall();
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189FastFallModule.PAUSE_WHILE_SNEAKING_SETTING_ID));
            controller.enable(Minecraft189FastFallModule.ID);
            fall.pauseWhileSneakingSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189FastFallModule.PAUSE_WHILE_SNEAKING_SETTING_ID));
            final TestPlayer player = new TestPlayer();
            runtime.playerMovementState().update(false, true, false);
            player.motionY = -0.05D;
            runtime.playerMotionControl(player);
            assertEquals(-0.05D, player.motionY, 0.000000001D);
            runtime.playerMovementState().update(false, false, false);
            runtime.playerMotionControl(player);
            assertEquals(-0.30D, player.motionY, 0.000000001D);
            fall.pauseWhileSneakingSetting().set(Boolean.FALSE);
            runtime.playerMovementState().update(false, true, false);
            player.motionY = -0.05D;
            runtime.playerMotionControl(player);
            assertEquals(-0.30D, player.motionY, 0.000000001D);
            controller.disable(Minecraft189FastFallModule.ID);
        } finally {
            runtime.close();
        }
        assertNull(settings.find(
                Minecraft189FastFallModule.PAUSE_WHILE_SNEAKING_SETTING_ID));
        assertNull(modules.find(Minecraft189FastFallModule.ID));
    }

    private static final class TestPlayer
            implements Minecraft189PlayerMotionControl {
        private double motionX;
        private double motionY;
        private double motionZ;
        private int verticalSetCalls;

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
            verticalSetCalls++;
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
