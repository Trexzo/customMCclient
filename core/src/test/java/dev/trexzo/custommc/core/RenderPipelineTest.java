package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.render.RenderFrame;
import dev.trexzo.custommc.core.render.RenderPass;
import dev.trexzo.custommc.core.render.RenderPipeline;
import dev.trexzo.custommc.core.render.RenderStage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class RenderPipelineTest {
    @Test
    void orderingIsDeterministic() {
        final RenderPipeline pipeline =
                new RenderPipeline();
        final List<String> calls =
                new ArrayList<String>();

        pipeline.register(pass(
                "z",
                RenderStage.HUD,
                10,
                calls));
        pipeline.register(pass(
                "a",
                RenderStage.HUD,
                10,
                calls));
        pipeline.register(pass(
                "first",
                RenderStage.HUD,
                0,
                calls));

        pipeline.render(
                RenderStage.HUD,
                new RenderFrame(1L, 0.5F));

        assertEquals(
                Arrays.asList("first", "a", "z"),
                calls);
    }

    @Test
    void stageIsolationIsExplicit() {
        final RenderPipeline pipeline =
                new RenderPipeline();
        final List<String> calls =
                new ArrayList<String>();

        pipeline.register(pass(
                "world",
                RenderStage.WORLD,
                0,
                calls));
        pipeline.register(pass(
                "hud",
                RenderStage.HUD,
                0,
                calls));

        pipeline.render(
                RenderStage.HUD,
                new RenderFrame(0L, 0.0F));

        assertEquals(
                Arrays.asList("hud"),
                calls);
    }

    @Test
    void duplicateIdsAreRejected() {
        final RenderPipeline pipeline =
                new RenderPipeline();
        final List<String> calls =
                new ArrayList<String>();

        pipeline.register(pass(
                "same",
                RenderStage.HUD,
                0,
                calls));

        assertThrows(
                IllegalArgumentException.class,
                () -> pipeline.register(pass(
                        "same",
                        RenderStage.WORLD,
                        1,
                        calls)));
    }

    @Test
    void planIsCachedUntilItsStageChanges() {
        final RenderPipeline pipeline =
                new RenderPipeline();
        final List<String> calls =
                new ArrayList<String>();

        pipeline.register(pass(
                "hud",
                RenderStage.HUD,
                0,
                calls));

        final List<RenderPass> hudPlan =
                pipeline.snapshotFor(RenderStage.HUD);

        assertSame(
                hudPlan,
                pipeline.snapshotFor(RenderStage.HUD));

        pipeline.register(pass(
                "world",
                RenderStage.WORLD,
                0,
                calls));

        assertSame(
                hudPlan,
                pipeline.snapshotFor(RenderStage.HUD));

        pipeline.register(pass(
                "hud-second",
                RenderStage.HUD,
                1,
                calls));

        assertNotSame(
                hudPlan,
                pipeline.snapshotFor(RenderStage.HUD));
    }

    @Test
    void registrationOwnsPassLifetime() {
        final RenderPipeline pipeline =
                new RenderPipeline();
        final List<String> calls =
                new ArrayList<String>();
        final RenderPipeline.Registration registration =
                pipeline.register(pass(
                        "hud",
                        RenderStage.HUD,
                        0,
                        calls));

        pipeline.render(
                RenderStage.HUD,
                new RenderFrame(0L, 0.0F));
        assertEquals(Arrays.asList("hud"), calls);

        registration.close();
        registration.close();
        assertFalse(registration.active());

        calls.clear();
        pipeline.render(
                RenderStage.HUD,
                new RenderFrame(1L, 0.0F));
        assertEquals(
                java.util.Collections.emptyList(),
                calls);
    }

    private static RenderPass pass(
            final String id,
            final RenderStage stage,
            final int priority,
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
                return priority;
            }

            @Override
            public void render(final RenderFrame frame) {
                calls.add(id);
            }
        };
    }
}
