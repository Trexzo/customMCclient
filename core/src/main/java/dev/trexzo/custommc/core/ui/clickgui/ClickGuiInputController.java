package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.ui.UiFocusManager;
import dev.trexzo.custommc.core.ui.UiFocusTarget;
import dev.trexzo.custommc.core.ui.UiKeyAction;
import dev.trexzo.custommc.core.ui.UiKeyEvent;
import dev.trexzo.custommc.core.ui.UiKeys;
import dev.trexzo.custommc.core.ui.UiPointerAction;
import dev.trexzo.custommc.core.ui.UiPointerButton;
import dev.trexzo.custommc.core.ui.UiPointerEvent;
import dev.trexzo.custommc.core.ui.UiViewport;

import java.util.Objects;

public final class ClickGuiInputController
        implements AutoCloseable {
    public static final String SEARCH_FOCUS_ID =
            "clickgui.search";

    private static final int MAX_SEARCH_LENGTH = 64;

    private final ClickGuiModel model;
    private final ClickGuiLayoutEngine layoutEngine;
    private final UiFocusManager focusManager;
    private final UiFocusManager.Registration searchRegistration;

    public ClickGuiInputController(
            final ClickGuiModel model,
            final UiFocusManager focusManager) {
        this(
                model,
                new ClickGuiLayoutEngine(),
                focusManager);
    }

    ClickGuiInputController(
            final ClickGuiModel model,
            final ClickGuiLayoutEngine layoutEngine,
            final UiFocusManager focusManager) {
        this.model =
                Objects.requireNonNull(
                        model,
                        "model");
        this.layoutEngine =
                Objects.requireNonNull(
                        layoutEngine,
                        "layoutEngine");
        this.focusManager =
                Objects.requireNonNull(
                        focusManager,
                        "focusManager");
        this.searchRegistration =
                focusManager.register(
                        new SearchFocusTarget());
    }

    public boolean pointer(
            final UiPointerEvent event,
            final UiViewport viewport) {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(viewport, "viewport");

        final ClickGuiSnapshot snapshot =
                model.snapshot();
        if (!snapshot.open()) {
            return false;
        }

        if (event.action() != UiPointerAction.PRESS
                || event.button() != UiPointerButton.LEFT) {
            return false;
        }

        final ClickGuiLayout layout =
                layoutEngine.layout(viewport);

        if (layout.search().contains(
                event.x(),
                event.y())) {
            focusManager.requestFocus(
                    SEARCH_FOCUS_ID);
            return true;
        }

        if (layout.navigation().contains(
                event.x(),
                event.y())) {
            focusManager.clearFocus();
            selectNavigationPage(
                    snapshot,
                    layout,
                    event.y());
            return true;
        }

        if (layout.root().contains(
                event.x(),
                event.y())) {
            focusManager.clearFocus();
            return true;
        }

        focusManager.clearFocus();
        return false;
    }

    public boolean key(final UiKeyEvent event) {
        Objects.requireNonNull(event, "event");

        if (!model.snapshot().open()) {
            return false;
        }

        if (focusManager.dispatchKey(event)) {
            return true;
        }

        if (event.action() == UiKeyAction.PRESS
                && UiKeys.ESCAPE.equals(event.key())) {
            focusManager.clearFocus();
            model.close();
            return true;
        }

        return false;
    }

    @Override
    public void close() {
        searchRegistration.close();
    }

    private void selectNavigationPage(
            final ClickGuiSnapshot snapshot,
            final ClickGuiLayout layout,
            final float pointerY) {
        float rowY = layout.navigation().y();

        for (ClickGuiPage page : snapshot.pages()) {
            if (pointerY >= rowY
                    && pointerY
                    < rowY + ClickGuiMetrics.PAGE_HEIGHT) {
                model.select(page.id());
                return;
            }

            rowY += ClickGuiMetrics.PAGE_HEIGHT
                    + ClickGuiMetrics.PAGE_GAP;
        }
    }

    private final class SearchFocusTarget
            implements UiFocusTarget {
        @Override
        public String id() {
            return SEARCH_FOCUS_ID;
        }

        @Override
        public void onFocusChanged(
                final boolean focused) {
            // Focus state is owned by UiFocusManager.
        }

        @Override
        public boolean onKey(
                final UiKeyEvent event) {
            if (event.action() == UiKeyAction.RELEASE) {
                return false;
            }

            if (UiKeys.ESCAPE.equals(event.key())
                    || UiKeys.ENTER.equals(event.key())) {
                focusManager.clearFocus();
                return true;
            }

            if (UiKeys.BACKSPACE.equals(event.key())) {
                final String query =
                        model.snapshot().searchQuery();
                if (!query.isEmpty()) {
                    model.setSearchQuery(
                            query.substring(
                                    0,
                                    query.length() - 1));
                }
                return true;
            }

            if (event.hasCharacter()
                    && !event.control()
                    && !event.alt()) {
                final char character =
                        event.character();
                if (!Character.isISOControl(character)) {
                    final String query =
                            model.snapshot().searchQuery();
                    if (query.length()
                            < MAX_SEARCH_LENGTH) {
                        model.setSearchQuery(
                                query + character);
                    }
                    return true;
                }
            }

            return false;
        }
    }
}
