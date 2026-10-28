package dev.trexzo.custommc.platform.v1_8_9.ui;

import dev.trexzo.custommc.core.ui.UiFontHandle;

/** Optional exact string measurement for the active native renderer font. */
public interface LegacyUiTextMeasurer extends LegacyUiTextRenderer {
    /** Returns a nonnegative logical-pixel width, or -1 if unavailable. */
    int textWidth(UiFontHandle font, String text);
}
