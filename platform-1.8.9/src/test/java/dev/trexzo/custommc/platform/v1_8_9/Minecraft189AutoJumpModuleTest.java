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
            assertEquals(Integer.valueOf(0), runtime.featureCatalog()
                    .autoJump().landingDelayTicksSetting().get());
            assertEquals("0", settings.snapshotEncoded().get(
                    Minecraft189AutoJumpModule.LANDING_DELAY_SETTING_ID));

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

            // The new persisted delay must remain opt-in and must not
            // alter the established instant-jump behavior at zero.
            assertEquals("0", settings.snapshotEncoded().get(
                    Minecraft189AutoJumpModule.LANDING_DELAY_SETTING_ID));

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
        assertNull(settings.find(
                Minecraft189AutoJumpModule.LANDING_DELAY_SETTING_ID));
        assertNull(
                categories.find(
                        Minecraft189FeatureCatalog.MOVEMENT_CATEGORY_ID));
    }

    @Test
    void landingDelayWaitsExactGroundedUpdatesAndCancelsOnFreshAirContact() {
        final Minecraft189AutoJumpModule module = new Minecraft189AutoJumpModule();
        final Minecraft189PlayerMovementState state =
                new Minecraft189PlayerMovementState();
        final TestPlayer player = new TestPlayer();
        assertEquals(Integer.valueOf(0), module.landingDelayTicksSetting().get());
        module.onEnable();
        state.update(true, false, false);
        module.apply(player, state.snapshot(), true);
        assertEquals(1, player.jumpCalls);
        module.apply(player, state.snapshot(), true);
        assertEquals(1, player.jumpCalls);

        module.landingDelayTicksSetting().set(2);
        assertEquals(Integer.valueOf(2), module.landingDelayTicksSetting().get());
        state.update(false, false, false);
        module.apply(player, state.snapshot(), true);
        state.update(true, false, false);
        module.apply(player, state.snapshot(), true); // Grounded 1/2.
        assertEquals(1, player.jumpCalls);
        module.apply(player, state.snapshot(), true); // Grounded 2/2.
        assertEquals(1, player.jumpCalls);
        module.apply(player, state.snapshot(), true); // First eligible callback.
        assertEquals(2, player.jumpCalls);
        module.apply(player, state.snapshot(), true);
        assertEquals(2, player.jumpCalls); // No duplicate on same contact.

        // An intervening airborne observation restarts the complete delay.
        state.update(false, false, false);
        module.apply(player, state.snapshot(), true);
        state.update(true, false, false);
        module.apply(player, state.snapshot(), true); // Grounded 1/2.
        assertEquals(2, player.jumpCalls);
        state.update(false, false, false);
        module.apply(player, state.snapshot(), true); // Cancel pending.
        state.update(true, false, false);
        module.apply(player, state.snapshot(), true); // New grounded 1/2.
        module.apply(player, state.snapshot(), true); // Grounded 2/2.
        assertEquals(2, player.jumpCalls);
        module.apply(player, state.snapshot(), true);
        assertEquals(3, player.jumpCalls);

        // The require-forward gate cannot consume or rearm a second jump.
        module.requireForwardSetting().set(Boolean.TRUE);
        state.update(false, false, false);
        module.apply(player, state.snapshot(), false);
        state.update(true, false, false);
        module.apply(player, state.snapshot(), false); // 1/2.
        module.apply(player, state.snapshot(), false); // 2/2.
        module.apply(player, state.snapshot(), false); // Eligible but gated.
        assertEquals(3, player.jumpCalls);
        module.apply(player, state.snapshot(), true);
        assertEquals(4, player.jumpCalls);
        module.apply(player, state.snapshot(), true);
        assertEquals(4, player.jumpCalls);

        // Unknown movement never decrements/consumes pending delay.
        state.update(false, false, false);
        module.apply(player, state.snapshot(), true);
        state.update(true, false, false);
        module.apply(player, state.snapshot(), true); // 1/2
        state.clear();
        module.apply(player, state.snapshot(), true);
        assertEquals(4, player.jumpCalls);
        state.update(true, false, false);
        module.apply(player, state.snapshot(), true);
        assertEquals(5, player.jumpCalls); // Fresh known ground after reset.
        module.onDisable();
        module.apply(player, state.snapshot(), true);
        assertEquals(5, player.jumpCalls);
        module.onEnable();
        module.landingDelayTicksSetting().set(0);
        state.update(false, false, false);
        module.apply(player, state.snapshot(), true);
        state.update(true, false, false);
        module.apply(player, state.snapshot(), true);
        assertEquals(6, player.jumpCalls); // Exact original no-delay parity.
        assertThrows(IllegalArgumentException.class,
                () -> module.landingDelayTicksSetting().set(-1));
        assertThrows(IllegalArgumentException.class,
                () -> module.landingDelayTicksSetting().set(11));
        module.onDisable();
    }

    @Test
    void landingDelayPersistsThroughFeatureAndUnregistersOnClose() {
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
            final Minecraft189AutoJumpModule module =
                    runtime.featureCatalog().autoJump();
            assertEquals("0", settings.snapshotEncoded().get(
                    Minecraft189AutoJumpModule.LANDING_DELAY_SETTING_ID));
            module.landingDelayTicksSetting().set(3);
            assertEquals("3", settings.snapshotEncoded().get(
                    Minecraft189AutoJumpModule.LANDING_DELAY_SETTING_ID));
            controller.enable(Minecraft189AutoJumpModule.ID);
            final TestPlayer player = new TestPlayer();
            // Initial ground remains eligible without an observed landing.
            runtime.playerMovementState(player);
            runtime.playerJumpControl(player);
            assertEquals(1, player.jumpCalls);
            player.onGround = false;
            runtime.playerMovementState(player);
            runtime.playerJumpControl(player);
            player.onGround = true;
            runtime.playerMovementState(player);
            runtime.playerJumpControl(player); // 1/3
            runtime.playerJumpControl(player); // 2/3
            runtime.playerJumpControl(player); // 3/3
            assertEquals(1, player.jumpCalls);
            runtime.playerJumpControl(player); // Now eligible
            assertEquals(2, player.jumpCalls);
            controller.disable(Minecraft189AutoJumpModule.ID);
            runtime.playerJumpControl(player);
            assertEquals(2, player.jumpCalls);
        } finally {
            runtime.close();
        }
        assertNull(settings.find(
                Minecraft189AutoJumpModule.LANDING_DELAY_SETTING_ID));
        assertNull(modules.find(Minecraft189AutoJumpModule.ID));
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
