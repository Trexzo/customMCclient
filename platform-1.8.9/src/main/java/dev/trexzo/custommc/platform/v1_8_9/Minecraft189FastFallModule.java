package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

public final class Minecraft189FastFallModule
        implements Module {
    public static final String ID =
            "movement.fastFall";
    public static final String FALL_SPEED_SETTING_ID =
            "movement.fastFall.fallSpeed";
    public static final String PROGRESSIVE_SETTING_ID =
            "movement.fastFall.progressive";
    public static final String RAMP_STEP_SETTING_ID =
            "movement.fastFall.rampStep";
    public static final double DEFAULT_FALL_SPEED =
            0.30D;
    public static final double MINIMUM_FALL_SPEED =
            0.05D;
    public static final double MAXIMUM_FALL_SPEED =
            1.00D;
    public static final double DEFAULT_RAMP_STEP = 0.05D;
    public static final double MINIMUM_RAMP_STEP = 0.01D;
    public static final double MAXIMUM_RAMP_STEP = 0.50D;

    private final Setting<Double> fallSpeed =
            new Setting<Double>(
                    FALL_SPEED_SETTING_ID,
                    DEFAULT_FALL_SPEED,
                    Minecraft189FastFallModule::validFallSpeed,
                    SettingCodecs.DOUBLE);
    private final Setting<Boolean> progressive =
            new Setting<Boolean>(
                    PROGRESSIVE_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private final Setting<Double> rampStep =
            new Setting<Double>(
                    RAMP_STEP_SETTING_ID,
                    DEFAULT_RAMP_STEP,
                    value -> value != null
                            && Double.isFinite(value.doubleValue())
                            && value.doubleValue() >= MINIMUM_RAMP_STEP
                            && value.doubleValue() <= MAXIMUM_RAMP_STEP,
                    SettingCodecs.DOUBLE);
    private boolean enabled;

    @Override
    public String id() {
        return ID;
    }

    public Setting<Double> fallSpeedSetting() {
        return fallSpeed;
    }

    public Setting<Boolean> progressiveSetting() {
        return progressive;
    }

    public Setting<Double> rampStepSetting() {
        return rampStep;
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
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean suspended) {
        if (!enabled
                || suspended
                || player == null
                || movement == null
                || !movement.available()
                || movement.onGround()) {
            return;
        }

        final double currentMotionY =
                player.customMcMotionY();
        // Unknown vertical motion must not produce a synthetic write.
        // Preserve the existing no-write behavior for upward and stationary
        // motion, and for descent already at or faster than the target.
        if (!Double.isFinite(currentMotionY) || currentMotionY >= 0.0D) {
            return;
        }

        final double targetMotionY =
                -fallSpeed.get().doubleValue();
        if (currentMotionY > targetMotionY) {
            final double nextMotionY = progressive.get().booleanValue()
                    ? Math.max(targetMotionY,
                            currentMotionY - rampStep.get().doubleValue())
                    : targetMotionY;
            if (nextMotionY < currentMotionY) {
                player.customMcSetMotionY(nextMotionY);
            }
        }
    }

    synchronized boolean active() {
        return enabled;
    }

    private static boolean validFallSpeed(
            final Double value) {
        return value != null
                && !Double.isNaN(value.doubleValue())
                && !Double.isInfinite(value.doubleValue())
                && value.doubleValue() >= MINIMUM_FALL_SPEED
                && value.doubleValue() <= MAXIMUM_FALL_SPEED;
    }
}
