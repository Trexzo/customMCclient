package dev.trexzo.custommc.platform.v1_8_9;

import java.util.Objects;

/**
 * Pure 1.8.9 HUD presentation bounds; never writes HUD settings or changes
 * enabled module state. Slinky/Rise-inspired text-first list stays inside the
 * real logical viewport even at 854x480 and after a GUI-scale change.
 */
final class Minecraft189ArrayListLayout {
    static final float LINE_HEIGHT = 12.0F;
    private static final float SCREEN_PADDING = 4.0F;
    private static final float MIN_TOP_ROOM = 16.0F;

    private Minecraft189ArrayListLayout() { }

    static float safeTop(final float configuredY, final float screenHeight) {
        if (!Float.isFinite(configuredY) || !Float.isFinite(screenHeight)
                || screenHeight <= 0.0F) return 0.0F;
        return Math.min(Math.max(SCREEN_PADDING, configuredY),
                Math.max(0.0F, screenHeight - MIN_TOP_ROOM));
    }

    static float safeLeft(final float configuredX, final float screenWidth) {
        if (!Float.isFinite(configuredX) || !Float.isFinite(screenWidth)
                || screenWidth <= 0.0F) return 0.0F;
        return Math.min(Math.max(SCREEN_PADDING, configuredX),
                Math.max(0.0F, screenWidth - SCREEN_PADDING));
    }

    static float safeRightEdge(final float inset, final float screenWidth) {
        if (!Float.isFinite(inset) || !Float.isFinite(screenWidth)
                || screenWidth <= 0.0F) return 0.0F;
        return Math.min(Math.max(0.0F, screenWidth - inset),
                Math.max(0.0F, screenWidth - SCREEN_PADDING));
    }

    static int rowCapacity(final float screenHeight, final float top) {
        if (!Float.isFinite(top) || !Float.isFinite(screenHeight)
                || top < 0.0F || screenHeight < 0.0F) return 0;
        final double available = (double) screenHeight - top - SCREEN_PADDING;
        return (int) Math.min(128.0D, Math.max(0.0D,
                Math.floor(available / LINE_HEIGHT)));
    }

    static int visibleRows(final int enabledCount, final int configuredMax,
            final int capacity, final boolean showOverflow) {
        if (enabledCount < 0 || configuredMax < 0 || capacity < 0) {
            throw new IllegalArgumentException("negative ArrayList capacity");
        }
        final int ordinary = Math.min(enabledCount,
                Math.min(configuredMax, capacity));
        // Reserve the final available row for an accurate overflow summary
        // when, and only when, there are more enabled modules than can fit.
        if (showOverflow && enabledCount > ordinary && ordinary == capacity) {
            return Math.max(0, ordinary - 1);
        }
        return ordinary;
    }

    static String fitLabel(final String label, final float maxWidth) {
        Objects.requireNonNull(label, "label");
        if (!Float.isFinite(maxWidth) || maxWidth <= 0.0F) return "";
        if (Minecraft189HudTextMetrics.width(label) <= maxWidth) return label;
        final String ellipsis = "...";
        final int dotsWidth = Minecraft189HudTextMetrics.width(ellipsis);
        if (dotsWidth > maxWidth) return "";
        int end = label.length();
        while (end > 0
                && Minecraft189HudTextMetrics.width(label.substring(0, end))
                    + dotsWidth > maxWidth) {
            end--;
        }
        // Avoid trailing spaces on an ellipsized module name.
        while (end > 0 && label.charAt(end - 1) == ' ') end--;
        return end == 0 ? ellipsis : label.substring(0, end) + ellipsis;
    }
}
