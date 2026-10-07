package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;

public final class Minecraft189NoWebModule
        implements Module {
    public static final String ID =
            "movement.noWeb";

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
            final Minecraft189PlayerWebControl player) {
        if (!enabled || player == null) {
            return;
        }

        if (player.customMcInWeb()) {
            player.customMcSetInWeb(false);
        }
    }

    synchronized boolean active() {
        return enabled;
    }
}
