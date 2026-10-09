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

    @Override
    public String id() {
        return ID;
    }

    @Override
    public synchronized void onEnable() {
        enabled = true;
        baselineCaptured = false;
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
        if (player == null) {
            return;
        }

        if (enabled) {
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
        if (player.customMcNoClip() != baselineNoClip) {
            player.customMcSetNoClip(baselineNoClip);
        }
        baselineCaptured = false;
    }

    synchronized boolean active() {
        return enabled;
    }

    synchronized boolean restorePending() {
        return restorePending;
    }
}
