package dev.trexzo.custommc.platform.v1_8_9;

import java.util.HashSet;
import java.util.Set;

public final class Minecraft189InputState {
    private final Set<Integer> pressedKeys =
            new HashSet<Integer>();
    private final Set<Integer> pressedButtons =
            new HashSet<Integer>();

    public synchronized void key(
            final int legacyKeyCode,
            final boolean pressed) {
        if (legacyKeyCode < 0) {
            throw new IllegalArgumentException(
                    "legacyKeyCode must be non-negative");
        }
        if (pressed) {
            pressedKeys.add(
                    legacyKeyCode);
        } else {
            pressedKeys.remove(
                    legacyKeyCode);
        }
    }

    public synchronized void pointerButton(
            final int legacyButton,
            final boolean pressed) {
        if (legacyButton < 0) {
            return;
        }
        if (pressed) {
            pressedButtons.add(
                    legacyButton);
        } else {
            pressedButtons.remove(
                    legacyButton);
        }
    }

    public synchronized boolean keyPressed(
            final int legacyKeyCode) {
        return pressedKeys.contains(
                legacyKeyCode);
    }

    public synchronized boolean pointerPressed(
            final int legacyButton) {
        return pressedButtons.contains(
                legacyButton);
    }

    public synchronized void clear() {
        pressedKeys.clear();
        pressedButtons.clear();
    }
}
