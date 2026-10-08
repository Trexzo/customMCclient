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
    public static final String GROUND_ONLY_SETTING_ID =
            "combat.noHitDelay.groundOnly";
    public static final String PAUSE_WHILE_SNEAKING_SETTING_ID =
            "combat.noHitDelay.pauseWhileSneaking";
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

    private final Setting<Boolean> groundOnly =
            new Setting<Boolean>(
                    GROUND_ONLY_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
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

    public Setting<Integer> delaySetting() {
        return delay;
    }

    public Setting<Boolean> groundOnlySetting() {
        return groundOnly;
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
            final int currentCounter) {
        return apply(currentCounter, null);
    }

    synchronized int apply(
            final int currentCounter,
            final Minecraft189PlayerMovementState.Snapshot movement) {
        if (!enabled) {
            return currentCounter;
        }
        if (groundOnly.get().booleanValue()
                || pauseWhileSneaking.get().booleanValue()) {
            if (movement == null || !movement.available()) {
                // A configured movement gate requires actual mapped player
                // state. Never accelerate a counter using missing authority.
                return currentCounter;
            }
            if ((groundOnly.get().booleanValue() && !movement.onGround())
                    || (pauseWhileSneaking.get().booleanValue()
                            && movement.sneaking())) {
                return currentCounter;
            }
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
