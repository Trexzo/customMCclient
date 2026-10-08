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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189AutoClickerModuleTest {
    @Test
    void autoClickerUsesPhysicalHoldAndTwentyTickCpsSchedule() {
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
            final Minecraft189AutoClickerModule autoClicker =
                    runtime.featureCatalog()
                            .autoClicker();

            assertEquals(
                    ModuleState.DISABLED,
                    controller.stateOf(
                            Minecraft189AutoClickerModule.ID));
            assertFalse(
                    runtime.shouldAutoClick());

            autoClicker.minCpsSetting().set(10);
            autoClicker.maxCpsSetting().set(10);
            assertTrue(
                    autoClicker.requireHoldSetting()
                            .get()
                            .booleanValue());
            assertFalse(autoClicker.requireForwardSetting()
                    .get().booleanValue());
            assertFalse(autoClicker.pauseWhileRightClickingSetting()
                    .get().booleanValue());
            assertFalse(autoClicker.requireNearbyPlayerSetting()
                    .get().booleanValue());
            assertEquals(4.0D,
                    autoClicker.maxPlayerDistanceSetting().get().doubleValue(), 0.00001D);
            assertEquals(
                    "10",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189AutoClickerModule.MIN_CPS_SETTING_ID));
            assertEquals(
                    "10",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189AutoClickerModule.MAX_CPS_SETTING_ID));
            assertEquals(
                    "true",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189AutoClickerModule.REQUIRE_HOLD_SETTING_ID));
            assertEquals("false", settings.snapshotEncoded()
                    .get(Minecraft189AutoClickerModule.REQUIRE_FORWARD_SETTING_ID));
            assertEquals("false", settings.snapshotEncoded()
                    .get(Minecraft189AutoClickerModule.PAUSE_WHILE_RIGHT_CLICKING_SETTING_ID));
            assertEquals("false", settings.snapshotEncoded()
                    .get(Minecraft189AutoClickerModule.PAUSE_WHILE_SNEAKING_SETTING_ID));
            assertEquals("false", settings.snapshotEncoded()
                    .get(Minecraft189AutoClickerModule.REQUIRE_NEARBY_PLAYER_SETTING_ID));
            assertEquals("4.0", settings.snapshotEncoded()
                    .get(Minecraft189AutoClickerModule.MAX_PLAYER_DISTANCE_SETTING_ID));

            controller.enable(
                    Minecraft189AutoClickerModule.ID);
            assertTrue(
                    autoClicker.active());

            for (int tick = 0; tick < 5; tick++) {
                assertFalse(
                        runtime.shouldAutoClick());
            }

            runtime.inputState()
                    .pointerButton(
                            Minecraft189ClickRateTracker.LEFT_BUTTON,
                            true);

            int generated = 0;
            for (int tick = 0; tick < 20; tick++) {
                if (runtime.shouldAutoClick()) {
                    generated++;
                }
            }
            assertEquals(
                    10,
                    generated);
            assertEquals(
                    10,
                    runtime.clickRateTracker()
                            .clicksPerSecond(
                                    Minecraft189ClickRateTracker.LEFT_BUTTON));

            runtime.inputState()
                    .pointerButton(
                            Minecraft189ClickRateTracker.LEFT_BUTTON,
                            false);
            assertFalse(
                    runtime.shouldAutoClick());

            autoClicker.requireHoldSetting()
                    .set(
                            Boolean.FALSE);
            int toggleGenerated = 0;
            for (int tick = 0; tick < 20; tick++) {
                if (runtime.shouldAutoClick()) {
                    toggleGenerated++;
                }
            }
            assertEquals(
                    10,
                    toggleGenerated);

            autoClicker.requireHoldSetting()
                    .set(
                            Boolean.TRUE);
            assertFalse(
                    runtime.shouldAutoClick());

            runtime.inputState()
                    .pointerButton(
                            Minecraft189ClickRateTracker.LEFT_BUTTON,
                            true);
            assertFalse(
                    runtime.shouldAutoClick());
            assertTrue(
                    runtime.shouldAutoClick());

            // M228: the new default-off gate uses real W input and resets
            // accumulated CPS credit each time physical forward is released.
            autoClicker.requireForwardSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded()
                    .get(Minecraft189AutoClickerModule.REQUIRE_FORWARD_SETTING_ID));
            assertFalse(runtime.shouldAutoClick());
            assertFalse(runtime.shouldAutoClick());
            runtime.inputState().key(LegacyKeyboardCodes.W, true);
            assertFalse(runtime.shouldAutoClick());
            assertTrue(runtime.shouldAutoClick());
            runtime.inputState().key(LegacyKeyboardCodes.W, false);
            assertFalse(runtime.shouldAutoClick());
            assertFalse(runtime.shouldAutoClick());
            runtime.inputState().key(LegacyKeyboardCodes.W, true);
            assertFalse(runtime.shouldAutoClick());
            assertTrue(runtime.shouldAutoClick());
            runtime.inputState().key(LegacyKeyboardCodes.W, false);
            autoClicker.requireForwardSetting().set(Boolean.FALSE);
            assertFalse(runtime.shouldAutoClick());
            assertTrue(runtime.shouldAutoClick());

            // M229: physical right hold suspends the click schedule and
            // resuming never consumes accumulated CPS phase immediately.
            autoClicker.pauseWhileRightClickingSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded()
                    .get(Minecraft189AutoClickerModule.PAUSE_WHILE_RIGHT_CLICKING_SETTING_ID));
            runtime.inputState().pointerButton(
                    Minecraft189ClickRateTracker.RIGHT_BUTTON, true);
            assertFalse(runtime.shouldAutoClick());
            assertFalse(runtime.shouldAutoClick());
            runtime.inputState().pointerButton(
                    Minecraft189ClickRateTracker.RIGHT_BUTTON, false);
            assertFalse(runtime.shouldAutoClick());
            assertTrue(runtime.shouldAutoClick());

            // Both independent gates must pass together.
            autoClicker.requireForwardSetting().set(Boolean.TRUE);
            runtime.inputState().pointerButton(
                    Minecraft189ClickRateTracker.RIGHT_BUTTON, true);
            runtime.inputState().key(LegacyKeyboardCodes.W, true);
            assertFalse(runtime.shouldAutoClick());
            runtime.inputState().pointerButton(
                    Minecraft189ClickRateTracker.RIGHT_BUTTON, false);
            assertFalse(runtime.shouldAutoClick());
            assertTrue(runtime.shouldAutoClick());
            runtime.inputState().key(LegacyKeyboardCodes.W, false);
            assertFalse(runtime.shouldAutoClick());

            // Default-off parity: right-clicking no longer suppresses CPS.
            autoClicker.requireForwardSetting().set(Boolean.FALSE);
            autoClicker.pauseWhileRightClickingSetting().set(Boolean.FALSE);
            runtime.inputState().pointerButton(
                    Minecraft189ClickRateTracker.RIGHT_BUTTON, true);
            assertFalse(runtime.shouldAutoClick());
            assertTrue(runtime.shouldAutoClick());
            runtime.inputState().pointerButton(
                    Minecraft189ClickRateTracker.RIGHT_BUTTON, false);

            // M233: this is proximity-only, never a claimed raycast.
            autoClicker.requireNearbyPlayerSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded()
                    .get(Minecraft189AutoClickerModule.REQUIRE_NEARBY_PLAYER_SETTING_ID));
            assertFalse(runtime.shouldAutoClick());
            assertFalse(runtime.shouldAutoClick()); // missing authority
            runtime.playerPositionState().update(0.0D, 0.0D, 0.0D);
            runtime.worldEntityPositionState().update(
                    new double[]{0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 4.0D});
            runtime.worldEntityKindState().update(new int[]{
                    Minecraft189WorldEntityKindState.LIVING
                            | Minecraft189WorldEntityKindState.PLAYER
                            | Minecraft189WorldEntityKindState.LOCAL_PLAYER,
                    Minecraft189WorldEntityKindState.LIVING
                            | Minecraft189WorldEntityKindState.PLAYER});
            runtime.nearestPlayerTargetState().update(
                    runtime.playerPositionState().snapshot(),
                    runtime.worldEntityPositionState().snapshot(),
                    runtime.worldEntityKindState().snapshot());
            assertFalse(runtime.shouldAutoClick());
            assertTrue(runtime.shouldAutoClick()); // inclusive 4 blocks

            autoClicker.maxPlayerDistanceSetting().set(3.5D);
            assertFalse(runtime.shouldAutoClick());
            assertFalse(runtime.shouldAutoClick());
            autoClicker.maxPlayerDistanceSetting().set(4.0D);
            assertFalse(runtime.shouldAutoClick());
            assertTrue(runtime.shouldAutoClick());

            runtime.nearestPlayerTargetState().clear();
            assertFalse(runtime.shouldAutoClick());
            assertFalse(runtime.shouldAutoClick());
            autoClicker.requireNearbyPlayerSetting().set(Boolean.FALSE);
            assertFalse(runtime.shouldAutoClick());
            assertTrue(runtime.shouldAutoClick());

            controller.disable(
                    Minecraft189AutoClickerModule.ID);
            assertFalse(
                    autoClicker.active());
            assertFalse(
                    runtime.shouldAutoClick());
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189AutoClickerModule.ID));
        assertNull(
                settings.find(
                        Minecraft189AutoClickerModule.MIN_CPS_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189AutoClickerModule.MAX_CPS_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189AutoClickerModule.REQUIRE_HOLD_SETTING_ID));
        assertNull(settings.find(
                Minecraft189AutoClickerModule.REQUIRE_FORWARD_SETTING_ID));
        assertNull(settings.find(
                Minecraft189AutoClickerModule.PAUSE_WHILE_RIGHT_CLICKING_SETTING_ID));
        assertNull(settings.find(
                Minecraft189AutoClickerModule.PAUSE_WHILE_SNEAKING_SETTING_ID));
        assertNull(settings.find(
                Minecraft189AutoClickerModule.REQUIRE_NEARBY_PLAYER_SETTING_ID));
        assertNull(settings.find(
                Minecraft189AutoClickerModule.MAX_PLAYER_DISTANCE_SETTING_ID));
        assertNull(settings.find(
                Minecraft189AutoClickerModule.RAMP_UP_SETTING_ID));
        assertNull(settings.find(
                Minecraft189AutoClickerModule.RAMP_UP_TICKS_SETTING_ID));
    }

    @Test
    void editingEitherCpsBoundResetsPendingPhaseAndTarget() {
        final Minecraft189AutoClickerModule module =
                new Minecraft189AutoClickerModule();
        module.minCpsSetting().set(10);
        module.maxCpsSetting().set(10);
        module.onEnable();
        assertFalse(module.shouldClick(true, false, false));
        assertTrue(module.shouldClick(true, false, false));
        // Credit at 10 CPS would complete on the next tick. Retuning
        // must discard that near-complete previous interval.
        assertFalse(module.shouldClick(true, false, false));
        module.minCpsSetting().set(4);
        module.maxCpsSetting().set(4);
        for (int tick = 0; tick < 4; tick++) {
            assertFalse(module.shouldClick(true, false, false));
        }
        assertTrue(module.shouldClick(true, false, false));

        // Change only Max during an in-progress 4 CPS interval.
        for (int tick = 0; tick < 4; tick++) {
            assertFalse(module.shouldClick(true, false, false));
        }
        module.maxCpsSetting().set(5);
        assertFalse(module.shouldClick(true, false, false));
        module.maxCpsSetting().set(4);
        for (int tick = 0; tick < 4; tick++) {
            assertFalse(module.shouldClick(true, false, false));
        }
        assertTrue(module.shouldClick(true, false, false));

        module.minCpsSetting().set(20);
        module.maxCpsSetting().set(20);
        assertTrue(module.shouldClick(true, false, false));
        assertTrue(module.shouldClick(true, false, false));
        module.onDisable();
        assertFalse(module.shouldClick(true, false, false));
        module.minCpsSetting().set(10);
        module.maxCpsSetting().set(10);
        module.onEnable();
        assertFalse(module.shouldClick(true, false, false));
        assertTrue(module.shouldClick(true, false, false));
        module.onDisable();
    }

    @Test
    void pauseWhileSneakingUsesCertifiedMovementAndResetsCpsPhase() {
        final Minecraft189AutoClickerModule module = new Minecraft189AutoClickerModule();
        final Minecraft189PlayerMovementState movement =
                new Minecraft189PlayerMovementState();
        assertFalse(module.pauseWhileSneakingSetting().get().booleanValue());
        module.minCpsSetting().set(10);
        module.maxCpsSetting().set(10);
        module.onEnable();

        // Default OFF must retain the original behavior without movement authority.
        assertFalse(module.shouldClick(true, false, false, null, null));
        assertTrue(module.shouldClick(true, false, false, null, null));
        module.pauseWhileSneakingSetting().set(Boolean.TRUE);
        assertFalse(module.shouldClick(true, false, false, null, null));
        assertFalse(module.shouldClick(true, false, false, null, movement.snapshot()));
        movement.update(true, true, false);
        assertFalse(module.shouldClick(true, false, false, null, movement.snapshot()));
        assertFalse(module.shouldClick(true, false, false, null, movement.snapshot()));

        // Resume on unsneak from zero CPS credit, not the pre-pause phase.
        movement.update(true, false, false);
        assertFalse(module.shouldClick(true, false, false, null, movement.snapshot()));
        assertTrue(module.shouldClick(true, false, false, null, movement.snapshot()));
        movement.clear();
        assertFalse(module.shouldClick(true, false, false, null, movement.snapshot()));

        module.pauseWhileSneakingSetting().set(Boolean.FALSE);
        assertFalse(module.shouldClick(true, false, false, null, null));
        assertTrue(module.shouldClick(true, false, false, null, null));
        module.onDisable();
        assertFalse(module.shouldClick(true, false, false, null, null));
    }

    @Test
    void runtimeAutoClickerUsesMappedSneakStateAndPersistsNewGate() {
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
            final Minecraft189AutoClickerModule clicker =
                    runtime.featureCatalog().autoClicker();
            clicker.minCpsSetting().set(10);
            clicker.maxCpsSetting().set(10);
            clicker.pauseWhileSneakingSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189AutoClickerModule.PAUSE_WHILE_SNEAKING_SETTING_ID));
            controller.enable(Minecraft189AutoClickerModule.ID);
            runtime.inputState().pointerButton(Minecraft189ClickRateTracker.LEFT_BUTTON, true);

            assertFalse(runtime.shouldAutoClick()); // movement unavailable
            assertFalse(runtime.shouldAutoClick());
            runtime.playerMovementState().update(true, true, false);
            assertFalse(runtime.shouldAutoClick());
            assertFalse(runtime.shouldAutoClick());
            runtime.playerMovementState().update(true, false, false);
            assertFalse(runtime.shouldAutoClick());
            assertTrue(runtime.shouldAutoClick());

            runtime.playerMovementState().update(true, true, false);
            assertFalse(runtime.shouldAutoClick());
            runtime.playerMovementState().clear();
            assertFalse(runtime.shouldAutoClick());
            clicker.pauseWhileSneakingSetting().set(Boolean.FALSE);
            assertFalse(runtime.shouldAutoClick());
            assertTrue(runtime.shouldAutoClick());

            controller.disable(Minecraft189AutoClickerModule.ID);
            assertFalse(runtime.shouldAutoClick());
        } finally {
            runtime.close();
        }
        assertNull(settings.find(
                Minecraft189AutoClickerModule.PAUSE_WHILE_SNEAKING_SETTING_ID));
    }

    @Test
    void rampUpStartsSlowConvergesToTargetAndResetsAcrossInputAndEdits() {
        final Minecraft189AutoClickerModule module =
                new Minecraft189AutoClickerModule();
        assertFalse(module.rampUpSetting().get().booleanValue());
        assertEquals(Integer.valueOf(20), module.rampUpTicksSetting().get());
        module.minCpsSetting().set(10);
        module.maxCpsSetting().set(10);
        module.onEnable();

        // Original steady 10 CPS always produces 10 clicks / 20 ticks.
        int steadyClicks = 0;
        for (int tick = 0; tick < 20; tick++) {
            if (module.shouldClick(true)) {
                steadyClicks++;
            }
        }
        assertEquals(10, steadyClicks);

        module.rampUpSetting().set(Boolean.TRUE);
        final int[] expectedFirstRampClicks = {8, 12, 15, 17, 19};
        int observedHits = 0;
        for (int tick = 1; tick <= 20; tick++) {
            if (module.shouldClick(true)) {
                assertEquals(expectedFirstRampClicks[observedHits], tick);
                observedHits++;
            }
        }
        assertEquals(5, observedHits);
        int steadyAfterRamp = 0;
        for (int tick = 0; tick < 20; tick++) {
            if (module.shouldClick(true)) {
                steadyAfterRamp++;
            }
        }
        assertEquals(10, steadyAfterRamp);

        // Invalid left input resets the entire pending CPS phase + ramp.
        assertFalse(module.shouldClick(false));
        for (int tick = 1; tick <= 7; tick++) {
            assertFalse(module.shouldClick(true));
        }
        assertTrue(module.shouldClick(true)); // ramp tick 8, not prior phase.

        // Live ramp length change must reset the credit as well.
        module.rampUpTicksSetting().set(10);
        for (int tick = 1; tick <= 5; tick++) {
            assertFalse(module.shouldClick(true));
        }
        assertTrue(module.shouldClick(true)); // ramp 10 ticks, first click at 6.

        // Toggle OFF does not retain ramp phase and restores 10 CPS instantly.
        module.rampUpSetting().set(Boolean.FALSE);
        assertFalse(module.shouldClick(true));
        assertTrue(module.shouldClick(true));

        // A one-tick ramp is equivalent to immediate full CPS.
        module.rampUpTicksSetting().set(1);
        module.rampUpSetting().set(Boolean.TRUE);
        assertFalse(module.shouldClick(true));
        assertTrue(module.shouldClick(true));

        // Either CPS bound edit also resets the ongoing ramp, as before.
        module.minCpsSetting().set(20);
        module.maxCpsSetting().set(20);
        assertTrue(module.shouldClick(true));
        assertTrue(module.shouldClick(true));
        assertThrows(IllegalArgumentException.class,
                () -> module.rampUpTicksSetting().set(0));
        assertThrows(IllegalArgumentException.class,
                () -> module.rampUpTicksSetting().set(101));
        module.onDisable();
        assertFalse(module.shouldClick(true));
        module.onEnable();
        assertTrue(module.shouldClick(true));
        module.onDisable();
    }

    @Test
    void cpsRampUsesMappedHostAndPersistsAndClosesBothSettings() {
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
            final Minecraft189AutoClickerModule module =
                    runtime.featureCatalog().autoClicker();
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189AutoClickerModule.RAMP_UP_SETTING_ID));
            assertEquals("20", settings.snapshotEncoded().get(
                    Minecraft189AutoClickerModule.RAMP_UP_TICKS_SETTING_ID));
            module.minCpsSetting().set(10);
            module.maxCpsSetting().set(10);
            module.rampUpSetting().set(Boolean.TRUE);
            module.rampUpTicksSetting().set(20);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189AutoClickerModule.RAMP_UP_SETTING_ID));
            controller.enable(Minecraft189AutoClickerModule.ID);
            runtime.inputState().pointerButton(
                    Minecraft189ClickRateTracker.LEFT_BUTTON, true);

            int first20 = 0;
            for (int tick = 0; tick < 20; tick++) {
                if (runtime.shouldAutoClick()) {
                    first20++;
                }
            }
            assertEquals(5, first20);
            int second20 = 0;
            for (int tick = 0; tick < 20; tick++) {
                if (runtime.shouldAutoClick()) {
                    second20++;
                }
            }
            assertEquals(10, second20);

            runtime.inputState().pointerButton(
                    Minecraft189ClickRateTracker.LEFT_BUTTON, false);
            assertFalse(runtime.shouldAutoClick());
            runtime.inputState().pointerButton(
                    Minecraft189ClickRateTracker.LEFT_BUTTON, true);
            for (int tick = 0; tick < 7; tick++) {
                assertFalse(runtime.shouldAutoClick());
            }
            assertTrue(runtime.shouldAutoClick());
            module.rampUpTicksSetting().set(10);
            assertEquals("10", settings.snapshotEncoded().get(
                    Minecraft189AutoClickerModule.RAMP_UP_TICKS_SETTING_ID));

            controller.disable(Minecraft189AutoClickerModule.ID);
            assertFalse(runtime.shouldAutoClick());
        } finally {
            runtime.close();
        }
        assertNull(settings.find(
                Minecraft189AutoClickerModule.RAMP_UP_SETTING_ID));
        assertNull(settings.find(
                Minecraft189AutoClickerModule.RAMP_UP_TICKS_SETTING_ID));
        assertNull(modules.find(Minecraft189AutoClickerModule.ID));
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
