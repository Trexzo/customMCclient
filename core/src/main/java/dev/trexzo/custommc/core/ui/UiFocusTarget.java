package dev.trexzo.custommc.core.ui;

public interface UiFocusTarget {
    String id();

    void onFocusChanged(boolean focused);

    boolean onKey(UiKeyEvent event);
}
