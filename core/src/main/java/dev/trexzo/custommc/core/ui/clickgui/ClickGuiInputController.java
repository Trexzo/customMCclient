package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.ui.UiFocusManager;
import dev.trexzo.custommc.core.ui.UiFocusTarget;
import dev.trexzo.custommc.core.ui.UiKeyAction;
import dev.trexzo.custommc.core.ui.UiKeyEvent;
import dev.trexzo.custommc.core.ui.UiKeys;
import dev.trexzo.custommc.core.ui.UiPointerAction;
import dev.trexzo.custommc.core.ui.UiPointerButton;
import dev.trexzo.custommc.core.ui.UiPointerEvent;
import dev.trexzo.custommc.core.ui.UiScrollEvent;
import dev.trexzo.custommc.core.ui.UiViewport;

import java.util.Objects;

public final class ClickGuiInputController
        implements AutoCloseable {
    public static final String SEARCH_FOCUS_ID =
            "clickgui.search";

    private static final int MAX_SEARCH_LENGTH = 64;

    private final ClickGuiModel model;
    private final ClickGuiLayoutEngine layoutEngine;
    private final ClickGuiContentRegistry contentRegistry;
    private final UiFocusManager focusManager;
    private final UiFocusManager.Registration searchRegistration;

    public ClickGuiInputController(
            final ClickGuiModel model,
            final UiFocusManager focusManager) {
        this(
                model,
                new ClickGuiLayoutEngine(),
                new ClickGuiContentRegistry(),
                focusManager);
    }

    public ClickGuiInputController(
            final ClickGuiModel model,
            final ClickGuiContentRegistry contentRegistry,
            final UiFocusManager focusManager) {
        this(
                model,
                new ClickGuiLayoutEngine(),
                contentRegistry,
                focusManager);
    }

    ClickGuiInputController(
            final ClickGuiModel model,
            final ClickGuiLayoutEngine layoutEngine,
            final ClickGuiContentRegistry contentRegistry,
            final UiFocusManager focusManager) {
        this.model =
                Objects.requireNonNull(
                        model,
                        "model");
        this.layoutEngine =
                Objects.requireNonNull(
                        layoutEngine,
                        "layoutEngine");
        this.contentRegistry =
                Objects.requireNonNull(
                        contentRegistry,
                        "contentRegistry");
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

        if (event.action() != UiPointerAction.PRESS) {
            return false;
        }

        final ClickGuiLayout layout =
                layoutEngine.layout(viewport);

        if (layout.search().contains(
                event.x(),
                event.y())) {
            if (event.button() == UiPointerButton.LEFT) {
                focusManager.requestFocus(
                        SEARCH_FOCUS_ID);
            }
            return true;
        }

        if (layout.navigation().contains(
                event.x(),
                event.y())) {
            if (event.button() == UiPointerButton.LEFT) {
                focusManager.clearFocus();
                selectNavigationPage(
                        snapshot,
                        layout,
                        event.y());
            }
            return true;
        }

        if (layout.content().contains(
                event.x(),
                event.y())) {
            focusManager.clearFocus();
            dispatchContentPointer(
                    snapshot,
                    layout,
                    event);
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

    public boolean scroll(
            final UiScrollEvent event,
            final UiViewport viewport) {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(viewport, "viewport");

        final ClickGuiSnapshot snapshot =
                model.snapshot();
        if (!snapshot.open()) {
            return false;
        }

        final ClickGuiLayout layout =
                layoutEngine.layout(viewport);
        if (layout.navigation().contains(
                event.x(),
                event.y())) {
            final float current =
                    ClickGuiMetrics.clampNavigationScroll(
                            snapshot.navigationScroll(),
                            snapshot.pages().size(),
                            layout.navigation().height());
            final float next =
                    ClickGuiMetrics.clampNavigationScroll(
                            current
                                    - event.deltaY()
                                    * ClickGuiMetrics.NAVIGATION_SCROLL_STEP,
                            snapshot.pages().size(),
                            layout.navigation().height());

            model.setNavigationScroll(next);
            return true;
        }

        if (layout.content().contains(
                event.x(),
                event.y())) {
            return dispatchContentScroll(
                    snapshot,
                    layout,
                    event);
        }

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
        focusManager.clearFocus();
        searchRegistration.close();
    }

    private void dispatchContentPointer(
            final ClickGuiSnapshot snapshot,
            final ClickGuiLayout layout,
            final UiPointerEvent event) {
        final ClickGuiPage selected =
                selectedPage(snapshot);
        if (selected == null) {
            return;
        }

        final ClickGuiPageContent content =
                contentRegistry.find(
                        selected.id());
        if (content == null) {
            return;
        }

        content.pointer(
                new ClickGuiContentInputContext(
                        snapshot,
                        selected,
                        layout.content(),
                        focusManager),
                event);
    }

    private boolean dispatchContentScroll(
            final ClickGuiSnapshot snapshot,
            final ClickGuiLayout layout,
            final UiScrollEvent event) {
        final ClickGuiPage selected =
                selectedPage(snapshot);
        if (selected == null) {
            return false;
        }

        final ClickGuiPageContent content =
                contentRegistry.find(
                        selected.id());
        if (content == null) {
            return false;
        }

        return content.scroll(
                new ClickGuiContentInputContext(
                        snapshot,
                        selected,
                        layout.content(),
                        focusManager),
                event);
    }

    private void selectNavigationPage(
            final ClickGuiSnapshot snapshot,
            final ClickGuiLayout layout,
            final float pointerY) {
        final float scroll =
                ClickGuiMetrics.clampNavigationScroll(
                        snapshot.navigationScroll(),
                        snapshot.pages().size(),
                        layout.navigation().height());
        float rowY =
                layout.navigation().y() - scroll;

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

    private static ClickGuiPage selectedPage(
            final ClickGuiSnapshot snapshot) {
        final String selectedId =
                snapshot.selectedPageId();
        if (selectedId == null) {
            return null;
        }

        for (ClickGuiPage page : snapshot.pages()) {
            if (selectedId.equals(page.id())) {
                return page;
            }
        }
        return null;
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
