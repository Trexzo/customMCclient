package dev.trexzo.custommc.platform.v1_8_9;

/** Read-only source-mapped 1.8.9 Entity AABB in world coordinates. */
public interface Minecraft189EntityHitboxBoundsAccess {
    /** [minX, minY, minZ, maxX, maxY, maxZ], or null without a box. */
    double[] customMcHitboxBounds();
}
