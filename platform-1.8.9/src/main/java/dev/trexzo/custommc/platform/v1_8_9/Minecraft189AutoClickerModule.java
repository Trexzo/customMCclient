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
    public static final String REQUIRE_NEARBY_PLAYER_SETTING_ID =
            "combat.autoClicker.requireNearbyPlayer";
    public static final String MAX_PLAYER_DISTANCE_SETTING_ID =
            "combat.autoClicker.maxPlayerDistance";
    public static final double DEFAULT_MAX_PLAYER_DISTANCE = 4.0D;
    public static final String REQUIRE_FORWARD_SETTING_ID =
            "combat.autoClicker.requireForward";
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
    private final Setting<Boolean> pauseWhileRightClicking =
            new Setting<Boolean>(
                    PAUSE_WHILE_RIGHT_CLICKING_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);
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

    private boolean enabled;
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
        if (!enabled
                || (requireHold.get().booleanValue()
                        && !leftButtonHeld)
                || (requireForward.get().booleanValue()
                        && !forwardHeld)
                || (pauseWhileRightClicking.get().booleanValue()
                        && rightButtonHeld)
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
        if (scheduledMinimumCps != currentMinimumCps
                || scheduledMaximumCps != currentMaximumCps) {
            resetSchedule();
            scheduledMinimumCps = currentMinimumCps;
            scheduledMaximumCps = currentMaximumCps;
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
        scheduledMinimumCps = 0;
        scheduledMaximumCps = 0;
    }
}
