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
    /** Optional acceptance-only diagnosis; production never uses it.
     * 20=provider-null,21=provided-chunk-null,22=EmptyChunk dummy,
     * 23=secondary loaded-veto,11=air,12=null-state,13=null-block,
     * 14=full-solid,15=other; -1=unsupported source.
     */
    default int customMcScaffoldCellTrace(int x, int y, int z) { return -1; }
}
