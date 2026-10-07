package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;

import java.util.Objects;

public final class Minecraft189AutoSneakModule
        implements Module {
    public static final String ID =
            "movement.autoSneak";

    private boolean enabled;

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
        Objects.requireNonNull(
                movement,
                "movement");
        if (!enabled
                || player == null
                || !movement.available()
                || movement.sneaking()) {
            return;
        }
        player.customMcSetSneaking(
                true);
    }

    synchronized boolean active() {
        return enabled;
    }
}
