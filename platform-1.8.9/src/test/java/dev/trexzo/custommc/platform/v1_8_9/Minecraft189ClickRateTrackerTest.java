package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;

import java.util.function.LongSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class Minecraft189ClickRateTrackerTest {
    @Test
    void rollingWindowTracksLeftAndRightIndependently() {
        final MutableClock clock =
                new MutableClock();
        final Minecraft189ClickRateTracker tracker =
                new Minecraft189ClickRateTracker(clock);

        press(
                tracker,
                clock,
                Minecraft189ClickRateTracker.LEFT_BUTTON,
                0L);
        press(
                tracker,
                clock,
                Minecraft189ClickRateTracker.LEFT_BUTTON,
                200_000_000L);
        press(
                tracker,
                clock,
                Minecraft189ClickRateTracker.RIGHT_BUTTON,
                400_000_000L);

        Minecraft189ClickRateTracker.Rates rates =
                tracker.snapshot();
        assertEquals(
                2,
                rates.left());
        assertEquals(
                1,
                rates.right());

        clock.now = 1_000_000_000L;
        rates = tracker.snapshot();
        assertEquals(
                1,
                rates.left());
        assertEquals(
                1,
                rates.right());

        clock.now = 1_400_000_000L;
        rates = tracker.snapshot();
        assertEquals(
                0,
                rates.left());
        assertEquals(
                0,
                rates.right());
    }

    @Test
    void unsupportedButtonsAndBackwardsClockFailExplicitly() {
        final MutableClock clock =
                new MutableClock();
        final Minecraft189ClickRateTracker tracker =
                new Minecraft189ClickRateTracker(clock);

        assertThrows(
                IllegalArgumentException.class,
                () -> tracker.recordPress(2));

        clock.now = 100L;
        tracker.recordPress(
                Minecraft189ClickRateTracker.LEFT_BUTTON);
        clock.now = 99L;

        assertThrows(
                IllegalStateException.class,
                tracker::snapshot);

        tracker.clear();
        assertEquals(
                0,
                tracker.snapshot()
                        .left());
    }

    private static void press(
            final Minecraft189ClickRateTracker tracker,
            final MutableClock clock,
            final int button,
            final long now) {
        clock.now = now;
        tracker.recordPress(button);
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
