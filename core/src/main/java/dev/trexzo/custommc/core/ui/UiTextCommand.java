package dev.trexzo.custommc.core.ui;

import java.util.Objects;

public final class UiTextCommand implements UiDrawCommand {
    private final int layer;
    private final float x;
    private final float y;
    private final String text;
    private final int argb;

    public UiTextCommand(
            final int layer,
            final float x,
            final float y,
            final String text,
            final int argb) {
        requireFinite(x, "x");
        requireFinite(y, "y");
        this.layer = layer;
        this.x = x;
        this.y = y;
        this.text = Objects.requireNonNull(text, "text");
        this.argb = argb;
    }

    @Override
    public int layer() {
        return layer;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public String text() {
        return text;
    }

    public int argb() {
        return argb;
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
