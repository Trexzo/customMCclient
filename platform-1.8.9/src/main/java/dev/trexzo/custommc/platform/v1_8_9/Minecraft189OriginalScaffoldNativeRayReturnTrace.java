package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Records ONLY the return branch of the already-executed transformed vanilla
 * World.rayTraceBlocks call. Does not call World, ray trace, or mutate a hit.
 *
 * The original obfuscated World class must be able to invoke this public
 * static helper. No diagnostic result authorizes Scaffold or a V19 PASS.
 */
public final class Minecraft189OriginalScaffoldNativeRayReturnTrace {
    public static final int MISS = 1;
    public static final int WRONG_TYPE = 2;
    public static final int MISSING_FACE = 3;
    public static final int WRONG_FACE = 4;
    public static final int WRONG_BLOCK = 5;
    public static final int EXACT_MATCH = 6;

    private static int sourceCalls;
    private static int expectedUpCalls;
    private static int miss;
    private static int wrongType;
    private static int missingFace;
    private static int wrongFace;
    private static int wrongBlock;
    private static int exactMatch;
    private static int lastCode;

    private Minecraft189OriginalScaffoldNativeRayReturnTrace() { }

    public static boolean recordResult(
            final int reason, final int requestedFace,
            final int supportX, final int supportY, final int supportZ,
            final boolean actualOriginalReturn) {
        if (!Boolean.getBoolean(
                "custommc.acceptance.requireNativeScaffoldRayTrace"))
            return actualOriginalReturn;
        // Count only the *expected positive* ray invoked by the genuine V19
        // source acceptance; other deliberate negative calls do not pollute
        // the reasons we are trying to distinguish.
        if (requestedFace != 1 || supportX != 0
                || supportY != 99 || supportZ != 0)
            return actualOriginalReturn;
        synchronized (Minecraft189OriginalScaffoldNativeRayReturnTrace.class) {
            sourceCalls++;
            expectedUpCalls++;
            lastCode = reason;
            switch (reason) {
                case MISS: miss++; break;
                case WRONG_TYPE: wrongType++; break;
                case MISSING_FACE: missingFace++; break;
                case WRONG_FACE: wrongFace++; break;
                case WRONG_BLOCK: wrongBlock++; break;
                case EXACT_MATCH: exactMatch++; break;
                default: throw new IllegalArgumentException(
                        "unexpected native World.rayTraceBlocks return branch");
            }
        }
        return actualOriginalReturn;
    }

    static synchronized void reset() {
        sourceCalls = expectedUpCalls = miss = wrongType = missingFace = 0;
        wrongFace = wrongBlock = exactMatch = lastCode = 0;
    }

    static synchronized Snapshot snapshot() {
        return new Snapshot(sourceCalls, expectedUpCalls,
                miss, wrongType, missingFace, wrongFace, wrongBlock,
                exactMatch, lastCode);
    }

    static final class Snapshot {
        final int sourceCalls;
        final int expectedUpCalls;
        final int miss;
        final int wrongType;
        final int missingFace;
        final int wrongFace;
        final int wrongBlock;
        final int exactMatch;
        final int lastCode;

        Snapshot(int sourceCalls, int expectedUpCalls, int miss,
                int wrongType, int missingFace, int wrongFace,
                int wrongBlock, int exactMatch, int lastCode) {
            this.sourceCalls = sourceCalls;
            this.expectedUpCalls = expectedUpCalls;
            this.miss = miss;
            this.wrongType = wrongType;
            this.missingFace = missingFace;
            this.wrongFace = wrongFace;
            this.wrongBlock = wrongBlock;
            this.exactMatch = exactMatch;
            this.lastCode = lastCode;
        }
    }
}
