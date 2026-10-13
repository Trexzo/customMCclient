package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Native-source-only vanilla placement bridge on transformed bda.
 * All arguments must be original Mojang 1.8.9 references of the exact
 * verified method signature. No caller is wired in V28; default fails shut.
 * Do not call without the current World/ItemBlock/crosshair ownership gate.
 */
public interface Minecraft189ScaffoldNativePlacementAccess {
    default boolean customMcOriginalControllerPlace(
            Object localPlayer, Object originalWorld, Object selectedItemStack,
            Object clickedBlockPos, Object originalSideHit, Object originalHitVec) {
        return false;
    }
}
