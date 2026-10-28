package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiClipCommand;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiRoundedRectCommand;
import dev.trexzo.custommc.core.ui.UiColorRole;
import dev.trexzo.custommc.core.ui.UiTextCommand;
import dev.trexzo.custommc.core.ui.UiThemes;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiContentContext;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiContentRegistry;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiPage;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiPageContent;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiSnapshot;
import dev.trexzo.custommc.core.ui.clickgui.ModuleListPageContent;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ClickGuiPageContentTest {
    @Test
    void contentRegistrationHasExplicitLifetime() {
        final ClickGuiContentRegistry registry =
                new ClickGuiContentRegistry();
        final ClickGuiPageContent content =
                context ->
                        Collections.<UiDrawCommand>emptyList();

        final ClickGuiContentRegistry.Registration registration =
                registry.register(
                        "modules",
                        content);

        assertTrue(registration.active());
        assertEquals(
                content,
                registry.find("modules"));
        assertThrows(
                IllegalArgumentException.class,
                () -> registry.register(
                        "modules",
                        content));

        registration.close();
        registration.close();

        assertFalse(registration.active());
        assertNull(registry.find("modules"));
    }

    @Test
    void moduleContentFiltersByRetainedSearchAndReadsControllerState() {
        final ModuleRegistry modules =
                new ModuleRegistry();
        modules.register(module("fly"));
        modules.register(module("esp"));

        final ModuleController controller =
                new ModuleController(modules);
        controller.enable("esp");

        final ModuleListPageContent content =
                new ModuleListPageContent(
                        modules,
                        controller);
        final ClickGuiPage page =
                new ClickGuiPage(
                        "modules",
                        "Modules",
                        0);
        final ClickGuiSnapshot snapshot =
                new ClickGuiSnapshot(
                        true,
                        "modules",
                        "esp",
                        Arrays.asList(page));

        final List<UiDrawCommand> commands =
                content.compose(
                        new ClickGuiContentContext(
                                snapshot,
                                page,
                                new UiBounds(
                                        200.0F,
                                        100.0F,
                                        600.0F,
                                        400.0F),
                                UiThemes.darkDefault()));

        boolean sawEnabled = false;
        for (UiDrawCommand command : commands) {
            if (command instanceof UiRoundedRectCommand) {
                sawEnabled |= ((UiRoundedRectCommand) command).argb()
                        == UiThemes.darkDefault().color(UiColorRole.ACCENT);
            }
        }

        assertTrue(containsText(commands, "Modules"));
        assertTrue(containsText(commands, "esp"));
        assertTrue(sawEnabled);
        assertFalse(containsText(commands, "fly"));
    }

    private static boolean containsText(
            final List<UiDrawCommand> commands,
            final String expected) {
        for (UiDrawCommand command : commands) {
            if (command instanceof UiTextCommand
                    && expected.equals(((UiTextCommand) command).text())) {
                return true;
            }
            if (command instanceof UiClipCommand
                    && containsText(((UiClipCommand) command).commands(), expected)) {
                return true;
            }
        }
        return false;
    }

    private static Module module(
            final String id) {
        return new Module() {
            @Override
            public String id() {
                return id;
            }
        };
    }
}
