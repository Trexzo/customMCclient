package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.render.RenderFrame;
import dev.trexzo.custommc.core.render.RenderPipeline;
import dev.trexzo.custommc.core.render.RenderStage;
import dev.trexzo.custommc.core.ui.HudDrawContext;
import dev.trexzo.custommc.core.ui.HudLayoutState;
import dev.trexzo.custommc.core.ui.HudRenderPass;
import dev.trexzo.custommc.core.ui.HudWidget;
import dev.trexzo.custommc.core.ui.HudWidgetRegistry;
import dev.trexzo.custommc.core.ui.UiAnchor;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiRectCommand;
import dev.trexzo.custommc.core.ui.UiRenderer;
import dev.trexzo.custommc.core.ui.UiSize;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.core.ui.UiViewportProvider;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class HudRenderPassTest {
    @Test
    void hudStageComposesAndForwardsCommandsOnce() {
        final HudWidgetRegistry widgets =
                new HudWidgetRegistry();
        widgets.register(widget());

        final AtomicInteger calls = new AtomicInteger();
        final AtomicInteger commandCount = new AtomicInteger();
        final AtomicLong frameIndex = new AtomicLong(-1L);

        final UiRenderer renderer =
                new UiRenderer() {
                    @Override
                    public void render(
                            final RenderFrame frame,
                            final UiViewport viewport,
                            final List<UiDrawCommand> commands) {
                        calls.incrementAndGet();
                        commandCount.set(commands.size());
                        frameIndex.set(frame.frameIndex());
                        assertEquals(800.0F, viewport.logicalWidth());
                    }
                };

        final UiViewportProvider viewport =
                new UiViewportProvider() {
                    @Override
                    public UiViewport viewport(
                            final RenderFrame frame) {
                        return new UiViewport(
                                800,
                                600,
                                1.0F);
                    }
                };

        final RenderPipeline pipeline =
                new RenderPipeline();
        pipeline.register(new HudRenderPass(
                "hud-ui",
                100,
                widgets,
                new HudLayoutState(),
                viewport,
                renderer));

        pipeline.render(
                RenderStage.WORLD,
                new RenderFrame(6L, 0.25F));
        assertEquals(0, calls.get());

        pipeline.render(
                RenderStage.HUD,
                new RenderFrame(7L, 0.5F));

        assertEquals(1, calls.get());
        assertEquals(1, commandCount.get());
        assertEquals(7L, frameIndex.get());
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
                return 4.0F;
            }

            @Override
            public float offsetY() {
                return 5.0F;
            }

            @Override
            public UiSize measure(
                    final UiViewport viewport) {
                return new UiSize(80.0F, 16.0F);
            }

            @Override
            public void draw(
                    final HudDrawContext context) {
                context.commands().add(
                        new UiRectCommand(
                                0,
                                context.bounds().x(),
                                context.bounds().y(),
                                context.bounds().width(),
                                context.bounds().height(),
                                0xCC000000));
            }
        };
    }
}
