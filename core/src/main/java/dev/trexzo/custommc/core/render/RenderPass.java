package dev.trexzo.custommc.core.render;

public interface RenderPass {
    String id();

    RenderStage stage();

    int priority();

    void render(RenderFrame frame);
}
