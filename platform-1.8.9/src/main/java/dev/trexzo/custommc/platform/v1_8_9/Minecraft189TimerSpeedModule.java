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
        final float target = enabled ? configuredPercent / 100.0F : 1.0F;
        final float current = timer.customMcTimerSpeed();
        if (!Float.isFinite(current)
                || Math.abs(current - target) > 0.000001F) {
            timer.customMcSetTimerSpeed(target);
        }
    }

    synchronized boolean active() {
        return enabled;
    }
}
