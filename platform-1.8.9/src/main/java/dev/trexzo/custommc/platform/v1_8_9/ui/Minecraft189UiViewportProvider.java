package dev.trexzo.custommc.platform.v1_8_9.ui;

import dev.trexzo.custommc.core.render.RenderFrame;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.core.ui.UiViewportProvider;

import java.util.Objects;

public final class Minecraft189UiViewportProvider
        implements UiViewportProvider {
    private final LegacyUiViewportSource source;

    public Minecraft189UiViewportProvider(
            final LegacyUiViewportSource source) {
        this.source = Objects.requireNonNull(
                source,
                "source");
    }

    @Override
    public UiViewport viewport(
            final RenderFrame frame) {
        Objects.requireNonNull(frame, "frame");
        return new UiViewport(
                source.framebufferWidth(),
                source.framebufferHeight(),
                source.uiScale());
    }
}
