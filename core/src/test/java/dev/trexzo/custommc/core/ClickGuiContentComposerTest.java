package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.ui.UiClipCommand;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiFonts;
import dev.trexzo.custommc.core.ui.UiTextCommand;
import dev.trexzo.custommc.core.ui.UiThemes;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiComposer;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiContentRegistry;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiPage;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiSnapshot;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ClickGuiContentComposerTest {
    @Test
    void selectedRegisteredContentReplacesFallbackBody() {
        final ClickGuiContentRegistry registry =
                new ClickGuiContentRegistry();
        registry.register(
                "modules",
                context ->
                        Collections.<UiDrawCommand>singletonList(
                                new UiTextCommand(
                                        0,
                                        context.bounds().x() + 8.0F,
                                        context.bounds().y() + 8.0F,
                                        UiFonts.DEFAULT,
                                        "custom-content",
                                        context.theme().color(
                                                dev.trexzo.custommc.core.ui.UiColorRole.TEXT_PRIMARY))));

        final ClickGuiPage page =
                new ClickGuiPage(
                        "modules",
                        "Modules",
                        0);
        final List<UiDrawCommand> commands =
                new ClickGuiComposer(registry)
                        .compose(
                                new ClickGuiSnapshot(
                                        true,
                                        "modules",
                                        "",
                                        Arrays.asList(page)),
                                new UiViewport(
                                        1200,
                                        800,
                                        1.0F),
                                UiThemes.darkDefault());

        boolean sawCustom = false;
        boolean sawTopLevelFallback = false;

        for (UiDrawCommand command : commands) {
            if (command instanceof UiTextCommand
                    && "Modules".equals(
                    ((UiTextCommand) command).text())) {
                sawTopLevelFallback = true;
            }
            if (command instanceof UiClipCommand) {
                for (UiDrawCommand child :
                        ((UiClipCommand) command).commands()) {
                    if (child instanceof UiTextCommand
                            && "custom-content".equals(
                            ((UiTextCommand) child).text())) {
                        sawCustom = true;
                    }
                }
            }
        }

        assertTrue(sawCustom);
        assertFalse(sawTopLevelFallback);
    }
}
