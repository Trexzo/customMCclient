package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;

public final class Minecraft189NoSlowModule
        implements Module {
    public static final String ID =
            "movement.noSlow";

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

    synchronized float adjustSlowedMovement(
            final float slowedValue) {
        return enabled
                ? slowedValue * 5.0F
                : slowedValue;
    }

    synchronized boolean active() {
        return enabled;
    }
}
