package dev.trexzo.custommc.platform.v1_8_9.ui;

public interface LegacyUiBatchGraphics
        extends LegacyUiGraphics {
    boolean supportsShapeBatching();

    void beginShapeBatch();

    void endShapeBatch();
}
