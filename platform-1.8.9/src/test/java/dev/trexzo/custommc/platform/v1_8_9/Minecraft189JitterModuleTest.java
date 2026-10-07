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

final class Minecraft189JitterModuleTest {
    @Test
    void jitterAlternatesWhileHeldAndResetsOnRelease() {
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

        final Minecraft189JitterFeature feature =
                Minecraft189JitterFeature.install(
                        modules,
                        controller,
                        presentations,
                        moduleSettings,
                        settings,
                        settingPresentations);
        try {
            final Minecraft189JitterModule module =
                    feature.module();
            assertNotNull(
                    settings.find(
                            Minecraft189JitterModule.YAW_SETTING_ID));
            assertNotNull(
                    settings.find(
                            Minecraft189JitterModule.PITCH_SETTING_ID));
            assertNotNull(
                    settings.find(
                            Minecraft189JitterModule.INTERVAL_SETTING_ID));
            assertEquals(
                    Minecraft189JitterModule.DEFAULT_INTERVAL_TICKS,
                    module.intervalTicksSetting()
                            .get()
                            .intValue());

            controller.enable(
                    Minecraft189JitterModule.ID);
            assertTrue(
                    module.active());

            final Minecraft189PlayerRotationState state =
                    new Minecraft189PlayerRotationState();
            final TestPlayer player =
                    new TestPlayer(
                            10.0F,
                            5.0F);

            state.update(
                    player.yaw,
                    player.pitch);
            assertTrue(
                    module.apply(
                            player,
                            state.snapshot(),
                            true));
            assertEquals(
                    10.5F,
                    player.yaw,
                    0.000001F);
            assertEquals(
                    5.5F,
                    player.pitch,
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
                    10.0F,
                    player.yaw,
                    0.000001F);
            assertEquals(
                    5.0F,
                    player.pitch,
                    0.000001F);

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
            assertEquals(
                    5.0F,
                    player.pitch,
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
                    10.5F,
                    player.yaw,
                    0.000001F);
            assertEquals(
                    5.5F,
                    player.pitch,
                    0.000001F);

            module.yawDegreesSetting()
                    .set(
                            1.25D);
            module.pitchDegreesSetting()
                    .set(
                            2.00D);
            player.yaw = 30.0F;
            player.pitch = 89.5F;
            module.onDisable();
            module.onEnable();
            state.update(
                    player.yaw,
                    player.pitch);
            assertTrue(
                    module.apply(
                            player,
                            state.snapshot(),
                            true));
            assertEquals(
                    31.25F,
                    player.yaw,
                    0.000001F);
            assertEquals(
                    90.0F,
                    player.pitch,
                    0.000001F);

            module.intervalTicksSetting()
                    .set(
                            3);
            player.yaw = 40.0F;
            player.pitch = 10.0F;
            module.onDisable();
            module.onEnable();

            state.update(
                    player.yaw,
                    player.pitch);
            assertTrue(
                    module.apply(
                            player,
                            state.snapshot(),
                            true));
            assertEquals(
                    41.25F,
                    player.yaw,
                    0.000001F);
            assertEquals(
                    12.0F,
                    player.pitch,
                    0.000001F);

            state.update(
                    player.yaw,
                    player.pitch);
            assertFalse(
                    module.apply(
                            player,
                            state.snapshot(),
                            true));
            state.update(
                    player.yaw,
                    player.pitch);
            assertFalse(
                    module.apply(
                            player,
                            state.snapshot(),
                            true));

            state.update(
                    player.yaw,
                    player.pitch);
            assertTrue(
                    module.apply(
                            player,
                            state.snapshot(),
                            true));
            assertEquals(
                    40.0F,
                    player.yaw,
                    0.000001F);
            assertEquals(
                    10.0F,
                    player.pitch,
                    0.000001F);

            state.update(
                    player.yaw,
                    player.pitch);
            assertFalse(
                    module.apply(
                            player,
                            state.snapshot(),
                            false));
            state.update(
                    player.yaw,
                    player.pitch);
            assertTrue(
                    module.apply(
                            player,
                            state.snapshot(),
                            true));
            assertEquals(
                    41.25F,
                    player.yaw,
                    0.000001F);

            controller.disable(
                    Minecraft189JitterModule.ID);
            assertFalse(
                    module.active());
        } finally {
            feature.close();
        }

        assertNull(
                modules.find(
                        Minecraft189JitterModule.ID));
        assertNull(
                settings.find(
                        Minecraft189JitterModule.YAW_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189JitterModule.PITCH_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189JitterModule.INTERVAL_SETTING_ID));
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
