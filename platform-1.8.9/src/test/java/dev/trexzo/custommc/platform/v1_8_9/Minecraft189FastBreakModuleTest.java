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

final class Minecraft189FastBreakModuleTest {
    @Test
    void fastBreakWritesZeroOnlyWhileEnabledAndOwnsPlayerLifecycle() {
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
                    ModuleState.DISABLED,
                    controller.stateOf(
                            Minecraft189FastBreakModule.ID));
            assertNotNull(
                    settings.find(
                            Minecraft189FastBreakModule.DELAY_SETTING_ID));
            assertEquals(
                    Minecraft189FastBreakModule.DEFAULT_DELAY,
                    runtime.featureCatalog()
                            .fastBreak()
                            .delaySetting()
                            .get()
                            .intValue());

            final TestController live =
                    new TestController();
            live.delay = 4;

            runtime.playerControllerBreakControl(
                    live);
            assertEquals(
                    4,
                    live.delay);
            assertEquals(
                    0,
                    live.setCalls);

            controller.enable(
                    Minecraft189FastBreakModule.ID);
            assertTrue(
                    runtime.featureCatalog()
                            .fastBreak()
                            .active());

            runtime.playerControllerBreakControl(
                    live);
            assertEquals(
                    0,
                    live.delay);
            assertEquals(
                    1,
                    live.setCalls);

            runtime.featureCatalog()
                    .fastBreak()
                    .delaySetting()
                    .set(
                            2);
            live.delay = 3;
            runtime.playerControllerBreakControl(
                    live);
            assertEquals(
                    2,
                    live.delay);
            assertEquals(
                    2,
                    live.setCalls);

            controller.disable(
                    Minecraft189FastBreakModule.ID);
            assertFalse(
                    runtime.featureCatalog()
                            .fastBreak()
                            .active());

            live.delay = 5;
            runtime.playerControllerBreakControl(
                    live);
            assertEquals(
                    5,
                    live.delay);
            assertEquals(
                    2,
                    live.setCalls);

            runtime.playerControllerBreakControl(
                    null);
            assertEquals(
                    2,
                    live.setCalls);
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189FastBreakModule.ID));
        assertNull(
                settings.find(
                        Minecraft189FastBreakModule.DELAY_SETTING_ID));
        assertNull(settings.find(
                Minecraft189FastBreakModule.REQUIRE_ATTACK_HELD_SETTING_ID));
        assertNull(settings.find(
                Minecraft189FastBreakModule.PAUSE_WHILE_SNEAKING_SETTING_ID));
        assertNull(
                categories.find(
                        Minecraft189FeatureCatalog.PLAYER_CATEGORY_ID));
    }

    @Test
    void fastBreakConditionalsUseLiveAttackAndSneakWithoutChangingDefaults() {
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
            final Minecraft189FastBreakModule fastBreak =
                    runtime.featureCatalog().fastBreak();
            assertFalse(fastBreak.requireAttackHeldSetting().get().booleanValue());
            assertFalse(fastBreak.pauseWhileSneakingSetting().get().booleanValue());
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189FastBreakModule.REQUIRE_ATTACK_HELD_SETTING_ID));
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189FastBreakModule.PAUSE_WHILE_SNEAKING_SETTING_ID));
            controller.enable(Minecraft189FastBreakModule.ID);
            fastBreak.delaySetting().set(2);
            final TestController live = new TestController();
            live.delay = 5;
            // Existing behavior stays active without input/movement authority.
            runtime.playerControllerBreakControl(live);
            assertEquals(2, live.delay);
            assertEquals(1, live.setCalls);
            fastBreak.requireAttackHeldSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189FastBreakModule.REQUIRE_ATTACK_HELD_SETTING_ID));
            live.delay = 5;
            runtime.playerControllerBreakControl(live);
            assertEquals(5, live.delay);
            assertEquals(1, live.setCalls);

            runtime.inputState().pointerButton(
                    Minecraft189ClickRateTracker.LEFT_BUTTON, true);
            runtime.playerControllerBreakControl(live);
            assertEquals(2, live.delay);
            assertEquals(2, live.setCalls);

            fastBreak.pauseWhileSneakingSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189FastBreakModule.PAUSE_WHILE_SNEAKING_SETTING_ID));
            live.delay = 5;
            runtime.playerControllerBreakControl(live);
            assertEquals(5, live.delay);
            assertEquals(2, live.setCalls); // No movement snapshot.

            runtime.playerMovementState().update(true, true, false);
            runtime.playerControllerBreakControl(live);
            assertEquals(2, live.setCalls); // Sneaking suppresses Fast Break.

            runtime.playerMovementState().update(false, false, false);
            runtime.playerControllerBreakControl(live);
            assertEquals(2, live.delay);
            assertEquals(3, live.setCalls); // Airborne unsneaking works.

            runtime.inputState().pointerButton(
                    Minecraft189ClickRateTracker.LEFT_BUTTON, false);
            live.delay = 5;
            runtime.playerControllerBreakControl(live);
            assertEquals(5, live.delay);
            assertEquals(3, live.setCalls);
            runtime.inputState().pointerButton(
                    Minecraft189ClickRateTracker.LEFT_BUTTON, true);
            runtime.playerMovementState().clear();
            runtime.playerControllerBreakControl(live);
            assertEquals(3, live.setCalls); // Missing state fails closed.

            fastBreak.pauseWhileSneakingSetting().set(Boolean.FALSE);
            runtime.playerControllerBreakControl(live);
            assertEquals(4, live.setCalls);
            fastBreak.requireAttackHeldSetting().set(Boolean.FALSE);
            runtime.inputState().pointerButton(
                    Minecraft189ClickRateTracker.LEFT_BUTTON, false);
            live.delay = 5;
            runtime.playerControllerBreakControl(live);
            assertEquals(2, live.delay);
            assertEquals(5, live.setCalls); // Both settings OFF restores default.

            controller.disable(Minecraft189FastBreakModule.ID);
            live.delay = 5;
            runtime.playerControllerBreakControl(live);
            assertEquals(5, live.delay);
            assertEquals(5, live.setCalls);
        } finally {
            runtime.close();
        }
        assertNull(settings.find(
                Minecraft189FastBreakModule.REQUIRE_ATTACK_HELD_SETTING_ID));
        assertNull(settings.find(
                Minecraft189FastBreakModule.PAUSE_WHILE_SNEAKING_SETTING_ID));
    }

    @Test
    void existingFastBreakCallerOverloadFailsClosedForOptionalGates() {
        final Minecraft189FastBreakModule module = new Minecraft189FastBreakModule();
        final TestController live = new TestController();
        module.onEnable();
        module.apply(live);
        assertEquals(1, live.setCalls);
        module.requireAttackHeldSetting().set(Boolean.TRUE);
        module.apply(live);
        assertEquals(1, live.setCalls); // Legacy caller has no attack state.
        module.requireAttackHeldSetting().set(Boolean.FALSE);
        module.pauseWhileSneakingSetting().set(Boolean.TRUE);
        module.apply(live);
        assertEquals(1, live.setCalls); // Legacy caller has no mapped sneak state.
        module.pauseWhileSneakingSetting().set(Boolean.FALSE);
        module.apply(live);
        assertEquals(2, live.setCalls);
        module.onDisable();
        module.apply(live);
        assertEquals(2, live.setCalls);
    }

    @Test
    void fastBreakGroundOnlyUsesMappedMovementWithoutChangingDefault() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final ServiceRegistry services = new ServiceRegistry();
        services.register(RenderPipeline.class, new RenderPipeline());
        final Minecraft189Platform platform = new Minecraft189Platform();
        platform.attach(new PlatformContext(
                new EventBus(), modules, controller, services));
        final Minecraft189HostRuntime runtime = Minecraft189HostRuntime.install(
                platform, new ModulePresentationRegistry(),
                new ModuleCategoryRegistry(), new ModuleSettingRegistry(modules, settings),
                null, null, settings, new SettingPresentationRegistry(),
                new NoOpHost());
        try {
            final Minecraft189FastBreakModule fastBreak =
                    runtime.featureCatalog().fastBreak();
            final TestController live = new TestController();
            controller.enable(Minecraft189FastBreakModule.ID);
            assertFalse(fastBreak.groundOnlySetting().get().booleanValue());
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189FastBreakModule.GROUND_ONLY_SETTING_ID));

            // Legacy output is unchanged with the new option OFF,
            // including missing movement authority or airborne state.
            live.delay = 5;
            runtime.playerControllerBreakControl(live);
            assertEquals(0, live.delay);
            assertEquals(1, live.setCalls);
            runtime.playerMovementState().update(false, true, false);
            live.delay = 5;
            runtime.playerControllerBreakControl(live);
            assertEquals(2, live.setCalls);

            fastBreak.groundOnlySetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189FastBreakModule.GROUND_ONLY_SETTING_ID));
            live.delay = 5;
            runtime.playerControllerBreakControl(live);
            assertEquals(5, live.delay);
            assertEquals(2, live.setCalls);  // Airborne.
            runtime.playerMovementState().clear();
            runtime.playerControllerBreakControl(live);
            assertEquals(2, live.setCalls);  // Missing state.
            runtime.playerMovementState().update(true, true, true);
            runtime.playerControllerBreakControl(live);
            assertEquals(0, live.delay);
            assertEquals(3, live.setCalls);  // Ground Only does not forbid sneak.
            fastBreak.pauseWhileSneakingSetting().set(Boolean.TRUE);
            live.delay = 5;
            runtime.playerControllerBreakControl(live);
            assertEquals(5, live.delay);
            assertEquals(3, live.setCalls);  // Independent sneak gate.
            runtime.playerMovementState().update(true, false, false);
            runtime.playerControllerBreakControl(live);
            assertEquals(0, live.delay);
            assertEquals(4, live.setCalls);

            // The legacy overload lacks the required movement authority.
            fastBreak.apply(live);
            assertEquals(4, live.setCalls);
            fastBreak.groundOnlySetting().set(Boolean.FALSE);
            fastBreak.pauseWhileSneakingSetting().set(Boolean.FALSE);
            runtime.playerMovementState().clear();
            runtime.playerControllerBreakControl(live);
            assertEquals(5, live.setCalls);
            controller.disable(Minecraft189FastBreakModule.ID);
            runtime.playerControllerBreakControl(live);
            assertEquals(5, live.setCalls);
        } finally {
            runtime.close();
        }
        assertNull(settings.find(
                Minecraft189FastBreakModule.GROUND_ONLY_SETTING_ID));
        assertNull(modules.find(Minecraft189FastBreakModule.ID));
    }

    @Test
    void airborneDelayOverrideUsesOnlyConfirmedAirborneMovement() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final ServiceRegistry services = new ServiceRegistry();
        services.register(RenderPipeline.class, new RenderPipeline());
        final Minecraft189Platform platform = new Minecraft189Platform();
        platform.attach(new PlatformContext(
                new EventBus(), modules, controller, services));
        final Minecraft189HostRuntime runtime = Minecraft189HostRuntime.install(
                platform, new ModulePresentationRegistry(),
                new ModuleCategoryRegistry(),
                new ModuleSettingRegistry(modules, settings),
                null, null, settings, new SettingPresentationRegistry(),
                new NoOpHost());
        try {
            final Minecraft189FastBreakModule module =
                    runtime.featureCatalog().fastBreak();
            final TestController live = new TestController();
            assertFalse(module.airborneOverrideSetting().get().booleanValue());
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189FastBreakModule.AIRBORNE_OVERRIDE_SETTING_ID));
            assertEquals("0", settings.snapshotEncoded().get(
                    Minecraft189FastBreakModule.AIRBORNE_DELAY_SETTING_ID));
            controller.enable(Minecraft189FastBreakModule.ID);
            module.delaySetting().set(2);
            module.airborneDelaySetting().set(4);

            runtime.playerMovementState().update(false, false, false);
            runtime.playerControllerBreakControl(live);
            assertEquals(2, live.delay); // Original setting while override OFF.

            module.airborneOverrideSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189FastBreakModule.AIRBORNE_OVERRIDE_SETTING_ID));
            assertEquals("4", settings.snapshotEncoded().get(
                    Minecraft189FastBreakModule.AIRBORNE_DELAY_SETTING_ID));
            runtime.playerControllerBreakControl(live);
            assertEquals(4, live.delay); // Confirmed airborne uses air delay.

            runtime.playerMovementState().update(true, false, false);
            runtime.playerControllerBreakControl(live);
            assertEquals(2, live.delay); // Grounded retains base delay.
            runtime.playerMovementState().clear();
            runtime.playerControllerBreakControl(live);
            assertEquals(2, live.delay); // Missing snapshot retains base delay.
            module.apply(live);
            assertEquals(2, live.delay); // Legacy overload cannot infer air.

            module.groundOnlySetting().set(Boolean.TRUE);
            runtime.playerMovementState().update(false, false, false);
            final int prior = live.setCalls;
            runtime.playerControllerBreakControl(live);
            assertEquals(prior, live.setCalls); // Ground Only wins.
            module.groundOnlySetting().set(Boolean.FALSE);
            module.pauseWhileSneakingSetting().set(Boolean.TRUE);
            runtime.playerMovementState().update(false, true, false);
            runtime.playerControllerBreakControl(live);
            assertEquals(prior, live.setCalls); // Sneak gate wins.
            module.pauseWhileSneakingSetting().set(Boolean.FALSE);
            runtime.playerControllerBreakControl(live);
            assertEquals(4, live.delay); // Composes with original gates.

            module.airborneOverrideSetting().set(Boolean.FALSE);
            runtime.playerControllerBreakControl(live);
            assertEquals(2, live.delay); // Live OFF restores old behavior.
            controller.disable(Minecraft189FastBreakModule.ID);
            final int stoppedCalls = live.setCalls;
            runtime.playerControllerBreakControl(live);
            assertEquals(stoppedCalls, live.setCalls);
            assertThrows(IllegalArgumentException.class,
                    () -> module.airborneDelaySetting().set(-1));
            assertThrows(IllegalArgumentException.class,
                    () -> module.airborneDelaySetting().set(6));
        } finally {
            runtime.close();
        }
        assertNull(settings.find(
                Minecraft189FastBreakModule.AIRBORNE_OVERRIDE_SETTING_ID));
        assertNull(settings.find(
                Minecraft189FastBreakModule.AIRBORNE_DELAY_SETTING_ID));
        assertNull(modules.find(Minecraft189FastBreakModule.ID));
    }

    private static final class TestController
            implements Minecraft189BlockHitDelayControl {
        private int delay;
        private int setCalls;

        @Override
        public void customMcSetBlockHitDelay(
                final int delayTicks) {
            setCalls++;
            delay = delayTicks;
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
