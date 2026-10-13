package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Read-only bridge between a source-transformed original World and V13's
 * conservative planner. The owner must invalidate on world unload/switch;
 * epoch is assigned by the caller and not fabricated from a world hash.
 */
final class Minecraft189ScaffoldNativeWorldEvidence
        implements Minecraft189ScaffoldSupportPlanner.WorldEvidence, AutoCloseable {
    private final Minecraft189ScaffoldWorldReadAccess source;
    private final long epoch;
    private boolean closed;

    Minecraft189ScaffoldNativeWorldEvidence(
            Minecraft189ScaffoldWorldReadAccess source, long epoch) {
        if (source == null || epoch <= 0L)
            throw new IllegalArgumentException("missing native World or epoch");
        this.source = source;
        this.epoch = epoch;
    }

    @Override public synchronized long generation() {
        return closed ? -1L : epoch;
    }

    @Override public synchronized Minecraft189ScaffoldSupportPlanner.Cell cell(
            int x, int y, int z) {
        if (closed || y < 0 || y > 255
                || x < -29999984 || x > 29999984
                || z < -29999984 || z > 29999984)
            return Minecraft189ScaffoldSupportPlanner.Cell.UNKNOWN;
        try {
            final int value = source.customMcScaffoldCellCode(x, y, z);
            switch (value) {
                case Minecraft189ScaffoldWorldReadAccess.AIR:
                    return Minecraft189ScaffoldSupportPlanner.Cell.REPLACEABLE;
                case Minecraft189ScaffoldWorldReadAccess.FULL_SOLID:
                    return Minecraft189ScaffoldSupportPlanner.Cell.FULL_SOLID;
                case Minecraft189ScaffoldWorldReadAccess.OTHER:
                    return Minecraft189ScaffoldSupportPlanner.Cell.OTHER;
                default:
                    return Minecraft189ScaffoldSupportPlanner.Cell.UNKNOWN;
            }
        } catch (RuntimeException nativeWorldUnavailable) {
            return Minecraft189ScaffoldSupportPlanner.Cell.UNKNOWN;
        }
    }

    @Override public synchronized void close() {
        closed = true;
    }
}
