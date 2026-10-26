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
    @Test
    void boundedDragClampsAllEdgesAndCommitRetainsPlacement() {
        final HudLayoutState state = new HudLayoutState();
        final HudWidgetRegistry registry = new HudWidgetRegistry();
        registry.register(widget("status", 0));
        final UiViewport viewport = new UiViewport(800, 600, 1.0F);
        final HudLayoutSnapshot layout = new HudLayoutEngine()
                .layout(viewport, registry, state);
        final HudDragController drag = new HudDragController(state);
        assertTrue(drag.begin(5.0F, 5.0F,
                UiPointerButton.LEFT, layout, viewport));
        drag.move(100000.0F, 100000.0F);
        assertEquals(700.0F, state.overrideFor("status").offsetX());
        assertEquals(550.0F, state.overrideFor("status").offsetY());
        drag.move(-9000.0F, -9000.0F);
        assertEquals(0.0F, state.overrideFor("status").offsetX());
        assertEquals(0.0F, state.overrideFor("status").offsetY());
        drag.commit();
        assertFalse(drag.active());
        assertEquals(0.0F, state.overrideFor("status").offsetX());
    }

    @Test
    void snappedDragUsesLogicalCoordinatesAndKeepsBounds() {
        final HudLayoutState state = new HudLayoutState();
        final HudWidgetRegistry registry = new HudWidgetRegistry();
        registry.register(widget("status", 0));
        final UiViewport viewport = new UiViewport(800, 600, 2.0F);
        final HudDragController drag = new HudDragController(state);
        final HudLayoutSnapshot layout = new HudLayoutEngine()
                .layout(viewport, registry, state);
        assertTrue(drag.begin(5.0F, 5.0F,
                UiPointerButton.LEFT, layout, viewport));
        drag.moveSnapped(32.0F, 29.0F, 10.0F);
        assertEquals(30.0F, state.overrideFor("status").offsetX());
        assertEquals(20.0F, state.overrideFor("status").offsetY());
        drag.moveSnapped(900.0F, 900.0F, 10.0F);
        assertEquals(300.0F, state.overrideFor("status").offsetX());
        assertEquals(250.0F, state.overrideFor("status").offsetY());
        drag.cancel();
        assertNull(state.overrideFor("status"));
    }

    @Test
    void boundedDragPreservesNonTopLeftAnchorAndRevertsOverride() {
        final HudLayoutState state = new HudLayoutState();
        final HudWidgetRegistry registry = new HudWidgetRegistry();
        registry.register(anchoredWidget("status", UiAnchor.BOTTOM_RIGHT,
                -20.0F, -30.0F));
        final UiViewport viewport = new UiViewport(800, 600, 1.0F);
        final HudDragController drag = new HudDragController(state);
        final HudLayoutSnapshot layout = new HudLayoutEngine()
                .layout(viewport, registry, state);
        assertTrue(drag.begin(682.0F, 524.0F,
                UiPointerButton.LEFT, layout, viewport));
        drag.move(5000.0F, 5000.0F);
        assertEquals(0.0F, state.overrideFor("status").offsetX());
        assertEquals(0.0F, state.overrideFor("status").offsetY());
        assertEquals(UiAnchor.BOTTOM_RIGHT,
                state.overrideFor("status").anchor());
        drag.cancel();
        assertNull(state.overrideFor("status"));
    }

    @Test
    void snappedModeFailsClosedWithoutViewportOrWithInvalidGrid() {
        final HudLayoutState state = new HudLayoutState();
        final HudWidgetRegistry registry = new HudWidgetRegistry();
        registry.register(widget("status", 0));
        final HudLayoutSnapshot layout = new HudLayoutEngine()
                .layout(new UiViewport(800, 600, 1.0F),
                        registry, state);
        final HudDragController drag = new HudDragController(state);
        assertTrue(drag.begin(5.0F, 5.0F,
                UiPointerButton.LEFT, layout));
        assertThrows(IllegalStateException.class,
                () -> drag.moveSnapped(10.0F, 10.0F, 10.0F));
        assertThrows(IllegalArgumentException.class,
                () -> drag.moveSnapped(10.0F, 10.0F, Float.NaN));
        assertThrows(IllegalArgumentException.class,
                () -> drag.moveSnapped(10.0F, 10.0F, 0.0F));
        assertNull(state.overrideFor("status"));
        drag.cancel();
    }

    @Test
    void oversizedWidgetsCanStillBeDraggedToRecoverableOrigin() {
        final HudLayoutState state = new HudLayoutState();
        final HudWidgetRegistry registry = new HudWidgetRegistry();
        registry.register(widget("status", 0));
        final UiViewport small = new UiViewport(80, 40, 1.0F);
        final HudLayoutSnapshot layout = new HudLayoutEngine()
                .layout(small, registry, state);
        final HudDragController drag = new HudDragController(state);
        assertTrue(drag.begin(5.0F, 5.0F,
                UiPointerButton.LEFT, layout, small));
        drag.move(30.0F, 30.0F);
        assertEquals(0.0F, state.overrideFor("status").offsetX());
        assertEquals(0.0F, state.overrideFor("status").offsetY());
        drag.cancel();
    }

    private static HudWidget anchoredWidget(
            final String id, final UiAnchor anchor,
            final float x, final float y) {
        return new HudWidget() {
            @Override public String id() { return id; }
            @Override public int priority() { return 0; }
            @Override public UiAnchor anchor() { return anchor; }
            @Override public float offsetX() { return x; }
            @Override public float offsetY() { return y; }
            @Override public UiSize measure(UiViewport viewport) {
                return new UiSize(100.0F, 50.0F);
            }
            @Override public void draw(HudDrawContext context) { }
        };
    }
}
