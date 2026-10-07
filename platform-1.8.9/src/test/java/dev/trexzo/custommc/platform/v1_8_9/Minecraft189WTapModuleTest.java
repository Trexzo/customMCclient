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
                            true));
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
                            true));
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
                            true));
            assertTrue(
                    player.sprinting);

            assertFalse(
                    module.apply(
                            player,
                            state.snapshot(),
                            false));
            state.update(
                    true,
                    false,
                    true);
            assertTrue(
                    module.apply(
                            player,
                            state.snapshot(),
                            true));
            assertFalse(
                    player.sprinting);

            player.sprinting = true;
            assertFalse(
                    module.apply(
                            player,
                            state.snapshot(),
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
                            true));
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
                            true));
            assertTrue(
                    player.sprinting);

            assertFalse(
                    module.apply(
                            player,
                            state.snapshot(),
                            false));
            state.update(
                    true,
                    false,
                    true);
            assertTrue(
                    module.apply(
                            player,
                            state.snapshot(),
                            true));
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
                            true));
            assertFalse(
                    player.sprinting);

            player.sprinting = true;
            assertFalse(
                    module.apply(
                            player,
                            state.snapshot(),
                            false));
            state.update(
                    true,
                    false,
                    true);
            assertFalse(
                    module.apply(
                            player,
                            state.snapshot(),
                            true));
            assertTrue(
                    player.sprinting);

            assertFalse(
                    module.apply(
                            player,
                            state.snapshot(),
                            false));
            state.update(
                    true,
                    false,
                    true);
            assertTrue(
                    module.apply(
                            player,
                            state.snapshot(),
                            true));
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
                            true));
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
                            true));
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
                            true));
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
                            true));
            assertTrue(
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
    }

    private static final class TestPlayer
            implements Minecraft189PlayerSprintControl {
        private boolean sprinting = true;

        @Override
        public void customMcSetSprinting(
                final boolean sprinting) {
            this.sprinting = sprinting;
        }
    }
}
