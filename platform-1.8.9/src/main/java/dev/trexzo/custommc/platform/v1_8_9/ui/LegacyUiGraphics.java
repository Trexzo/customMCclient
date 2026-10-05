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

    void drawText(
            float x,
            float y,
            String text,
            int argb);

    void end();
}
