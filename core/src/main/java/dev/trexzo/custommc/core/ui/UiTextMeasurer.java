package dev.trexzo.custommc.core.ui;

public interface UiTextMeasurer {
    UiTextMetrics measure(
            UiFontHandle font,
            String text);
}
