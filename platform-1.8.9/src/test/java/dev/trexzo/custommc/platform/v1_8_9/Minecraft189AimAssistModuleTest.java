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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189AimAssistModuleTest {
    @Test
    void aimAssistOwnsHeldTargetRotationAndLifecycle() {
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

        final Minecraft189AimAssistFeature feature =
                Minecraft189AimAssistFeature.install(
                        modules,
                        controller,
                        presentations,
                        moduleSettings,
                        settings,
                        settingPresentations);
        try {
            final Minecraft189AimAssistModule module =
                    feature.module();
            assertEquals(
                    Minecraft189AimAssistModule.DEFAULT_YAW_SPEED,
                    module.yawSpeedSetting()
                            .get()
                            .doubleValue(),
                    0.000001D);
            assertEquals(
                    Minecraft189AimAssistModule.DEFAULT_PITCH_SPEED,
                    module.pitchSpeedSetting()
                            .get()
                            .doubleValue(),
                    0.000001D);
            assertFalse(module.pauseWhileSneakingSetting().get().booleanValue());
            assertFalse(module.requireGroundSetting().get().booleanValue());
            assertFalse(module.requireForwardSetting().get().booleanValue());
            assertTrue(
                    module.requireHoldSetting()
                            .get()
                            .booleanValue());
            assertEquals(
                    Minecraft189AimAssistModule.DEFAULT_MIN_DISTANCE,
                    module.minDistanceSetting().get().doubleValue(),
                    0.000001D);
            assertEquals(
                    Minecraft189AimAssistModule.DEFAULT_MAX_DISTANCE,
                    module.maxDistanceSetting()
                            .get()
                            .doubleValue(),
                    0.000001D);
            assertEquals(
                    Minecraft189AimAssistModule.DEFAULT_MAX_FOV,
                    module.maxFovSetting()
                            .get()
                            .doubleValue(),
                    0.000001D);
            assertEquals(
                    Minecraft189AimAssistModule.DEFAULT_MAX_PITCH_FOV,
                    module.maxPitchFovSetting().get().doubleValue(),
                    0.000001D);
            assertEquals(
                    Minecraft189AimAssistModule.DEFAULT_YAW_OFFSET,
                    module.yawOffsetSetting().get().doubleValue(),
                    0.000001D);
            assertEquals(
                    Minecraft189AimAssistModule.DEFAULT_PITCH_OFFSET,
                    module.pitchOffsetSetting().get().doubleValue(),
                    0.000001D);
            assertEquals(
                    Minecraft189AimAssistModule.DEFAULT_DEAD_ZONE,
                    module.deadZoneSetting().get().doubleValue(),
                    0.000001D);
            assertTrue(
                    module.yawEnabledSetting()
                            .get()
                            .booleanValue());
            assertTrue(
                    module.pitchEnabledSetting()
                            .get()
                            .booleanValue());
            assertTrue(
                    settings.find(
                            Minecraft189AimAssistModule.YAW_SPEED_SETTING_ID)
                            != null);
            assertTrue(
                    settings.find(
                            Minecraft189AimAssistModule.PITCH_SPEED_SETTING_ID)
                            != null);
            assertTrue(
                    settings.find(
                            Minecraft189AimAssistModule.PAUSE_WHILE_SNEAKING_SETTING_ID)
                            != null);
            assertTrue(
                    settings.find(
                            Minecraft189AimAssistModule.REQUIRE_GROUND_SETTING_ID)
                            != null);
            assertTrue(
                    settings.find(
                            Minecraft189AimAssistModule.REQUIRE_FORWARD_SETTING_ID)
                            != null);
            assertTrue(
                    settings.find(
                            Minecraft189AimAssistModule.REQUIRE_HOLD_SETTING_ID)
                            != null);
            assertTrue(
                    settings.find(
                            Minecraft189AimAssistModule.MIN_DISTANCE_SETTING_ID)
                            != null);
            assertTrue(
                    settings.find(
                            Minecraft189AimAssistModule.MAX_DISTANCE_SETTING_ID)
                            != null);
            assertTrue(
                    settings.find(
                            Minecraft189AimAssistModule.MAX_FOV_SETTING_ID)
                            != null);
            assertTrue(
                    settings.find(
                            Minecraft189AimAssistModule.MAX_PITCH_FOV_SETTING_ID)
                            != null);
            assertTrue(
                    settings.find(
                            Minecraft189AimAssistModule.YAW_OFFSET_SETTING_ID)
                            != null);
            assertTrue(
                    settings.find(
                            Minecraft189AimAssistModule.PITCH_OFFSET_SETTING_ID)
                            != null);
            assertTrue(
                    settings.find(
                            Minecraft189AimAssistModule.DEAD_ZONE_SETTING_ID)
                            != null);
            assertTrue(
                    settings.find(
                            Minecraft189AimAssistModule.YAW_ENABLED_SETTING_ID)
                            != null);
            assertTrue(
                    settings.find(
                            Minecraft189AimAssistModule.PITCH_ENABLED_SETTING_ID)
                            != null);
            final Minecraft189PlayerPositionState local =
                    new Minecraft189PlayerPositionState();
            final Minecraft189WorldEntityPositionState positions =
                    new Minecraft189WorldEntityPositionState();
            final Minecraft189WorldEntityKindState kinds =
                    new Minecraft189WorldEntityKindState();
            final Minecraft189NearestPlayerTargetState target =
                    new Minecraft189NearestPlayerTargetState();
            final Minecraft189TargetRotationState targetRotation =
                    new Minecraft189TargetRotationState();
            final Minecraft189PlayerRotationState currentRotation =
                    new Minecraft189PlayerRotationState();
            final TestPlayer player =
                    new TestPlayer(
                            30.0F,
                            10.0F);

            local.update(
                    0.0D,
                    0.0D,
                    0.0D);
            positions.update(
                    new double[]{
                            0.0D,
                            0.0D,
                            10.0D
                    });
            kinds.update(
                    new int[]{
                            Minecraft189WorldEntityKindState.LIVING
                                    | Minecraft189WorldEntityKindState.PLAYER
                    });
            target.update(
                    local.snapshot(),
                    positions.snapshot(),
                    kinds.snapshot());
            targetRotation.update(
                    local.snapshot(),
                    target.snapshot());

            currentRotation.update(
                    player.yaw,
                    player.pitch);
            assertFalse(
                    module.apply(
                            player,
                            currentRotation.snapshot(),
                            targetRotation.snapshot(),
                            true));

            controller.enable(
                    Minecraft189AimAssistModule.ID);
            assertTrue(
                    module.active());

            currentRotation.update(
                    player.yaw,
                    player.pitch);
            assertFalse(
                    module.apply(
                            player,
                            currentRotation.snapshot(),
                            targetRotation.snapshot(),
                            false));
            assertEquals(
                    30.0F,
                    player.yaw,
                    0.000001F);
            assertEquals(
                    10.0F,
                    player.pitch,
                    0.000001F);

            module.maxDistanceSetting()
                    .set(
                            5.0D);
            currentRotation.update(
                    player.yaw,
                    player.pitch);
            assertFalse(
                    module.apply(
                            player,
                            currentRotation.snapshot(),
                            targetRotation.snapshot(),
                            true));
            assertEquals(
                    30.0F,
                    player.yaw,
                    0.000001F);

            module.maxDistanceSetting()
                    .set(
                            15.0D);
            module.maxFovSetting()
                    .set(
                            20.0D);
            currentRotation.update(
                    player.yaw,
                    player.pitch);
            assertFalse(
                    module.apply(
                            player,
                            currentRotation.snapshot(),
                            targetRotation.snapshot(),
                            true));
            assertEquals(
                    30.0F,
                    player.yaw,
                    0.000001F);

            module.maxFovSetting()
                    .set(
                            45.0D);
            module.maxPitchFovSetting().set(5.0D);
            currentRotation.update(player.yaw, player.pitch);
            assertFalse(module.apply(player, currentRotation.snapshot(),
                    targetRotation.snapshot(), true));
            assertEquals(30.0F, player.yaw, 0.0001F);
            assertEquals(10.0F, player.pitch, 0.0001F);
            module.maxPitchFovSetting().set(15.0D);
            currentRotation.update(
                    player.yaw,
                    player.pitch);
            assertTrue(
                    module.apply(
                            player,
                            currentRotation.snapshot(),
                            targetRotation.snapshot(),
                            true));
            assertEquals(
                    0.0F,
                    player.yaw,
                    0.000001F);
            assertEquals(
                    0.0F,
                    player.pitch,
                    0.000001F);
            assertEquals(
                    1,
                    player.yawWrites);
            assertEquals(
                    1,
                    player.pitchWrites);

            currentRotation.update(
                    player.yaw,
                    player.pitch);
            assertTrue(
                    module.apply(
                            player,
                            currentRotation.snapshot(),
                            targetRotation.snapshot(),
                            true));
            assertEquals(
                    1,
                    player.yawWrites);
            assertEquals(
                    1,
                    player.pitchWrites);

            // Restore the default before testing an independently relocated target.
            module.maxPitchFovSetting().set(
                    Minecraft189AimAssistModule.DEFAULT_MAX_PITCH_FOV);

            positions.update(
                    new double[]{
                            -10.0D,
                            10.0D,
                            0.0D
                    });
            target.update(
                    local.snapshot(),
                    positions.snapshot(),
                    kinds.snapshot());
            targetRotation.update(
                    local.snapshot(),
                    target.snapshot());
            module.maxFovSetting()
                    .set(
                            Minecraft189AimAssistModule.DEFAULT_MAX_FOV);
            module.yawSpeedSetting()
                    .set(
                            20.0D);
            module.pitchSpeedSetting()
                    .set(
                            5.0D);
            player.yaw = 0.0F;
            player.pitch = 0.0F;
            currentRotation.update(
                    player.yaw,
                    player.pitch);
            assertTrue(
                    module.apply(
                            player,
                            currentRotation.snapshot(),
                            targetRotation.snapshot(),
                            true));
            assertEquals(
                    20.0F,
                    player.yaw,
                    0.0001F);
            assertEquals(
                    -5.0F,
                    player.pitch,
                    0.0001F);
            assertEquals(
                    2,
                    player.yawWrites);
            assertEquals(
                    2,
                    player.pitchWrites);

            module.pitchEnabledSetting()
                    .set(
                            Boolean.FALSE);
            player.yaw = 0.0F;
            player.pitch = 0.0F;
            currentRotation.update(
                    player.yaw,
                    player.pitch);
            final int pitchWritesBeforeYawOnly =
                    player.pitchWrites;
            assertTrue(
                    module.apply(
                            player,
                            currentRotation.snapshot(),
                            targetRotation.snapshot(),
                            true));
            assertEquals(
                    20.0F,
                    player.yaw,
                    0.0001F);
            assertEquals(
                    0.0F,
                    player.pitch,
                    0.0001F);
            assertEquals(
                    pitchWritesBeforeYawOnly,
                    player.pitchWrites);

            module.yawEnabledSetting()
                    .set(
                            Boolean.FALSE);
            module.pitchEnabledSetting()
                    .set(
                            Boolean.TRUE);
            player.yaw = 0.0F;
            player.pitch = 0.0F;
            currentRotation.update(
                    player.yaw,
                    player.pitch);
            final int yawWritesBeforePitchOnly =
                    player.yawWrites;
            assertTrue(
                    module.apply(
                            player,
                            currentRotation.snapshot(),
                            targetRotation.snapshot(),
                            true));
            assertEquals(
                    0.0F,
                    player.yaw,
                    0.0001F);
            assertEquals(
                    -5.0F,
                    player.pitch,
                    0.0001F);
            assertEquals(
                    yawWritesBeforePitchOnly,
                    player.yawWrites);

            module.pitchEnabledSetting()
                    .set(
                            Boolean.FALSE);
            currentRotation.update(
                    player.yaw,
                    player.pitch);
            assertFalse(
                    module.apply(
                            player,
                            currentRotation.snapshot(),
                            targetRotation.snapshot(),
                            true));

            module.yawEnabledSetting()
                    .set(
                            Boolean.TRUE);
            module.pitchEnabledSetting()
                    .set(
                            Boolean.TRUE);

            module.deadZoneSetting().set(30.0D);
            player.yaw = 80.0F;
            player.pitch = -40.0F;
            currentRotation.update(player.yaw, player.pitch);
            final int deadZoneYawWrites = player.yawWrites;
            final int deadZonePitchWrites = player.pitchWrites;
            assertFalse(module.apply(player, currentRotation.snapshot(),
                    targetRotation.snapshot(), true));
            assertEquals(80.0F, player.yaw, 0.0001F);
            assertEquals(-40.0F, player.pitch, 0.0001F);
            assertEquals(deadZoneYawWrites, player.yawWrites);
            assertEquals(deadZonePitchWrites, player.pitchWrites);

            module.deadZoneSetting().set(15.0D);
            player.yaw = 80.0F;
            player.pitch = 0.0F;
            currentRotation.update(player.yaw, player.pitch);
            assertTrue(module.apply(player, currentRotation.snapshot(),
                    targetRotation.snapshot(), true));
            assertEquals(80.0F, player.yaw, 0.0001F);
            assertEquals(-5.0F, player.pitch, 0.0001F);
            assertEquals(deadZoneYawWrites, player.yawWrites);
            assertEquals(deadZonePitchWrites + 1, player.pitchWrites);

            module.deadZoneSetting().set(0.0D);
            player.yaw = 80.0F;
            player.pitch = 0.0F;
            currentRotation.update(player.yaw, player.pitch);
            assertTrue(module.apply(player, currentRotation.snapshot(),
                    targetRotation.snapshot(), true));
            assertEquals(90.0F, player.yaw, 0.0001F);
            assertEquals(-5.0F, player.pitch, 0.0001F);

            module.requireHoldSetting()
                    .set(
                            Boolean.FALSE);
            player.yaw = 0.0F;
            player.pitch = 0.0F;
            currentRotation.update(
                    player.yaw,
                    player.pitch);
            assertTrue(
                    module.apply(
                            player,
                            currentRotation.snapshot(),
                            targetRotation.snapshot(),
                            false));
            assertEquals(
                    20.0F,
                    player.yaw,
                    0.0001F);
            assertEquals(
                    -5.0F,
                    player.pitch,
                    0.0001F);

            targetRotation.clear();
            currentRotation.update(
                    player.yaw,
                    player.pitch);
            assertFalse(
                    module.apply(
                            player,
                            currentRotation.snapshot(),
                            targetRotation.snapshot(),
                            true));

            controller.disable(
                    Minecraft189AimAssistModule.ID);
            assertFalse(
                    module.active());
        } finally {
            feature.close();
        }

        assertNull(
                modules.find(
                        Minecraft189AimAssistModule.ID));
        assertNull(
                settings.find(
                        Minecraft189AimAssistModule.YAW_SPEED_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189AimAssistModule.PITCH_SPEED_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189AimAssistModule.PAUSE_WHILE_SNEAKING_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189AimAssistModule.REQUIRE_GROUND_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189AimAssistModule.REQUIRE_FORWARD_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189AimAssistModule.REQUIRE_HOLD_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189AimAssistModule.MIN_DISTANCE_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189AimAssistModule.MAX_DISTANCE_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189AimAssistModule.MAX_FOV_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189AimAssistModule.MAX_PITCH_FOV_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189AimAssistModule.YAW_OFFSET_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189AimAssistModule.PITCH_OFFSET_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189AimAssistModule.DEAD_ZONE_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189AimAssistModule.YAW_ENABLED_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189AimAssistModule.PITCH_ENABLED_SETTING_ID));
    }


    @Test
    void pitchOffsetRespectsSmoothingFovAndDeadZone() {
        final Minecraft189AimAssistModule module = new Minecraft189AimAssistModule();
        final Minecraft189PlayerPositionState local = new Minecraft189PlayerPositionState();
        final Minecraft189WorldEntityPositionState positions =
                new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds = new Minecraft189WorldEntityKindState();
        final Minecraft189NearestPlayerTargetState nearest =
                new Minecraft189NearestPlayerTargetState();
        final Minecraft189TargetRotationState target = new Minecraft189TargetRotationState();
        final Minecraft189PlayerRotationState rotation = new Minecraft189PlayerRotationState();
        final TestPlayer player = new TestPlayer(0.0F, 0.0F);

        local.update(0.0D, 0.0D, 0.0D);
        positions.update(new double[]{0.0D, 0.0D, 10.0D});
        kinds.update(new int[]{Minecraft189WorldEntityKindState.LIVING
                | Minecraft189WorldEntityKindState.PLAYER});
        nearest.update(local.snapshot(), positions.snapshot(), kinds.snapshot());
        target.update(local.snapshot(), nearest.snapshot());

        module.onEnable();
        module.pitchSpeedSetting().set(4.0D);
        module.pitchOffsetSetting().set(8.0D);
        rotation.update(player.yaw, player.pitch);
        assertTrue(module.apply(player, rotation.snapshot(), target.snapshot(), true));
        assertEquals(4.0F, player.pitch, 0.0001F);

        player.pitch = 0.0F;
        module.maxPitchFovSetting().set(5.0D);
        rotation.update(player.yaw, player.pitch);
        assertFalse(module.apply(player, rotation.snapshot(), target.snapshot(), true));
        assertEquals(0.0F, player.pitch, 0.0001F);

        module.maxPitchFovSetting().set(180.0D);
        module.deadZoneSetting().set(10.0D);
        rotation.update(player.yaw, player.pitch);
        assertFalse(module.apply(player, rotation.snapshot(), target.snapshot(), true));
        assertEquals(0.0F, player.pitch, 0.0001F);

        module.deadZoneSetting().set(0.0D);
        module.pitchOffsetSetting().set(-8.0D);
        rotation.update(player.yaw, player.pitch);
        assertTrue(module.apply(player, rotation.snapshot(), target.snapshot(), true));
        assertEquals(-4.0F, player.pitch, 0.0001F);

        module.pitchOffsetSetting().set(0.0D);
        player.pitch = 0.0F;
        rotation.update(player.yaw, player.pitch);
        assertTrue(module.apply(player, rotation.snapshot(), target.snapshot(), true));
        assertEquals(0.0F, player.pitch, 0.0001F);
        module.onDisable();
    }


    @Test
    void yawOffsetRespectsSmoothingFovAndDeadZone() {
        final Minecraft189AimAssistModule module = new Minecraft189AimAssistModule();
        final Minecraft189PlayerPositionState local = new Minecraft189PlayerPositionState();
        final Minecraft189WorldEntityPositionState positions =
                new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds = new Minecraft189WorldEntityKindState();
        final Minecraft189NearestPlayerTargetState nearest =
                new Minecraft189NearestPlayerTargetState();
        final Minecraft189TargetRotationState target = new Minecraft189TargetRotationState();
        final Minecraft189PlayerRotationState rotation = new Minecraft189PlayerRotationState();
        final TestPlayer player = new TestPlayer(0.0F, 0.0F);

        local.update(0.0D, 0.0D, 0.0D);
        positions.update(new double[]{0.0D, 0.0D, 10.0D});
        kinds.update(new int[]{Minecraft189WorldEntityKindState.LIVING
                | Minecraft189WorldEntityKindState.PLAYER});
        nearest.update(local.snapshot(), positions.snapshot(), kinds.snapshot());
        target.update(local.snapshot(), nearest.snapshot());

        module.onEnable();
        module.yawSpeedSetting().set(4.0D);
        module.yawOffsetSetting().set(8.0D);
        rotation.update(player.yaw, player.pitch);
        assertTrue(module.apply(player, rotation.snapshot(), target.snapshot(), true));
        assertEquals(4.0F, player.yaw, 0.0001F);

        player.yaw = 0.0F;
        module.maxFovSetting().set(5.0D);
        rotation.update(player.yaw, player.pitch);
        assertFalse(module.apply(player, rotation.snapshot(), target.snapshot(), true));
        assertEquals(0.0F, player.yaw, 0.0001F);

        module.maxFovSetting().set(180.0D);
        module.deadZoneSetting().set(10.0D);
        rotation.update(player.yaw, player.pitch);
        assertFalse(module.apply(player, rotation.snapshot(), target.snapshot(), true));
        assertEquals(0.0F, player.yaw, 0.0001F);

        module.deadZoneSetting().set(0.0D);
        module.yawOffsetSetting().set(-8.0D);
        rotation.update(player.yaw, player.pitch);
        assertTrue(module.apply(player, rotation.snapshot(), target.snapshot(), true));
        assertEquals(-4.0F, player.yaw, 0.0001F);

        module.yawOffsetSetting().set(0.0D);
        player.yaw = 0.0F;
        rotation.update(player.yaw, player.pitch);
        assertTrue(module.apply(player, rotation.snapshot(), target.snapshot(), true));
        assertEquals(0.0F, player.yaw, 0.0001F);

        // Target at +90°, offset to +120°; from -179° the shortest step
        // crosses the -180° boundary and must wrap to +177°.
        positions.update(new double[]{-10.0D, 0.0D, 0.0D});
        nearest.update(local.snapshot(), positions.snapshot(), kinds.snapshot());
        target.update(local.snapshot(), nearest.snapshot());
        module.yawOffsetSetting().set(30.0D);
        player.yaw = -179.0F;
        rotation.update(player.yaw, player.pitch);
        assertTrue(module.apply(player, rotation.snapshot(), target.snapshot(), true));
        assertEquals(177.0F, player.yaw, 0.0001F);
        module.onDisable();
    }


    @Test
    void minimumDistanceBoundsAreInclusiveAndFailClosed() {
        final Minecraft189AimAssistModule module = new Minecraft189AimAssistModule();
        final Minecraft189PlayerPositionState local =
                new Minecraft189PlayerPositionState();
        final Minecraft189WorldEntityPositionState positions =
                new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds =
                new Minecraft189WorldEntityKindState();
        final Minecraft189NearestPlayerTargetState nearest =
                new Minecraft189NearestPlayerTargetState();
        final Minecraft189TargetRotationState target =
                new Minecraft189TargetRotationState();
        final Minecraft189PlayerRotationState rotation =
                new Minecraft189PlayerRotationState();
        final TestPlayer player = new TestPlayer(30.0F, 10.0F);

        local.update(0.0D, 0.0D, 0.0D);
        positions.update(new double[]{0.0D, 0.0D, 10.0D});
        kinds.update(new int[]{
                Minecraft189WorldEntityKindState.LIVING
                        | Minecraft189WorldEntityKindState.PLAYER
        });
        nearest.update(local.snapshot(), positions.snapshot(), kinds.snapshot());
        target.update(local.snapshot(), nearest.snapshot());
        module.onEnable();

        rotation.update(player.yaw, player.pitch);
        assertTrue(module.apply(player, rotation.snapshot(),
                target.snapshot(), true));
        assertEquals(0.0F, player.yaw, 0.0001F);
        assertEquals(0.0F, player.pitch, 0.0001F);

        module.minDistanceSetting().set(10.5D);
        player.yaw = 30.0F;
        player.pitch = 10.0F;
        rotation.update(player.yaw, player.pitch);
        assertFalse(module.apply(player, rotation.snapshot(),
                target.snapshot(), true));
        assertEquals(30.0F, player.yaw, 0.0001F);
        assertEquals(10.0F, player.pitch, 0.0001F);

        module.minDistanceSetting().set(10.0D);
        rotation.update(player.yaw, player.pitch);
        assertTrue(module.apply(player, rotation.snapshot(),
                target.snapshot(), true));

        module.minDistanceSetting().set(11.0D);
        module.maxDistanceSetting().set(10.0D);
        player.yaw = 30.0F;
        player.pitch = 10.0F;
        rotation.update(player.yaw, player.pitch);
        assertFalse(module.apply(player, rotation.snapshot(),
                target.snapshot(), true));

        module.minDistanceSetting().set(0.0D);
        module.maxDistanceSetting().set(9.5D);
        assertFalse(module.apply(player, rotation.snapshot(),
                target.snapshot(), true));

        module.maxDistanceSetting().set(10.0D);
        assertTrue(module.apply(player, rotation.snapshot(),
                target.snapshot(), true));

        module.onDisable();
    }


    @Test
    void requireForwardUsesPhysicalWAndYieldsWithoutRotationWrites() {
        final Minecraft189AimAssistModule module = new Minecraft189AimAssistModule();
        final Minecraft189PlayerPositionState local = new Minecraft189PlayerPositionState();
        final Minecraft189WorldEntityPositionState positions =
                new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds = new Minecraft189WorldEntityKindState();
        final Minecraft189NearestPlayerTargetState nearest =
                new Minecraft189NearestPlayerTargetState();
        final Minecraft189TargetRotationState target = new Minecraft189TargetRotationState();
        final Minecraft189PlayerRotationState rotation = new Minecraft189PlayerRotationState();
        final TestPlayer player = new TestPlayer(30.0F, 10.0F);
        local.update(0.0D, 0.0D, 0.0D);
        positions.update(new double[]{0.0D, 0.0D, 10.0D});
        kinds.update(new int[]{Minecraft189WorldEntityKindState.LIVING
                | Minecraft189WorldEntityKindState.PLAYER});
        nearest.update(local.snapshot(), positions.snapshot(), kinds.snapshot());
        target.update(local.snapshot(), nearest.snapshot());
        module.onEnable();
        rotation.update(player.yaw, player.pitch);

        // Default false retains the original four-argument path.
        assertTrue(module.apply(player, rotation.snapshot(), target.snapshot(), true, false));
        assertEquals(0.0F, player.yaw, 0.0001F);
        player.yaw = 30.0F;
        player.pitch = 10.0F;
        rotation.update(player.yaw, player.pitch);

        module.requireForwardSetting().set(Boolean.TRUE);
        final int yawWrites = player.yawWrites;
        final int pitchWrites = player.pitchWrites;
        assertFalse(module.apply(player, rotation.snapshot(), target.snapshot(), true, false));
        assertFalse(module.apply(player, rotation.snapshot(), target.snapshot(), true));
        assertEquals(yawWrites, player.yawWrites);
        assertEquals(pitchWrites, player.pitchWrites);
        assertTrue(module.apply(player, rotation.snapshot(), target.snapshot(), true, true));
        assertEquals(0.0F, player.yaw, 0.0001F);
        assertEquals(0.0F, player.pitch, 0.0001F);

        module.requireHoldSetting().set(Boolean.TRUE);
        player.yaw = 30.0F;
        player.pitch = 10.0F;
        rotation.update(player.yaw, player.pitch);
        assertFalse(module.apply(player, rotation.snapshot(), target.snapshot(), false, true));
        module.requireHoldSetting().set(Boolean.FALSE);
        assertTrue(module.apply(player, rotation.snapshot(), target.snapshot(), false, true));
        module.onDisable();
        player.yaw = 30.0F;
        rotation.update(player.yaw, player.pitch);
        assertFalse(module.apply(player, rotation.snapshot(), target.snapshot(), true, true));
    }


    @Test
    void requireGroundRejectsAirborneAndUnavailableMovement() {
        final Minecraft189AimAssistModule module = new Minecraft189AimAssistModule();
        final Minecraft189PlayerPositionState local = new Minecraft189PlayerPositionState();
        final Minecraft189WorldEntityPositionState positions =
                new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds =
                new Minecraft189WorldEntityKindState();
        final Minecraft189NearestPlayerTargetState nearest =
                new Minecraft189NearestPlayerTargetState();
        final Minecraft189TargetRotationState target =
                new Minecraft189TargetRotationState();
        final Minecraft189PlayerRotationState rotation =
                new Minecraft189PlayerRotationState();
        final Minecraft189PlayerMovementState movement =
                new Minecraft189PlayerMovementState();
        final TestPlayer player = new TestPlayer(30.0F, 10.0F);
        local.update(0.0D, 0.0D, 0.0D);
        positions.update(new double[]{0.0D, 0.0D, 10.0D});
        kinds.update(new int[]{Minecraft189WorldEntityKindState.LIVING
                | Minecraft189WorldEntityKindState.PLAYER});
        nearest.update(local.snapshot(), positions.snapshot(), kinds.snapshot());
        target.update(local.snapshot(), nearest.snapshot());
        rotation.update(player.yaw, player.pitch);
        module.onEnable();

        // Default disabled: no movement authority required.
        assertTrue(module.apply(player, rotation.snapshot(), target.snapshot(),
                true, false, null));
        player.yaw = 30.0F;
        player.pitch = 10.0F;
        rotation.update(player.yaw, player.pitch);
        module.requireGroundSetting().set(Boolean.TRUE);
        final int yawWrites = player.yawWrites;
        final int pitchWrites = player.pitchWrites;
        assertFalse(module.apply(player, rotation.snapshot(), target.snapshot(),
                true, false, null));
        assertFalse(module.apply(player, rotation.snapshot(), target.snapshot(),
                true, false, movement.snapshot()));
        movement.update(false, false, false);
        assertFalse(module.apply(player, rotation.snapshot(), target.snapshot(),
                true, false, movement.snapshot()));
        assertEquals(yawWrites, player.yawWrites);
        assertEquals(pitchWrites, player.pitchWrites);
        assertEquals(30.0F, player.yaw, 0.0001F);
        assertEquals(10.0F, player.pitch, 0.0001F);

        movement.update(true, false, false);
        assertTrue(module.apply(player, rotation.snapshot(), target.snapshot(),
                true, false, movement.snapshot()));
        assertEquals(0.0F, player.yaw, 0.0001F);
        assertEquals(0.0F, player.pitch, 0.0001F);
        player.yaw = 30.0F;
        player.pitch = 10.0F;
        rotation.update(player.yaw, player.pitch);
        movement.clear();
        assertFalse(module.apply(player, rotation.snapshot(), target.snapshot(),
                true, false, movement.snapshot()));

        module.requireGroundSetting().set(Boolean.FALSE);
        assertTrue(module.apply(player, rotation.snapshot(), target.snapshot(),
                true, false, null));
        module.onDisable();
        assertFalse(module.apply(player, rotation.snapshot(), target.snapshot(),
                true, false, movement.snapshot()));
    }


    @Test
    void axisDisabledDoesNotGateOtherAxisByItsFov() {
        final Minecraft189AimAssistModule module = new Minecraft189AimAssistModule();
        final Minecraft189PlayerPositionState local = new Minecraft189PlayerPositionState();
        final Minecraft189WorldEntityPositionState positions =
                new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds =
                new Minecraft189WorldEntityKindState();
        final Minecraft189NearestPlayerTargetState nearest =
                new Minecraft189NearestPlayerTargetState();
        final Minecraft189TargetRotationState target = new Minecraft189TargetRotationState();
        final Minecraft189PlayerRotationState rotation = new Minecraft189PlayerRotationState();
        final TestPlayer player = new TestPlayer(80.0F, 60.0F);
        local.update(0.0D, 0.0D, 0.0D);
        positions.update(new double[]{0.0D, 0.0D, 10.0D});
        kinds.update(new int[]{Minecraft189WorldEntityKindState.LIVING
                | Minecraft189WorldEntityKindState.PLAYER});
        nearest.update(local.snapshot(), positions.snapshot(), kinds.snapshot());
        target.update(local.snapshot(), nearest.snapshot());
        module.onEnable();

        module.pitchEnabledSetting().set(Boolean.FALSE);
        module.maxFovSetting().set(90.0D);
        module.maxPitchFovSetting().set(5.0D);
        rotation.update(player.yaw, player.pitch);
        assertTrue(module.apply(player, rotation.snapshot(), target.snapshot(), true));
        assertEquals(0.0F, player.yaw, 0.0001F);
        assertEquals(60.0F, player.pitch, 0.0001F);
        assertEquals(0, player.pitchWrites);

        module.yawEnabledSetting().set(Boolean.FALSE);
        module.pitchEnabledSetting().set(Boolean.TRUE);
        module.maxFovSetting().set(5.0D);
        module.maxPitchFovSetting().set(90.0D);
        player.yaw = 80.0F;
        player.pitch = 60.0F;
        rotation.update(player.yaw, player.pitch);
        final int yawWrites = player.yawWrites;
        assertTrue(module.apply(player, rotation.snapshot(), target.snapshot(), true));
        assertEquals(80.0F, player.yaw, 0.0001F);
        assertEquals(0.0F, player.pitch, 0.0001F);
        assertEquals(yawWrites, player.yawWrites);

        // When both axes are enabled, either violated FOV remains a hard veto.
        module.yawEnabledSetting().set(Boolean.TRUE);
        player.yaw = 80.0F;
        player.pitch = 60.0F;
        rotation.update(player.yaw, player.pitch);
        assertFalse(module.apply(player, rotation.snapshot(), target.snapshot(), true));
        assertEquals(80.0F, player.yaw, 0.0001F);
        assertEquals(60.0F, player.pitch, 0.0001F);

        module.yawEnabledSetting().set(Boolean.FALSE);
        module.pitchEnabledSetting().set(Boolean.FALSE);
        module.maxFovSetting().set(180.0D);
        module.maxPitchFovSetting().set(180.0D);
        assertFalse(module.apply(player, rotation.snapshot(), target.snapshot(), true));
        module.onDisable();
    }


    @Test
    void pauseWhileSneakingIsDefaultOffAndFailsClosedOnMissingState() {
        final Minecraft189AimAssistModule module = new Minecraft189AimAssistModule();
        final Minecraft189PlayerPositionState local = new Minecraft189PlayerPositionState();
        final Minecraft189WorldEntityPositionState positions =
                new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds =
                new Minecraft189WorldEntityKindState();
        final Minecraft189NearestPlayerTargetState nearest =
                new Minecraft189NearestPlayerTargetState();
        final Minecraft189TargetRotationState target = new Minecraft189TargetRotationState();
        final Minecraft189PlayerRotationState rotation = new Minecraft189PlayerRotationState();
        final Minecraft189PlayerMovementState movement = new Minecraft189PlayerMovementState();
        final TestPlayer player = new TestPlayer(30.0F, 10.0F);
        local.update(0.0D, 0.0D, 0.0D);
        positions.update(new double[]{0.0D, 0.0D, 10.0D});
        kinds.update(new int[]{Minecraft189WorldEntityKindState.LIVING
                | Minecraft189WorldEntityKindState.PLAYER});
        nearest.update(local.snapshot(), positions.snapshot(), kinds.snapshot());
        target.update(local.snapshot(), nearest.snapshot());
        rotation.update(player.yaw, player.pitch);
        module.onEnable();

        // OFF by default preserves the historical absence-of-movement path.
        assertTrue(module.apply(player, rotation.snapshot(), target.snapshot(),
                true, false, null));
        player.yaw = 30.0F;
        player.pitch = 10.0F;
        rotation.update(player.yaw, player.pitch);

        module.pauseWhileSneakingSetting().set(Boolean.TRUE);
        final int yawWrites = player.yawWrites;
        final int pitchWrites = player.pitchWrites;
        assertFalse(module.apply(player, rotation.snapshot(), target.snapshot(),
                true, false, null));
        assertFalse(module.apply(player, rotation.snapshot(), target.snapshot(),
                true, false, movement.snapshot()));
        movement.update(true, true, false);
        assertFalse(module.apply(player, rotation.snapshot(), target.snapshot(),
                true, false, movement.snapshot()));
        assertEquals(yawWrites, player.yawWrites);
        assertEquals(pitchWrites, player.pitchWrites);
        assertEquals(30.0F, player.yaw, 0.0001F);
        assertEquals(10.0F, player.pitch, 0.0001F);

        movement.update(true, false, false);
        assertTrue(module.apply(player, rotation.snapshot(), target.snapshot(),
                true, false, movement.snapshot()));
        assertEquals(0.0F, player.yaw, 0.0001F);
        assertEquals(0.0F, player.pitch, 0.0001F);

        player.yaw = 30.0F;
        player.pitch = 10.0F;
        rotation.update(player.yaw, player.pitch);
        movement.update(false, true, false);
        assertFalse(module.apply(player, rotation.snapshot(), target.snapshot(),
                true, false, movement.snapshot()));

        module.pauseWhileSneakingSetting().set(Boolean.FALSE);
        assertTrue(module.apply(player, rotation.snapshot(), target.snapshot(),
                true, false, null));
        module.onDisable();
        assertFalse(module.apply(player, rotation.snapshot(), target.snapshot(),
                true, false, movement.snapshot()));
    }

    private static final class TestPlayer
            implements Minecraft189PlayerRotationControl {
        private float yaw;
        private float pitch;
        private int yawWrites;
        private int pitchWrites;

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
            yawWrites++;
        }

        @Override
        public void customMcSetRotationPitch(
                final float pitch) {
            this.pitch = pitch;
            pitchWrites++;
        }
    }
}
