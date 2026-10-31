package dev.trexzo.custommc.platform.v1_8_9.ui;

import dev.trexzo.custommc.core.ui.UiFontHandle;
import dev.trexzo.custommc.core.ui.UiViewport;

public interface LegacyUiHostCallbacks
        extends LegacyUiViewportSource {
    void beginUi(UiViewport viewport);

    void fillRect(
            float x,
            float y,
            float width,
            float height,
            int argb);

    void fillRoundedRect(
            float x,
            float y,
            float width,
            float height,
            float radius,
            int argb);

    void strokeRect(
            float x,
            float y,
            float width,
            float height,
            float thickness,
            int argb);

    void pushClip(
            float x,
            float y,
            float width,
            float height);

    void popClip();

    void drawText(
            UiFontHandle font,
            float x,
            float y,
            String text,
            int argb);

    // Non-native/mock hosts remain source-compatible. On the original
    // LWJGL/Mojang host, this calls FontRenderer.drawString(..., true).
    default void drawTextShadow(final UiFontHandle font,
            final float x, final float y, final String text, final int argb) {
        drawText(font, x, y, text, argb);
    }

    // Existing non-LWJGL test hosts stay source-compatible; -1 requests
    // the bounded historical approximation in the ArrayList renderer.
    default int measureText(final String text) { return -1; }

    void endUi();
}
