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
    public static final String PAUSE_WHILE_RIGHT_CLICKING_SETTING_ID =
            "combat.autoClicker.pauseWhileRightClicking";
    public static final String PAUSE_WHILE_SNEAKING_SETTING_ID =
            "combat.autoClicker.pauseWhileSneaking";
    public static final String REQUIRE_NEARBY_PLAYER_SETTING_ID =
            "combat.autoClicker.requireNearbyPlayer";
    public static final String MAX_PLAYER_DISTANCE_SETTING_ID =
            "combat.autoClicker.maxPlayerDistance";
    public static final double DEFAULT_MAX_PLAYER_DISTANCE = 4.0D;
    public static final String REQUIRE_FORWARD_SETTING_ID =
            "combat.autoClicker.requireForward";
    public static final String REQUIRE_HOLD_SETTING_ID =
            "combat.autoClicker.requireHold";
    public static final String RAMP_UP_SETTING_ID =
            "combat.autoClicker.rampUp";
    public static final String RAMP_UP_TICKS_SETTING_ID =
            "combat.autoClicker.rampUpTicks";
    public static final int DEFAULT_RAMP_UP_TICKS = 20;
    public static final int MINIMUM_RAMP_UP_TICKS = 1;
    public static final int MAXIMUM_RAMP_UP_TICKS = 100;

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
    private final Setting<Boolean> pauseWhileRightClicking =
            new Setting<Boolean>(
                    PAUSE_WHILE_RIGHT_CLICKING_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private final Setting<Boolean> pauseWhileSneaking = new Setting<Boolean>(
            PAUSE_WHILE_SNEAKING_SETTING_ID, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> requireNearbyPlayer = new Setting<Boolean>(
            REQUIRE_NEARBY_PLAYER_SETTING_ID, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Double> maxPlayerDistance = new Setting<Double>(
            MAX_PLAYER_DISTANCE_SETTING_ID, DEFAULT_MAX_PLAYER_DISTANCE,
            value -> value != null && Double.isFinite(value.doubleValue())
                    && value.doubleValue() >= 0.5D && value.doubleValue() <= 16.0D,
            SettingCodecs.DOUBLE);
    private final Setting<Boolean> requireForward =
            new Setting<Boolean>(
                    REQUIRE_FORWARD_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
    private final Setting<Boolean> requireHold =
            new Setting<Boolean>(
                    REQUIRE_HOLD_SETTING_ID,
                    Boolean.TRUE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);

    private final Setting<Boolean> rampUp = new Setting<Boolean>(
            RAMP_UP_SETTING_ID, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Integer> rampUpTicks = new Setting<Integer>(
            RAMP_UP_TICKS_SETTING_ID, DEFAULT_RAMP_UP_TICKS,
            value -> value != null
                    && value >= MINIMUM_RAMP_UP_TICKS
                    && value <= MAXIMUM_RAMP_UP_TICKS,
            SettingCodecs.INTEGER);

    private boolean enabled;
    private int elapsedEligibleTicks;
    private boolean scheduledRampUp;
    private int scheduledRampUpTicks;
    private int phaseCredit;
    private int targetCps;
    private int scheduledMinimumCps;
    private int scheduledMaximumCps;

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

    public Setting<Boolean> pauseWhileRightClickingSetting() {
        return pauseWhileRightClicking;
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

    public Setting<Boolean> requireForwardSetting() {
        return requireForward;
    }

    public Setting<Boolean> requireHoldSetting() {
        return requireHold;
    }

    public Setting<Boolean> rampUpSetting() {
        return rampUp;
    }

    public Setting<Integer> rampUpTicksSetting() {
        return rampUpTicks;
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
        return shouldClick(leftButtonHeld, false);
    }

    synchronized boolean shouldClick(
            final boolean leftButtonHeld,
            final boolean forwardHeld) {
        return shouldClick(leftButtonHeld, forwardHeld, false);
    }

    synchronized boolean shouldClick(
            final boolean leftButtonHeld,
            final boolean forwardHeld,
            final boolean rightButtonHeld) {
        return shouldClick(leftButtonHeld, forwardHeld, rightButtonHeld, null);
    }

    synchronized boolean shouldClick(
            final boolean leftButtonHeld,
            final boolean forwardHeld,
            final boolean rightButtonHeld,
            final Minecraft189NearestPlayerTargetState.Snapshot nearestPlayer) {
        return shouldClick(leftButtonHeld, forwardHeld, rightButtonHeld,
                nearestPlayer, null);
    }

    synchronized boolean shouldClick(
            final boolean leftButtonHeld,
            final boolean forwardHeld,
            final boolean rightButtonHeld,
            final Minecraft189NearestPlayerTargetState.Snapshot nearestPlayer,
            final Minecraft189PlayerMovementState.Snapshot movement) {
        if (!enabled
                || (requireHold.get().booleanValue()
                        && !leftButtonHeld)
                || (requireForward.get().booleanValue()
                        && !forwardHeld)
                || (pauseWhileRightClicking.get().booleanValue()
                        && rightButtonHeld)
                || (pauseWhileSneaking.get().booleanValue()
                        && (movement == null || !movement.available()
                        || movement.sneaking()))
                || (requireNearbyPlayer.get().booleanValue()
                        && (nearestPlayer == null
                        || !nearestPlayer.available()
                        || !nearestPlayer.found()
                        || !Double.isFinite(nearestPlayer.distance())
                        || nearestPlayer.distance() > maxPlayerDistance.get().doubleValue()))) {
            resetSchedule();
            return false;
        }

        // A live CPS edit must not inherit phase credit or a sampled
        // click target from the previous configuration. Compare BOTH
        // bounds so editing either Min or Max resets the cadence.
        final int currentMinimumCps = minCps.get().intValue();
        final int currentMaximumCps = maxCps.get().intValue();
        final boolean currentRampUp = rampUp.get().booleanValue();
        final int currentRampUpTicks = rampUpTicks.get().intValue();
        if (scheduledMinimumCps != currentMinimumCps
                || scheduledMaximumCps != currentMaximumCps
                || scheduledRampUp != currentRampUp
                || scheduledRampUpTicks != currentRampUpTicks) {
            resetSchedule();
            scheduledMinimumCps = currentMinimumCps;
            scheduledMaximumCps = currentMaximumCps;
            scheduledRampUp = currentRampUp;
            scheduledRampUpTicks = currentRampUpTicks;
        }

        if (targetCps <= 0) {
            targetCps = nextTargetCps();
        }

        // Progress only while all existing click gates pass. The effective
        // rate starts at the first ramp fraction (minimum 1 CPS) and reaches
        // the target CPS within rampUpTicks eligible callbacks.
        // Target CPS is still re-sampled by the original click scheduler.
        final int effectiveCps;
        if (currentRampUp) {
            elapsedEligibleTicks = Math.min(
                    currentRampUpTicks, elapsedEligibleTicks + 1);
            effectiveCps = Math.max(1,
                    (targetCps * elapsedEligibleTicks
                            + currentRampUpTicks - 1)
                            / currentRampUpTicks);
        } else {
            effectiveCps = targetCps;
        }
        phaseCredit += effectiveCps;
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
        scheduledMinimumCps = 0;
        scheduledMaximumCps = 0;
        scheduledRampUp = false;
        scheduledRampUpTicks = 0;
        elapsedEligibleTicks = 0;
    }
}
