package dev.trexzo.custommc.core.ui;

import java.util.Objects;

public final class UiPointerEvent {
    private final float x;
    private final float y;
    private final UiPointerButton button;
    private final UiPointerAction action;

    public UiPointerEvent(
            final float x,
            final float y,
            final UiPointerButton button,
            final UiPointerAction action) {
        requireFinite(x, "x");
        requireFinite(y, "y");
        this.x = x;
        this.y = y;
        this.button = Objects.requireNonNull(
                button,
                "button");
        this.action = Objects.requireNonNull(
                action,
                "action");
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public UiPointerButton button() {
        return button;
    }

    public UiPointerAction action() {
        return action;
    }

    private static void requireFinite(
            final float value,
            final String name) {
        if (Float.isNaN(value)
                || Float.isInfinite(value)) {
            throw new IllegalArgumentException(
                    name + " must be finite");
        }
    }
}
