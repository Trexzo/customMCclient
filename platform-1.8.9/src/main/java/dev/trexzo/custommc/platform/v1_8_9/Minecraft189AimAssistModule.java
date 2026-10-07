package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.Module;

public final class Minecraft189AimAssistModule
        implements Module {
    public static final String ID =
            "combat.aimAssist";

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

    synchronized boolean apply(
            final Minecraft189PlayerRotationControl player,
            final Minecraft189PlayerRotationState.Snapshot rotation,
            final Minecraft189TargetRotationState.Snapshot target,
            final boolean leftButtonHeld) {
        if (!enabled
                || player == null
                || rotation == null
                || !rotation.available()
                || target == null
                || !target.available()
                || !leftButtonHeld) {
            return false;
        }

        final float targetYaw =
                target.yaw();
        final float targetPitch =
                target.pitch();

        if (Float.compare(
                rotation.yaw(),
                targetYaw) != 0) {
            player.customMcSetRotationYaw(
                    targetYaw);
        }
        if (Float.compare(
                rotation.pitch(),
                targetPitch) != 0) {
            player.customMcSetRotationPitch(
                    targetPitch);
        }

        return true;
    }

    synchronized boolean active() {
        return enabled;
    }
}
