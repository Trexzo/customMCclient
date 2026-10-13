package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Read-only, per-host lifecycle authority for original World cell evidence.
 * Epochs are monotonic owner values; never derived from time, hashCode,
 * dimension, player index, or a client-side guess about server state.
 *
 * No Scaffold module registration, movement, packets or world writes.
 */
final class Minecraft189ScaffoldWorldOwnership implements AutoCloseable {
    private Minecraft189ScaffoldWorldReadAccess source;
    private Minecraft189ScaffoldNativeWorldEvidence current;
    private long generation;
    private boolean closed;

    synchronized void observe(final Object world) {
        if (closed) return;
        final Minecraft189ScaffoldWorldReadAccess candidate =
                world instanceof Minecraft189ScaffoldWorldReadAccess
                        ? (Minecraft189ScaffoldWorldReadAccess) world : null;
        if (candidate == source) return;
        revoke();
        if (candidate == null) return;
        if (generation == Long.MAX_VALUE) {
            closed = true; // impossible to issue another unique generation
            return;
        }
        generation++;
        source = candidate;
        current = new Minecraft189ScaffoldNativeWorldEvidence(candidate, generation);
    }

    private void revoke() {
        if (current != null) current.close();
        current = null;
        source = null;
    }

    synchronized Minecraft189ScaffoldNativeWorldEvidence current() {
        return closed ? null : current;
    }

    synchronized boolean owns(final Object world) {
        return !closed && current != null && source == world
                && current.generation() == generation;
    }

    synchronized long generation() {
        return current != null && !closed ? generation : -1L;
    }

    @Override public synchronized void close() {
        if (closed) return;
        closed = true;
        revoke();
    }
}
