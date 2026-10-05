package dev.trexzo.custommc.core.ui;

import dev.trexzo.custommc.core.render.RenderFrame;

public interface UiViewportProvider {
    UiViewport viewport(RenderFrame frame);
}
