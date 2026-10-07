package dev.trexzo.custommc.platform.v1_8_9;

public interface Minecraft189PlayerRotationAccess {
    float customMcRotationYaw();

    default float customMcRotationPitch() {
        return 0.0F;
    }
}
