package dev.trexzo.custommc.platform.v1_8_9;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.function.LongSupplier;

public final class Minecraft189FrameRateTracker {
    private static final long WINDOW_NANOS =
            1_000_000_000L;

    private final LongSupplier nanoClock;
    private final Deque<Long> frames =
            new ArrayDeque<Long>();
    private long lastTimestamp = Long.MIN_VALUE;

    public Minecraft189FrameRateTracker() {
        this(System::nanoTime);
    }

    Minecraft189FrameRateTracker(
            final LongSupplier nanoClock) {
        this.nanoClock =
                Objects.requireNonNull(
                        nanoClock,
                        "nanoClock");
    }

    public synchronized void frameStarted() {
        final long now =
                nanoClock.getAsLong();
        requireMonotonic(now);
        lastTimestamp = now;
        frames.addLast(now);
        trim(now);
    }

    public synchronized int framesPerSecond() {
        final long now =
                nanoClock.getAsLong();
        requireMonotonic(now);
        lastTimestamp = now;
        trim(now);
        return frames.size();
    }

    public synchronized void clear() {
        frames.clear();
        lastTimestamp = Long.MIN_VALUE;
    }

    private void trim(
            final long now) {
        final long threshold =
                now - WINDOW_NANOS;
        while (!frames.isEmpty()
                && frames.peekFirst() <= threshold) {
            frames.removeFirst();
        }
    }

    private void requireMonotonic(
            final long now) {
        if (lastTimestamp != Long.MIN_VALUE
                && now < lastTimestamp) {
            throw new IllegalStateException(
                    "frame clock moved backwards");
        }
    }
}
