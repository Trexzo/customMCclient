package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
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
