package dev.trexzo.custommc.platform.v1_8_9;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.function.LongSupplier;

public final class Minecraft189ClickRateTracker {
    public static final int LEFT_BUTTON = 0;
    public static final int RIGHT_BUTTON = 1;

    private static final long WINDOW_NANOS =
            1_000_000_000L;

    private final LongSupplier nanoClock;
    private final Deque<Long> leftClicks =
            new ArrayDeque<Long>();
    private final Deque<Long> rightClicks =
            new ArrayDeque<Long>();
    // System.nanoTime() has an arbitrary signed origin. MIN_VALUE is a
    // valid real timestamp, never a reliable "not yet sampled" marker.
    private long lastTimestamp;
    private boolean lastTimestampAvailable;

    public Minecraft189ClickRateTracker() {
        this(System::nanoTime);
    }

    Minecraft189ClickRateTracker(
            final LongSupplier nanoClock) {
        this.nanoClock =
                Objects.requireNonNull(
                        nanoClock,
                        "nanoClock");
    }

    public synchronized void recordPress(
            final int legacyButton) {
        final Deque<Long> clicks =
                clicks(legacyButton);
        final long now =
                nanoClock.getAsLong();
        requireMonotonic(now);
        lastTimestamp = now;
        lastTimestampAvailable = true;
        clicks.addLast(now);
        trim(
                leftClicks,
                now);
        trim(
                rightClicks,
                now);
    }

    public synchronized int clicksPerSecond(
            final int legacyButton) {
        final Rates rates =
                snapshot();
        if (legacyButton == LEFT_BUTTON) {
            return rates.left();
        }
        if (legacyButton == RIGHT_BUTTON) {
            return rates.right();
        }
        throw new IllegalArgumentException(
                "CPS supports only left/right mouse buttons");
    }

    public synchronized Rates snapshot() {
        final long now =
                nanoClock.getAsLong();
        requireMonotonic(now);
        lastTimestamp = now;
        lastTimestampAvailable = true;
        trim(
                leftClicks,
                now);
        trim(
                rightClicks,
                now);
        return new Rates(
                leftClicks.size(),
                rightClicks.size());
    }

    public synchronized void clear() {
        leftClicks.clear();
        rightClicks.clear();
        lastTimestamp = 0L;
        lastTimestampAvailable = false;
    }

    private static void trim(
            final Deque<Long> clicks,
            final long now) {
        // Subtract the two genuine System.nanoTime values, then compare
        // unsigned elapsed nanos with 1 second. Calculating a signed
        // absolute threshold (now - 1_000_000_000L) overflows for an
        // ordinary negative nanoTime close to Long.MIN_VALUE and silently
        // expires valid clicks. The caller already verifies that signed
        // time never moves backwards; large forward jumps also expire.
        while (!clicks.isEmpty()
                && Long.compareUnsigned(now - clicks.peekFirst(),
                        WINDOW_NANOS) >= 0) {
            clicks.removeFirst();
        }
    }

    private static Deque<Long> clicks(
            final int legacyButton,
            final Deque<Long> left,
            final Deque<Long> right) {
        if (legacyButton == LEFT_BUTTON) {
            return left;
        }
        if (legacyButton == RIGHT_BUTTON) {
            return right;
        }
        throw new IllegalArgumentException(
                "CPS supports only left/right mouse buttons");
    }

    private Deque<Long> clicks(
            final int legacyButton) {
        return clicks(
                legacyButton,
                leftClicks,
                rightClicks);
    }

    public static final class Rates {
        private final int left;
        private final int right;

        private Rates(
                final int left,
                final int right) {
            this.left = left;
            this.right = right;
        }

        public int left() {
            return left;
        }

        public int right() {
            return right;
        }
    }

    private void requireMonotonic(
            final long now) {
        if (lastTimestampAvailable && now < lastTimestamp) {
            throw new IllegalStateException(
                    "click clock moved backwards");
        }
    }
}
