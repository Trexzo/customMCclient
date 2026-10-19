package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

public final class Minecraft189StepModule
        implements Module {
    public static final String ID =
            "movement.step";
    public static final String HEIGHT_SETTING_ID =
            "movement.step.heightPercent";
    public static final String GROUND_ONLY_SETTING_ID =
            "movement.step.groundOnly";
    public static final String PAUSE_WHILE_SNEAKING_SETTING_ID =
            "movement.step.pauseWhileSneaking";
    public static final String AIRBORNE_OVERRIDE_SETTING_ID =
            "movement.step.airborneOverride";
    public static final String AIRBORNE_HEIGHT_SETTING_ID =
            "movement.step.airborneHeightPercent";
    public static final int DEFAULT_HEIGHT_PERCENT = 100;
    public static final float VANILLA_STEP_HEIGHT = 0.6F;

    private final Setting<Integer> heightPercent =
            new Setting<Integer>(
                    HEIGHT_SETTING_ID,
                    DEFAULT_HEIGHT_PERCENT,
                    value -> value >= 60
                            && value <= 250,
                    SettingCodecs.INTEGER);

    private final Setting<Boolean> groundOnly =
            new Setting<Boolean>(
                    GROUND_ONLY_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> pauseWhileSneaking =
            new Setting<Boolean>(
                    PAUSE_WHILE_SNEAKING_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Boolean> airborneOverride = new Setting<Boolean>(
            AIRBORNE_OVERRIDE_SETTING_ID, Boolean.FALSE,
            value -> value != null, SettingCodecs.BOOLEAN);
    private final Setting<Integer> airborneHeightPercent = new Setting<Integer>(
            AIRBORNE_HEIGHT_SETTING_ID, DEFAULT_HEIGHT_PERCENT,
            value -> value != null && value >= 60 && value <= 250,
            SettingCodecs.INTEGER);
    private boolean enabled;
    // Real mapped step-height getter can reenter module or GUI setting.
    private long nativeReadEpoch;
    // Real mapped stepHeight getter and setter can synchronously
    // reenter movement processing. One original player owns this stack.
    private boolean nativeStepDispatching;
    private Minecraft189PlayerStepControl nativeStepPlayer;
    private boolean nativeStepRevoked;
    private boolean nativeStepChangedPlayer;
    // Real GUI focus or disallowed grounded/sneak evidence must not
    // leave a stale elevated native collision height on this player.
    private boolean nativeStepRestoreVanilla;

    @Override
    public String id() {
        return ID;
    }

    public Setting<Integer> heightPercentSetting() {
        return heightPercent;
    }

    public Setting<Boolean> groundOnlySetting() {
        return groundOnly;
    }

    public Setting<Boolean> pauseWhileSneakingSetting() {
        return pauseWhileSneaking;
    }

    public Setting<Boolean> airborneOverrideSetting() {
        return airborneOverride;
    }

    public Setting<Integer> airborneHeightPercentSetting() {
        return airborneHeightPercent;
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
            final Minecraft189PlayerStepControl player) {
        apply(player, null);
    }

    synchronized void apply(
            final Minecraft189PlayerStepControl player,
            final Minecraft189PlayerMovementState.Snapshot movement) {
        apply(player, movement, false);
    }

    synchronized void apply(
            final Minecraft189PlayerStepControl player,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean actualClickGuiOpen) {
        if (nativeStepDispatching) {
            if (player != nativeStepPlayer) {
                nativeStepRevoked = true;
                nativeStepChangedPlayer = true;
            } else if (!enabled || actualClickGuiOpen
                    || (groundOnly.get().booleanValue()
                        && (movement == null || !movement.available()
                            || !movement.onGround()))
                    || (pauseWhileSneaking.get().booleanValue()
                        && (movement == null || !movement.available()
                            || movement.sneaking()))) {
                nativeStepRevoked = true;
                nativeStepRestoreVanilla = true;
            }
            return;
        }
        if (player == null) {
            return;
        }

        // Capture the current native-player read owner's full policy
        // before calling the external mapped source getter.
        final long ownerEpoch = nativeReadEpoch;
        final boolean groundGate = groundOnly.get().booleanValue();
        final boolean sneakGate = pauseWhileSneaking.get().booleanValue();
        final boolean airOverride = airborneOverride.get().booleanValue();
        final int airbornePercent = airborneHeightPercent.get().intValue();
        final int groundPercent = heightPercent.get().intValue();
        // A boosted step height is persistent native player state. When
        // the original GUI takes focus, restore normal vanilla collision
        // height rather than merely skipping this callback and retaining
        // an old enabled 1.0+ block climb.
        final boolean permitted =
                enabled && !actualClickGuiOpen
                        && ((!groundGate && !sneakGate)
                        || (movement != null && movement.available()
                        && (!groundGate || movement.onGround())
                        && (!sneakGate || !movement.sneaking())));
        // Restoring vanilla on failed authority is essential: simply
        // skipping writes would retain a stale boosted step height while
        // airborne, sneaking, disconnected or disabled.
        // Select the opt-in airborne height only when the mapped state
        // confirms a real airborne condition. Existing permission gates,
        // particularly Ground Only, retain priority.
        final boolean airborne = permitted && airOverride
                && movement != null && movement.available()
                && !movement.onGround();
        final float target = permitted
                ? (airborne ? airbornePercent : groundPercent) / 100.0F
                : VANILLA_STEP_HEIGHT;
        nativeStepDispatching = true;
        nativeStepPlayer = player;
        nativeStepRevoked = false;
        nativeStepChangedPlayer = false;
        nativeStepRestoreVanilla = false;
        try {
            final float current = player.customMcStepHeight();
            if (ownerEpoch != nativeReadEpoch || nativeStepRevoked
                    || groundOnly.get().booleanValue() != groundGate
                    || pauseWhileSneaking.get().booleanValue() != sneakGate
                    || airborneOverride.get().booleanValue() != airOverride
                    || airborneHeightPercent.get().intValue() != airbornePercent
                    || heightPercent.get().intValue() != groundPercent) {
                // The original source may have disabled Step during the
                // getter. Restore real vanilla collision only on the
                // still-pinned player, never after a player swap.
                // A new ON owner is a fresh lease and is not overwritten.
                if (!nativeStepChangedPlayer
                        && (!enabled || (nativeStepRestoreVanilla
                            && ownerEpoch == nativeReadEpoch))
                        && (!Float.isFinite(current)
                            || Math.abs(current - VANILLA_STEP_HEIGHT)
                                > 0.000001F)) {
                    player.customMcSetStepHeight(VANILLA_STEP_HEIGHT);
                }
                return;
            }
            if (!Float.isFinite(current)
                    || Math.abs(current - target) > 0.000001F) {
                player.customMcSetStepHeight(target);
            }
            // A genuine native setter can reenter the GUI (or disable
            // Step) AFTER changing the collision height. Restore the
            // original vanilla height only for this still-pinned player;
            // a changed-player callback must never alter the old player.
            if (!nativeStepChangedPlayer
                    && (nativeStepRestoreVanilla || !enabled)
                    && (ownerEpoch == nativeReadEpoch || !enabled)
                    && Math.abs(target - VANILLA_STEP_HEIGHT) > 0.000001F) {
                player.customMcSetStepHeight(VANILLA_STEP_HEIGHT);
            }
        } finally {
            // A mapped setter may mutate vanilla height then throw.
            // Release the guard; never invent an automatic rollback.
            nativeStepPlayer = null;
            nativeStepRevoked = false;
            nativeStepChangedPlayer = false;
            nativeStepRestoreVanilla = false;
            nativeStepDispatching = false;
        }
    }

    synchronized boolean active() {
        return enabled;
    }
}
