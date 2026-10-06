package dev.trexzo.custommc.platform.v1_8_9.ui;

import dev.trexzo.custommc.core.ui.UiViewport;

public interface LegacyGlApi {
    void beginUi(UiViewport viewport);

    void endUi();

    void prepareShapeState();

    void prepareTextState();

    void setColorArgb(int argb);

    void setLineWidth(float width);

    void beginPrimitive(LegacyUiPrimitiveMode primitive);

    void vertex(float x, float y);

    void endPrimitive();

    void applyScissor(LegacyFramebufferRect rect);

    void disableScissor();
}
