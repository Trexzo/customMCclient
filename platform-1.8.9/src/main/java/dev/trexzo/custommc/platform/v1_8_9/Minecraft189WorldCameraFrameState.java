package dev.trexzo.custommc.platform.v1_8_9;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

/**
 * Frame-owned, read-only capture of actual OpenGL world camera state from
 * original EntityRenderer.renderWorldPass immediately after camera setup.
 * Native matrices rotate the world around the camera; entity AABBs still
 * need the certified interpolated render-view origin before projection.
 */
final class Minecraft189WorldCameraFrameState {
    private Snapshot snapshot;
    private Minecraft189ProjectedPlayerFrame projectedPlayers;
    private String captureStatus = "not-invoked";

    synchronized String captureStatus() { return captureStatus; }

    synchronized void clear() {
        snapshot = null;
        projectedPlayers = null;
    }

    synchronized Snapshot snapshot() { return snapshot; }

    synchronized Minecraft189ProjectedPlayerFrame projectedPlayers() {
        return projectedPlayers;
    }

    synchronized void captureNativePlayerBoxes(final long frame, final int pass,
            final float partialTicks, final Object[] objects,
            final int[] kinds, final java.util.UUID[] uuids) {
        if (snapshot == null || !snapshot.originAvailable()
                || snapshot.frame() != frame || snapshot.pass() != pass
                || Math.abs(snapshot.partialTicks() - partialTicks) > 0.0001F) {
            clear();
            return;
        }
        final Minecraft189ProjectedPlayerFrame next =
                Minecraft189ProjectedPlayerFrame.collect(
                        frame, snapshot, objects, kinds, uuids);
        if (next == null) {
            clear();
            return;
        }
        projectedPlayers = next;
    }

    /**
     * Exact original world-render entity interpolation, same frame/pass.
     * Native projection is camera-relative: subtract this origin from
     * native world-coordinate AABB corners BEFORE applying GL matrices.
     */
    synchronized void captureOrigin(final long frame, final int pass,
            final float partialTicks,
            final Minecraft189PlayerPositionAccess entity) {
        if (snapshot == null || snapshot.frame() != frame
                || snapshot.pass() != pass
                || Math.abs(snapshot.partialTicks() - partialTicks) > 0.0001F
                || entity == null) {
            clear();
            return;
        }
        try {
            final double x = lerp(entity.customMcLastTickPositionX(),
                    entity.customMcPositionX(), partialTicks);
            final double y = lerp(entity.customMcLastTickPositionY(),
                    entity.customMcPositionY(), partialTicks);
            final double z = lerp(entity.customMcLastTickPositionZ(),
                    entity.customMcPositionZ(), partialTicks);
            if (!Double.isFinite(x) || !Double.isFinite(y)
                    || !Double.isFinite(z)) {
                clear();
                return;
            }
            snapshot = snapshot.withOrigin(x, y, z);
            projectedPlayers = null;
        } catch (RuntimeException | LinkageError invalidNativeEntity) {
            clear();
        }
    }

    static double lerp(final double lastTick, final double current,
            final float partialTicks) {
        if (!Double.isFinite(lastTick) || !Double.isFinite(current)
                || !Float.isFinite(partialTicks)
                || partialTicks < 0.0F || partialTicks > 1.0F)
            return Double.NaN;
        final double out = lastTick + (current - lastTick) * partialTicks;
        return Double.isFinite(out) ? out : Double.NaN;
    }

    synchronized void captureNative(
            final long frame, final int pass, final float partialTicks) {
        if (frame < 0 || pass < 0 || pass > 2
                || !Float.isFinite(partialTicks)
                || partialTicks < 0.0F || partialTicks > 1.0F
                || !Display.isCreated()) {
            captureStatus = "invalid-frame-pass-or-display";
            clear();
            return;
        }
        try {
            final FloatBuffer modelview = BufferUtils.createFloatBuffer(16);
            final FloatBuffer projection = BufferUtils.createFloatBuffer(16);
            // LWJGL 2 glGetIntegerv's Java buffer overload requires 16 ints
            // regardless of GL_VIEWPORT's four returned components.
            // Preserve only the first 4 native values in the snapshot.
            final IntBuffer viewport = BufferUtils.createIntBuffer(16);
            GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelview);
            GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projection);
            GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);
            final double[] view = new double[16];
            final double[] proj = new double[16];
            final int[] vp = new int[4];
            for (int i = 0; i < 16; i++) {
                view[i] = modelview.get(i);
                proj[i] = projection.get(i);
            }
            for (int i = 0; i < 4; i++) vp[i] = viewport.get(i);
            // Actual native GL viewport begins at lower-left. Projection
            // consumers use a top-left viewport; preserve exact origin.
            vp[1] = Math.subtractExact(Display.getHeight(),
                    Math.addExact(vp[1], vp[3]));
            accept(frame, pass, partialTicks, view, proj, vp);
            captureStatus = snapshot == null
                    ? "invalid-native-gl-snapshot" : "accepted";
        } catch (RuntimeException | LinkageError invalidContext) {
            // No guessed matrices and no crash if native display state fails.
            captureStatus = "native-gl-exception:"
                    + invalidContext.getClass().getSimpleName();
            clear();
        }
    }

    synchronized void accept(final long frame, final int pass,
            final float partialTicks, final double[] view,
            final double[] projection, final int[] viewport) {
        if (frame < 0 || pass < 0 || pass > 2
                || !Float.isFinite(partialTicks)
                || partialTicks < 0.0F || partialTicks > 1.0F
                || !valid(view, 16) || !valid(projection, 16)
                || viewport == null || viewport.length != 4
                || viewport[0] < 0 || viewport[1] < 0
                || viewport[2] <= 0 || viewport[3] <= 0
                || viewport[2] > 32768 || viewport[3] > 32768) {
            clear();
            return;
        }
        snapshot = new Snapshot(frame, pass, partialTicks, view, projection,
                viewport);
        projectedPlayers = null;
    }

    private static boolean valid(final double[] values, final int n) {
        if (values == null || values.length != n) return false;
        for (final double v : values) if (!Double.isFinite(v)) return false;
        return true;
    }

    static final class Snapshot {
        private final long frame;
        private final int pass;
        private final float partialTicks;
        private final double[] view, projection;
        private final int[] viewport;
        private final boolean originAvailable;
        private final double originX, originY, originZ;

        Snapshot(final long frame, final int pass, final float partialTicks,
                final double[] view, final double[] projection,
                final int[] viewport) {
            this(frame, pass, partialTicks, view, projection, viewport,
                    false, 0, 0, 0);
        }

        private Snapshot(final long frame, final int pass,
                final float partialTicks, final double[] view,
                final double[] projection, final int[] viewport,
                final boolean originAvailable, final double originX,
                final double originY, final double originZ) {
            this.frame = frame;
            this.pass = pass;
            this.partialTicks = partialTicks;
            this.view = Arrays.copyOf(view, 16);
            this.projection = Arrays.copyOf(projection, 16);
            this.viewport = Arrays.copyOf(viewport, 4);
            this.originAvailable = originAvailable;
            this.originX = originX;
            this.originY = originY;
            this.originZ = originZ;
        }

        Snapshot withOrigin(final double x, final double y, final double z) {
            return new Snapshot(frame, pass, partialTicks, view, projection,
                    viewport, true, x, y, z);
        }

        long frame() { return frame; }
        int pass() { return pass; }
        float partialTicks() { return partialTicks; }
        double[] view() { return Arrays.copyOf(view, 16); }
        double[] projection() { return Arrays.copyOf(projection, 16); }
        int[] viewport() { return Arrays.copyOf(viewport, 4); }
        boolean originAvailable() { return originAvailable; }
        double originX() { return originX; }
        double originY() { return originY; }
        double originZ() { return originZ; }
    }
}
