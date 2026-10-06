package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.ui.clickgui.ClickGuiModel;
import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;

import java.util.Objects;

public final class Minecraft189ClickGuiToggleController {
    public static final int DEFAULT_TOGGLE_KEY =
            LegacyKeyboardCodes.RIGHT_SHIFT;

    private final ClickGuiModel model;
    private final int toggleKeyCode;

    public Minecraft189ClickGuiToggleController(
            final ClickGuiModel model) {
        this(
                model,
                DEFAULT_TOGGLE_KEY);
    }

    Minecraft189ClickGuiToggleController(
            final ClickGuiModel model,
            final int toggleKeyCode) {
        this.model =
                Objects.requireNonNull(
                        model,
                        "model");
        if (toggleKeyCode < 0) {
            throw new IllegalArgumentException(
                    "toggleKeyCode must be non-negative");
        }
        this.toggleKeyCode = toggleKeyCode;
    }

    public boolean key(
            final int legacyKeyCode,
            final boolean pressed,
            final boolean repeat) {
        if (legacyKeyCode != toggleKeyCode
                || !pressed
                || repeat) {
            return false;
        }

        model.toggle();
        return true;
    }

    public int toggleKeyCode() {
        return toggleKeyCode;
    }
}
