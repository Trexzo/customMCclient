package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;

public final class Minecraft189FreezeModule
        implements Module {
    public static final String ID =
            "movement.freeze";

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
            final Minecraft189PlayerMotionControl player) {
        if (!enabled || player == null) {
            return;
        }

        if (Double.compare(
                player.customMcMotionX(),
                0.0D) != 0) {
            player.customMcSetMotionX(
                    0.0D);
        }
        if (Double.compare(
                player.customMcMotionY(),
                0.0D) != 0) {
            player.customMcSetMotionY(
                    0.0D);
        }
        if (Double.compare(
                player.customMcMotionZ(),
                0.0D) != 0) {
            player.customMcSetMotionZ(
                    0.0D);
        }
    }

    synchronized boolean active() {
        return enabled;
    }
}
