package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;

public final class Minecraft189WTapModule
        implements Module {
    public static final String ID =
            "combat.wTap";

    private boolean enabled;
    private boolean previousLeftButtonHeld;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public synchronized void onEnable() {
        enabled = true;
        resetInput();
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
        resetInput();
    }

    synchronized boolean apply(
            final Minecraft189PlayerSprintControl player,
            final Minecraft189PlayerMovementState.Snapshot movement,
            final boolean leftButtonHeld) {
        if (!enabled
                || player == null
                || movement == null
                || !movement.available()) {
            resetInput();
            return false;
        }

        if (!leftButtonHeld) {
            previousLeftButtonHeld = false;
            return false;
        }
        if (previousLeftButtonHeld) {
            return false;
        }
        previousLeftButtonHeld = true;

        if (!movement.sprinting()) {
            return false;
        }

        player.customMcSetSprinting(
                false);
        return true;
    }

    synchronized boolean active() {
        return enabled;
    }

    private void resetInput() {
        previousLeftButtonHeld = false;
    }
}
