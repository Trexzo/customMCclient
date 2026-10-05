package dev.trexzo.custommc.core.ui;

public interface HudWidget {
    String id();

    int priority();

    UiAnchor anchor();

    float offsetX();

    float offsetY();

    UiSize measure(UiViewport viewport);

    void draw(HudDrawContext context);
}
