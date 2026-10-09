package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.event.EventBus;
import dev.trexzo.custommc.core.module.ModuleCategoryRegistry;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
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
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189JitterModule.RANDOM_INTERVAL_SETTING_ID));
            assertEquals("2", settings.snapshotEncoded().get(
                    Minecraft189JitterModule.INTERVAL_VARIATION_SETTING_ID));
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
        assertNull(settings.find(
                Minecraft189JitterModule.RANDOM_INTERVAL_SETTING_ID));
        assertNull(settings.find(
                Minecraft189JitterModule.INTERVAL_VARIATION_SETTING_ID));
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

    @Test
    void randomIntervalVariesPairedStrokeSpacingWithoutChangingLegacyDefault() {
        // Discrete draws: -2, 0, +2, with 3 tick base -> 1, 3, 5 ticks.
        final double[] samples = {0.0D, 0.5D, 0.99D, 0.0D};
        final int[] draws = {0};
        final Minecraft189JitterModule module = new Minecraft189JitterModule(
                () -> samples[draws[0]++ % samples.length]);
        final Minecraft189PlayerRotationState state =
                new Minecraft189PlayerRotationState();
        final TestPlayer player = new TestPlayer(10.0F, 10.0F);
        module.intervalTicksSetting().set(3);
        module.yawDegreesSetting().set(2.0D);
        module.pitchEnabledSetting().set(Boolean.FALSE);
        module.onEnable();
        assertFalse(module.randomIntervalSetting().get().booleanValue());
        assertEquals(2, module.intervalVariationTicksSetting().get().intValue());
        state.update(player.yaw, player.pitch);
        assertTrue(module.apply(player, state.snapshot(), true)); // Fixed 3 tick.
        assertEquals(0, draws[0]); // Default has no RNG calls.
        assertEquals(12.0F, player.yaw, 0.000001F);
        assertFalse(module.apply(player, state.snapshot(), true));
        assertFalse(module.apply(player, state.snapshot(), true));
        state.update(player.yaw, player.pitch);
        assertTrue(module.apply(player, state.snapshot(), true));
        assertEquals(10.0F, player.yaw, 0.000001F);

        module.randomIntervalSetting().set(Boolean.TRUE);
        // Live edit cancels old countdown / pair and starts a fresh stroke.
        state.update(player.yaw, player.pitch);
        assertTrue(module.apply(player, state.snapshot(), true)); // -2 => 1 tick
        assertEquals(12.0F, player.yaw, 0.000001F);
        assertEquals(1, draws[0]);
        state.update(player.yaw, player.pitch);
        assertTrue(module.apply(player, state.snapshot(), true)); // 0 => 3 ticks
        assertEquals(10.0F, player.yaw, 0.000001F);
        assertEquals(2, draws[0]);
        assertFalse(module.apply(player, state.snapshot(), true));
        assertFalse(module.apply(player, state.snapshot(), true));
        state.update(player.yaw, player.pitch);
        assertTrue(module.apply(player, state.snapshot(), true)); // +2 => 5 ticks
        assertEquals(12.0F, player.yaw, 0.000001F);
        assertEquals(3, draws[0]);
        for (int i = 0; i < 4; i++) {
            assertFalse(module.apply(player, state.snapshot(), true));
        }
        state.update(player.yaw, player.pitch);
        assertTrue(module.apply(player, state.snapshot(), true));
        assertEquals(10.0F, player.yaw, 0.000001F);
        assertEquals(4, draws[0]);

        // Live bounded setting edits reset cadence, no carry-over draws.
        module.intervalVariationTicksSetting().set(0);
        state.update(player.yaw, player.pitch);
        assertTrue(module.apply(player, state.snapshot(), true));
        assertEquals(12.0F, player.yaw, 0.000001F);
        assertEquals(4, draws[0]);
        assertFalse(module.apply(player, state.snapshot(), true));
        assertFalse(module.apply(player, state.snapshot(), true));
        state.update(player.yaw, player.pitch);
        assertTrue(module.apply(player, state.snapshot(), true));
        assertEquals(10.0F, player.yaw, 0.000001F);
        assertEquals(4, draws[0]);

        // Hold release/disable reset cadence; bad RNG never schedules
        // outside bounded configured interval and does not throw.
        module.intervalVariationTicksSetting().set(2);
        module.randomIntervalSetting().set(Boolean.FALSE);
        assertFalse(module.apply(player, state.snapshot(), false));
        state.update(player.yaw, player.pitch);
        assertTrue(module.apply(player, state.snapshot(), true));
        assertEquals(12.0F, player.yaw, 0.000001F);
        assertEquals(4, draws[0]);
        module.onDisable();
        assertFalse(module.apply(player, state.snapshot(), true));
        assertThrows(IllegalArgumentException.class,
                () -> module.intervalVariationTicksSetting().set(-1));
        assertThrows(IllegalArgumentException.class,
                () -> module.intervalVariationTicksSetting().set(6));
    }

    @Test
    void randomIntervalClampsToOneAndTenTicksAndHandlesBadSample() {
        final Minecraft189PlayerRotationState rotation =
                new Minecraft189PlayerRotationState();
        final TestPlayer player = new TestPlayer(0.0F, 0.0F);
        final Minecraft189JitterModule lower = new Minecraft189JitterModule(
                () -> 0.0D);
        lower.intervalTicksSetting().set(1);
        lower.intervalVariationTicksSetting().set(5);
        lower.randomIntervalSetting().set(Boolean.TRUE);
        lower.onEnable();
        rotation.update(player.yaw, player.pitch);
        assertTrue(lower.apply(player, rotation.snapshot(), true));
        rotation.update(player.yaw, player.pitch);
        assertTrue(lower.apply(player, rotation.snapshot(), true)); // Clamp to 1
        lower.onDisable();

        final Minecraft189JitterModule upper = new Minecraft189JitterModule(
                () -> 0.99D);
        upper.intervalTicksSetting().set(10);
        upper.intervalVariationTicksSetting().set(5);
        upper.randomIntervalSetting().set(Boolean.TRUE);
        upper.onEnable();
        rotation.update(player.yaw, player.pitch);
        assertTrue(upper.apply(player, rotation.snapshot(), true));
        for (int i = 0; i < 9; i++) {
            assertFalse(upper.apply(player, rotation.snapshot(), true));
        }
        rotation.update(player.yaw, player.pitch);
        assertTrue(upper.apply(player, rotation.snapshot(), true)); // Clamp to 10
        upper.onDisable();

        final Minecraft189JitterModule invalid = new Minecraft189JitterModule(
                () -> Double.NaN);
        invalid.intervalTicksSetting().set(3);
        invalid.intervalVariationTicksSetting().set(2);
        invalid.randomIntervalSetting().set(Boolean.TRUE);
        invalid.onEnable();
        rotation.update(player.yaw, player.pitch);
        assertTrue(invalid.apply(player, rotation.snapshot(), true));
        assertFalse(invalid.apply(player, rotation.snapshot(), true));
        assertFalse(invalid.apply(player, rotation.snapshot(), true));
        rotation.update(player.yaw, player.pitch);
        assertTrue(invalid.apply(player, rotation.snapshot(), true)); // Neutral 3
        invalid.onDisable();
    }

    @Test
    void movementGuardsRejectUnknownAirAndSneakAndResetJitterCadence() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final SettingPresentationRegistry descriptors =
                new SettingPresentationRegistry();
        final Minecraft189JitterFeature feature = Minecraft189JitterFeature.install(
                modules, controller, new ModulePresentationRegistry(),
                new ModuleSettingRegistry(modules, settings), settings, descriptors);
        try {
            final Minecraft189JitterModule jitter = feature.module();
            final Minecraft189PlayerRotationState rotation =
                    new Minecraft189PlayerRotationState();
            final Minecraft189PlayerMovementState movement =
                    new Minecraft189PlayerMovementState();
            final TestPlayer player = new TestPlayer(20.0F, 10.0F);
            assertFalse(jitter.groundOnlySetting().get().booleanValue());
            assertFalse(jitter.pauseWhileSneakingSetting().get().booleanValue());
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189JitterModule.GROUND_ONLY_SETTING_ID));
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189JitterModule.PAUSE_SNEAKING_SETTING_ID));
            jitter.pitchEnabledSetting().set(Boolean.FALSE);
            jitter.yawDegreesSetting().set(2.0D);
            jitter.intervalTicksSetting().set(3);
            controller.enable(Minecraft189JitterModule.ID);

            rotation.update(player.yaw, player.pitch);
            assertTrue(jitter.apply(player, rotation.snapshot(), true));
            assertEquals(22.0F, player.yaw, 0.00001F);
            // A configured condition is now fail closed without movement.
            jitter.groundOnlySetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189JitterModule.GROUND_ONLY_SETTING_ID));
            rotation.update(player.yaw, player.pitch);
            assertFalse(jitter.apply(player, rotation.snapshot(), true));
            assertEquals(22.0F, player.yaw, 0.00001F);
            movement.update(false, false, false);
            assertFalse(jitter.apply(player, rotation.snapshot(), true,
                    movement.snapshot()));
            movement.update(true, false, false);
            rotation.update(player.yaw, player.pitch);
            assertTrue(jitter.apply(player, rotation.snapshot(), true,
                    movement.snapshot()));
            assertEquals(24.0F, player.yaw, 0.00001F);
            // Cadence after this outward stroke must not replay through air.
            movement.update(false, false, false);
            assertFalse(jitter.apply(player, rotation.snapshot(), true,
                    movement.snapshot()));
            movement.update(true, false, false);
            rotation.update(player.yaw, player.pitch);
            assertTrue(jitter.apply(player, rotation.snapshot(), true,
                    movement.snapshot()));
            assertEquals(26.0F, player.yaw, 0.00001F);

            jitter.pauseWhileSneakingSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189JitterModule.PAUSE_SNEAKING_SETTING_ID));
            movement.update(true, true, false);
            assertFalse(jitter.apply(player, rotation.snapshot(), true,
                    movement.snapshot()));
            movement.update(true, false, false);
            rotation.update(player.yaw, player.pitch);
            assertTrue(jitter.apply(player, rotation.snapshot(), true,
                    movement.snapshot()));
            assertEquals(28.0F, player.yaw, 0.00001F);
            jitter.groundOnlySetting().set(Boolean.FALSE);
            movement.update(false, false, false);
            rotation.update(player.yaw, player.pitch);
            assertFalse(jitter.apply(player, rotation.snapshot(), true,
                    movement.snapshot())); // 3-tick cadence still applies.
            assertFalse(jitter.apply(player, rotation.snapshot(), true,
                    movement.snapshot()));
            assertTrue(jitter.apply(player, rotation.snapshot(), true,
                    movement.snapshot()));
            assertEquals(26.0F, player.yaw, 0.00001F); // Paired inward stroke.
            movement.clear();
            assertFalse(jitter.apply(player, rotation.snapshot(), true,
                    movement.snapshot()));
            jitter.pauseWhileSneakingSetting().set(Boolean.FALSE);
            rotation.update(player.yaw, player.pitch);
            assertTrue(jitter.apply(player, rotation.snapshot(), true));
            assertEquals(28.0F, player.yaw, 0.00001F);
            controller.disable(Minecraft189JitterModule.ID);
            assertFalse(jitter.apply(player, rotation.snapshot(), true));
        } finally {
            feature.close();
        }
        assertNull(settings.find(Minecraft189JitterModule.GROUND_ONLY_SETTING_ID));
        assertNull(settings.find(Minecraft189JitterModule.PAUSE_SNEAKING_SETTING_ID));
        assertNull(modules.find(Minecraft189JitterModule.ID));
    }

    @Test
    void pauseOnPhysicalRightButtonRejectsCachedStrokeAndRequiresMappedInput() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final Minecraft189JitterFeature feature = Minecraft189JitterFeature.install(
                modules, controller, new ModulePresentationRegistry(),
                new ModuleSettingRegistry(modules, settings), settings,
                new SettingPresentationRegistry());
        try {
            final Minecraft189JitterModule jitter = feature.module();
            final Minecraft189PlayerRotationState rotation =
                    new Minecraft189PlayerRotationState();
            final TestPlayer player = new TestPlayer(20.0F, 10.0F);
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189JitterModule.PAUSE_RIGHT_CLICKING_SETTING_ID));
            jitter.pitchEnabledSetting().set(Boolean.FALSE);
            jitter.yawDegreesSetting().set(2.0D);
            jitter.intervalTicksSetting().set(3);
            controller.enable(Minecraft189JitterModule.ID);

            rotation.update(player.yaw, player.pitch);
            assertTrue(jitter.apply(player, rotation.snapshot(), true));
            assertEquals(22.0F, player.yaw, 0.000001F);
            jitter.pauseWhileRightClickingSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189JitterModule.PAUSE_RIGHT_CLICKING_SETTING_ID));
            rotation.update(player.yaw, player.pitch);
            assertFalse(jitter.apply(player, rotation.snapshot(), true,
                    null, Boolean.TRUE)); // Paused, cadence cleared.
            assertFalse(jitter.apply(player, rotation.snapshot(), true,
                    null, Boolean.TRUE));
            assertFalse(jitter.apply(player, rotation.snapshot(), true));
            assertFalse(jitter.apply(player, rotation.snapshot(), true, null));
            assertEquals(22.0F, player.yaw, 0.000001F);
            assertTrue(jitter.apply(player, rotation.snapshot(), true,
                    null, Boolean.FALSE));
            assertEquals(24.0F, player.yaw, 0.000001F); // Fresh outward stroke.
            rotation.update(player.yaw, player.pitch);
            assertFalse(jitter.apply(player, rotation.snapshot(), true,
                    null, Boolean.FALSE)); // Interval tick.
            assertFalse(jitter.apply(player, rotation.snapshot(), true,
                    null, Boolean.TRUE)); // Pause clears pending 2nd stroke.
            assertTrue(jitter.apply(player, rotation.snapshot(), true,
                    null, Boolean.FALSE));
            assertEquals(26.0F, player.yaw, 0.000001F);

            // Live OFF returns to original legacy behavior and does not
            // reintroduce a buffered or synthetic physical button state.
            jitter.pauseWhileRightClickingSetting().set(Boolean.FALSE);
            rotation.update(player.yaw, player.pitch);
            assertFalse(jitter.apply(player, rotation.snapshot(), true));
            assertFalse(jitter.apply(player, rotation.snapshot(), true));
            assertTrue(jitter.apply(player, rotation.snapshot(), true));
            assertEquals(24.0F, player.yaw, 0.000001F);
            controller.disable(Minecraft189JitterModule.ID);
        } finally {
            feature.close();
        }
        assertNull(settings.find(
                Minecraft189JitterModule.PAUSE_RIGHT_CLICKING_SETTING_ID));
        assertNull(modules.find(Minecraft189JitterModule.ID));
    }

    @Test
    void hostRoutesRealRightButtonToJitterAndRestoresCadenceOnRelease() {
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
            final Minecraft189JitterModule jitter = runtime.featureCatalog().jitter();
            final TestPlayer player = new TestPlayer(0.0F, 0.0F);
            jitter.pitchEnabledSetting().set(Boolean.FALSE);
            jitter.yawDegreesSetting().set(2.0D);
            jitter.pauseWhileRightClickingSetting().set(Boolean.TRUE);
            controller.enable(Minecraft189JitterModule.ID);
            runtime.inputState().pointerButton(
                    Minecraft189ClickRateTracker.LEFT_BUTTON, true);
            runtime.inputState().pointerButton(
                    Minecraft189ClickRateTracker.RIGHT_BUTTON, true);
            runtime.playerRotation(player);
            runtime.playerRotation(player);
            assertEquals(0.0F, player.yaw, 0.000001F);
            runtime.inputState().pointerButton(
                    Minecraft189ClickRateTracker.RIGHT_BUTTON, false);
            runtime.playerRotation(player);
            assertEquals(2.0F, player.yaw, 0.000001F);
            runtime.inputState().pointerButton(
                    Minecraft189ClickRateTracker.RIGHT_BUTTON, true);
            runtime.playerRotation(player);
            assertEquals(2.0F, player.yaw, 0.000001F);
            runtime.inputState().pointerButton(
                    Minecraft189ClickRateTracker.RIGHT_BUTTON, false);
            runtime.playerRotation(player);
            assertEquals(4.0F, player.yaw, 0.000001F);
            controller.disable(Minecraft189JitterModule.ID);
            runtime.playerRotation(player);
            assertEquals(4.0F, player.yaw, 0.000001F);
        } finally {
            runtime.close();
        }
        assertNull(settings.find(
                Minecraft189JitterModule.PAUSE_RIGHT_CLICKING_SETTING_ID));
        assertNull(modules.find(Minecraft189JitterModule.ID));
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
        @Override public void drawText(final UiFontHandle font, final float x,
                final float y, final String text, final int argb) { }
        @Override public void endUi() { }
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
