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
import static org.junit.jupiter.api.Assertions.assertThrows;
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
                            Minecraft189JitterModule.YAW_ENABLED_SETTING_ID));
            assertNotNull(
                    settings.find(
                            Minecraft189JitterModule.PITCH_ENABLED_SETTING_ID));
            assertNotNull(
                    settings.find(
                            Minecraft189JitterModule.INTERVAL_SETTING_ID));
            assertNotNull(
                    settings.find(
                            Minecraft189JitterModule.REQUIRE_HOLD_SETTING_ID));
            assertNotNull(settings.find(
                    Minecraft189JitterModule.VARIABLE_STRENGTH_SETTING_ID));
            assertNotNull(settings.find(
                    Minecraft189JitterModule.STRENGTH_VARIATION_SETTING_ID));
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189JitterModule.VARIABLE_STRENGTH_SETTING_ID));
            assertEquals("35", settings.snapshotEncoded().get(
                    Minecraft189JitterModule.STRENGTH_VARIATION_SETTING_ID));
            assertTrue(
                    module.requireHoldSetting()
                            .get()
                            .booleanValue());
            assertTrue(
                    module.yawEnabledSetting()
                            .get()
                            .booleanValue());
            assertTrue(
                    module.pitchEnabledSetting()
                            .get()
                            .booleanValue());
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

            module.intervalTicksSetting()
                    .set(
                            1);
            module.requireHoldSetting()
                    .set(
                            Boolean.FALSE);
            player.yaw = 50.0F;
            player.pitch = 20.0F;
            module.onDisable();
            module.onEnable();
            state.update(
                    player.yaw,
                    player.pitch);
            assertTrue(
                    module.apply(
                            player,
                            state.snapshot(),
                            false));
            assertEquals(
                    51.25F,
                    player.yaw,
                    0.000001F);
            assertEquals(
                    22.0F,
                    player.pitch,
                    0.000001F);

            module.requireHoldSetting()
                    .set(
                            Boolean.TRUE);
            state.update(
                    player.yaw,
                    player.pitch);
            assertFalse(
                    module.apply(
                            player,
                            state.snapshot(),
                            false));
            assertEquals(
                    51.25F,
                    player.yaw,
                    0.000001F);

            module.yawEnabledSetting()
                    .set(
                            Boolean.FALSE);
            module.pitchEnabledSetting()
                    .set(
                            Boolean.TRUE);
            player.yaw = 70.0F;
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
                    70.0F,
                    player.yaw,
                    0.000001F);
            assertEquals(
                    12.0F,
                    player.pitch,
                    0.000001F);

            module.yawEnabledSetting()
                    .set(
                            Boolean.TRUE);
            module.pitchEnabledSetting()
                    .set(
                            Boolean.FALSE);
            player.yaw = 80.0F;
            player.pitch = 15.0F;
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
                    81.25F,
                    player.yaw,
                    0.000001F);
            assertEquals(
                    15.0F,
                    player.pitch,
                    0.000001F);

            module.yawEnabledSetting()
                    .set(
                            Boolean.FALSE);
            state.update(
                    player.yaw,
                    player.pitch);
            assertFalse(
                    module.apply(
                            player,
                            state.snapshot(),
                            true));
            assertEquals(
                    81.25F,
                    player.yaw,
                    0.000001F);
            assertEquals(
                    15.0F,
                    player.pitch,
                    0.000001F);

            module.yawEnabledSetting()
                    .set(
                            Boolean.TRUE);
            state.update(
                    player.yaw,
                    player.pitch);
            assertTrue(
                    module.apply(
                            player,
                            state.snapshot(),
                            true));
            assertEquals(
                    82.5F,
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
                        Minecraft189JitterModule.YAW_ENABLED_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189JitterModule.PITCH_ENABLED_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189JitterModule.INTERVAL_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189JitterModule.REQUIRE_HOLD_SETTING_ID));
        assertNull(settings.find(
                Minecraft189JitterModule.VARIABLE_STRENGTH_SETTING_ID));
        assertNull(settings.find(
                Minecraft189JitterModule.STRENGTH_VARIATION_SETTING_ID));
    }

    @Test
    void variableStrengthPairsExactYawAndClippedPitchWithDeterministicSamples() {
        final double[] samples = {0.5D, 0.25D, 0.75D, 0.0D};
        final int[] read = {0};
        final Minecraft189JitterModule module = new Minecraft189JitterModule(
                () -> samples[read[0]++ % samples.length]);
        final Minecraft189PlayerRotationState state =
                new Minecraft189PlayerRotationState();
        final TestPlayer player = new TestPlayer(10.0F, 89.0F);
        module.yawDegreesSetting().set(2.0D);
        module.pitchDegreesSetting().set(4.0D);
        module.strengthVariationPercentSetting().set(50);
        assertFalse(module.variableStrengthSetting().get().booleanValue());
        assertEquals(Minecraft189JitterModule.DEFAULT_STRENGTH_VARIATION_PERCENT,
                new Minecraft189JitterModule().strengthVariationPercentSetting()
                        .get().intValue());

        module.onEnable();
        module.variableStrengthSetting().set(Boolean.TRUE);
        state.update(player.yaw, player.pitch);
        assertTrue(module.apply(player, state.snapshot(), true));
        assertEquals(11.5F, player.yaw, 0.000001F);
        assertEquals(90.0F, player.pitch, 0.000001F);
        assertEquals(2, read[0]); // One independent draw per enabled axis.
        state.update(player.yaw, player.pitch);
        assertTrue(module.apply(player, state.snapshot(), true));
        assertEquals(10.0F, player.yaw, 0.000001F);
        assertEquals(89.0F, player.pitch, 0.000001F);
        assertEquals(2, read[0]); // Return stroke consumes no RNG.

        state.update(player.yaw, player.pitch);
        assertTrue(module.apply(player, state.snapshot(), true));
        assertEquals(11.25F, player.yaw, 0.000001F);
        assertEquals(90.0F, player.pitch, 0.000001F);
        assertEquals(4, read[0]);
        state.update(player.yaw, player.pitch);
        assertTrue(module.apply(player, state.snapshot(), true));
        assertEquals(10.0F, player.yaw, 0.000001F);
        assertEquals(89.0F, player.pitch, 0.000001F);

        // Zero variation equals the configured amplitude without RNG calls.
        module.strengthVariationPercentSetting().set(0);
        state.update(player.yaw, player.pitch);
        assertTrue(module.apply(player, state.snapshot(), true));
        assertEquals(12.0F, player.yaw, 0.000001F);
        assertEquals(90.0F, player.pitch, 0.000001F);
        assertEquals(4, read[0]);
        state.update(player.yaw, player.pitch);
        assertTrue(module.apply(player, state.snapshot(), true));
        assertEquals(10.0F, player.yaw, 0.000001F);
        assertEquals(89.0F, player.pitch, 0.000001F);

        // Toggling variable strength resets cadence, preserving classic
        // full-strength alternation exactly when turned OFF.
        module.variableStrengthSetting().set(Boolean.FALSE);
        player.pitch = 10.0F;
        state.update(player.yaw, player.pitch);
        assertTrue(module.apply(player, state.snapshot(), true));
        assertEquals(12.0F, player.yaw, 0.000001F);
        assertEquals(14.0F, player.pitch, 0.000001F);
        state.update(player.yaw, player.pitch);
        assertTrue(module.apply(player, state.snapshot(), true));
        assertEquals(10.0F, player.yaw, 0.000001F);
        assertEquals(10.0F, player.pitch, 0.000001F);
        assertEquals(4, read[0]);
        assertThrows(IllegalArgumentException.class,
                () -> module.strengthVariationPercentSetting().set(-1));
        assertThrows(IllegalArgumentException.class,
                () -> module.strengthVariationPercentSetting().set(101));
        module.onDisable();
        state.update(player.yaw, player.pitch);
        assertFalse(module.apply(player, state.snapshot(), true));
    }

    @Test
    void variableJitterResetsOnInputLossAndRespectsCadenceAndAxes() {
        final int[] draws = {0};
        final Minecraft189JitterModule module = new Minecraft189JitterModule(
                () -> { draws[0]++; return 0.5D; });
        final Minecraft189PlayerRotationState state =
                new Minecraft189PlayerRotationState();
        final TestPlayer player = new TestPlayer(20.0F, -89.0F);
        module.variableStrengthSetting().set(Boolean.TRUE);
        module.strengthVariationPercentSetting().set(100);
        module.yawDegreesSetting().set(2.0D);
        module.pitchDegreesSetting().set(2.0D);
        module.intervalTicksSetting().set(3);
        module.onEnable();
        state.update(player.yaw, player.pitch);
        assertTrue(module.apply(player, state.snapshot(), true));
        assertEquals(21.0F, player.yaw, 0.000001F);
        assertEquals(-88.0F, player.pitch, 0.000001F);
        assertEquals(2, draws[0]);
        state.update(player.yaw, player.pitch);
        assertFalse(module.apply(player, state.snapshot(), true));
        assertFalse(module.apply(player, state.snapshot(), true));
        assertTrue(module.apply(player, state.snapshot(), true));
        assertEquals(20.0F, player.yaw, 0.000001F);
        assertEquals(-89.0F, player.pitch, 0.000001F);
        assertEquals(2, draws[0]);

        // Losing the held input discards the pending pair/cadence.
        state.update(player.yaw, player.pitch);
        assertFalse(module.apply(player, state.snapshot(), false));
        module.pitchEnabledSetting().set(Boolean.FALSE);
        state.update(player.yaw, player.pitch);
        assertTrue(module.apply(player, state.snapshot(), true));
        assertEquals(21.0F, player.yaw, 0.000001F);
        assertEquals(-89.0F, player.pitch, 0.000001F);
        state.update(player.yaw, player.pitch);
        assertFalse(module.apply(player, state.snapshot(), true));
        assertFalse(module.apply(player, state.snapshot(), true));
        assertTrue(module.apply(player, state.snapshot(), true));
        assertEquals(20.0F, player.yaw, 0.000001F);
        assertEquals(-89.0F, player.pitch, 0.000001F);

        module.yawEnabledSetting().set(Boolean.FALSE);
        state.update(player.yaw, player.pitch);
        assertFalse(module.apply(player, state.snapshot(), true));
        module.onDisable();
        module.onEnable();
        module.pitchEnabledSetting().set(Boolean.TRUE);
        state.update(player.yaw, player.pitch);
        assertTrue(module.apply(player, state.snapshot(), true));
        assertEquals(20.0F, player.yaw, 0.000001F);
        assertEquals(-88.0F, player.pitch, 0.000001F);
        state.clear();
        assertFalse(module.apply(player, state.snapshot(), true));
        module.onDisable();
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
