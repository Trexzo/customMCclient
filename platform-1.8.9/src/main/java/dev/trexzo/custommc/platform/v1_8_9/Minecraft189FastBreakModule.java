package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

public final class Minecraft189FastBreakModule
        implements Module {
    public static final String ID =
            "player.fastBreak";
    public static final String DELAY_SETTING_ID =
            "player.fastBreak.delay";
    public static final String REQUIRE_ATTACK_HELD_SETTING_ID =
            "player.fastBreak.requireAttackHeld";
    public static final String PAUSE_WHILE_SNEAKING_SETTING_ID =
            "player.fastBreak.pauseWhileSneaking";
    public static final int DEFAULT_DELAY = 0;
    public static final int MINIMUM_DELAY = 0;
    public static final int MAXIMUM_DELAY = 5;

    private final Setting<Integer> delay =
            new Setting<Integer>(
                    DELAY_SETTING_ID,
                    DEFAULT_DELAY,
                    value -> value != null
                            && value >= MINIMUM_DELAY
                            && value <= MAXIMUM_DELAY,
                    SettingCodecs.INTEGER);

    private final Setting<Boolean> requireAttackHeld =
            new Setting<Boolean>(
                    REQUIRE_ATTACK_HELD_SETTING_ID,
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

    public Setting<Boolean> requireAttackHeldSetting() {
        return requireAttackHeld;
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

    synchronized void apply(
            final Minecraft189BlockHitDelayControl controller) {
        apply(controller, false, null);
    }

    synchronized void apply(
            final Minecraft189BlockHitDelayControl controller,
            final boolean attackHeld,
            final Minecraft189PlayerMovementState.Snapshot movement) {
        if (!enabled
                || controller == null
                || (requireAttackHeld.get().booleanValue()
                        && !attackHeld)
                || (pauseWhileSneaking.get().booleanValue()
                        && (movement == null
                        || !movement.available()
                        || movement.sneaking()))) {
            return;
        }
        controller.customMcSetBlockHitDelay(
                delay.get().intValue());
    }

    synchronized boolean active() {
        return enabled;
    }
}
