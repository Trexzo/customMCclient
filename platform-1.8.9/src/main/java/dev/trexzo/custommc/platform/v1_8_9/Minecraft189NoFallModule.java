package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;

public final class Minecraft189NoFallModule
        implements Module {
    public static final String ID =
            "movement.noFall";

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
            final Minecraft189PlayerFallDistanceControl player) {
        if (!enabled || player == null) {
            return;
        }

        if (Math.abs(player.customMcFallDistance()) > 0.000001F) {
            player.customMcSetFallDistance(0.0F);
        }
    }

    synchronized boolean active() {
        return enabled;
    }
}
