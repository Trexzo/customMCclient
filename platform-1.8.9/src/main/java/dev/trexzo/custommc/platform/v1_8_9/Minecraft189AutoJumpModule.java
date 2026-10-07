package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;

import java.util.Objects;

public final class Minecraft189AutoJumpModule
        implements Module {
    public static final String ID =
            "movement.autoJump";

    private boolean enabled;
    private boolean armed = true;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public synchronized void onEnable() {
        enabled = true;
        armed = true;
    }

    @Override
    public synchronized void onDisable() {
        enabled = false;
        armed = true;
    }

    synchronized void apply(
            final Minecraft189PlayerJumpControl player,
            final Minecraft189PlayerMovementState.Snapshot movement) {
        Objects.requireNonNull(
                movement,
                "movement");
        if (!enabled
                || player == null
                || !movement.available()) {
            return;
        }

        if (!movement.onGround()) {
            armed = true;
            return;
        }

        if (!armed) {
            return;
        }

        player.customMcJump();
        armed = false;
    }

    synchronized boolean active() {
        return enabled;
    }
}
