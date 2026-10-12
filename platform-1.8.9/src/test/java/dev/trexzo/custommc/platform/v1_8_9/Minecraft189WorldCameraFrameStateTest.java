package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189WorldCameraFrameStateTest {
    private static final double[] IDENTITY = {
            1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1
    };

    @Test void acceptsGenuineFrameEvidenceAndDefensivelyCopies() {
        Minecraft189WorldCameraFrameState state =
                new Minecraft189WorldCameraFrameState();
        assertNull(state.snapshot());
        double[] view = IDENTITY.clone();
        double[] proj = IDENTITY.clone();
        int[] vp = {25,35,800,600};
        state.accept(7L,2,0.5F,view,proj,vp);
        Minecraft189WorldCameraFrameState.Snapshot s = state.snapshot();
        assertNotNull(s);
        assertEquals(7L,s.frame());
        assertEquals(2,s.pass());
        assertEquals(0.5F,s.partialTicks());
        view[0] = 9000.0D;
        proj[0] = 9000.0D;
        vp[0] = 9000;
        assertEquals(1.0D,s.view()[0]);
        assertEquals(1.0D,s.projection()[0]);
        assertEquals(25,s.viewport()[0]);
        s.view()[0] = 5000.0D;
        s.viewport()[1] = 5000;
        assertEquals(1.0D,state.snapshot().view()[0]);
        assertEquals(35,state.snapshot().viewport()[1]);
        state.clear();
        assertNull(state.snapshot());
    }

    @Test void originalWorldRenderEntityInterpolationIsFrameLocked() {
        final Minecraft189WorldCameraFrameState state =
                new Minecraft189WorldCameraFrameState();
        final Minecraft189PlayerPositionAccess entity =
                new Minecraft189PlayerPositionAccess() {
                    public double customMcPositionX() { return 200; }
                    public double customMcPositionY() { return 70; }
                    public double customMcPositionZ() { return -30; }
                    public double customMcLastTickPositionX() { return 198; }
                    public double customMcLastTickPositionY() { return 66; }
                    public double customMcLastTickPositionZ() { return -26; }
                };
        state.accept(7, 2, 0.5F, IDENTITY, IDENTITY,
                new int[]{0,0,800,600});
        state.captureOrigin(7, 2, 0.5F, entity);
        final Minecraft189WorldCameraFrameState.Snapshot s = state.snapshot();
        assertNotNull(s);
        assertTrue(s.originAvailable());
        assertEquals(199.0D, s.originX(), 0.00001D);
        assertEquals(68.0D, s.originY(), 0.00001D);
        assertEquals(-28.0D, s.originZ(), 0.00001D);
        assertEquals(198.0D, Minecraft189WorldCameraFrameState.lerp(
                198.0D, 200.0D, 0.0F));
        assertEquals(200.0D, Minecraft189WorldCameraFrameState.lerp(
                198.0D, 200.0D, 1.0F));
        assertTrue(Double.isNaN(Minecraft189WorldCameraFrameState.lerp(
                Double.NaN, 200, 0.5F)));
        state.clear();
        assertNull(state.snapshot());
        state.accept(8, 2, 0.5F, IDENTITY, IDENTITY,
                new int[]{0,0,800,600});
        state.captureOrigin(7, 2, 0.5F, entity);
        assertNull(state.snapshot(), "stale frame must invalidate camera");
        state.accept(9, 2, 0.5F, IDENTITY, IDENTITY,
                new int[]{0,0,800,600});
        state.captureOrigin(9, 1, 0.5F, entity);
        assertNull(state.snapshot(), "wrong render pass must invalidate camera");
        state.accept(10, 2, 0.5F, IDENTITY, IDENTITY,
                new int[]{0,0,800,600});
        state.captureOrigin(10, 2, 0.5F,
                new Minecraft189PlayerPositionAccess() {
                    public double customMcPositionX() { return 1; }
                    public double customMcPositionY() { return 1; }
                    public double customMcPositionZ() { return 1; }
                });
        assertNull(state.snapshot(),
                "unmapped last-tick fields must never authorize origin");
    }

    @Test void invalidNativeStateNeverLeaksAnEarlierCamera() {
        Minecraft189WorldCameraFrameState state =
                new Minecraft189WorldCameraFrameState();
        state.accept(2L,2,0.4F,IDENTITY,IDENTITY,
                new int[]{0,0,800,600});
        assertNotNull(state.snapshot());
        state.accept(3L,3,0.4F,IDENTITY,IDENTITY,
                new int[]{0,0,800,600});
        assertNull(state.snapshot());
        state.accept(4L,2,Float.NaN,IDENTITY,IDENTITY,
                new int[]{0,0,800,600});
        assertNull(state.snapshot());
        double[] invalid = IDENTITY.clone();
        invalid[3] = Double.NaN;
        state.accept(5L,2,0.4F,invalid,IDENTITY,
                new int[]{0,0,800,600});
        assertNull(state.snapshot());
        state.accept(6L,2,0.4F,IDENTITY,IDENTITY,
                new int[]{0,0,800,600});
        state.accept(6L,2,0.4F,IDENTITY,IDENTITY,
                new int[]{0,-1,800,600});
        assertNull(state.snapshot());
        state.accept(-1L,2,0.4F,IDENTITY,IDENTITY,
                new int[]{0,0,800,600});
        assertNull(state.snapshot());
    }
}
