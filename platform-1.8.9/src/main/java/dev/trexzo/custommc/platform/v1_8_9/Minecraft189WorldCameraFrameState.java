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

    synchronized void clear() { snapshot = null; }

    synchronized Snapshot snapshot() { return snapshot; }

    synchronized void captureNative(
            final long frame, final int pass, final float partialTicks) {
        if (frame < 0 || pass < 0 || pass > 2
                || !Float.isFinite(partialTicks)
                || partialTicks < 0.0F || partialTicks > 1.0F
                || !Display.isCreated()) {
            clear();
            return;
        }
        try {
            final FloatBuffer modelview = BufferUtils.createFloatBuffer(16);
            final FloatBuffer projection = BufferUtils.createFloatBuffer(16);
            final IntBuffer viewport = BufferUtils.createIntBuffer(4);
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
        } catch (RuntimeException | LinkageError invalidContext) {
            // No guessed matrices and no crash if native display state fails.
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

        Snapshot(final long frame, final int pass, final float partialTicks,
                final double[] view, final double[] projection,
                final int[] viewport) {
            this.frame = frame;
            this.pass = pass;
            this.partialTicks = partialTicks;
            this.view = Arrays.copyOf(view, 16);
            this.projection = Arrays.copyOf(projection, 16);
            this.viewport = Arrays.copyOf(viewport, 4);
        }

        long frame() { return frame; }
        int pass() { return pass; }
        float partialTicks() { return partialTicks; }
        double[] view() { return Arrays.copyOf(view, 16); }
        double[] projection() { return Arrays.copyOf(projection, 16); }
        int[] viewport() { return Arrays.copyOf(viewport, 4); }
    }
}
