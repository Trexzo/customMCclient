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

final class Minecraft189NoWebModuleTest {
    @Test
    void noWebClearsStateOnlyWhileEnabledAndNeverForcesWebOnDisable() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final ModuleCategoryRegistry categories = new ModuleCategoryRegistry();
        final ServiceRegistry services = new ServiceRegistry();
        services.register(RenderPipeline.class, new RenderPipeline());

        final Minecraft189Platform platform = new Minecraft189Platform();
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
                        new ModuleSettingRegistry(modules, settings),
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
                            Minecraft189NoWebModule.ID));

            final TestPlayer player = new TestPlayer();
            player.inWeb = true;
            runtime.playerWebControl(player);
            assertTrue(player.inWeb);
            assertEquals(0, player.setCalls);

            controller.enable(Minecraft189NoWebModule.ID);
            assertTrue(runtime.featureCatalog().noWeb().active());

            runtime.playerWebControl(player);
            assertFalse(player.inWeb);
            assertEquals(1, player.setCalls);

            runtime.playerWebControl(player);
            assertEquals(1, player.setCalls);

            player.inWeb = true;
            runtime.playerWebControl(player);
            assertFalse(player.inWeb);
            assertEquals(2, player.setCalls);

            controller.disable(Minecraft189NoWebModule.ID);
            assertFalse(runtime.featureCatalog().noWeb().active());

            player.inWeb = true;
            runtime.playerWebControl(player);
            assertTrue(player.inWeb);
            assertEquals(2, player.setCalls);

            runtime.playerWebControl(null);
        } finally {
            runtime.close();
        }

        assertNull(modules.find(Minecraft189NoWebModule.ID));
        assertNull(
                categories.find(
                        Minecraft189FeatureCatalog.MOVEMENT_CATEGORY_ID));
    }

    @Test
    void noWebOptionalGroundAndSneakGatesFailClosedAndComposeIndependently() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final ServiceRegistry services = new ServiceRegistry();
        services.register(RenderPipeline.class, new RenderPipeline());
        final Minecraft189Platform platform = new Minecraft189Platform();
        platform.attach(new PlatformContext(new EventBus(),
                modules, controller, services));
        final Minecraft189HostRuntime runtime = Minecraft189HostRuntime.install(
                platform, new ModulePresentationRegistry(),
                new ModuleCategoryRegistry(),
                new ModuleSettingRegistry(modules, settings),
                null, null, settings, new SettingPresentationRegistry(),
                new NoOpHost());
        try {
            final Minecraft189NoWebModule web = runtime.featureCatalog().noWeb();
            final TestPlayer player = new TestPlayer();
            assertFalse(web.groundOnlySetting().get().booleanValue());
            assertFalse(web.requireSneakingSetting().get().booleanValue());
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189NoWebModule.GROUND_ONLY_SETTING_ID));
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189NoWebModule.REQUIRE_SNEAKING_SETTING_ID));
            controller.enable(Minecraft189NoWebModule.ID);

            player.inWeb = true;
            runtime.playerWebControl(player); // Default no state, clears.
            assertFalse(player.inWeb);
            assertEquals(1, player.setCalls);

            web.groundOnlySetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189NoWebModule.GROUND_ONLY_SETTING_ID));
            player.inWeb = true;
            runtime.playerWebControl(player); // Unknown, no clear.
            assertTrue(player.inWeb);
            runtime.playerMovementState().update(false, false, false);
            runtime.playerWebControl(player); // Airborne, no clear.
            assertTrue(player.inWeb);
            runtime.playerMovementState().update(true, false, false);
            runtime.playerWebControl(player); // Grounded, clear.
            assertFalse(player.inWeb);
            assertEquals(2, player.setCalls);

            // Require Sneaking independently blocks the grounded case.
            web.requireSneakingSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189NoWebModule.REQUIRE_SNEAKING_SETTING_ID));
            player.inWeb = true;
            runtime.playerWebControl(player);
            assertTrue(player.inWeb);
            runtime.playerMovementState().update(true, true, false);
            runtime.playerWebControl(player);
            assertFalse(player.inWeb);
            assertEquals(3, player.setCalls);

            // Disabling Ground Only allows airborne sneak to qualify.
            web.groundOnlySetting().set(Boolean.FALSE);
            player.inWeb = true;
            runtime.playerMovementState().update(false, false, false);
            runtime.playerWebControl(player);
            assertTrue(player.inWeb);
            runtime.playerMovementState().update(false, true, false);
            runtime.playerWebControl(player);
            assertFalse(player.inWeb);
            assertEquals(4, player.setCalls);

            // Legacy API remains conservative with a configured gate.
            player.inWeb = true;
            web.apply(player);
            assertTrue(player.inWeb);
            runtime.playerMovementState().clear();
            runtime.playerWebControl(player);
            assertTrue(player.inWeb);

            web.requireSneakingSetting().set(Boolean.FALSE);
            web.apply(player); // Original direct no-state API.
            assertFalse(player.inWeb);
            assertEquals(5, player.setCalls);
            controller.disable(Minecraft189NoWebModule.ID);
            player.inWeb = true;
            runtime.playerWebControl(player);
            assertTrue(player.inWeb);
            assertEquals(5, player.setCalls);
        } finally {
            runtime.close();
        }
        assertNull(settings.find(Minecraft189NoWebModule.GROUND_ONLY_SETTING_ID));
        assertNull(settings.find(
                Minecraft189NoWebModule.REQUIRE_SNEAKING_SETTING_ID));
        assertNull(modules.find(Minecraft189NoWebModule.ID));
    }

    @Test
    void airborneOnlyRequiresConfirmedAirAndComposesWithSneakAndGroundVetoes() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final ServiceRegistry services = new ServiceRegistry();
        services.register(RenderPipeline.class, new RenderPipeline());
        final Minecraft189Platform platform = new Minecraft189Platform();
        platform.attach(new PlatformContext(new EventBus(), modules, controller,
                services));
        final Minecraft189HostRuntime runtime = Minecraft189HostRuntime.install(
                platform, new ModulePresentationRegistry(),
                new ModuleCategoryRegistry(),
                new ModuleSettingRegistry(modules, settings),
                null, null, settings, new SettingPresentationRegistry(),
                new NoOpHost());
        try {
            final Minecraft189NoWebModule web = runtime.featureCatalog().noWeb();
            final TestPlayer player = new TestPlayer();
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189NoWebModule.AIRBORNE_ONLY_SETTING_ID));
            controller.enable(Minecraft189NoWebModule.ID);
            runtime.playerMovementState().update(true, false, false);
            player.inWeb = true;
            runtime.playerWebControl(player);
            assertFalse(player.inWeb); // Default legacy behavior.
            assertEquals(1, player.setCalls);

            web.airborneOnlySetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189NoWebModule.AIRBORNE_ONLY_SETTING_ID));
            player.inWeb = true;
            runtime.playerWebControl(player);
            assertTrue(player.inWeb); // Ground veto.
            assertEquals(1, player.setCalls);
            runtime.playerMovementState().clear();
            runtime.playerWebControl(player);
            assertTrue(player.inWeb); // Unknown state fails closed.
            web.apply(player);
            assertTrue(player.inWeb); // Legacy overload has no air authority.
            runtime.playerMovementState().update(false, false, false);
            runtime.playerWebControl(player);
            assertFalse(player.inWeb);
            assertEquals(2, player.setCalls);

            web.requireSneakingSetting().set(Boolean.TRUE);
            player.inWeb = true;
            runtime.playerWebControl(player);
            assertTrue(player.inWeb); // Sneak gate fails.
            runtime.playerMovementState().update(false, true, false);
            runtime.playerWebControl(player);
            assertFalse(player.inWeb); // Both opt-in conditions met.
            assertEquals(3, player.setCalls);
            web.groundOnlySetting().set(Boolean.TRUE);
            player.inWeb = true;
            runtime.playerWebControl(player);
            assertTrue(player.inWeb); // Ground + air contradictory.
            runtime.playerMovementState().update(true, true, false);
            runtime.playerWebControl(player);
            assertTrue(player.inWeb); // Never allowed by both.
            web.groundOnlySetting().set(Boolean.FALSE);
            web.requireSneakingSetting().set(Boolean.FALSE);
            web.airborneOnlySetting().set(Boolean.FALSE);
            runtime.playerWebControl(player);
            assertFalse(player.inWeb); // Legacy behavior restored.
            assertEquals(4, player.setCalls);
            controller.disable(Minecraft189NoWebModule.ID);
            player.inWeb = true;
            runtime.playerWebControl(player);
            assertTrue(player.inWeb);
            assertEquals(4, player.setCalls);
        } finally {
            runtime.close();
        }
        assertNull(settings.find(Minecraft189NoWebModule.AIRBORNE_ONLY_SETTING_ID));
        assertNull(modules.find(Minecraft189NoWebModule.ID));
    }

    private static final class TestPlayer
            implements Minecraft189PlayerWebControl {
        private boolean inWeb;
        private int setCalls;

        @Override
        public boolean customMcInWeb() {
            return inWeb;
        }

        @Override
        public void customMcSetInWeb(
                final boolean inWeb) {
            setCalls++;
            this.inWeb = inWeb;
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
        public void beginUi(final UiViewport viewport) {
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
