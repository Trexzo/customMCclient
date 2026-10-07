package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

public final class Minecraft189NoHitDelayModule
        implements Module {
    public static final String ID =
            "combat.noHitDelay";
    public static final String DELAY_SETTING_ID =
            "combat.noHitDelay.delay";
    public static final int DEFAULT_DELAY = 0;
    public static final int MINIMUM_DELAY = 0;
    public static final int MAXIMUM_DELAY = 10;

    private final Setting<Integer> delay =
            new Setting<Integer>(
                    DELAY_SETTING_ID,
                    DEFAULT_DELAY,
                    value -> value != null
                            && value >= MINIMUM_DELAY
                            && value <= MAXIMUM_DELAY,
                    SettingCodecs.INTEGER);

    private boolean enabled;

    @Override
    public String id() {
        return ID;
    }

    public Setting<Integer> delaySetting() {
        return delay;
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
            final int currentCounter) {
        if (!enabled) {
            return currentCounter;
        }
        final int configured =
                delay.get().intValue();
        return currentCounter > configured
                ? configured
                : currentCounter;
    }

    synchronized boolean active() {
        return enabled;
    }
}
