package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.ui.UiFontHandle;
import dev.trexzo.custommc.core.ui.UiFonts;
import dev.trexzo.custommc.core.ui.UiTextCommand;
import dev.trexzo.custommc.core.ui.UiTextMetrics;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class UiFontMetricsTest {
    @Test
    void fontHandleUsesStableLogicalIdentity() {
        final UiFontHandle first =
                new UiFontHandle("ui-medium");
        final UiFontHandle same =
                new UiFontHandle("ui-medium");
        final UiFontHandle other =
                new UiFontHandle("ui-bold");

        assertEquals(first, same);
        assertEquals(first.hashCode(), same.hashCode());
        assertNotEquals(first, other);
        assertEquals("ui-medium", first.id());
    }

    @Test
    void legacyTextConstructorUsesDefaultFont() {
        final UiTextCommand command =
                new UiTextCommand(
                        0,
                        1.0F,
                        2.0F,
                        "hello",
                        0xFFFFFFFF);

        assertEquals(
                UiFonts.DEFAULT,
                command.font());
    }

    @Test
    void textCommandRetainsExplicitFontHandle() {
        final UiFontHandle font =
                new UiFontHandle("ui-medium");
        final UiTextCommand command =
                new UiTextCommand(
                        0,
                        1.0F,
                        2.0F,
                        font,
                        "hello",
                        0xFFFFFFFF);

        assertEquals(font, command.font());
    }

    @Test
    void metricsRejectImpossibleGeometry() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new UiTextMetrics(
                        Float.NaN,
                        10.0F,
                        8.0F));
        assertThrows(
                IllegalArgumentException.class,
                () -> new UiTextMetrics(
                        20.0F,
                        -1.0F,
                        0.0F));
        assertThrows(
                IllegalArgumentException.class,
                () -> new UiTextMetrics(
                        20.0F,
                        10.0F,
                        11.0F));

        final UiTextMetrics metrics =
                new UiTextMetrics(
                        42.0F,
                        12.0F,
                        9.0F);

        assertEquals(42.0F, metrics.width());
        assertEquals(12.0F, metrics.height());
        assertEquals(9.0F, metrics.baseline());
    }
}
