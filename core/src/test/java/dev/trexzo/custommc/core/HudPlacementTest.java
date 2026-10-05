package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.setting.Setting;
import dev.trexzo.custommc.core.ui.HudComposer;
import dev.trexzo.custommc.core.ui.HudDrawContext;
import dev.trexzo.custommc.core.ui.HudLayoutState;
import dev.trexzo.custommc.core.ui.HudPlacement;
import dev.trexzo.custommc.core.ui.HudPlacementCodec;
import dev.trexzo.custommc.core.ui.HudWidget;
import dev.trexzo.custommc.core.ui.HudWidgetRegistry;
import dev.trexzo.custommc.core.ui.UiAnchor;
import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiRectCommand;
import dev.trexzo.custommc.core.ui.UiSize;
import dev.trexzo.custommc.core.ui.UiViewport;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class HudPlacementTest {
    @Test
    void placementCodecIsDeterministicAndSettingCompatible() {
        final HudPlacementCodec codec =
                new HudPlacementCodec();
        final Setting<HudPlacement> setting =
                new Setting<HudPlacement>(
                        "hud.status.placement",
                        new HudPlacement(
                                UiAnchor.TOP_RIGHT,
                                -12.5F,
                                8.0F),
                        value -> true,
                        codec);

        assertEquals(
                "TOP_RIGHT;-12.5;8.0",
                setting.encode());

        final HudPlacement decoded =
                setting.decode("BOTTOM_LEFT;4.0;-2.0");

        assertEquals(
                UiAnchor.BOTTOM_LEFT,
                decoded.anchor());
        assertEquals(4.0F, decoded.offsetX());
        assertEquals(-2.0F, decoded.offsetY());
    }

    @Test
    void runtimeOverrideMovesWidgetWithoutMutatingWidget() {
        final HudWidgetRegistry registry =
                new HudWidgetRegistry();
        registry.register(widget());

        final HudLayoutState state =
                new HudLayoutState();
        state.set(
                "status",
                new HudPlacement(
                        UiAnchor.BOTTOM_RIGHT,
                        -10.0F,
                        -5.0F));

        final List<UiDrawCommand> commands =
                new HudComposer().compose(
                        new UiViewport(800, 600, 1.0F),
                        registry,
                        state);

        final UiRectCommand rect =
                (UiRectCommand) commands.get(0);

        assertEquals(690.0F, rect.x());
        assertEquals(575.0F, rect.y());

        state.clear("status");
        assertNull(state.overrideFor("status"));

        final UiRectCommand restored =
                (UiRectCommand) new HudComposer().compose(
                        new UiViewport(800, 600, 1.0F),
                        registry,
                        state).get(0);

        assertEquals(0.0F, restored.x());
        assertEquals(0.0F, restored.y());
    }

    @Test
    void malformedPlacementEncodingIsRejected() {
        final HudPlacementCodec codec =
                new HudPlacementCodec();

        assertThrows(
                IllegalArgumentException.class,
                () -> codec.decode("TOP_LEFT;1.0"));
        assertThrows(
                IllegalArgumentException.class,
                () -> codec.decode("NOPE;1.0;2.0"));
        assertThrows(
                IllegalArgumentException.class,
                () -> codec.decode("TOP_LEFT;NaN;2.0"));
    }

    private static HudWidget widget() {
        return new HudWidget() {
            @Override
            public String id() {
                return "status";
            }

            @Override
            public int priority() {
                return 0;
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
                return new UiSize(100.0F, 20.0F);
            }

            @Override
            public void draw(
                    final HudDrawContext context) {
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
