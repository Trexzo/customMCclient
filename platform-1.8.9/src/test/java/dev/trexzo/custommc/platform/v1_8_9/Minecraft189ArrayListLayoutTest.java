package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189ArrayListLayoutTest {
    @Test
    void ordinary1280x720PositionAndSettingsRemainPixelCompatible() {
        assertEquals(24.0F, Minecraft189ArrayListLayout.safeTop(24, 720), 0.001F);
        assertEquals(40.0F, Minecraft189ArrayListLayout.safeLeft(40, 1280), 0.001F);
        assertEquals(1240.0F,
                Minecraft189ArrayListLayout.safeRightEdge(40, 1280), 0.001F);
        assertTrue(Minecraft189ArrayListLayout.rowCapacity(720, 24) >= 5);
        assertEquals(5, Minecraft189ArrayListLayout.visibleRows(
                5, 128, 50, false));
        assertEquals(2, Minecraft189ArrayListLayout.visibleRows(
                5, 2, 50, true));
    }

    @Test
    void veryDenseActual854x480HudNeverRendersBelowBottomEdge() {
        final float top = Minecraft189ArrayListLayout.safeTop(24, 480);
        final int cap = Minecraft189ArrayListLayout.rowCapacity(480, top);
        assertTrue(cap > 0);
        assertEquals(cap, Minecraft189ArrayListLayout.visibleRows(
                85, 128, cap, false));
        final int rows = Minecraft189ArrayListLayout.visibleRows(
                85, 128, cap, true);
        assertEquals(cap - 1, rows);
        assertEquals(11.0F, Minecraft189ArrayListLayout.LINE_HEIGHT, .001F);
        assertTrue(top + (rows + 1)
                * Minecraft189ArrayListLayout.LINE_HEIGHT <= 480 - 4.0F);
        assertEquals(0, Minecraft189ArrayListLayout.visibleRows(
                85, 128, 0, true));
        assertEquals(0, Minecraft189ArrayListLayout.rowCapacity(12, 10));
    }

    @Test
    void compactPitchIncreasesDensityWithoutDrawingOutsideSmallViewport() {
        final float y = Minecraft189ArrayListLayout.safeTop(24, 120);
        final int expected = (int) Math.floor((120 - y - 4) / 11.0F);
        assertEquals(expected, Minecraft189ArrayListLayout.rowCapacity(120, y));
        assertTrue(y + expected * Minecraft189ArrayListLayout.LINE_HEIGHT
                <= 120 - 4);
        // An overflowing viewport reserves its last full 11px row for
        // a truthful '+ N more' item, never half a glyph or fake module.
        final int displayed = Minecraft189ArrayListLayout.visibleRows(
                128, 128, expected, true);
        assertEquals(expected - 1, displayed);
        assertTrue(y + (displayed + 1)
                * Minecraft189ArrayListLayout.LINE_HEIGHT <= 120 - 4);
        assertEquals(1, Minecraft189ArrayListLayout.rowCapacity(480, 464));
    }

    @Test
    void savedOffscreenPositionsRecoverWithoutOverwritingUserPreferences() {
        assertEquals(464.0F,
                Minecraft189ArrayListLayout.safeTop(4096, 480), 0.001F);
        assertEquals(846.0F,
                Minecraft189ArrayListLayout.safeRightEdge(4096, 854), 0.001F);
        assertEquals(8.0F,
                Minecraft189ArrayListLayout.safeLeft(4096, 854), 0.001F);
        assertEquals(1,
                Minecraft189ArrayListLayout.rowCapacity(480, 464));
    }

    @Test
    void nativeFontWidthsDriveEllipsisInsteadOfApproximateGlyphTable() {
        final java.util.function.ToIntFunction<String> original =
                text -> text.length() * 9;
        assertEquals("Wide...",
                Minecraft189ArrayListLayout.fitLabel(
                        "Wide Name", 63.0F, original));
        assertEquals("", Minecraft189ArrayListLayout.fitLabel(
                "Wide Name", 10.0F, original));
        assertEquals("Name", Minecraft189ArrayListLayout.fitLabel(
                "Name", 36.0F, original));
        assertEquals(28.0F,
                Minecraft189ArrayListModule.rightAlignedTextX(64, 8, 20),
                .001F);
    }

    @Test
    void ellipsisNeverCutsUnicodeSurrogateOrMinecraftFormattingCode() {
        final java.util.function.ToIntFunction<String> sourceFont =
                Minecraft189ArrayListLayoutTest::simulatedNativeFontWidth;
        assertEquals("A...", Minecraft189ArrayListLayout.fitLabel(
                "A\uD83D\uDC8EB", 12, sourceFont));
        assertEquals("A\uD83D\uDC8EB", Minecraft189ArrayListLayout.fitLabel(
                "A\uD83D\uDC8EB", 18, sourceFont));
        assertEquals("AB...", Minecraft189ArrayListLayout.fitLabel(
                "AB\u00a7aCD", 18, sourceFont));
        assertEquals("AB...", Minecraft189ArrayListLayout.fitLabel(
                "AB\u00a7lCDEFG", 27, sourceFont));
        // A complete styling prefix can be kept when visible glyphs follow.
        assertEquals("\u00a7aGreen...", Minecraft189ArrayListLayout.fitLabel(
                "\u00a7aGreen Extended", 42, sourceFont));
        assertEquals("...", Minecraft189ArrayListLayout.fitLabel(
                "\u00a7bLong", 6, sourceFont));
    }

    @Test
    void deterministicNativeFormattedUnicodeWidthsRemainBounded() {
        final java.util.Random random = new java.util.Random(41989L);
        final String[] glyphs = {"A", "M", " ", "\uD83D\uDC8E", "Z",
                "\u00a7a", "\u00a7l", "\u00a7r", ":", "q"};
        for (int sample = 0; sample < 1500; sample++) {
            final StringBuilder name = new StringBuilder();
            for (int i = 0, n = 2 + random.nextInt(22); i < n; i++) {
                name.append(glyphs[random.nextInt(glyphs.length)]);
            }
            final String original = name.toString();
            final int budget = 6 + random.nextInt(170);
            final String result = Minecraft189ArrayListLayout.fitLabel(
                    original, budget,
                    Minecraft189ArrayListLayoutTest::simulatedNativeFontWidth);
            assertTrue(simulatedNativeFontWidth(result) <= budget,
                    "over-wide native formatted label: " + result);
            assertTrue(!hasIsolatedSurrogate(result),
                    "truncated surrogate pair: " + result);
            assertTrue(!result.endsWith("\u00a7"),
                    "dangling Minecraft section code: " + result);
            if (result.endsWith("...")) {
                final String prefix = result.substring(0, result.length() - 3);
                assertTrue(prefix.length() < 2
                        || prefix.charAt(prefix.length() - 2) != '\u00a7',
                        "invisible dangling formatting pair: " + result);
            } else {
                assertEquals(original, result);
            }
            assertEquals(original, name.toString());
        }
    }

    private static boolean hasIsolatedSurrogate(final String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isHighSurrogate(value.charAt(i))) {
                if (i + 1 >= value.length()
                        || !Character.isLowSurrogate(value.charAt(i + 1))) {
                    return true;
                }
                i++;
            } else if (Character.isLowSurrogate(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static int simulatedNativeFontWidth(final String text) {
        int width = 0;
        boolean bold = false;
        for (int i = 0; i < text.length();) {
            final int cp = text.codePointAt(i);
            if (cp == '\u00a7' && i + 1 < text.length()) {
                final char format = text.charAt(i + 1);
                if (format == 'l') bold = true;
                if (format == 'r' || "0123456789abcdef".indexOf(format) >= 0) {
                    bold = false;
                }
                i += 2;
                continue;
            }
            width += (cp == '.' ? 2 : cp == ' ' ? 4 : 6)
                    + (bold ? 1 : 0);
            i += Character.charCount(cp);
        }
        return width;
    }

    @Test
    void longOriginalLabelGetsBoundedOnlyInPresentation() {
        final String label = "[Combat] A very long module name with full settings";
        final String fitted = Minecraft189ArrayListLayout.fitLabel(label, 96);
        assertTrue(fitted.endsWith("..."));
        assertTrue(Minecraft189HudTextMetrics.width(fitted) <= 96);
        assertTrue(fitted.length() < label.length());
        assertEquals(label, Minecraft189ArrayListLayout.fitLabel(label, 640));
        assertEquals("", Minecraft189ArrayListLayout.fitLabel(label, 2));
        assertFalse(Minecraft189ArrayListLayout.fitLabel(label, 8).isEmpty());
        assertThrows(IllegalArgumentException.class, () ->
                Minecraft189ArrayListLayout.visibleRows(1, -1, 5, true));
    }
}
