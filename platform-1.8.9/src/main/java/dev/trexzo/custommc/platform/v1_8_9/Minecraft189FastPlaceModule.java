package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

public final class Minecraft189FastPlaceModule
        implements Module {
    public static final String ID =
            "player.fastPlace";
    public static final String DELAY_SETTING_ID =
            "player.fastPlace.delay";

    private final Setting<Integer> delayTicks =
            new Setting<Integer>(
                    DELAY_SETTING_ID,
                    0,
                    value -> value >= 0
                            && value <= 4,
                    SettingCodecs.INTEGER);

    private boolean enabled;

    @Override
    public String id() {
        return ID;
    }

    public Setting<Integer> delayTicksSetting() {
        return delayTicks;
    }

    @Override
    public synchronized void onEnable() {
        enabled = true;
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
    }

    synchronized int apply(
            final int currentDelay) {
        if (!enabled) {
            return currentDelay;
        }
        final int configured =
                delayTicks.get().intValue();
        return currentDelay > configured
                ? configured
                : currentDelay;
    }

    synchronized boolean active() {
        return enabled;
    }
}
