package dev.trexzo.custommc.core.ui;

import java.util.Objects;

public final class HudDrawContext {
    private final UiViewport viewport;
    private final UiBounds bounds;
    private final UiCommandBuffer commands;

    public HudDrawContext(
            final UiViewport viewport,
            final UiBounds bounds,
            final UiCommandBuffer commands) {
        this.viewport = Objects.requireNonNull(
                viewport,
                "viewport");
        this.bounds = Objects.requireNonNull(
                bounds,
                "bounds");
        this.commands = Objects.requireNonNull(
                commands,
                "commands");
    }

    public UiViewport viewport() {
        return viewport;
    }

    public UiBounds bounds() {
        return bounds;
    }

    public UiCommandBuffer commands() {
        return commands;
    }
}
