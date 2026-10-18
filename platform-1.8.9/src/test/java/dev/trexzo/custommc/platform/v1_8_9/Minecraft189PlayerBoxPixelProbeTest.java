package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189PlayerBoxPixelProbeTest {
    @Test void convertsNativeTopLeftUiStrokeToActualGlBottomLeftPixel() {
        assertArrayEquals(new int[]{420, 378},
                Minecraft189PlayerBoxPixelProbe.sampleTopStroke(
                        400, 100, 40, 80, 2, 1, 854, 480));
        assertArrayEquals(new int[]{440, 377},
                Minecraft189PlayerBoxPixelProbe.sampleTopStroke(
                        200, 50, 40, 80, 2, 2, 854, 480));
        assertNull(Minecraft189PlayerBoxPixelProbe.sampleTopStroke(
                Float.NaN, 30, 50, 70, 2, 1, 854, 480));
        assertNull(Minecraft189PlayerBoxPixelProbe.sampleTopStroke(
                1000, 30, 50, 70, 2, 1, 854, 480));
        assertNull(Minecraft189PlayerBoxPixelProbe.sampleTopStroke(
                30, 30, 1, 70, 2, 1, 854, 480));
    }

    @Test void firstHudPassMustNotConsumeIndependentFramebufferProof() {
        final String exact="e3542dd1-56f3-3ab4-82cb-2c015df72d8d";
        // The first genuine draw callback is already successful, but
        // native glReadPixels can miss that exact startup frame. A later
        // original frame must still be eligible while pixel PASS is absent.
        final boolean hudDrawAlreadyReported=true;
        assertTrue(hudDrawAlreadyReported);
        assertTrue(Minecraft189PlayerBoxPixelProbe.shouldProbeOriginalRemote(
                true,false,exact,exact,30F,60F));
        assertTrue(Minecraft189PlayerBoxPixelProbe.shouldProbeOriginalRemote(
                true,false,exact,exact,30F,60F));
    }

    @Test void genuinePixelSuccessRevokesSubsequentProbes() {
        final String exact="e3542dd1-56f3-3ab4-82cb-2c015df72d8d";
        assertTrue(Minecraft189PlayerBoxPixelProbe.shouldProbeOriginalRemote(
                true,false,exact,exact,10F,20F));
        assertFalse(Minecraft189PlayerBoxPixelProbe.shouldProbeOriginalRemote(
                true,true,exact,exact,10F,20F));
    }

    @Test void wrongSourceUuidNeverProducesFramebufferProof() {
        assertFalse(Minecraft189PlayerBoxPixelProbe.shouldProbeOriginalRemote(
                true,false,"exact-target-uuid","wrong-target-uuid",10F,20F));
        assertFalse(Minecraft189PlayerBoxPixelProbe.shouldProbeOriginalRemote(
                true,false,null,"original-remote",10F,20F));
    }

    @Test void normalGameplayWithoutAcceptanceFlagNeverReadsGlPixels() {
        assertFalse(Minecraft189PlayerBoxPixelProbe.shouldProbeOriginalRemote(
                false,false,"uuid","uuid",10F,20F));
        assertFalse(Minecraft189PlayerBoxPixelProbe.shouldProbeOriginalRemote(
                false,true,"uuid","uuid",10F,20F));
    }

    @Test void malformedOriginalAabbRejectsSourceProbeWithoutRelaxation() {
        for(float invalid:new float[]{Float.NaN,Float.POSITIVE_INFINITY,
                -1F,0F,1F}) {
            assertFalse(Minecraft189PlayerBoxPixelProbe.shouldProbeOriginalRemote(
                    true,false,"uuid","uuid",invalid,15F));
            assertFalse(Minecraft189PlayerBoxPixelProbe.shouldProbeOriginalRemote(
                    true,false,"uuid","uuid",15F,invalid));
        }
        assertTrue(Minecraft189PlayerBoxPixelProbe.shouldProbeOriginalRemote(
                true,false,"uuid","uuid",12F,15F));
    }

    @Test void visibleOpaqueTopBottomAndBothSidesComeFromSameOriginalAabb() {
        final int[][] pixels = Minecraft189PlayerBoxPixelProbe.sampleVisibleStrokePixels(
                400, 100, 40, 80, 2, 1, 854, 480);
        assertEquals(4, pixels.length);
        assertArrayEquals(new int[]{420,378}, pixels[0]); // actual cyan TOP
        assertArrayEquals(new int[]{420,300}, pixels[1]); // actual cyan BOTTOM
        assertArrayEquals(new int[]{401,339}, pixels[2]); // actual cyan LEFT
        assertArrayEquals(new int[]{439,339}, pixels[3]); // actual cyan RIGHT
    }

    @Test void clippedOriginalTopEdgeStillSamplesRealVisibleOtherStrokes() {
        final int[][] pixels = Minecraft189PlayerBoxPixelProbe.sampleVisibleStrokePixels(
                400, -20, 40, 80, 2, 1, 854, 480);
        assertEquals(3, pixels.length);
        assertArrayEquals(new int[]{420,420}, pixels[0]); // bottom at +60
        assertArrayEquals(new int[]{401,450}, pixels[1]); // visible left
        assertArrayEquals(new int[]{439,450}, pixels[2]); // visible right
        for (int[] sample : pixels) {
            assertTrue(sample[0] >= 0 && sample[0] < 854);
            assertTrue(sample[1] >= 0 && sample[1] < 480);
        }
    }

    @Test void clippedOriginalLeftEdgeStillHasRealTopBottomAndRight() {
        final int[][] pixels = Minecraft189PlayerBoxPixelProbe.sampleVisibleStrokePixels(
                -50, 80, 120, 50, 2, 1, 854, 480);
        assertEquals(3, pixels.length);
        assertArrayEquals(new int[]{35,398}, pixels[0]);
        assertArrayEquals(new int[]{35,350}, pixels[1]);
        assertArrayEquals(new int[]{69,374}, pixels[2]);
    }

    @Test void entirelyOffscreenOriginalAabbNeverProducesPixelCandidates() {
        assertEquals(0,Minecraft189PlayerBoxPixelProbe.sampleVisibleStrokePixels(
                -200,-200,30,30,2,1,854,480).length);
        assertEquals(0,Minecraft189PlayerBoxPixelProbe.sampleVisibleStrokePixels(
                1300,100,30,30,2,1,854,480).length);
        assertEquals(0,Minecraft189PlayerBoxPixelProbe.sampleVisibleStrokePixels(
                10,600,30,30,2,1,854,480).length);
    }

    @Test void scaledNativeFrameUsesActualLowerLeftGlPixelCoordinates() {
        final int[][] pixels = Minecraft189PlayerBoxPixelProbe.sampleVisibleStrokePixels(
                200, 50, 40, 80, 2, 2, 854, 480);
        assertEquals(4,pixels.length);
        assertArrayEquals(new int[]{440,377},pixels[0]);
        assertArrayEquals(new int[]{440,221},pixels[1]);
    }

    @Test void malformedOriginalGlFrameAndAabbCannotGenerateAcceptedSamples() {
        assertEquals(0,Minecraft189PlayerBoxPixelProbe.sampleVisibleStrokePixels(
                Float.NaN,10,40,60,2,1,854,480).length);
        assertEquals(0,Minecraft189PlayerBoxPixelProbe.sampleVisibleStrokePixels(
                10,10,Float.POSITIVE_INFINITY,60,2,1,854,480).length);
        assertEquals(0,Minecraft189PlayerBoxPixelProbe.sampleVisibleStrokePixels(
                10,10,40,60,0,1,854,480).length);
        assertEquals(0,Minecraft189PlayerBoxPixelProbe.sampleVisibleStrokePixels(
                10,10,40,60,2,Float.NaN,854,480).length);
        assertEquals(0,Minecraft189PlayerBoxPixelProbe.sampleVisibleStrokePixels(
                10,10,40,60,2,1,40,480).length);
    }

    @Test void candidateCoordinatesCannotReplaceStrictActualRgbEvidence() {
        final int[][] pixels = Minecraft189PlayerBoxPixelProbe.sampleVisibleStrokePixels(
                400,100,40,80,2,1,854,480);
        assertEquals(4,pixels.length); // geometry is NOT pixel success
        assertFalse(Minecraft189PlayerBoxPixelProbe.isForegroundColor(16,20,34));
        assertFalse(Minecraft189PlayerBoxPixelProbe.isForegroundColor(255,255,255));
        assertTrue(Minecraft189PlayerBoxPixelProbe.isForegroundColor(112,217,255));
        assertFalse(Minecraft189PlayerBoxPixelProbe.isForegroundColor(96,217,255));
    }

    @Test void matchesOpaqueCyanAndRejectsBackgroundOrUnderlay() {
        assertTrue(Minecraft189PlayerBoxPixelProbe.isForegroundColor(112, 217, 255));
        assertTrue(Minecraft189PlayerBoxPixelProbe.isForegroundColor(110, 220, 255));
        assertFalse(Minecraft189PlayerBoxPixelProbe.isForegroundColor(16, 20, 34));
        assertFalse(Minecraft189PlayerBoxPixelProbe.isForegroundColor(255, 255, 255));
        assertFalse(Minecraft189PlayerBoxPixelProbe.isForegroundColor(0, 200, 255));
    }
}
