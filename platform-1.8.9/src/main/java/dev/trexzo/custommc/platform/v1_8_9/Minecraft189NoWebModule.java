package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

public final class Minecraft189NoWebModule
        implements Module {
    public static final String ID =
            "movement.noWeb";
    public static final String GROUND_ONLY_SETTING_ID =
            "movement.noWeb.groundOnly";
    public static final String REQUIRE_SNEAKING_SETTING_ID =
            "movement.noWeb.requireSneaking";
    public static final String AIRBORNE_ONLY_SETTING_ID =
            "movement.noWeb.airborneOnly";

    private final Setting<Boolean> groundOnly = new Setting<Boolean>(
            GROUND_ONLY_SETTING_ID, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> requireSneaking = new Setting<Boolean>(
            REQUIRE_SNEAKING_SETTING_ID, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> airborneOnly = new Setting<Boolean>(
            AIRBORNE_ONLY_SETTING_ID, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);

    public Setting<Boolean> groundOnlySetting() {
        return groundOnly;
    }

    public Setting<Boolean> requireSneakingSetting() {
        return requireSneaking;
    }

    public Setting<Boolean> airborneOnlySetting() {
        return airborneOnly;
    }

    private boolean enabled;
    // Invalidates callbacks that read a native source owner before restart.
    private long nativeReadEpoch;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public synchronized void onEnable() {
        enabled = true;
        nativeReadEpoch++;
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
        nativeReadEpoch++;
    }

    synchronized void apply(
            final Minecraft189PlayerWebControl player) {
        apply(player, null);
    }

    synchronized void apply(
            final Minecraft189PlayerWebControl player,
            final Minecraft189PlayerMovementState.Snapshot movement) {
        apply(player, movement, false);
    }

    synchronized void apply(
            final Minecraft189PlayerWebControl player,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean actualClickGuiOpen) {
        // The original player owns its web collision flag while the
        // CustomMC menu has keyboard focus. Never clear a real web hit
        // during a menu callback or manufacture a delayed removal.
        if (!enabled || actualClickGuiOpen || player == null) {
            return;
        }
        final boolean ground = groundOnly.get().booleanValue();
        final boolean sneak = requireSneaking.get().booleanValue();
        final boolean airborne = airborneOnly.get().booleanValue();
        // Contradictory ground/air conditions never silently override one
        // another. Without confirmed movement, opted-in state gates cannot
        // clear the local web flag or synthesize a new flag value.
        if ((ground && airborne)
                || ((ground || sneak || airborne)
                && (movement == null || !movement.available()
                        || (ground && !movement.onGround())
                        || (airborne && movement.onGround())
                        || (sneak && !movement.sneaking())))) {
            // Existing local web flag is left intact when a configured
            // mapped-state condition is not confirmed.
            return;
        }
        // The mapped player getter is an external callback. A module
        // OFF -> ON, or GUI policy mutation while reading it, cannot
        // authorize a write under the previous source lease.
        final long approvedEpoch = nativeReadEpoch;
        final boolean originalInWeb = player.customMcInWeb();
        if (originalInWeb
                && enabled
                && nativeReadEpoch == approvedEpoch
                && groundOnly.get().booleanValue() == ground
                && requireSneaking.get().booleanValue() == sneak
                && airborneOnly.get().booleanValue() == airborne) {
            player.customMcSetInWeb(false);
        }
    }

    synchronized boolean active() {
        return enabled;
    }
}
