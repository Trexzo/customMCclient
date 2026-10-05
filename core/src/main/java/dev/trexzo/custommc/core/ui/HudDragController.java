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

    public void move(
            final float x,
            final float y) {
        requireFinite(x, "x");
        requireFinite(y, "y");
        requireActive();

        state.set(
                widgetId,
                new HudPlacement(
                        originalPlacement.anchor(),
                        originalPlacement.offsetX()
                                + (x - startX),
                        originalPlacement.offsetY()
                                + (y - startY)));
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
