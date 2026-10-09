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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189FlightModuleTest {
    @Test
    void flightControlsVerticalMotionAndStopsWritingOnDisable() {
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
                            Minecraft189FlightModule.ID));
            assertNotNull(
                    settings.find(
                            Minecraft189FlightModule.HORIZONTAL_SPEED_SETTING_ID));
            assertNotNull(
                    settings.find(
                            Minecraft189FlightModule.VERTICAL_SPEED_SETTING_ID));
            assertEquals(
                    Minecraft189FlightModule.DEFAULT_HORIZONTAL_SPEED,
                    runtime.featureCatalog()
                            .flight()
                            .horizontalSpeedSetting()
                            .get()
                            .doubleValue(),
                    0.000000001D);
            assertEquals(
                    Minecraft189FlightModule.DEFAULT_VERTICAL_SPEED,
                    runtime.featureCatalog()
                            .flight()
                            .verticalSpeedSetting()
                            .get()
                            .doubleValue(),
                    0.000000001D);

            final TestPlayer player =
                    new TestPlayer();
            player.motionY = 0.42D;

            runtime.playerMotionControl(
                    player);
            assertEquals(
                    0.42D,
                    player.motionY,
                    0.000000001D);
            assertEquals(
                    0,
                    player.setCalls);

            controller.enable(
                    Minecraft189FlightModule.ID);
            assertTrue(
                    runtime.featureCatalog()
                            .flight()
                            .active());

            runtime.playerMotionControl(
                    player);
            assertEquals(
                    Minecraft189FlightModule.HOVER_MOTION_Y,
                    player.motionY,
                    0.000000001D);
            assertEquals(
                    1,
                    player.setCalls);

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
            assertEquals(
                    2,
                    player.setCalls);

            runtime.playerMotionControl(
                    player);
            assertEquals(
                    2,
                    player.setCalls);

            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.LEFT_SHIFT,
                            true);
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    Minecraft189FlightModule.HOVER_MOTION_Y,
                    player.motionY,
                    0.000000001D);

            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.SPACE,
                            false);
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    Minecraft189FlightModule.DESCEND_MOTION_Y,
                    player.motionY,
                    0.000000001D);

            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.LEFT_SHIFT,
                            false);
            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.RIGHT_SHIFT,
                            true);
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    Minecraft189FlightModule.DESCEND_MOTION_Y,
                    player.motionY,
                    0.000000001D);

            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.RIGHT_SHIFT,
                            false);
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    Minecraft189FlightModule.HOVER_MOTION_Y,
                    player.motionY,
                    0.000000001D);

            runtime.playerRotationState()
                    .update(
                            0.0F);
            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.W,
                            true);
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

            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.A,
                            true);
            runtime.playerMotionControl(
                    player);
            final double diagonal =
                    Minecraft189FlightModule.HORIZONTAL_MOTION
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
            runtime.playerRotationState()
                    .update(
                            90.0F);
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    -Minecraft189FlightModule.HORIZONTAL_MOTION,
                    player.motionX,
                    0.000000001D);
            assertEquals(
                    0.0D,
                    player.motionZ,
                    0.000000001D);

            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.W,
                            false);
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    0.0D,
                    player.motionX,
                    0.000000001D);
            assertEquals(
                    0.0D,
                    player.motionZ,
                    0.000000001D);

            runtime.featureCatalog()
                    .flight()
                    .horizontalSpeedSetting()
                    .set(
                            0.60D);
            runtime.featureCatalog()
                    .flight()
                    .verticalSpeedSetting()
                    .set(
                            0.45D);
            runtime.playerRotationState()
                    .update(
                            0.0F);
            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.W,
                            true);
            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.SPACE,
                            true);
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    0.0D,
                    player.motionX,
                    0.000000001D);
            assertEquals(
                    0.45D,
                    player.motionY,
                    0.000000001D);
            assertEquals(
                    0.60D,
                    player.motionZ,
                    0.000000001D);

            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.W,
                            false);
            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.SPACE,
                            false);

            controller.disable(
                    Minecraft189FlightModule.ID);
            assertFalse(
                    runtime.featureCatalog()
                            .flight()
                            .active());

            player.motionX = 0.11D;
            player.motionY = 0.42D;
            player.motionZ = -0.17D;
            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.SPACE,
                            true);
            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.W,
                            true);
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    0.11D,
                    player.motionX,
                    0.000000001D);
            assertEquals(
                    0.42D,
                    player.motionY,
                    0.000000001D);
            assertEquals(
                    -0.17D,
                    player.motionZ,
                    0.000000001D);

            runtime.playerMotionControl(
                    null);
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189FlightModule.ID));
        assertNull(
                settings.find(
                        Minecraft189FlightModule.HORIZONTAL_SPEED_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189FlightModule.VERTICAL_SPEED_SETTING_ID));
        assertNull(settings.find(
                Minecraft189FlightModule.SPRINT_BOOST_SETTING_ID));
        assertNull(settings.find(
                Minecraft189FlightModule.SPRINT_MULTIPLIER_SETTING_ID));
        assertNull(
                categories.find(
                        Minecraft189FeatureCatalog.MOVEMENT_CATEGORY_ID));
    }

    @Test
    void flightSprintBoostScalesHorizontalOnlyWhenMappedSprinting() {
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
            final Minecraft189FlightModule flight = runtime.featureCatalog().flight();
            assertFalse(flight.sprintBoostSetting().get().booleanValue());
            assertEquals(Minecraft189FlightModule.DEFAULT_SPRINT_MULTIPLIER,
                    flight.sprintMultiplierSetting().get().doubleValue(), 0.000001D);
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189FlightModule.SPRINT_BOOST_SETTING_ID));
            assertEquals("1.5", settings.snapshotEncoded().get(
                    Minecraft189FlightModule.SPRINT_MULTIPLIER_SETTING_ID));

            final TestPlayer player = new TestPlayer();
            controller.enable(Minecraft189FlightModule.ID);
            runtime.playerRotationState().update(0.0F);
            runtime.playerMovementState().update(true, false, true);
            runtime.inputState().key(LegacyKeyboardCodes.W, true);
            runtime.playerMotionControl(player);
            // Default OFF must ignore even a known sprinting snapshot.
            assertEquals(0.30D, player.motionZ, 0.000000001D);
            assertEquals(0.0D, player.motionX, 0.000000001D);
            assertEquals(0.0D, player.motionY, 0.000000001D);

            flight.sprintBoostSetting().set(Boolean.TRUE);
            flight.sprintMultiplierSetting().set(2.0D);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189FlightModule.SPRINT_BOOST_SETTING_ID));
            assertEquals("2.0", settings.snapshotEncoded().get(
                    Minecraft189FlightModule.SPRINT_MULTIPLIER_SETTING_ID));
            runtime.playerMotionControl(player);
            assertEquals(0.60D, player.motionZ, 0.000000001D);
            assertEquals(0.0D, player.motionY, 0.000000001D);
            final int unchangedWrites = player.setCalls;
            runtime.playerMotionControl(player);
            assertEquals(unchangedWrites, player.setCalls);

            // A second movement axis preserves normalized diagonal speed.
            runtime.inputState().key(LegacyKeyboardCodes.A, true);
            runtime.inputState().key(LegacyKeyboardCodes.SPACE, true);
            runtime.playerMotionControl(player);
            assertEquals(0.60D / Math.sqrt(2.0D), player.motionX, 0.000000001D);
            assertEquals(0.60D / Math.sqrt(2.0D), player.motionZ, 0.000000001D);
            assertEquals(0.30D, player.motionY, 0.000000001D);
            flight.sprintMultiplierSetting().set(3.0D);
            runtime.playerMotionControl(player);
            assertEquals(0.90D / Math.sqrt(2.0D), player.motionX, 0.000000001D);
            assertEquals(0.90D / Math.sqrt(2.0D), player.motionZ, 0.000000001D);
            assertEquals(0.30D, player.motionY, 0.000000001D);

            runtime.inputState().key(LegacyKeyboardCodes.A, false);
            runtime.inputState().key(LegacyKeyboardCodes.SPACE, false);
            runtime.playerMovementState().update(true, false, false);
            runtime.playerMotionControl(player);
            assertEquals(0.30D, player.motionZ, 0.000000001D);
            assertEquals(0.0D, player.motionY, 0.000000001D);
            runtime.playerMovementState().clear();
            runtime.playerMotionControl(player);
            assertEquals(0.30D, player.motionZ, 0.000000001D);

            runtime.playerMovementState().update(false, false, true);
            runtime.playerMotionControl(player);
            assertEquals(0.90D, player.motionZ, 0.000000001D);
            flight.sprintBoostSetting().set(Boolean.FALSE);
            runtime.playerMotionControl(player);
            assertEquals(0.30D, player.motionZ, 0.000000001D);

            assertThrows(IllegalArgumentException.class,
                    () -> flight.sprintMultiplierSetting().set(0.99D));
            assertThrows(IllegalArgumentException.class,
                    () -> flight.sprintMultiplierSetting().set(3.01D));
            assertThrows(IllegalArgumentException.class,
                    () -> flight.sprintMultiplierSetting().set(Double.NaN));
            assertThrows(IllegalArgumentException.class,
                    () -> flight.sprintMultiplierSetting().set(Double.POSITIVE_INFINITY));
            controller.disable(Minecraft189FlightModule.ID);
            player.motionZ = 0.13D;
            runtime.playerMotionControl(player);
            assertEquals(0.13D, player.motionZ, 0.000000001D);
        } finally {
            runtime.close();
        }
        assertNull(settings.find(Minecraft189FlightModule.SPRINT_BOOST_SETTING_ID));
        assertNull(settings.find(Minecraft189FlightModule.SPRINT_MULTIPLIER_SETTING_ID));
        assertNull(modules.find(Minecraft189FlightModule.ID));
    }

    @Test
    void oldFlightApplyOverloadUsesNormalSpeedWithNoMovementAuthority() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189FlightModule flight = new Minecraft189FlightModule(input);
        final Minecraft189PlayerRotationState rotation = new Minecraft189PlayerRotationState();
        final Minecraft189PlayerMovementState movement = new Minecraft189PlayerMovementState();
        final TestPlayer player = new TestPlayer();
        rotation.update(0.0F);
        input.key(LegacyKeyboardCodes.W, true);
        flight.onEnable();
        flight.sprintBoostSetting().set(Boolean.TRUE);
        flight.sprintMultiplierSetting().set(2.0D);
        flight.apply(player, rotation.snapshot());
        assertEquals(0.30D, player.motionZ, 0.000000001D);
        movement.update(true, false, true);
        flight.apply(player, rotation.snapshot(), movement.snapshot());
        assertEquals(0.60D, player.motionZ, 0.000000001D);
        flight.apply(player, rotation.snapshot());
        assertEquals(0.30D, player.motionZ, 0.000000001D);
        flight.onDisable();
        player.motionZ = 0.20D;
        flight.apply(player, rotation.snapshot(), movement.snapshot());
        assertEquals(0.20D, player.motionZ, 0.000000001D);
    }

    @Test
    void optionalVerticalSmoothingConvergesWithoutOvershoot() {
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
            final Minecraft189FlightModule flight = runtime.featureCatalog().flight();
            assertFalse(flight.smoothVerticalSetting().get().booleanValue());
            assertEquals(Minecraft189FlightModule.DEFAULT_VERTICAL_STEP,
                    flight.verticalStepSetting().get().doubleValue(), 0.000001D);
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189FlightModule.SMOOTH_VERTICAL_SETTING_ID));
            controller.enable(Minecraft189FlightModule.ID);
            final TestPlayer player = new TestPlayer();
            runtime.inputState().key(LegacyKeyboardCodes.SPACE, true);
            runtime.playerMotionControl(player);
            assertEquals(0.30D, player.motionY, 0.000001D); // Default instant.

            flight.smoothVerticalSetting().set(Boolean.TRUE);
            flight.verticalStepSetting().set(0.10D);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189FlightModule.SMOOTH_VERTICAL_SETTING_ID));
            assertEquals("0.1", settings.snapshotEncoded().get(
                    Minecraft189FlightModule.VERTICAL_STEP_SETTING_ID));
            player.motionY = 0.0D;
            runtime.playerMotionControl(player);
            assertEquals(0.10D, player.motionY, 0.000001D);
            runtime.playerMotionControl(player);
            assertEquals(0.20D, player.motionY, 0.000001D);
            runtime.playerMotionControl(player);
            assertEquals(0.30D, player.motionY, 0.000001D);
            final int steadyCalls = player.setCalls;
            runtime.playerMotionControl(player);
            assertEquals(steadyCalls, player.setCalls); // No steady-state writes.

            runtime.inputState().key(LegacyKeyboardCodes.LEFT_SHIFT, true);
            runtime.playerMotionControl(player);
            assertEquals(0.20D, player.motionY, 0.000001D);
            runtime.playerMotionControl(player);
            runtime.playerMotionControl(player);
            assertEquals(0.0D, player.motionY, 0.000001D); // Hover convergence.

            runtime.inputState().key(LegacyKeyboardCodes.SPACE, false);
            runtime.playerMotionControl(player);
            assertEquals(-0.10D, player.motionY, 0.000001D);
            runtime.playerMotionControl(player);
            runtime.playerMotionControl(player);
            assertEquals(-0.30D, player.motionY, 0.000001D);

            flight.verticalStepSetting().set(0.20D); // Live change.
            runtime.inputState().key(LegacyKeyboardCodes.LEFT_SHIFT, false);
            runtime.inputState().key(LegacyKeyboardCodes.SPACE, true);
            runtime.playerMotionControl(player);
            assertEquals(-0.10D, player.motionY, 0.000001D);
            flight.smoothVerticalSetting().set(Boolean.FALSE);
            runtime.playerMotionControl(player);
            assertEquals(0.30D, player.motionY, 0.000001D); // Instant restored.

            assertThrows(IllegalArgumentException.class,
                    () -> flight.verticalStepSetting().set(Double.NaN));
            assertThrows(IllegalArgumentException.class,
                    () -> flight.verticalStepSetting().set(Double.POSITIVE_INFINITY));
            assertThrows(IllegalArgumentException.class,
                    () -> flight.verticalStepSetting().set(0.009D));
            assertThrows(IllegalArgumentException.class,
                    () -> flight.verticalStepSetting().set(0.501D));

            flight.smoothVerticalSetting().set(Boolean.TRUE);
            player.motionY = Double.NaN;
            final int before = player.setCalls;
            runtime.playerMotionControl(player);
            assertEquals(before, player.setCalls); // Unknown source: no write.
            controller.disable(Minecraft189FlightModule.ID);
            player.motionY = 0.0D;
            runtime.playerMotionControl(player);
            assertEquals(0.0D, player.motionY, 0.000001D);
        } finally {
            runtime.close();
        }
        assertNull(settings.find(
                Minecraft189FlightModule.SMOOTH_VERTICAL_SETTING_ID));
        assertNull(settings.find(
                Minecraft189FlightModule.VERTICAL_STEP_SETTING_ID));
        assertNull(modules.find(Minecraft189FlightModule.ID));
    }

    private static final class TestPlayer
            implements Minecraft189PlayerMotionControl {
        private double motionX;
        private double motionY;
        private double motionZ;
        private int setCalls;

        @Override
        public double customMcMotionX() {
            return motionX;
        }

        @Override
        public void customMcSetMotionX(
                final double motionX) {
            setCalls++;
            this.motionX = motionX;
        }

        @Override
        public double customMcMotionY() {
            return motionY;
        }

        @Override
        public void customMcSetMotionY(
                final double motionY) {
            setCalls++;
            this.motionY = motionY;
        }

        @Override
        public double customMcMotionZ() {
            return motionZ;
        }

        @Override
        public void customMcSetMotionZ(
                final double motionZ) {
            setCalls++;
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
