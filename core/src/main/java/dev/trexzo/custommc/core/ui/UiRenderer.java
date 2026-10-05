package dev.trexzo.custommc.core.ui;

import dev.trexzo.custommc.core.render.RenderFrame;

import java.util.List;

public interface UiRenderer {
    void render(
            RenderFrame frame,
            UiViewport viewport,
            List<UiDrawCommand> commands);
}
