package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.ui.UiClipCommand;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiTextCommand;
import dev.trexzo.custommc.core.ui.UiThemes;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiComposer;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiLayout;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiLayoutEngine;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiPage;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiSnapshot;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ClickGuiComposerTest {
    @Test
    void layoutIsCenteredAndBoundedByViewport() {
        final UiViewport viewport =
                new UiViewport(
                        1200,
                        800,
                        1.0F);
        final ClickGuiLayout layout =
                new ClickGuiLayoutEngine()
                        .layout(viewport);

        assertTrue(layout.root().width() <= 920.0F);
        assertTrue(layout.root().height() <= 600.0F);
        assertEquals(
                (viewport.logicalWidth()
                        - layout.root().width()) * 0.5F,
                layout.root().x());
        assertEquals(
                (viewport.logicalHeight()
                        - layout.root().height()) * 0.5F,
                layout.root().y());
        assertTrue(layout.content().width() > 0.0F);
    }

    @Test
    void closedSnapshotProducesNoCommands() {
        final List<UiDrawCommand> commands =
                new ClickGuiComposer().compose(
                        new ClickGuiSnapshot(
                                false,
                                null,
                                "",
                                Collections.<ClickGuiPage>emptyList()),
                        new UiViewport(
                                1200,
                                800,
                                1.0F),
                        UiThemes.darkDefault());

        assertTrue(commands.isEmpty());
    }

    @Test
    void openSnapshotComposesSearchNavigationAndContent() {
        final List<UiDrawCommand> commands =
                new ClickGuiComposer().compose(
                        new ClickGuiSnapshot(
                                true,
                                "render",
                                "esp",
                                Arrays.asList(
                                        new ClickGuiPage(
                                                "combat",
                                                "Combat",
                                                0),
                                        new ClickGuiPage(
                                                "render",
                                                "Render",
                                                1))),
                        new UiViewport(
                                1200,
                                800,
                                1.0F),
                        UiThemes.darkDefault());

        boolean sawClip = false;
        boolean sawSearch = false;
        boolean sawSelectedTitle = false;

        for (UiDrawCommand command : commands) {
            if (command instanceof UiClipCommand) {
                sawClip = true;
            }
            if (command instanceof UiTextCommand) {
                final String text =
                        ((UiTextCommand) command).text();
                sawSearch |= "esp".equals(text);
                sawSelectedTitle |= "Render".equals(text);
            }
        }

        assertFalse(commands.isEmpty());
        assertTrue(sawClip);
        assertTrue(sawSearch);
        assertTrue(sawSelectedTitle);
    }
}
