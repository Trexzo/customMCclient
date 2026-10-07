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
    private boolean enabled;

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
            final Minecraft189PlayerRotationState.Snapshot rotation,
            final boolean suspended) {
        Objects.requireNonNull(
                rotation,
                "rotation");
        if (!enabled
                || suspended
                || player == null
                || !rotation.available()) {
            return;
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

        if (Double.compare(
                forward,
                0.0D) == 0
                && Double.compare(
                        strafe,
                        0.0D) == 0) {
            return;
        }

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
                        rotation.yaw());
        final double sin =
                Math.sin(
                        yawRadians);
        final double cos =
                Math.cos(
                        yawRadians);
        final double configuredSpeed =
                speed.get().doubleValue();

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
    }

    synchronized boolean active() {
        return enabled;
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
