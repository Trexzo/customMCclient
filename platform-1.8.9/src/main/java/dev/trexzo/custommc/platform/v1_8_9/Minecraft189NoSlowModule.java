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

    private boolean enabled;

    @Override
    public String id() {
        return ID;
    }

    public Setting<Integer> speedPercentSetting() {
        return speedPercent;
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
        if (!enabled) {
            return slowedValue;
        }
        return slowedValue
                * (speedPercent.get().intValue() / 20.0F);
    }

    synchronized boolean active() {
        return enabled;
    }
}
