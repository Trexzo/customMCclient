package dev.trexzo.custommc.core.ui.clickgui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ClickGuiModel {
    private static final Comparator<ClickGuiPage> PAGE_ORDER =
            new Comparator<ClickGuiPage>() {
                @Override
                public int compare(
                        final ClickGuiPage left,
                        final ClickGuiPage right) {
                    final int priority =
                            Integer.compare(
                                    left.priority(),
                                    right.priority());
                    if (priority != 0) {
                        return priority;
                    }
                    return left.id().compareTo(right.id());
                }
            };

    private final Map<String, ClickGuiPage> pages =
            new LinkedHashMap<String, ClickGuiPage>();

    private List<ClickGuiPage> orderedPages =
            Collections.emptyList();
    private String selectedPageId;
    private String searchQuery = "";
    private boolean open;

    public synchronized Registration register(
            final ClickGuiPage page) {
        Objects.requireNonNull(page, "page");

        if (pages.containsKey(page.id())) {
            throw new IllegalArgumentException(
                    "duplicate ClickGUI page id: "
                            + page.id());
        }

        pages.put(page.id(), page);
        rebuildPages();

        if (selectedPageId == null) {
            selectedPageId =
                    orderedPages.get(0).id();
        }

        return new RegistrationImpl(
                this,
                page.id(),
                page);
    }

    public synchronized void open() {
        open = true;
    }

    public synchronized void close() {
        open = false;
    }

    public synchronized void toggle() {
        open = !open;
    }

    public synchronized boolean select(
            final String pageId) {
        Objects.requireNonNull(pageId, "pageId");
        if (!pages.containsKey(pageId)) {
            return false;
        }
        selectedPageId = pageId;
        return true;
    }

    public synchronized void setSearchQuery(
            final String searchQuery) {
        this.searchQuery =
                Objects.requireNonNull(
                        searchQuery,
                        "searchQuery");
    }

    public synchronized ClickGuiSnapshot snapshot() {
        return new ClickGuiSnapshot(
                open,
                selectedPageId,
                searchQuery,
                orderedPages);
    }

    private synchronized void unregister(
            final String id,
            final ClickGuiPage expected) {
        final ClickGuiPage current =
                pages.get(id);
        if (current != expected) {
            return;
        }

        pages.remove(id);
        rebuildPages();

        if (id.equals(selectedPageId)) {
            selectedPageId =
                    orderedPages.isEmpty()
                            ? null
                            : orderedPages.get(0).id();
        }
    }

    private void rebuildPages() {
        final List<ClickGuiPage> next =
                new ArrayList<ClickGuiPage>(
                        pages.values());
        Collections.sort(next, PAGE_ORDER);
        orderedPages =
                Collections.unmodifiableList(next);
    }

    public interface Registration extends AutoCloseable {
        ClickGuiPage page();

        boolean active();

        @Override
        void close();
    }

    private static final class RegistrationImpl
            implements Registration {
        private final ClickGuiModel model;
        private final String id;
        private final ClickGuiPage page;
        private boolean active = true;

        RegistrationImpl(
                final ClickGuiModel model,
                final String id,
                final ClickGuiPage page) {
            this.model = model;
            this.id = id;
            this.page = page;
        }

        @Override
        public ClickGuiPage page() {
            return page;
        }

        @Override
        public synchronized boolean active() {
            return active;
        }

        @Override
        public void close() {
            synchronized (this) {
                if (!active) {
                    return;
                }
                active = false;
            }

            model.unregister(id, page);
        }
    }
}
