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
    public static final String SPRINT_BOOST_SETTING_ID =
            "movement.flight.sprintBoost";
    public static final String SPRINT_MULTIPLIER_SETTING_ID =
            "movement.flight.sprintMultiplier";
    public static final String SMOOTH_VERTICAL_SETTING_ID =
            "movement.flight.smoothVertical";
    public static final String VERTICAL_STEP_SETTING_ID =
            "movement.flight.verticalStep";
    public static final String SMOOTH_HORIZONTAL_SETTING_ID =
            "movement.flight.smoothHorizontal";
    public static final String HORIZONTAL_STEP_SETTING_ID =
            "movement.flight.horizontalStep";
    public static final double DEFAULT_HORIZONTAL_STEP = 0.10D;
    public static final double MINIMUM_HORIZONTAL_STEP = 0.01D;
    public static final double MAXIMUM_HORIZONTAL_STEP = 1.00D;
    public static final double DEFAULT_VERTICAL_STEP = 0.10D;
    public static final double MINIMUM_VERTICAL_STEP = 0.01D;
    public static final double MAXIMUM_VERTICAL_STEP = 0.50D;
    public static final double DEFAULT_SPRINT_MULTIPLIER = 1.50D;
    public static final double MINIMUM_SPRINT_MULTIPLIER = 1.00D;
    public static final double MAXIMUM_SPRINT_MULTIPLIER = 3.00D;
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
    private final Setting<Boolean> sprintBoost =
            new Setting<Boolean>(
                    SPRINT_BOOST_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private final Setting<Double> sprintMultiplier =
            new Setting<Double>(
                    SPRINT_MULTIPLIER_SETTING_ID,
                    DEFAULT_SPRINT_MULTIPLIER,
                    Minecraft189FlightModule::validSprintMultiplier,
                    SettingCodecs.DOUBLE);
    private final Setting<Boolean> smoothVertical =
            new Setting<Boolean>(
                    SMOOTH_VERTICAL_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Double> verticalStep =
            new Setting<Double>(
                    VERTICAL_STEP_SETTING_ID, DEFAULT_VERTICAL_STEP,
                    value -> value != null
                            && Double.isFinite(value.doubleValue())
                            && value.doubleValue() >= MINIMUM_VERTICAL_STEP
                            && value.doubleValue() <= MAXIMUM_VERTICAL_STEP,
                    SettingCodecs.DOUBLE);
    private final Setting<Boolean> smoothHorizontal =
            new Setting<Boolean>(
                    SMOOTH_HORIZONTAL_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private final Setting<Double> horizontalStep =
            new Setting<Double>(
                    HORIZONTAL_STEP_SETTING_ID, DEFAULT_HORIZONTAL_STEP,
                    value -> value != null
                            && Double.isFinite(value.doubleValue())
                            && value.doubleValue() >= MINIMUM_HORIZONTAL_STEP
                            && value.doubleValue() <= MAXIMUM_HORIZONTAL_STEP,
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

    public Setting<Boolean> sprintBoostSetting() {
        return sprintBoost;
    }

    public Setting<Double> sprintMultiplierSetting() {
        return sprintMultiplier;
    }

    public Setting<Boolean> smoothVerticalSetting() {
        return smoothVertical;
    }

    public Setting<Double> verticalStepSetting() {
        return verticalStep;
    }

    public Setting<Boolean> smoothHorizontalSetting() {
        return smoothHorizontal;
    }

    public Setting<Double> horizontalStepSetting() {
        return horizontalStep;
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
        apply(player, rotation, null);
    }

    synchronized void apply(
            final Minecraft189PlayerMotionControl player,
            final Minecraft189PlayerRotationState.Snapshot rotation,
            final Minecraft189PlayerMovementState.Snapshot movement) {
        Objects.requireNonNull(rotation, "rotation");
        if (!enabled || player == null) {
            return;
        }

        applyVertical(player);
        if (rotation.available()) {
            applyHorizontal(player, rotation.yaw(), movement);
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

        final double currentMotionY = player.customMcMotionY();
        double nextMotionY = targetMotionY;
        if (smoothVertical.get().booleanValue()) {
            // Fail closed when the mapped motion source is not finite.
            // No synthetic motion writes should be derived from NaN/Infinity.
            if (!Double.isFinite(currentMotionY)) {
                return;
            }
            final double delta = targetMotionY - currentMotionY;
            final double maxStep = verticalStep.get().doubleValue();
            if (Math.abs(delta) > maxStep) {
                nextMotionY = currentMotionY
                        + Math.copySign(maxStep, delta);
            }
        }
        if (Double.compare(currentMotionY, nextMotionY) != 0) {
            player.customMcSetMotionY(nextMotionY);
        }
    }

    private void applyHorizontal(
            final Minecraft189PlayerMotionControl player,
            final float yawDegrees,
            final Minecraft189PlayerMovementState.Snapshot movement) {
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
        // Sprint boost applies only to horizontal motion when the mapped
        // live player snapshot confirms sprinting. Unknown movement state
        // retains the certified normal Flight speed and vertical motion.
        final double multiplier =
                sprintBoost.get().booleanValue()
                        && movement != null
                        && movement.available()
                        && movement.sprinting()
                                ? sprintMultiplier.get().doubleValue()
                                : 1.0D;
        final double configuredHorizontalSpeed =
                horizontalSpeed.get().doubleValue() * multiplier;

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

        final double currentMotionX = player.customMcMotionX();
        final double currentMotionZ = player.customMcMotionZ();
        double nextMotionX = targetMotionX;
        double nextMotionZ = targetMotionZ;
        if (smoothHorizontal.get().booleanValue()) {
            // Failed authority cannot produce a synthetic horizontal write.
            if (!Double.isFinite(currentMotionX)
                    || !Double.isFinite(currentMotionZ)) {
                return;
            }
            final double dx = targetMotionX - currentMotionX;
            final double dz = targetMotionZ - currentMotionZ;
            final double magnitude = Math.hypot(dx, dz);
            if (!Double.isFinite(magnitude)) {
                return;
            }
            final double step = horizontalStep.get().doubleValue();
            if (magnitude > step) {
                final double ratio = step / magnitude;
                nextMotionX = cleanZero(currentMotionX + dx * ratio);
                nextMotionZ = cleanZero(currentMotionZ + dz * ratio);
            }
        }
        if (Double.compare(currentMotionX, nextMotionX) != 0) {
            player.customMcSetMotionX(nextMotionX);
        }
        if (Double.compare(currentMotionZ, nextMotionZ) != 0) {
            player.customMcSetMotionZ(nextMotionZ);
        }
    }

    private static boolean validSprintMultiplier(final Double value) {
        return value != null
                && Double.isFinite(value.doubleValue())
                && value.doubleValue() >= MINIMUM_SPRINT_MULTIPLIER
                && value.doubleValue() <= MAXIMUM_SPRINT_MULTIPLIER;
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
