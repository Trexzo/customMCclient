package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.ui.UiClipCommand;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiFocusManager;
import dev.trexzo.custommc.core.ui.UiScrollEvent;
import dev.trexzo.custommc.core.ui.UiTextCommand;
import dev.trexzo.custommc.core.ui.UiThemes;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiComposer;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiInputController;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiLayout;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiLayoutEngine;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiMetrics;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiModel;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiPage;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiSnapshot;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ClickGuiNavigationScrollTest {
    @Test
    void wheelScrollIsClampedAndSharedByHitTesting() {
        final ClickGuiModel model =
                populatedModel(20);
        final UiViewport viewport =
                new UiViewport(
                        1200,
                        800,
                        1.0F);
        final ClickGuiLayout layout =
                new ClickGuiLayoutEngine()
                        .layout(viewport);

        try (ClickGuiInputController input =
                     new ClickGuiInputController(
                             model,
                             new UiFocusManager())) {
            final float x =
                    layout.navigation().x() + 4.0F;
            final float y =
                    layout.navigation().y() + 10.0F;

            assertTrue(
                    input.scroll(
                            new UiScrollEvent(
                                    x,
                                    y,
                                    -1.0F),
                            viewport));
            assertEquals(
                    ClickGuiMetrics.NAVIGATION_SCROLL_STEP,
                    model.snapshot().navigationScroll());

            assertTrue(
                    input.pointer(
                            new dev.trexzo.custommc.core.ui.UiPointerEvent(
                                    x,
                                    y,
                                    dev.trexzo.custommc.core.ui.UiPointerButton.LEFT,
                                    dev.trexzo.custommc.core.ui.UiPointerAction.PRESS),
                            viewport));
            assertEquals(
                    "page-01",
                    model.snapshot().selectedPageId());

            for (int i = 0; i < 100; i++) {
                input.scroll(
                        new UiScrollEvent(
                                x,
                                y,
                                -1.0F),
                        viewport);
            }

            final float maximum =
                    ClickGuiMetrics.maxNavigationScroll(
                            model.snapshot().pages().size(),
                            layout.navigation().height());
            assertEquals(
                    maximum,
                    model.snapshot().navigationScroll());

            assertTrue(
                    input.scroll(
                            new UiScrollEvent(
                                    x,
                                    y,
                                    1.0F),
                            viewport));
            assertTrue(
                    model.snapshot().navigationScroll()
                            < maximum);
        }
    }

    @Test
    void outsideNavigationDoesNotConsumeScroll() {
        final ClickGuiModel model =
                populatedModel(20);
        final UiViewport viewport =
                new UiViewport(
                        1200,
                        800,
                        1.0F);
        final ClickGuiLayout layout =
                new ClickGuiLayoutEngine()
                        .layout(viewport);

        try (ClickGuiInputController input =
                     new ClickGuiInputController(
                             model,
                             new UiFocusManager())) {
            assertFalse(
                    input.scroll(
                            new UiScrollEvent(
                                    layout.content().x() + 4.0F,
                                    layout.content().y() + 4.0F,
                                    -1.0F),
                            viewport));
            assertEquals(
                    0.0F,
                    model.snapshot().navigationScroll());
        }
    }

    @Test
    void composerUsesSameClampedNavigationOffset() {
        final ClickGuiModel model =
                populatedModel(20);
        model.setNavigationScroll(
                ClickGuiMetrics.NAVIGATION_SCROLL_STEP);

        final UiViewport viewport =
                new UiViewport(
                        1200,
                        800,
                        1.0F);
        final ClickGuiLayout layout =
                new ClickGuiLayoutEngine()
                        .layout(viewport);
        final List<UiDrawCommand> commands =
                new ClickGuiComposer().compose(
                        model.snapshot(),
                        viewport,
                        UiThemes.darkDefault());

        UiClipCommand navigationClip = null;
        for (UiDrawCommand command : commands) {
            if (command instanceof UiClipCommand) {
                navigationClip =
                        (UiClipCommand) command;
                break;
            }
        }

        assertTrue(navigationClip != null);

        UiTextCommand firstPage = null;
        for (UiDrawCommand command :
                navigationClip.commands()) {
            if (command instanceof UiTextCommand
                    && "Page 0".equals(
                    ((UiTextCommand) command).text())) {
                firstPage =
                        (UiTextCommand) command;
                break;
            }
        }

        assertTrue(firstPage != null);
        assertEquals(
                layout.navigation().y()
                        - ClickGuiMetrics.NAVIGATION_SCROLL_STEP
                        + 9.0F,
                firstPage.y());
    }

    @Test
    void retainedScrollRejectsInvalidValues() {
        final ClickGuiModel model =
                new ClickGuiModel();

        assertThrows(
                IllegalArgumentException.class,
                () -> model.setNavigationScroll(-1.0F));
        assertThrows(
                IllegalArgumentException.class,
                () -> model.setNavigationScroll(Float.NaN));

        final ClickGuiSnapshot snapshot =
                new ClickGuiSnapshot(
                        true,
                        null,
                        "",
                        java.util.Collections.<ClickGuiPage>emptyList(),
                        0.0F);
        assertEquals(0.0F, snapshot.navigationScroll());
    }

    private static ClickGuiModel populatedModel(
            final int count) {
        final ClickGuiModel model =
                new ClickGuiModel();
        for (int i = 0; i < count; i++) {
            model.register(
                    new ClickGuiPage(
                            String.format(
                                    "page-%02d",
                                    i),
                            "Page " + i,
                            i));
        }
        model.open();
        return model;
    }
}
