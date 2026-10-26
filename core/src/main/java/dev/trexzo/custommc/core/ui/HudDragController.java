package dev.trexzo.custommc.core.ui;

import java.util.Objects;

public final class HudDragController {
    private final HudLayoutState state;

    private String widgetId;
    private HudPlacement originalPlacement;
    private HudPlacement originalOverride;
    private UiViewport boundedViewport;
    private UiBounds initialBounds;
    private float startX;
    private float startY;
    private boolean active;

    public HudDragController(final HudLayoutState state) {
        this.state = Objects.requireNonNull(state, "state");
    }

    public boolean begin(
            final float x,
            final float y,
            final UiPointerButton button,
            final HudLayoutSnapshot snapshot) {
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

        final HudLayoutEntry hit =
                snapshot.topmostAt(x, y);
        if (hit == null) {
            return false;
        }

        widgetId = hit.widget().id();
        originalPlacement = hit.placement();
        originalOverride = state.overrideFor(widgetId);
        startX = x;
        startY = y;
        active = true;
        return true;
    }

    /**
     * Begins an opt-in viewport-aware drag. Legacy callers of begin(...)
     * retain their original unrestricted movement behavior.
     *
     * Widgets larger than the viewport are pinned to its leading edge
     * on that axis, because full visibility is impossible.
     */
    public boolean beginWithinViewport(
            final float x,
            final float y,
            final UiPointerButton button,
            final HudLayoutSnapshot snapshot,
            final UiViewport viewport) {
        Objects.requireNonNull(viewport, "viewport");
        if (!Float.isFinite(viewport.logicalWidth())
                || !Float.isFinite(viewport.logicalHeight())) {
            throw new IllegalArgumentException(
                    "viewport logical dimensions must be finite");
        }
        if (!begin(x, y, button, snapshot)) {
            return false;
        }

        initialBounds = snapshot.topmostAt(x, y).bounds();
        boundedViewport = viewport;
        return true;
    }

    public void move(
            final float x,
            final float y) {
        requireFinite(x, "x");
        requireFinite(y, "y");
        requireActive();

        final float offsetX;
        final float offsetY;
        if (boundedViewport == null) {
            offsetX = originalPlacement.offsetX() + (x - startX);
            offsetY = originalPlacement.offsetY() + (y - startY);
        } else {
            final double maxX = Math.max(0.0D,
                    (double) boundedViewport.logicalWidth()
                            - initialBounds.width());
            final double maxY = Math.max(0.0D,
                    (double) boundedViewport.logicalHeight()
                            - initialBounds.height());

            // Work in double so extreme (but finite) pointer positions
            // cannot overflow an intermediate float delta.
            final double requestedX = initialBounds.x()
                    + ((double) x - startX);
            final double requestedY = initialBounds.y()
                    + ((double) y - startY);
            final double boundedX = clamp(requestedX, 0.0D, maxX);
            final double boundedY = clamp(requestedY, 0.0D, maxY);
            offsetX = (float) ((double) originalPlacement.offsetX()
                    + boundedX - initialBounds.x());
            offsetY = (float) ((double) originalPlacement.offsetY()
                    + boundedY - initialBounds.y());
        }

        state.set(
                widgetId,
                new HudPlacement(
                        originalPlacement.anchor(),
                        offsetX,
                        offsetY));
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
        boundedViewport = null;
        initialBounds = null;
        startX = 0.0F;
        startY = 0.0F;
    }

    private static double clamp(
            final double value,
            final double minimum,
            final double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
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
