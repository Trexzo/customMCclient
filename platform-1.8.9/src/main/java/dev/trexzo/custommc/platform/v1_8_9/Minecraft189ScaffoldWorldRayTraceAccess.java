package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Read-only World.rayTraceBlocks -> original MovingObjectPosition authority.
 *
 * The source-original implementation can return true only after verifying
 * original typeOfHit BLOCK, exact original BlockPos and EnumFacing.getIndex.
 * Native hit coordinates are supplied externally and must be independently
 * range/finite validated before invocation. No packet or block mutation.
 */
public interface Minecraft189ScaffoldWorldRayTraceAccess {
    default boolean customMcScaffoldRayTraceSupport(
            double startX, double startY, double startZ,
            double endX, double endY, double endZ,
            int supportX, int supportY, int supportZ, int faceIndex) {
        return false;
    }
}
