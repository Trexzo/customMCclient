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
        assertNull(
                categories.find(
                        Minecraft189FeatureCatalog.MOVEMENT_CATEGORY_ID));
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
