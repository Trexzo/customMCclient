package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
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

        boolean sawTitle = false;
        boolean sawEsp = false;
        boolean sawEnabled = false;
        boolean sawFly = false;

        for (UiDrawCommand command : commands) {
            if (!(command instanceof UiTextCommand)) {
                continue;
            }
            final String text =
                    ((UiTextCommand) command).text();
            sawTitle |= "Modules".equals(text);
            sawEsp |= "esp".equals(text);
            sawEnabled |= "ENABLED".equals(text);
            sawFly |= "fly".equals(text);
        }

        assertTrue(sawTitle);
        assertTrue(sawEsp);
        assertTrue(sawEnabled);
        assertFalse(sawFly);
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
