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

final class Minecraft189AutoSprintModuleTest {
    @Test
    void autoSprintUsesCurrentMovementSnapshotAndStopsOnDisable() {
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
                    "Movement",
                    categories.find(
                                    Minecraft189FeatureCatalog.MOVEMENT_CATEGORY_ID)
                            .displayName());
            assertEquals(
                    ModuleState.DISABLED,
                    controller.stateOf(
                            Minecraft189AutoSprintModule.ID));
            assertNotNull(
                    settings.find(
                            Minecraft189AutoSprintModule.REQUIRE_FORWARD_SETTING_ID));
            assertNotNull(settings.find(Minecraft189AutoSprintModule.GROUND_ONLY_SETTING_ID));
            assertFalse(runtime.featureCatalog().autoSprint()
                    .groundOnlySetting().get().booleanValue());
            assertFalse(
                    runtime.featureCatalog()
                            .autoSprint()
                            .requireForwardSetting()
                            .get()
                            .booleanValue());

            final TestPlayer player =
                    new TestPlayer();

            runtime.playerMovementState(
                    player);
            runtime.playerSprintControl(
                    player);
            assertFalse(
                    player.sprinting);
            assertEquals(
                    0,
                    player.setCalls);

            controller.enable(
                    Minecraft189AutoSprintModule.ID);
            assertTrue(
                    runtime.featureCatalog()
                            .autoSprint()
                            .active());

            runtime.playerMovementState(
                    player);
            runtime.playerSprintControl(
                    player);
            assertTrue(
                    player.sprinting);
            assertEquals(
                    1,
                    player.setCalls);

            runtime.playerMovementState(
                    player);
            runtime.playerSprintControl(
                    player);
            assertEquals(
                    1,
                    player.setCalls);

            player.sprinting = false;
            player.sneaking = true;
            runtime.playerMovementState(
                    player);
            runtime.playerSprintControl(
                    player);
            assertFalse(
                    player.sprinting);
            assertEquals(
                    1,
                    player.setCalls);

            player.sprinting = false;
            player.sneaking = false;
            runtime.featureCatalog()
                    .autoSprint()
                    .requireForwardSetting()
                    .set(
                            Boolean.TRUE);
            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.W,
                            false);
            runtime.playerMovementState(
                    player);
            runtime.playerSprintControl(
                    player);
            assertFalse(
                    player.sprinting);
            assertEquals(
                    1,
                    player.setCalls);

            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.W,
                            true);
            runtime.playerMovementState(
                    player);
            runtime.playerSprintControl(
                    player);
            assertTrue(
                    player.sprinting);
            assertEquals(
                    2,
                    player.setCalls);
            runtime.inputState()
                    .key(
                            LegacyKeyboardCodes.W,
                            false);

            controller.disable(
                    Minecraft189AutoSprintModule.ID);
            assertFalse(
                    runtime.featureCatalog()
                            .autoSprint()
                            .active());

            player.sneaking = false;
            player.sprinting = false;
            runtime.playerMovementState(
                    player);
            runtime.playerSprintControl(
                    player);
            assertFalse(
                    player.sprinting);
            assertEquals(
                    2,
                    player.setCalls);
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189AutoSprintModule.ID));
        assertNull(
                settings.find(
                        Minecraft189AutoSprintModule.REQUIRE_FORWARD_SETTING_ID));
        assertNull(settings.find(Minecraft189AutoSprintModule.GROUND_ONLY_SETTING_ID));
        assertNull(
                categories.find(
                        Minecraft189FeatureCatalog.MOVEMENT_CATEGORY_ID));
    }

    @Test
    void groundOnlyUsesLiveMappedGroundStateWithoutDisruptingForwardOrSneak() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final ServiceRegistry services = new ServiceRegistry();
        services.register(RenderPipeline.class, new RenderPipeline());
        final Minecraft189Platform platform = new Minecraft189Platform();
        platform.attach(new PlatformContext(new EventBus(), modules,
                controller, services));
        final Minecraft189HostRuntime runtime = Minecraft189HostRuntime.install(
                platform,
                new ModulePresentationRegistry(), new ModuleCategoryRegistry(),
                new ModuleSettingRegistry(modules, settings),
                null, null, settings, new SettingPresentationRegistry(),
                new NoOpHost());
        try {
            final Minecraft189AutoSprintModule sprint =
                    runtime.featureCatalog().autoSprint();
            final TestPlayer player = new TestPlayer();
            player.onGround = false;
            controller.enable(Minecraft189AutoSprintModule.ID);
            sprint.groundOnlySetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189AutoSprintModule.GROUND_ONLY_SETTING_ID));

            runtime.playerMovementState(player);
            runtime.playerSprintControl(player);
            assertEquals(0, player.setCalls);
            assertFalse(player.sprinting);

            player.onGround = true;
            runtime.playerMovementState(player);
            runtime.playerSprintControl(player);
            assertEquals(1, player.setCalls);
            assertTrue(player.sprinting);
            runtime.playerMovementState(player);
            runtime.playerSprintControl(player);
            assertEquals(1, player.setCalls);

            player.sprinting = false;
            player.sneaking = true;
            runtime.playerMovementState(player);
            runtime.playerSprintControl(player);
            assertEquals(1, player.setCalls);

            player.sneaking = false;
            sprint.requireForwardSetting().set(Boolean.TRUE);
            runtime.playerMovementState(player);
            runtime.playerSprintControl(player);
            assertEquals(1, player.setCalls);

            runtime.inputState().key(LegacyKeyboardCodes.W, true);
            runtime.playerMovementState(player);
            runtime.playerSprintControl(player);
            assertEquals(2, player.setCalls);

            player.onGround = false;
            player.sprinting = false;
            runtime.playerMovementState(player);
            runtime.playerSprintControl(player);
            assertEquals(2, player.setCalls);

            // Previous airborne behavior remains available when OFF.
            sprint.groundOnlySetting().set(Boolean.FALSE);
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189AutoSprintModule.GROUND_ONLY_SETTING_ID));
            runtime.playerMovementState(player);
            runtime.playerSprintControl(player);
            assertEquals(3, player.setCalls);

            // No mapped movement authority never invents an on-ground value.
            player.sprinting = false;
            runtime.playerMovementState(null);
            runtime.playerSprintControl(player);
            assertEquals(3, player.setCalls);

            controller.disable(Minecraft189AutoSprintModule.ID);
            player.onGround = true;
            runtime.playerMovementState(player);
            runtime.playerSprintControl(player);
            assertEquals(3, player.setCalls);
        } finally {
            runtime.close();
        }
        assertNull(settings.find(Minecraft189AutoSprintModule.GROUND_ONLY_SETTING_ID));
    }

    @Test
    void requireWASDUsesRealHostKeysWithIndependentGroundForwardAndSneakGates() {
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
            final Minecraft189AutoSprintModule sprint =
                    runtime.featureCatalog().autoSprint();
            final TestPlayer player = new TestPlayer();
            assertFalse(sprint.requireMovementSetting().get().booleanValue());
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189AutoSprintModule.REQUIRE_MOVEMENT_SETTING_ID));
            controller.enable(Minecraft189AutoSprintModule.ID);
            sprint.requireMovementSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189AutoSprintModule.REQUIRE_MOVEMENT_SETTING_ID));

            runtime.playerMovementState(player);
            runtime.playerSprintControl(player);
            assertEquals(0, player.setCalls); // Stationary even though enabled.

            final int[] keys = {
                    LegacyKeyboardCodes.A, LegacyKeyboardCodes.S,
                    LegacyKeyboardCodes.D, LegacyKeyboardCodes.W};
            for (int i = 0; i < keys.length; i++) {
                runtime.inputState().key(keys[i], true);
                runtime.playerMovementState(player);
                runtime.playerSprintControl(player);
                assertEquals(i + 1, player.setCalls);
                assertTrue(player.sprinting);
                // Each real client tick refreshes the mapped sprint snapshot.
                // Without this update, the previous 'not sprinting' snapshot
                // would cause another write in a synthetic double callback.
                runtime.playerMovementState(player);
                runtime.playerSprintControl(player);
                assertEquals(i + 1, player.setCalls); // No redundant writes.
                runtime.inputState().key(keys[i], false);
                player.sprinting = false;
            }
            runtime.playerMovementState(player);
            runtime.playerSprintControl(player);
            assertEquals(4, player.setCalls);

            // Require Forward is stricter and independent of Require WASD.
            sprint.requireForwardSetting().set(Boolean.TRUE);
            runtime.inputState().key(LegacyKeyboardCodes.D, true);
            runtime.playerSprintControl(player);
            assertEquals(4, player.setCalls);
            runtime.inputState().key(LegacyKeyboardCodes.D, false);
            runtime.inputState().key(LegacyKeyboardCodes.W, true);
            runtime.playerSprintControl(player);
            assertEquals(5, player.setCalls);
            runtime.inputState().key(LegacyKeyboardCodes.W, false);
            player.sprinting = false;

            sprint.requireForwardSetting().set(Boolean.FALSE);
            sprint.groundOnlySetting().set(Boolean.TRUE);
            player.onGround = false;
            runtime.playerMovementState(player);
            runtime.inputState().key(LegacyKeyboardCodes.S, true);
            runtime.playerSprintControl(player);
            assertEquals(5, player.setCalls);
            player.onGround = true;
            player.sneaking = true;
            runtime.playerMovementState(player);
            runtime.playerSprintControl(player);
            assertEquals(5, player.setCalls); // Sneak still blocks.
            player.sneaking = false;
            runtime.playerMovementState(player);
            runtime.playerSprintControl(player);
            assertEquals(6, player.setCalls);
            runtime.inputState().key(LegacyKeyboardCodes.S, false);
            player.sprinting = false;

            // Legacy 3-argument API does not infer unknown A/S/D keys.
            sprint.apply(player, runtime.playerMovementState().snapshot(), false);
            assertEquals(6, player.setCalls);
            sprint.apply(player, runtime.playerMovementState().snapshot(), true);
            assertEquals(7, player.setCalls);
            player.sprinting = false;

            // Unknown mapped player state stays fail-closed.
            runtime.playerMovementState(null);
            runtime.inputState().key(LegacyKeyboardCodes.W, true);
            runtime.playerSprintControl(player);
            assertEquals(7, player.setCalls);
            runtime.inputState().key(LegacyKeyboardCodes.W, false);

            // Default mode retained when the option is toggled off.
            runtime.playerMovementState(player);
            sprint.groundOnlySetting().set(Boolean.FALSE);
            sprint.requireMovementSetting().set(Boolean.FALSE);
            runtime.playerSprintControl(player);
            assertEquals(8, player.setCalls);
            controller.disable(Minecraft189AutoSprintModule.ID);
            player.sprinting = false;
            runtime.playerSprintControl(player);
            assertEquals(8, player.setCalls);
        } finally {
            runtime.close();
        }
        assertNull(settings.find(
                Minecraft189AutoSprintModule.REQUIRE_MOVEMENT_SETTING_ID));
        assertNull(modules.find(Minecraft189AutoSprintModule.ID));
    }

    private static final class TestPlayer
            implements Minecraft189PlayerMovementStateAccess,
            Minecraft189PlayerSprintControl {
        private boolean onGround = true;
        private boolean sneaking;
        private boolean sprinting;
        private int setCalls;

        @Override
        public boolean customMcOnGround() {
            return onGround;
        }

        @Override
        public boolean customMcSneaking() {
            return sneaking;
        }

        @Override
        public boolean customMcSprinting() {
            return sprinting;
        }

        @Override
        public void customMcSetSprinting(
                final boolean sprinting) {
            setCalls++;
            this.sprinting = sprinting;
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
