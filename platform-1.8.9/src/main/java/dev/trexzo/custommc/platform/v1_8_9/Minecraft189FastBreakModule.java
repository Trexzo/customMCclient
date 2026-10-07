package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;

public final class Minecraft189FastBreakModule
        implements Module {
    public static final String ID =
            "player.fastBreak";

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
            final Minecraft189BlockHitDelayControl controller) {
        if (!enabled || controller == null) {
            return;
        }
        controller.customMcSetBlockHitDelay(0);
    }

    synchronized boolean active() {
        return enabled;
    }
}
