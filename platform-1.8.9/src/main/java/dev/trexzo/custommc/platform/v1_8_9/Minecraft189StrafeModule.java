package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;

import java.util.Objects;

public final class Minecraft189StrafeModule
        implements Module {
    public static final String ID =
            "movement.strafe";
    public static final String SPEED_SETTING_ID =
            "movement.strafe.speed";
    public static final String SMOOTH_ACCELERATION_SETTING_ID =
            "movement.strafe.smoothAcceleration";
    public static final String ACCELERATION_PERCENT_SETTING_ID =
            "movement.strafe.accelerationPercent";
    public static final String GROUND_ONLY_SETTING_ID =
            "movement.strafe.groundOnly";
    public static final String PAUSE_WHILE_SNEAKING_SETTING_ID =
            "movement.strafe.pauseWhileSneaking";
    public static final int DEFAULT_ACCELERATION_PERCENT = 50;
    public static final double DEFAULT_SPEED =
            0.30D;
    public static final double MINIMUM_SPEED =
            0.05D;
    public static final double MAXIMUM_SPEED =
            1.00D;

    private final Minecraft189InputState inputState;
    private final Setting<Double> speed =
            new Setting<Double>(
                    SPEED_SETTING_ID,
                    DEFAULT_SPEED,
                    Minecraft189StrafeModule::validSpeed,
                    SettingCodecs.DOUBLE);
    private final Setting<Boolean> smoothAcceleration =
            new Setting<Boolean>(
                    SMOOTH_ACCELERATION_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private final Setting<Integer> accelerationPercent =
            new Setting<Integer>(
                    ACCELERATION_PERCENT_SETTING_ID,
                    DEFAULT_ACCELERATION_PERCENT,
                    value -> value != null && value >= 10 && value <= 100,
                    SettingCodecs.INTEGER);
    private final Setting<Boolean> groundOnly =
            new Setting<Boolean>(
                    GROUND_ONLY_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private final Setting<Boolean> pauseWhileSneaking =
            new Setting<Boolean>(
                    PAUSE_WHILE_SNEAKING_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private boolean enabled;

    Minecraft189StrafeModule(
            final Minecraft189InputState inputState) {
        this.inputState =
                Objects.requireNonNull(
                        inputState,
                        "inputState");
    }

    @Override
    public String id() {
        return ID;
    }

    public Setting<Double> speedSetting() {
        return speed;
    }

    public Setting<Boolean> smoothAccelerationSetting() {
        return smoothAcceleration;
    }

    public Setting<Integer> accelerationPercentSetting() {
        return accelerationPercent;
    }

    public Setting<Boolean> groundOnlySetting() {
        return groundOnly;
    }

    public Setting<Boolean> pauseWhileSneakingSetting() {
        return pauseWhileSneaking;
    }

    @Override
    public synchronized void onEnable() {
        enabled = true;
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
    }

    synchronized void apply(
            final Minecraft189PlayerMotionControl player,
            final Minecraft189PlayerRotationState.Snapshot rotation,
            final boolean suspended) {
        apply(player, rotation, suspended, null);
    }

    synchronized void apply(
            final Minecraft189PlayerMotionControl player,
            final Minecraft189PlayerRotationState.Snapshot rotation,
            final boolean suspended,
            final Minecraft189PlayerMovementState.Snapshot movement) {
        Objects.requireNonNull(
                rotation,
                "rotation");
        if (!enabled
                || suspended
                || player == null
                || !rotation.available()
                || (groundOnly.get().booleanValue()
                        && (movement == null
                        || !movement.available()
                        || !movement.onGround()))
                || (pauseWhileSneaking.get().booleanValue()
                        && (movement == null
                        || !movement.available()
                        || movement.sneaking()))) {
            // Rejected authority never writes motion or advances a
            // smoothing schedule; next allowed tick samples live motion.
            return;
        }

        double forward =
                (inputState.keyPressed(
                        LegacyKeyboardCodes.W)
                        ? 1.0D
                        : 0.0D)
                        + (inputState.keyPressed(
                                LegacyKeyboardCodes.S)
                                ? -1.0D
                                : 0.0D);
        double strafe =
                (inputState.keyPressed(
                        LegacyKeyboardCodes.A)
                        ? 1.0D
                        : 0.0D)
                        + (inputState.keyPressed(
                                LegacyKeyboardCodes.D)
                                ? -1.0D
                                : 0.0D);

        if (Double.compare(
                forward,
                0.0D) == 0
                && Double.compare(
                        strafe,
                        0.0D) == 0) {
            return;
        }

        final double inputLength =
                Math.sqrt(
                        forward * forward
                                + strafe * strafe);
        if (inputLength > 1.0D) {
            forward /= inputLength;
            strafe /= inputLength;
        }

        final double yawRadians =
                Math.toRadians(
                        rotation.yaw());
        final double sin =
                Math.sin(
                        yawRadians);
        final double cos =
                Math.cos(
                        yawRadians);
        final double configuredSpeed =
                speed.get().doubleValue();

        final double targetMotionX =
                cleanZero(
                        (-sin * forward
                                + cos * strafe)
                                * configuredSpeed);
        final double targetMotionZ =
                cleanZero(
                        (cos * forward
                                + sin * strafe)
                                * configuredSpeed);

        final boolean smooth = smoothAcceleration.get().booleanValue();
        final double fraction = smooth
                ? accelerationPercent.get().intValue() / 100.0D
                : 1.0D;
        final double currentX = player.customMcMotionX();
        final double currentZ = player.customMcMotionZ();
        final double nextX = interpolate(currentX, targetMotionX, fraction);
        final double nextZ = interpolate(currentZ, targetMotionZ, fraction);

        if (Double.compare(currentX, nextX) != 0) {
            player.customMcSetMotionX(nextX);
        }
        if (Double.compare(currentZ, nextZ) != 0) {
            player.customMcSetMotionZ(nextZ);
        }
    }

    synchronized boolean active() {
        return enabled;
    }

    static double interpolate(
            final double current,
            final double target,
            final double fraction) {
        // The original instantaneous path remains bit-for-bit compatible.
        // Nonfinite mapped motion cannot poison a smoothed target.
        if (fraction >= 1.0D || !Double.isFinite(current)) {
            return target;
        }
        final double delta = target - current;
        if (!Double.isFinite(delta)) {
            return target;
        }
        if (Math.abs(delta) <= 0.000001D) {
            return target;
        }
        return cleanZero(current + delta * fraction);
    }

    private static boolean validSpeed(
            final Double value) {
        return value != null
                && !Double.isNaN(value.doubleValue())
                && !Double.isInfinite(value.doubleValue())
                && value.doubleValue() >= MINIMUM_SPEED
                && value.doubleValue() <= MAXIMUM_SPEED;
    }

    private static double cleanZero(
            final double value) {
        return Math.abs(value) < 0.000000000001D
                ? 0.0D
                : value;
    }
}
