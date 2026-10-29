package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.module.ModuleCategoryDescriptor;
import dev.trexzo.custommc.core.module.ModuleCategoryRegistry;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleDescriptor;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleState;
import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiClipCommand;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiPointerAction;
import dev.trexzo.custommc.core.ui.UiPointerButton;
import dev.trexzo.custommc.core.ui.UiPointerEvent;
import dev.trexzo.custommc.core.ui.UiTextCommand;
import dev.trexzo.custommc.core.ui.UiThemes;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiContentContext;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiContentInputContext;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiPage;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiSnapshot;
import dev.trexzo.custommc.core.ui.clickgui.ModuleListPageContent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ClickGuiModuleInformationHierarchyTest {
    @Test
    void registeredDescriptionAndCategoryCountRenderWithoutChangingToggleHitbox() {
        final ModuleRegistry modules = new ModuleRegistry();
        modules.register(new Module() {
            @Override public String id() { return "combat.autoclicker"; }
        });
        modules.register(new Module() {
            @Override public String id() { return "combat.velocity"; }
        });
        final ModulePresentationRegistry presentation =
                new ModulePresentationRegistry();
        presentation.register(new ModuleDescriptor(
                "combat.autoclicker", "Auto Clicker",
                "Automates clicking while attacking", "combat", 0));
        presentation.register(new ModuleDescriptor(
                "combat.velocity", "Velocity",
                "Adjusts knockback response", "combat", 1));
        final ModuleCategoryRegistry categories = new ModuleCategoryRegistry();
        categories.register(new ModuleCategoryDescriptor("combat", "Combat", 0));
        final ModuleController controller = new ModuleController(modules);
        final ModuleListPageContent content = new ModuleListPageContent(
                modules, controller, presentation, categories);
        final ClickGuiPage page = new ClickGuiPage("modules", "Modules", 0);
        final ClickGuiSnapshot snapshot = new ClickGuiSnapshot(
                true, "modules", "", Collections.singletonList(page));
        final UiBounds bounds = new UiBounds(200, 100, 500, 320);
        final List<UiDrawCommand> commands = content.compose(
                new ClickGuiContentContext(
                        snapshot, page, bounds, UiThemes.graphite()));
        final List<String> allText = new ArrayList<String>();
        collectText(commands, allText);
        assertTrue(allText.contains("Combat"));
        assertTrue(allText.contains("2 modules"));
        assertTrue(allText.contains("Auto Clicker"));
        assertTrue(allText.contains("Automates clicking while attacking"));
        assertTrue(allText.contains("Adjusts knockback response"));

        // Native first category header occupies y148..166. The first
        // module row remains y166..195, regardless of its new second
        // line. Source pointer routing is unchanged.
        final ClickGuiContentInputContext input =
                new ClickGuiContentInputContext(snapshot, page, bounds);
        assertTrue(content.pointer(input,
                new UiPointerEvent(230, 180,
                        UiPointerButton.LEFT, UiPointerAction.PRESS)));
        assertEquals(ModuleState.ENABLED,
                controller.stateOf("combat.autoclicker"));
        assertEquals(ModuleState.DISABLED,
                controller.stateOf("combat.velocity"));
    }

    private static void collectText(
            final List<UiDrawCommand> commands, final List<String> result) {
        for (UiDrawCommand command : commands) {
            if (command instanceof UiTextCommand) {
                result.add(((UiTextCommand) command).text());
            } else if (command instanceof UiClipCommand) {
                collectText(((UiClipCommand) command).commands(), result);
            }
        }
    }
}
