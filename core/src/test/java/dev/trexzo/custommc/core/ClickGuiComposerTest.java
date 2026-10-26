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
                for (UiDrawCommand child :
                        ((UiClipCommand) command).commands()) {
                    if (child instanceof UiTextCommand) {
                        sawSearch |= "esp".equals(
                                ((UiTextCommand) child).text());
                    }
                }
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
    @Test
    void searchTextIsClippedAndClearAffordanceIsConditional() {
        final UiViewport viewport = new UiViewport(1200, 800, 1.0F);
        final ClickGuiLayout layout = new ClickGuiLayoutEngine()
                .layout(viewport);
        final ClickGuiComposer composer = new ClickGuiComposer();
        final ClickGuiPage page = new ClickGuiPage("modules", "Modules", 0);
        final String longQuery = String.join("",
                Collections.nCopies(64, "z"));
        final List<UiDrawCommand> filtered = composer.compose(
                new ClickGuiSnapshot(true, "modules", longQuery,
                        Arrays.asList(page)),
                viewport, UiThemes.darkDefault());

        boolean sawClippedQuery = false;
        boolean sawClear = false;
        for (UiDrawCommand command : filtered) {
            if (command instanceof UiClipCommand) {
                final UiClipCommand clip = (UiClipCommand) command;
                for (UiDrawCommand child : clip.commands()) {
                    if (child instanceof UiTextCommand
                            && longQuery.equals(((UiTextCommand) child).text())) {
                        sawClippedQuery = true;
                        assertTrue(clip.bounds().x() >= layout.search().x());
                        assertTrue(clip.bounds().x() + clip.bounds().width()
                                < layout.search().x() + layout.search().width());
                    }
                }
            }
            if (command instanceof UiTextCommand
                    && "x".equals(((UiTextCommand) command).text())) {
                sawClear = true;
            }
        }
        assertTrue(sawClippedQuery);
        assertTrue(sawClear);

        final List<UiDrawCommand> empty = composer.compose(
                new ClickGuiSnapshot(true, "modules", "",
                        Arrays.asList(page)),
                viewport, UiThemes.darkDefault());
        for (UiDrawCommand command : empty) {
            if (command instanceof UiTextCommand) {
                assertFalse("x".equals(((UiTextCommand) command).text()));
            }
        }
    }

}
