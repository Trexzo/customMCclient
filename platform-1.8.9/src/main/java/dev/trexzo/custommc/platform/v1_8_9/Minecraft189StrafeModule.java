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
    // Mapped native motion getter callbacks may reenter module lifecycle.
    private long nativeReadEpoch;
    // Mapped original X/Z getters and setters can reenter synchronously.
    // One native source stack owns exactly one original player.
    private boolean nativeMotionDispatching;
    private Minecraft189PlayerMotionControl nativeMotionPlayer;
    private boolean nativeMotionRevoked;

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
        nativeReadEpoch++;
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
        nativeReadEpoch++;
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
        if (nativeMotionDispatching) {
            // Another player, suspended host or a newly ineligible
            // grounded/sneaking source revokes the outer X/Z transaction.
            if (!enabled || player != nativeMotionPlayer || suspended
                    || !rotation.available()
                    || (groundOnly.get().booleanValue()
                        && (movement == null || !movement.available()
                            || !movement.onGround()))
                    || (pauseWhileSneaking.get().booleanValue()
                        && (movement == null || !movement.available()
                            || movement.sneaking()))) {
                nativeMotionRevoked = true;
            }
            return;
        }
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

        final long ownerEpoch = nativeReadEpoch;
        final boolean w = inputState.keyPressed(LegacyKeyboardCodes.W);
        final boolean backward = inputState.keyPressed(LegacyKeyboardCodes.S);
        final boolean a = inputState.keyPressed(LegacyKeyboardCodes.A);
        final boolean d = inputState.keyPressed(LegacyKeyboardCodes.D);
        double forward = (w ? 1.0D : 0.0D) - (backward ? 1.0D : 0.0D);
        double strafe = (a ? 1.0D : 0.0D) - (d ? 1.0D : 0.0D);
        if (forward == 0.0D && strafe == 0.0D) {
            return;
        }
        final double inputLength = Math.hypot(forward, strafe);
        if (inputLength > 1.0D) {
            forward /= inputLength;
            strafe /= inputLength;
        }

        final double configuredSpeed = speed.get().doubleValue();
        final boolean smooth = smoothAcceleration.get().booleanValue();
        final int acceleration = accelerationPercent.get().intValue();
        final boolean groundGuard = groundOnly.get().booleanValue();
        final boolean sneakGuard = pauseWhileSneaking.get().booleanValue();
        final double fraction = smooth ? acceleration / 100.0D : 1.0D;
        final double yawRadians = Math.toRadians(rotation.yaw());
        final double sin = Math.sin(yawRadians);
        final double cos = Math.cos(yawRadians);
        final double targetX = cleanZero(
                (-sin * forward + cos * strafe) * configuredSpeed);
        final double targetZ = cleanZero(
                (cos * forward + sin * strafe) * configuredSpeed);

        nativeMotionDispatching = true;
        nativeMotionPlayer = player;
        nativeMotionRevoked = false;
        try {
            // Actual mapped player motion getters can synchronously reenter
            // module activation, edit speed/ground/sneak policy, or change
            // physical keys. Recheck BEFORE reading Z, before either write,
            // and after X setter before a dependent Z setter.
            final double currentX = player.customMcMotionX();
            if (!nativeMotionOwned(ownerEpoch, w, backward, a, d,
                    configuredSpeed, smooth, acceleration, groundGuard,
                    sneakGuard, movement)) {
                return;
            }
            final double currentZ = player.customMcMotionZ();
            if (!nativeMotionOwned(ownerEpoch, w, backward, a, d,
                    configuredSpeed, smooth, acceleration, groundGuard,
                    sneakGuard, movement)) {
                return;
            }
            final double nextX = interpolate(currentX, targetX, fraction);
            final double nextZ = interpolate(currentZ, targetZ, fraction);
            if (Double.compare(currentX, nextX) != 0) {
                player.customMcSetMotionX(nextX);
                if (!nativeMotionOwned(ownerEpoch, w, backward, a, d,
                        configuredSpeed, smooth, acceleration, groundGuard,
                        sneakGuard, movement)) {
                    return;
                }
            }
            if (Double.compare(currentZ, nextZ) != 0) {
                player.customMcSetMotionZ(nextZ);
            }
        } finally {
            // Original X/Z writes may complete before their mapped
            // setter throws; never fake a vanilla velocity rollback.
            nativeMotionPlayer = null;
            nativeMotionRevoked = false;
            nativeMotionDispatching = false;
        }
    }

    private boolean nativeMotionOwned(
            final long epoch, final boolean w, final boolean backward,
            final boolean a, final boolean d, final double desiredSpeed,
            final boolean smooth, final int acceleration,
            final boolean groundGuard, final boolean sneakGuard,
            final Minecraft189PlayerMovementState.Snapshot movement) {
        return enabled && !nativeMotionRevoked
                && nativeReadEpoch == epoch
                && inputState.keyPressed(LegacyKeyboardCodes.W) == w
                && inputState.keyPressed(LegacyKeyboardCodes.S) == backward
                && inputState.keyPressed(LegacyKeyboardCodes.A) == a
                && inputState.keyPressed(LegacyKeyboardCodes.D) == d
                && speed.get().doubleValue() == desiredSpeed
                && smoothAcceleration.get().booleanValue() == smooth
                && accelerationPercent.get().intValue() == acceleration
                && groundOnly.get().booleanValue() == groundGuard
                && pauseWhileSneaking.get().booleanValue() == sneakGuard
                && (!groundGuard || (movement != null
                    && movement.available() && movement.onGround()))
                && (!sneakGuard || (movement != null
                    && movement.available() && !movement.sneaking()));
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
