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
    public static final String REQUIRE_USE_HELD_SETTING_ID =
            "player.fastPlace.requireUseHeld";
    public static final String PAUSE_WHILE_SNEAKING_SETTING_ID =
            "player.fastPlace.pauseWhileSneaking";

    private final Setting<Integer> delayTicks =
            new Setting<Integer>(
                    DELAY_SETTING_ID,
                    0,
                    value -> value >= 0
                            && value <= 4,
                    SettingCodecs.INTEGER);

    private final Setting<Boolean> requireUseHeld =
            new Setting<Boolean>(
                    REQUIRE_USE_HELD_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> pauseWhileSneaking =
            new Setting<Boolean>(
                    PAUSE_WHILE_SNEAKING_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);

    private boolean enabled;

    @Override
    public String id() {
        return ID;
    }

    public Setting<Integer> delayTicksSetting() {
        return delayTicks;
    }

    public Setting<Boolean> requireUseHeldSetting() {
        return requireUseHeld;
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

    synchronized int apply(
            final int currentDelay) {
        return apply(currentDelay, false, null);
    }

    synchronized int apply(
            final int currentDelay,
            final boolean useHeld,
            final Minecraft189PlayerMovementState.Snapshot movement) {
        if (!enabled
                || (requireUseHeld.get().booleanValue() && !useHeld)
                || (pauseWhileSneaking.get().booleanValue()
                        && (movement == null || !movement.available()
                        || movement.sneaking()))) {
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
