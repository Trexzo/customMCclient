package dev.trexzo.custommc.platform.v1_8_9;

/** Strictly CI-only observation window, never permission to click. */
final class Minecraft189ScaffoldKeyboardWalkDiagnosticsGate {
    private Minecraft189ScaffoldKeyboardWalkDiagnosticsGate() {}
    static boolean waitingForOriginalNextController(
            final int receipts, final int attempts, final int quota) {
        if (quota < 2 || quota > 6 || receipts <= 0
                || receipts >= quota || receipts != attempts) return false;
        return receipts == 1
                || (receipts == 4 && quota >= 5)
                || (receipts == 5 && quota == 6);
    }
}
