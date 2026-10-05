package dev.trexzo.custommc.platform.v1_8_9.ui;

import dev.trexzo.custommc.core.render.RenderFrame;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.core.ui.UiViewportProvider;

import java.util.Objects;

public final class Minecraft189ViewportProvider
        implements UiViewportProvider {
    private final LegacyViewportAccess access;

    public Minecraft189ViewportProvider(
            final LegacyViewportAccess access) {
        this.access = Objects.requireNonNull(
                access,
                "access");
    }

    @Override
    public UiViewport viewport(
            final RenderFrame frame) {
        Objects.requireNonNull(frame, "frame");
        return new UiViewport(
                access.framebufferWidth(),
                access.framebufferHeight(),
                access.uiScale());
    }
}
