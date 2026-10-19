package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

import java.util.Objects;

public final class Minecraft189AutoSneakModule
        implements Module {
    public static final String ID =
            "movement.autoSneak";
    public static final String GROUND_ONLY_SETTING_ID =
            "movement.autoSneak.groundOnly";
    public static final String REQUIRE_MOVEMENT_SETTING_ID =
            "movement.autoSneak.requireMovement";
    public static final String REQUIRE_FORWARD_SETTING_ID =
            "movement.autoSneak.requireForward";
    public static final String PAUSE_SPRINTING_SETTING_ID =
            "movement.autoSneak.pauseSprinting";

    private final Setting<Boolean> groundOnly = new Setting<Boolean>(
            GROUND_ONLY_SETTING_ID, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> pauseSprinting = new Setting<Boolean>(
            PAUSE_SPRINTING_SETTING_ID, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);

    private final Setting<Boolean> requireMovement = new Setting<Boolean>(
            REQUIRE_MOVEMENT_SETTING_ID, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> requireForward = new Setting<Boolean>(
            REQUIRE_FORWARD_SETTING_ID, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private boolean enabled;
    // EntityPlayer.setSneaking(true) can synchronously re-enter the
    // original movement host before the native sneaking bit updates.
    // An OFF->ON during that callback must not permit nested writes.
    private boolean nativeSneakDispatching;

    public Setting<Boolean> groundOnlySetting() {
        return groundOnly;
    }

    public Setting<Boolean> pauseSprintingSetting() {
        return pauseSprinting;
    }

    public Setting<Boolean> requireMovementSetting() {
        return requireMovement;
    }

    public Setting<Boolean> requireForwardSetting() {
        return requireForward;
    }

    @Override
    public String id() {
        return ID;
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
            final Minecraft189PlayerSneakControl player,
            final Minecraft189PlayerMovementState.Snapshot movement) {
        // Old callers have no physical W or WASD authority.
        apply(player, movement, false, false);
    }

    synchronized void apply(
            final Minecraft189PlayerSneakControl player,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean anyMovementHeld) {
        // This legacy overload can only confirm that some movement key
        // was held. It must not infer W from an A/S/D movement signal.
        apply(player, movement, false, anyMovementHeld);
    }

    synchronized void apply(
            final Minecraft189PlayerSneakControl player,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean forwardHeld,
            final boolean anyMovementHeld) {
        apply(player, movement, forwardHeld, anyMovementHeld, false);
    }

    synchronized void apply(
            final Minecraft189PlayerSneakControl player,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean forwardHeld,
            final boolean anyMovementHeld,
            final boolean actualClickGuiOpen) {
        Objects.requireNonNull(
                movement,
                "movement");
        // The source GUI owns input focus. AutoSneak must not inject a
        // synthetic native player sneak write while menus are active.
        // Existing user/vanilla sneaking remains untouched.
        if (!enabled
                || nativeSneakDispatching
                || actualClickGuiOpen
                || player == null
                || !movement.available()
                || movement.sneaking()
                || (groundOnly.get().booleanValue() && !movement.onGround())
                || (pauseSprinting.get().booleanValue() && movement.sprinting())
                || (requireMovement.get().booleanValue() && !anyMovementHeld)
                || (requireForward.get().booleanValue() && !forwardHeld)) {
            return;
        }
        nativeSneakDispatching = true;
        try {
            player.customMcSetSneaking(true);
        } finally {
            // A native setter may throw after applying a real state
            // change. Never invent an unsneak rollback; merely release
            // the same-stack guard for a future real host callback.
            nativeSneakDispatching = false;
        }
    }

    synchronized boolean active() {
        return enabled;
    }
}
