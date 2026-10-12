package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189WorldToScreenProjectionTest {
    private static final double[] IDENTITY = {
            1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1
    };
    private static final int[] VIEWPORT = {0, 0, 800, 600};

    private static Minecraft189WorldToScreenProjection.Rect clip(
            double[] box, double[] view, double[] proj) {
        return Minecraft189WorldToScreenProjection.projectAabb(
                box, view, proj, VIEWPORT);
    }

    @Test void orthographicProjectedWorldBoxIsMeasuredFromAllEightCorners() {
        final Minecraft189WorldToScreenProjection.Rect box =
                clip(new double[]{-0.5,-0.5,-0.5,0.5,0.5,0.5},
                        IDENTITY, IDENTITY);
        assertNotNull(box);
        assertEquals(200.0F, box.left, 0.001F);
        assertEquals(150.0F, box.top, 0.001F);
        assertEquals(600.0F, box.right, 0.001F);
        assertEquals(450.0F, box.bottom, 0.001F);
        assertEquals(400.0F, box.width(), 0.001F);
    }

    @Test void halfOutsideViewportClipsAtTheRightFrustumPlane() {
        final Minecraft189WorldToScreenProjection.Rect box =
                clip(new double[]{0.5,-0.5,-0.2,1.5,0.5,0.2},
                        IDENTITY, IDENTITY);
        assertNotNull(box);
        assertEquals(600.0F, box.left, 0.001F);
        assertEquals(800.0F, box.right, 0.001F);
    }

    @Test void rejectsOffscreenInvalidAndMissingNativeCameraAuthority() {
        assertNull(clip(new double[]{2,0,0,3,1,1}, IDENTITY, IDENTITY));
        assertNull(clip(new double[]{0,0,2,1,1,3}, IDENTITY, IDENTITY));
        assertNull(clip(new double[]{0,0,0,1,1,1}, null, IDENTITY));
        assertNull(clip(new double[]{0,0,0,1,1,1}, IDENTITY, null));
        assertNull(clip(new double[]{0,0,0,1,1,1}, IDENTITY, new double[15]));
        assertNull(clip(new double[]{2,0,0,1,1,1}, IDENTITY, IDENTITY));
        assertNull(clip(new double[]{0,0,0,Double.NaN,1,1},
                IDENTITY, IDENTITY));
        assertNull(Minecraft189WorldToScreenProjection.projectAabb(
                new double[]{0,0,0,1,1,1}, IDENTITY, IDENTITY,
                new int[]{0,0,0,600}));
    }

    @Test void truePerspectiveRejectsBehindCameraAndClipsAcrossNearPlane() {
        // Column-major canonical perspective (near=1, far=10, scale=1).
        final double[] perspective = {
                1,0,0,0, 0,1,0,0, 0,0,-11.0D/9.0D,-1,
                0,0,-20.0D/9.0D,0
        };
        final Minecraft189WorldToScreenProjection.Rect front =
                clip(new double[]{-0.5,-0.5,-3,0.5,0.5,-2},
                        IDENTITY, perspective);
        assertNotNull(front);
        assertTrue(front.width() > 0.0F);
        assertTrue(front.height() > 0.0F);
        assertNull(clip(new double[]{-0.5,-0.5,2,0.5,0.5,3},
                IDENTITY, perspective));
        final Minecraft189WorldToScreenProjection.Rect acrossNear =
                clip(new double[]{-0.5,-0.5,-2,0.5,0.5,-0.5},
                        IDENTITY, perspective);
        assertNotNull(acrossNear);
        assertTrue(acrossNear.left >= 0);
        assertTrue(acrossNear.right <= 800);
        assertTrue(acrossNear.top >= 0);
        assertTrue(acrossNear.bottom <= 600);
    }

    @Test void viewportOriginAndCameraTranslationBothAffectProjection() {
        final double[] view = {
                1,0,0,0, 0,1,0,0, 0,0,1,0, -1,0,0,1
        };
        final Minecraft189WorldToScreenProjection.Rect r =
                Minecraft189WorldToScreenProjection.projectAabb(
                        new double[]{0.5,-0.5,-0.5,1.5,0.5,0.5},
                        view, IDENTITY, new int[]{25,35,800,600});
        assertNotNull(r);
        assertEquals(225.0F, r.left, 0.001F);
        assertEquals(625.0F, r.right, 0.001F);
        assertEquals(185.0F, r.top, 0.001F);
    }
}
