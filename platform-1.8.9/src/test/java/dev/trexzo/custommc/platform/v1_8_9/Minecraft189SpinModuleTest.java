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

final class Minecraft189SpinModuleTest {
    @Test
    void spinRotatesYawAndWrapsWithoutTouchingPitch() {
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

        final Minecraft189SpinFeature feature =
                Minecraft189SpinFeature.install(
                        modules,
                        controller,
                        presentations,
                        moduleSettings,
                        settings,
                        settingPresentations);
        try {
            final Minecraft189SpinModule module =
                    feature.module();
            assertNotNull(
                    settings.find(
                            Minecraft189SpinModule.YAW_SPEED_SETTING_ID));
            assertNotNull(
                    settings.find(
                            Minecraft189SpinModule.REVERSE_SETTING_ID));
            assertNotNull(
                    settings.find(
                            Minecraft189SpinModule.REQUIRE_HOLD_SETTING_ID));
            assertFalse(
                    module.requireHoldSetting()
                            .get()
                            .booleanValue());
            assertFalse(
                    module.reverseSetting()
                            .get()
                            .booleanValue());
            assertEquals(
                    Minecraft189SpinModule.DEFAULT_YAW_SPEED,
                    module.yawSpeedSetting()
                            .get()
                            .doubleValue(),
                    0.000001D);

            final Minecraft189PlayerRotationState state =
                    new Minecraft189PlayerRotationState();
            final TestPlayer player =
                    new TestPlayer(
                            170.0F,
                            25.0F);

            state.update(
                    player.yaw,
                    player.pitch);
            assertFalse(
                    module.apply(
                            player,
                            state.snapshot(),
                            false));
            assertEquals(
                    170.0F,
                    player.yaw,
                    0.000001F);

            controller.enable(
                    Minecraft189SpinModule.ID);
            assertTrue(
                    module.active());

            state.update(
                    player.yaw,
                    player.pitch);
            assertTrue(
                    module.apply(
                            player,
                            state.snapshot(),
                            false));
            assertEquals(
                    -170.0F,
                    player.yaw,
                    0.000001F);
            assertEquals(
                    25.0F,
                    player.pitch,
                    0.000001F);

            module.yawSpeedSetting()
                    .set(
                            45.0D);
            player.yaw = -170.0F;
            state.update(
                    player.yaw,
                    player.pitch);
            assertTrue(
                    module.apply(
                            player,
                            state.snapshot(),
                            false));
            assertEquals(
                    -125.0F,
                    player.yaw,
                    0.000001F);
            assertEquals(
                    25.0F,
                    player.pitch,
                    0.000001F);

            module.reverseSetting()
                    .set(
                            Boolean.TRUE);
            player.yaw = -170.0F;
            state.update(
                    player.yaw,
                    player.pitch);
            assertTrue(
                    module.apply(
                            player,
                            state.snapshot(),
                            false));
            assertEquals(
                    145.0F,
                    player.yaw,
                    0.000001F);
            assertEquals(
                    25.0F,
                    player.pitch,
                    0.000001F);

            module.requireHoldSetting()
                    .set(
                            Boolean.TRUE);
            player.yaw = 10.0F;
            state.update(
                    player.yaw,
                    player.pitch);
            assertFalse(
                    module.apply(
                            player,
                            state.snapshot(),
                            false));
            assertEquals(
                    10.0F,
                    player.yaw,
                    0.000001F);
            state.update(
                    player.yaw,
                    player.pitch);
            assertTrue(
                    module.apply(
                            player,
                            state.snapshot(),
                            true));
            assertEquals(
                    -35.0F,
                    player.yaw,
                    0.000001F);

            controller.disable(
                    Minecraft189SpinModule.ID);
            assertFalse(
                    module.active());
            state.update(
                    player.yaw,
                    player.pitch);
            assertFalse(
                    module.apply(
                            player,
                            state.snapshot(),
                            false));
            assertEquals(
                    -35.0F,
                    player.yaw,
                    0.000001F);
        } finally {
            feature.close();
        }

        assertNull(
                modules.find(
                        Minecraft189SpinModule.ID));
        assertNull(
                settings.find(
                        Minecraft189SpinModule.YAW_SPEED_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189SpinModule.REVERSE_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189SpinModule.REQUIRE_HOLD_SETTING_ID));
    }

    private static final class TestPlayer
            implements Minecraft189PlayerRotationControl {
        private float yaw;
        private float pitch;

        private TestPlayer(
                final float yaw,
                final float pitch) {
            this.yaw = yaw;
            this.pitch = pitch;
        }

        @Override
        public float customMcRotationYaw() {
            return yaw;
        }

        @Override
        public float customMcRotationPitch() {
            return pitch;
        }

        @Override
        public void customMcSetRotationYaw(
                final float yaw) {
            this.yaw = yaw;
        }

        @Override
        public void customMcSetRotationPitch(
                final float pitch) {
            this.pitch = pitch;
        }
    }
}
