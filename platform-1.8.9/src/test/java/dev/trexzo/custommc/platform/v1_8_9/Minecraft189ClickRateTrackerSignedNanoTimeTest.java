package dev.trexzo.custommc.platform.v1_8_9;

import java.util.function.LongSupplier;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The real System.nanoTime() clock origin can be negative or MIN_VALUE.
 * These are offline source-clock regressions, not pinned Mojang runtime.
 */
final class Minecraft189ClickRateTrackerSignedNanoTimeTest {
    private static final long SECOND = 1_000_000_000L;

    private static final class Clock implements LongSupplier {
        long now;
        Clock(final long timestamp) { now = timestamp; }
        @Override public long getAsLong() { return now; }
    }
    private static Minecraft189ClickRateTracker tracker(final Clock clock) {
        return new Minecraft189ClickRateTracker(clock);
    }
    @Test void negativeNanoTimeNearLongMinDoesNotEvictNewClick() {
        Clock clock = new Clock(Long.MIN_VALUE + 100);
        Minecraft189ClickRateTracker t=tracker(clock);
        t.recordPress(Minecraft189ClickRateTracker.LEFT_BUTTON);
        clock.now=Long.MIN_VALUE + 200;
        assertEquals(1,t.snapshot().left());
    }
    @Test void negativeNanoTimeExactlyOneSecondOldExpires() {
        Clock clock=new Clock(Long.MIN_VALUE + 7);
        Minecraft189ClickRateTracker t=tracker(clock);
        t.recordPress(Minecraft189ClickRateTracker.LEFT_BUTTON);
        clock.now=Long.MIN_VALUE + 7 + SECOND - 1;
        assertEquals(1,t.snapshot().left());
        clock.now++;
        assertEquals(0,t.snapshot().left());
    }
    @Test void originalClockMayLegitimatelyStartAtLongMinValue() {
        Clock clock=new Clock(Long.MIN_VALUE);
        Minecraft189ClickRateTracker t=tracker(clock);
        t.recordPress(Minecraft189ClickRateTracker.LEFT_BUTTON);
        assertEquals(1,t.snapshot().left());
        clock.now=Long.MIN_VALUE + SECOND;
        assertEquals(0,t.snapshot().left());
    }
    @Test void rightAndLeftNegativeOriginUseIndependentSameWindow() {
        Clock clock=new Clock(Long.MIN_VALUE + 17);
        Minecraft189ClickRateTracker t=tracker(clock);
        t.recordPress(Minecraft189ClickRateTracker.LEFT_BUTTON);
        clock.now+=SECOND / 2;
        t.recordPress(Minecraft189ClickRateTracker.RIGHT_BUTTON);
        clock.now=Long.MIN_VALUE + 17 + SECOND;
        assertEquals(0,t.snapshot().left());
        assertEquals(1,t.snapshot().right());
        clock.now+=SECOND / 2;
        assertEquals(0,t.snapshot().right());
    }
    @Test void simultaneousOriginalClicksCountSeparately() {
        Clock clock=new Clock(-1_500_000_000L);
        Minecraft189ClickRateTracker t=tracker(clock);
        t.recordPress(Minecraft189ClickRateTracker.LEFT_BUTTON);
        t.recordPress(Minecraft189ClickRateTracker.LEFT_BUTTON);
        t.recordPress(Minecraft189ClickRateTracker.RIGHT_BUTTON);
        assertEquals(2,t.snapshot().left());
        assertEquals(1,t.snapshot().right());
    }
    @Test void negativeToPositiveOriginHasAccurateElapsedWindow() {
        Clock clock=new Clock(-700_000_000L);
        Minecraft189ClickRateTracker t=tracker(clock);
        t.recordPress(Minecraft189ClickRateTracker.LEFT_BUTTON);
        clock.now=299_999_999L;
        assertEquals(1,t.snapshot().left());
        clock.now=300_000_000L;
        assertEquals(0,t.snapshot().left());
    }
    @Test void hugeSignedForwardClockGapExpiresAllClicksWithoutThresholdOverflow() {
        Clock clock=new Clock(Long.MIN_VALUE + 1);
        Minecraft189ClickRateTracker t=tracker(clock);
        t.recordPress(Minecraft189ClickRateTracker.LEFT_BUTTON);
        t.recordPress(Minecraft189ClickRateTracker.RIGHT_BUTTON);
        clock.now=Long.MAX_VALUE - 2;
        assertEquals(0,t.snapshot().left());
        assertEquals(0,t.snapshot().right());
    }
    @Test void backwardsNativeClockStillFailsExplicitlyAndRetainsClicks() {
        Clock clock=new Clock(Long.MIN_VALUE + 500);
        Minecraft189ClickRateTracker t=tracker(clock);
        t.recordPress(Minecraft189ClickRateTracker.LEFT_BUTTON);
        clock.now=Long.MIN_VALUE + 499;
        assertThrows(IllegalStateException.class,t::snapshot);
        clock.now=Long.MIN_VALUE + 800;
        assertEquals(1,t.snapshot().left());
    }
    @Test void backwardsRecordDoesNotBankInvalidClick() {
        Clock clock=new Clock(-2_000_000_000L);
        Minecraft189ClickRateTracker t=tracker(clock);
        t.recordPress(Minecraft189ClickRateTracker.LEFT_BUTTON);
        clock.now=-2_000_000_001L;
        assertThrows(IllegalStateException.class,
                ()->t.recordPress(Minecraft189ClickRateTracker.RIGHT_BUTTON));
        clock.now=-1_900_000_000L;
        assertEquals(1,t.snapshot().left());
        assertEquals(0,t.snapshot().right());
    }
    @Test void validFutureTickAfterBackwardsSnapshotCanStillAdvance() {
        Clock clock=new Clock(-1_000_000_000L);
        Minecraft189ClickRateTracker t=tracker(clock);
        t.recordPress(Minecraft189ClickRateTracker.RIGHT_BUTTON);
        clock.now=-1_000_000_010L;
        assertThrows(IllegalStateException.class,t::snapshot);
        clock.now=-500_000_000L;
        assertEquals(1,t.snapshot().right());
    }
    @Test void clearErasesNegativeOriginAndRearmsNewIndependentClockBaseline() {
        Clock clock=new Clock(Long.MIN_VALUE + 19);
        Minecraft189ClickRateTracker t=tracker(clock);
        t.recordPress(Minecraft189ClickRateTracker.LEFT_BUTTON);
        t.clear();
        clock.now=Long.MIN_VALUE;
        t.recordPress(Minecraft189ClickRateTracker.RIGHT_BUTTON);
        assertEquals(0,t.snapshot().left());
        assertEquals(1,t.snapshot().right());
    }
    @Test void unsupportedButtonDoesNotReadClockOrChangeWindow() {
        final int[] samples=new int[1];
        LongSupplier supplier=()->{ samples[0]++; return Long.MIN_VALUE+10; };
        Minecraft189ClickRateTracker t=new Minecraft189ClickRateTracker(supplier);
        assertThrows(IllegalArgumentException.class,()->t.recordPress(2));
        assertEquals(0,samples[0]);
        t.recordPress(Minecraft189ClickRateTracker.LEFT_BUTTON);
        assertEquals(1,samples[0]);
        assertThrows(IllegalArgumentException.class,()->t.clicksPerSecond(2));
    }
    @Test void negativeOriginTwoClicksOnBoundaryRetainRecentOnly() {
        Clock clock=new Clock(Long.MIN_VALUE + 300);
        Minecraft189ClickRateTracker t=tracker(clock);
        t.recordPress(Minecraft189ClickRateTracker.LEFT_BUTTON);
        clock.now+=SECOND / 4;
        t.recordPress(Minecraft189ClickRateTracker.LEFT_BUTTON);
        clock.now=Long.MIN_VALUE + 300 + SECOND;
        assertEquals(1,t.snapshot().left());
        clock.now+=SECOND / 4;
        assertEquals(0,t.snapshot().left());
    }
    @Test void zeroElapsedTimeNeverPrematurelyEvictsMinValueClockClick() {
        Clock clock=new Clock(Long.MIN_VALUE);
        Minecraft189ClickRateTracker t=tracker(clock);
        t.recordPress(Minecraft189ClickRateTracker.RIGHT_BUTTON);
        assertEquals(1,t.snapshot().right());
        assertEquals(1,t.snapshot().right());
    }
}
