package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Primitive-only target uniqueness lease for one bounded Scaffold activation.
 *
 * Recording occurs ONLY after the exact original WorldClient/inventory receipt.
 * Unlike comparing solely to the last target, this rejects A -> B -> A
 * when a previously confirmed block becomes a candidate again (e.g. after
 * server rollback, concurrent block removal or client/world desynchronization).
 * No native World, player, BlockPos, controller or mutable source references
 * are stored here.
 */
final class Minecraft189ScaffoldTargetHistory {
    private static final int CAPACITY = 8;
    private final int[] xs = new int[CAPACITY];
    private final int[] ys = new int[CAPACITY];
    private final int[] zs = new int[CAPACITY];
    private int count;

    boolean contains(final int x, final int y, final int z) {
        for (int i = 0; i < count; i++) {
            if (xs[i] == x && ys[i] == y && zs[i] == z)
                return true;
        }
        return false;
    }

    boolean recordConfirmed(final int x, final int y, final int z) {
        if (contains(x, y, z) || count >= CAPACITY)
            return false;
        xs[count] = x;
        ys[count] = y;
        zs[count] = z;
        count++;
        return true;
    }

    int size() { return count; }

    void clear() {
        // Zero every recorded source-derived coordinate when the owner
        // invalidates the activation, rather than carrying target history
        // into a new enable/session.
        for (int i = 0; i < count; i++) {
            xs[i] = 0;
            ys[i] = 0;
            zs[i] = 0;
        }
        count = 0;
    }
}
