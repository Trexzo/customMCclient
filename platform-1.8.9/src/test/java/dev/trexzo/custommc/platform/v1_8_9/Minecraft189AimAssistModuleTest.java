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
            assertTrue(
                    module.requireHoldSetting()
                            .get()
                            .booleanValue());
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
                            Minecraft189AimAssistModule.REQUIRE_HOLD_SETTING_ID)
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
                        Minecraft189AimAssistModule.REQUIRE_HOLD_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189AimAssistModule.MAX_DISTANCE_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189AimAssistModule.MAX_FOV_SETTING_ID));
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
