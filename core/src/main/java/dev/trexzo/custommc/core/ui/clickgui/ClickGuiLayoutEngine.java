package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiViewport;

import java.util.Objects;

public final class ClickGuiLayoutEngine {
    public ClickGuiLayout layout(
            final UiViewport viewport) {
        Objects.requireNonNull(viewport, "viewport");

        final float viewportWidth =
                viewport.logicalWidth();
        final float viewportHeight =
                viewport.logicalHeight();

        final float width =
                Math.min(
                        920.0F,
                        viewportWidth * 0.84F);
        final float height =
                Math.min(
                        600.0F,
                        viewportHeight * 0.80F);
        final float x =
                (viewportWidth - width) * 0.5F;
        final float y =
                (viewportHeight - height) * 0.5F;

        final float sidebarWidth =
                Math.min(
                        190.0F,
                        width * 0.30F);
        final float padding =
                Math.min(
                        18.0F,
                        width * 0.025F);
        final float titleHeight =
                Math.min(
                        38.0F,
                        height * 0.10F);
        final float searchHeight =
                Math.min(
                        32.0F,
                        height * 0.09F);

        final UiBounds root =
                new UiBounds(
                        x,
                        y,
                        width,
                        height);
        final UiBounds sidebar =
                new UiBounds(
                        x,
                        y,
                        sidebarWidth,
                        height);
        final UiBounds search =
                new UiBounds(
                        x + padding,
                        y + padding + titleHeight,
                        Math.max(
                                0.0F,
                                sidebarWidth - padding * 2.0F),
                        searchHeight);

        final float navigationY =
                search.y()
                        + search.height()
                        + padding;
        final UiBounds navigation =
                new UiBounds(
                        x + padding,
                        navigationY,
                        Math.max(
                                0.0F,
                                sidebarWidth - padding * 2.0F),
                        Math.max(
                                0.0F,
                                y + height
                                        - padding
                                        - navigationY));

        final UiBounds content =
                new UiBounds(
                        x + sidebarWidth,
                        y,
                        Math.max(
                                0.0F,
                                width - sidebarWidth),
                        height);

        return new ClickGuiLayout(
                root,
                sidebar,
                search,
                navigation,
                content);
    }
}
