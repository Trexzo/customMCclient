package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

public final class Minecraft189NoSlowModule
        implements Module {
    public static final String ID =
            "movement.noSlow";
    public static final String SPEED_PERCENT_SETTING_ID =
            "movement.noSlow.speedPercent";
    public static final String AIRBORNE_OVERRIDE_SETTING_ID =
            "movement.noSlow.airborneOverride";
    public static final String AIRBORNE_SPEED_SETTING_ID =
            "movement.noSlow.airborneSpeedPercent";
    public static final String PAUSE_WHILE_SNEAKING_SETTING_ID =
            "movement.noSlow.pauseWhileSneaking";
    public static final int DEFAULT_SPEED_PERCENT = 100;
    public static final int MINIMUM_SPEED_PERCENT = 20;
    public static final int MAXIMUM_SPEED_PERCENT = 100;

    private final Setting<Integer> speedPercent =
            new Setting<Integer>(
                    SPEED_PERCENT_SETTING_ID,
                    DEFAULT_SPEED_PERCENT,
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
                    DEFAULT_SPEED_PERCENT,
                    value -> value != null
                            && value >= MINIMUM_SPEED_PERCENT
                            && value <= MAXIMUM_SPEED_PERCENT,
                    SettingCodecs.INTEGER);
    private final Setting<Boolean> pauseWhileSneaking =
            new Setting<Boolean>(
                    PAUSE_WHILE_SNEAKING_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
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

    synchronized float adjustSlowedMovement(
            final float slowedValue) {
        return adjustSlowedMovement(slowedValue, null);
    }

    synchronized float adjustSlowedMovement(
            final float slowedValue,
            final Minecraft189PlayerMovementState.Snapshot movement) {
        if (!enabled) {
            return slowedValue;
        }
        // Never use stale or missing movement evidence to bypass an
        // explicitly enabled sneaking guard.
        if (pauseWhileSneaking.get().booleanValue()
                && (movement == null
                    || !movement.available()
                    || movement.sneaking())) {
            return slowedValue;
        }
        // The legacy hook and unknown movement authority always use
        // the original percentage. Only confirmed airborne state may
        // select the opt-in independent air movement factor.
        final boolean airborne =
                airborneOverride.get().booleanValue()
                && movement != null
                && movement.available()
                && !movement.onGround();
        final int percent = airborne
                ? airborneSpeedPercent.get().intValue()
                : speedPercent.get().intValue();
        return slowedValue * (percent / 20.0F);
    }

    synchronized boolean active() {
        return enabled;
    }
}
