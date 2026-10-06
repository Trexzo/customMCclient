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
    private long lastTimestamp = Long.MIN_VALUE;

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
        final Deque<Long> clicks =
                clicks(legacyButton);
        final long now =
                nanoClock.getAsLong();
        requireMonotonic(now);
        lastTimestamp = now;
        trim(
                leftClicks,
                now);
        trim(
                rightClicks,
                now);
        return clicks.size();
    }

    public synchronized void clear() {
        leftClicks.clear();
        rightClicks.clear();
        lastTimestamp = Long.MIN_VALUE;
    }

    private static void trim(
            final Deque<Long> clicks,
            final long now) {
        final long threshold =
                now - WINDOW_NANOS;
        while (!clicks.isEmpty()
                && clicks.peekFirst() <= threshold) {
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

    private void requireMonotonic(
            final long now) {
        if (lastTimestamp != Long.MIN_VALUE
                && now < lastTimestamp) {
            throw new IllegalStateException(
                    "click clock moved backwards");
        }
    }
}
