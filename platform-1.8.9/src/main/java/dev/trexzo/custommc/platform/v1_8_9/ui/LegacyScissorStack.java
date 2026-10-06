package dev.trexzo.custommc.platform.v1_8_9.ui;

import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiViewport;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

public final class LegacyScissorStack {
    private final UiViewport viewport;
    private final LegacyUiFramebufferMapper mapper;
    private final Deque<LegacyFramebufferRect> stack =
            new ArrayDeque<LegacyFramebufferRect>();

    public LegacyScissorStack(
            final UiViewport viewport) {
        this(
                viewport,
                new LegacyUiFramebufferMapper());
    }

    LegacyScissorStack(
            final UiViewport viewport,
            final LegacyUiFramebufferMapper mapper) {
        this.viewport = Objects.requireNonNull(
                viewport,
                "viewport");
        this.mapper = Objects.requireNonNull(
                mapper,
                "mapper");
    }

    public LegacyFramebufferRect push(
            final UiBounds bounds) {
        final LegacyFramebufferRect mapped =
                mapper.scissor(
                        viewport,
                        Objects.requireNonNull(
                                bounds,
                                "bounds"));
        final LegacyFramebufferRect active =
                stack.isEmpty()
                        ? mapped
                        : stack.peek()
                        .intersect(mapped);
        stack.push(active);
        return active;
    }

    public LegacyFramebufferRect pop() {
        if (stack.isEmpty()) {
            throw new IllegalStateException(
                    "legacy scissor stack underflow");
        }
        stack.pop();
        return stack.peek();
    }

    public LegacyFramebufferRect current() {
        return stack.peek();
    }

    public int depth() {
        return stack.size();
    }

    public boolean empty() {
        return stack.isEmpty();
    }
}
