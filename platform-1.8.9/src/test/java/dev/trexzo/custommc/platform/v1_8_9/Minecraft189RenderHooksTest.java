package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.event.EventBus;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.render.RenderFrame;
import dev.trexzo.custommc.core.render.RenderPass;
import dev.trexzo.custommc.core.render.RenderPipeline;
import dev.trexzo.custommc.core.render.RenderStage;
import dev.trexzo.custommc.core.service.ServiceRegistry;
import dev.trexzo.custommc.platform.PlatformContext;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class Minecraft189RenderHooksTest {
    @Test
    void hudHookRoutesOnlyHudStage() {
        final RenderPipeline pipeline =
                new RenderPipeline();
        final List<String> calls =
                new ArrayList<String>();

        pipeline.register(pass(
                "world",
                RenderStage.WORLD,
                calls));
        pipeline.register(pass(
                "hud",
                RenderStage.HUD,
                calls));

        final Minecraft189Hooks hooks = hooks(pipeline);
        hooks.renderHud(7L, 0.25F);

        assertEquals(Arrays.asList("hud"), calls);
    }

    @Test
    void renderHookRequiresRegisteredPipeline() {
        final Minecraft189Platform platform =
                new Minecraft189Platform();
        final ModuleRegistry modules =
                new ModuleRegistry();

        platform.attach(new PlatformContext(
                new EventBus(),
                modules,
                new ModuleController(modules),
                new ServiceRegistry()));

        assertThrows(
                IllegalStateException.class,
                () -> new Minecraft189Hooks(platform)
                        .renderHud(0L, 0.0F));
    }

    private static Minecraft189Hooks hooks(
            final RenderPipeline pipeline) {
        final ModuleRegistry modules =
                new ModuleRegistry();
        final ServiceRegistry services =
                new ServiceRegistry();
        services.register(RenderPipeline.class, pipeline);

        final Minecraft189Platform platform =
                new Minecraft189Platform();
        platform.attach(new PlatformContext(
                new EventBus(),
                modules,
                new ModuleController(modules),
                services));

        return new Minecraft189Hooks(platform);
    }

    private static RenderPass pass(
            final String id,
            final RenderStage stage,
            final List<String> calls) {
        return new RenderPass() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public RenderStage stage() {
                return stage;
            }

            @Override
            public int priority() {
                return 0;
            }

            @Override
            public void render(final RenderFrame frame) {
                calls.add(id);
            }
        };
    }
}
