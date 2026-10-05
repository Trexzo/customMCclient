package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.ui.HudDrawContext;
import dev.trexzo.custommc.core.ui.HudDragController;
import dev.trexzo.custommc.core.ui.HudLayoutEngine;
import dev.trexzo.custommc.core.ui.HudLayoutSnapshot;
import dev.trexzo.custommc.core.ui.HudLayoutState;
import dev.trexzo.custommc.core.ui.HudPlacement;
import dev.trexzo.custommc.core.ui.HudWidget;
import dev.trexzo.custommc.core.ui.HudWidgetRegistry;
import dev.trexzo.custommc.core.ui.UiAnchor;
import dev.trexzo.custommc.core.ui.UiPointerButton;
import dev.trexzo.custommc.core.ui.UiSize;
import dev.trexzo.custommc.core.ui.UiViewport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class HudDragControllerTest {
    @Test
    void hitTestingSelectsTopmostWidget() {
        final HudLayoutState state = new HudLayoutState();
        final HudWidgetRegistry registry =
                new HudWidgetRegistry();
        registry.register(widget("low", 0));
        registry.register(widget("high", 10));

        final HudLayoutSnapshot layout =
                new HudLayoutEngine().layout(
                        new UiViewport(800, 600, 1.0F),
                        registry,
                        state);

        final HudDragController drag =
                new HudDragController(state);

        assertTrue(drag.begin(
                10.0F,
                10.0F,
                UiPointerButton.LEFT,
                layout));
        assertEquals("high", drag.widgetId());
    }

    @Test
    void dragUpdatesOnlyRuntimePlacementAndCommitKeepsIt() {
        final HudLayoutState state = new HudLayoutState();
        final HudWidgetRegistry registry =
                new HudWidgetRegistry();
        registry.register(widget("status", 0));

        final HudLayoutSnapshot layout =
                new HudLayoutEngine().layout(
                        new UiViewport(800, 600, 1.0F),
                        registry,
                        state);

        final HudDragController drag =
                new HudDragController(state);

        assertTrue(drag.begin(
                10.0F,
                10.0F,
                UiPointerButton.LEFT,
                layout));

        drag.move(35.0F, 42.0F);

        final HudPlacement moved =
                state.overrideFor("status");
        assertEquals(25.0F, moved.offsetX());
        assertEquals(32.0F, moved.offsetY());

        drag.commit();

        assertFalse(drag.active());
        assertEquals(25.0F,
                state.overrideFor("status").offsetX());
    }

    @Test
    void cancelRestoresPreviousOverrideOrDefault() {
        final HudLayoutState state = new HudLayoutState();
        final HudWidgetRegistry registry =
                new HudWidgetRegistry();
        registry.register(widget("status", 0));

        final HudDragController drag =
                new HudDragController(state);

        HudLayoutSnapshot layout =
                new HudLayoutEngine().layout(
                        new UiViewport(800, 600, 1.0F),
                        registry,
                        state);

        assertTrue(drag.begin(
                5.0F,
                5.0F,
                UiPointerButton.LEFT,
                layout));
        drag.move(20.0F, 20.0F);
        drag.cancel();

        assertNull(state.overrideFor("status"));

        state.set(
                "status",
                new HudPlacement(
                        UiAnchor.TOP_LEFT,
                        3.0F,
                        4.0F));

        layout = new HudLayoutEngine().layout(
                new UiViewport(800, 600, 1.0F),
                registry,
                state);

        assertTrue(drag.begin(
                5.0F,
                5.0F,
                UiPointerButton.LEFT,
                layout));
        drag.move(30.0F, 30.0F);
        drag.cancel();

        assertEquals(3.0F,
                state.overrideFor("status").offsetX());
        assertEquals(4.0F,
                state.overrideFor("status").offsetY());
    }

    @Test
    void nonLeftButtonAndEmptySpaceDoNotStartDrag() {
        final HudLayoutState state = new HudLayoutState();
        final HudWidgetRegistry registry =
                new HudWidgetRegistry();
        registry.register(widget("status", 0));

        final HudLayoutSnapshot layout =
                new HudLayoutEngine().layout(
                        new UiViewport(800, 600, 1.0F),
                        registry,
                        state);

        final HudDragController drag =
                new HudDragController(state);

        assertFalse(drag.begin(
                10.0F,
                10.0F,
                UiPointerButton.RIGHT,
                layout));
        assertFalse(drag.begin(
                400.0F,
                400.0F,
                UiPointerButton.LEFT,
                layout));
        assertThrows(
                IllegalStateException.class,
                () -> drag.move(1.0F, 1.0F));
    }

    private static HudWidget widget(
            final String id,
            final int priority) {
        return new HudWidget() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public int priority() {
                return priority;
            }

            @Override
            public UiAnchor anchor() {
                return UiAnchor.TOP_LEFT;
            }

            @Override
            public float offsetX() {
                return 0.0F;
            }

            @Override
            public float offsetY() {
                return 0.0F;
            }

            @Override
            public UiSize measure(
                    final UiViewport viewport) {
                return new UiSize(100.0F, 50.0F);
            }

            @Override
            public void draw(
                    final HudDrawContext context) {
            }
        };
    }
}
