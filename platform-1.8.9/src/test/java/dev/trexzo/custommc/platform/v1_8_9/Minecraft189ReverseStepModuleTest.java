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

final class Minecraft189ReverseStepModuleTest {
    @Test
    void reverseStepOnlySnapsRealDownwardGroundToAirTransitions() {
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
                            Minecraft189ReverseStepModule.ID));
            assertNotNull(
                    settings.find(
                            Minecraft189ReverseStepModule.SPEED_SETTING_ID));
            assertEquals("0", settings.snapshotEncoded().get(
                    Minecraft189ReverseStepModule.DELAY_TICKS_SETTING_ID));
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189ReverseStepModule.REQUIRE_SNEAK_SETTING_ID));

            final TestPlayer player =
                    new TestPlayer();
            controller.enable(
                    Minecraft189ReverseStepModule.ID);
            assertTrue(
                    runtime.featureCatalog()
                            .reverseStep()
                            .active());

            runtime.playerMovementState()
                    .update(
                            true,
                            false,
                            false);
            player.motionY = 0.0D;
            runtime.playerMotionControl(
                    player);
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
                    -Minecraft189ReverseStepModule.DEFAULT_SPEED,
                    player.motionY,
                    0.000000001D);
            assertEquals(
                    1,
                    player.verticalSetCalls);

            player.motionY = 0.0D;
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    0.0D,
                    player.motionY,
                    0.000000001D);
            assertEquals(
                    1,
                    player.verticalSetCalls);

            runtime.playerMovementState()
                    .update(
                            true,
                            false,
                            false);
            runtime.playerMotionControl(
                    player);
            runtime.playerMovementState()
                    .update(
                            false,
                            false,
                            false);
            player.motionY = 0.42D;
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    0.42D,
                    player.motionY,
                    0.000000001D);
            assertEquals(
                    1,
                    player.verticalSetCalls);

            runtime.playerMovementState()
                    .update(
                            true,
                            false,
                            false);
            runtime.playerMotionControl(
                    player);
            runtime.playerMovementState()
                    .update(
                            false,
                            false,
                            false);
            player.motionY = -0.80D;
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    -0.80D,
                    player.motionY,
                    0.000000001D);
            assertEquals(
                    1,
                    player.verticalSetCalls);

            runtime.featureCatalog()
                    .reverseStep()
                    .speedSetting()
                    .set(
                            0.75D);
            runtime.playerMovementState()
                    .update(
                            true,
                            false,
                            false);
            runtime.playerMotionControl(
                    player);
            runtime.playerMovementState()
                    .update(
                            false,
                            false,
                            false);
            player.motionY = -0.10D;
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    -0.75D,
                    player.motionY,
                    0.000000001D);
            assertEquals(
                    2,
                    player.verticalSetCalls);

            controller.enable(
                    Minecraft189NoGravityModule.ID);
            runtime.playerMovementState()
                    .update(
                            true,
                            false,
                            false);
            runtime.playerMotionControl(
                    player);
            runtime.playerMovementState()
                    .update(
                            false,
                            false,
                            false);
            player.motionY = -0.10D;
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    0.0D,
                    player.motionY,
                    0.000000001D);

            controller.disable(
                    Minecraft189NoGravityModule.ID);
            player.motionY = -0.10D;
            final int beforeDelayed =
                    player.verticalSetCalls;
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    -0.10D,
                    player.motionY,
                    0.000000001D);
            assertEquals(
                    beforeDelayed,
                    player.verticalSetCalls);

            controller.disable(
                    Minecraft189ReverseStepModule.ID);
            assertFalse(
                    runtime.featureCatalog()
                            .reverseStep()
                            .active());

            runtime.playerMovementState()
                    .update(
                            true,
                            false,
                            false);
            runtime.playerMotionControl(
                    player);
            runtime.playerMovementState()
                    .update(
                            false,
                            false,
                            false);
            player.motionY = 0.0D;
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    0.0D,
                    player.motionY,
                    0.000000001D);

            runtime.playerMotionControl(
                    null);
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189ReverseStepModule.ID));
        assertNull(
                settings.find(
                        Minecraft189ReverseStepModule.SPEED_SETTING_ID));
        assertNull(settings.find(Minecraft189ReverseStepModule.DELAY_TICKS_SETTING_ID));
        assertNull(settings.find(Minecraft189ReverseStepModule.REQUIRE_SNEAK_SETTING_ID));
        assertNull(
                categories.find(
                        Minecraft189FeatureCatalog.MOVEMENT_CATEGORY_ID));
    }

    @Test
    void delayedReverseStepUsesOneRealDepartureAndCancelsPendingState() {
        final Minecraft189ReverseStepModule step =
                new Minecraft189ReverseStepModule();
        final Minecraft189PlayerMovementState movement =
                new Minecraft189PlayerMovementState();
        final TestPlayer player = new TestPlayer();
        assertEquals(Integer.valueOf(0), step.delayTicksSetting().get());
        assertFalse(step.requireSneakingSetting().get().booleanValue());
        step.onEnable();
        movement.update(true, false, false);
        assertFalse(step.apply(player, movement.snapshot(), false));
        movement.update(false, false, false);
        player.motionY = -0.10D;
        assertTrue(step.apply(player, movement.snapshot(), false));
        assertEquals(-0.50D, player.motionY, 0.000000001D);
        assertEquals(1, player.verticalSetCalls);
        // Legacy zero delay always consumes the observed edge once.
        player.motionY = -0.10D;
        assertFalse(step.apply(player, movement.snapshot(), false));
        assertEquals(1, player.verticalSetCalls);

        step.delayTicksSetting().set(2);
        movement.update(true, false, false);
        assertFalse(step.apply(player, movement.snapshot(), false));
        movement.update(false, false, false);
        player.motionY = -0.10D;
        assertFalse(step.apply(player, movement.snapshot(), false)); // first skipped air
        assertFalse(step.apply(player, movement.snapshot(), false)); // second skipped air
        assertEquals(-0.10D, player.motionY, 0.000000001D);
        assertTrue(step.apply(player, movement.snapshot(), false)); // third eligible air
        assertEquals(-0.50D, player.motionY, 0.000000001D);
        assertEquals(2, player.verticalSetCalls);
        player.motionY = -0.10D;
        assertFalse(step.apply(player, movement.snapshot(), false));
        assertEquals(2, player.verticalSetCalls);

        // Ground contact cancels a delayed drop; an actual subsequent
        // ground-to-air transition must start a completely new countdown.
        movement.update(true, false, false);
        step.apply(player, movement.snapshot(), false);
        movement.update(false, false, false);
        assertFalse(step.apply(player, movement.snapshot(), false)); // pending 1
        movement.update(true, false, false);
        assertFalse(step.apply(player, movement.snapshot(), false)); // cancel
        movement.update(false, false, false);
        assertFalse(step.apply(player, movement.snapshot(), false)); // new 1
        assertFalse(step.apply(player, movement.snapshot(), false)); // new 2
        assertEquals(2, player.verticalSetCalls);
        assertTrue(step.apply(player, movement.snapshot(), false));
        assertEquals(3, player.verticalSetCalls);

        // Suspension while a drop is pending cancels it; resuming while
        // still airborne must not replay the lost edge.
        movement.update(true, false, false);
        step.apply(player, movement.snapshot(), false);
        movement.update(false, false, false);
        assertFalse(step.apply(player, movement.snapshot(), false));
        assertFalse(step.apply(player, movement.snapshot(), true));
        assertFalse(step.apply(player, movement.snapshot(), false));
        assertFalse(step.apply(player, movement.snapshot(), false));
        assertEquals(3, player.verticalSetCalls);

        // Disabling/re-enabling also invalidates previous ground authority.
        step.onDisable();
        step.onEnable();
        assertFalse(step.apply(player, movement.snapshot(), false));
        movement.update(true, false, false);
        step.apply(player, movement.snapshot(), false);
        movement.update(false, false, false);
        assertFalse(step.apply(player, movement.snapshot(), false));
        movement.clear();
        assertFalse(step.apply(player, movement.snapshot(), false));
        movement.update(false, false, false);
        assertFalse(step.apply(player, movement.snapshot(), false));
        assertEquals(3, player.verticalSetCalls);

        // An upward or invalid velocity is not a late deferred snap.
        step.delayTicksSetting().set(0);
        movement.update(true, false, false);
        step.apply(player, movement.snapshot(), false);
        movement.update(false, false, false);
        player.motionY = 0.42D;
        assertFalse(step.apply(player, movement.snapshot(), false));
        player.motionY = -0.10D;
        assertFalse(step.apply(player, movement.snapshot(), false));
        movement.update(true, false, false);
        step.apply(player, movement.snapshot(), false);
        movement.update(false, false, false);
        player.motionY = Double.NaN;
        assertFalse(step.apply(player, movement.snapshot(), false));
        assertEquals(3, player.verticalSetCalls);

        assertThrows(IllegalArgumentException.class,
                () -> step.delayTicksSetting().set(-1));
        assertThrows(IllegalArgumentException.class,
                () -> step.delayTicksSetting().set(11));
        step.onDisable();
    }

    @Test
    void sneakOnlyReverseStepComposesWithDelayAndPersistsThroughRuntime() {
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
            final Minecraft189ReverseStepModule step =
                    runtime.featureCatalog().reverseStep();
            controller.enable(Minecraft189ReverseStepModule.ID);
            step.delayTicksSetting().set(2);
            step.requireSneakingSetting().set(Boolean.TRUE);
            assertEquals("2", settings.snapshotEncoded().get(
                    Minecraft189ReverseStepModule.DELAY_TICKS_SETTING_ID));
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189ReverseStepModule.REQUIRE_SNEAK_SETTING_ID));
            final TestPlayer player = new TestPlayer();

            runtime.playerMovementState().update(true, false, false);
            runtime.playerMotionControl(player);
            runtime.playerMovementState().update(false, false, false);
            player.motionY = -0.10D;
            runtime.playerMotionControl(player);
            assertEquals(0, player.verticalSetCalls);
            // Sneaking in mid-air cannot rearm an already rejected edge.
            runtime.playerMovementState().update(false, true, false);
            runtime.playerMotionControl(player);
            runtime.playerMotionControl(player);
            assertEquals(0, player.verticalSetCalls);

            runtime.playerMovementState().update(true, true, false);
            runtime.playerMotionControl(player);
            runtime.playerMovementState().update(false, true, false);
            runtime.playerMotionControl(player); // delay skip 1
            runtime.playerMotionControl(player); // delay skip 2
            assertEquals(0, player.verticalSetCalls);
            runtime.playerMotionControl(player); // eligible
            assertEquals(1, player.verticalSetCalls);
            assertEquals(-0.50D, player.motionY, 0.000000001D);

            // Releasing sneak while countdown active cancels pending
            // transition and requires another real departure.
            runtime.playerMovementState().update(true, true, false);
            runtime.playerMotionControl(player);
            runtime.playerMovementState().update(false, true, false);
            player.motionY = -0.10D;
            runtime.playerMotionControl(player);
            runtime.playerMovementState().update(false, false, false);
            runtime.playerMotionControl(player);
            runtime.playerMovementState().update(false, true, false);
            runtime.playerMotionControl(player);
            runtime.playerMotionControl(player);
            assertEquals(1, player.verticalSetCalls);

            controller.disable(Minecraft189ReverseStepModule.ID);
            runtime.playerMotionControl(player);
            assertEquals(1, player.verticalSetCalls);
        } finally {
            runtime.close();
        }
        assertNull(modules.find(Minecraft189ReverseStepModule.ID));
        assertNull(settings.find(Minecraft189ReverseStepModule.SPEED_SETTING_ID));
        assertNull(settings.find(Minecraft189ReverseStepModule.DELAY_TICKS_SETTING_ID));
        assertNull(settings.find(Minecraft189ReverseStepModule.REQUIRE_SNEAK_SETTING_ID));
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
