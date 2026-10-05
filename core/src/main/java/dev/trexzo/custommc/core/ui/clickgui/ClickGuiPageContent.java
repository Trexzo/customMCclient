package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiPointerEvent;

import java.util.List;

public interface ClickGuiPageContent {
    List<UiDrawCommand> compose(
            ClickGuiContentContext context);

    default boolean pointer(
            final ClickGuiContentInputContext context,
            final UiPointerEvent event) {
        return false;
    }
}
