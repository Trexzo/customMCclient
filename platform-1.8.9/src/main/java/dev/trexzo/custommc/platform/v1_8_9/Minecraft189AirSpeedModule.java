package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;

import java.util.Objects;

public final class Minecraft189AirSpeedModule
        implements Module {
    public static final String ID =
            "movement.airSpeed";
    public static final String SPEED_SETTING_ID =
            "movement.airSpeed.speed";
    public static final String SMOOTH_ACCELERATION_SETTING_ID =
            "movement.airSpeed.smoothAcceleration";
    public static final String ACCELERATION_PERCENT_SETTING_ID =
            "movement.airSpeed.accelerationPercent";
    public static final int DEFAULT_ACCELERATION_PERCENT = 50;
    public static final double DEFAULT_SPEED =
            0.35D;
    public static final double MINIMUM_SPEED =
            0.10D;
    public static final double MAXIMUM_SPEED =
            1.00D;

    private final Minecraft189InputState inputState;
    private final Setting<Double> speed =
            new Setting<Double>(
                    SPEED_SETTING_ID,
                    DEFAULT_SPEED,
                    Minecraft189AirSpeedModule::validSpeed,
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
    private boolean enabled;
    // Reject native X/Z reads that outlive the current enabled activation.
    private long nativeReadEpoch;

    Minecraft189AirSpeedModule(
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

    synchronized boolean apply(
            final Minecraft189PlayerMotionControl player,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final Minecraft189PlayerRotationState.Snapshot rotation,
            final boolean suspended) {
        Objects.requireNonNull(
                movement,
                "movement");
        Objects.requireNonNull(
                rotation,
                "rotation");
        if (!enabled
                || suspended
                || player == null
                || !movement.available()
                || movement.onGround()
                || !rotation.available()) {
            return false;
        }

        final long epoch = nativeReadEpoch;
        final boolean w = inputState.keyPressed(LegacyKeyboardCodes.W);
        final boolean backward = inputState.keyPressed(LegacyKeyboardCodes.S);
        final boolean a = inputState.keyPressed(LegacyKeyboardCodes.A);
        final boolean d = inputState.keyPressed(LegacyKeyboardCodes.D);
        double forward = (w ? 1.0D : 0.0D) - (backward ? 1.0D : 0.0D);
        double strafe = (a ? 1.0D : 0.0D) - (d ? 1.0D : 0.0D);
        final double inputLength = Math.hypot(forward, strafe);
        if (inputLength <= 0.0D) {
            return false;
        }
        if (inputLength > 1.0D) {
            forward /= inputLength;
            strafe /= inputLength;
        }
        final double configuredSpeed = speed.get().doubleValue();
        final boolean smoothing = smoothAcceleration.get().booleanValue();
        final int acceleration = accelerationPercent.get().intValue();
        final double fraction = smoothing ? acceleration / 100.0D : 1.0D;
        final double yawRadians = Math.toRadians(rotation.yaw());
        final double sin = Math.sin(yawRadians);
        final double cos = Math.cos(yawRadians);
        final double targetX = cleanZero(
                (-sin * forward + cos * strafe) * configuredSpeed);
        final double targetZ = cleanZero(
                (cos * forward + sin * strafe) * configuredSpeed);

        // Original native getters may synchronously reenter module state,
        // physical WASD or GUI policy. Stale reads cannot control the
        // second axis or authorize stale velocity writes.
        final double currentX = player.customMcMotionX();
        if (!nativeReadOwned(epoch, w, backward, a, d, configuredSpeed,
                smoothing, acceleration)) {
            return false;
        }
        final double currentZ = player.customMcMotionZ();
        if (!nativeReadOwned(epoch, w, backward, a, d, configuredSpeed,
                smoothing, acceleration)) {
            return false;
        }
        final double nextX = interpolate(currentX, targetX, fraction);
        final double nextZ = interpolate(currentZ, targetZ, fraction);
        if (Double.compare(currentX, nextX) != 0) {
            player.customMcSetMotionX(nextX);
            if (!nativeReadOwned(epoch, w, backward, a, d, configuredSpeed,
                    smoothing, acceleration)) {
                return false;
            }
        }
        if (Double.compare(currentZ, nextZ) != 0) {
            player.customMcSetMotionZ(nextZ);
        }
        return true;
    }

    private boolean nativeReadOwned(final long epoch,
            final boolean w, final boolean backward,
            final boolean a, final boolean d, final double configuredSpeed,
            final boolean smoothing, final int acceleration) {
        return enabled && nativeReadEpoch == epoch
                && inputState.keyPressed(LegacyKeyboardCodes.W) == w
                && inputState.keyPressed(LegacyKeyboardCodes.S) == backward
                && inputState.keyPressed(LegacyKeyboardCodes.A) == a
                && inputState.keyPressed(LegacyKeyboardCodes.D) == d
                && speed.get().doubleValue() == configuredSpeed
                && smoothAcceleration.get().booleanValue() == smoothing
                && accelerationPercent.get().intValue() == acceleration;
    }

    synchronized boolean active() {
        return enabled;
    }

    static double interpolate(
            final double current,
            final double target,
            final double fraction) {
        // Default-off mode retains the exact original target write.
        // Invalid source velocity is repaired to the finite target.
        if (fraction >= 1.0D || !Double.isFinite(current)) {
            return target;
        }
        final double delta = target - current;
        if (!Double.isFinite(delta) || Math.abs(delta) <= 0.000001D) {
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
