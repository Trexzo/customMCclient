package dev.trexzo.custommc.core.ui.clickgui;

import dev.trexzo.custommc.core.ui.UiDrawCommand;

import java.util.List;

public interface ClickGuiPageContent {
    List<UiDrawCommand> compose(
            ClickGuiContentContext context);
}
