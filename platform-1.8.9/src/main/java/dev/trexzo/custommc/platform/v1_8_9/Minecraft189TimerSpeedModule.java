package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

public final class Minecraft189TimerSpeedModule
        implements Module {
    public static final String ID =
            "player.timer";
    public static final String SPEED_SETTING_ID =
            "player.timer.speedPercent";
    public static final String AIRBORNE_OVERRIDE_SETTING_ID =
            "player.timer.airborneOverride";
    public static final String AIRBORNE_SPEED_SETTING_ID =
            "player.timer.airborneSpeedPercent";
    public static final String SMOOTH_TRANSITION_SETTING_ID =
            "player.timer.smoothTransition";
    public static final String TRANSITION_STEP_SETTING_ID =
            "player.timer.transitionStepPercent";
    public static final String PAUSE_WHILE_SNEAKING_SETTING_ID =
            "player.timer.pauseWhileSneaking";
    public static final int DEFAULT_TRANSITION_STEP_PERCENT = 25;
    public static final int MINIMUM_TRANSITION_STEP_PERCENT = 5;
    public static final int MAXIMUM_TRANSITION_STEP_PERCENT = 100;
    public static final int MINIMUM_SPEED_PERCENT = 10;
    public static final int MAXIMUM_SPEED_PERCENT = 300;

    private final Setting<Integer> speedPercent =
            new Setting<Integer>(
                    SPEED_SETTING_ID,
                    100,
                    value -> value != null
                            && value >= MINIMUM_SPEED_PERCENT
                            && value <= MAXIMUM_SPEED_PERCENT,
                    SettingCodecs.INTEGER);

    private final Setting<Boolean> airborneOverride =
            new Setting<Boolean>(
                    AIRBORNE_OVERRIDE_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private final Setting<Integer> airborneSpeedPercent =
            new Setting<Integer>(
                    AIRBORNE_SPEED_SETTING_ID,
                    100,
                    value -> value != null
                            && value >= MINIMUM_SPEED_PERCENT
                            && value <= MAXIMUM_SPEED_PERCENT,
                    SettingCodecs.INTEGER);

    private final Setting<Boolean> smoothTransition =
            new Setting<Boolean>(
                    SMOOTH_TRANSITION_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private final Setting<Integer> transitionStepPercent =
            new Setting<Integer>(
                    TRANSITION_STEP_SETTING_ID,
                    DEFAULT_TRANSITION_STEP_PERCENT,
                    value -> value != null
                            && value >= MINIMUM_TRANSITION_STEP_PERCENT
                            && value <= MAXIMUM_TRANSITION_STEP_PERCENT,
                    SettingCodecs.INTEGER);
    private final Setting<Boolean> pauseWhileSneaking = new Setting<Boolean>(
            PAUSE_WHILE_SNEAKING_SETTING_ID, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private boolean enabled;

    @Override
    public String id() {
        return ID;
    }

    public Setting<Integer> speedPercentSetting() {
        return speedPercent;
    }

    public Setting<Boolean> airborneOverrideSetting() {
        return airborneOverride;
    }

    public Setting<Integer> airborneSpeedPercentSetting() {
        return airborneSpeedPercent;
    }

    public Setting<Boolean> smoothTransitionSetting() {
        return smoothTransition;
    }

    public Setting<Integer> transitionStepPercentSetting() {
        return transitionStepPercent;
    }

    public Setting<Boolean> pauseWhileSneakingSetting() {
        return pauseWhileSneaking;
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
            final Minecraft189TimerSpeedControl timer) {
        apply(timer, null);
    }

    synchronized void apply(
            final Minecraft189TimerSpeedControl timer,
            final Minecraft189PlayerMovementState.Snapshot movement) {
        if (timer == null) {
            return;
        }

        final boolean airborne = enabled
                && airborneOverride.get().booleanValue()
                && movement != null
                && movement.available()
                && !movement.onGround();
        final int configuredPercent = airborne
                ? airborneSpeedPercent.get().intValue()
                : speedPercent.get().intValue();
        // An opt-in sneak pause is a hard safety gate, not a gradual
        // smoothing target. No movement authority also restores vanilla
        // instead of inheriting a previously accelerated clock.
        final boolean paused = enabled && pauseWhileSneaking.get().booleanValue()
                && (movement == null || !movement.available()
                        || movement.sneaking());
        final float target = enabled && !paused
                ? configuredPercent / 100.0F : 1.0F;
        final float current = timer.customMcTimerSpeed();
        // Disabling Timer must always restore vanilla immediately, even
        // when smoothing was enabled and an earlier transition was active.
        // Nonfinite mapped current state is repaired directly to target.
        final float next = enabled && !paused
                && smoothTransition.get().booleanValue()
                ? stepToward(current, target,
                        transitionStepPercent.get().intValue())
                : target;
        if (!Float.isFinite(current)
                || Math.abs(current - next) > 0.000001F) {
            timer.customMcSetTimerSpeed(next);
        }
    }

    static float stepToward(
            final float current,
            final float target,
            final int stepPercent) {
        if (!Float.isFinite(current) || !Float.isFinite(target)
                || stepPercent < MINIMUM_TRANSITION_STEP_PERCENT
                || stepPercent > MAXIMUM_TRANSITION_STEP_PERCENT) {
            return target;
        }
        final float maxDelta = stepPercent / 100.0F;
        final float difference = target - current;
        if (!Float.isFinite(difference)
                || Math.abs(difference) <= maxDelta + 0.000001F) {
            return target;
        }
        return current + Math.copySign(maxDelta, difference);
    }

    synchronized boolean active() {
        return enabled;
    }
}
