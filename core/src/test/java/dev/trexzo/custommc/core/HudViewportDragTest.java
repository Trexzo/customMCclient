package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.ui.HudDragController;
import dev.trexzo.custommc.core.ui.HudDrawContext;
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

final class HudViewportDragTest {
    private static final UiViewport VIEWPORT =
            new UiViewport(800, 600, 1.0F);

    @Test
    void viewportDragClampsEdgesAndCancelRestoresDefault() {
        final HudLayoutState state = new HudLayoutState();
        final HudLayoutSnapshot snapshot =
                layout(state, widget("hud", UiAnchor.TOP_LEFT, 100, 50));
        final HudDragController drag = new HudDragController(state);
        assertTrue(drag.beginWithinViewport(
                10, 10, UiPointerButton.LEFT, snapshot, VIEWPORT));

        drag.move(5000, 5000);
        assertEquals(700.0F, state.overrideFor("hud").offsetX());
        assertEquals(550.0F, state.overrideFor("hud").offsetY());

        drag.move(-5000, -5000);
        assertEquals(0.0F, state.overrideFor("hud").offsetX());
        assertEquals(0.0F, state.overrideFor("hud").offsetY());

        drag.cancel();
        assertFalse(drag.active());
        assertNull(state.overrideFor("hud"));
    }

    @Test
    void centeredAnchorClampsRelativeToResolvedPosition() {
        final HudLayoutState state = new HudLayoutState();
        final HudLayoutSnapshot snapshot =
                layout(state, widget("center", UiAnchor.CENTER, 100, 50));
        final HudDragController drag = new HudDragController(state);
        assertTrue(drag.beginWithinViewport(
                360, 285, UiPointerButton.LEFT, snapshot, VIEWPORT));

        drag.move(10000, 10000);
        assertEquals(350.0F, state.overrideFor("center").offsetX());
        assertEquals(275.0F, state.overrideFor("center").offsetY());
        drag.commit();

        assertFalse(drag.active());
        assertEquals(350.0F, state.overrideFor("center").offsetX());
    }

    @Test
    void oversizedWidgetCanStillBePositionedAtVisibleLeadingEdge() {
        final HudLayoutState state = new HudLayoutState();
        final HudLayoutSnapshot snapshot =
                layout(state, widget("wide", UiAnchor.TOP_LEFT, 900, 50));
        final HudDragController drag = new HudDragController(state);
        assertTrue(drag.beginWithinViewport(
                5, 5, UiPointerButton.LEFT, snapshot, VIEWPORT));
        drag.move(9000, 9000);
        assertEquals(0.0F, state.overrideFor("wide").offsetX());
        assertEquals(550.0F, state.overrideFor("wide").offsetY());
    }

    @Test
    void rejectedDragsAndInvalidViewportDoNotCreateSession() {
        final HudLayoutState state = new HudLayoutState();
        final HudLayoutSnapshot snapshot =
                layout(state, widget("hud", UiAnchor.TOP_LEFT, 100, 50));
        final HudDragController drag = new HudDragController(state);
        assertFalse(drag.beginWithinViewport(
                5, 5, UiPointerButton.RIGHT, snapshot, VIEWPORT));
        assertFalse(drag.active());
        assertThrows(IllegalArgumentException.class,
                () -> drag.beginWithinViewport(
                        5, 5, UiPointerButton.LEFT, snapshot,
                        new UiViewport(800, 600, Float.MIN_VALUE)));
        assertFalse(drag.active());
    }

    private static HudLayoutSnapshot layout(
            final HudLayoutState state,
            final HudWidget widget) {
        final HudWidgetRegistry registry = new HudWidgetRegistry();
        registry.register(widget);
        return new HudLayoutEngine().layout(VIEWPORT, registry, state);
    }

    private static HudWidget widget(
            final String id,
            final UiAnchor anchor,
            final float width,
            final float height) {
        return new HudWidget() {
            @Override public String id() { return id; }
            @Override public int priority() { return 0; }
            @Override public UiAnchor anchor() { return anchor; }
            @Override public float offsetX() { return 0.0F; }
            @Override public float offsetY() { return 0.0F; }
            @Override public UiSize measure(final UiViewport viewport) {
                return new UiSize(width, height);
            }
            @Override public void draw(final HudDrawContext context) { }
        };
    }
}
