package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Pure primitive-only pacing and receipt transaction policy, now used by
 * the real source-native bounded production Scaffold repeat sequence.
 *
 * This class never places blocks or sends packets. The outer sequence must
 * revalidate a fresh original World/player/cursor/ItemBlock and one positive
 * client World/stack readback per admitted native controller call; independent
 * original dedicated-server stone/inventory proof remains the CI authority.
 *
 * One accepted controller call requires one positive receipt before another
 * call can be considered. False controller return, timeout, stale activation,
 * a bad tick, or a rejected receipt fail shut for the entire activation.
 */
final class Minecraft189ScaffoldPlacementCadence {
    private static final int MAX_BLOCKS = 8;
    private static final int MAX_GAP_TICKS = 100;
    private static final int RECEIPT_TIMEOUT_TICKS = 120;

    private long activation;
    // Never allow a completed/failed activation to be re-armed to bypass
    // attempt caps or a failed original controller/readback transaction.
    private long highestArmedActivation;
    private long dueTick;
    private long receiptDeadline;
    private long lastTick;
    private int gapTicks;
    private int maximumAttempts;
    private int attempts;
    private boolean pending;
    private boolean closed = true;

    synchronized boolean arm(final long sourceActivation,
            final long currentTick, final int requestedAttempts,
            final int minimumGapTicks) {
        invalidate();
        if (sourceActivation <= highestArmedActivation || currentTick < 0
                || requestedAttempts < 1 || requestedAttempts > MAX_BLOCKS
                || minimumGapTicks < 2 || minimumGapTicks > MAX_GAP_TICKS
                || currentTick > Long.MAX_VALUE - RECEIPT_TIMEOUT_TICKS)
            return false;
        activation = sourceActivation;
        highestArmedActivation = sourceActivation;
        dueTick = currentTick;
        lastTick = currentTick;
        gapTicks = minimumGapTicks;
        maximumAttempts = requestedAttempts;
        closed = false;
        return true;
    }

    private boolean validTick(final long sourceActivation, final long tick) {
        if (closed || sourceActivation != activation
                || tick < lastTick || tick < 0
                || tick > Long.MAX_VALUE - RECEIPT_TIMEOUT_TICKS) {
            invalidate();
            return false;
        }
        lastTick = tick;
        if (pending && tick > receiptDeadline) {
            invalidate();
            return false;
        }
        return true;
    }

    synchronized boolean mayAttempt(final long sourceActivation, final long tick) {
        return validTick(sourceActivation, tick)
                && !pending && attempts < maximumAttempts && tick >= dueTick;
    }

    synchronized boolean controllerAttempt(final long sourceActivation,
            final long tick, final boolean originalControllerAccepted) {
        if (!mayAttempt(sourceActivation, tick)) return false;
        attempts++;
        if (!originalControllerAccepted) {
            invalidate();
            return false;
        }
        pending = true;
        receiptDeadline = tick + RECEIPT_TIMEOUT_TICKS;
        dueTick = tick + gapTicks;
        return true;
    }

    synchronized boolean receipt(final long sourceActivation,
            final long tick, final boolean originalReadbackConfirmed) {
        if (!validTick(sourceActivation, tick)) return false;
        if (!pending) {
            invalidate();
            return false;
        }
        pending = false;
        if (!originalReadbackConfirmed) {
            invalidate();
            return false;
        }
        if (attempts >= maximumAttempts) {
            closed = true;
            return true;
        }
        dueTick = Math.max(dueTick, tick + gapTicks);
        return true;
    }

    synchronized boolean pending() { return pending && !closed; }
    synchronized boolean stopped() { return closed; }
    synchronized int attemptedCount() { return attempts; }

    synchronized void invalidate() {
        closed = true;
        pending = false;
        activation = 0L;
        dueTick = receiptDeadline = lastTick = 0L;
        gapTicks = maximumAttempts = attempts = 0;
    }
}
