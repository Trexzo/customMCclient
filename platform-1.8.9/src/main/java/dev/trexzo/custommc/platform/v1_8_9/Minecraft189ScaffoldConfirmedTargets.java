package dev.trexzo.custommc.platform.v1_8_9;

/**
 * V57 bounded, primitive-only history of genuinely RECEIPTED original
 * Scaffold targets from exactly one module activation.
 *
 * A candidate from step 3 may not reuse step 1 after a server rollback,
 * manual block break or original-world state change. Checking only the
 * immediately previous target would admit the A -> B -> A case.
 *
 * No Minecraft objects, player references, packets or native controller
 * actions are ever retained here. Receipts are still proven by the caller.
 */
final class Minecraft189ScaffoldConfirmedTargets {
    static final int LIMIT = 8;

    private final int[] x = new int[LIMIT];
    private final int[] y = new int[LIMIT];
    private final int[] z = new int[LIMIT];
    private int count;

    boolean contains(final int targetX, final int targetY, final int targetZ) {
        for (int index = 0; index < count; index++) {
            if (x[index] == targetX && y[index] == targetY
                    && z[index] == targetZ)
                return true;
        }
        return false;
    }

    /**
     * Called only after a positive original WorldClient + inventory debit
     * receipt. Returning false must invalidate the whole module sequence.
     */
    boolean recordReceipt(final int targetX, final int targetY,
            final int targetZ) {
        if (count >= LIMIT || contains(targetX, targetY, targetZ))
            return false;
        x[count] = targetX;
        y[count] = targetY;
        z[count] = targetZ;
        count++;
        return true;
    }

    int count() { return count; }

    void reset() {
        for (int index = 0; index < count; index++)
            x[index] = y[index] = z[index] = 0;
        count = 0;
    }
}
