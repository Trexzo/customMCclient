package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Source mapped original player X/Z positions and visible HUD statistics.
 * Numeric offline source tests do not replace pinned original Mojang CI.
 */
final class Minecraft189MovementSpeedTrackerFiniteTest {
    private static Minecraft189MovementSpeedTracker.Snapshot speed(
            final Minecraft189MovementSpeedTracker tracker,
            final double x, final double z) {
        tracker.sample(x,z);
        return tracker.snapshot();
    }
    @Test void firstActualPositionIsBaselineNotMadeUpSpeed() {
        Minecraft189MovementSpeedTracker t=new Minecraft189MovementSpeedTracker();
        var s=speed(t,3D,4D);
        assertFalse(s.available());
        assertEquals(0D,s.blocksPerSecond());
        assertEquals(0D,s.peakBlocksPerSecond());
    }
    @Test void genuineThreeFourDisplacementStillHundredBlocksPerSecond() {
        Minecraft189MovementSpeedTracker t=new Minecraft189MovementSpeedTracker();
        t.sample(0D,0D);
        var s=speed(t,3D,4D);
        assertTrue(s.available());
        assertEquals(100D,s.blocksPerSecond(),1e-12);
        assertEquals(100D,s.peakBlocksPerSecond(),1e-12);
    }
    @Test void hugeFiniteMappedDeltaDoesNotOverflowBySquaring() {
        Minecraft189MovementSpeedTracker t=new Minecraft189MovementSpeedTracker();
        t.sample(0D,0D);
        var s=speed(t,1e200,0D);
        assertTrue(s.available());
        assertEquals(2e201,s.blocksPerSecond(),2e188);
        assertEquals(2e201,s.peakBlocksPerSecond(),2e188);
        assertTrue(Double.isFinite(s.blocksPerSecond()));
    }
    @Test void twoHugeFiniteAxesUseHypotInsteadOfNaiveSquaredInfinity() {
        Minecraft189MovementSpeedTracker t=new Minecraft189MovementSpeedTracker();
        t.sample(0D,0D);
        var s=speed(t,3e200,4e200);
        assertTrue(s.available());
        assertEquals(1e202,s.blocksPerSecond(),1e189);
    }
    @Test void hugeFiniteNegativeCoordinateStillProducesRealFiniteSpeed() {
        Minecraft189MovementSpeedTracker t=new Minecraft189MovementSpeedTracker();
        t.sample(0D,0D);
        var s=speed(t,-1e200,0D);
        assertTrue(s.available());
        assertEquals(2e201,s.blocksPerSecond(),2e188);
    }
    @Test void finiteCoordinateWithNonfiniteDeltaDoesNotPublishInfinity() {
        Minecraft189MovementSpeedTracker t=new Minecraft189MovementSpeedTracker();
        t.sample(-1e308,0D);
        var s=speed(t,1e308,0D);
        assertFalse(s.available());
        assertEquals(0D,s.blocksPerSecond());
        assertEquals(0D,s.peakBlocksPerSecond());
    }
    @Test void conversionOverflowSuppressesCurrentSampleAndPeak() {
        Minecraft189MovementSpeedTracker t=new Minecraft189MovementSpeedTracker();
        t.sample(0D,0D);
        var s=speed(t,1e308,0D);
        assertFalse(s.available());
        assertEquals(0D,s.blocksPerSecond());
        assertEquals(0D,s.peakBlocksPerSecond());
    }
    @Test void laterSmallMovementRecoversAfterExtremeCoordinateOverflow() {
        Minecraft189MovementSpeedTracker t=new Minecraft189MovementSpeedTracker();
        t.sample(0D,0D);
        t.sample(1e308,0D);
        var s=speed(t,1e308-1e292,0D);
        assertTrue(s.available());
        assertTrue(Double.isFinite(s.blocksPerSecond()));
        assertTrue(s.blocksPerSecond()>0D);
    }
    @Test void knownNormalPeakSurvivesUnrepresentableOutlier() {
        Minecraft189MovementSpeedTracker t=new Minecraft189MovementSpeedTracker();
        t.sample(0D,0D);
        t.sample(3D,4D);
        var s=speed(t,1e308,0D);
        assertFalse(s.available());
        assertEquals(100D,s.peakBlocksPerSecond(),1e-12);
    }
    @Test void peakTracksOnlyRealFiniteSourceSpeed() {
        Minecraft189MovementSpeedTracker t=new Minecraft189MovementSpeedTracker();
        t.sample(0D,0D);
        t.sample(3D,4D);
        var s=speed(t,4D,4D);
        assertEquals(20D,s.blocksPerSecond(),1e-12);
        assertEquals(100D,s.peakBlocksPerSecond(),1e-12);
    }
    @Test void zeroHorizontalMotionRemainsLegitimatelyAvailableZero() {
        Minecraft189MovementSpeedTracker t=new Minecraft189MovementSpeedTracker();
        t.sample(2D,2D);
        var s=speed(t,2D,2D);
        assertTrue(s.available());
        assertEquals(0D,s.blocksPerSecond(),0D);
    }
    @Test void signedZeroDoesNotInventSpeedOrDamagePeak() {
        Minecraft189MovementSpeedTracker t=new Minecraft189MovementSpeedTracker();
        t.sample(-0D,0D);
        var s=speed(t,0D,-0D);
        assertTrue(s.available());
        assertEquals(0D,s.blocksPerSecond(),0D);
        assertEquals(0D,s.peakBlocksPerSecond(),0D);
    }
    @Test void nonfiniteNativeXRejectedBeforeChangingPositionOrPeak() {
        Minecraft189MovementSpeedTracker t=new Minecraft189MovementSpeedTracker();
        t.sample(0D,0D);
        assertThrows(IllegalArgumentException.class,()->t.sample(Double.NaN,0D));
        var s=speed(t,3D,4D);
        assertTrue(s.available());
        assertEquals(100D,s.blocksPerSecond(),1e-12);
    }
    @Test void nonfiniteNativeZRejectedBeforeChangingPositionOrPeak() {
        Minecraft189MovementSpeedTracker t=new Minecraft189MovementSpeedTracker();
        t.sample(0D,0D);
        assertThrows(IllegalArgumentException.class,
                ()->t.sample(0D,Double.POSITIVE_INFINITY));
        var s=speed(t,3D,4D);
        assertEquals(100D,s.blocksPerSecond(),1e-12);
    }
    @Test void clearErasesOriginalSpeedBaselineAndPeak() {
        Minecraft189MovementSpeedTracker t=new Minecraft189MovementSpeedTracker();
        t.sample(0D,0D);
        t.sample(3D,4D);
        t.clear();
        var s=t.snapshot();
        assertFalse(s.available());
        assertEquals(0D,s.blocksPerSecond());
        assertEquals(0D,s.peakBlocksPerSecond());
        assertFalse(speed(t,10D,10D).available());
        assertEquals(20D,speed(t,10D,11D).blocksPerSecond(),1e-12);
    }
}
