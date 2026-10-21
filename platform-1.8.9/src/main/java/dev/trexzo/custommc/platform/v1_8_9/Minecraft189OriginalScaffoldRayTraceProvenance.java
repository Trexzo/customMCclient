package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Original 1.8.9 V19 ONLY: records outcomes of the ALREADY EXECUTED source
 * World cell/ray callbacks. It cannot access game objects, decide acceptance,
 * invoke a ray trace or mutate original World/player state.
 */
final class Minecraft189OriginalScaffoldRayTraceProvenance {
    private int sourceCallbacks;
    private int missingInterfaces;
    private int worldCellChecks;
    private int fullSolidCells;
    private int unavailableOrOtherCells;
    private int correctFaceRayCalls;
    private int correctFaceHits;
    private int wrongFaceHits;
    private int wrongBlockHits;
    private int lastCellCode = Integer.MIN_VALUE;
    private String lastDecision = "not-observed";

    synchronized void reset() {
        sourceCallbacks = missingInterfaces = worldCellChecks = 0;
        fullSolidCells = unavailableOrOtherCells = 0;
        correctFaceRayCalls = correctFaceHits = 0;
        wrongFaceHits = wrongBlockHits = 0;
        lastCellCode = Integer.MIN_VALUE;
        lastDecision = "not-observed";
    }

    synchronized void observeInterfaces(final boolean read, final boolean ray) {
        sourceCallbacks++;
        if (!read || !ray) {
            missingInterfaces++;
            lastDecision = !read ? "original-world-cell-interface-unavailable"
                    : "original-world-ray-interface-unavailable";
        }
    }

    synchronized void observeCell(final int originalCellCode,
            final int expectedFullSolid) {
        worldCellChecks++;
        lastCellCode = originalCellCode;
        if (originalCellCode == expectedFullSolid) {
            fullSolidCells++;
            lastDecision = "original-client-stone-full-solid";
        } else {
            unavailableOrOtherCells++;
            lastDecision = "original-client-cell-not-full-solid";
        }
    }

    synchronized void observeRayResults(final boolean correctFace,
            final boolean wrongFace, final boolean wrongBlock) {
        // Only after the original three real native ray callbacks returned.
        correctFaceRayCalls++;
        if (correctFace) correctFaceHits++;
        if (wrongFace) wrongFaceHits++;
        if (wrongBlock) wrongBlockHits++;
        lastDecision = wrongFace || wrongBlock
                ? "original-negative-ray-accepted-unexpectedly"
                : correctFace
                    ? "original-correct-ray-up-face"
                    : "original-correct-ray-up-face-missed";
    }

    synchronized Snapshot snapshot() {
        return new Snapshot(sourceCallbacks, missingInterfaces,
                worldCellChecks, fullSolidCells, unavailableOrOtherCells,
                correctFaceRayCalls, correctFaceHits, wrongFaceHits,
                wrongBlockHits, lastCellCode, lastDecision);
    }

    static final class Snapshot {
        final int sourceCallbacks;
        final int missingInterfaces;
        final int worldCellChecks;
        final int fullSolidCells;
        final int unavailableOrOtherCells;
        final int correctFaceRayCalls;
        final int correctFaceHits;
        final int wrongFaceHits;
        final int wrongBlockHits;
        final int lastCellCode;
        final String lastDecision;

        Snapshot(int callbacks, int missing, int cells, int solid,
                int other, int rayCalls, int correct, int wrongFace,
                int wrongBlock, int cellCode, String decision) {
            sourceCallbacks = callbacks;
            missingInterfaces = missing;
            worldCellChecks = cells;
            fullSolidCells = solid;
            unavailableOrOtherCells = other;
            correctFaceRayCalls = rayCalls;
            correctFaceHits = correct;
            wrongFaceHits = wrongFace;
            wrongBlockHits = wrongBlock;
            lastCellCode = cellCode;
            lastDecision = decision;
        }
    }
}
