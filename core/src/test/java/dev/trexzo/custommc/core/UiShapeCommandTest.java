package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.ui.UiOutlineCommand;
import dev.trexzo.custommc.core.ui.UiRoundedRectCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class UiShapeCommandTest {
    @Test
    void roundedRectangleRetainsValidatedGeometry() {
        final UiRoundedRectCommand command =
                new UiRoundedRectCommand(
                        2,
                        4.0F,
                        5.0F,
                        100.0F,
                        40.0F,
                        8.0F,
                        0xFFFFFFFF);

        assertEquals(2, command.layer());
        assertEquals(8.0F, command.radius());
        assertEquals(100.0F, command.width());
    }

    @Test
    void roundedRectangleRejectsImpossibleRadius() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new UiRoundedRectCommand(
                        0,
                        0.0F,
                        0.0F,
                        20.0F,
                        10.0F,
                        6.0F,
                        0));
    }

    @Test
    void outlineRequiresPositiveFiniteThickness() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new UiOutlineCommand(
                        0,
                        0.0F,
                        0.0F,
                        20.0F,
                        10.0F,
                        0.0F,
                        0));
        assertThrows(
                IllegalArgumentException.class,
                () -> new UiOutlineCommand(
                        0,
                        0.0F,
                        0.0F,
                        20.0F,
                        10.0F,
                        Float.NaN,
                        0));

        final UiOutlineCommand command =
                new UiOutlineCommand(
                        1,
                        1.0F,
                        2.0F,
                        30.0F,
                        40.0F,
                        1.5F,
                        0xFF000000);

        assertEquals(1.5F, command.thickness());
    }
}
