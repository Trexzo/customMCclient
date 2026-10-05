package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.ui.UiPointerAction;
import dev.trexzo.custommc.core.ui.UiPointerButton;
import dev.trexzo.custommc.core.ui.UiPointerEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class UiPointerEventTest {
    @Test
    void pointerEventRetainsLogicalInput() {
        final UiPointerEvent event =
                new UiPointerEvent(
                        12.5F,
                        20.0F,
                        UiPointerButton.LEFT,
                        UiPointerAction.PRESS);

        assertEquals(12.5F, event.x());
        assertEquals(20.0F, event.y());
        assertEquals(UiPointerButton.LEFT, event.button());
        assertEquals(UiPointerAction.PRESS, event.action());
    }

    @Test
    void pointerEventRejectsNonFiniteCoordinates() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new UiPointerEvent(
                        Float.NaN,
                        0.0F,
                        UiPointerButton.LEFT,
                        UiPointerAction.PRESS));
    }
}
