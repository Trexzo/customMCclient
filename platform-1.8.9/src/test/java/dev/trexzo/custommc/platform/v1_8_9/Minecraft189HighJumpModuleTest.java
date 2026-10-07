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

final class Minecraft189HighJumpModuleTest {
    @Test
    void highJumpRaisesFreshGroundJumpAndKeepsHorizontalStrafeIndependent() {
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
                            Minecraft189HighJumpModule.ID));
            assertNotNull(
                    settings.find(
                            Minecraft189HighJumpModule.VERTICAL_SPEED_SETTING_ID));
            assertEquals(
                    Minecraft189HighJumpModule.DEFAULT_VERTICAL_SPEED,
                    runtime.featureCatalog()
                            .highJump()
                            .verticalSpeedSetting()
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
                    Minecraft189HighJumpModule.ID);
            assertTrue(
                    runtime.featureCatalog()
                            .highJump()
                            .active());

            controller.enable(
                    Minecraft189AutoJumpModule.ID);
            runtime.playerJumpControl(
                    player);
            assertEquals(
                    0,
                    player.jumpCalls);

            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.SPACE,
                            true);
            runtime.playerJumpControl(
                    player);
            assertEquals(
                    1,
                    player.jumpCalls);
            assertEquals(
                    0.42D,
                    player.motionY,
                    0.000000001D);

            runtime.playerMotionControl(
                    player);
            assertEquals(
                    Minecraft189HighJumpModule.DEFAULT_VERTICAL_SPEED,
                    player.motionY,
                    0.000000001D);

            runtime.playerJumpControl(
                    player);
            assertEquals(
                    1,
                    player.jumpCalls);

            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.SPACE,
                            false);
            runtime.playerJumpControl(
                    player);
            runtime.featureCatalog()
                    .highJump()
                    .verticalSpeedSetting()
                    .set(
                            0.95D);
            controller.enable(
                    Minecraft189StrafeModule.ID);
            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.W,
                            true);
            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.SPACE,
                            true);
            runtime.playerJumpControl(
                    player);
            assertEquals(
                    2,
                    player.jumpCalls);
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    0.95D,
                    player.motionY,
                    0.000000001D);
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
                            LegacyKeyboardCodes.SPACE,
                            false);
            runtime.playerJumpControl(
                    player);
            controller.enable(
                    Minecraft189LongJumpModule.ID);
            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.SPACE,
                            true);
            runtime.playerJumpControl(
                    player);
            assertEquals(
                    3,
                    player.jumpCalls);
            runtime.playerMotionControl(
                    player);
            assertEquals(
                    0.42D,
                    player.motionY,
                    0.000000001D);
            assertEquals(
                    Minecraft189LongJumpModule.DEFAULT_SPEED,
                    player.motionZ,
                    0.000000001D);
            controller.disable(
                    Minecraft189LongJumpModule.ID);

            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.SPACE,
                            false);
            runtime.playerJumpControl(
                    player);
            controller.enable(
                    Minecraft189FlightModule.ID);
            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.SPACE,
                            true);
            runtime.playerJumpControl(
                    player);
            assertEquals(
                    3,
                    player.jumpCalls);
            controller.disable(
                    Minecraft189FlightModule.ID);

            controller.disable(
                    Minecraft189HighJumpModule.ID);
            assertFalse(
                    runtime.featureCatalog()
                            .highJump()
                            .active());

            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.SPACE,
                            false);
            runtime.playerJumpControl(
                    player);
            assertEquals(
                    4,
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
                        Minecraft189HighJumpModule.ID));
        assertNull(
                settings.find(
                        Minecraft189HighJumpModule.VERTICAL_SPEED_SETTING_ID));
        assertNull(
                categories.find(
                        Minecraft189FeatureCatalog.MOVEMENT_CATEGORY_ID));
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
