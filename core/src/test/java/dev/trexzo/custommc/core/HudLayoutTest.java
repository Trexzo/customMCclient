package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.ui.HudComposer;
import dev.trexzo.custommc.core.ui.HudDrawContext;
import dev.trexzo.custommc.core.ui.HudWidget;
import dev.trexzo.custommc.core.ui.HudWidgetRegistry;
import dev.trexzo.custommc.core.ui.UiAnchor;
import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiRectCommand;
import dev.trexzo.custommc.core.ui.UiSize;
import dev.trexzo.custommc.core.ui.UiViewport;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class HudLayoutTest {
    @Test
    void anchorsUseLogicalViewportCoordinates() {
        final UiViewport viewport =
                new UiViewport(1920, 1080, 2.0F);
        final UiSize size =
                new UiSize(100.0F, 40.0F);

        final UiBounds topRight =
                UiAnchor.TOP_RIGHT.resolve(
                        viewport,
                        size,
                        -10.0F,
                        5.0F);
        final UiBounds center =
                UiAnchor.CENTER.resolve(
                        viewport,
                        size,
                        0.0F,
                        0.0F);

        assertEquals(850.0F, topRight.x());
        assertEquals(5.0F, topRight.y());
        assertEquals(430.0F, center.x());
        assertEquals(250.0F, center.y());
    }

    @Test
    void widgetPlanIsDeterministicAndCached() {
        final HudWidgetRegistry registry =
                new HudWidgetRegistry();
        final List<String> calls =
                new ArrayList<String>();

        registry.register(widget(
                "z",
                10,
                UiAnchor.TOP_LEFT,
                calls));
        registry.register(widget(
                "a",
                10,
                UiAnchor.TOP_LEFT,
                calls));
        registry.register(widget(
                "first",
                0,
                UiAnchor.TOP_LEFT,
                calls));

        final List<HudWidget> plan = registry.snapshot();

        assertEquals("first", plan.get(0).id());
        assertEquals("a", plan.get(1).id());
        assertEquals("z", plan.get(2).id());
        assertSame(plan, registry.snapshot());
    }

    @Test
    void registrationOwnsWidgetLifetime() {
        final HudWidgetRegistry registry =
                new HudWidgetRegistry();
        final List<String> calls =
                new ArrayList<String>();

        final HudWidgetRegistry.Registration registration =
                registry.register(widget(
                        "hud",
                        0,
                        UiAnchor.TOP_LEFT,
                        calls));

        registration.close();
        registration.close();

        assertFalse(registration.active());
        assertEquals(0, registry.snapshot().size());
    }

    @Test
    void composerResolvesBoundsAndEmitsCommands() {
        final HudWidgetRegistry registry =
                new HudWidgetRegistry();
        final List<String> calls =
                new ArrayList<String>();

        registry.register(widget(
                "status",
                0,
                UiAnchor.BOTTOM_RIGHT,
                calls));

        final List<UiDrawCommand> commands =
                new HudComposer().compose(
                        new UiViewport(800, 600, 1.0F),
                        registry);

        assertEquals(Arrays.asList("status"), calls);
        assertEquals(1, commands.size());

        final UiRectCommand rect =
                (UiRectCommand) commands.get(0);
        assertEquals(700.0F, rect.x());
        assertEquals(580.0F, rect.y());
        assertEquals(100.0F, rect.width());
        assertEquals(20.0F, rect.height());
    }

    @Test
    void viewportRejectsInvalidGeometry() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new UiViewport(0, 600, 1.0F));
        assertThrows(
                IllegalArgumentException.class,
                () -> new UiViewport(
                        800,
                        600,
                        Float.NaN));
    }

    private static HudWidget widget(
            final String id,
            final int priority,
            final UiAnchor anchor,
            final List<String> calls) {
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
                return anchor;
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
                return new UiSize(100.0F, 20.0F);
            }

            @Override
            public void draw(
                    final HudDrawContext context) {
                calls.add(id);
                final UiBounds bounds = context.bounds();
                context.commands().add(
                        new UiRectCommand(
                                0,
                                bounds.x(),
                                bounds.y(),
                                bounds.width(),
                                bounds.height(),
                                0xFFFFFFFF));
            }
        };
    }
}
