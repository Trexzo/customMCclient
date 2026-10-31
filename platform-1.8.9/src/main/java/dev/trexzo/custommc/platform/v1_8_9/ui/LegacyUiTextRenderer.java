package dev.trexzo.custommc.platform.v1_8_9.ui;

import dev.trexzo.custommc.core.ui.UiFontHandle;

public interface LegacyUiTextRenderer {
    void drawText(
            UiFontHandle font,
            float x,
            float y,
            String text,
            int argb);

    // Older test/alternate renderers retain their existing plain-text behavior.
    default void drawTextShadow(final UiFontHandle font,
            final float x, final float y, final String text, final int argb) {
        drawText(font, x, y, text, argb);
    }

    // Negative means the legacy host has no native measurement facility.
    default int measureText(final String text) { return -1; }
}
