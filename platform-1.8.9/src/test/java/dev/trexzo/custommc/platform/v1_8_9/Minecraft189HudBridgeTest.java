package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.event.EventBus;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.render.RenderFrame;
import dev.trexzo.custommc.core.render.RenderPipeline;
import dev.trexzo.custommc.core.service.ServiceRegistry;
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
import dev.trexzo.custommc.platform.PlatformContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class Minecraft189HudBridgeTest {
    @Test
    void minecraftHudHookReachesBackendNeutralUiRenderer() {
        final HudWidgetRegistry widgets =
                new HudWidgetRegistry();
        widgets.register(widget());

        final AtomicInteger renderCalls =
                new AtomicInteger();
        final AtomicLong frameSeen =
                new AtomicLong(-1L);

        final UiRenderer renderer =
                new UiRenderer() {
                    @Override
                    public void render(
                            final RenderFrame frame,
                            final UiViewport viewport,
                            final List<UiDrawCommand> commands) {
                        renderCalls.incrementAndGet();
                        frameSeen.set(frame.frameIndex());
                        assertEquals(1, commands.size());
                        assertEquals(
                                400.0F,
                                viewport.logicalWidth());
                    }
                };

        final RenderPipeline pipeline =
                new RenderPipeline();
        pipeline.register(new HudRenderPass(
                "hud-ui",
                100,
                widgets,
                new HudLayoutState(),
                new UiViewportProvider() {
                    @Override
                    public UiViewport viewport(
                            final RenderFrame frame) {
                        return new UiViewport(
                                800,
                                600,
                                2.0F);
                    }
                },
                renderer));

        final ServiceRegistry services =
                new ServiceRegistry();
        services.register(RenderPipeline.class, pipeline);

        final ModuleRegistry modules =
                new ModuleRegistry();
        final Minecraft189Platform platform =
                new Minecraft189Platform();
        platform.attach(new PlatformContext(
                new EventBus(),
                modules,
                new ModuleController(modules),
                services));

        new Minecraft189Hooks(platform)
                .renderHud(42L, 0.5F);

        assertEquals(1, renderCalls.get());
        assertEquals(42L, frameSeen.get());
    }

    private static HudWidget widget() {
        return new HudWidget() {
            @Override
            public String id() {
                return "bridge-status";
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
                return new UiSize(20.0F, 20.0F);
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
                                0xFFFFFFFF));
            }
        };
    }
}
