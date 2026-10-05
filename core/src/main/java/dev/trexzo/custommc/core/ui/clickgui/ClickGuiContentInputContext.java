package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.ui.UiBounds;

import java.util.Objects;

public final class ClickGuiContentInputContext {
    private final ClickGuiSnapshot snapshot;
    private final ClickGuiPage page;
    private final UiBounds bounds;

    public ClickGuiContentInputContext(
            final ClickGuiSnapshot snapshot,
            final ClickGuiPage page,
            final UiBounds bounds) {
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
}
