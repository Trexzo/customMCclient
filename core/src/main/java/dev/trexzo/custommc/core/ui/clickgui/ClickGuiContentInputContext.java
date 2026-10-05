package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiFocusManager;

import java.util.Objects;

public final class ClickGuiContentInputContext {
    private final ClickGuiSnapshot snapshot;
    private final ClickGuiPage page;
    private final UiBounds bounds;
    private final UiFocusManager focusManager;
    private final ClickGuiNavigator navigator;

    public ClickGuiContentInputContext(
            final ClickGuiSnapshot snapshot,
            final ClickGuiPage page,
            final UiBounds bounds) {
        this(
                snapshot,
                page,
                bounds,
                null,
                null);
    }

    public ClickGuiContentInputContext(
            final ClickGuiSnapshot snapshot,
            final ClickGuiPage page,
            final UiBounds bounds,
            final UiFocusManager focusManager) {
        this(
                snapshot,
                page,
                bounds,
                focusManager,
                null);
    }

    public ClickGuiContentInputContext(
            final ClickGuiSnapshot snapshot,
            final ClickGuiPage page,
            final UiBounds bounds,
            final UiFocusManager focusManager,
            final ClickGuiNavigator navigator) {
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
        this.focusManager = focusManager;
        this.navigator = navigator;
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

    public UiFocusManager focusManager() {
        return focusManager;
    }

    public ClickGuiNavigator navigator() {
        return navigator;
    }
}
