package dev.trexzo.custommc.platform.v1_8_9;

import java.util.Objects;

/**
 * Conservative Minecraft 1.8.9 default glyph-width estimates, not native
 * FontRenderer.getStringWidth measurements. Formatting codes consume no
 * rendered space; native drawString stays the source of displayed text.
 */
final class Minecraft189HudTextMetrics {
    private Minecraft189HudTextMetrics() { }

    static int width(final String label) {
        Objects.requireNonNull(label, "label");
        int width = 0;
        for (int i = 0; i < label.length(); i++) {
            final char ch = label.charAt(i);
            if (ch == '\u00a7' && i + 1 < label.length()) {
                i++;
                continue;
            }
            if (ch == ' ') width += 4;
            else if ("!|i:l'.,;".indexOf(ch) >= 0) width += 2;
            else if ("I[](){}tfr\"".indexOf(ch) >= 0) width += 4;
            else if ("mwMW@".indexOf(ch) >= 0) width += 6;
            else width += 6;
        }
        return width;
    }
}
