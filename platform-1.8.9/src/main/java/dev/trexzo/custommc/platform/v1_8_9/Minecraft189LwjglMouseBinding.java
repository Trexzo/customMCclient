package dev.trexzo.custommc.platform.v1_8_9;

import org.lwjgl.input.Mouse;

import java.util.Objects;

public final class Minecraft189LwjglMouseBinding {
    private static final int LEGACY_MOUSE_BASE = -100;
    private static final int SUPPORTED_BUTTONS = 3;

    private Minecraft189LwjglMouseBinding() {
    }

    public static void forwardWheelDelta(
            final int wheelDelta) {
        forwardWheelDelta(
                wheelDelta,
                LwjglMousePositionSource.INSTANCE);
    }

    static boolean forwardWheelDelta(
            final int wheelDelta,
            final MousePositionSource source) {
        if (wheelDelta == 0) {
            return false;
        }

        final MousePositionSource positionSource =
                Objects.requireNonNull(
                        source,
                        "source");

        return Minecraft189RuntimeBridge
                .scroll(
                        positionSource.x(),
                        positionSource.yFromBottom(),
                        wheelDelta);
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

    /**
     * Polled during the native HUD frame, but only when ClickGUI is open.
     * Never consumes vanilla Mouse.next() events, changes button state or
     * injects a synthetic click. Grabbed-gameplay mouse is excluded.
     */
    public static boolean forwardHeldPointerPosition() {
        return forwardHeldPointerPosition(LwjglHeldPointerSource.INSTANCE);
    }

    static boolean forwardHeldPointerPosition(final HeldPointerSource source) {
        final HeldPointerSource pointer = Objects.requireNonNull(
                source, "source");
        if (!pointer.available() || !pointer.leftHeld()) {
            return false;
        }
        return Minecraft189RuntimeBridge.pointerMove(
                pointer.x(), pointer.yFromBottom());
    }

    interface HeldPointerSource extends MousePositionSource {
        boolean available();

        boolean leftHeld();
    }

    private enum LwjglHeldPointerSource implements HeldPointerSource {
        INSTANCE;

        @Override
        public boolean available() {
            return Mouse.isCreated() && !Mouse.isGrabbed()
                    && Mouse.isInsideWindow();
        }

        @Override
        public boolean leftHeld() {
            return Mouse.isButtonDown(0);
        }

        @Override
        public int x() {
            return Mouse.getX();
        }

        @Override
        public int yFromBottom() {
            return Mouse.getY();
        }
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
