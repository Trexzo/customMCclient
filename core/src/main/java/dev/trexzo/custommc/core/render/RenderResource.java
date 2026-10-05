package dev.trexzo.custommc.core.render;

public interface RenderResource extends AutoCloseable {
    String id();

    @Override
    void close();
}
