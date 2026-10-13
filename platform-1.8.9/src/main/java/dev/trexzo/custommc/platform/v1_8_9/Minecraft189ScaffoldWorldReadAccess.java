package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Implemented solely by transformed original Minecraft 1.8.9 World (adm).
 * Read-only native BlockPos -> World.isBlockLoaded/isAirBlock/getBlockState
 * -> IBlockState.getBlock -> Block.isFullCube. Never writes to the world.
 */
public interface Minecraft189ScaffoldWorldReadAccess {
    int UNKNOWN = 0;
    int AIR = 1;
    int FULL_SOLID = 2;
    int OTHER = 3;

    /** Return only source-native cell evidence; UNKNOWN if not loaded. */
    int customMcScaffoldCellCode(int x, int y, int z);
}
