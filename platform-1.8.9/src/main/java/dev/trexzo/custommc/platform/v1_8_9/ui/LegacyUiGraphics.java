package dev.trexzo.custommc.platform.v1_8_9.ui;

import dev.trexzo.custommc.core.ui.UiViewport;

public interface LegacyUiGraphics {
    void begin(UiViewport viewport);

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
            float x,
            float y,
            String text,
            int argb);

    void end();
}
