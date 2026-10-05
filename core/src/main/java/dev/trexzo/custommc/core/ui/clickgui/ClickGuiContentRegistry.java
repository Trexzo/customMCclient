package dev.trexzo.custommc.core.ui.clickgui;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class ClickGuiContentRegistry {
    private final Map<String, ClickGuiPageContent> contents =
            new LinkedHashMap<String, ClickGuiPageContent>();

    public synchronized Registration register(
            final String pageId,
            final ClickGuiPageContent content) {
        final String id =
                requireId(pageId);
        Objects.requireNonNull(content, "content");

        if (contents.containsKey(id)) {
            throw new IllegalArgumentException(
                    "duplicate ClickGUI content page id: " + id);
        }

        contents.put(id, content);
        return new RegistrationImpl(
                this,
                id,
                content);
    }

    public synchronized ClickGuiPageContent find(
            final String pageId) {
        return contents.get(
                Objects.requireNonNull(
                        pageId,
                        "pageId"));
    }

    private synchronized void unregister(
            final String pageId,
            final ClickGuiPageContent expected) {
        final ClickGuiPageContent current =
                contents.get(pageId);
        if (current == expected) {
            contents.remove(pageId);
        }
    }

    private static String requireId(
            final String pageId) {
        Objects.requireNonNull(pageId, "pageId");
        final String value =
                pageId.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(
                    "pageId must not be blank");
        }
        return value;
    }

    public interface Registration extends AutoCloseable {
        String pageId();

        ClickGuiPageContent content();

        boolean active();

        @Override
        void close();
    }

    private static final class RegistrationImpl
            implements Registration {
        private final ClickGuiContentRegistry registry;
        private final String pageId;
        private final ClickGuiPageContent content;
        private boolean active = true;

        RegistrationImpl(
                final ClickGuiContentRegistry registry,
                final String pageId,
                final ClickGuiPageContent content) {
            this.registry = registry;
            this.pageId = pageId;
            this.content = content;
        }

        @Override
        public String pageId() {
            return pageId;
        }

        @Override
        public ClickGuiPageContent content() {
            return content;
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
            registry.unregister(
                    pageId,
                    content);
        }
    }
}
