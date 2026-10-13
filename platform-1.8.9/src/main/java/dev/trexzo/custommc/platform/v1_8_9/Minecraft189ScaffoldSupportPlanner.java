package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Bounded, read-only selection of a support face for a future Scaffold action.
 *
 * No runtime adapter is installed in V13: the caller MUST supply original-world
 * loaded-chunk/block-state provenance and a verified native ItemBlock/ItemStack.
 * This class never clicks, mutates inventory, constructs packets or places blocks.
 */
final class Minecraft189ScaffoldSupportPlanner {
    enum Cell {
        UNKNOWN, REPLACEABLE, FULL_SOLID, OTHER
    }

    enum Face {
        UP, EAST, WEST, SOUTH, NORTH
    }

    interface WorldEvidence {
        /** Source-native generation of the observed world, never a tick guess. */
        long generation();
        /** UNKNOWN for unloaded chunk, absent/native-incomplete evidence or stale state. */
        Cell cell(int x, int y, int z);
    }

    static final class Candidate {
        final long generation;
        final int targetX, targetY, targetZ;
        final int supportX, supportY, supportZ;
        final Face face;
        final double hitX, hitY, hitZ;

        private Candidate(long generation, int targetX, int targetY, int targetZ,
                          int supportX, int supportY, int supportZ,
                          Face face, double hitX, double hitY, double hitZ) {
            this.generation = generation;
            this.targetX = targetX;
            this.targetY = targetY;
            this.targetZ = targetZ;
            this.supportX = supportX;
            this.supportY = supportY;
            this.supportZ = supportZ;
            this.face = face;
            this.hitX = hitX;
            this.hitY = hitY;
            this.hitZ = hitZ;
        }
    }

    private Minecraft189ScaffoldSupportPlanner() { }

    /**
     * Return exactly one supported candidate under the actual local player's
     * feet, or null. The world and item facts are REQUIRED input, not inferred.
     * On-ground-only first milestone: airborne/tower behavior is unsupported.
     */
    static Candidate choose(WorldEvidence world, long expectedGeneration,
                            boolean nativeLocalPlayer, boolean onGround,
                            double feetX, double feetY, double feetZ,
                            double eyeY, boolean nativeItemBlock,
                            int nativeStackSize, double nativeReach) {
        if (world == null || expectedGeneration <= 0L
                || !nativeLocalPlayer || !onGround || !nativeItemBlock
                || nativeStackSize <= 0 || !Double.isFinite(feetX)
                || !Double.isFinite(feetY) || !Double.isFinite(feetZ)
                || !Double.isFinite(eyeY) || !Double.isFinite(nativeReach)
                || nativeReach <= 0.0D || nativeReach > 4.5D
                || Math.abs(feetX) > 29999990.0D
                || Math.abs(feetZ) > 29999990.0D
                || feetY < 2.0D || feetY >= 255.0D
                || eyeY < feetY || eyeY > feetY + 2.2D)
            return null;
        int x = (int)Math.floor(feetX);
        int y = (int)Math.floor(feetY) - 1;
        int z = (int)Math.floor(feetZ);
        if (y < 1 || y > 253) return null;
        try {
            if (world.generation() != expectedGeneration
                    || world.cell(x, y, z) != Cell.REPLACEABLE)
                return null;
            // Stable deterministic order: underneath first, then X-, X+, Z-, Z+.
            int[][] neighbours = {
                {x, y - 1, z},
                {x - 1, y, z}, {x + 1, y, z},
                {x, y, z - 1}, {x, y, z + 1}
            };
            Face[] faces = {Face.UP, Face.EAST, Face.WEST, Face.SOUTH, Face.NORTH};
            for (int i = 0; i < neighbours.length; i++) {
                int[] p = neighbours[i];
                if (world.cell(p[0], p[1], p[2]) != Cell.FULL_SOLID)
                    continue;
                // Original BlockPos/EnumFacing side-center hit; the final ray
                // and native server confirmation are deliberately future gates.
                double hx = p[0] + 0.5D, hy = p[1] + 0.5D, hz = p[2] + 0.5D;
                switch (faces[i]) {
                    case UP:    hy = p[1] + 1.0D; break;
                    case EAST:  hx = p[0] + 1.0D; break;
                    case WEST:  hx = p[0]; break;
                    case SOUTH: hz = p[2] + 1.0D; break;
                    case NORTH: hz = p[2]; break;
                    default: return null;
                }
                double dx = hx - feetX, dy = hy - eyeY, dz = hz - feetZ;
                if (dx * dx + dy * dy + dz * dz > nativeReach * nativeReach)
                    continue;
                if (world.generation() != expectedGeneration) return null;
                return new Candidate(expectedGeneration, x, y, z,
                        p[0], p[1], p[2], faces[i], hx, hy, hz);
            }
        } catch (RuntimeException unsupportedNativeRead) {
            // Unavailable world/chunk evidence MUST veto a candidate.
            return null;
        }
        return null;
    }
}
