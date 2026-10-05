package dev.trexzo.custommc.core.ui.clickgui;

public final class ClickGuiMetrics {
    public static final float PAGE_HEIGHT = 30.0F;
    public static final float PAGE_GAP = 4.0F;
    public static final float NAVIGATION_SCROLL_STEP = 28.0F;

    private ClickGuiMetrics() {
    }

    public static float navigationContentHeight(
            final int pageCount) {
        if (pageCount < 0) {
            throw new IllegalArgumentException(
                    "pageCount must be non-negative");
        }
        if (pageCount == 0) {
            return 0.0F;
        }
        return pageCount * PAGE_HEIGHT
                + (pageCount - 1) * PAGE_GAP;
    }

    public static float maxNavigationScroll(
            final int pageCount,
            final float viewportHeight) {
        if (Float.isNaN(viewportHeight)
                || Float.isInfinite(viewportHeight)
                || viewportHeight < 0.0F) {
            throw new IllegalArgumentException(
                    "viewportHeight must be finite and non-negative");
        }
        return Math.max(
                0.0F,
                navigationContentHeight(pageCount)
                        - viewportHeight);
    }

    public static float clampNavigationScroll(
            final float scroll,
            final int pageCount,
            final float viewportHeight) {
        if (Float.isNaN(scroll)
                || Float.isInfinite(scroll)) {
            throw new IllegalArgumentException(
                    "scroll must be finite");
        }
        return Math.max(
                0.0F,
                Math.min(
                        scroll,
                        maxNavigationScroll(
                                pageCount,
                                viewportHeight)));
    }
}
