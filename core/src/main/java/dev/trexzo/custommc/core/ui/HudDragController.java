package dev.trexzo.custommc.core.ui;

import java.util.Objects;

public final class HudDragController {
    private final HudLayoutState state;

    private String widgetId;
    private HudPlacement originalPlacement;
    private HudPlacement originalOverride;
    private float startX;
    private float startY;
    private boolean active;
    // Null preserves the original unbounded, compatibility-mode drag API.
    private UiViewport boundedViewport;
    private UiBounds originalBounds;

    public HudDragController(final HudLayoutState state) {
        this.state = Objects.requireNonNull(state, "state");
    }

    public boolean begin(
            final float x,
            final float y,
            final UiPointerButton button,
            final HudLayoutSnapshot snapshot) {
        return beginInternal(x, y, button, snapshot, null);
    }

    /**
     * Begin an editor drag constrained to the visible logical viewport.
     * Old callers may still use the original unbounded overload.
     */
    public boolean begin(
            final float x,
            final float y,
            final UiPointerButton button,
            final HudLayoutSnapshot snapshot,
            final UiViewport viewport) {
        Objects.requireNonNull(viewport, "viewport");
        requireFinite(viewport.logicalWidth(), "viewport width");
        requireFinite(viewport.logicalHeight(), "viewport height");
        return beginInternal(x, y, button, snapshot, viewport);
    }

    private boolean beginInternal(
            final float x,
            final float y,
            final UiPointerButton button,
            final HudLayoutSnapshot snapshot,
            final UiViewport viewport) {
        requireFinite(x, "x");
        requireFinite(y, "y");
        Objects.requireNonNull(button, "button");
        Objects.requireNonNull(snapshot, "snapshot");

        if (active) {
            throw new IllegalStateException(
                    "HUD drag is already active");
        }
        if (button != UiPointerButton.LEFT) {
            return false;
        }

        final HudLayoutEntry hit = snapshot.topmostAt(x, y);
        if (hit == null) {
            return false;
        }

        widgetId = hit.widget().id();
        originalPlacement = hit.placement();
        originalOverride = state.overrideFor(widgetId);
        originalBounds = hit.bounds();
        boundedViewport = viewport;
        startX = x;
        startY = y;
        active = true;
        return true;
    }

    public void move(
            final float x,
            final float y) {
        update(x, y, 0.0F);
    }

    /**
     * Snap to a configurable logical-pixel grid while staying on-screen.
     * Useful for an editor's optional "align to grid" interaction.
     */
    public void moveSnapped(
            final float x,
            final float y,
            final float gridStep) {
        requireFinite(gridStep, "gridStep");
        if (gridStep <= 0.0F) {
            throw new IllegalArgumentException(
                    "gridStep must be positive");
        }
        requireActive();
        if (boundedViewport == null) {
            throw new IllegalStateException(
                    "snapping requires a viewport-bounded drag");
        }
        update(x, y, gridStep);
    }

    private void update(
            final float x,
            final float y,
            final float gridStep) {
        requireFinite(x, "x");
        requireFinite(y, "y");
        requireActive();

        if (boundedViewport == null) {
            // Preserve the original API's exact unbounded calculation.
            state.set(
                    widgetId,
                    new HudPlacement(
                            originalPlacement.anchor(),
                            originalPlacement.offsetX() + (x - startX),
                            originalPlacement.offsetY() + (y - startY)));
            return;
        }

        final double maximumX = Math.max(
                0.0D, (double) boundedViewport.logicalWidth()
                        - originalBounds.width());
        final double maximumY = Math.max(
                0.0D, (double) boundedViewport.logicalHeight()
                        - originalBounds.height());
        double left = clamp(
                (double) originalBounds.x() + ((double) x - startX),
                maximumX);
        double top = clamp(
                (double) originalBounds.y() + ((double) y - startY),
                maximumY);
        if (gridStep > 0.0F) {
            left = clamp(
                    Math.round(left / gridStep) * (double) gridStep,
                    maximumX);
            top = clamp(
                    Math.round(top / gridStep) * (double) gridStep,
                    maximumY);
        }

        // Keep the original anchor and convert screen coordinates back
        // into anchor-relative offsets. Never store unbounded NaN/Infinity.
        final double newX = (double) originalPlacement.offsetX()
                + left - originalBounds.x();
        final double newY = (double) originalPlacement.offsetY()
                + top - originalBounds.y();
        if (!Double.isFinite(newX) || !Double.isFinite(newY)
                || Math.abs(newX) > Float.MAX_VALUE
                || Math.abs(newY) > Float.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "resulting HUD placement cannot be represented");
        }
        state.set(widgetId,
                new HudPlacement(originalPlacement.anchor(),
                        (float) newX, (float) newY));
    }

    private static double clamp(
            final double candidate,
            final double upper) {
        return Math.max(0.0D, Math.min(candidate, upper));
    }

    public void commit() {
        requireActive();
        clearSession();
    }

    public void cancel() {
        requireActive();

        if (originalOverride == null) {
            state.clear(widgetId);
        } else {
            state.set(widgetId, originalOverride);
        }

        clearSession();
    }

    public boolean active() {
        return active;
    }

    public String widgetId() {
        return widgetId;
    }

    private void requireActive() {
        if (!active) {
            throw new IllegalStateException(
                    "no HUD drag is active");
        }
    }

    private void clearSession() {
        active = false;
        widgetId = null;
        originalPlacement = null;
        originalOverride = null;
        startX = 0.0F;
        startY = 0.0F;
        boundedViewport = null;
        originalBounds = null;
    }

    private static void requireFinite(
            final float value,
            final String name) {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            throw new IllegalArgumentException(
                    name + " must be finite");
        }
    }
}
