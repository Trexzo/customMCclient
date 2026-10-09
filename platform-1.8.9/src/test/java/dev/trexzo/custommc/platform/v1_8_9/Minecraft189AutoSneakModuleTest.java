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
import static org.junit.jupiter.api.Assertions.assertThrows;

final class Minecraft189AutoSneakModuleTest {
    @Test
    void autoSneakUsesCurrentMovementSnapshotAndStopsForcingOnDisable() {
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
                            Minecraft189AutoSneakModule.ID));
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189AutoSneakModule.GROUND_ONLY_SETTING_ID));
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189AutoSneakModule.PAUSE_SPRINTING_SETTING_ID));

            final TestPlayer player =
                    new TestPlayer();

            runtime.playerMovementState(
                    player);
            runtime.playerSneakControl(
                    player);
            assertFalse(
                    player.sneaking);
            assertEquals(
                    0,
                    player.setCalls);

            controller.enable(
                    Minecraft189AutoSneakModule.ID);
            assertTrue(
                    runtime.featureCatalog()
                            .autoSneak()
                            .active());

            runtime.playerMovementState(
                    player);
            runtime.playerSneakControl(
                    player);
            assertTrue(
                    player.sneaking);
            assertEquals(
                    1,
                    player.setCalls);

            runtime.playerMovementState(
                    player);
            runtime.playerSneakControl(
                    player);
            assertEquals(
                    1,
                    player.setCalls);

            controller.disable(
                    Minecraft189AutoSneakModule.ID);
            assertFalse(
                    runtime.featureCatalog()
                            .autoSneak()
                            .active());

            player.sneaking = false;
            runtime.playerMovementState(
                    player);
            runtime.playerSneakControl(
                    player);
            assertFalse(
                    player.sneaking);
            assertEquals(
                    1,
                    player.setCalls);
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189AutoSneakModule.ID));
        assertNull(settings.find(
                Minecraft189AutoSneakModule.GROUND_ONLY_SETTING_ID));
        assertNull(settings.find(
                Minecraft189AutoSneakModule.PAUSE_SPRINTING_SETTING_ID));
        assertNull(
                categories.find(
                        Minecraft189FeatureCatalog.MOVEMENT_CATEGORY_ID));
    }

    @Test
    void movementGatesOptInAndPreserveExistingSneakAndAuthority() {
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
            final Minecraft189AutoSneakModule sneak = runtime.featureCatalog().autoSneak();
            final TestPlayer player = new TestPlayer();
            assertFalse(sneak.groundOnlySetting().get().booleanValue());
            assertFalse(sneak.pauseSprintingSetting().get().booleanValue());
            controller.enable(Minecraft189AutoSneakModule.ID);

            // Default OFF: still forces sneak midair and while sprinting.
            player.onGround = false;
            player.sprinting = true;
            runtime.playerMovementState(player);
            runtime.playerSneakControl(player);
            assertTrue(player.sneaking);
            assertEquals(1, player.setCalls);

            // Both settings persist through existing encoded state.
            sneak.groundOnlySetting().set(Boolean.TRUE);
            sneak.pauseSprintingSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189AutoSneakModule.GROUND_ONLY_SETTING_ID));
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189AutoSneakModule.PAUSE_SPRINTING_SETTING_ID));

            // Existing sneaking is never forcibly released by a gate.
            runtime.playerMovementState(player);
            runtime.playerSneakControl(player);
            assertTrue(player.sneaking);
            assertEquals(1, player.setCalls);

            player.sneaking = false;
            runtime.playerMovementState(player);
            runtime.playerSneakControl(player);
            assertFalse(player.sneaking); // Airborne and sprinting.
            assertEquals(1, player.setCalls);
            player.onGround = true;
            runtime.playerMovementState(player);
            runtime.playerSneakControl(player);
            assertFalse(player.sneaking); // Sprint gate still active.
            assertEquals(1, player.setCalls);
            player.sprinting = false;
            runtime.playerMovementState(player);
            runtime.playerSneakControl(player);
            assertTrue(player.sneaking); // Both gates satisfied.
            assertEquals(2, player.setCalls);

            // Gate only blocks new sneak writes; it does not undo a sneak.
            player.sprinting = true;
            runtime.playerMovementState(player);
            runtime.playerSneakControl(player);
            assertTrue(player.sneaking);
            assertEquals(2, player.setCalls);

            // Independent toggles: disabling Sprint Pause allows grounded
            // auto-sneak even while sprinting.
            player.sneaking = false;
            sneak.pauseSprintingSetting().set(Boolean.FALSE);
            runtime.playerMovementState(player);
            runtime.playerSneakControl(player);
            assertTrue(player.sneaking);
            assertEquals(3, player.setCalls);

            // Ground Only still prevents midair writes.
            player.sneaking = false;
            player.onGround = false;
            runtime.playerMovementState(player);
            runtime.playerSneakControl(player);
            assertFalse(player.sneaking);
            assertEquals(3, player.setCalls);
            sneak.groundOnlySetting().set(Boolean.FALSE);
            runtime.playerMovementState(player);
            runtime.playerSneakControl(player);
            assertTrue(player.sneaking);
            assertEquals(4, player.setCalls);

            // Cleared movement authority cannot produce another write.
            player.sneaking = false;
            runtime.playerMovementState().clear();
            runtime.playerSneakControl(player);
            assertFalse(player.sneaking);
            assertEquals(4, player.setCalls);
            controller.disable(Minecraft189AutoSneakModule.ID);
            player.onGround = true;
            player.sprinting = false;
            runtime.playerMovementState(player);
            runtime.playerSneakControl(player);
            assertFalse(player.sneaking);
            assertEquals(4, player.setCalls);
        } finally {
            runtime.close();
        }
        assertNull(settings.find(Minecraft189AutoSneakModule.GROUND_ONLY_SETTING_ID));
        assertNull(settings.find(Minecraft189AutoSneakModule.PAUSE_SPRINTING_SETTING_ID));
        assertNull(modules.find(Minecraft189AutoSneakModule.ID));
    }

    @Test
    void requireMovementGatesNewSneakWritesButNeverForcesUnsneakOnRelease() {
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
            final Minecraft189AutoSneakModule sneak =
                    runtime.featureCatalog().autoSneak();
            final TestPlayer player = new TestPlayer();
            assertFalse(sneak.requireMovementSetting().get().booleanValue());
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189AutoSneakModule.REQUIRE_MOVEMENT_SETTING_ID));
            controller.enable(Minecraft189AutoSneakModule.ID);
            sneak.requireMovementSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189AutoSneakModule.REQUIRE_MOVEMENT_SETTING_ID));
            runtime.playerMovementState(player);
            runtime.playerSneakControl(player);
            assertFalse(player.sneaking);
            assertEquals(0, player.setCalls);
            final int[] keys = {
                    LegacyKeyboardCodes.W, LegacyKeyboardCodes.A,
                    LegacyKeyboardCodes.S, LegacyKeyboardCodes.D};
            for (int i = 0; i < keys.length; i++) {
                runtime.inputState().key(keys[i], true);
                runtime.playerMovementState(player);
                runtime.playerSneakControl(player);
                assertEquals(i + 1, player.setCalls);
                assertTrue(player.sneaking);
                runtime.inputState().key(keys[i], false);
                runtime.playerMovementState(player);
                runtime.playerSneakControl(player);
                assertEquals(i + 1, player.setCalls);
                assertTrue(player.sneaking); // Never owns un-sneak writes.
                player.sneaking = false; // Simulate external release.
            }
            runtime.playerMovementState(player);
            runtime.playerSneakControl(player);
            assertEquals(4, player.setCalls);

            // Ground Only and Sprint Pause remain independent.
            sneak.groundOnlySetting().set(Boolean.TRUE);
            player.onGround = false;
            runtime.inputState().key(LegacyKeyboardCodes.W, true);
            runtime.playerMovementState(player);
            runtime.playerSneakControl(player);
            assertEquals(4, player.setCalls);
            player.onGround = true;
            player.sprinting = true;
            sneak.pauseSprintingSetting().set(Boolean.TRUE);
            runtime.playerMovementState(player);
            runtime.playerSneakControl(player);
            assertEquals(4, player.setCalls);
            player.sprinting = false;
            runtime.playerMovementState(player);
            runtime.playerSneakControl(player);
            assertEquals(5, player.setCalls);
            runtime.inputState().key(LegacyKeyboardCodes.W, false);
            player.sneaking = false;

            // Old two-argument API cannot invent a movement key.
            runtime.playerMovementState(player);
            sneak.apply(player, runtime.playerMovementState().snapshot());
            assertEquals(5, player.setCalls);
            runtime.playerMovementState().clear();
            runtime.inputState().key(LegacyKeyboardCodes.A, true);
            runtime.playerSneakControl(player);
            assertEquals(5, player.setCalls);
            runtime.inputState().key(LegacyKeyboardCodes.A, false);

            // Setting OFF recovers the exact stationary default behavior.
            sneak.requireMovementSetting().set(Boolean.FALSE);
            sneak.groundOnlySetting().set(Boolean.FALSE);
            sneak.pauseSprintingSetting().set(Boolean.FALSE);
            runtime.playerMovementState(player);
            runtime.playerSneakControl(player);
            assertEquals(6, player.setCalls);
            controller.disable(Minecraft189AutoSneakModule.ID);
            player.sneaking = false;
            runtime.playerMovementState(player);
            runtime.playerSneakControl(player);
            assertEquals(6, player.setCalls);
        } finally {
            runtime.close();
        }
        assertNull(settings.find(
                Minecraft189AutoSneakModule.REQUIRE_MOVEMENT_SETTING_ID));
        assertNull(modules.find(Minecraft189AutoSneakModule.ID));
    }

    @Test
    void requireForwardUsesPhysicalWAndNeverWritesUnsneak() {
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
            final Minecraft189AutoSneakModule sneak =
                    runtime.featureCatalog().autoSneak();
            final TestPlayer player = new TestPlayer();
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189AutoSneakModule.REQUIRE_FORWARD_SETTING_ID));
            controller.enable(Minecraft189AutoSneakModule.ID);

            // Default OFF retains original unconditional sneak behavior.
            runtime.playerMovementState(player);
            runtime.playerSneakControl(player);
            assertEquals(1, player.setCalls);
            assertTrue(player.sneaking);

            player.sneaking = false;
            sneak.requireForwardSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189AutoSneakModule.REQUIRE_FORWARD_SETTING_ID));
            final int[] nonForwardKeys = {
                    LegacyKeyboardCodes.A,
                    LegacyKeyboardCodes.S,
                    LegacyKeyboardCodes.D};
            for (int key : nonForwardKeys) {
                runtime.inputState().key(key, true);
                runtime.playerMovementState(player);
                runtime.playerSneakControl(player);
                assertFalse(player.sneaking);
                assertEquals(1, player.setCalls);
                runtime.inputState().key(key, false);
            }
            // The legacy any-movement overload must not reinterpret A/S/D
            // as an observed forward key.
            final Minecraft189PlayerMovementState.Snapshot current =
                    runtime.playerMovementState().snapshot();
            sneak.apply(player, current, true);
            assertFalse(player.sneaking);
            assertEquals(1, player.setCalls);
            sneak.apply(player, current);
            assertFalse(player.sneaking);

            runtime.inputState().key(LegacyKeyboardCodes.W, true);
            runtime.playerMovementState(player);
            runtime.playerSneakControl(player);
            assertTrue(player.sneaking);
            assertEquals(2, player.setCalls);
            runtime.inputState().key(LegacyKeyboardCodes.W, false);
            runtime.playerMovementState(player);
            runtime.playerSneakControl(player);
            assertTrue(player.sneaking); // Never forcibly unsneak.
            assertEquals(2, player.setCalls);
            player.sneaking = false; // Simulate external release.

            sneak.requireMovementSetting().set(Boolean.TRUE);
            runtime.playerMovementState(player);
            runtime.playerSneakControl(player);
            assertFalse(player.sneaking);
            runtime.inputState().key(LegacyKeyboardCodes.W, true);
            runtime.playerMovementState(player);
            runtime.playerSneakControl(player);
            assertTrue(player.sneaking); // Both gates qualified.
            assertEquals(3, player.setCalls);
            player.sneaking = false;
            sneak.groundOnlySetting().set(Boolean.TRUE);
            player.onGround = false;
            runtime.playerMovementState(player);
            runtime.playerSneakControl(player);
            assertEquals(3, player.setCalls);
            player.onGround = true;
            player.sprinting = true;
            sneak.pauseSprintingSetting().set(Boolean.TRUE);
            runtime.playerMovementState(player);
            runtime.playerSneakControl(player);
            assertEquals(3, player.setCalls);
            player.sprinting = false;
            runtime.playerMovementState().clear();
            runtime.playerSneakControl(player);
            assertEquals(3, player.setCalls); // Unavailable state veto.
            runtime.playerMovementState(player);
            runtime.playerSneakControl(player);
            assertEquals(4, player.setCalls);
            sneak.requireForwardSetting().set(Boolean.FALSE);
            sneak.requireMovementSetting().set(Boolean.FALSE);
            sneak.groundOnlySetting().set(Boolean.FALSE);
            sneak.pauseSprintingSetting().set(Boolean.FALSE);
            runtime.inputState().key(LegacyKeyboardCodes.W, false);
            player.sneaking = false;
            runtime.playerMovementState(player);
            runtime.playerSneakControl(player);
            assertEquals(5, player.setCalls); // Live original behavior.
            assertThrows(NullPointerException.class,
                    () -> sneak.requireForwardSetting().set(null));
            controller.disable(Minecraft189AutoSneakModule.ID);
            player.sneaking = false;
            runtime.playerMovementState(player);
            runtime.playerSneakControl(player);
            assertEquals(5, player.setCalls);
        } finally {
            runtime.close();
        }
        assertNull(settings.find(
                Minecraft189AutoSneakModule.REQUIRE_FORWARD_SETTING_ID));
        assertNull(modules.find(Minecraft189AutoSneakModule.ID));
    }

    private static final class TestPlayer
            implements Minecraft189PlayerMovementStateAccess,
            Minecraft189PlayerSneakControl {
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
        public void customMcSetSneaking(
                final boolean sneaking) {
            setCalls++;
            this.sneaking = sneaking;
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
