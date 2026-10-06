package dev.trexzo.custommc.platform.v1_8_9;

import org.lwjgl.input.Keyboard;

import java.util.Objects;

public final class Minecraft189LwjglKeyboardBinding {
    private static final int LEFT_SHIFT = 42;
    private static final int RIGHT_SHIFT = 54;
    private static final int LEFT_CONTROL = 29;
    private static final int RIGHT_CONTROL = 157;
    private static final int LEFT_ALT = 56;
    private static final int RIGHT_ALT = 184;

    private Minecraft189LwjglKeyboardBinding() {
    }

    public static void forwardCurrentEvent() {
        forward(
                LwjglKeyboardEventSource.INSTANCE);
    }

    static boolean forward(
            final KeyboardEventSource source) {
        final KeyboardEventSource eventSource =
                Objects.requireNonNull(
                        source,
                        "source");

        return Minecraft189RuntimeBridge.key(
                eventSource.eventKey(),
                eventSource.eventCharacter(),
                eventSource.eventKeyState(),
                eventSource.repeatEvent(),
                modifierDown(
                        eventSource,
                        LEFT_SHIFT,
                        RIGHT_SHIFT),
                modifierDown(
                        eventSource,
                        LEFT_CONTROL,
                        RIGHT_CONTROL),
                modifierDown(
                        eventSource,
                        LEFT_ALT,
                        RIGHT_ALT));
    }

    private static boolean modifierDown(
            final KeyboardEventSource source,
            final int left,
            final int right) {
        return source.keyDown(left)
                || source.keyDown(right);
    }

    interface KeyboardEventSource {
        int eventKey();

        char eventCharacter();

        boolean eventKeyState();

        boolean repeatEvent();

        boolean keyDown(int keyCode);
    }

    private enum LwjglKeyboardEventSource
            implements KeyboardEventSource {
        INSTANCE;

        @Override
        public int eventKey() {
            return Keyboard.getEventKey();
        }

        @Override
        public char eventCharacter() {
            return Keyboard.getEventCharacter();
        }

        @Override
        public boolean eventKeyState() {
            return Keyboard.getEventKeyState();
        }

        @Override
        public boolean repeatEvent() {
            return Keyboard.isRepeatEvent();
        }

        @Override
        public boolean keyDown(
                final int keyCode) {
            return Keyboard.isKeyDown(
                    keyCode);
        }
    }
}
