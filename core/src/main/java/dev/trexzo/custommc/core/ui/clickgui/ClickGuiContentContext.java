package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiTheme;

import java.util.Objects;

public final class ClickGuiContentContext {
    private final ClickGuiSnapshot snapshot;
    private final ClickGuiPage page;
    private final UiBounds bounds;
    private final UiTheme theme;

    public ClickGuiContentContext(
            final ClickGuiSnapshot snapshot,
            final ClickGuiPage page,
            final UiBounds bounds,
            final UiTheme theme) {
        this.snapshot =
                Objects.requireNonNull(
                        snapshot,
                        "snapshot");
        this.page =
                Objects.requireNonNull(
                        page,
                        "page");
        this.bounds =
                Objects.requireNonNull(
                        bounds,
                        "bounds");
        this.theme =
                Objects.requireNonNull(
                        theme,
                        "theme");
    }

    public ClickGuiSnapshot snapshot() {
        return snapshot;
    }

    public ClickGuiPage page() {
        return page;
    }

    public UiBounds bounds() {
        return bounds;
    }

    public UiTheme theme() {
        return theme;
    }
}
