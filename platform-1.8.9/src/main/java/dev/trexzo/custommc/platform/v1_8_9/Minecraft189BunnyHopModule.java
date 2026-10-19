package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;

import java.util.Objects;

public final class Minecraft189BunnyHopModule
        implements Module {
    public static final String ID =
            "movement.bunnyHop";
    public static final String SPEED_SETTING_ID =
            "movement.bunnyHop.speed";
    public static final String SMOOTH_ACCELERATION_SETTING_ID =
            "movement.bunnyHop.smoothAcceleration";
    public static final String LANDING_DELAY_SETTING_ID =
            "movement.bunnyHop.landingDelayTicks";
    public static final int DEFAULT_LANDING_DELAY_TICKS = 0;
    public static final int MAXIMUM_LANDING_DELAY_TICKS = 10;
    public static final String ACCELERATION_PERCENT_SETTING_ID =
            "movement.bunnyHop.accelerationPercent";
    public static final int DEFAULT_ACCELERATION_PERCENT = 50;
    public static final double DEFAULT_SPEED =
            0.38D;
    public static final double MINIMUM_SPEED =
            0.10D;
    public static final double MAXIMUM_SPEED =
            1.00D;

    private final Minecraft189InputState inputState;
    private final Setting<Double> speed =
            new Setting<Double>(
                    SPEED_SETTING_ID,
                    DEFAULT_SPEED,
                    Minecraft189BunnyHopModule::validSpeed,
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
                    value -> value != null && value >= 10
                            && value <= 100,
                    SettingCodecs.INTEGER);
    private final Setting<Integer> landingDelayTicks = new Setting<Integer>(
            LANDING_DELAY_SETTING_ID, DEFAULT_LANDING_DELAY_TICKS,
            value -> value != null && value >= 0
                    && value <= MAXIMUM_LANDING_DELAY_TICKS,
            SettingCodecs.INTEGER);
    private boolean enabled;
    // Original native jump/velocity callbacks cannot own a later activation.
    private long nativeHopEpoch;
    private boolean armed = true;
    private int eligibleLandingCallbacks;
    private int observedLandingDelay;
    // An original mapped jump() can restart BunnyHop and rearm before
    // the outer native call has unwound; never spend that new owner
    // from the still-running original source stack.
    private boolean nativeJumpDispatching;
    // Mapped horizontal getters/setters may reenter the same host while
    // the first player and X/Z values have not yet been committed.
    private boolean nativeMotionDispatching;
    private Minecraft189PlayerMotionControl nativeMotionPlayer;
    private boolean nativeMotionRevoked;

    Minecraft189BunnyHopModule(
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

    public Setting<Integer> landingDelayTicksSetting() {
        return landingDelayTicks;
    }

    @Override
    public synchronized void onEnable() {
        enabled = true;
        nativeHopEpoch++;
        armed = true;
        resetLandingDelay();
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
        nativeHopEpoch++;
        armed = true;
        resetLandingDelay();
    }

    synchronized void applyJump(
            final Minecraft189PlayerJumpControl player,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean suspended) {
        applyJump(player, movement, suspended, false);
    }

    synchronized void applyJump(
            final Minecraft189PlayerJumpControl player,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean suspended,
            final boolean actualClickGuiOpen) {
        Objects.requireNonNull(movement, "movement");
        if (nativeJumpDispatching || nativeMotionDispatching) {
            // Do not mutate a newly reenabled activation's cadence
            // from a nested stale original host callback.
            return;
        }
        // Discard the old grounded jump lease even if the GUI was opened
        // while airborne. Merely suspending one callback leaves 'armed'
        // true and could replay an old W hold after menu close.
        if (actualClickGuiOpen) {
            armed = false;
            resetLandingDelay();
            return;
        }
        if (!enabled
                || player == null
                || !movement.available()) {
            resetLandingDelay();
            return;
        }

        if (!movement.onGround()) {
            armed = true;
            resetLandingDelay();
            return;
        }

        final int configuredDelay = landingDelayTicks.get().intValue();
        if (configuredDelay != observedLandingDelay) {
            // A live edit never inherits waiting credit from the previous
            // configuration, even on a continuously held movement key.
            resetLandingDelay();
        }
        if (suspended || !movementInputHeld() || !armed) {
            // Waiting is earned only on consecutive eligible grounded
            // callbacks, never while an owner blocks Bunny Hop.
            eligibleLandingCallbacks = 0;
            return;
        }
        if (eligibleLandingCallbacks < configuredDelay) {
            eligibleLandingCallbacks++;
            return;
        }

        // Consume the old ground-contact lease BEFORE the mapped original
        // player jump. That external call may synchronously OFF->ON and
        // arm a NEW BunnyHop activation; the old callback cannot then
        // disarm its new owner after the jump.
        armed = false;
        eligibleLandingCallbacks = 0;
        nativeJumpDispatching = true;
        try {
            player.customMcJump();
        } finally {
            // Actual native jump motion may already be changed.
            // Never synthesize an un-jump on a source exception.
            nativeJumpDispatching = false;
        }
    }

    synchronized boolean applyMotion(
            final Minecraft189PlayerMotionControl player,
            final Minecraft189PlayerRotationState.Snapshot rotation,
            final boolean suspended) {
        Objects.requireNonNull(
                rotation,
                "rotation");
        if (nativeMotionDispatching) {
            if (!enabled || player != nativeMotionPlayer || suspended
                    || !rotation.available()) {
                nativeMotionRevoked = true;
            }
            return false;
        }
        if (nativeJumpDispatching) return false;
        if (!enabled
                || suspended
                || player == null
                || !rotation.available()) {
            return false;
        }

        nativeMotionDispatching = true;
        nativeMotionPlayer = player;
        nativeMotionRevoked = false;
        try {
            final long ownerEpoch = nativeHopEpoch;
            final boolean w = inputState.keyPressed(LegacyKeyboardCodes.W);
            final boolean backward = inputState.keyPressed(LegacyKeyboardCodes.S);
            final boolean a = inputState.keyPressed(LegacyKeyboardCodes.A);
            final boolean d = inputState.keyPressed(LegacyKeyboardCodes.D);
            double forward = (w ? 1.0D : 0.0D)
                    - (backward ? 1.0D : 0.0D);
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
            final double targetMotionX = cleanZero(
                    (-sin * forward + cos * strafe) * configuredSpeed);
            final double targetMotionZ = cleanZero(
                    (cos * forward + sin * strafe) * configuredSpeed);

            // Use genuine original velocity for interpolation; each native
            // getter may synchronously reenter module lifecycle, GUI policy
            // or physical keys. Never read Z or write old X/Z after losing
            // the exact callback owner.
            final double currentX = player.customMcMotionX();
            if (nativeMotionRevoked
                    || !nativeMotionReadOwned(ownerEpoch, w, backward, a, d,
                            configuredSpeed, smoothing, acceleration)) {
                return false;
            }
            final double currentZ = player.customMcMotionZ();
            if (nativeMotionRevoked
                    || !nativeMotionReadOwned(ownerEpoch, w, backward, a, d,
                            configuredSpeed, smoothing, acceleration)) {
                return false;
            }

            final double nextX = interpolate(currentX, targetMotionX, fraction);
            final double nextZ = interpolate(currentZ, targetMotionZ, fraction);
            if (Double.compare(currentX, nextX) != 0) {
                player.customMcSetMotionX(nextX);
                if (nativeMotionRevoked
                        || !nativeMotionReadOwned(ownerEpoch, w, backward, a, d,
                                configuredSpeed, smoothing, acceleration)) {
                    return false;
                }
            }
            if (Double.compare(currentZ, nextZ) != 0) {
                player.customMcSetMotionZ(nextZ);
            }
            return !nativeMotionRevoked
                    && nativeMotionReadOwned(ownerEpoch, w, backward, a, d,
                            configuredSpeed, smoothing, acceleration);
        } finally {
            // Genuine native X/Z setter can change original motion
            // and then throw. Clear only our same-stack authority.
            nativeMotionPlayer = null;
            nativeMotionRevoked = false;
            nativeMotionDispatching = false;
        }
    }

    private boolean nativeMotionReadOwned(
            final long epoch, final boolean w, final boolean backward,
            final boolean a, final boolean d, final double speedAtRead,
            final boolean smoothing, final int acceleration) {
        return enabled && nativeHopEpoch == epoch
                && inputState.keyPressed(LegacyKeyboardCodes.W) == w
                && inputState.keyPressed(LegacyKeyboardCodes.S) == backward
                && inputState.keyPressed(LegacyKeyboardCodes.A) == a
                && inputState.keyPressed(LegacyKeyboardCodes.D) == d
                && speed.get().doubleValue() == speedAtRead
                && smoothAcceleration.get().booleanValue() == smoothing
                && accelerationPercent.get().intValue() == acceleration;
    }

    synchronized boolean active() {
        return enabled;
    }

    private void resetLandingDelay() {
        eligibleLandingCallbacks = 0;
        observedLandingDelay = landingDelayTicks.get().intValue();
    }

    private boolean movementInputHeld() {
        return inputState.keyPressed(
                LegacyKeyboardCodes.W)
                || inputState.keyPressed(
                        LegacyKeyboardCodes.A)
                || inputState.keyPressed(
                        LegacyKeyboardCodes.S)
                || inputState.keyPressed(
                        LegacyKeyboardCodes.D);
    }

    static double interpolate(
            final double current,
            final double target,
            final double fraction) {
        // Smooth mode must not propagate invalid mapped velocity.
        // With the feature OFF, preserve the original direct target.
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
