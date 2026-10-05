package dev.trexzo.custommc.core.ui;

import java.util.Objects;

public final class UiKeyEvent {
    private final UiKey key;
    private final UiKeyAction action;
    private final char character;
    private final boolean shift;
    private final boolean control;
    private final boolean alt;

    public UiKeyEvent(
            final UiKey key,
            final UiKeyAction action,
            final char character,
            final boolean shift,
            final boolean control,
            final boolean alt) {
        this.key = Objects.requireNonNull(key, "key");
        this.action = Objects.requireNonNull(
                action,
                "action");
        this.character = character;
        this.shift = shift;
        this.control = control;
        this.alt = alt;
    }

    public UiKey key() {
        return key;
    }

    public UiKeyAction action() {
        return action;
    }

    public char character() {
        return character;
    }

    public boolean hasCharacter() {
        return character != '\0';
    }

    public boolean shift() {
        return shift;
    }

    public boolean control() {
        return control;
    }

    public boolean alt() {
        return alt;
    }
}
