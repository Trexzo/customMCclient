package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

public final class Minecraft189WTapModule
        implements Module {
    public static final String ID =
            "combat.wTap";
    public static final String REQUIRE_GROUND_SETTING_ID =
            "combat.wTap.requireGround";
    public static final String COOLDOWN_TICKS_SETTING_ID =
            "combat.wTap.cooldownTicks";
    public static final int DEFAULT_COOLDOWN_TICKS =
            0;
    public static final int MINIMUM_COOLDOWN_TICKS =
            0;
    public static final int MAXIMUM_COOLDOWN_TICKS =
            20;

    private final Setting<Boolean> requireGround =
            new Setting<Boolean>(
                    REQUIRE_GROUND_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private final Setting<Integer> cooldownTicks =
            new Setting<Integer>(
                    COOLDOWN_TICKS_SETTING_ID,
                    DEFAULT_COOLDOWN_TICKS,
                    value -> value != null
                            && value >= MINIMUM_COOLDOWN_TICKS
                            && value <= MAXIMUM_COOLDOWN_TICKS,
                    SettingCodecs.INTEGER);

    private boolean enabled;
    private boolean previousLeftButtonHeld;
    private int cooldownRemaining;

    @Override
    public String id() {
        return ID;
    }

    public Setting<Boolean> requireGroundSetting() {
        return requireGround;
    }

    public Setting<Integer> cooldownTicksSetting() {
        return cooldownTicks;
    }

    @Override
    public synchronized void onEnable() {
        enabled = true;
        resetState();
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
        resetState();
    }

    synchronized boolean apply(
            final Minecraft189PlayerSprintControl player,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean leftButtonHeld) {
        if (!enabled
                || player == null
                || movement == null
                || !movement.available()) {
            resetState();
            return false;
        }

        final boolean cooldownActive =
                cooldownRemaining > 0;
        if (cooldownActive) {
            cooldownRemaining--;
        }

        if (!leftButtonHeld) {
            previousLeftButtonHeld = false;
            return false;
        }
        if (previousLeftButtonHeld) {
            return false;
        }
        previousLeftButtonHeld = true;

        if (cooldownActive
                || (requireGround.get().booleanValue()
                        && !movement.onGround())
                || !movement.sprinting()) {
            return false;
        }

        player.customMcSetSprinting(
                false);
        cooldownRemaining =
                cooldownTicks.get().intValue();
        return true;
    }

    synchronized boolean active() {
        return enabled;
    }

    private void resetState() {
        previousLeftButtonHeld = false;
        cooldownRemaining = 0;
    }
}
