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

final class Minecraft189StepModuleTest {
    @Test
    void stepUsesConfiguredHeightAndRestoresVanillaWhenDisabled() {
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
                            Minecraft189StepModule.ID));

            final TestPlayer player = new TestPlayer();
            player.height = 1.25F;
            runtime.playerStepControl(player);
            assertEquals(
                    Minecraft189StepModule.VANILLA_STEP_HEIGHT,
                    player.height,
                    0.000001F);

            controller.enable(Minecraft189StepModule.ID);
            assertTrue(runtime.featureCatalog().step().active());

            runtime.playerStepControl(player);
            assertEquals(1.0F, player.height, 0.000001F);

            runtime.featureCatalog()
                    .step()
                    .heightPercentSetting()
                    .set(150);
            assertEquals(
                    "150",
                    settings.snapshotEncoded()
                            .get(Minecraft189StepModule.HEIGHT_SETTING_ID));
            runtime.playerStepControl(player);
            assertEquals(1.5F, player.height, 0.000001F);

            controller.disable(Minecraft189StepModule.ID);
            assertFalse(runtime.featureCatalog().step().active());
            runtime.playerStepControl(player);
            assertEquals(
                    Minecraft189StepModule.VANILLA_STEP_HEIGHT,
                    player.height,
                    0.000001F);

            runtime.playerStepControl(null);
        } finally {
            runtime.close();
        }

        assertNull(modules.find(Minecraft189StepModule.ID));
        assertNull(settings.find(Minecraft189StepModule.HEIGHT_SETTING_ID));
        assertNull(
                categories.find(
                        Minecraft189FeatureCatalog.MOVEMENT_CATEGORY_ID));
    }

    @Test
    void stepGroundAndSneakGatesRestoreVanillaOnFailedMovementAuthority() {
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
            final Minecraft189StepModule step = runtime.featureCatalog().step();
            final TestPlayer player = new TestPlayer();
            assertFalse(step.groundOnlySetting().get().booleanValue());
            assertFalse(step.pauseWhileSneakingSetting().get().booleanValue());
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189StepModule.GROUND_ONLY_SETTING_ID));
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189StepModule.PAUSE_WHILE_SNEAKING_SETTING_ID));
            controller.enable(Minecraft189StepModule.ID);
            step.heightPercentSetting().set(180);

            // Legacy default: elevated step height without movement state.
            runtime.playerStepControl(player);
            assertEquals(1.8F, player.height, 0.000001F);
            step.groundOnlySetting().set(Boolean.TRUE);
            runtime.playerStepControl(player);
            assertEquals(0.6F, player.height, 0.000001F);
            runtime.playerMovementState().update(false, false, false);
            runtime.playerStepControl(player);
            assertEquals(0.6F, player.height, 0.000001F);
            runtime.playerMovementState().update(true, true, false);
            runtime.playerStepControl(player);
            assertEquals(1.8F, player.height, 0.000001F); // Ground gate alone.

            step.pauseWhileSneakingSetting().set(Boolean.TRUE);
            runtime.playerStepControl(player);
            assertEquals(0.6F, player.height, 0.000001F);
            runtime.playerMovementState().update(true, false, false);
            runtime.playerStepControl(player);
            assertEquals(1.8F, player.height, 0.000001F);
            runtime.playerMovementState().clear();
            runtime.playerStepControl(player);
            assertEquals(0.6F, player.height, 0.000001F);

            // Sneaking suppression independently of the ground gate.
            step.groundOnlySetting().set(Boolean.FALSE);
            runtime.playerMovementState().update(false, false, false);
            runtime.playerStepControl(player);
            assertEquals(1.8F, player.height, 0.000001F);
            runtime.playerMovementState().update(false, true, false);
            runtime.playerStepControl(player);
            assertEquals(0.6F, player.height, 0.000001F);

            // Live OFF returns exact legacy behavior even with absent state.
            step.pauseWhileSneakingSetting().set(Boolean.FALSE);
            runtime.playerMovementState().clear();
            runtime.playerStepControl(player);
            assertEquals(1.8F, player.height, 0.000001F);
            step.groundOnlySetting().set(Boolean.TRUE);
            step.apply(player); // Legacy overload fails closed safely.
            assertEquals(0.6F, player.height, 0.000001F);
            step.groundOnlySetting().set(Boolean.FALSE);
            step.apply(player); // Default legacy overload unchanged.
            assertEquals(1.8F, player.height, 0.000001F);

            player.height = Float.NaN;
            runtime.playerStepControl(player);
            assertEquals(1.8F, player.height, 0.000001F);
            controller.disable(Minecraft189StepModule.ID);
            runtime.playerStepControl(player);
            assertEquals(0.6F, player.height, 0.000001F);
        } finally {
            runtime.close();
        }
        assertNull(settings.find(Minecraft189StepModule.GROUND_ONLY_SETTING_ID));
        assertNull(settings.find(
                Minecraft189StepModule.PAUSE_WHILE_SNEAKING_SETTING_ID));
        assertNull(modules.find(Minecraft189StepModule.ID));
    }

    private static final class TestPlayer
            implements Minecraft189PlayerStepControl {
        private float height =
                Minecraft189StepModule.VANILLA_STEP_HEIGHT;

        @Override
        public float customMcStepHeight() {
            return height;
        }

        @Override
        public void customMcSetStepHeight(
                final float height) {
            this.height = height;
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
