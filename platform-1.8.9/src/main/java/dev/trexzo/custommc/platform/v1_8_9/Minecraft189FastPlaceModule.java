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
    public static final String AIRBORNE_OVERRIDE_SETTING_ID =
            "player.fastPlace.airborneOverride";
    public static final String AIRBORNE_DELAY_SETTING_ID =
            "player.fastPlace.airborneDelay";
    public static final String REQUIRE_USE_HELD_SETTING_ID =
            "player.fastPlace.requireUseHeld";
    public static final String PAUSE_WHILE_SPRINTING_SETTING_ID =
            "player.fastPlace.pauseWhileSprinting";
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

    private final Setting<Boolean> pauseWhileSprinting =
            new Setting<Boolean>(
                    PAUSE_WHILE_SPRINTING_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);

    private final Setting<Boolean> airborneOverride =
            new Setting<Boolean>(
                    AIRBORNE_OVERRIDE_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Integer> airborneDelay =
            new Setting<Integer>(
                    AIRBORNE_DELAY_SETTING_ID, 0,
                    value -> value != null && value >= 0 && value <= 4,
                    SettingCodecs.INTEGER);
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

    public Setting<Boolean> pauseWhileSprintingSetting() {
        return pauseWhileSprinting;
    }

    public Setting<Boolean> airborneOverrideSetting() {
        return airborneOverride;
    }

    public Setting<Integer> airborneDelaySetting() {
        return airborneDelay;
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
                        || movement.sneaking()))
                || (pauseWhileSprinting.get().booleanValue()
                        && (movement == null || !movement.available()
                        || movement.sprinting()))) {
            return currentDelay;
        }
        // Optional airborne delay is selected only from real available
        // mapped movement state; missing state uses the normal delay.
        final int configured =
                airborneOverride.get().booleanValue()
                        && movement != null
                        && movement.available()
                        && !movement.onGround()
                        ? airborneDelay.get().intValue()
                        : delayTicks.get().intValue();
        return currentDelay > configured
                ? configured
                : currentDelay;
    }

    synchronized boolean active() {
        return enabled;
    }
}
