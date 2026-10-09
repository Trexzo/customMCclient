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

final class Minecraft189NoFallModuleTest {
    @Test
    void noFallZerosDistanceOnlyWhileEnabledAndNeverRestoresSyntheticState() {
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
                            Minecraft189NoFallModule.ID));
            assertNotNull(
                    settings.find(
                            Minecraft189NoFallModule.THRESHOLD_SETTING_ID));
            assertEquals(
                    Minecraft189NoFallModule.DEFAULT_THRESHOLD,
                    runtime.featureCatalog()
                            .noFall()
                            .thresholdSetting()
                            .get()
                            .doubleValue(),
                    0.000001D);

            final TestPlayer player =
                    new TestPlayer();
            player.distance = 7.5F;
            runtime.playerFallDistanceControl(
                    player);
            assertEquals(
                    7.5F,
                    player.distance,
                    0.000001F);
            assertEquals(
                    0,
                    player.setCalls);

            controller.enable(
                    Minecraft189NoFallModule.ID);
            assertTrue(
                    runtime.featureCatalog()
                            .noFall()
                            .active());

            runtime.playerFallDistanceControl(
                    player);
            assertEquals(
                    0.0F,
                    player.distance,
                    0.000001F);
            assertEquals(
                    1,
                    player.setCalls);

            runtime.playerFallDistanceControl(
                    player);
            assertEquals(
                    1,
                    player.setCalls);

            runtime.featureCatalog()
                    .noFall()
                    .thresholdSetting()
                    .set(
                            3.0D);

            player.distance = 2.5F;
            runtime.playerFallDistanceControl(
                    player);
            assertEquals(
                    2.5F,
                    player.distance,
                    0.000001F);
            assertEquals(
                    1,
                    player.setCalls);

            player.distance = 3.0F;
            runtime.playerFallDistanceControl(
                    player);
            assertEquals(
                    3.0F,
                    player.distance,
                    0.000001F);
            assertEquals(
                    1,
                    player.setCalls);

            player.distance = 3.25F;
            runtime.playerFallDistanceControl(
                    player);
            assertEquals(
                    0.0F,
                    player.distance,
                    0.000001F);
            assertEquals(
                    2,
                    player.setCalls);

            controller.disable(
                    Minecraft189NoFallModule.ID);
            assertFalse(
                    runtime.featureCatalog()
                            .noFall()
                            .active());

            player.distance = 4.5F;
            runtime.playerFallDistanceControl(
                    player);
            assertEquals(
                    4.5F,
                    player.distance,
                    0.000001F);
            assertEquals(
                    2,
                    player.setCalls);

            runtime.playerFallDistanceControl(
                    null);
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189NoFallModule.ID));
        assertNull(
                settings.find(
                        Minecraft189NoFallModule.THRESHOLD_SETTING_ID));
        assertNull(
                categories.find(
                        Minecraft189FeatureCatalog.MOVEMENT_CATEGORY_ID));
    }

    @Test
    void optionalAirborneAndSneakGuardsPreserveFallDistanceWhenUnqualified() {
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
            final Minecraft189NoFallModule noFall = runtime.featureCatalog().noFall();
            final TestPlayer player = new TestPlayer();
            assertFalse(noFall.airborneOnlySetting().get().booleanValue());
            assertFalse(noFall.pauseWhileSneakingSetting().get().booleanValue());
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189NoFallModule.AIRBORNE_ONLY_SETTING_ID));
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189NoFallModule.PAUSE_WHILE_SNEAKING_SETTING_ID));
            controller.enable(Minecraft189NoFallModule.ID);
            noFall.thresholdSetting().set(2.0D);
            player.distance = 3.0F;
            runtime.playerFallDistanceControl(player);
            assertEquals(0.0F, player.distance, 0.000001F); // Legacy default.

            noFall.airborneOnlySetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189NoFallModule.AIRBORNE_ONLY_SETTING_ID));
            player.distance = 3.0F;
            runtime.playerFallDistanceControl(player); // Missing mapped authority.
            assertEquals(3.0F, player.distance, 0.000001F);
            runtime.playerMovementState().update(true, false, false);
            runtime.playerFallDistanceControl(player);
            assertEquals(3.0F, player.distance, 0.000001F); // Grounded.
            runtime.playerMovementState().update(false, false, false);
            runtime.playerFallDistanceControl(player);
            assertEquals(0.0F, player.distance, 0.000001F);
            player.distance = 2.0F;
            runtime.playerFallDistanceControl(player);
            assertEquals(2.0F, player.distance, 0.000001F); // Threshold unchanged.

            noFall.pauseWhileSneakingSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189NoFallModule.PAUSE_WHILE_SNEAKING_SETTING_ID));
            player.distance = 3.0F;
            runtime.playerMovementState().update(false, true, false);
            runtime.playerFallDistanceControl(player);
            assertEquals(3.0F, player.distance, 0.000001F);
            runtime.playerMovementState().update(false, false, false);
            runtime.playerFallDistanceControl(player);
            assertEquals(0.0F, player.distance, 0.000001F);

            noFall.airborneOnlySetting().set(Boolean.FALSE);
            runtime.playerMovementState().update(true, false, false);
            player.distance = 3.0F;
            runtime.playerFallDistanceControl(player);
            assertEquals(0.0F, player.distance, 0.000001F); // Independent gate.
            runtime.playerMovementState().update(true, true, false);
            player.distance = 3.0F;
            runtime.playerFallDistanceControl(player);
            assertEquals(3.0F, player.distance, 0.000001F);

            runtime.playerMovementState().clear();
            noFall.apply(player); // Legacy one-arg overload fails closed.
            assertEquals(3.0F, player.distance, 0.000001F);
            noFall.pauseWhileSneakingSetting().set(Boolean.FALSE);
            noFall.apply(player); // Old direct API and baseline restored.
            assertEquals(0.0F, player.distance, 0.000001F);
            controller.disable(Minecraft189NoFallModule.ID);
            player.distance = 4.0F;
            runtime.playerFallDistanceControl(player);
            assertEquals(4.0F, player.distance, 0.000001F);
        } finally {
            runtime.close();
        }
        assertNull(settings.find(Minecraft189NoFallModule.AIRBORNE_ONLY_SETTING_ID));
        assertNull(settings.find(
                Minecraft189NoFallModule.PAUSE_WHILE_SNEAKING_SETTING_ID));
        assertNull(modules.find(Minecraft189NoFallModule.ID));
    }

    private static final class TestPlayer
            implements Minecraft189PlayerFallDistanceControl {
        private float distance;
        private int setCalls;

        @Override
        public float customMcFallDistance() {
            return distance;
        }

        @Override
        public void customMcSetFallDistance(
                final float distance) {
            setCalls++;
            this.distance = distance;
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
