package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.setting.SettingCodecs;

public final class Minecraft189NoClipModule
        implements Module {
    public static final String ID =
            "movement.noClip";
    public static final String REQUIRE_SNEAK_SETTING_ID =
            "movement.noClip.requireSneaking";

    private final Setting<Boolean> requireSneaking =
            new Setting<Boolean>(
                    REQUIRE_SNEAK_SETTING_ID, Boolean.FALSE,
                    value -> value != null, SettingCodecs.BOOLEAN);

    public Setting<Boolean> requireSneakingSetting() {
        return requireSneaking;
    }

    private boolean enabled;
    private boolean baselineCaptured;
    private boolean baselineNoClip;
    private boolean restorePending;
    // Original native player instance that owns the captured collision
    // baseline. A World transition or respawn is NOT that same player.
    private Minecraft189PlayerNoClipControl baselineOwner;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public synchronized void onEnable() {
        enabled = true;
        // Disable followed by Enable BEFORE the next original tick must
        // retain the original player's captured baseline. Throwing it away
        // would re-capture the module's OWN forced true as the new default.
        if (!restorePending) {
            baselineCaptured = false;
            baselineOwner = null;
        }
        restorePending = false;
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
        restorePending = baselineCaptured;
    }

    synchronized void apply(
            final Minecraft189PlayerNoClipControl player) {
        apply(player, null);
    }

    synchronized void apply(
            final Minecraft189PlayerNoClipControl player,
            final Minecraft189PlayerMovementState.Snapshot movement) {
        apply(player, movement, false);
    }

    synchronized void apply(
            final Minecraft189PlayerNoClipControl player,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean actualClickGuiOpen) {
        if (player == null) {
            return;
        }
        if (baselineCaptured && baselineOwner != player) {
            // Source-native player object changed (respawn / world swap).
            // Never restore the OLD player's collision state onto a new
            // object; the new player must supply a fresh original baseline.
            baselineCaptured = false;
            baselineOwner = null;
            restorePending = false;
        }

        if (enabled) {
            // A collision override outlives its setter: merely skipping
            // ClickGUI callbacks would leave noClip=true through menus.
            // Release only this player's captured lease to its own native
            // baseline. After closing GUI we may capture a new baseline.
            if (actualClickGuiOpen) {
                restoreCaptured(player);
                return;
            }
            final boolean gated = requireSneaking.get().booleanValue()
                    && (movement == null || !movement.available()
                    || !movement.sneaking());
            if (gated) {
                // Hold-to-activate mode must also release our previous
                // collision override, not leave No Clip forced true.
                restoreCaptured(player);
                return;
            }
            if (!baselineCaptured) {
                baselineNoClip = player.customMcNoClip();
                baselineOwner = player;
                baselineCaptured = true;
            }
            if (!player.customMcNoClip()) {
                player.customMcSetNoClip(true);
            }
            return;
        }

        if (restorePending) {
            restoreCaptured(player);
            restorePending = false;
        }
    }

    private void restoreCaptured(
            final Minecraft189PlayerNoClipControl player) {
        if (!baselineCaptured) {
            return;
        }
        // Only undo a forced TRUE that this module could have written.
        // When the original baseline was true, a user's later native
        // change to false belongs to them, not to this stale lease.
        if (baselineOwner == player
                && player.customMcNoClip() && !baselineNoClip) {
            player.customMcSetNoClip(false);
        }
        baselineCaptured = false;
        baselineOwner = null;
    }

    synchronized boolean active() {
        return enabled;
    }

    synchronized boolean restorePending() {
        return restorePending;
    }
}
