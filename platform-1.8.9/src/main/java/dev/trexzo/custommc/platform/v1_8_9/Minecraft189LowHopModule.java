package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;

import java.util.Objects;

public final class Minecraft189LowHopModule
        implements Module {
    public static final String ID =
            "movement.lowHop";
    public static final String REQUIRE_MOVEMENT_SETTING_ID =
            "movement.lowHop.requireMovement";
    public static final String VERTICAL_SPEED_SETTING_ID =
            "movement.lowHop.verticalSpeed";
    public static final double DEFAULT_VERTICAL_SPEED =
            0.25D;
    public static final double MINIMUM_VERTICAL_SPEED =
            0.05D;
    public static final double MAXIMUM_VERTICAL_SPEED =
            0.41D;

    private final Minecraft189InputState inputState;
    private final Setting<Double> verticalSpeed =
            new Setting<Double>(
                    VERTICAL_SPEED_SETTING_ID,
                    DEFAULT_VERTICAL_SPEED,
                    Minecraft189LowHopModule::validVerticalSpeed,
                    SettingCodecs.DOUBLE);
    private final Setting<Boolean> requireMovement =
            new Setting<Boolean>(
                    REQUIRE_MOVEMENT_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);
    private boolean enabled;
    // Old native jump and mapped Y reads never own a new activation.
    private long nativeJumpEpoch;
    private boolean spaceWasPressed;
    private boolean adjustmentPending;
    // A mapped original EntityPlayer.jump() can synchronously reenter
    // before its physical Space edge is consumed by the outer callback.
    private boolean nativeJumpDispatching;
    // A later original Y getter or setter also owns only one native
    // player and motion callback at a time.
    private boolean nativeMotionDispatching;
    private Minecraft189PlayerMotionControl nativeMotionPlayer;
    private boolean nativeMotionRevoked;

    Minecraft189LowHopModule(
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
        adjustmentPending = false;
        spaceWasPressed =
                inputState.keyPressed(
                        LegacyKeyboardCodes.SPACE);
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
        nativeJumpEpoch++;
        adjustmentPending = false;
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
        if (nativeJumpDispatching || nativeMotionDispatching) return false;
        final boolean spacePressed =
                inputState.keyPressed(
                        LegacyKeyboardCodes.SPACE);
        final long ownerEpoch = nativeJumpEpoch;
        final boolean needsMovement = requireMovement.get().booleanValue();
        try {
            if (!enabled
                    || suspended
                    || player == null
                    || !movement.available()
                    || !movement.onGround()
                    || !spacePressed
                    || spaceWasPressed
                    || (needsMovement
                            && !movementInputHeld())) {
                return false;
            }

            nativeJumpDispatching = true;
            try {
                player.customMcJump();
            } finally {
                // A partially completing genuine jump must not be
                // undone if the mapped callback throws.
                nativeJumpDispatching = false;
            }
            if (!enabled || ownerEpoch != nativeJumpEpoch
                    || requireMovement.get().booleanValue() != needsMovement
) {
                return false;
            }
            adjustmentPending = true;
            return true;
        } finally {
            // Reentrant module restart has already sampled its new Space.
            // The old jump callback must never clobber a fresh key edge.
            if (ownerEpoch == nativeJumpEpoch) {
                spaceWasPressed = inputState.keyPressed(
                        LegacyKeyboardCodes.SPACE);
            }
        }
    }

    synchronized boolean applyMotion(
            final Minecraft189PlayerMotionControl player,
            final boolean suspended) {
        if (nativeMotionDispatching) {
            if (player != nativeMotionPlayer || suspended || !enabled) {
                nativeMotionRevoked = true;
            }
            return false;
        }
        if (!adjustmentPending) return false;

        // This one source Space edge may lower original Y once only.
        adjustmentPending = false;
        if (!enabled || suspended || player == null) return false;

        nativeMotionDispatching = true;
        nativeMotionPlayer = player;
        nativeMotionRevoked = false;
        try {
            final long ownerEpoch = nativeJumpEpoch;
            final double targetMotionY = verticalSpeed.get().doubleValue();
            final double originalMotionY = player.customMcMotionY();
            if (!enabled || nativeMotionRevoked
                    || ownerEpoch != nativeJumpEpoch || adjustmentPending
                    || verticalSpeed.get().doubleValue() != targetMotionY
                    || !Double.isFinite(originalMotionY)) {
                // An invalid genuine Y getter cannot justify lowering
                // the player's vanilla jump with a fabricated value.
                return false;
            }
            if (originalMotionY > targetMotionY) {
                player.customMcSetMotionY(targetMotionY);
            }
            return enabled && !nativeMotionRevoked
                    && ownerEpoch == nativeJumpEpoch
                    && verticalSpeed.get().doubleValue() == targetMotionY;
        } finally {
            // Never revert a genuine original Y write after its setter
            // changed vanilla motion and then raised an exception.
            nativeMotionPlayer = null;
            nativeMotionRevoked = false;
            nativeMotionDispatching = false;
        }
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
