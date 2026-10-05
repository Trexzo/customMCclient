package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.ui.UiColorRole;
import dev.trexzo.custommc.core.ui.UiTheme;
import dev.trexzo.custommc.core.ui.UiThemes;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class UiThemeTest {
    @Test
    void defaultThemeCoversEverySemanticRole() {
        final UiTheme theme =
                UiThemes.darkDefault();

        assertEquals("dark-default", theme.id());
        assertEquals(
                UiColorRole.values().length,
                theme.colors().size());

        for (UiColorRole role : UiColorRole.values()) {
            theme.color(role);
        }
    }

    @Test
    void themeDefensivelyCopiesPalette() {
        final EnumMap<UiColorRole, Integer> colors =
                new EnumMap<UiColorRole, Integer>(
                        UiColorRole.class);

        for (UiColorRole role : UiColorRole.values()) {
            colors.put(
                    role,
                    0xFF000000 | role.ordinal());
        }

        final UiTheme theme =
                new UiTheme(
                        "test-theme",
                        colors);
        final int originalAccent =
                theme.color(UiColorRole.ACCENT);

        colors.put(
                UiColorRole.ACCENT,
                0xFFFFFFFF);

        assertEquals(
                originalAccent,
                theme.color(UiColorRole.ACCENT));
        assertThrows(
                UnsupportedOperationException.class,
                () -> theme.colors().clear());
    }

    @Test
    void themeRejectsIncompletePalette() {
        final Map<UiColorRole, Integer> colors =
                new EnumMap<UiColorRole, Integer>(
                        UiColorRole.class);
        colors.put(
                UiColorRole.BACKGROUND,
                0xFF000000);

        assertThrows(
                IllegalArgumentException.class,
                () -> new UiTheme(
                        "incomplete",
                        colors));
    }
}
