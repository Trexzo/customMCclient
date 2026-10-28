package dev.trexzo.custommc.platform.v1_8_9.ui;

public interface Minecraft189FontRendererAccess {
    int drawString(
            String text,
            float x,
            float y,
            int argb,
            boolean shadow);

    /**
     * Optional to preserve existing synthetic renderer hosts. The exact
     * obfuscated 1.8.9 FontRenderer receives a native bridge by ASM.
     * Negative means the host does not support native measurement.
     */
    default int getStringWidth(final String text) {
        return -1;
    }
}
