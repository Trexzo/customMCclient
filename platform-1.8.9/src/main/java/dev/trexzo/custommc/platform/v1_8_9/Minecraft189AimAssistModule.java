package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

public final class Minecraft189AimAssistModule
        implements Module {
    public static final String ID =
            "combat.aimAssist";
    public static final String YAW_SPEED_SETTING_ID =
            "combat.aimAssist.yawSpeed";
    public static final String PITCH_SPEED_SETTING_ID =
            "combat.aimAssist.pitchSpeed";
    public static final String REQUIRE_GROUND_SETTING_ID =
            "combat.aimAssist.requireGround";
    public static final String REQUIRE_FORWARD_SETTING_ID =
            "combat.aimAssist.requireForward";
    public static final String REQUIRE_HOLD_SETTING_ID =
            "combat.aimAssist.requireHold";
    public static final String MIN_DISTANCE_SETTING_ID =
            "combat.aimAssist.minDistance";
    public static final String MAX_DISTANCE_SETTING_ID =
            "combat.aimAssist.maxDistance";
    public static final String MAX_FOV_SETTING_ID =
            "combat.aimAssist.maxFov";
    public static final String MAX_PITCH_FOV_SETTING_ID =
            "combat.aimAssist.maxPitchFov";
    public static final String YAW_OFFSET_SETTING_ID =
            "combat.aimAssist.yawOffset";
    public static final String PITCH_OFFSET_SETTING_ID =
            "combat.aimAssist.pitchOffset";
    public static final String DEAD_ZONE_SETTING_ID =
            "combat.aimAssist.deadZone";
    public static final String YAW_ENABLED_SETTING_ID =
            "combat.aimAssist.yawEnabled";
    public static final String PITCH_ENABLED_SETTING_ID =
            "combat.aimAssist.pitchEnabled";
    public static final double DEFAULT_YAW_SPEED =
            180.0D;
    public static final double DEFAULT_PITCH_SPEED =
            180.0D;
    public static final double MINIMUM_SPEED =
            0.1D;
    public static final double MAXIMUM_SPEED =
            180.0D;
    public static final double DEFAULT_MIN_DISTANCE = 0.0D;
    public static final double MINIMUM_MIN_DISTANCE = 0.0D;
    public static final double MAXIMUM_MIN_DISTANCE = 128.0D;
    public static final double DEFAULT_MAX_DISTANCE =
            128.0D;
    public static final double MINIMUM_MAX_DISTANCE =
            0.5D;
    public static final double MAXIMUM_MAX_DISTANCE =
            128.0D;
    public static final double DEFAULT_MAX_FOV =
            180.0D;
    public static final double DEFAULT_MAX_PITCH_FOV =
            180.0D;
    public static final double MINIMUM_MAX_PITCH_FOV =
            1.0D;
    public static final double MAXIMUM_MAX_PITCH_FOV =
            180.0D;
    public static final double DEFAULT_YAW_OFFSET = 0.0D;
    public static final double MINIMUM_YAW_OFFSET = -30.0D;
    public static final double MAXIMUM_YAW_OFFSET = 30.0D;
    public static final double DEFAULT_PITCH_OFFSET = 0.0D;
    public static final double MINIMUM_PITCH_OFFSET = -30.0D;
    public static final double MAXIMUM_PITCH_OFFSET = 30.0D;
    public static final double DEFAULT_DEAD_ZONE =
            0.0D;
    public static final double MINIMUM_DEAD_ZONE =
            0.0D;
    public static final double MAXIMUM_DEAD_ZONE =
            30.0D;
    public static final double MINIMUM_MAX_FOV =
            1.0D;
    public static final double MAXIMUM_MAX_FOV =
            180.0D;

    private final Setting<Double> yawSpeed =
            new Setting<Double>(
                    YAW_SPEED_SETTING_ID,
                    DEFAULT_YAW_SPEED,
                    Minecraft189AimAssistModule::validSpeed,
                    SettingCodecs.DOUBLE);
    private final Setting<Double> pitchSpeed =
            new Setting<Double>(
                    PITCH_SPEED_SETTING_ID,
                    DEFAULT_PITCH_SPEED,
                    Minecraft189AimAssistModule::validSpeed,
                    SettingCodecs.DOUBLE);
    private final Setting<Boolean> requireGround =
            new Setting<Boolean>(
                    REQUIRE_GROUND_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private final Setting<Boolean> requireForward =
            new Setting<Boolean>(
                    REQUIRE_FORWARD_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private final Setting<Boolean> requireHold =
            new Setting<Boolean>(
                    REQUIRE_HOLD_SETTING_ID,
                    Boolean.TRUE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private final Setting<Double> minDistance =
            new Setting<Double>(
                    MIN_DISTANCE_SETTING_ID,
                    DEFAULT_MIN_DISTANCE,
                    Minecraft189AimAssistModule::validMinDistance,
                    SettingCodecs.DOUBLE);
    private final Setting<Double> maxDistance =
            new Setting<Double>(
                    MAX_DISTANCE_SETTING_ID,
                    DEFAULT_MAX_DISTANCE,
                    Minecraft189AimAssistModule::validMaxDistance,
                    SettingCodecs.DOUBLE);
    private final Setting<Double> maxFov =
            new Setting<Double>(
                    MAX_FOV_SETTING_ID,
                    DEFAULT_MAX_FOV,
                    Minecraft189AimAssistModule::validMaxFov,
                    SettingCodecs.DOUBLE);
    private final Setting<Double> maxPitchFov =
            new Setting<Double>(
                    MAX_PITCH_FOV_SETTING_ID,
                    DEFAULT_MAX_PITCH_FOV,
                    Minecraft189AimAssistModule::validMaxPitchFov,
                    SettingCodecs.DOUBLE);
    private final Setting<Double> yawOffset =
            new Setting<Double>(
                    YAW_OFFSET_SETTING_ID,
                    DEFAULT_YAW_OFFSET,
                    Minecraft189AimAssistModule::validYawOffset,
                    SettingCodecs.DOUBLE);
    private final Setting<Double> pitchOffset =
            new Setting<Double>(
                    PITCH_OFFSET_SETTING_ID,
                    DEFAULT_PITCH_OFFSET,
                    Minecraft189AimAssistModule::validPitchOffset,
                    SettingCodecs.DOUBLE);
    private final Setting<Double> deadZone =
            new Setting<Double>(
                    DEAD_ZONE_SETTING_ID,
                    DEFAULT_DEAD_ZONE,
                    Minecraft189AimAssistModule::validDeadZone,
                    SettingCodecs.DOUBLE);
    private final Setting<Boolean> yawEnabled =
            new Setting<Boolean>(
                    YAW_ENABLED_SETTING_ID,
                    Boolean.TRUE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private final Setting<Boolean> pitchEnabled =
            new Setting<Boolean>(
                    PITCH_ENABLED_SETTING_ID,
                    Boolean.TRUE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);

    private boolean enabled;

    @Override
    public String id() {
        return ID;
    }

    public Setting<Double> yawSpeedSetting() {
        return yawSpeed;
    }

    public Setting<Double> pitchSpeedSetting() {
        return pitchSpeed;
    }

    public Setting<Boolean> requireGroundSetting() {
        return requireGround;
    }

    public Setting<Boolean> requireForwardSetting() {
        return requireForward;
    }

    public Setting<Boolean> requireHoldSetting() {
        return requireHold;
    }

    public Setting<Double> minDistanceSetting() {
        return minDistance;
    }

    public Setting<Double> maxDistanceSetting() {
        return maxDistance;
    }

    public Setting<Double> maxFovSetting() {
        return maxFov;
    }

    public Setting<Double> maxPitchFovSetting() {
        return maxPitchFov;
    }

    public Setting<Double> yawOffsetSetting() {
        return yawOffset;
    }

    public Setting<Double> pitchOffsetSetting() {
        return pitchOffset;
    }

    public Setting<Double> deadZoneSetting() {
        return deadZone;
    }

    public Setting<Boolean> yawEnabledSetting() {
        return yawEnabled;
    }

    public Setting<Boolean> pitchEnabledSetting() {
        return pitchEnabled;
    }

    @Override
    public synchronized void onEnable() {
        enabled = true;
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
    }

    synchronized boolean apply(
            final Minecraft189PlayerRotationControl player,
            final Minecraft189PlayerRotationState.Snapshot rotation,
            final Minecraft189TargetRotationState.Snapshot target,
            final boolean leftButtonHeld) {
        return apply(player, rotation, target, leftButtonHeld, false, null);
    }

    synchronized boolean apply(
            final Minecraft189PlayerRotationControl player,
            final Minecraft189PlayerRotationState.Snapshot rotation,
            final Minecraft189TargetRotationState.Snapshot target,
            final boolean leftButtonHeld,
            final boolean forwardHeld) {
        return apply(player, rotation, target, leftButtonHeld, forwardHeld, null);
    }

    synchronized boolean apply(
            final Minecraft189PlayerRotationControl player,
            final Minecraft189PlayerRotationState.Snapshot rotation,
            final Minecraft189TargetRotationState.Snapshot target,
            final boolean leftButtonHeld,
            final boolean forwardHeld,
            final Minecraft189PlayerMovementState.Snapshot movement) {
        if (!enabled
                || (!yawEnabled.get().booleanValue()
                        && !pitchEnabled.get().booleanValue())
                || player == null
                || rotation == null
                || !rotation.available()
                || target == null
                || !target.available()
                || !withinRange(
                        target,
                        minDistance.get().doubleValue(),
                        maxDistance.get().doubleValue())
                || (yawEnabled.get().booleanValue()
                        && !withinFov(
                                rotation,
                                target,
                                maxFov.get().doubleValue()))
                || (pitchEnabled.get().booleanValue()
                        && !withinPitchFov(
                                rotation,
                                target,
                                maxPitchFov.get().doubleValue()))
                || (requireHold.get().booleanValue()
                        && !leftButtonHeld)
                || (requireForward.get().booleanValue()
                        && !forwardHeld)
                || (requireGround.get().booleanValue()
                        && (movement == null
                        || !movement.available()
                        || !movement.onGround()))) {
            return false;
        }

        final float desiredYaw = effectiveYaw(target.yaw());
        final float desiredPitch = effectivePitch(target.pitch());
        final double deadZoneDegrees =
                deadZone.get().doubleValue();
        final boolean adjustYaw =
                yawEnabled.get().booleanValue()
                        && (deadZoneDegrees == 0.0D
                        || Math.abs(wrapYaw(
                                desiredYaw - rotation.yaw()))
                                > deadZoneDegrees);
        final boolean adjustPitch =
                pitchEnabled.get().booleanValue()
                        && (deadZoneDegrees == 0.0D
                        || Math.abs(desiredPitch - rotation.pitch())
                                > deadZoneDegrees);
        if (!adjustYaw && !adjustPitch) {
            return false;
        }

        final float targetYaw =
                stepYaw(
                        rotation.yaw(),
                        desiredYaw,
                        yawSpeed.get().doubleValue());
        final float targetPitch =
                stepLinear(
                        rotation.pitch(),
                        desiredPitch,
                        pitchSpeed.get().doubleValue());

        if (adjustYaw
                && Float.compare(
                        rotation.yaw(),
                        targetYaw) != 0) {
            player.customMcSetRotationYaw(
                    targetYaw);
        }
        if (adjustPitch
                && Float.compare(
                        rotation.pitch(),
                        targetPitch) != 0) {
            player.customMcSetRotationPitch(
                    targetPitch);
        }

        return true;
    }

    synchronized boolean active() {
        return enabled;
    }

    private float effectiveYaw(final float rawYaw) {
        return wrapYaw(rawYaw + yawOffset.get().floatValue());
    }

    private float effectivePitch(final float rawPitch) {
        return Math.max(-90.0F, Math.min(90.0F,
                rawPitch + pitchOffset.get().floatValue()));
    }

    private static float stepYaw(
            final float current,
            final float target,
            final double maximumStep) {
        final float delta =
                wrapYaw(
                        target - current);
        final float limited =
                limitStep(
                        delta,
                        maximumStep);
        return wrapYaw(
                current + limited);
    }

    private static float stepLinear(
            final float current,
            final float target,
            final double maximumStep) {
        return current
                + limitStep(
                        target - current,
                        maximumStep);
    }

    private static float limitStep(
            final float delta,
            final double maximumStep) {
        final float max =
                (float) maximumStep;
        if (delta > max) {
            return max;
        }
        if (delta < -max) {
            return -max;
        }
        return delta;
    }

    private static float wrapYaw(
            final float yaw) {
        float wrapped =
                yaw % 360.0F;
        if (wrapped >= 180.0F) {
            wrapped -= 360.0F;
        }
        if (wrapped < -180.0F) {
            wrapped += 360.0F;
        }
        return wrapped;
    }

    private static boolean withinRange(
            final Minecraft189TargetRotationState.Snapshot target,
            final double minimumDistance,
            final double maximumDistance) {
        final double minimumDistanceSquared = minimumDistance * minimumDistance;
        final double maximumDistanceSquared = maximumDistance * maximumDistance;
        return minimumDistance <= maximumDistance
                && target.distanceSquared() >= minimumDistanceSquared
                && target.distanceSquared() <= maximumDistanceSquared;
    }

    private boolean withinFov(
            final Minecraft189PlayerRotationState.Snapshot rotation,
            final Minecraft189TargetRotationState.Snapshot target,
            final double maximumFov) {
        final float yawDelta =
                wrapYaw(
                        effectiveYaw(target.yaw())
                                - rotation.yaw());
        return Math.abs(
                yawDelta)
                <= maximumFov;
    }

    private boolean withinPitchFov(
            final Minecraft189PlayerRotationState.Snapshot rotation,
            final Minecraft189TargetRotationState.Snapshot target,
            final double maximumPitchFov) {
        return Math.abs(effectivePitch(target.pitch()) - rotation.pitch())
                <= maximumPitchFov;
    }

    private static boolean validYawOffset(final Double value) {
        return value != null
                && !Double.isNaN(value.doubleValue())
                && !Double.isInfinite(value.doubleValue())
                && value.doubleValue() >= MINIMUM_YAW_OFFSET
                && value.doubleValue() <= MAXIMUM_YAW_OFFSET;
    }

    private static boolean validPitchOffset(final Double value) {
        return value != null
                && !Double.isNaN(value.doubleValue())
                && !Double.isInfinite(value.doubleValue())
                && value.doubleValue() >= MINIMUM_PITCH_OFFSET
                && value.doubleValue() <= MAXIMUM_PITCH_OFFSET;
    }

    private static boolean validMaxPitchFov(
            final Double value) {
        return value != null
                && !Double.isNaN(value.doubleValue())
                && !Double.isInfinite(value.doubleValue())
                && value.doubleValue() >= MINIMUM_MAX_PITCH_FOV
                && value.doubleValue() <= MAXIMUM_MAX_PITCH_FOV;
    }

    private static boolean validDeadZone(
            final Double value) {
        return value != null
                && !Double.isNaN(value.doubleValue())
                && !Double.isInfinite(value.doubleValue())
                && value.doubleValue() >= MINIMUM_DEAD_ZONE
                && value.doubleValue() <= MAXIMUM_DEAD_ZONE;
    }

    private static boolean validMaxFov(
            final Double value) {
        return value != null
                && !Double.isNaN(value.doubleValue())
                && !Double.isInfinite(value.doubleValue())
                && value.doubleValue() >= MINIMUM_MAX_FOV
                && value.doubleValue() <= MAXIMUM_MAX_FOV;
    }

    private static boolean validMinDistance(final Double value) {
        return value != null
                && !Double.isNaN(value.doubleValue())
                && !Double.isInfinite(value.doubleValue())
                && value.doubleValue() >= MINIMUM_MIN_DISTANCE
                && value.doubleValue() <= MAXIMUM_MIN_DISTANCE;
    }

    private static boolean validMaxDistance(
            final Double value) {
        return value != null
                && !Double.isNaN(value.doubleValue())
                && !Double.isInfinite(value.doubleValue())
                && value.doubleValue() >= MINIMUM_MAX_DISTANCE
                && value.doubleValue() <= MAXIMUM_MAX_DISTANCE;
    }

    private static boolean validSpeed(
            final Double value) {
        return value != null
                && !Double.isNaN(value.doubleValue())
                && !Double.isInfinite(value.doubleValue())
                && value.doubleValue() >= MINIMUM_SPEED
                && value.doubleValue() <= MAXIMUM_SPEED;
    }
}
