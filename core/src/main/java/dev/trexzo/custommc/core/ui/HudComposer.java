package dev.trexzo.custommc.core.ui;

import java.util.List;
import java.util.Objects;

public final class HudComposer {
    public List<UiDrawCommand> compose(
            final UiViewport viewport,
            final HudWidgetRegistry registry) {
        Objects.requireNonNull(viewport, "viewport");
        Objects.requireNonNull(registry, "registry");

        final UiCommandBuffer commands =
                new UiCommandBuffer();

        for (HudWidget widget : registry.snapshot()) {
            final UiSize size = Objects.requireNonNull(
                    widget.measure(viewport),
                    "widget.measure");
            final UiBounds bounds =
                    widget.anchor().resolve(
                            viewport,
                            size,
                            widget.offsetX(),
                            widget.offsetY());

            widget.draw(new HudDrawContext(
                    viewport,
                    bounds,
                    commands));
        }

        return commands.seal();
    }
}
