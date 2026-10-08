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
    public static final String PAUSE_WHILE_SNEAKING_SETTING_ID =
            "combat.wTap.pauseWhileSneaking";
    public static final String REQUIRE_NEARBY_PLAYER_SETTING_ID =
            "combat.wTap.requireNearbyPlayer";
    public static final String MAX_PLAYER_DISTANCE_SETTING_ID =
            "combat.wTap.maxPlayerDistance";
    public static final double DEFAULT_MAX_PLAYER_DISTANCE = 4.0D;
    public static final double MINIMUM_MAX_PLAYER_DISTANCE = 0.5D;
    public static final double MAXIMUM_MAX_PLAYER_DISTANCE = 16.0D;
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

    private final Setting<Boolean> pauseWhileSneaking =
            new Setting<Boolean>(
                    PAUSE_WHILE_SNEAKING_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);

    private final Setting<Boolean> requireNearbyPlayer =
            new Setting<Boolean>(
                    REQUIRE_NEARBY_PLAYER_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private final Setting<Double> maxPlayerDistance =
            new Setting<Double>(
                    MAX_PLAYER_DISTANCE_SETTING_ID,
                    DEFAULT_MAX_PLAYER_DISTANCE,
                    value -> value != null && Double.isFinite(value.doubleValue())
                            && value >= MINIMUM_MAX_PLAYER_DISTANCE
                            && value <= MAXIMUM_MAX_PLAYER_DISTANCE,
                    SettingCodecs.DOUBLE);

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

    public Setting<Boolean> pauseWhileSneakingSetting() {
        return pauseWhileSneaking;
    }

    public Setting<Boolean> requireNearbyPlayerSetting() {
        return requireNearbyPlayer;
    }

    public Setting<Double> maxPlayerDistanceSetting() {
        return maxPlayerDistance;
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
        return apply(player, movement, leftButtonHeld, forwardHeld, null);
    }

    synchronized boolean apply(
            final Minecraft189PlayerSprintControl player,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean leftButtonHeld,
            final boolean forwardHeld,
            final Minecraft189NearestPlayerTargetState.Snapshot nearestPlayer) {
        if (!enabled
                || player == null
                || movement == null
                || !movement.available()) {
            resetState();
            return false;
        }

        if (pauseWhileSneaking.get().booleanValue()
                && movement.sneaking()) {
            // A pause cancels even an in-flight multi-tick reset. Preserve
            // the physical attack-button edge so holding attack while
            // leaving sneak does not create a delayed sprint reset.
            resetState();
            previousLeftButtonHeld = leftButtonHeld;
            return false;
        }

        if (requireNearbyPlayer.get().booleanValue()
                && (nearestPlayer == null
                    || !nearestPlayer.available()
                    || !nearestPlayer.found()
                    || !Double.isFinite(nearestPlayer.distance())
                    || nearestPlayer.distance() > maxPlayerDistance.get().doubleValue())) {
            // Missing, out-of-range, or stale nearest-player evidence must
            // cancel an in-flight reset and preserve the physical edge.
            // A held attack cannot retrigger when a target reappears.
            resetState();
            previousLeftButtonHeld = leftButtonHeld;
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
