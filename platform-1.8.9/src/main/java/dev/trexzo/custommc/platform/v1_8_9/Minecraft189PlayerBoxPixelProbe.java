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
