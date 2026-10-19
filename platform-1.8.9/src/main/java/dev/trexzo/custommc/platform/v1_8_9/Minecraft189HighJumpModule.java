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
    // Original EntityPlayer.jump() can reenter the host before the Space
    // edge is consumed. Only one physical edge owns a native jump stack.
    private boolean nativeJumpDispatching;
    // The follow-up original Y getter/setter can also reenter the host.
    // Pin the native player and prohibit a nested boost from borrowing it.
    private boolean nativeBoostDispatching;
    private Minecraft189PlayerMotionControl nativeBoostPlayer;
    private boolean nativeBoostRevoked;

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
        // The mapped jump or boost setter may synchronously call the
        // original host while the prior Space edge is still pending.
        if (nativeJumpDispatching || nativeBoostDispatching) return false;
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

            nativeJumpDispatching = true;
            try {
                player.customMcJump();
            } finally {
                // Native jump may have already changed real motion before
                // throwing. Release only the stack guard, never un-jump.
                nativeJumpDispatching = false;
            }
            // Real mapped jump() can synchronously disable/re-enable
            // HighJump, alter its policy, or consume the input edge.
            // Only the ORIGINAL activation may queue a subsequent motion.
            if (!enabled || ownerEpoch != nativeJumpEpoch
                    || requireMovement.get().booleanValue() != requiresMovement
                    || !inputState.keyPressed(LegacyKeyboardCodes.SPACE)) {
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
        if (nativeBoostDispatching) {
            // The original mapped getter/setter may reenter movement.
            // Another player or a suspended/disabled host callback must
            // revoke the outer, still-active source motion transaction.
            if (player != nativeBoostPlayer || suspended || !enabled) {
                nativeBoostRevoked = true;
            }
            return false;
        }
        if (!boostPending) return false;

        // Spend this particular jump's boost once. A reentrant native
        // motion callback must never reuse it as a second source write.
        boostPending = false;
        if (!enabled || suspended || player == null) return false;

        nativeBoostDispatching = true;
        nativeBoostPlayer = player;
        nativeBoostRevoked = false;
        try {
            final long ownerEpoch = nativeJumpEpoch;
            final double targetMotionY = verticalSpeed.get().doubleValue();
            final double originalMotionY = player.customMcMotionY();
            // Require finite ORIGINAL Y evidence and unchanged source
            // lifetime/policy after mapped getters have run. A new jump
            // queued in a getter belongs to the next original callback.
            if (!enabled || nativeBoostRevoked
                    || ownerEpoch != nativeJumpEpoch || boostPending
                    || verticalSpeed.get().doubleValue() != targetMotionY
                    || !Double.isFinite(originalMotionY)) {
                return false;
            }
            if (originalMotionY < targetMotionY) {
                player.customMcSetMotionY(targetMotionY);
            }
            return enabled && !nativeBoostRevoked
                    && nativeJumpEpoch == ownerEpoch
                    && verticalSpeed.get().doubleValue() == targetMotionY;
        } finally {
            // A vanilla setter may write motionY and then throw: never
            // fabricate a rollback of the genuine player's vertical Y.
            nativeBoostPlayer = null;
            nativeBoostRevoked = false;
            nativeBoostDispatching = false;
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
