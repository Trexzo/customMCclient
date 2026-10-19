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
    public static final String REQUIRE_MOVEMENT_SETTING_ID =
            "movement.autoJump.requireMovement";
    public static final String LANDING_DELAY_SETTING_ID =
            "movement.autoJump.landingDelayTicks";
    public static final String REQUIRE_SPRINTING_SETTING_ID =
            "movement.autoJump.requireSprinting";
    public static final String PAUSE_WHILE_SNEAKING_SETTING_ID =
            "movement.autoJump.pauseWhileSneaking";
    public static final int MAXIMUM_LANDING_DELAY_TICKS = 10;

    private final Setting<Boolean> requireForward =
            new Setting<Boolean>(
                    REQUIRE_FORWARD_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);

    private final Setting<Boolean> requireMovement =
            new Setting<Boolean>(
                    REQUIRE_MOVEMENT_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Integer> landingDelayTicks =
            new Setting<Integer>(
                    LANDING_DELAY_SETTING_ID,
                    0,
                    value -> value != null && value >= 0
                            && value <= MAXIMUM_LANDING_DELAY_TICKS,
                    SettingCodecs.INTEGER);

    private final Setting<Boolean> pauseWhileSneaking =
            new Setting<Boolean>(
                    PAUSE_WHILE_SNEAKING_SETTING_ID,
                    Boolean.FALSE,
                    value -> value != null,
                    SettingCodecs.BOOLEAN);

    // Optional real mapped sprint gate, OFF by default for legacy Jump.
    private final Setting<Boolean> requireSprinting = new Setting<Boolean>(
            REQUIRE_SPRINTING_SETTING_ID, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);

    private boolean enabled;
    private boolean armed = true;
    private boolean airborneObserved;
    private int groundedUpdatesRemaining;
    // One grounded contact owns one specific persisted delay policy. Changing
    // its countdown after landing must not replay an old native jump on the
    // same player contact (including after countdown reaches zero).
    private int landingDelayAtContact = -1;
    // A physical airborne -> grounded contact owns the entire current
    // persisted movement eligibility profile, not just its delay value.
    private boolean forwardRequiredAtContact;
    private boolean movementRequiredAtContact;
    private boolean sprintRequiredAtContact;
    private boolean pauseSneakingAtContact;
    // Actual EntityPlayer.jump() can synchronously reenter the module,
    // including a nested OFF/ON which re-arms a new landing contact.
    // Prevent that new activation from borrowing the still-running
    // source jump's original native stack.
    private boolean nativeJumpDispatching;

    @Override
    public String id() {
        return ID;
    }

    public Setting<Boolean> requireForwardSetting() {
        return requireForward;
    }

    public Setting<Boolean> requireMovementSetting() {
        return requireMovement;
    }

    public Setting<Integer> landingDelayTicksSetting() {
        return landingDelayTicks;
    }

    public Setting<Boolean> pauseWhileSneakingSetting() {
        return pauseWhileSneaking;
    }

    public Setting<Boolean> requireSprintingSetting() {
        return requireSprinting;
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
        // Legacy callers have no independent A/S/D input authority.
        apply(player, movement, forwardHeld, forwardHeld);
    }

    synchronized void apply(
            final Minecraft189PlayerJumpControl player,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean forwardHeld,
            final boolean anyMovementHeld) {
        apply(player, movement, forwardHeld, anyMovementHeld, false);
    }

    synchronized void apply(
            final Minecraft189PlayerJumpControl player,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean forwardHeld,
            final boolean anyMovementHeld,
            final boolean actualClickGuiOpen) {
        Objects.requireNonNull(movement, "movement");
        if (nativeJumpDispatching) {
            // One genuine original jump stack may dispatch at most once.
            // Do not mutate a freshly re-enabled owner's cadence from
            // an older native callback.
            return;
        }
        if (actualClickGuiOpen) {
            // Even if W is physically held while opening the menu, a
            // previously eligible grounded click must not fire AFTER
            // focus returns. Only new original airborne→landing evidence
            // can authorize a fresh source-native jump.
            armed = false;
            airborneObserved = false;
            groundedUpdatesRemaining = 0;
            landingDelayAtContact = -1;
            return;
        }
        if (!enabled || player == null || !movement.available()) {
            // An unknown or stale movement snapshot must never advance
            // a pending landing countdown into a synthetic jump.
            if (enabled) {
                resetCadence();
            }
            return;
        }

        if (pauseWhileSneaking.get().booleanValue()
                && movement.sneaking()) {
            // Cancels a pending landing and disarms this ground contact.
            // Releasing Sneak while still standing on the same tile must
            // not trigger a delayed synthetic jump. A fresh airborne
            // observation is required to arm the next landing.
            armed = false;
            airborneObserved = false;
            groundedUpdatesRemaining = 0;
            landingDelayAtContact = -1;
            return;
        }

        if (!movement.onGround()) {
            airborneObserved = true;
            armed = true;
            groundedUpdatesRemaining = 0;
            landingDelayAtContact = -1;
            return;
        }

        if (airborneObserved) {
            // Apply the configured delay once per observed landing, not on
            // every grounded callback. Each delayed callback represents one
            // grounded update; a new airborne sample cancels the countdown.
            airborneObserved = false;
            landingDelayAtContact = landingDelayTicks.get().intValue();
            forwardRequiredAtContact = requireForward.get().booleanValue();
            movementRequiredAtContact = requireMovement.get().booleanValue();
            sprintRequiredAtContact = requireSprinting.get().booleanValue();
            pauseSneakingAtContact = pauseWhileSneaking.get().booleanValue();
            groundedUpdatesRemaining = landingDelayAtContact;
        }
        if (armed && landingDelayAtContact >= 0
                && (landingDelayAtContact != landingDelayTicks.get()
                || (landingDelayAtContact > 0
                    && (forwardRequiredAtContact
                            != requireForward.get().booleanValue()
                    || movementRequiredAtContact
                            != requireMovement.get().booleanValue()
                    || sprintRequiredAtContact
                            != requireSprinting.get().booleanValue()
                    || pauseSneakingAtContact
                            != pauseWhileSneaking.get().booleanValue())))) {
            // A GUI/profile edit invalidates THIS observed landing even if
            // its old countdown has already elapsed and movement is gated.
            // Do not schedule a new jump until real source air->ground occurs.
            armed = false;
            groundedUpdatesRemaining = 0;
            landingDelayAtContact = -1;
            return;
        }
        if (groundedUpdatesRemaining > 0) {
            groundedUpdatesRemaining--;
            return;
        }

        if ((requireForward.get().booleanValue()
                && !forwardHeld)
                || (requireMovement.get().booleanValue()
                        && !anyMovementHeld)
                || (requireSprinting.get().booleanValue()
                        && !movement.sprinting())
                || !armed) {
            return;
        }

        // Consume the grounded lease BEFORE the original native action.
        // A throwing source bridge cannot replay the same grounded jump.
        armed = false;
        landingDelayAtContact = -1;
        nativeJumpDispatching = true;
        try {
            player.customMcJump();
        } finally {
            // A mapped original jump can update actual Y and then
            // throw; release only this lease, never fake a rollback.
            nativeJumpDispatching = false;
        }
    }

    synchronized boolean active() {
        return enabled;
    }

    private void resetCadence() {
        armed = true;
        airborneObserved = false;
        groundedUpdatesRemaining = 0;
        landingDelayAtContact = -1;
    }
}
