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
import static org.junit.jupiter.api.Assertions.assertThrows;

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
            assertNotNull(
                    settings.find(
                            Minecraft189SpinModule.INTERVAL_SETTING_ID));
            assertEquals(
                    Minecraft189SpinModule.DEFAULT_INTERVAL_TICKS,
                    module.intervalTicksSetting()
                            .get()
                            .intValue());
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

            module.requireHoldSetting()
                    .set(
                            Boolean.FALSE);
            module.reverseSetting()
                    .set(
                            Boolean.FALSE);
            module.yawSpeedSetting()
                    .set(
                            30.0D);
            module.intervalTicksSetting()
                    .set(
                            3);
            module.onDisable();
            module.onEnable();
            player.yaw = 0.0F;
            state.update(
                    player.yaw,
                    player.pitch);
            assertTrue(
                    module.apply(
                            player,
                            state.snapshot(),
                            false));
            assertEquals(
                    30.0F,
                    player.yaw,
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
                    30.0F,
                    player.yaw,
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
                    30.0F,
                    player.yaw,
                    0.000001F);
            state.update(
                    player.yaw,
                    player.pitch);
            assertTrue(
                    module.apply(
                            player,
                            state.snapshot(),
                            false));
            assertEquals(
                    60.0F,
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
                    60.0F,
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
        assertNull(
                settings.find(
                        Minecraft189SpinModule.INTERVAL_SETTING_ID));
        assertNull(settings.find(
                Minecraft189SpinModule.RANDOM_INTERVAL_SETTING_ID));
        assertNull(settings.find(
                Minecraft189SpinModule.INTERVAL_VARIATION_SETTING_ID));
    }

    @Test
    void randomSpinIntervalsAreDiscreteBoundedAndResetOnLiveSettingsEdits() {
        final double[] samples = {0.0D, 0.5D, 0.99D, 0.0D};
        final int[] draws = {0};
        final Minecraft189SpinModule spin = new Minecraft189SpinModule(
                () -> samples[draws[0]++ % samples.length]);
        final Minecraft189PlayerRotationState rotation =
                new Minecraft189PlayerRotationState();
        final TestPlayer player = new TestPlayer(0.0F, 0.0F);
        spin.intervalTicksSetting().set(3);
        spin.yawSpeedSetting().set(20.0D);
        spin.onEnable();
        assertFalse(spin.randomIntervalSetting().get().booleanValue());
        assertEquals(2, spin.intervalVariationTicksSetting().get().intValue());

        rotation.update(player.yaw, player.pitch);
        assertTrue(spin.apply(player, rotation.snapshot(), false));
        assertEquals(20.0F, player.yaw, 0.000001F);
        assertEquals(0, draws[0]); // Fixed interval consumes no RNG.
        assertFalse(spin.apply(player, rotation.snapshot(), false));
        assertFalse(spin.apply(player, rotation.snapshot(), false));
        rotation.update(player.yaw, player.pitch);
        assertTrue(spin.apply(player, rotation.snapshot(), false));
        assertEquals(40.0F, player.yaw, 0.000001F);

        // Turning variation ON starts a fresh sequence: offsets -2, 0, +2.
        spin.randomIntervalSetting().set(Boolean.TRUE);
        rotation.update(player.yaw, player.pitch);
        assertTrue(spin.apply(player, rotation.snapshot(), false)); // interval 1
        assertEquals(60.0F, player.yaw, 0.000001F);
        assertEquals(1, draws[0]);
        rotation.update(player.yaw, player.pitch);
        assertTrue(spin.apply(player, rotation.snapshot(), false)); // interval 3
        assertEquals(80.0F, player.yaw, 0.000001F);
        assertEquals(2, draws[0]);
        assertFalse(spin.apply(player, rotation.snapshot(), false));
        assertFalse(spin.apply(player, rotation.snapshot(), false));
        rotation.update(player.yaw, player.pitch);
        assertTrue(spin.apply(player, rotation.snapshot(), false)); // interval 5
        assertEquals(100.0F, player.yaw, 0.000001F);
        assertEquals(3, draws[0]);
        for (int i = 0; i < 4; i++) {
            assertFalse(spin.apply(player, rotation.snapshot(), false));
        }
        rotation.update(player.yaw, player.pitch);
        assertTrue(spin.apply(player, rotation.snapshot(), false));
        assertEquals(120.0F, player.yaw, 0.000001F);
        assertEquals(4, draws[0]);

        // Live variation edit drops the old countdown and does not sample
        // when variation is zero; spin resumes fixed interval.
        spin.intervalVariationTicksSetting().set(0);
        rotation.update(player.yaw, player.pitch);
        assertTrue(spin.apply(player, rotation.snapshot(), false));
        assertEquals(140.0F, player.yaw, 0.000001F);
        assertEquals(4, draws[0]);
        assertFalse(spin.apply(player, rotation.snapshot(), false));
        assertFalse(spin.apply(player, rotation.snapshot(), false));
        rotation.update(player.yaw, player.pitch);
        assertTrue(spin.apply(player, rotation.snapshot(), false));
        assertEquals(160.0F, player.yaw, 0.000001F);

        // Live base interval edits reset when random mode is ON, but
        // not when it is OFF (legacy fixed scheduler semantics).
        spin.intervalVariationTicksSetting().set(2);
        spin.intervalTicksSetting().set(10);
        rotation.update(player.yaw, player.pitch);
        assertTrue(spin.apply(player, rotation.snapshot(), false));
        assertEquals(5, draws[0]); // clamp sample -2 => interval 8
        spin.randomIntervalSetting().set(Boolean.FALSE);
        rotation.update(player.yaw, player.pitch);
        assertTrue(spin.apply(player, rotation.snapshot(), false));
        assertEquals(5, draws[0]);
        spin.intervalTicksSetting().set(1);
        assertFalse(spin.apply(player, rotation.snapshot(), false)); // 10 cadence
        spin.requireHoldSetting().set(Boolean.TRUE);
        assertFalse(spin.apply(player, rotation.snapshot(), false));
        rotation.update(player.yaw, player.pitch);
        assertTrue(spin.apply(player, rotation.snapshot(), true));
        assertEquals(5, draws[0]);
        spin.onDisable();
        assertFalse(spin.apply(player, rotation.snapshot(), true));
        assertThrows(IllegalArgumentException.class,
                () -> spin.intervalVariationTicksSetting().set(-1));
        assertThrows(IllegalArgumentException.class,
                () -> spin.intervalVariationTicksSetting().set(6));
    }

    @Test
    void spinVariationClampsOneToTenAndNeutralizesInvalidSamples() {
        final Minecraft189PlayerRotationState rotation =
                new Minecraft189PlayerRotationState();
        final TestPlayer player = new TestPlayer(0.0F, 0.0F);
        final Minecraft189SpinModule low = new Minecraft189SpinModule(() -> 0.0D);
        low.intervalTicksSetting().set(1);
        low.intervalVariationTicksSetting().set(5);
        low.randomIntervalSetting().set(Boolean.TRUE);
        low.onEnable();
        rotation.update(player.yaw, player.pitch);
        assertTrue(low.apply(player, rotation.snapshot(), false));
        rotation.update(player.yaw, player.pitch);
        assertTrue(low.apply(player, rotation.snapshot(), false)); // Clamp to 1

        final Minecraft189SpinModule high = new Minecraft189SpinModule(() -> 0.99D);
        high.intervalTicksSetting().set(10);
        high.intervalVariationTicksSetting().set(5);
        high.randomIntervalSetting().set(Boolean.TRUE);
        high.onEnable();
        rotation.update(player.yaw, player.pitch);
        assertTrue(high.apply(player, rotation.snapshot(), false));
        for (int i = 0; i < 9; i++) {
            assertFalse(high.apply(player, rotation.snapshot(), false));
        }
        rotation.update(player.yaw, player.pitch);
        assertTrue(high.apply(player, rotation.snapshot(), false)); // Clamp to 10

        final Minecraft189SpinModule invalid = new Minecraft189SpinModule(
                () -> Double.NaN);
        invalid.intervalTicksSetting().set(3);
        invalid.intervalVariationTicksSetting().set(2);
        invalid.randomIntervalSetting().set(Boolean.TRUE);
        invalid.onEnable();
        rotation.update(player.yaw, player.pitch);
        assertTrue(invalid.apply(player, rotation.snapshot(), false));
        assertFalse(invalid.apply(player, rotation.snapshot(), false));
        assertFalse(invalid.apply(player, rotation.snapshot(), false));
        rotation.update(player.yaw, player.pitch);
        assertTrue(invalid.apply(player, rotation.snapshot(), false));
    }

    @Test
    void spinVariationSettingsPersistAndUnregisterWithFeature() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final Minecraft189SpinFeature feature = Minecraft189SpinFeature.install(
                modules, controller, new ModulePresentationRegistry(),
                new ModuleSettingRegistry(modules, settings), settings,
                new SettingPresentationRegistry());
        try {
            final Minecraft189SpinModule spin = feature.module();
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189SpinModule.RANDOM_INTERVAL_SETTING_ID));
            assertEquals("2", settings.snapshotEncoded().get(
                    Minecraft189SpinModule.INTERVAL_VARIATION_SETTING_ID));
            spin.randomIntervalSetting().set(Boolean.TRUE);
            spin.intervalVariationTicksSetting().set(5);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189SpinModule.RANDOM_INTERVAL_SETTING_ID));
            assertEquals("5", settings.snapshotEncoded().get(
                    Minecraft189SpinModule.INTERVAL_VARIATION_SETTING_ID));
            controller.enable(Minecraft189SpinModule.ID);
            controller.disable(Minecraft189SpinModule.ID);
        } finally {
            feature.close();
        }
        assertNull(settings.find(Minecraft189SpinModule.RANDOM_INTERVAL_SETTING_ID));
        assertNull(settings.find(Minecraft189SpinModule.INTERVAL_VARIATION_SETTING_ID));
        assertNull(modules.find(Minecraft189SpinModule.ID));
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
