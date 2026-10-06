package dev.trexzo.custommc.platform.v1_8_9.ui;

import dev.trexzo.custommc.core.ui.UiFontHandle;

public interface LegacyUiTextRenderer {
    void drawText(
            UiFontHandle font,
            float x,
            float y,
            String text,
            int argb);
}
