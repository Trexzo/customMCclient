package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

import java.util.Objects;

public final class Minecraft189AutoJumpModule
        implements Module {
    public static final String ID =
            "movement.autoJump";
    public static final String REQUIRE_FORWARD_SETTING_ID =
            "movement.autoJump.requireForward";
    public static final String LANDING_DELAY_SETTING_ID =
            "movement.autoJump.landingDelayTicks";
    public static final int MAXIMUM_LANDING_DELAY_TICKS = 10;

    private final Setting<Boolean> requireForward =
            new Setting<Boolean>(
                    REQUIRE_FORWARD_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);

    private final Setting<Integer> landingDelayTicks =
            new Setting<Integer>(
                    LANDING_DELAY_SETTING_ID,
                    0,
                    value -> value != null && value >= 0
                            && value <= MAXIMUM_LANDING_DELAY_TICKS,
                    SettingCodecs.INTEGER);

    private boolean enabled;
    private boolean armed = true;
    private boolean airborneObserved;
    private int groundedUpdatesRemaining;

    @Override
    public String id() {
        return ID;
    }

    public Setting<Boolean> requireForwardSetting() {
        return requireForward;
    }

    public Setting<Integer> landingDelayTicksSetting() {
        return landingDelayTicks;
    }

    @Override
    public synchronized void onEnable() {
        enabled = true;
        resetCadence();
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
        resetCadence();
    }

    synchronized void apply(
            final Minecraft189PlayerJumpControl player,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean forwardHeld) {
        Objects.requireNonNull(
                movement,
                "movement");
        if (!enabled || player == null || !movement.available()) {
            // An unknown or stale movement snapshot must never advance
            // a pending landing countdown into a synthetic jump.
            if (enabled) {
                resetCadence();
            }
            return;
        }

        if (!movement.onGround()) {
            airborneObserved = true;
            armed = true;
            groundedUpdatesRemaining = 0;
            return;
        }

        if (airborneObserved) {
            // Apply the configured delay once per observed landing, not on
            // every grounded callback. Each delayed callback represents one
            // grounded update; a new airborne sample cancels the countdown.
            airborneObserved = false;
            groundedUpdatesRemaining = landingDelayTicks.get().intValue();
        }
        if (groundedUpdatesRemaining > 0) {
            groundedUpdatesRemaining--;
            return;
        }

        if ((requireForward.get().booleanValue()
                && !forwardHeld) || !armed) {
            return;
        }

        player.customMcJump();
        armed = false;
    }

    synchronized boolean active() {
        return enabled;
    }

    private void resetCadence() {
        armed = true;
        airborneObserved = false;
        groundedUpdatesRemaining = 0;
    }
}
