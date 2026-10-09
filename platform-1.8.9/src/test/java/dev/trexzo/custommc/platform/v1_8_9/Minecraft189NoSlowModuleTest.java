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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class Minecraft189NoSlowModuleTest {
    @Test
    void noSlowRestoresVanillaItemUseMovementFactorOnlyWhileEnabled() {
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
                            Minecraft189NoSlowModule.ID));
            assertNotNull(
                    settings.find(
                            Minecraft189NoSlowModule.SPEED_PERCENT_SETTING_ID));
            assertEquals(
                    Minecraft189NoSlowModule.DEFAULT_SPEED_PERCENT,
                    runtime.featureCatalog()
                            .noSlow()
                            .speedPercentSetting()
                            .get()
                            .intValue());
            assertEquals(
                    0.2F,
                    runtime.adjustNoSlowMovement(
                            0.2F));
            assertEquals(
                    -0.15F,
                    runtime.adjustNoSlowMovement(
                            -0.15F));

            controller.enable(
                    Minecraft189NoSlowModule.ID);
            assertTrue(
                    runtime.featureCatalog()
                            .noSlow()
                            .active());
            assertEquals(
                    1.0F,
                    runtime.adjustNoSlowMovement(
                            0.2F),
                    0.000001F);
            assertEquals(
                    -0.75F,
                    runtime.adjustNoSlowMovement(
                            -0.15F),
                    0.000001F);

            runtime.featureCatalog()
                    .noSlow()
                    .speedPercentSetting()
                    .set(
                            60);
            assertEquals(
                    0.6F,
                    runtime.adjustNoSlowMovement(
                            0.2F),
                    0.000001F);
            assertEquals(
                    -0.45F,
                    runtime.adjustNoSlowMovement(
                            -0.15F),
                    0.000001F);

            controller.disable(
                    Minecraft189NoSlowModule.ID);
            assertEquals(
                    0.2F,
                    runtime.adjustNoSlowMovement(
                            0.2F));
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189NoSlowModule.ID));
        assertNull(
                settings.find(
                        Minecraft189NoSlowModule.SPEED_PERCENT_SETTING_ID));
        assertNull(
                categories.find(
                        Minecraft189FeatureCatalog.MOVEMENT_CATEGORY_ID));
    }

    @Test
    void airborneNoSlowOverrideUsesOnlyConfirmedAirborneMappedState() {
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
            final Minecraft189NoSlowModule module = runtime.featureCatalog().noSlow();
            assertFalse(module.airborneOverrideSetting().get().booleanValue());
            assertEquals(100, module.airborneSpeedPercentSetting().get().intValue());
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189NoSlowModule.AIRBORNE_OVERRIDE_SETTING_ID));
            assertEquals("100", settings.snapshotEncoded().get(
                    Minecraft189NoSlowModule.AIRBORNE_SPEED_SETTING_ID));
            controller.enable(Minecraft189NoSlowModule.ID);
            module.speedPercentSetting().set(60);
            module.airborneSpeedPercentSetting().set(30);
            runtime.playerMovementState().update(false, true, false);
            assertEquals(0.60F, runtime.adjustNoSlowMovement(0.20F),
                    0.000001F); // Default OFF even while airborne.
            assertEquals(-0.45F, runtime.adjustNoSlowMovement(-0.15F),
                    0.000001F);

            module.airborneOverrideSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189NoSlowModule.AIRBORNE_OVERRIDE_SETTING_ID));
            assertEquals("30", settings.snapshotEncoded().get(
                    Minecraft189NoSlowModule.AIRBORNE_SPEED_SETTING_ID));
            assertEquals(0.30F, runtime.adjustNoSlowMovement(0.20F),
                    0.000001F);
            assertEquals(-0.225F, runtime.adjustNoSlowMovement(-0.15F),
                    0.000001F);
            runtime.playerMovementState().update(true, false, false);
            assertEquals(0.60F, runtime.adjustNoSlowMovement(0.20F),
                    0.000001F);
            runtime.playerMovementState().clear();
            assertEquals(0.60F, runtime.adjustNoSlowMovement(0.20F),
                    0.000001F);
            // Legacy overload cannot infer airborne state and remains
            // tied to original Speed %.
            assertEquals(0.60F, module.adjustSlowedMovement(0.20F),
                    0.000001F);

            // Both percentages may be updated live and do not leak state.
            module.speedPercentSetting().set(100);
            module.airborneSpeedPercentSetting().set(20);
            runtime.playerMovementState().update(false, false, false);
            assertEquals(0.20F, runtime.adjustNoSlowMovement(0.20F),
                    0.000001F);
            runtime.playerMovementState().update(true, false, false);
            assertEquals(1.00F, runtime.adjustNoSlowMovement(0.20F),
                    0.000001F);
            module.airborneOverrideSetting().set(Boolean.FALSE);
            runtime.playerMovementState().update(false, false, false);
            assertEquals(1.00F, runtime.adjustNoSlowMovement(0.20F),
                    0.000001F);

            controller.disable(Minecraft189NoSlowModule.ID);
            assertEquals(0.20F, runtime.adjustNoSlowMovement(0.20F),
                    0.000001F);
            assertThrows(IllegalArgumentException.class,
                    () -> module.airborneSpeedPercentSetting().set(19));
            assertThrows(IllegalArgumentException.class,
                    () -> module.airborneSpeedPercentSetting().set(101));
        } finally {
            runtime.close();
        }
        assertNull(settings.find(
                Minecraft189NoSlowModule.AIRBORNE_OVERRIDE_SETTING_ID));
        assertNull(settings.find(
                Minecraft189NoSlowModule.AIRBORNE_SPEED_SETTING_ID));
        assertNull(modules.find(Minecraft189NoSlowModule.ID));
    }

    @Test
    void pauseWhileSneakingRestoresOriginalSlowdownAndRejectsUnknownMovement() {
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
            final Minecraft189NoSlowModule module = runtime.featureCatalog().noSlow();
            assertFalse(module.pauseWhileSneakingSetting().get().booleanValue());
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189NoSlowModule.PAUSE_WHILE_SNEAKING_SETTING_ID));
            controller.enable(Minecraft189NoSlowModule.ID);
            module.speedPercentSetting().set(80);
            runtime.playerMovementState().update(true, true, false);
            assertEquals(0.80F, runtime.adjustNoSlowMovement(0.20F),
                    0.000001F); // Default disabled preserves original override.

            module.pauseWhileSneakingSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189NoSlowModule.PAUSE_WHILE_SNEAKING_SETTING_ID));
            assertEquals(0.20F, runtime.adjustNoSlowMovement(0.20F),
                    0.000001F); // Sneaking preserves vanilla slowdown.
            assertEquals(-0.15F, runtime.adjustNoSlowMovement(-0.15F),
                    0.000001F);
            runtime.playerMovementState().update(true, false, false);
            assertEquals(0.80F, runtime.adjustNoSlowMovement(0.20F),
                    0.000001F); // Confirmed non-sneaking still uses No Slow.

            module.airborneOverrideSetting().set(Boolean.TRUE);
            module.airborneSpeedPercentSetting().set(40);
            runtime.playerMovementState().update(false, false, false);
            assertEquals(0.40F, runtime.adjustNoSlowMovement(0.20F),
                    0.000001F); // Air override works when not sneaking.
            runtime.playerMovementState().update(false, true, false);
            assertEquals(0.20F, runtime.adjustNoSlowMovement(0.20F),
                    0.000001F); // Sneak wins over air override.

            runtime.playerMovementState().clear();
            assertEquals(0.20F, runtime.adjustNoSlowMovement(0.20F),
                    0.000001F); // Unknown movement fails closed.
            assertEquals(0.20F, module.adjustSlowedMovement(0.20F),
                    0.000001F); // Legacy no-snapshot entry fails closed.
            module.pauseWhileSneakingSetting().set(Boolean.FALSE);
            assertEquals(0.80F, runtime.adjustNoSlowMovement(0.20F),
                    0.000001F); // Live toggle restores old behavior.

            controller.disable(Minecraft189NoSlowModule.ID);
            assertEquals(0.20F, runtime.adjustNoSlowMovement(0.20F),
                    0.000001F);
        } finally {
            runtime.close();
        }
        assertNull(settings.find(
                Minecraft189NoSlowModule.PAUSE_WHILE_SNEAKING_SETTING_ID));
        assertNull(modules.find(Minecraft189NoSlowModule.ID));
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
