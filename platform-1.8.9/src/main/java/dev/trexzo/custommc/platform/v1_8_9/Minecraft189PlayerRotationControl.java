package dev.trexzo.custommc.platform.v1_8_9;

public interface Minecraft189PlayerRotationControl
        extends Minecraft189PlayerRotationAccess {
    void customMcSetRotationYaw(float yaw);

    void customMcSetRotationPitch(float pitch);
}
