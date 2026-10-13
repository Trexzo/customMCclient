package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Read-only, conservative check against the ORIGINAL Minecraft 1.8.9 ray-hit.
 * A candidate face must be genuinely targeted by the camera; the planner's
 * proposed face-center alone never authorizes native controller placement.
 * This is an acceptance-only prerequisite, not an automatic placement path.
 */
final class Minecraft189ScaffoldNativeFaceGate {
    private Minecraft189ScaffoldNativeFaceGate() { }

    static boolean matches(
            Minecraft189ScaffoldNativeBlockHitAccess originalHit,
            Minecraft189ScaffoldSupportPlanner.Candidate candidate,
            double sourceEyeX, double sourceEyeY, double sourceEyeZ,
            double nativeReach) {
        if (originalHit == null || candidate == null
                || !Double.isFinite(sourceEyeX)
                || !Double.isFinite(sourceEyeY)
                || !Double.isFinite(sourceEyeZ)
                || !Double.isFinite(nativeReach)
                || nativeReach <= 0 || nativeReach > 4.5D)
            return false;
        final double[] hit;
        try {
            hit = originalHit.customMcScaffoldNativeBlockHit();
        } catch (RuntimeException unavailableNativeRay) {
            return false;
        }
        if (hit == null || hit.length != 8 || hit[0] != 1.0D)
            return false;
        for (double value : hit)
            if (!Double.isFinite(value)) return false;
        if (hit[1] != candidate.supportX || hit[2] != candidate.supportY
                || hit[3] != candidate.supportZ)
            return false;
        // Original vanilla EnumFacing.getIndex: DOWN=0, UP=1, NORTH=2,
        // SOUTH=3, WEST=4, EAST=5. No guessed face ordinal.
        final int expectedFace;
        switch (candidate.face) {
            case UP:    expectedFace = 1; break;
            case NORTH: expectedFace = 2; break;
            case SOUTH: expectedFace = 3; break;
            case WEST:  expectedFace = 4; break;
            case EAST:  expectedFace = 5; break;
            default: return false;
        }
        if (hit[4] != expectedFace) return false;
        final double epsilon = 0.003D;
        final double hx = hit[5], hy = hit[6], hz = hit[7];
        if (hx < candidate.supportX - epsilon
                || hx > candidate.supportX + 1.0D + epsilon
                || hy < candidate.supportY - epsilon
                || hy > candidate.supportY + 1.0D + epsilon
                || hz < candidate.supportZ - epsilon
                || hz > candidate.supportZ + 1.0D + epsilon)
            return false;
        final double plane;
        switch (candidate.face) {
            case UP:    plane = candidate.supportY + 1.0D - hy; break;
            case NORTH: plane = candidate.supportZ - hz; break;
            case SOUTH: plane = candidate.supportZ + 1.0D - hz; break;
            case WEST:  plane = candidate.supportX - hx; break;
            case EAST:  plane = candidate.supportX + 1.0D - hx; break;
            default: return false;
        }
        if (Math.abs(plane) > epsilon) return false;
        final double dx = hx - sourceEyeX;
        final double dy = hy - sourceEyeY;
        final double dz = hz - sourceEyeZ;
        return dx * dx + dy * dy + dz * dz
                <= nativeReach * nativeReach;
    }
}
