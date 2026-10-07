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

final class Minecraft189AutoJumpModuleTest {
    @Test
    void autoJumpFiresOncePerGroundContactAndStopsOnDisable() {
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
                            Minecraft189AutoJumpModule.ID));
            assertNotNull(
                    settings.find(
                            Minecraft189AutoJumpModule.REQUIRE_FORWARD_SETTING_ID));
            assertFalse(
                    runtime.featureCatalog()
                            .autoJump()
                            .requireForwardSetting()
                            .get()
                            .booleanValue());

            final TestPlayer player =
                    new TestPlayer();

            runtime.playerMovementState(
                    player);
            runtime.playerJumpControl(
                    player);
            assertEquals(
                    0,
                    player.jumpCalls);

            controller.enable(
                    Minecraft189AutoJumpModule.ID);
            assertTrue(
                    runtime.featureCatalog()
                            .autoJump()
                            .active());

            runtime.playerMovementState(
                    player);
            runtime.playerJumpControl(
                    player);
            assertEquals(
                    1,
                    player.jumpCalls);

            runtime.playerMovementState(
                    player);
            runtime.playerJumpControl(
                    player);
            assertEquals(
                    1,
                    player.jumpCalls);

            player.onGround = false;
            runtime.playerMovementState(
                    player);
            runtime.playerJumpControl(
                    player);
            assertEquals(
                    1,
                    player.jumpCalls);

            player.onGround = true;
            runtime.playerMovementState(
                    player);
            runtime.playerJumpControl(
                    player);
            assertEquals(
                    2,
                    player.jumpCalls);

            runtime.featureCatalog()
                    .autoJump()
                    .requireForwardSetting()
                    .set(
                            Boolean.TRUE);
            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.W,
                            false);
            player.onGround = false;
            runtime.playerMovementState(
                    player);
            runtime.playerJumpControl(
                    player);
            player.onGround = true;
            runtime.playerMovementState(
                    player);
            runtime.playerJumpControl(
                    player);
            assertEquals(
                    2,
                    player.jumpCalls);

            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.W,
                            true);
            runtime.playerMovementState(
                    player);
            runtime.playerJumpControl(
                    player);
            assertEquals(
                    3,
                    player.jumpCalls);
            runtime.playerMovementState(
                    player);
            runtime.playerJumpControl(
                    player);
            assertEquals(
                    3,
                    player.jumpCalls);
            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.W,
                            false);

            controller.disable(
                    Minecraft189AutoJumpModule.ID);
            assertFalse(
                    runtime.featureCatalog()
                            .autoJump()
                            .active());

            player.onGround = false;
            runtime.playerMovementState(
                    player);
            runtime.playerJumpControl(
                    player);
            player.onGround = true;
            runtime.playerMovementState(
                    player);
            runtime.playerJumpControl(
                    player);
            assertEquals(
                    3,
                    player.jumpCalls);
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189AutoJumpModule.ID));
        assertNull(
                settings.find(
                        Minecraft189AutoJumpModule.REQUIRE_FORWARD_SETTING_ID));
        assertNull(
                categories.find(
                        Minecraft189FeatureCatalog.MOVEMENT_CATEGORY_ID));
    }

    private static final class TestPlayer
            implements Minecraft189PlayerMovementStateAccess,
            Minecraft189PlayerJumpControl {
        private boolean onGround = true;
        private int jumpCalls;

        @Override
        public boolean customMcOnGround() {
            return onGround;
        }

        @Override
        public boolean customMcSneaking() {
            return false;
        }

        @Override
        public boolean customMcSprinting() {
            return false;
        }

        @Override
        public void customMcJump() {
            jumpCalls++;
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
