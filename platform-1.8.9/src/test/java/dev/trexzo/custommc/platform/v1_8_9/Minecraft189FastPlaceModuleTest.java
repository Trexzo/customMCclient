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
import static org.junit.jupiter.api.Assertions.assertThrows;

final class Minecraft189FastPlaceModuleTest {
    @Test
    void fastPlaceReducesOnlyHigherDelayAndOwnsPlayerCategoryLifecycle() {
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
                            Minecraft189FeatureCatalog.PLAYER_CATEGORY_ID));
            assertEquals(
                    "Player",
                    categories.find(
                                    Minecraft189FeatureCatalog.PLAYER_CATEGORY_ID)
                            .displayName());
            assertEquals(
                    ModuleState.DISABLED,
                    controller.stateOf(
                            Minecraft189FastPlaceModule.ID));
            assertEquals(
                    4,
                    runtime.rightClickDelay(
                            4));

            controller.enable(
                    Minecraft189FastPlaceModule.ID);
            assertTrue(
                    runtime.featureCatalog()
                            .fastPlace()
                            .active());
            assertEquals(
                    0,
                    runtime.rightClickDelay(
                            4));
            assertEquals(
                    0,
                    runtime.rightClickDelay(
                            0));

            runtime.featureCatalog()
                    .fastPlace()
                    .delayTicksSetting()
                    .set(2);
            assertEquals(
                    2,
                    runtime.rightClickDelay(
                            4));
            assertEquals(
                    1,
                    runtime.rightClickDelay(
                            1));
            assertEquals(
                    "2",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189FastPlaceModule.DELAY_SETTING_ID));

            controller.disable(
                    Minecraft189FastPlaceModule.ID);
            assertFalse(
                    runtime.featureCatalog()
                            .fastPlace()
                            .active());
            assertEquals(
                    4,
                    runtime.rightClickDelay(
                            4));
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189FastPlaceModule.ID));
        assertNull(
                settings.find(
                        Minecraft189FastPlaceModule.DELAY_SETTING_ID));
        assertNull(settings.find(
                Minecraft189FastPlaceModule.REQUIRE_USE_HELD_SETTING_ID));
        assertNull(settings.find(
                Minecraft189FastPlaceModule.PAUSE_WHILE_SNEAKING_SETTING_ID));
        assertNull(
                categories.find(
                        Minecraft189FeatureCatalog.PLAYER_CATEGORY_ID));
    }

    @Test
    void fastPlaceHeldUseAndSneakSafetyGatesComposeWithLiveMappedInputs() {
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
                null, null, settings, new SettingPresentationRegistry(), new NoOpHost());
        try {
            final Minecraft189FastPlaceModule fastPlace =
                    runtime.featureCatalog().fastPlace();
            assertFalse(fastPlace.requireUseHeldSetting().get().booleanValue());
            assertFalse(fastPlace.pauseWhileSneakingSetting().get().booleanValue());
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189FastPlaceModule.REQUIRE_USE_HELD_SETTING_ID));
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189FastPlaceModule.PAUSE_WHILE_SNEAKING_SETTING_ID));
            controller.enable(Minecraft189FastPlaceModule.ID);
            fastPlace.delayTicksSetting().set(1);
            // Legacy behavior: no held input or movement required if both OFF.
            assertEquals(1, runtime.rightClickDelay(4));
            assertEquals(0, runtime.rightClickDelay(0));

            fastPlace.requireUseHeldSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189FastPlaceModule.REQUIRE_USE_HELD_SETTING_ID));
            assertEquals(4, runtime.rightClickDelay(4));
            runtime.inputState().pointerButton(Minecraft189ClickRateTracker.RIGHT_BUTTON, true);
            assertEquals(1, runtime.rightClickDelay(4));
            assertEquals(0, runtime.rightClickDelay(0));

            fastPlace.pauseWhileSneakingSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189FastPlaceModule.PAUSE_WHILE_SNEAKING_SETTING_ID));
            assertEquals(4, runtime.rightClickDelay(4)); // No movement authority
            runtime.playerMovementState().update(true, true, false);
            assertEquals(4, runtime.rightClickDelay(4)); // Sneaking
            runtime.playerMovementState().update(false, false, false);
            assertEquals(1, runtime.rightClickDelay(4)); // Not sneaking, airborne
            runtime.inputState().pointerButton(Minecraft189ClickRateTracker.RIGHT_BUTTON, false);
            assertEquals(4, runtime.rightClickDelay(4)); // No physical use hold
            runtime.inputState().pointerButton(Minecraft189ClickRateTracker.RIGHT_BUTTON, true);
            runtime.playerMovementState().clear();
            assertEquals(4, runtime.rightClickDelay(4)); // Stale state cleared

            fastPlace.pauseWhileSneakingSetting().set(Boolean.FALSE);
            assertEquals(1, runtime.rightClickDelay(4)); // Missing state now allowed
            fastPlace.requireUseHeldSetting().set(Boolean.FALSE);
            runtime.inputState().pointerButton(Minecraft189ClickRateTracker.RIGHT_BUTTON, false);
            assertEquals(1, runtime.rightClickDelay(4)); // Default-off parity
            controller.disable(Minecraft189FastPlaceModule.ID);
            assertEquals(4, runtime.rightClickDelay(4));
        } finally {
            runtime.close();
        }
        assertNull(settings.find(Minecraft189FastPlaceModule.REQUIRE_USE_HELD_SETTING_ID));
        assertNull(settings.find(Minecraft189FastPlaceModule.PAUSE_WHILE_SNEAKING_SETTING_ID));
    }

    @Test
    void fastPlaceLegacyOverloadStaysUnchangedUntilSafetyGatesAreEnabled() {
        final Minecraft189FastPlaceModule module = new Minecraft189FastPlaceModule();
        module.onEnable();
        assertEquals(0, module.apply(4));
        module.delayTicksSetting().set(2);
        assertEquals(2, module.apply(4));
        module.requireUseHeldSetting().set(Boolean.TRUE);
        assertEquals(4, module.apply(4)); // Caller has no physical input
        module.requireUseHeldSetting().set(Boolean.FALSE);
        module.pauseWhileSneakingSetting().set(Boolean.TRUE);
        assertEquals(4, module.apply(4)); // Caller has no movement snapshot
        module.pauseWhileSneakingSetting().set(Boolean.FALSE);
        assertEquals(2, module.apply(4));
        module.onDisable();
        assertEquals(4, module.apply(4));
    }

    @Test
    void airborneFastPlaceDelayUsesMappedStateAndComposesWithExistingGates() {
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
            final Minecraft189FastPlaceModule place =
                    runtime.featureCatalog().fastPlace();
            assertFalse(place.airborneOverrideSetting().get().booleanValue());
            assertEquals(0, place.airborneDelaySetting().get().intValue());
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189FastPlaceModule.AIRBORNE_OVERRIDE_SETTING_ID));
            assertEquals("0", settings.snapshotEncoded().get(
                    Minecraft189FastPlaceModule.AIRBORNE_DELAY_SETTING_ID));
            controller.enable(Minecraft189FastPlaceModule.ID);
            place.delayTicksSetting().set(2);
            place.airborneDelaySetting().set(0);
            runtime.playerMovementState().update(false, false, false);
            assertEquals(2, runtime.rightClickDelay(4)); // Default OFF.
            place.airborneOverrideSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189FastPlaceModule.AIRBORNE_OVERRIDE_SETTING_ID));
            assertEquals(0, runtime.rightClickDelay(4));
            assertEquals(0, runtime.rightClickDelay(0));
            assertEquals(-1, runtime.rightClickDelay(-1)); // Preserve expired counters.
            runtime.playerMovementState().update(true, false, false);
            assertEquals(2, runtime.rightClickDelay(4));
            runtime.playerMovementState().clear();
            assertEquals(2, runtime.rightClickDelay(4)); // No inferred airborne state.
            assertEquals(2, place.apply(4)); // Legacy overload also grounded fallback.

            place.airborneDelaySetting().set(1);
            assertEquals("1", settings.snapshotEncoded().get(
                    Minecraft189FastPlaceModule.AIRBORNE_DELAY_SETTING_ID));
            runtime.playerMovementState().update(false, false, false);
            assertEquals(1, runtime.rightClickDelay(4));
            place.requireUseHeldSetting().set(Boolean.TRUE);
            assertEquals(4, runtime.rightClickDelay(4));
            runtime.inputState().pointerButton(
                    Minecraft189ClickRateTracker.RIGHT_BUTTON, true);
            assertEquals(1, runtime.rightClickDelay(4));
            place.pauseWhileSneakingSetting().set(Boolean.TRUE);
            runtime.playerMovementState().update(false, true, false);
            assertEquals(4, runtime.rightClickDelay(4));
            runtime.playerMovementState().update(false, false, false);
            assertEquals(1, runtime.rightClickDelay(4));
            runtime.playerMovementState().clear();
            assertEquals(4, runtime.rightClickDelay(4)); // Safety gate fails closed.
            place.pauseWhileSneakingSetting().set(Boolean.FALSE);
            assertEquals(2, runtime.rightClickDelay(4)); // No state = base delay.
            place.requireUseHeldSetting().set(Boolean.FALSE);
            place.airborneOverrideSetting().set(Boolean.FALSE);
            runtime.playerMovementState().update(false, false, false);
            assertEquals(2, runtime.rightClickDelay(4));
            controller.disable(Minecraft189FastPlaceModule.ID);
            assertEquals(4, runtime.rightClickDelay(4));
            assertThrows(IllegalArgumentException.class,
                    () -> place.airborneDelaySetting().set(-1));
            assertThrows(IllegalArgumentException.class,
                    () -> place.airborneDelaySetting().set(5));
        } finally {
            runtime.close();
        }
        assertNull(settings.find(
                Minecraft189FastPlaceModule.AIRBORNE_OVERRIDE_SETTING_ID));
        assertNull(settings.find(
                Minecraft189FastPlaceModule.AIRBORNE_DELAY_SETTING_ID));
        assertNull(modules.find(Minecraft189FastPlaceModule.ID));
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
