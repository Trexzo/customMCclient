package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;

import java.util.function.LongSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class Minecraft189FrameRateTrackerTest {
    @Test
    void rollingOneSecondWindowAgesFramesOut() {
        final MutableClock clock =
                new MutableClock();
        final Minecraft189FrameRateTracker tracker =
                new Minecraft189FrameRateTracker(clock);

        frame(tracker, clock, 0L);
        frame(tracker, clock, 250_000_000L);
        frame(tracker, clock, 500_000_000L);
        frame(tracker, clock, 750_000_000L);

        assertEquals(
                4,
                tracker.framesPerSecond());

        clock.now = 1_000_000_000L;
        assertEquals(
                3,
                tracker.framesPerSecond());

        tracker.frameStarted();
        assertEquals(
                4,
                tracker.framesPerSecond());

        clock.now = 1_750_000_000L;
        assertEquals(
                1,
                tracker.framesPerSecond());
    }

    @Test
    void backwardsClockIsRejectedAndClearStartsFreshLifetime() {
        final MutableClock clock =
                new MutableClock();
        final Minecraft189FrameRateTracker tracker =
                new Minecraft189FrameRateTracker(clock);

        frame(tracker, clock, 100L);
        clock.now = 99L;

        assertThrows(
                IllegalStateException.class,
                tracker::framesPerSecond);

        tracker.clear();
        tracker.frameStarted();

        assertEquals(
                1,
                tracker.framesPerSecond());
    }

    private static void frame(
            final Minecraft189FrameRateTracker tracker,
            final MutableClock clock,
            final long now) {
        clock.now = now;
        tracker.frameStarted();
    }

    private static final class MutableClock
            implements LongSupplier {
        private long now;

        @Override
        public long getAsLong() {
            return now;
        }
    }
}
