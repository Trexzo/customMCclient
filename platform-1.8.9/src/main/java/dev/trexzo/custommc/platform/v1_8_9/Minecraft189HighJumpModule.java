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
    private boolean enabled;
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
        try {
            if (!enabled
                    || suspended
                    || player == null
                    || !movement.available()
                    || !movement.onGround()
                    || !spacePressed
                    || spaceWasPressed) {
                return false;
            }

            player.customMcJump();
            boostPending = true;
            return true;
        } finally {
            spaceWasPressed = spacePressed;
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

        final double targetMotionY =
                verticalSpeed.get().doubleValue();
        if (player.customMcMotionY()
                < targetMotionY) {
            player.customMcSetMotionY(
                    targetMotionY);
        }
        return true;
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
