package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

public final class Minecraft189GlideModule
        implements Module {
    public static final String ID =
            "movement.glide";
    public static final String FALL_SPEED_SETTING_ID =
            "movement.glide.fallSpeed";
    public static final String PROGRESSIVE_SETTING_ID =
            "movement.glide.progressiveDeceleration";
    public static final String DECELERATION_STEP_SETTING_ID =
            "movement.glide.decelerationStep";
    public static final double DEFAULT_DECELERATION_STEP = 0.10D;
    public static final double MINIMUM_DECELERATION_STEP = 0.01D;
    public static final double MAXIMUM_DECELERATION_STEP = 0.50D;
    public static final String REQUIRE_SNEAKING_SETTING_ID =
            "movement.glide.requireSneaking";
    public static final String ACTIVATION_DELAY_SETTING_ID =
            "movement.glide.activationDelayTicks";
    public static final int DEFAULT_ACTIVATION_DELAY_TICKS = 0;
    public static final int MAXIMUM_ACTIVATION_DELAY_TICKS = 10;
    public static final double DEFAULT_FALL_SPEED =
            0.08D;
    public static final double MINIMUM_FALL_SPEED =
            0.01D;
    public static final double MAXIMUM_FALL_SPEED =
            0.50D;

    private final Setting<Double> fallSpeed =
            new Setting<Double>(
                    FALL_SPEED_SETTING_ID,
                    DEFAULT_FALL_SPEED,
                    Minecraft189GlideModule::validFallSpeed,
                    SettingCodecs.DOUBLE);
    private final Setting<Boolean> requireSneaking =
            new Setting<Boolean>(
                    REQUIRE_SNEAKING_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private final Setting<Boolean> progressiveDeceleration =
            new Setting<Boolean>(
                    PROGRESSIVE_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Double> decelerationStep =
            new Setting<Double>(
                    DECELERATION_STEP_SETTING_ID,
                    DEFAULT_DECELERATION_STEP,
                    value -> value != null && Double.isFinite(value.doubleValue())
                            && value >= MINIMUM_DECELERATION_STEP
                            && value <= MAXIMUM_DECELERATION_STEP,
                    SettingCodecs.DOUBLE);
    private final Setting<Integer> activationDelayTicks = new Setting<Integer>(
            ACTIVATION_DELAY_SETTING_ID, DEFAULT_ACTIVATION_DELAY_TICKS,
            value -> value != null && value >= 0
                    && value <= MAXIMUM_ACTIVATION_DELAY_TICKS,
            SettingCodecs.INTEGER);
    private boolean enabled;
    private int eligibleDescentCallbacks;
    private int observedActivationDelay;

    @Override
    public String id() {
        return ID;
    }

    public Setting<Double> fallSpeedSetting() {
        return fallSpeed;
    }

    public Setting<Boolean> requireSneakingSetting() {
        return requireSneaking;
    }

    public Setting<Boolean> progressiveDecelerationSetting() {
        return progressiveDeceleration;
    }

    public Setting<Double> decelerationStepSetting() {
        return decelerationStep;
    }

    public Setting<Integer> activationDelayTicksSetting() {
        return activationDelayTicks;
    }

    @Override
    public synchronized void onEnable() {
        enabled = true;
        resetActivationDelay();
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
        resetActivationDelay();
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
                || movement.onGround()
                || (requireSneaking.get().booleanValue()
                        && !movement.sneaking())) {
            resetActivationDelay();
            return;
        }

        final double targetMotionY =
                -fallSpeed.get().doubleValue();
        final double currentMotionY = player.customMcMotionY();
        if (!Double.isFinite(currentMotionY)
                || currentMotionY >= targetMotionY) {
            // No eligible descent (or invalid mapped motion): accumulated
            // waiting is never carried into a later drop.
            resetActivationDelay();
            return;
        }

        final int delay = activationDelayTicks.get().intValue();
        if (delay != observedActivationDelay) {
            eligibleDescentCallbacks = 0;
            observedActivationDelay = delay;
        }
        if (eligibleDescentCallbacks < delay) {
            eligibleDescentCallbacks++;
            return;
        }

        {
            // An optional per-callback deceleration step avoids a
            // sudden jump from fast descent to the Glide cap. The next
            // value always derives from current mapped motion, never
            // from accumulated credit or a hidden scheduler.
            final double nextMotionY = progressiveDeceleration.get().booleanValue()
                    ? Math.min(targetMotionY,
                            currentMotionY + decelerationStep.get().doubleValue())
                    : targetMotionY;
            if (nextMotionY > currentMotionY) {
                player.customMcSetMotionY(nextMotionY);
            }
        }
    }

    synchronized boolean active() {
        return enabled;
    }

    private void resetActivationDelay() {
        eligibleDescentCallbacks = 0;
        observedActivationDelay = activationDelayTicks.get().intValue();
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
