package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.render.RenderFrame;
import dev.trexzo.custommc.core.render.RenderPipeline;
import dev.trexzo.custommc.core.render.RenderStage;

import java.util.Objects;

public final class Minecraft189Hooks {
    private final Minecraft189Platform platform;

    public Minecraft189Hooks(final Minecraft189Platform platform) {
        this.platform = Objects.requireNonNull(platform, "platform");
    }

    public void publishTick(final long tickIndex) {
        platform.requireContext()
                .events()
                .publish(new TickEvent(tickIndex));
    }

    public void renderWorld(
            final long frameIndex,
            final float partialTicks) {
        render(RenderStage.WORLD, frameIndex, partialTicks);
    }

    public void renderWorldOverlay(
            final long frameIndex,
            final float partialTicks) {
        render(RenderStage.WORLD_OVERLAY, frameIndex, partialTicks);
    }

    public void renderHud(
            final long frameIndex,
            final float partialTicks) {
        render(RenderStage.HUD, frameIndex, partialTicks);
    }

    public void renderPostProcess(
            final long frameIndex,
            final float partialTicks) {
        render(RenderStage.POST_PROCESS, frameIndex, partialTicks);
    }

    private void render(
            final RenderStage stage,
            final long frameIndex,
            final float partialTicks) {
        final RenderPipeline pipeline =
                platform.requireContext()
                        .services()
                        .require(RenderPipeline.class);

        pipeline.render(
                stage,
                new RenderFrame(frameIndex, partialTicks));
    }

    public static final class TickEvent {
        private final long tickIndex;

        public TickEvent(final long tickIndex) {
            if (tickIndex < 0L) {
                throw new IllegalArgumentException(
                        "tickIndex must be non-negative");
            }
            this.tickIndex = tickIndex;
        }

        public long tickIndex() {
            return tickIndex;
        }
    }
}
