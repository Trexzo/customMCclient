package dev.trexzo.custommc.platform.v1_8_9;

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
