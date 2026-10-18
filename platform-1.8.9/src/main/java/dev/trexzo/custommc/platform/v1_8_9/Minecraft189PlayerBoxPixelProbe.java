package dev.trexzo.custommc.platform.v1_8_9;

/**
 * Exact UI top-stroke sample in the framebuffer's GL lower-left coordinates.
 * Only used by the explicit original-game pixel acceptance (never gameplay).
 */
final class Minecraft189PlayerBoxPixelProbe {
    private Minecraft189PlayerBoxPixelProbe() { }

    static int[] sampleTopStroke(final float left, final float top,
            final float width, final float height, final float thickness,
            final float scale, final int framebufferWidth,
            final int framebufferHeight) {
        if (!Float.isFinite(left) || !Float.isFinite(top)
                || !Float.isFinite(width) || !Float.isFinite(height)
                || !Float.isFinite(thickness) || !Float.isFinite(scale)
                || width <= 2 || height <= 2 || thickness <= 0 || scale <= 0
                || framebufferWidth < 100 || framebufferHeight < 100)
            return null;
        // The center of the actual FULLY OPAQUE cyan top stroke. Never
        // sample the translucent underlay, polygon edges, or remote skin.
        final double x = (left + width * 0.5D) * scale;
        final double fromTop = (top + thickness * 0.5D) * scale;
        if (!Double.isFinite(x) || !Double.isFinite(fromTop)
                || x < 0 || fromTop < 0
                || x >= framebufferWidth || fromTop >= framebufferHeight)
            return null;
        final int ix = (int)Math.floor(x);
        final int iy = framebufferHeight - 1 - (int)Math.floor(fromTop);
        return new int[]{ix, iy};
    }

    /**
     * Candidate pixels from the four ACTUALLY DRAWN opaque cyan stroke
     * rectangles, clipped to the real framebuffer. The original projected
     * AABB may extend beyond the X11 viewport: its top stroke can then be
     * invisible even while a genuine left/right/bottom segment is drawn.
     * Only internal pixels of visible foreground segments are eligible.
     * No sample represents a synthetic success; the caller still MUST
     * perform actual GL11.glReadPixels and enforce isForegroundColor.
     */
    static int[][] sampleVisibleStrokePixels(
            final float left, final float top,
            final float width, final float height,
            final float thickness, final float scale,
            final int framebufferWidth, final int framebufferHeight) {
        if (!Float.isFinite(left) || !Float.isFinite(top)
                || !Float.isFinite(width) || !Float.isFinite(height)
                || !Float.isFinite(thickness) || !Float.isFinite(scale)
                || width <= 2.0F || height <= 2.0F
                || thickness <= 0.0F || scale <= 0.0F
                || framebufferWidth < 100 || framebufferHeight < 100) {
            return new int[0][2];
        }
        final double t = Math.min(thickness, Math.min(width, height) * 0.5D);
        final double centerHeight = Math.max(0D, height - 2D * t);
        // Same four foreground rectangles as BoxPass.drawOutline.
        final double[][] segments = {
                {left, top, width, t},
                {left, top + height - t, width, t},
                {left, top + t, t, centerHeight},
                {left + width - t, top + t, t, centerHeight}
        };
        final int[][] candidates = new int[4][2];
        int used = 0;
        for (final double[] segment : segments) {
            if (segment[2] <= 0D || segment[3] <= 0D) continue;
            final double x0 = Math.max(0D, segment[0] * scale);
            final double y0 = Math.max(0D, segment[1] * scale);
            final double x1 = Math.min(framebufferWidth,
                    (segment[0] + segment[2]) * scale);
            final double y1 = Math.min(framebufferHeight,
                    (segment[1] + segment[3]) * scale);
            // Require a genuine pixel footprint inside opaque foreground,
            // not a cropped edge, translucent underlay or outside area.
            if (!Double.isFinite(x0) || !Double.isFinite(y0)
                    || !Double.isFinite(x1) || !Double.isFinite(y1)
                    || x1 - x0 <= 1D || y1 - y0 <= 1D) continue;
            final int px = (int)Math.floor((x0 + x1) * 0.5D);
            final int fromTop = (int)Math.floor((y0 + y1) * 0.5D);
            if (px < 0 || px >= framebufferWidth
                    || fromTop < 0 || fromTop >= framebufferHeight) continue;
            final int py = framebufferHeight - 1 - fromTop;
            boolean duplicate = false;
            for (int i = 0; i < used; i++) {
                if (candidates[i][0] == px && candidates[i][1] == py) {
                    duplicate = true;
                    break;
                }
            }
            if (!duplicate) {
                candidates[used][0] = px;
                candidates[used][1] = py;
                used++;
            }
        }
        final int[][] result = new int[used][2];
        for (int i = 0; i < used; i++) {
            result[i][0] = candidates[i][0];
            result[i][1] = candidates[i][1];
        }
        return result;
    }

    /**
     * The first successful original native HUD draw does not prove that
     * a glReadPixels from that same frame saw the opaque foreground.
     * Retry on every independently source-verified subsequent frame until
     * a real framebuffer sample passes. Wrong player, invalid rectangle
     * and normal gameplay (acceptance switch OFF) never arm the probe.
     */
    static boolean shouldProbeOriginalRemote(
            final boolean acceptanceEnabled,
            final boolean pixelAlreadyReported,
            final String expectedUuid, final String actualUuid,
            final float width, final float height) {
        return acceptanceEnabled && !pixelAlreadyReported
                && expectedUuid != null && expectedUuid.equals(actualUuid)
                && Float.isFinite(width) && Float.isFinite(height)
                && width > 1.0F && height > 1.0F;
    }

    static boolean isForegroundColor(final int red, final int green,
            final int blue) {
        // GL11.glColor4f(112,217,255,255) is fully opaque. Small
        // framebuffer quantization tolerance, never accept dark underlay.
        return Math.abs(red - 112) <= 12
                && Math.abs(green - 217) <= 12
                && Math.abs(blue - 255) <= 12;
    }
}
