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
        boostPending = false;
        spaceWasPressed =
                inputState.keyPressed(
                        LegacyKeyboardCodes.SPACE);
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
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
            boostPending = true;
        } finally {
            spaceWasPressed = spacePressed;
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

        final double inputLength =
                Math.sqrt(
                        forward * forward
                                + strafe * strafe);
        if (inputLength <= 0.0D) {
            return false;
        }
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

        if (preserveHigherMomentum.get().booleanValue()) {
            final double existingX = player.customMcMotionX();
            final double existingZ = player.customMcMotionZ();
            final double existingMagnitude = Math.hypot(existingX, existingZ);
            // Preserve the full existing horizontal vector if its real
            // magnitude already exceeds the configured Long Jump boost.
            // A fresh jump still owns this callback, preventing lower
            // priority movement modules from overwriting the preserved
            // momentum. Invalid mapped velocity fails closed.
            if (!Double.isFinite(existingMagnitude)
                    || existingMagnitude + 0.000000001D >= configuredSpeed) {
                return true;
            }
        }

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

        if (Double.compare(
                player.customMcMotionX(),
                targetMotionX) != 0) {
            player.customMcSetMotionX(
                    targetMotionX);
        }
        if (Double.compare(
                player.customMcMotionZ(),
                targetMotionZ) != 0) {
            player.customMcSetMotionZ(
                    targetMotionZ);
        }
        return true;
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
