package dev.trexzo.custommc.platform.event;

public final class RenderFrameEvent {
    private final long sequence;
    private final float partialTicks;

    public RenderFrameEvent(
            final long sequence,
            final float partialTicks) {
        if (sequence < 0L) {
            throw new IllegalArgumentException(
                    "sequence must not be negative");
        }
        if (Float.isNaN(partialTicks)
                || Float.isInfinite(partialTicks)) {
            throw new IllegalArgumentException(
                    "partialTicks must be finite");
        }

        this.sequence = sequence;
        this.partialTicks = partialTicks;
    }

    public long sequence() {
        return sequence;
    }

    public float partialTicks() {
        return partialTicks;
    }
}
