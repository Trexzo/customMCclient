package dev.trexzo.custommc.core.render;

public final class RenderFrame {
    private final long frameIndex;
    private final float partialTicks;

    public RenderFrame(
            final long frameIndex,
            final float partialTicks) {
        if (frameIndex < 0L) {
            throw new IllegalArgumentException(
                    "frameIndex must be non-negative");
        }
        if (Float.isNaN(partialTicks)
                || partialTicks < 0.0F
                || partialTicks > 1.0F) {
            throw new IllegalArgumentException(
                    "partialTicks must be within [0,1]");
        }
        this.frameIndex = frameIndex;
        this.partialTicks = partialTicks;
    }

    public long frameIndex() {
        return frameIndex;
    }

    public float partialTicks() {
        return partialTicks;
    }
}
