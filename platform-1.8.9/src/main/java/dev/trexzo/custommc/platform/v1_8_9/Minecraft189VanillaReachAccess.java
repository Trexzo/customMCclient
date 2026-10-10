package dev.trexzo.custommc.platform.v1_8_9;

/** Read-only native 1.8.9 PlayerControllerMP reach facts. */
public interface Minecraft189VanillaReachAccess {
    /** Native PlayerControllerMP.getBlockReachDistance() result. */
    float customMcVanillaBlockReachDistance();
    /** Native PlayerControllerMP.extendedReach() result. */
    boolean customMcVanillaExtendedReach();
}
