package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;

public final class Minecraft189NoClipModule
        implements Module {
    public static final String ID =
            "movement.noClip";

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
        if (player == null) {
            return;
        }

        if (enabled) {
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
            if (player.customMcNoClip() != baselineNoClip) {
                player.customMcSetNoClip(
                        baselineNoClip);
            }
            restorePending = false;
            baselineCaptured = false;
        }
    }

    synchronized boolean active() {
        return enabled;
    }

    synchronized boolean restorePending() {
        return restorePending;
    }
}
