package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

public final class Minecraft189NoGravityModule
        implements Module {
    public static final String ID =
            "movement.noGravity";
    public static final String SMOOTH_LIFT_SETTING_ID =
            "movement.noGravity.smoothLift";
    public static final String LIFT_STEP_SETTING_ID =
            "movement.noGravity.liftStep";
    public static final double DEFAULT_LIFT_STEP = 0.10D;
    public static final double MINIMUM_LIFT_STEP = 0.01D;
    public static final double MAXIMUM_LIFT_STEP = 0.50D;
    public static final String LIFT_SPEED_SETTING_ID =
            "movement.noGravity.liftSpeed";
    public static final double DEFAULT_LIFT_SPEED = 0.0D;
    public static final double MAXIMUM_LIFT_SPEED = 0.30D;

    private final Setting<Double> liftSpeed =
            new Setting<Double>(
                    LIFT_SPEED_SETTING_ID, DEFAULT_LIFT_SPEED,
                    value -> value != null && Double.isFinite(value.doubleValue())
                            && value >= 0.0D && value <= MAXIMUM_LIFT_SPEED,
                    SettingCodecs.DOUBLE);
    private final Setting<Boolean> smoothLift =
            new Setting<Boolean>(
                    SMOOTH_LIFT_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Double> liftStep =
            new Setting<Double>(
                    LIFT_STEP_SETTING_ID, DEFAULT_LIFT_STEP,
                    value -> value != null && Double.isFinite(value.doubleValue())
                            && value >= MINIMUM_LIFT_STEP
                            && value <= MAXIMUM_LIFT_STEP,
                    SettingCodecs.DOUBLE);
    private boolean enabled;

    public Setting<Double> liftSpeedSetting() {
        return liftSpeed;
    }

    public Setting<Boolean> smoothLiftSetting() {
        return smoothLift;
    }

    public Setting<Double> liftStepSetting() {
        return liftStep;
    }

    @Override
    public String id() {
        return ID;
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

        final double target = liftSpeed.get().doubleValue();
        // The original zero-gravity behavior is exactly target=0.
        // A positive setting is an optional minimum upward speed:
        // never slow an existing faster upward jump.
        final double current = player.customMcMotionY();
        if (current < target) {
            // Optional smoothing reduces the gap at most one step per
            // eligible callback. Every value derives from current real
            // vertical motion: no hidden accumulator or catch-up credit.
            // Invalid mapped speed is safely normalized to finite target.
            final double next = smoothLift.get().booleanValue()
                    && Double.isFinite(current)
                            ? Math.min(target, current + liftStep.get().doubleValue())
                            : target;
            if (next > current) {
                player.customMcSetMotionY(next);
            }
        }
    }

    synchronized boolean active() {
        return enabled;
    }
}
