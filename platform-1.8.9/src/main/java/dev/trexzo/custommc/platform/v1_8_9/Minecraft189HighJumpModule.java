package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;

import java.util.Objects;

public final class Minecraft189HighJumpModule
        implements Module {
    public static final String ID =
            "movement.highJump";
    public static final String REQUIRE_MOVEMENT_SETTING_ID =
            "movement.highJump.requireMovement";
    public static final String VERTICAL_SPEED_SETTING_ID =
            "movement.highJump.verticalSpeed";
    public static final double DEFAULT_VERTICAL_SPEED =
            0.70D;
    public static final double MINIMUM_VERTICAL_SPEED =
            0.42D;
    public static final double MAXIMUM_VERTICAL_SPEED =
            1.50D;

    private final Minecraft189InputState inputState;
    private final Setting<Double> verticalSpeed =
            new Setting<Double>(
                    VERTICAL_SPEED_SETTING_ID,
                    DEFAULT_VERTICAL_SPEED,
                    Minecraft189HighJumpModule::validVerticalSpeed,
                    SettingCodecs.DOUBLE);
    private final Setting<Boolean> requireMovement =
            new Setting<Boolean>(
                    REQUIRE_MOVEMENT_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);
    private boolean enabled;
    // Original player jump()/motionY() may reenter module lifecycle.
    private long nativeJumpEpoch;
    private boolean spaceWasPressed;
    private boolean boostPending;

    Minecraft189HighJumpModule(
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

    public Setting<Double> verticalSpeedSetting() {
        return verticalSpeed;
    }

    public Setting<Boolean> requireMovementSetting() {
        return requireMovement;
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

    synchronized boolean applyJump(
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
        final boolean requiresMovement = requireMovement.get().booleanValue();
        try {
            if (!enabled
                    || suspended
                    || player == null
                    || !movement.available()
                    || !movement.onGround()
                    || !spacePressed
                    || spaceWasPressed
                    || (requiresMovement
                            && !movementInputHeld())) {
                return false;
            }

            player.customMcJump();
            // Real mapped jump() can synchronously disable/re-enable
            // HighJump, alter its policy, or consume the input edge.
            // Only the ORIGINAL activation may queue a subsequent motion.
            if (!enabled || ownerEpoch != nativeJumpEpoch
                    || requireMovement.get().booleanValue() != requiresMovement) {
                return false;
            }
            boostPending = true;
            return true;
        } finally {
            // onEnable/onDisable already sampled physical Space for the
            // NEW owner. Never overwrite that source edge with an old
            // pre-callback read. Normal ownership observes current input.
            if (ownerEpoch == nativeJumpEpoch) {
                spaceWasPressed = inputState.keyPressed(
                        LegacyKeyboardCodes.SPACE);
            }
        }
    }

    synchronized boolean applyMotion(
            final Minecraft189PlayerMotionControl player,
            final boolean suspended) {
        if (!boostPending) {
            return false;
        }

        boostPending = false;
        if (!enabled
                || suspended
                || player == null) {
            return false;
        }

        final long ownerEpoch = nativeJumpEpoch;
        final double targetMotionY =
                verticalSpeed.get().doubleValue();
        final double originalMotionY = player.customMcMotionY();
        // A mapped original getter may restart the module or change the
        // live boost speed. Do not apply a stale pending jump's velocity
        // to the new native player/module lifetime.
        if (!enabled || ownerEpoch != nativeJumpEpoch
                || verticalSpeed.get().doubleValue() != targetMotionY) {
            return false;
        }
        if (originalMotionY < targetMotionY) {
            player.customMcSetMotionY(targetMotionY);
        }
        return true;
    }

    private boolean movementInputHeld() {
        return inputState.keyPressed(LegacyKeyboardCodes.W)
                || inputState.keyPressed(LegacyKeyboardCodes.A)
                || inputState.keyPressed(LegacyKeyboardCodes.S)
                || inputState.keyPressed(LegacyKeyboardCodes.D);
    }

    synchronized boolean active() {
        return enabled;
    }

    private static boolean validVerticalSpeed(
            final Double value) {
        return value != null
                && !Double.isNaN(value.doubleValue())
                && !Double.isInfinite(value.doubleValue())
                && value.doubleValue() >= MINIMUM_VERTICAL_SPEED
                && value.doubleValue() <= MAXIMUM_VERTICAL_SPEED;
    }
}
