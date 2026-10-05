package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.ui.UiKeyAction;
import dev.trexzo.custommc.core.ui.UiKeys;
import dev.trexzo.custommc.core.ui.UiPointerAction;
import dev.trexzo.custommc.core.ui.UiPointerButton;
import dev.trexzo.custommc.core.ui.UiPointerEvent;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.platform.v1_8_9.input.LegacyInputTranslator;
import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LegacyInputTranslatorTest {
    private final LegacyInputTranslator translator =
            new LegacyInputTranslator();

    @Test
    void pointerCoordinatesBecomeTopLeftLogicalCoordinates() {
        final Optional<UiPointerEvent> translated =
                translator.pointerButtonEvent(
                        new UiViewport(
                                800,
                                600,
                                2.0F),
                        200,
                        99,
                        0,
                        true);

        assertTrue(translated.isPresent());
        assertEquals(100.0F, translated.get().x());
        assertEquals(250.0F, translated.get().y());
        assertEquals(
                UiPointerButton.LEFT,
                translated.get().button());
        assertEquals(
                UiPointerAction.PRESS,
                translated.get().action());
    }

    @Test
    void unsupportedMouseButtonsAreIgnored() {
        assertFalse(
                translator.pointerButtonEvent(
                        new UiViewport(
                                800,
                                600,
                                1.0F),
                        0,
                        0,
                        4,
                        true)
                        .isPresent());
    }

    @Test
    void semanticAndUnknownKeysRemainStable() {
        assertEquals(
                UiKeys.ESCAPE,
                translator.keyEvent(
                        LegacyKeyboardCodes.ESCAPE,
                        '\0',
                        true,
                        false,
                        false,
                        false,
                        false)
                        .key());

        assertEquals(
                "legacy-key-30",
                translator.keyEvent(
                        30,
                        'a',
                        true,
                        true,
                        true,
                        false,
                        false)
                        .key()
                        .id());

        assertEquals(
                UiKeyAction.REPEAT,
                translator.keyEvent(
                        30,
                        'a',
                        true,
                        true,
                        true,
                        false,
                        false)
                        .action());

        assertThrows(
                IllegalArgumentException.class,
                () -> translator.keyEvent(
                        -1,
                        '\0',
                        true,
                        false,
                        false,
                        false,
                        false));
    }
}
