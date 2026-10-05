package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiClipCommand;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiRectCommand;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class UiClipCommandTest {
    @Test
    void clipScopeOwnsImmutableCommandSnapshot() {
        final List<UiDrawCommand> source =
                new ArrayList<UiDrawCommand>();
        source.add(
                new UiRectCommand(
                        0,
                        1.0F,
                        2.0F,
                        3.0F,
                        4.0F,
                        0xFFFFFFFF));

        final UiClipCommand clip =
                new UiClipCommand(
                        7,
                        new UiBounds(
                                10.0F,
                                20.0F,
                                100.0F,
                                80.0F),
                        source);

        source.clear();

        assertEquals(7, clip.layer());
        assertEquals(10.0F, clip.bounds().x());
        assertEquals(1, clip.commands().size());
        assertThrows(
                UnsupportedOperationException.class,
                () -> clip.commands().clear());
    }

    @Test
    void clipScopeRejectsNullChildren() {
        assertThrows(
                NullPointerException.class,
                () -> new UiClipCommand(
                        0,
                        new UiBounds(
                                0.0F,
                                0.0F,
                                10.0F,
                                10.0F),
                        Arrays.<UiDrawCommand>asList(
                                (UiDrawCommand) null)));
    }
}
