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

    private final Setting<Integer> speedPercent =
            new Setting<Integer>(
                    SPEED_SETTING_ID,
                    100,
                    value -> value >= 10
                            && value <= 300,
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

    synchronized void apply(
            final Minecraft189TimerSpeedControl timer) {
        if (timer == null) {
            return;
        }

        final float target =
                enabled
                        ? speedPercent.get().intValue()
                        / 100.0F
                        : 1.0F;
        if (Math.abs(
                timer.customMcTimerSpeed()
                        - target) > 0.000001F) {
            timer.customMcSetTimerSpeed(
                    target);
        }
    }

    synchronized boolean active() {
        return enabled;
    }
}
