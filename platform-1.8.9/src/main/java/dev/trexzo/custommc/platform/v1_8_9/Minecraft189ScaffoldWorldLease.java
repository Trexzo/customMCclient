package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Single-owner read lease for the original, transformed Minecraft World.
 *
 * A lease is valid only during the same host callback that opened it. Callers
 * must explicitly end the lease before advancing a tick, changing worlds,
 * disconnecting or replacing the player. No client packet or world mutation.
 */
final class Minecraft189ScaffoldWorldLease implements AutoCloseable {
    private Minecraft189ScaffoldWorldReadAccess current;
    private Minecraft189ScaffoldNativeWorldEvidence evidence;
    private long nextEpoch;
    private boolean closed;

    synchronized Minecraft189ScaffoldSupportPlanner.WorldEvidence open(
            Minecraft189ScaffoldWorldReadAccess world) {
        invalidate();
        if (closed || world == null) return null;
        if (nextEpoch == Long.MAX_VALUE) {
            closed = true;
            return null;
        }
        final long epoch = ++nextEpoch;
        current = world;
        evidence = new Minecraft189ScaffoldNativeWorldEvidence(world, epoch);
        return evidence;
    }

    synchronized Minecraft189ScaffoldSupportPlanner.Candidate choose(
            Minecraft189ScaffoldWorldReadAccess world,
            boolean originalLocalPlayer, boolean onGround,
            double feetX, double feetY, double feetZ,
            double eyeY, boolean nativeBlockItem, int stackSize, double reach) {
        if (closed || world == null || world != current || evidence == null)
            return null;
        final long epoch = evidence.generation();
        if (epoch <= 0) return null;
        final Minecraft189ScaffoldSupportPlanner.Candidate candidate =
                Minecraft189ScaffoldSupportPlanner.choose(
                        evidence, epoch, originalLocalPlayer, onGround,
                        feetX, feetY, feetZ, eyeY, nativeBlockItem,
                        stackSize, reach);
        return candidate != null && evidence.generation() == epoch
                && world == current ? candidate : null;
    }

    /** V63 current-callback-only edge candidate from the REAL MOP side hit. */
    synchronized Minecraft189ScaffoldSupportPlanner.Candidate chooseNativeAdjacent(
            final Minecraft189ScaffoldWorldReadAccess world,
            final boolean nativeLocalPlayer, final boolean onGround,
            final double feetX, final double feetY, final double feetZ,
            final double eyeY, final boolean nativeItemBlock,
            final int stackSize, final double reach, final double[] sourceHit) {
        if (closed || world == null || world != current || evidence == null)
            return null;
        final long epoch = evidence.generation();
        if (epoch <= 0L) return null;
        final Minecraft189ScaffoldSupportPlanner.Candidate result =
                Minecraft189ScaffoldSupportPlanner.chooseNativeAdjacent(
                        evidence, epoch, nativeLocalPlayer, onGround,
                        feetX, feetY, feetZ, eyeY, nativeItemBlock,
                        stackSize, reach, sourceHit);
        return result != null && world == current
                && evidence.generation() == epoch ? result : null;
    }

    synchronized void invalidate() {
        if (evidence != null) evidence.close();
        evidence = null;
        current = null;
    }

    @Override public synchronized void close() {
        invalidate();
        closed = true;
    }
}
