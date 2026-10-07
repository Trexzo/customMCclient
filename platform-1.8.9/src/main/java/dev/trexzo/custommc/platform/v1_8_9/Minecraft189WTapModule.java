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
    public static final String RESET_TICKS_SETTING_ID =
            "combat.wTap.resetTicks";
    public static final String REQUIRE_FORWARD_SETTING_ID =
            "combat.wTap.requireForward";
    public static final int DEFAULT_COOLDOWN_TICKS =
            0;
    public static final int MINIMUM_COOLDOWN_TICKS =
            0;
    public static final int MAXIMUM_COOLDOWN_TICKS =
            20;
    public static final int DEFAULT_RESET_TICKS =
            1;
    public static final int MINIMUM_RESET_TICKS =
            1;
    public static final int MAXIMUM_RESET_TICKS =
            5;

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
    private final Setting<Integer> resetTicks =
            new Setting<Integer>(
                    RESET_TICKS_SETTING_ID,
                    DEFAULT_RESET_TICKS,
                    value -> value != null
                            && value >= MINIMUM_RESET_TICKS
                            && value <= MAXIMUM_RESET_TICKS,
                    SettingCodecs.INTEGER);
    private final Setting<Boolean> requireForward =
            new Setting<Boolean>(
                    REQUIRE_FORWARD_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);

    private boolean enabled;
    private boolean previousLeftButtonHeld;
    private int cooldownRemaining;
    private int resetTicksRemaining;

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

    public Setting<Integer> resetTicksSetting() {
        return resetTicks;
    }

    public Setting<Boolean> requireForwardSetting() {
        return requireForward;
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
            final boolean leftButtonHeld,
            final boolean forwardHeld) {
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

        if (resetTicksRemaining > 0) {
            resetTicksRemaining--;
            previousLeftButtonHeld =
                    leftButtonHeld;
            player.customMcSetSprinting(
                    false);
            return true;
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
                || (requireForward.get().booleanValue()
                        && !forwardHeld)
                || !movement.sprinting()) {
            return false;
        }

        player.customMcSetSprinting(
                false);
        cooldownRemaining =
                cooldownTicks.get().intValue();
        resetTicksRemaining =
                resetTicks.get().intValue() - 1;
        return true;
    }

    synchronized boolean active() {
        return enabled;
    }

    private void resetState() {
        previousLeftButtonHeld = false;
        cooldownRemaining = 0;
        resetTicksRemaining = 0;
    }
}
