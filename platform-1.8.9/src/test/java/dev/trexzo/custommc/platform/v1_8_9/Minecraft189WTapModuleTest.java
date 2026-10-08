package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.event.EventBus;
import dev.trexzo.custommc.core.module.ModuleCategoryRegistry;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.render.RenderPipeline;
import dev.trexzo.custommc.core.service.ServiceRegistry;
import dev.trexzo.custommc.core.ui.UiFontHandle;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.platform.PlatformContext;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class Minecraft189WTapModuleTest {
    @Test
    void freshLeftPressResetsSprintOnceAndRearmsOnRelease() {
        final ModuleRegistry modules =
                new ModuleRegistry();
        final ModuleController controller =
                new ModuleController(
                        modules);
        final ModulePresentationRegistry presentations =
                new ModulePresentationRegistry();
        final SettingRegistry settings =
                new SettingRegistry();
        final SettingPresentationRegistry settingPresentations =
                new SettingPresentationRegistry();
        final ModuleSettingRegistry moduleSettings =
                new ModuleSettingRegistry(
                        modules,
                        settings);

        final Minecraft189WTapFeature feature =
                Minecraft189WTapFeature.install(
                        modules,
                        controller,
                        presentations,
                        moduleSettings,
                        settings,
                        settingPresentations);
        try {
            final Minecraft189WTapModule module =
                    feature.module();
            assertNotNull(
                    settings.find(
                            Minecraft189WTapModule.REQUIRE_GROUND_SETTING_ID));
            assertNotNull(
                    settings.find(
                            Minecraft189WTapModule.COOLDOWN_TICKS_SETTING_ID));
            assertNotNull(
                    settings.find(
                            Minecraft189WTapModule.RESET_TICKS_SETTING_ID));
            assertNotNull(
                    settings.find(
                            Minecraft189WTapModule.REQUIRE_FORWARD_SETTING_ID));
            assertNotNull(settings.find(
                    Minecraft189WTapModule.PAUSE_WHILE_SNEAKING_SETTING_ID));
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189WTapModule.PAUSE_WHILE_SNEAKING_SETTING_ID));
            assertEquals(
                    Minecraft189WTapModule.DEFAULT_COOLDOWN_TICKS,
                    module.cooldownTicksSetting()
                            .get()
                            .intValue());
            assertEquals(
                    Minecraft189WTapModule.DEFAULT_RESET_TICKS,
                    module.resetTicksSetting()
                            .get()
                            .intValue());
            assertFalse(
                    module.requireForwardSetting()
                            .get()
                            .booleanValue());
            assertFalse(
                    module.requireGroundSetting()
                            .get()
                            .booleanValue());

            final Minecraft189PlayerMovementState state =
                    new Minecraft189PlayerMovementState();
            final TestPlayer player =
                    new TestPlayer();

            state.update(
                    true,
                    false,
                    true);
            assertFalse(
                    module.apply(
                            player,
                            state.snapshot(),
                            true,
                            false));
            assertTrue(
                    player.sprinting);

            controller.enable(
                    Minecraft189WTapModule.ID);
            assertTrue(
                    module.active());

            state.update(
                    true,
                    false,
                    true);
            assertTrue(
                    module.apply(
                            player,
                            state.snapshot(),
                            true,
                            false));
            assertFalse(
                    player.sprinting);

            player.sprinting = true;
            state.update(
                    true,
                    false,
                    true);
            assertFalse(
                    module.apply(
                            player,
                            state.snapshot(),
                            true,
                            false));
            assertTrue(
                    player.sprinting);

            assertFalse(
                    module.apply(
                            player,
                            state.snapshot(),
                            false,
                            false));
            state.update(
                    true,
                    false,
                    true);
            assertTrue(
                    module.apply(
                            player,
                            state.snapshot(),
                            true,
                            false));
            assertFalse(
                    player.sprinting);

            player.sprinting = true;
            assertFalse(
                    module.apply(
                            player,
                            state.snapshot(),
                            false,
                            false));
            module.requireGroundSetting()
                    .set(
                            Boolean.TRUE);
            state.update(
                    false,
                    false,
                    true);
            assertFalse(
                    module.apply(
                            player,
                            state.snapshot(),
                            true,
                            false));
            assertTrue(
                    player.sprinting);

            state.update(
                    true,
                    false,
                    true);
            assertFalse(
                    module.apply(
                            player,
                            state.snapshot(),
                            true,
                            false));
            assertTrue(
                    player.sprinting);

            assertFalse(
                    module.apply(
                            player,
                            state.snapshot(),
                            false,
                            false));
            state.update(
                    true,
                    false,
                    true);
            assertTrue(
                    module.apply(
                            player,
                            state.snapshot(),
                            true,
                            false));
            assertFalse(
                    player.sprinting);

            module.requireGroundSetting()
                    .set(
                            Boolean.FALSE);
            module.cooldownTicksSetting()
                    .set(
                            2);
            controller.disable(
                    Minecraft189WTapModule.ID);
            controller.enable(
                    Minecraft189WTapModule.ID);
            player.sprinting = true;
            state.update(
                    true,
                    false,
                    true);
            assertTrue(
                    module.apply(
                            player,
                            state.snapshot(),
                            true,
                            false));
            assertFalse(
                    player.sprinting);

            player.sprinting = true;
            assertFalse(
                    module.apply(
                            player,
                            state.snapshot(),
                            false,
                            false));
            state.update(
                    true,
                    false,
                    true);
            assertFalse(
                    module.apply(
                            player,
                            state.snapshot(),
                            true,
                            false));
            assertTrue(
                    player.sprinting);

            assertFalse(
                    module.apply(
                            player,
                            state.snapshot(),
                            false,
                            false));
            state.update(
                    true,
                    false,
                    true);
            assertTrue(
                    module.apply(
                            player,
                            state.snapshot(),
                            true,
                            false));
            assertFalse(
                    player.sprinting);

            module.cooldownTicksSetting()
                    .set(
                            0);
            module.resetTicksSetting()
                    .set(
                            3);
            controller.disable(
                    Minecraft189WTapModule.ID);
            controller.enable(
                    Minecraft189WTapModule.ID);
            player.sprinting = true;
            state.update(
                    true,
                    false,
                    true);
            assertTrue(
                    module.apply(
                            player,
                            state.snapshot(),
                            true,
                            false));
            assertFalse(
                    player.sprinting);

            player.sprinting = true;
            state.update(
                    true,
                    false,
                    true);
            assertTrue(
                    module.apply(
                            player,
                            state.snapshot(),
                            true,
                            false));
            assertFalse(
                    player.sprinting);

            player.sprinting = true;
            state.update(
                    true,
                    false,
                    true);
            assertTrue(
                    module.apply(
                            player,
                            state.snapshot(),
                            true,
                            false));
            assertFalse(
                    player.sprinting);

            player.sprinting = true;
            state.update(
                    true,
                    false,
                    true);
            assertFalse(
                    module.apply(
                            player,
                            state.snapshot(),
                            true,
                            false));
            assertTrue(
                    player.sprinting);

            module.resetTicksSetting()
                    .set(
                            1);
            module.requireForwardSetting()
                    .set(
                            Boolean.TRUE);
            controller.disable(
                    Minecraft189WTapModule.ID);
            controller.enable(
                    Minecraft189WTapModule.ID);
            player.sprinting = true;
            state.update(
                    true,
                    false,
                    true);
            assertFalse(
                    module.apply(
                            player,
                            state.snapshot(),
                            true,
                            false));
            assertTrue(
                    player.sprinting);

            assertFalse(
                    module.apply(
                            player,
                            state.snapshot(),
                            false,
                            false));
            state.update(
                    true,
                    false,
                    true);
            assertTrue(
                    module.apply(
                            player,
                            state.snapshot(),
                            true,
                            true));
            assertFalse(
                    player.sprinting);

            controller.disable(
                    Minecraft189WTapModule.ID);
            assertFalse(
                    module.active());
        } finally {
            feature.close();
        }

        assertNull(
                modules.find(
                        Minecraft189WTapModule.ID));
        assertNull(
                settings.find(
                        Minecraft189WTapModule.REQUIRE_GROUND_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189WTapModule.COOLDOWN_TICKS_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189WTapModule.RESET_TICKS_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189WTapModule.REQUIRE_FORWARD_SETTING_ID));
        assertNull(settings.find(
                Minecraft189WTapModule.PAUSE_WHILE_SNEAKING_SETTING_ID));
        assertNull(settings.find(
                Minecraft189WTapModule.REQUIRE_NEARBY_PLAYER_SETTING_ID));
        assertNull(settings.find(
                Minecraft189WTapModule.MAX_PLAYER_DISTANCE_SETTING_ID));
    }

    @Test
    void sneakPauseCancelsActiveResetAndRequiresFreshAttackPressToResume() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final Minecraft189WTapFeature feature = Minecraft189WTapFeature.install(
                modules, controller, new ModulePresentationRegistry(),
                new ModuleSettingRegistry(modules, settings),
                settings, new SettingPresentationRegistry());
        try {
            final Minecraft189WTapModule wTap = feature.module();
            assertFalse(wTap.pauseWhileSneakingSetting().get().booleanValue());
            final Minecraft189PlayerMovementState movement =
                    new Minecraft189PlayerMovementState();
            final TestPlayer player = new TestPlayer();
            controller.enable(Minecraft189WTapModule.ID);
            wTap.resetTicksSetting().set(3);
            wTap.cooldownTicksSetting().set(4);

            // Default-OFF parity: the old reset works even when sneaking.
            movement.update(true, true, true);
            assertTrue(wTap.apply(player, movement.snapshot(), true, false));
            assertEquals(1, player.setCalls);
            assertFalse(player.sprinting);
            controller.disable(Minecraft189WTapModule.ID);
            controller.enable(Minecraft189WTapModule.ID);
            wTap.pauseWhileSneakingSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189WTapModule.PAUSE_WHILE_SNEAKING_SETTING_ID));

            player.sprinting = true;
            movement.update(true, true, true);
            assertFalse(wTap.apply(player, movement.snapshot(), true, false));
            assertEquals(1, player.setCalls);
            // Holding the button while leaving sneak cannot queue a W-Tap.
            movement.update(true, false, true);
            assertFalse(wTap.apply(player, movement.snapshot(), true, false));
            assertEquals(1, player.setCalls);
            assertFalse(wTap.apply(player, movement.snapshot(), false, false));
            assertTrue(wTap.apply(player, movement.snapshot(), true, false));
            assertEquals(2, player.setCalls);

            // Sneaking halfway through a multi-tick reset cancels it.
            player.sprinting = true;
            movement.update(true, true, true);
            assertFalse(wTap.apply(player, movement.snapshot(), true, false));
            assertEquals(2, player.setCalls);
            movement.update(true, false, true);
            assertFalse(wTap.apply(player, movement.snapshot(), true, false));
            assertEquals(2, player.setCalls);
            assertFalse(wTap.apply(player, movement.snapshot(), false, false));
            // The pause clears both the pending reset and cooldown window.
            assertTrue(wTap.apply(player, movement.snapshot(), true, false));
            assertEquals(3, player.setCalls);

            // Missing mapping state cancels the reset and cannot replay it.
            player.sprinting = true;
            movement.clear();
            assertFalse(wTap.apply(player, movement.snapshot(), true, false));
            assertEquals(3, player.setCalls);
            movement.update(true, false, true);
            assertTrue(wTap.apply(player, movement.snapshot(), true, false));
            assertEquals(4, player.setCalls);

            controller.disable(Minecraft189WTapModule.ID);
            player.sprinting = true;
            movement.update(true, true, true);
            assertFalse(wTap.apply(player, movement.snapshot(), false, false));
            assertEquals(4, player.setCalls);
        } finally {
            feature.close();
        }
        assertNull(settings.find(Minecraft189WTapModule.PAUSE_WHILE_SNEAKING_SETTING_ID));
        assertNull(modules.find(Minecraft189WTapModule.ID));
    }

    @Test
    void proximityGateCancelsPendingResetAndRequiresFreshAttackPress() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final Minecraft189WTapFeature feature = Minecraft189WTapFeature.install(
                modules, controller, new ModulePresentationRegistry(),
                new ModuleSettingRegistry(modules, settings),
                settings, new SettingPresentationRegistry());
        try {
            final Minecraft189WTapModule tap = feature.module();
            final Minecraft189PlayerMovementState movement =
                    new Minecraft189PlayerMovementState();
            final Minecraft189NearestPlayerTargetState nearest =
                    new Minecraft189NearestPlayerTargetState();
            final TestPlayer player = new TestPlayer();
            assertFalse(tap.requireNearbyPlayerSetting().get().booleanValue());
            assertEquals(Double.valueOf(4.0D), tap.maxPlayerDistanceSetting().get());
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189WTapModule.REQUIRE_NEARBY_PLAYER_SETTING_ID));
            assertEquals("4.0", settings.snapshotEncoded().get(
                    Minecraft189WTapModule.MAX_PLAYER_DISTANCE_SETTING_ID));
            controller.enable(Minecraft189WTapModule.ID);
            tap.resetTicksSetting().set(3);
            tap.cooldownTicksSetting().set(4);
            movement.update(true, false, true);

            // Default OFF: legacy four-argument caller never requires a target.
            assertTrue(tap.apply(player, movement.snapshot(), true, false));
            assertEquals(1, player.setCalls);
            controller.disable(Minecraft189WTapModule.ID);
            controller.enable(Minecraft189WTapModule.ID);

            tap.requireNearbyPlayerSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189WTapModule.REQUIRE_NEARBY_PLAYER_SETTING_ID));
            player.sprinting = true;
            assertFalse(tap.apply(player, movement.snapshot(), true,
                    false, nearest.snapshot()));
            assertEquals(1, player.setCalls);
            targetAt(nearest, 4.0D);
            // Finding a player while already holding attack cannot queue WTap.
            assertFalse(tap.apply(player, movement.snapshot(), true,
                    false, nearest.snapshot()));
            assertFalse(tap.apply(player, movement.snapshot(), false,
                    false, nearest.snapshot()));
            assertTrue(tap.apply(player, movement.snapshot(), true,
                    false, nearest.snapshot())); // 4.0m boundary is inclusive
            assertEquals(2, player.setCalls);
            assertFalse(player.sprinting);

            player.sprinting = true;
            // Existing multi-tick reset is active before target authority is lost.
            assertTrue(tap.apply(player, movement.snapshot(), true,
                    false, nearest.snapshot()));
            assertEquals(3, player.setCalls);
            nearest.clear();
            assertFalse(tap.apply(player, movement.snapshot(), true,
                    false, nearest.snapshot()));
            assertEquals(3, player.setCalls); // Pending reset cancelled.
            targetAt(nearest, 4.0D);
            assertFalse(tap.apply(player, movement.snapshot(), true,
                    false, nearest.snapshot()));
            assertFalse(tap.apply(player, movement.snapshot(), false,
                    false, nearest.snapshot()));
            player.sprinting = true;
            tap.maxPlayerDistanceSetting().set(3.5D);
            assertEquals("3.5", settings.snapshotEncoded().get(
                    Minecraft189WTapModule.MAX_PLAYER_DISTANCE_SETTING_ID));
            assertFalse(tap.apply(player, movement.snapshot(), true,
                    false, nearest.snapshot())); // Outside the changed range.
            tap.maxPlayerDistanceSetting().set(4.0D);
            assertFalse(tap.apply(player, movement.snapshot(), true,
                    false, nearest.snapshot())); // Held; fresh edge required.
            assertFalse(tap.apply(player, movement.snapshot(), false,
                    false, nearest.snapshot()));
            assertTrue(tap.apply(player, movement.snapshot(), true,
                    false, nearest.snapshot()));
            assertEquals(4, player.setCalls);

            // Missing state, including old overload, must fail closed when gated.
            controller.disable(Minecraft189WTapModule.ID);
            controller.enable(Minecraft189WTapModule.ID);
            player.sprinting = true;
            assertFalse(tap.apply(player, movement.snapshot(), true, false));
            assertEquals(4, player.setCalls);

            assertThrows(IllegalArgumentException.class,
                    () -> tap.maxPlayerDistanceSetting().set(0.49D));
            assertThrows(IllegalArgumentException.class,
                    () -> tap.maxPlayerDistanceSetting().set(16.1D));
            assertThrows(IllegalArgumentException.class,
                    () -> tap.maxPlayerDistanceSetting().set(Double.NaN));
            assertThrows(IllegalArgumentException.class,
                    () -> tap.maxPlayerDistanceSetting().set(Double.POSITIVE_INFINITY));
            controller.disable(Minecraft189WTapModule.ID);
        } finally {
            feature.close();
        }
        assertNull(modules.find(Minecraft189WTapModule.ID));
        assertNull(settings.find(Minecraft189WTapModule.REQUIRE_NEARBY_PLAYER_SETTING_ID));
        assertNull(settings.find(Minecraft189WTapModule.MAX_PLAYER_DISTANCE_SETTING_ID));
    }

    @Test
    void mappedHostWTapUsesNearestPlayerSnapshotWithoutNewHooks() {
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
            final Minecraft189WTapModule tap = runtime.featureCatalog().wTap();
            controller.enable(Minecraft189WTapModule.ID);
            tap.requireNearbyPlayerSetting().set(Boolean.TRUE);
            final TestPlayer player = new TestPlayer();
            runtime.playerMovementState().update(true, false, true);
            runtime.inputState().pointerButton(
                    Minecraft189ClickRateTracker.LEFT_BUTTON, true);
            runtime.playerSprintControl(player);
            assertEquals(0, player.setCalls);
            targetAt(runtime.nearestPlayerTargetState(), 2.5D);
            runtime.playerSprintControl(player);
            assertEquals(0, player.setCalls); // Already-held click not replayed.
            runtime.inputState().pointerButton(
                    Minecraft189ClickRateTracker.LEFT_BUTTON, false);
            runtime.playerSprintControl(player);
            runtime.inputState().pointerButton(
                    Minecraft189ClickRateTracker.LEFT_BUTTON, true);
            runtime.playerSprintControl(player);
            assertEquals(1, player.setCalls);
            assertFalse(player.sprinting);
            controller.disable(Minecraft189WTapModule.ID);
        } finally {
            runtime.close();
        }
        assertNull(settings.find(Minecraft189WTapModule.REQUIRE_NEARBY_PLAYER_SETTING_ID));
        assertNull(settings.find(Minecraft189WTapModule.MAX_PLAYER_DISTANCE_SETTING_ID));
    }

    private static void targetAt(
            final Minecraft189NearestPlayerTargetState nearest,
            final double distance) {
        final Minecraft189PlayerPositionState local =
                new Minecraft189PlayerPositionState();
        final Minecraft189WorldEntityPositionState positions =
                new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds =
                new Minecraft189WorldEntityKindState();
        local.update(0.0D, 0.0D, 0.0D);
        positions.update(new double[]{0.0D, 0.0D, 0.0D, 0.0D, 0.0D, distance});
        kinds.update(new int[]{
                Minecraft189WorldEntityKindState.LIVING
                        | Minecraft189WorldEntityKindState.PLAYER
                        | Minecraft189WorldEntityKindState.LOCAL_PLAYER,
                Minecraft189WorldEntityKindState.LIVING
                        | Minecraft189WorldEntityKindState.PLAYER
        });
        nearest.update(local.snapshot(), positions.snapshot(), kinds.snapshot());
    }

    private static final class NoOpHost implements LegacyUiHostCallbacks {
        @Override public int framebufferWidth() { return 1280; }
        @Override public int framebufferHeight() { return 720; }
        @Override public float uiScale() { return 1.0F; }
        @Override public void beginUi(final UiViewport viewport) { }
        @Override public void fillRect(final float x, final float y,
                final float width, final float height, final int argb) { }
        @Override public void fillRoundedRect(final float x, final float y,
                final float width, final float height, final float radius,
                final int argb) { }
        @Override public void strokeRect(final float x, final float y,
                final float width, final float height, final float thickness,
                final int argb) { }
        @Override public void pushClip(final float x, final float y,
                final float width, final float height) { }
        @Override public void popClip() { }
        @Override public void drawText(final UiFontHandle font,
                final float x, final float y, final String text,
                final int argb) { }
        @Override public void endUi() { }
    }

    private static final class TestPlayer
            implements Minecraft189PlayerSprintControl {
        private boolean sprinting = true;
        private int setCalls;

        @Override
        public void customMcSetSprinting(
                final boolean sprinting) {
            setCalls++;
            this.sprinting = sprinting;
        }
    }
}
