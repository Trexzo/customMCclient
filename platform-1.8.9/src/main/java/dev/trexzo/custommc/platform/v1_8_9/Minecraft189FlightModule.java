package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;
import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;

import java.util.Objects;

public final class Minecraft189FlightModule
        implements Module {
    public static final String ID =
            "movement.flight";
    public static final String HORIZONTAL_SPEED_SETTING_ID =
            "movement.flight.horizontalSpeed";
    public static final String VERTICAL_SPEED_SETTING_ID =
            "movement.flight.verticalSpeed";
    public static final double DEFAULT_HORIZONTAL_SPEED =
            0.30D;
    public static final double DEFAULT_VERTICAL_SPEED =
            0.30D;
    public static final double HORIZONTAL_MOTION =
            DEFAULT_HORIZONTAL_SPEED;
    public static final double ASCEND_MOTION_Y =
            DEFAULT_VERTICAL_SPEED;
    public static final double DESCEND_MOTION_Y =
            -DEFAULT_VERTICAL_SPEED;
    public static final double MINIMUM_SPEED =
            0.05D;
    public static final double MAXIMUM_SPEED =
            1.00D;
    public static final double HOVER_MOTION_Y =
            0.0D;

    private final Minecraft189InputState inputState;
    private final Setting<Double> horizontalSpeed =
            new Setting<Double>(
                    HORIZONTAL_SPEED_SETTING_ID,
                    DEFAULT_HORIZONTAL_SPEED,
                    Minecraft189FlightModule::validSpeed,
                    SettingCodecs.DOUBLE);
    private final Setting<Double> verticalSpeed =
            new Setting<Double>(
                    VERTICAL_SPEED_SETTING_ID,
                    DEFAULT_VERTICAL_SPEED,
                    Minecraft189FlightModule::validSpeed,
                    SettingCodecs.DOUBLE);
    private boolean enabled;

    Minecraft189FlightModule(
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

    public Setting<Double> horizontalSpeedSetting() {
        return horizontalSpeed;
    }

    public Setting<Double> verticalSpeedSetting() {
        return verticalSpeed;
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
            final Minecraft189PlayerRotationState.Snapshot rotation) {
        Objects.requireNonNull(
                rotation,
                "rotation");
        if (!enabled || player == null) {
            return;
        }

        applyVertical(
                player);
        if (rotation.available()) {
            applyHorizontal(
                    player,
                    rotation.yaw());
        }
    }

    private void applyVertical(
            final Minecraft189PlayerMotionControl player) {
        final boolean ascend =
                inputState.keyPressed(
                        LegacyKeyboardCodes.SPACE);
        final boolean descend =
                inputState.keyPressed(
                        LegacyKeyboardCodes.LEFT_SHIFT)
                        || inputState.keyPressed(
                                LegacyKeyboardCodes.RIGHT_SHIFT);
        final double configuredVerticalSpeed =
                verticalSpeed.get().doubleValue();

        final double targetMotionY;
        if (ascend == descend) {
            targetMotionY =
                    HOVER_MOTION_Y;
        } else if (ascend) {
            targetMotionY =
                    configuredVerticalSpeed;
        } else {
            targetMotionY =
                    -configuredVerticalSpeed;
        }

        if (Double.compare(
                player.customMcMotionY(),
                targetMotionY) != 0) {
            player.customMcSetMotionY(
                    targetMotionY);
        }
    }

    private void applyHorizontal(
            final Minecraft189PlayerMotionControl player,
            final float yawDegrees) {
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
        if (inputLength > 1.0D) {
            forward /= inputLength;
            strafe /= inputLength;
        }

        final double yawRadians =
                Math.toRadians(
                        yawDegrees);
        final double sin =
                Math.sin(
                        yawRadians);
        final double cos =
                Math.cos(
                        yawRadians);
        final double configuredHorizontalSpeed =
                horizontalSpeed.get().doubleValue();

        final double targetMotionX =
                cleanZero(
                        (-sin * forward
                                + cos * strafe)
                                * configuredHorizontalSpeed);
        final double targetMotionZ =
                cleanZero(
                        (cos * forward
                                + sin * strafe)
                                * configuredHorizontalSpeed);

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

    synchronized boolean active() {
        return enabled;
    }
}
