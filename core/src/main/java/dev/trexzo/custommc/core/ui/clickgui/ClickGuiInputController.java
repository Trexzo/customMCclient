package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.ui.UiFocusManager;
import dev.trexzo.custommc.core.ui.UiKey;
import dev.trexzo.custommc.core.ui.UiFocusTarget;
import dev.trexzo.custommc.core.ui.UiKeyAction;
import dev.trexzo.custommc.core.ui.UiKeyEvent;
import dev.trexzo.custommc.core.ui.UiKeys;
import dev.trexzo.custommc.core.ui.UiPointerAction;
import dev.trexzo.custommc.core.ui.UiPointerButton;
import dev.trexzo.custommc.core.ui.UiPointerEvent;
import dev.trexzo.custommc.core.ui.UiScrollEvent;
import dev.trexzo.custommc.core.ui.UiViewport;

import java.util.List;
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
    private final ClickGuiNavigator navigator;
    private final UiFocusManager.Registration searchRegistration;
    private String capturedPageId;
    private long capturedPointerEpoch;

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
        this.navigator =
                new ClickGuiNavigator() {
                    @Override
                    public boolean selectPage(
                            final String pageId) {
                        return ClickGuiInputController.this.selectPage(pageId);
                    }
                };
        this.searchRegistration =
                focusManager.register(
                        new SearchFocusTarget());
    }

    public boolean pointer(
            final UiPointerEvent event,
            final UiViewport viewport) {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(viewport, "viewport");

        reconcilePointerCapture();
        // A fresh press starts a new gesture, never continuing a stale one.
        if (event.action() == UiPointerAction.PRESS) {
            cancelCapturedPointer();
        }
        final ClickGuiSnapshot snapshot =
                model.snapshot();
        if (!snapshot.open()) {
            return false;
        }

        if (event.action() == UiPointerAction.RELEASE) {
            // End a held drag even if the pointer left the content region.
            // No focus or page navigation side effects.
            return dispatchContentPointer(snapshot,
                    layoutEngine.layout(viewport), event, viewport);
        }
        if (event.action() == UiPointerAction.MOVE) {
            // An owned scrollbar/numeric drag must keep receiving physical
            // MOVE when the cursor crosses the content edge, just as RELEASE
            // already does. Unowned motion outside the content remains vetoed.
            // Never route such motion to search, navigation or module toggles.
            final ClickGuiLayout layout = layoutEngine.layout(viewport);
            if (!layout.content().contains(event.x(), event.y())) {
                final ClickGuiPage selected = selectedPage(snapshot);
                if (selected == null) return false;
                final ClickGuiPageContent content =
                        contentRegistry.find(selected.id());
                if (content == null || !content.capturesPointerDrag()) {
                    return false;
                }
            }
            return dispatchContentPointer(
                    snapshot, layout, event, viewport);
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
                // The rightmost 28 logical pixels are an explicit clear action.
                if (event.x() >= layout.search().x()
                        + layout.search().width() - 28.0F
                        && !snapshot.searchQuery().isEmpty()) {
                    model.setSearchQuery("");
                }
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
                    event,
                    viewport);
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
        reconcilePointerCapture();

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
                            ClickGuiNavigationPages.visible(snapshot).size(),
                            layout.navigation().height());
            final float next =
                    ClickGuiMetrics.clampNavigationScroll(
                            current
                                    - event.deltaY()
                                    * ClickGuiMetrics.NAVIGATION_SCROLL_STEP,
                            ClickGuiNavigationPages.visible(snapshot).size(),
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
                    event,
                    viewport);
        }

        return false;
    }

    public boolean key(final UiKeyEvent event) {
        Objects.requireNonNull(event, "event");
        reconcilePointerCapture();

        if (!model.snapshot().open()) {
            return false;
        }

        if (focusManager.dispatchKey(event)) {
            // Search editor can alter the visible Settings/Modules rows.
            // A captured scrub must not keep writing a now-hidden row.
            reconcilePointerCapture();
            return true;
        }

        // Ctrl+F opens search without a mouse (vanilla often captures
        // its cursor). Never steal text/keybind/editor focus.
        if (event.action() == UiKeyAction.PRESS
                && event.control() && !event.alt()
                && focusManager.focusedId() == null
                && "legacy-key-33".equals(event.key().id())) {
            cancelCapturedPointer();
            return focusManager.requestFocus(SEARCH_FOCUS_ID);
        }

        // Focused search and setting editors always keep their keyboard
        // ownership. Unfocused page shortcuts precede global page nav.
        if (focusManager.focusedId() == null
                && event.action() == UiKeyAction.PRESS
                && dispatchPageKey(event)) {
            return true;
        }

        if (event.action() == UiKeyAction.PRESS
                && navigatePages(event)) {
            return true;
        }

        if (event.action() == UiKeyAction.PRESS
                && UiKeys.ESCAPE.equals(event.key())) {
            focusManager.clearFocus();
            // Detail view is contextual. Escape goes back once; the next
            // Escape closes ClickGUI. Focused editors own Escape above.
            if (ClickGuiRuntime.MODULE_DETAIL_PAGE_ID.equals(
                    model.snapshot().selectedPageId())) {
                return selectPage(ClickGuiRuntime.MODULES_PAGE_ID);
            }
            model.close();
            reconcilePointerCapture();
            return true;
        }

        return false;
    }

    @Override
    public void close() {
        cancelCapturedPointer();
        focusManager.clearFocus();
        searchRegistration.close();
    }

    private boolean dispatchPageKey(final UiKeyEvent event) {
        final ClickGuiSnapshot snapshot = model.snapshot();
        final ClickGuiPage selected = selectedPage(snapshot);
        if (selected == null) {
            return false;
        }
        final ClickGuiPageContent content = contentRegistry.find(
                selected.id());
        return content != null && content.key(event);
    }

    /**
     * Navigation is owned by the GUI only when no search/text/keybind
     * editor currently owns focus. Never steals modifier shortcuts.
     */
    private boolean navigatePages(final UiKeyEvent event) {
        if (focusManager.focusedId() != null
                || event.control() || event.alt()) {
            return false;
        }
        final UiKey key = event.key();
        if (!UiKeys.UP.equals(key) && !UiKeys.DOWN.equals(key)
                && !UiKeys.HOME.equals(key) && !UiKeys.END.equals(key)) {
            return false;
        }
        final ClickGuiSnapshot snapshot = model.snapshot();
        final List<ClickGuiPage> pages =
                ClickGuiNavigationPages.visible(snapshot);
        if (pages.isEmpty()) {
            return false;
        }

        int current = -1;
        for (int i = 0; i < pages.size(); i++) {
            if (pages.get(i).id().equals(
                    ClickGuiNavigationPages.highlightedId(snapshot))) {
                current = i;
                break;
            }
        }

        final int next;
        if (UiKeys.HOME.equals(key)) {
            next = 0;
        } else if (UiKeys.END.equals(key)) {
            next = pages.size() - 1;
        } else if (UiKeys.DOWN.equals(key)) {
            next = (current + 1) % pages.size();
        } else {
            next = current < 0
                    ? pages.size() - 1
                    : (current + pages.size() - 1) % pages.size();
        }
        return selectPage(pages.get(next).id());
    }

    private boolean dispatchContentPointer(
            final ClickGuiSnapshot snapshot,
            final ClickGuiLayout layout,
            final UiPointerEvent event,
            final UiViewport viewport) {
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

        final boolean handled = content.pointer(
                new ClickGuiContentInputContext(
                        snapshot,
                        selected,
                        layout.content(),
                        focusManager,
                        navigator,
                        viewport),
                event);
        if (content.capturesPointerDrag()) {
            capturedPageId = selected.id();
            capturedPointerEpoch = model.pointerContextEpoch();
            reconcilePointerCapture();
        } else if (selected.id().equals(capturedPageId)) {
            capturedPageId = null;
        }
        return handled;
    }

    private boolean dispatchContentScroll(
            final ClickGuiSnapshot snapshot,
            final ClickGuiLayout layout,
            final UiScrollEvent event,
            final UiViewport viewport) {
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
                        focusManager,
                        navigator,
                        viewport),
                event);
    }

    private void selectNavigationPage(
            final ClickGuiSnapshot snapshot,
            final ClickGuiLayout layout,
            final float pointerY) {
        final float scroll =
                ClickGuiMetrics.clampNavigationScroll(
                        snapshot.navigationScroll(),
                        ClickGuiNavigationPages.visible(snapshot).size(),
                        layout.navigation().height());
        float rowY =
                layout.navigation().y() - scroll;

        for (ClickGuiPage page : ClickGuiNavigationPages.visible(snapshot)) {
            if (pointerY >= rowY
                    && pointerY
                    < rowY + ClickGuiMetrics.PAGE_HEIGHT) {
                selectPage(page.id());
                return;
            }

            rowY += ClickGuiMetrics.PAGE_HEIGHT
                    + ClickGuiMetrics.PAGE_GAP;
        }
    }

    private boolean selectPage(final String pageId) {
        final boolean selected = model.select(pageId);
        reconcilePointerCapture();
        return selected;
    }

    private void reconcilePointerCapture() {
        if (capturedPageId == null) return;
        final ClickGuiSnapshot snapshot = model.snapshot();
        if (!snapshot.open()
                || !capturedPageId.equals(snapshot.selectedPageId())
                || capturedPointerEpoch != model.pointerContextEpoch()) {
            cancelCapturedPointer();
        }
    }

    private void cancelCapturedPointer() {
        if (capturedPageId == null) return;
        final String formerPage = capturedPageId;
        capturedPageId = null;
        final ClickGuiPageContent content = contentRegistry.find(formerPage);
        if (content != null) content.cancelPointerDrag();
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

    /**
     * Search has a single end-of-string caret. Ctrl+Backspace deletes
     * the last complete token, including any spaces after it. Plain
     * Backspace removes a whole Unicode code point, never leaving half
     * of a valid UTF-16 surrogate pair in the search field.
     */
    private static String withoutTrailingWord(final String query) {
        int cut = query.length();
        while (cut > 0 && Character.isWhitespace(query.charAt(cut - 1))) {
            cut--;
        }
        while (cut > 0 && !Character.isWhitespace(query.charAt(cut - 1))) {
            cut--;
        }
        return query.substring(0, cut);
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
                    final String next = event.control() && !event.alt()
                            ? withoutTrailingWord(query)
                            : query.substring(0,
                                    query.offsetByCodePoints(
                                            query.length(), -1));
                    model.setSearchQuery(next);
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
