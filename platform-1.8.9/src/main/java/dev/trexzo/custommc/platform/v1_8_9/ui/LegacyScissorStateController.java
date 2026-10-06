package dev.trexzo.custommc.platform.v1_8_9.ui;

import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiViewport;

import java.util.Objects;

public final class LegacyScissorStateController {
    private final LegacyScissorStack stack;
    private final LegacyScissorStateSink sink;
    private LegacyFramebufferRect applied;

    public LegacyScissorStateController(
            final UiViewport viewport,
            final LegacyScissorStateSink sink) {
        this.stack =
                new LegacyScissorStack(
                        Objects.requireNonNull(
                                viewport,
                                "viewport"));
        this.sink = Objects.requireNonNull(
                sink,
                "sink");
    }

    public LegacyFramebufferRect push(
            final UiBounds bounds) {
        final LegacyFramebufferRect active =
                stack.push(
                        Objects.requireNonNull(
                                bounds,
                                "bounds"));
        applyIfChanged(active);
        return active;
    }

    public LegacyFramebufferRect pop() {
        final LegacyFramebufferRect restored =
                stack.pop();
        if (restored == null) {
            disableIfNeeded();
        } else {
            applyIfChanged(restored);
        }
        return restored;
    }

    public void reset() {
        while (!stack.empty()) {
            stack.pop();
        }
        disableIfNeeded();
    }

    public LegacyFramebufferRect current() {
        return stack.current();
    }

    public LegacyFramebufferRect applied() {
        return applied;
    }

    public int depth() {
        return stack.depth();
    }

    public boolean enabled() {
        return applied != null;
    }

    private void applyIfChanged(
            final LegacyFramebufferRect next) {
        if (next.equals(applied)) {
            return;
        }

        sink.apply(next);
        applied = next;
    }

    private void disableIfNeeded() {
        if (applied == null) {
            return;
        }

        sink.disable();
        applied = null;
    }
}
