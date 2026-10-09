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

final class Minecraft189GlideModuleTest {
    @Test
    void glideCapsOnlyExcessiveAirborneDescentAndYieldsToFlight() {
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
                            Minecraft189GlideModule.ID));
            assertNotNull(
                    settings.find(
                            Minecraft189GlideModule.FALL_SPEED_SETTING_ID));
            assertNotNull(settings.find(Minecraft189GlideModule.REQUIRE_SNEAKING_SETTING_ID));
            assertFalse(runtime.featureCatalog().glide()
                    .requireSneakingSetting().get().booleanValue());
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189GlideModule.REQUIRE_SNEAKING_SETTING_ID));
            assertEquals(
                    Minecraft189GlideModule.DEFAULT_FALL_SPEED,
                    runtime.featureCatalog()
                            .glide()
                            .fallSpeedSetting()
                            .get()
                            .doubleValue(),
                    0.000000001D);

            final TestPlayer player =
                    new TestPlayer();
            player.motionY = -0.40D;

            controller.enable(
                    Minecraft189GlideModule.ID);
            assertTrue(
                    runtime.featureCatalog()
                            .glide()
                            .active());

            runtime.playerMotionControl(
                    player);
            assertEquals(
                    -0.40D,
                    player.motionY,
                    0.000000001D);
            assertEquals(
                    0,
                    player.verticalSetCalls);

            runtime.playerMovementState()
                    .update(
                            true,
                            false,
                            false);
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    -0.40D,
                    player.motionY,
                    0.000000001D);
            assertEquals(
                    0,
                    player.verticalSetCalls);

            runtime.playerMovementState()
                    .update(
                            false,
                            false,
                            false);
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    -Minecraft189GlideModule.DEFAULT_FALL_SPEED,
                    player.motionY,
                    0.000000001D);
            assertEquals(
                    1,
                    player.verticalSetCalls);

            player.motionY = -0.03D;
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    -0.03D,
                    player.motionY,
                    0.000000001D);
            assertEquals(
                    1,
                    player.verticalSetCalls);

            player.motionY = 0.20D;
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    0.20D,
                    player.motionY,
                    0.000000001D);
            assertEquals(
                    1,
                    player.verticalSetCalls);

            runtime.featureCatalog()
                    .glide()
                    .fallSpeedSetting()
                    .set(
                            0.15D);
            player.motionY = -0.40D;
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    -0.15D,
                    player.motionY,
                    0.000000001D);

            player.motionY = -0.40D;
            controller.enable(
                    Minecraft189FlightModule.ID);
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    Minecraft189FlightModule.HOVER_MOTION_Y,
                    player.motionY,
                    0.000000001D);

            controller.disable(
                    Minecraft189FlightModule.ID);
            player.motionY = -0.40D;
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    -0.15D,
                    player.motionY,
                    0.000000001D);

            // Optional sneak-to-glide gate: default OFF is unchanged.
            final Minecraft189GlideModule glide = runtime.featureCatalog().glide();
            glide.requireSneakingSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189GlideModule.REQUIRE_SNEAKING_SETTING_ID));
            int writesBeforeGate = player.verticalSetCalls;
            player.motionY = -0.40D;
            runtime.playerMovementState().update(false, false, false);
            runtime.playerMotionControl(player);
            assertEquals(-0.40D, player.motionY, 0.000000001D);
            assertEquals(writesBeforeGate, player.verticalSetCalls);

            runtime.playerMovementState().update(false, true, false);
            runtime.playerMotionControl(player);
            assertEquals(-0.15D, player.motionY, 0.000000001D);
            assertEquals(writesBeforeGate + 1, player.verticalSetCalls);

            // Do not write again when the current motion is already capped.
            runtime.playerMotionControl(player);
            assertEquals(writesBeforeGate + 1, player.verticalSetCalls);

            player.motionY = -0.40D;
            runtime.playerMovementState().update(true, true, false);
            runtime.playerMotionControl(player);
            assertEquals(-0.40D, player.motionY, 0.000000001D);

            runtime.playerMovementState().clear();
            runtime.playerMotionControl(player);
            assertEquals(-0.40D, player.motionY, 0.000000001D);

            // Toggle OFF immediately restores original airborne behavior.
            runtime.playerMovementState().update(false, false, false);
            glide.requireSneakingSetting().set(Boolean.FALSE);
            runtime.playerMotionControl(player);
            assertEquals(-0.15D, player.motionY, 0.000000001D);
            player.motionY = Double.NaN;
            final int beforeInvalid = player.verticalSetCalls;
            runtime.playerMotionControl(player);
            assertEquals(beforeInvalid, player.verticalSetCalls);
            player.motionY = Double.NEGATIVE_INFINITY;
            runtime.playerMotionControl(player);
            assertEquals(beforeInvalid, player.verticalSetCalls);

            // Suspension priority is unchanged even while sneak gate is ON.
            glide.requireSneakingSetting().set(Boolean.TRUE);
            runtime.playerMovementState().update(false, true, false);
            controller.enable(Minecraft189FlightModule.ID);
            player.motionY = -0.40D;
            runtime.playerMotionControl(player);
            assertEquals(Minecraft189FlightModule.HOVER_MOTION_Y,
                    player.motionY, 0.000000001D);
            controller.disable(Minecraft189FlightModule.ID);

            controller.disable(
                    Minecraft189GlideModule.ID);
            assertFalse(
                    runtime.featureCatalog()
                            .glide()
                            .active());
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
                        Minecraft189GlideModule.ID));
        assertNull(
                settings.find(
                        Minecraft189GlideModule.FALL_SPEED_SETTING_ID));
        assertNull(settings.find(
                Minecraft189GlideModule.REQUIRE_SNEAKING_SETTING_ID));
        assertNull(
                categories.find(
                        Minecraft189FeatureCatalog.MOVEMENT_CATEGORY_ID));
    }

    @Test
    void optionalProgressiveGlideDeceleratesWithoutOvershootOrBufferedCredit() {
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
            final Minecraft189GlideModule glide = runtime.featureCatalog().glide();
            final TestPlayer player = new TestPlayer();
            assertFalse(glide.progressiveDecelerationSetting().get().booleanValue());
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189GlideModule.PROGRESSIVE_SETTING_ID));
            assertEquals("0.1", settings.snapshotEncoded().get(
                    Minecraft189GlideModule.DECELERATION_STEP_SETTING_ID));
            controller.enable(Minecraft189GlideModule.ID);
            runtime.playerMovementState().update(false, false, false);
            player.motionY = -0.40D;
            runtime.playerMotionControl(player);
            assertEquals(-0.08D, player.motionY, 0.000000001D); // Original instant.

            glide.progressiveDecelerationSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189GlideModule.PROGRESSIVE_SETTING_ID));
            player.motionY = -0.40D;
            runtime.playerMotionControl(player);
            assertEquals(-0.30D, player.motionY, 0.000000001D);
            runtime.playerMotionControl(player);
            assertEquals(-0.20D, player.motionY, 0.000000001D);
            runtime.playerMotionControl(player);
            assertEquals(-0.10D, player.motionY, 0.000000001D);
            runtime.playerMotionControl(player);
            assertEquals(-0.08D, player.motionY, 0.000000001D);
            final int writesAtCap = player.verticalSetCalls;
            runtime.playerMotionControl(player);
            assertEquals(writesAtCap, player.verticalSetCalls);

            glide.decelerationStepSetting().set(0.05D);
            assertEquals("0.05", settings.snapshotEncoded().get(
                    Minecraft189GlideModule.DECELERATION_STEP_SETTING_ID));
            player.motionY = -0.30D;
            runtime.playerMotionControl(player);
            assertEquals(-0.25D, player.motionY, 0.000000001D);
            // Ground and unavailable movement suspend without catch-up.
            runtime.playerMovementState().update(true, false, false);
            runtime.playerMotionControl(player);
            assertEquals(-0.25D, player.motionY, 0.000000001D);
            runtime.playerMovementState().clear();
            runtime.playerMotionControl(player);
            assertEquals(-0.25D, player.motionY, 0.000000001D);
            runtime.playerMovementState().update(false, false, false);
            runtime.playerMotionControl(player);
            assertEquals(-0.20D, player.motionY, 0.000000001D);

            // Sneak-only gating and Flight priority remain authoritative.
            glide.requireSneakingSetting().set(Boolean.TRUE);
            runtime.playerMotionControl(player);
            assertEquals(-0.20D, player.motionY, 0.000000001D);
            runtime.playerMovementState().update(false, true, false);
            runtime.playerMotionControl(player);
            assertEquals(-0.15D, player.motionY, 0.000000001D);
            controller.enable(Minecraft189FlightModule.ID);
            runtime.playerMotionControl(player);
            assertEquals(Minecraft189FlightModule.HOVER_MOTION_Y,
                    player.motionY, 0.000000001D);
            controller.disable(Minecraft189FlightModule.ID);

            player.motionY = Double.NEGATIVE_INFINITY;
            final int beforeInvalid = player.verticalSetCalls;
            runtime.playerMotionControl(player);
            assertEquals(beforeInvalid, player.verticalSetCalls);
            player.motionY = -0.03D;
            runtime.playerMotionControl(player);
            assertEquals(-0.03D, player.motionY, 0.000000001D);

            glide.requireSneakingSetting().set(Boolean.FALSE);
            glide.progressiveDecelerationSetting().set(Boolean.FALSE);
            player.motionY = -0.40D;
            runtime.playerMotionControl(player);
            assertEquals(-0.08D, player.motionY, 0.000000001D);
            controller.disable(Minecraft189GlideModule.ID);
            player.motionY = -0.40D;
            final int writesBeforeDisabled = player.verticalSetCalls;
            runtime.playerMotionControl(player);
            assertEquals(writesBeforeDisabled, player.verticalSetCalls);
            assertThrows(IllegalArgumentException.class,
                    () -> glide.decelerationStepSetting().set(0.009D));
            assertThrows(IllegalArgumentException.class,
                    () -> glide.decelerationStepSetting().set(0.501D));
            assertThrows(IllegalArgumentException.class,
                    () -> glide.decelerationStepSetting().set(Double.NaN));
        } finally {
            runtime.close();
        }
        assertNull(settings.find(Minecraft189GlideModule.PROGRESSIVE_SETTING_ID));
        assertNull(settings.find(
                Minecraft189GlideModule.DECELERATION_STEP_SETTING_ID));
        assertNull(modules.find(Minecraft189GlideModule.ID));
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
