package dev.trexzo.custommc.platform.v1_8_9;

import org.lwjgl.input.Mouse;

import java.util.Objects;

public final class Minecraft189LwjglMouseBinding {
    private static final int LEGACY_MOUSE_BASE = -100;
    private static final int SUPPORTED_BUTTONS = 3;

    private Minecraft189LwjglMouseBinding() {
    }

    public static void forwardKeyBindingState(
            final int keyCode,
            final boolean pressed) {
        forwardKeyBindingState(
                keyCode,
                pressed,
                LwjglMousePositionSource.INSTANCE);
    }

    static boolean forwardKeyBindingState(
            final int keyCode,
            final boolean pressed,
            final MousePositionSource source) {
        final int button =
                keyCode - LEGACY_MOUSE_BASE;
        if (button < 0
                || button >= SUPPORTED_BUTTONS) {
            return false;
        }

        final MousePositionSource positionSource =
                Objects.requireNonNull(
                        source,
                        "source");

        return Minecraft189RuntimeBridge
                .pointerButton(
                        positionSource.x(),
                        positionSource.yFromBottom(),
                        button,
                        pressed);
    }

    interface MousePositionSource {
        int x();

        int yFromBottom();
    }

    private enum LwjglMousePositionSource
            implements MousePositionSource {
        INSTANCE;

        @Override
        public int x() {
            return Mouse.getX();
        }

        @Override
        public int yFromBottom() {
            return Mouse.getY();
        }
    }
}
