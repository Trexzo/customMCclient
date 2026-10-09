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

final class Minecraft189NoClipModuleTest {
    @Test
    void noClipForcesTrueAndRestoresCapturedBaselineOnDisable() {
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
                            Minecraft189NoClipModule.ID));

            final TestPlayer player =
                    new TestPlayer();

            runtime.playerNoClipControl(
                    player);
            assertFalse(player.noClip);
            assertEquals(0, player.setCalls);

            controller.enable(
                    Minecraft189NoClipModule.ID);
            assertTrue(
                    runtime.featureCatalog()
                            .noClip()
                            .active());

            runtime.playerNoClipControl(
                    player);
            assertTrue(player.noClip);
            assertEquals(1, player.setCalls);

            runtime.playerNoClipControl(
                    player);
            assertEquals(1, player.setCalls);

            controller.disable(
                    Minecraft189NoClipModule.ID);
            assertTrue(
                    runtime.featureCatalog()
                            .noClip()
                            .restorePending());
            runtime.playerNoClipControl(
                    player);
            assertFalse(player.noClip);
            assertEquals(2, player.setCalls);
            assertFalse(
                    runtime.featureCatalog()
                            .noClip()
                            .restorePending());

            player.noClip = true;
            controller.enable(
                    Minecraft189NoClipModule.ID);
            runtime.playerNoClipControl(
                    player);
            assertTrue(player.noClip);
            assertEquals(2, player.setCalls);

            controller.disable(
                    Minecraft189NoClipModule.ID);
            runtime.playerNoClipControl(
                    player);
            assertTrue(player.noClip);
            assertEquals(2, player.setCalls);

            runtime.playerNoClipControl(
                    null);
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189NoClipModule.ID));
        assertNull(
                categories.find(
                        Minecraft189FeatureCatalog.MOVEMENT_CATEGORY_ID));
    }

    @Test
    void noClipHoldSneakRestoresOriginalCollisionStateOnReleaseAndStateLoss() {
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
            final Minecraft189NoClipModule clip = runtime.featureCatalog().noClip();
            final TestPlayer player = new TestPlayer();
            assertFalse(clip.requireSneakingSetting().get().booleanValue());
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189NoClipModule.REQUIRE_SNEAK_SETTING_ID));
            controller.enable(Minecraft189NoClipModule.ID);
            clip.requireSneakingSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189NoClipModule.REQUIRE_SNEAK_SETTING_ID));

            runtime.playerNoClipControl(player); // Absent state: fail closed.
            assertFalse(player.noClip);
            assertEquals(0, player.setCalls);
            runtime.playerMovementState().update(false, false, false);
            runtime.playerNoClipControl(player); // Sneak not held.
            assertFalse(player.noClip);

            runtime.playerMovementState().update(false, true, false);
            runtime.playerNoClipControl(player);
            assertTrue(player.noClip);
            assertEquals(1, player.setCalls);
            runtime.playerNoClipControl(player);
            assertEquals(1, player.setCalls);

            runtime.playerMovementState().update(false, false, false);
            runtime.playerNoClipControl(player); // Live release restores baseline.
            assertFalse(player.noClip);
            assertEquals(2, player.setCalls);
            runtime.playerNoClipControl(player);
            assertEquals(2, player.setCalls); // No redundant restore.

            runtime.playerMovementState().update(false, true, false);
            runtime.playerNoClipControl(player);
            assertTrue(player.noClip);
            assertEquals(3, player.setCalls);
            runtime.playerMovementState().clear();
            runtime.playerNoClipControl(player); // Authority loss restores.
            assertFalse(player.noClip);
            assertEquals(4, player.setCalls);

            // New activation recaptures a fresh original true baseline.
            player.noClip = true;
            runtime.playerMovementState().update(false, true, false);
            runtime.playerNoClipControl(player);
            assertTrue(player.noClip);
            assertEquals(4, player.setCalls);
            runtime.playerMovementState().update(false, false, false);
            runtime.playerNoClipControl(player);
            assertTrue(player.noClip); // Restore original true.
            assertEquals(4, player.setCalls);

            player.noClip = false;
            runtime.playerMovementState().update(false, true, false);
            runtime.playerNoClipControl(player);
            assertTrue(player.noClip);
            assertEquals(5, player.setCalls);
            controller.disable(Minecraft189NoClipModule.ID);
            assertTrue(clip.restorePending());
            runtime.playerNoClipControl(player);
            assertFalse(player.noClip);
            assertEquals(6, player.setCalls);
            assertFalse(clip.restorePending());

            // Default option OFF retains legacy unconditional No Clip.
            clip.requireSneakingSetting().set(Boolean.FALSE);
            controller.enable(Minecraft189NoClipModule.ID);
            runtime.playerMovementState().clear();
            runtime.playerNoClipControl(player);
            assertTrue(player.noClip);
            assertEquals(7, player.setCalls);
            controller.disable(Minecraft189NoClipModule.ID);
            runtime.playerNoClipControl(player);
            assertFalse(player.noClip);
            assertEquals(8, player.setCalls);
        } finally {
            runtime.close();
        }
        assertNull(settings.find(Minecraft189NoClipModule.REQUIRE_SNEAK_SETTING_ID));
        assertNull(modules.find(Minecraft189NoClipModule.ID));
    }

    private static final class TestPlayer
            implements Minecraft189PlayerNoClipControl {
        private boolean noClip;
        private int setCalls;

        @Override
        public boolean customMcNoClip() {
            return noClip;
        }

        @Override
        public void customMcSetNoClip(
                final boolean noClip) {
            setCalls++;
            this.noClip = noClip;
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
