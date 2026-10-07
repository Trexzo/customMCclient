package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;

public final class Minecraft189NoHitDelayModule
        implements Module {
    public static final String ID =
            "combat.noHitDelay";

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

    synchronized int apply(
            final int currentCounter) {
        if (!enabled) {
            return currentCounter;
        }
        return currentCounter > 0
                ? 0
                : currentCounter;
    }

    synchronized boolean active() {
        return enabled;
    }
}
