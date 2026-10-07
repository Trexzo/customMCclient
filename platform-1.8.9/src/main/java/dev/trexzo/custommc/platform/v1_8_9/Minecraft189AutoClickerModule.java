package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

import java.util.concurrent.ThreadLocalRandom;

public final class Minecraft189AutoClickerModule
        implements Module {
    public static final String ID =
            "combat.autoClicker";
    public static final String MIN_CPS_SETTING_ID =
            "combat.autoClicker.minCps";
    public static final String MAX_CPS_SETTING_ID =
            "combat.autoClicker.maxCps";
    public static final String REQUIRE_HOLD_SETTING_ID =
            "combat.autoClicker.requireHold";

    private static final int TICKS_PER_SECOND = 20;

    private final Setting<Integer> minCps =
            new Setting<Integer>(
                    MIN_CPS_SETTING_ID,
                    8,
                    value -> value >= 1
                            && value <= TICKS_PER_SECOND,
                    SettingCodecs.INTEGER);
    private final Setting<Integer> maxCps =
            new Setting<Integer>(
                    MAX_CPS_SETTING_ID,
                    12,
                    value -> value >= 1
                            && value <= TICKS_PER_SECOND,
                    SettingCodecs.INTEGER);
    private final Setting<Boolean> requireHold =
            new Setting<Boolean>(
                    REQUIRE_HOLD_SETTING_ID,
                    Boolean.TRUE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);

    private boolean enabled;
    private int phaseCredit;
    private int targetCps;

    @Override
    public String id() {
        return ID;
    }

    public Setting<Integer> minCpsSetting() {
        return minCps;
    }

    public Setting<Integer> maxCpsSetting() {
        return maxCps;
    }

    public Setting<Boolean> requireHoldSetting() {
        return requireHold;
    }

    @Override
    public synchronized void onEnable() {
        enabled = true;
        resetSchedule();
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
        resetSchedule();
    }

    synchronized boolean shouldClick(
            final boolean leftButtonHeld) {
        if (!enabled
                || (requireHold.get().booleanValue()
                        && !leftButtonHeld)) {
            resetSchedule();
            return false;
        }

        if (targetCps <= 0) {
            targetCps = nextTargetCps();
        }

        phaseCredit += targetCps;
        if (phaseCredit < TICKS_PER_SECOND) {
            return false;
        }

        phaseCredit -= TICKS_PER_SECOND;
        targetCps = nextTargetCps();
        return true;
    }

    synchronized boolean active() {
        return enabled;
    }

    private int nextTargetCps() {
        final int first =
                minCps.get().intValue();
        final int second =
                maxCps.get().intValue();
        final int low =
                Math.min(
                        first,
                        second);
        final int high =
                Math.max(
                        first,
                        second);
        if (low == high) {
            return low;
        }
        return ThreadLocalRandom.current()
                .nextInt(
                        low,
                        high + 1);
    }

    private void resetSchedule() {
        phaseCredit = 0;
        targetCps = 0;
    }
}
