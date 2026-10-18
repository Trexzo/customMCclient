package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;

import java.util.Objects;

public final class Minecraft189LongJumpModule
        implements Module {
    public static final String ID =
            "movement.longJump";
    public static final String PRESERVE_MOMENTUM_SETTING_ID =
            "movement.longJump.preserveHigherMomentum";
    public static final String SPEED_SETTING_ID =
            "movement.longJump.speed";
    public static final double DEFAULT_SPEED =
            0.65D;
    public static final double MINIMUM_SPEED =
            0.10D;
    public static final double MAXIMUM_SPEED =
            1.50D;

    private final Minecraft189InputState inputState;
    private final Setting<Double> speed =
            new Setting<Double>(
                    SPEED_SETTING_ID,
                    DEFAULT_SPEED,
                    Minecraft189LongJumpModule::validSpeed,
                    SettingCodecs.DOUBLE);
    private final Setting<Boolean> preserveHigherMomentum =
            new Setting<Boolean>(
                    PRESERVE_MOMENTUM_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);
    private boolean enabled;
    // Invalidate original jump and horizontal reads across OFF/ON.
    private long nativeJumpEpoch;
    private boolean spaceWasPressed;
    private boolean boostPending;

    Minecraft189LongJumpModule(
            final Minecraft189InputState inputState) {
        this.inputState =
                Objects.requireNonNull(
                        inputState,
                        "inputState");
        spaceWasPressed =
                inputState.keyPressed(
                        LegacyKeyboardCodes.SPACE);
    }

    @Override
    public String id() {
        return ID;
    }

    public Setting<Double> speedSetting() {
        return speed;
    }

    public Setting<Boolean> preserveHigherMomentumSetting() {
        return preserveHigherMomentum;
    }

    @Override
    public synchronized void onEnable() {
        enabled = true;
        nativeJumpEpoch++;
        boostPending = false;
        spaceWasPressed =
                inputState.keyPressed(
                        LegacyKeyboardCodes.SPACE);
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
        nativeJumpEpoch++;
        boostPending = false;
        spaceWasPressed =
                inputState.keyPressed(
                        LegacyKeyboardCodes.SPACE);
    }

    synchronized void applyJump(
            final Minecraft189PlayerJumpControl player,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean suspended) {
        Objects.requireNonNull(
                movement,
                "movement");
        final boolean spacePressed =
                inputState.keyPressed(
                        LegacyKeyboardCodes.SPACE);
        final long ownerEpoch = nativeJumpEpoch;
        try {
            if (!enabled
                    || suspended
                    || player == null
                    || !movement.available()
                    || !movement.onGround()
                    || !spacePressed
                    || spaceWasPressed
                    || !movementInputHeld()) {
                return;
            }

            player.customMcJump();
            if (enabled && ownerEpoch == nativeJumpEpoch) {
                boostPending = true;
            }
        } finally {
            if (ownerEpoch == nativeJumpEpoch) {
                spaceWasPressed = inputState.keyPressed(
                        LegacyKeyboardCodes.SPACE);
            }
        }
    }

    synchronized boolean applyMotion(
            final Minecraft189PlayerMotionControl player,
            final Minecraft189PlayerRotationState.Snapshot rotation,
            final boolean suspended) {
        Objects.requireNonNull(
                rotation,
                "rotation");
        if (!boostPending) {
            return false;
        }

        boostPending = false;
        if (!enabled
                || suspended
                || player == null
                || !rotation.available()) {
            return false;
        }

        final long ownerEpoch = nativeJumpEpoch;
        final boolean w = inputState.keyPressed(LegacyKeyboardCodes.W);
        final boolean a = inputState.keyPressed(LegacyKeyboardCodes.A);
        final boolean backward = inputState.keyPressed(LegacyKeyboardCodes.S);
        final boolean d = inputState.keyPressed(LegacyKeyboardCodes.D);
        double forward = (w ? 1.0D : 0.0D)
                - (backward ? 1.0D : 0.0D);
        double strafe = (a ? 1.0D : 0.0D)
                - (d ? 1.0D : 0.0D);
        final double inputLength = Math.hypot(forward, strafe);
        if (inputLength <= 0.0D) {
            return false;
        }
        if (inputLength > 1.0D) {
            forward /= inputLength;
            strafe /= inputLength;
        }

        final double configuredSpeed = speed.get().doubleValue();
        final boolean preserve = preserveHigherMomentum.get().booleanValue();
        final double yawRadians = Math.toRadians(rotation.yaw());
        final double sin = Math.sin(yawRadians);
        final double cos = Math.cos(yawRadians);
        final double targetMotionX = cleanZero(
                (-sin * forward + cos * strafe) * configuredSpeed);
        final double targetMotionZ = cleanZero(
                (cos * forward + sin * strafe) * configuredSpeed);

        // Read the two actual original player axes only once each; either
        // source getter may reenter module enable/disable, GUI settings
        // or mapped input. Revalidate BEFORE touching the second source
        // and BEFORE producing either horizontal write.
        final double originalX = player.customMcMotionX();
        if (!nativeMotionReadOwned(ownerEpoch, configuredSpeed, preserve,
                w, a, backward, d)) {
            return false;
        }
        final double originalZ = player.customMcMotionZ();
        if (!nativeMotionReadOwned(ownerEpoch, configuredSpeed, preserve,
                w, a, backward, d)) {
            return false;
        }

        if (preserve) {
            final double existingMagnitude = Math.hypot(originalX, originalZ);
            // Original momentum remains authoritative when above the
            // requested boost; invalid native source still fails closed.
            if (!Double.isFinite(existingMagnitude)
                    || existingMagnitude + 0.000000001D >= configuredSpeed) {
                return true;
            }
        }

        if (Double.compare(originalX, targetMotionX) != 0) {
            player.customMcSetMotionX(targetMotionX);
            // An original mapped X setter can revoke this entire jump
            // owner; never issue stale Z after an OFF/ON or key edit.
            if (!nativeMotionReadOwned(ownerEpoch, configuredSpeed, preserve,
                    w, a, backward, d)) {
                return false;
            }
        }
        if (Double.compare(originalZ, targetMotionZ) != 0) {
            player.customMcSetMotionZ(targetMotionZ);
        }
        return true;
    }

    private boolean nativeMotionReadOwned(
            final long epoch, final double desiredSpeed,
            final boolean preserve, final boolean w, final boolean a,
            final boolean backward, final boolean d) {
        return enabled && nativeJumpEpoch == epoch
                && speed.get().doubleValue() == desiredSpeed
                && preserveHigherMomentum.get().booleanValue() == preserve
                && inputState.keyPressed(LegacyKeyboardCodes.W) == w
                && inputState.keyPressed(LegacyKeyboardCodes.A) == a
                && inputState.keyPressed(LegacyKeyboardCodes.S) == backward
                && inputState.keyPressed(LegacyKeyboardCodes.D) == d;
    }

    synchronized boolean active() {
        return enabled;
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
