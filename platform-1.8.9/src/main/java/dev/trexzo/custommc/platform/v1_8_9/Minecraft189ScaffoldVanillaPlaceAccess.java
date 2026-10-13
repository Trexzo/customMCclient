package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Original Minecraft 1.8.9 PlayerControllerMP.onPlayerRightClick only.
 *
 * Implemented by the transformed original bda; every Object argument must
 * be an actual source-original EntityPlayerSP, WorldClient, selected
 * ItemStack and MovingObjectPosition. No fabricated BlockPos/EnumFacing/
 * Vec3 and no handwritten packet path. The default fails closed.
 *
 * The method HAS a world/inventory side effect. It must only be invoked by
 * a separately validated current-tick source-gated action owner.
 */
public interface Minecraft189ScaffoldVanillaPlaceAccess {
    default boolean customMcOriginalRightClick(Object originalPlayer,
            Object originalWorld, Object selectedStack, Object originalHit) {
        return false;
    }
}
