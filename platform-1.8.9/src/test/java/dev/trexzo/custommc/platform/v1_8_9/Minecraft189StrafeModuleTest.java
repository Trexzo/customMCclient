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

final class Minecraft189StrafeModuleTest {
    @Test
    void strafeUsesMappedYawAndYieldsToFlight() {
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
                            Minecraft189StrafeModule.ID));
            assertNotNull(
                    settings.find(
                            Minecraft189StrafeModule.SPEED_SETTING_ID));
            assertNotNull(settings.find(
                    Minecraft189StrafeModule.SMOOTH_ACCELERATION_SETTING_ID));
            assertNotNull(settings.find(
                    Minecraft189StrafeModule.ACCELERATION_PERCENT_SETTING_ID));
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189StrafeModule.SMOOTH_ACCELERATION_SETTING_ID));
            assertEquals("50", settings.snapshotEncoded().get(
                    Minecraft189StrafeModule.ACCELERATION_PERCENT_SETTING_ID));
            assertEquals(
                    Minecraft189StrafeModule.DEFAULT_SPEED,
                    runtime.featureCatalog()
                            .strafe()
                            .speedSetting()
                            .get()
                            .doubleValue(),
                    0.000000001D);

            final TestPlayer player =
                    new TestPlayer();
            player.motionX = 0.11D;
            player.motionZ = -0.17D;

            controller.enable(
                    Minecraft189StrafeModule.ID);
            assertTrue(
                    runtime.featureCatalog()
                            .strafe()
                            .active());
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
                    -0.17D,
                    player.motionZ,
                    0.000000001D);
            assertEquals(
                    0,
                    player.horizontalSetCalls);

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
                    Minecraft189StrafeModule.DEFAULT_SPEED,
                    player.motionZ,
                    0.000000001D);

            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.A,
                            true);
            runtime.playerMotionControl(
                    player);
            final double diagonal =
                    Minecraft189StrafeModule.DEFAULT_SPEED
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
                    .strafe()
                    .speedSetting()
                    .set(
                            0.55D);
            runtime.playerRotationState()
                    .update(
                            90.0F);
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

            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.W,
                            false);
            player.motionX = 0.12D;
            player.motionZ = 0.13D;
            final int beforeNoInput =
                    player.horizontalSetCalls;
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    0.12D,
                    player.motionX,
                    0.000000001D);
            assertEquals(
                    0.13D,
                    player.motionZ,
                    0.000000001D);
            assertEquals(
                    beforeNoInput,
                    player.horizontalSetCalls);

            runtime.featureCatalog()
                    .flight()
                    .horizontalSpeedSetting()
                    .set(
                            0.60D);
            runtime.playerRotationState()
                    .update(
                            0.0F);
            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.W,
                            true);
            controller.enable(
                    Minecraft189FlightModule.ID);
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    0.0D,
                    player.motionX,
                    0.000000001D);
            assertEquals(
                    0.60D,
                    player.motionZ,
                    0.000000001D);

            controller.disable(
                    Minecraft189FlightModule.ID);
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    0.0D,
                    player.motionX,
                    0.000000001D);
            assertEquals(
                    0.55D,
                    player.motionZ,
                    0.000000001D);

            controller.disable(
                    Minecraft189StrafeModule.ID);
            assertFalse(
                    runtime.featureCatalog()
                            .strafe()
                            .active());
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

            runtime.playerMotionControl(
                    null);
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189StrafeModule.ID));
        assertNull(
                settings.find(
                        Minecraft189StrafeModule.SPEED_SETTING_ID));
        assertNull(settings.find(
                Minecraft189StrafeModule.SMOOTH_ACCELERATION_SETTING_ID));
        assertNull(settings.find(
                Minecraft189StrafeModule.ACCELERATION_PERCENT_SETTING_ID));
        assertNull(
                categories.find(
                        Minecraft189FeatureCatalog.MOVEMENT_CATEGORY_ID));
    }

    @Test
    void smoothStrafeBlendsLiveHorizontalMotionWithoutChangingInstantDefault() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189StrafeModule module = new Minecraft189StrafeModule(input);
        final Minecraft189PlayerRotationState rotation =
                new Minecraft189PlayerRotationState();
        final TestPlayer player = new TestPlayer();
        rotation.update(0.0F);
        module.onEnable();
        input.key(LegacyKeyboardCodes.W, true);

        // Default OFF: previous immediate yaw-relative behavior is preserved.
        assertFalse(module.smoothAccelerationSetting().get().booleanValue());
        assertEquals(Integer.valueOf(50), module.accelerationPercentSetting().get());
        module.apply(player, rotation.snapshot(), false);
        assertEquals(0.0D, player.motionX, 0.000000001D);
        assertEquals(0.30D, player.motionZ, 0.000000001D);

        player.motionZ = 0.0D;
        module.smoothAccelerationSetting().set(Boolean.TRUE);
        module.apply(player, rotation.snapshot(), false);
        assertEquals(0.15D, player.motionZ, 0.000000001D);
        module.apply(player, rotation.snapshot(), false);
        assertEquals(0.225D, player.motionZ, 0.000000001D);

        module.accelerationPercentSetting().set(100);
        module.apply(player, rotation.snapshot(), false);
        assertEquals(0.30D, player.motionZ, 0.000000001D);

        module.speedSetting().set(0.60D);
        module.accelerationPercentSetting().set(10);
        module.apply(player, rotation.snapshot(), false);
        assertEquals(0.33D, player.motionZ, 0.000000001D);

        // The same fraction smooths a rapid direction reversal.
        module.accelerationPercentSetting().set(50);
        input.key(LegacyKeyboardCodes.W, false);
        input.key(LegacyKeyboardCodes.S, true);
        module.apply(player, rotation.snapshot(), false);
        assertEquals(-0.135D, player.motionZ, 0.000000001D);

        // When input stops, retain the existing no-write semantics.
        input.key(LegacyKeyboardCodes.S, false);
        final int writesAtRelease = player.horizontalSetCalls;
        module.apply(player, rotation.snapshot(), false);
        assertEquals(writesAtRelease, player.horizontalSetCalls);

        input.key(LegacyKeyboardCodes.D, true);
        rotation.update(90.0F);
        module.apply(player, rotation.snapshot(), false);
        // D at yaw=90 yields positive Z; X converges toward zero.
        assertEquals(0.0D, player.motionX, 0.000000001D);
        assertEquals(0.2325D, player.motionZ, 0.000000001D);

        // Other movement owners suspend Strafe entirely.
        final int writesBeforeSuspend = player.horizontalSetCalls;
        module.apply(player, rotation.snapshot(), true);
        assertEquals(writesBeforeSuspend, player.horizontalSetCalls);
        module.smoothAccelerationSetting().set(Boolean.FALSE);
        module.apply(player, rotation.snapshot(), false);
        assertEquals(0.60D, player.motionZ, 0.000000001D);

        module.onDisable();
        player.motionZ = 0.17D;
        module.apply(player, rotation.snapshot(), false);
        assertEquals(0.17D, player.motionZ, 0.000000001D);
        assertThrows(IllegalArgumentException.class,
                () -> module.accelerationPercentSetting().set(9));
        assertThrows(IllegalArgumentException.class,
                () -> module.accelerationPercentSetting().set(101));
    }

    @Test
    void smoothStrafeRepairsNonfiniteMotionAndConvergesExactly() {
        assertEquals(0.30D, Minecraft189StrafeModule.interpolate(
                Double.NaN, 0.30D, 0.50D), 0.000000001D);
        assertEquals(-0.30D, Minecraft189StrafeModule.interpolate(
                Double.NEGATIVE_INFINITY, -0.30D, 0.50D), 0.000000001D);
        assertEquals(0.30D, Minecraft189StrafeModule.interpolate(
                Double.POSITIVE_INFINITY, 0.30D, 0.50D), 0.000000001D);
        assertEquals(0.30D, Minecraft189StrafeModule.interpolate(
                0.10D, 0.30D, 1.0D), 0.000000001D);
        assertEquals(0.20D, Minecraft189StrafeModule.interpolate(
                0.10D, 0.30D, 0.50D), 0.000000001D);
        assertEquals(0.30D, Minecraft189StrafeModule.interpolate(
                0.2999997D, 0.30D, 0.10D), 0.000000001D);

        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189StrafeModule module = new Minecraft189StrafeModule(input);
        final Minecraft189PlayerRotationState rotation =
                new Minecraft189PlayerRotationState();
        final TestPlayer player = new TestPlayer();
        rotation.update(0.0F);
        module.onEnable();
        module.smoothAccelerationSetting().set(Boolean.TRUE);
        player.motionX = Double.NaN;
        player.motionZ = Double.POSITIVE_INFINITY;
        input.key(LegacyKeyboardCodes.W, true);
        module.apply(player, rotation.snapshot(), false);
        assertEquals(0.0D, player.motionX, 0.000000001D);
        assertEquals(0.30D, player.motionZ, 0.000000001D);
        module.onDisable();
    }

    private static final class TestPlayer
            implements Minecraft189PlayerMotionControl {
        private double motionX;
        private double motionY;
        private double motionZ;
        private int horizontalSetCalls;

        @Override
        public double customMcMotionX() {
            return motionX;
        }

        @Override
        public void customMcSetMotionX(
                final double motionX) {
            horizontalSetCalls++;
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
            horizontalSetCalls++;
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
