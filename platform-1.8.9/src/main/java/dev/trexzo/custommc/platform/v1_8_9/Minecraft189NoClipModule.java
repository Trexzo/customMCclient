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
    // A native getter must not outlive the ModuleController activation.
    private long nativeReadEpoch;
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
        nativeReadEpoch++;
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
        nativeReadEpoch++;
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
            final long currentEpoch = nativeReadEpoch;
            final boolean needsSneak = requireSneaking.get().booleanValue();
            if (!baselineCaptured) {
                // This first REAL original getter supplies the only valid
                // baseline; do not capture it if the getter reenters OFF/ON.
                final boolean originalValue = player.customMcNoClip();
                if (!activationOwnsRead(currentEpoch, needsSneak, movement)) {
                    return;
                }
                baselineNoClip = originalValue;
                baselineOwner = player;
                baselineCaptured = true;
            }
            final boolean nativeValue = player.customMcNoClip();
            if (!activationOwnsRead(currentEpoch, needsSneak, movement)
                    || !baselineCaptured || baselineOwner != player) {
                return;
            }
            if (!nativeValue) {
                player.customMcSetNoClip(true);
            }
            return;
        }

        if (restorePending) {
            final long restoringEpoch = nativeReadEpoch;
            restoreCaptured(player);
            // A getter can reenter enable/disable while trying to restore.
            // The newly owned pending lease must remain pending in that
            // case and must NOT be discarded by the old callback.
            if (!enabled && restoringEpoch == nativeReadEpoch) {
                restorePending = false;
            }
        }
    }

    private boolean activationOwnsRead(
            final long epoch, final boolean needsSneak,
            final Minecraft189PlayerMovementState.Snapshot movement) {
        return enabled && nativeReadEpoch == epoch
                && requireSneaking.get().booleanValue() == needsSneak
                && (!needsSneak
                    || (movement != null && movement.available()
                        && movement.sneaking()));
    }

    private void restoreCaptured(
            final Minecraft189PlayerNoClipControl player) {
        if (!baselineCaptured) {
            return;
        }
        final long restoringEpoch = nativeReadEpoch;
        final boolean originalEnabled = enabled;
        final Minecraft189PlayerNoClipControl owner = baselineOwner;
        final boolean originalNoClip = baselineNoClip;
        // Only undo forced true that this module owned on the SAME
        // original player. An original baseline already true is not ours.
        if (owner == player && !originalNoClip) {
            final boolean currentlyEnabled = player.customMcNoClip();
            if (restoringEpoch != nativeReadEpoch
                    || enabled != originalEnabled
                    || !baselineCaptured || baselineOwner != owner) {
                // Old read cannot revert a newly enabled module or a
                // different original player. Keep its lease for next tick.
                return;
            }
            if (currentlyEnabled) {
                player.customMcSetNoClip(false);
                if (restoringEpoch != nativeReadEpoch
                        || enabled != originalEnabled
                        || !baselineCaptured || baselineOwner != owner) {
                    return;
                }
            }
        }
        if (restoringEpoch == nativeReadEpoch
                && enabled == originalEnabled
                && baselineOwner == owner) {
            baselineCaptured = false;
            baselineOwner = null;
        }
    }

    synchronized boolean active() {
        return enabled;
    }

    synchronized boolean restorePending() {
        return restorePending;
    }
}
