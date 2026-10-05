package dev.trexzo.custommc.platform.v1_8_9.input;

import dev.trexzo.custommc.core.ui.UiKey;
import dev.trexzo.custommc.core.ui.UiKeyAction;
import dev.trexzo.custommc.core.ui.UiKeyEvent;
import dev.trexzo.custommc.core.ui.UiKeys;
import dev.trexzo.custommc.core.ui.UiPointerAction;
import dev.trexzo.custommc.core.ui.UiPointerButton;
import dev.trexzo.custommc.core.ui.UiPointerEvent;
import dev.trexzo.custommc.core.ui.UiViewport;

import java.util.Objects;
import java.util.Optional;

public final class LegacyInputTranslator {
    public Optional<UiPointerEvent> pointerButtonEvent(
            final UiViewport viewport,
            final int pixelX,
            final int pixelYFromBottom,
            final int legacyButton,
            final boolean pressed) {
        Objects.requireNonNull(viewport, "viewport");

        final UiPointerButton button =
                pointerButton(legacyButton);
        if (button == null) {
            return Optional.empty();
        }

        final float logicalX =
                pixelX / viewport.scale();
        final float logicalY =
                (viewport.pixelHeight()
                        - 1
                        - pixelYFromBottom)
                        / viewport.scale();

        return Optional.of(
                new UiPointerEvent(
                        logicalX,
                        logicalY,
                        button,
                        pressed
                                ? UiPointerAction.PRESS
                                : UiPointerAction.RELEASE));
    }

    public UiKeyEvent keyEvent(
            final int legacyKeyCode,
            final char character,
            final boolean pressed,
            final boolean repeat,
            final boolean shift,
            final boolean control,
            final boolean alt) {
        if (legacyKeyCode < 0) {
            throw new IllegalArgumentException(
                    "legacyKeyCode must be non-negative");
        }

        final UiKeyAction action;
        if (!pressed) {
            action = UiKeyAction.RELEASE;
        } else if (repeat) {
            action = UiKeyAction.REPEAT;
        } else {
            action = UiKeyAction.PRESS;
        }

        return new UiKeyEvent(
                key(legacyKeyCode),
                action,
                character,
                shift,
                control,
                alt);
    }

    private static UiPointerButton pointerButton(
            final int legacyButton) {
        switch (legacyButton) {
            case 0:
                return UiPointerButton.LEFT;
            case 1:
                return UiPointerButton.RIGHT;
            case 2:
                return UiPointerButton.MIDDLE;
            default:
                return null;
        }
    }

    private static UiKey key(final int legacyKeyCode) {
        switch (legacyKeyCode) {
            case LegacyKeyboardCodes.ESCAPE:
                return UiKeys.ESCAPE;
            case LegacyKeyboardCodes.BACKSPACE:
                return UiKeys.BACKSPACE;
            case LegacyKeyboardCodes.TAB:
                return UiKeys.TAB;
            case LegacyKeyboardCodes.ENTER:
                return UiKeys.ENTER;
            case LegacyKeyboardCodes.SPACE:
                return UiKeys.SPACE;
            case LegacyKeyboardCodes.HOME:
                return UiKeys.HOME;
            case LegacyKeyboardCodes.UP:
                return UiKeys.UP;
            case LegacyKeyboardCodes.LEFT:
                return UiKeys.LEFT;
            case LegacyKeyboardCodes.RIGHT:
                return UiKeys.RIGHT;
            case LegacyKeyboardCodes.END:
                return UiKeys.END;
            case LegacyKeyboardCodes.DOWN:
                return UiKeys.DOWN;
            case LegacyKeyboardCodes.DELETE:
                return UiKeys.DELETE;
            default:
                return new UiKey(
                        "legacy-key-" + legacyKeyCode);
        }
    }
}
