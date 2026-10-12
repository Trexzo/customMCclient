package dev.trexzo.custommc.platform.v1_8_9;

import java.util.Arrays;

/**
 * Stateless, fail-closed homogeneous clipping for source-native 1.8.9 AABBs.
 *
 * Matrices are COLUMN-MAJOR OpenGL 4x4 arrays; AABB world coordinates must
 * belong to the same coordinate system as the supplied modelview matrix.
 * Viewport [left, top, width, height] uses TOP-LEFT display coordinates.
 *
 * This is projection MATH ONLY. No default/fabricated camera matrix exists;
 * callers must first capture actual same-frame vanilla world-render matrices.
 * Never use this class to claim an operational 2D/3D ESP hook on its own.
 */
final class Minecraft189WorldToScreenProjection {
    private static final double W_EPS = 1.0E-6D;
    private static final int[][] EDGES = {
            {0, 1}, {0, 2}, {0, 4}, {1, 3}, {1, 5}, {2, 3},
            {2, 6}, {3, 7}, {4, 5}, {4, 6}, {5, 7}, {6, 7}
    };

    private Minecraft189WorldToScreenProjection() { }

    static Rect projectAabb(final double[] aabb, final double[] view,
            final double[] projection, final int[] viewport) {
        if (!valid(aabb, 6) || !valid(view, 16)
                || !valid(projection, 16) || viewport == null
                || viewport.length != 4 || viewport[2] <= 0
                || viewport[3] <= 0 || viewport[0] < 0 || viewport[1] < 0
                || viewport[2] > 32768 || viewport[3] > 32768
                || aabb[0] > aabb[3] || aabb[1] > aabb[4]
                || aabb[2] > aabb[5]) return null;

        final double[][] vertices = new double[8][];
        for (int i = 0; i < 8; i++) {
            final double x = aabb[(i & 1) == 0 ? 0 : 3];
            final double y = aabb[(i & 2) == 0 ? 1 : 4];
            final double z = aabb[(i & 4) == 0 ? 2 : 5];
            vertices[i] = transform(projection, transform(view,
                    new double[]{x, y, z, 1.0D}));
            if (!valid(vertices[i], 4)) return null;
        }
        double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        boolean found = false;
        for (final int[] edge : EDGES) {
            final double[] clipped = clipEdge(vertices[edge[0]], vertices[edge[1]]);
            if (clipped == null) continue;
            for (int endpoint = 0; endpoint < 2; endpoint++) {
                final int k = 4 * endpoint;
                final double w = clipped[k + 3];
                if (!(w >= W_EPS) || !Double.isFinite(w)) return null;
                final double nx = clipped[k] / w, ny = clipped[k + 1] / w;
                final double sx = viewport[0] + (1.0D + nx) * 0.5D * viewport[2];
                final double sy = viewport[1] + (1.0D - ny) * 0.5D * viewport[3];
                if (!Double.isFinite(sx) || !Double.isFinite(sy)) return null;
                minX = Math.min(minX, sx);
                maxX = Math.max(maxX, sx);
                minY = Math.min(minY, sy);
                maxY = Math.max(maxY, sy);
                found = true;
            }
        }
        if (!found) return null;
        final double left = Math.max(viewport[0], minX);
        final double right = Math.min((double)viewport[0] + viewport[2], maxX);
        final double top = Math.max(viewport[1], minY);
        final double bottom = Math.min((double)viewport[1] + viewport[3], maxY);
        if (!Double.isFinite(left) || !Double.isFinite(right)
                || !Double.isFinite(top) || !Double.isFinite(bottom)
                || right <= left || bottom <= top) return null;
        return new Rect((float)left, (float)top, (float)right, (float)bottom);
    }

    private static boolean valid(final double[] values, final int expected) {
        if (values == null || values.length != expected) return false;
        for (final double value : values) if (!Double.isFinite(value)) return false;
        return true;
    }

    private static double[] transform(final double[] m, final double[] p) {
        final double[] r = new double[4];
        for (int row = 0; row < 4; row++) {
            r[row] = m[row] * p[0] + m[4 + row] * p[1]
                    + m[8 + row] * p[2] + m[12 + row] * p[3];
        }
        return r;
    }

    /**
     * Clips 12 actual AABB edges against all six homogeneous frustum planes
     * plus w >= epsilon. Retains both endpoints to prevent boxes behind
     * the near plane from becoming inverted or incorrectly screen-filling.
     */
    private static double[] clipEdge(final double[] a, final double[] b) {
        double t0 = 0.0D, t1 = 1.0D;
        for (int plane = 0; plane < 7; plane++) {
            final double fa = planeValue(a, plane), fb = planeValue(b, plane);
            if (fa < 0.0D && fb < 0.0D) return null;
            if (fa >= 0.0D && fb >= 0.0D) continue;
            final double denominator = fa - fb;
            if (denominator == 0.0D || !Double.isFinite(denominator))
                return null;
            final double t = fa / denominator;
            if (!Double.isFinite(t)) return null;
            if (fa < 0.0D) t0 = Math.max(t0, t);
            else t1 = Math.min(t1, t);
            if (t0 > t1) return null;
        }
        final double[] out = new double[8];
        for (int k = 0; k < 4; k++) {
            out[k] = a[k] + (b[k] - a[k]) * t0;
            out[k + 4] = a[k] + (b[k] - a[k]) * t1;
        }
        return valid(out, 8) ? out : null;
    }

    private static double planeValue(final double[] v, final int plane) {
        switch (plane) {
            case 0: return v[3] + v[0];  // left
            case 1: return v[3] - v[0];  // right
            case 2: return v[3] + v[1];  // bottom
            case 3: return v[3] - v[1];  // top
            case 4: return v[3] + v[2];  // near
            case 5: return v[3] - v[2];  // far
            case 6: return v[3] - W_EPS; // behind viewer / singularity
            default: throw new IllegalArgumentException("plane=" + plane);
        }
    }

    static final class Rect {
        final float left, top, right, bottom;

        Rect(final float left, final float top,
                final float right, final float bottom) {
            this.left = left; this.top = top;
            this.right = right; this.bottom = bottom;
        }

        float width() { return right - left; }
        float height() { return bottom - top; }

        @Override public String toString() {
            return Arrays.toString(new float[]{left, top, right, bottom});
        }
    }
}
