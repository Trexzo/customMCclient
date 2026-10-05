package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.module.ModuleCategoryDescriptor;
import dev.trexzo.custommc.core.module.ModuleCategoryRegistry;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleDescriptor;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.ui.UiBounds;
import dev.trexzo.custommc.core.ui.UiDrawCommand;
import dev.trexzo.custommc.core.ui.UiTextCommand;
import dev.trexzo.custommc.core.ui.UiThemes;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiContentContext;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiPage;
import dev.trexzo.custommc.core.ui.clickgui.ClickGuiSnapshot;
import dev.trexzo.custommc.core.ui.clickgui.ModuleListPageContent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ModuleCategoryPresentationTest {
    @Test
    void categoryAndModulePriorityDriveDeterministicPresentationOrder() {
        final ModuleRegistry modules =
                new ModuleRegistry();
        modules.register(module("render.esp"));
        modules.register(module("combat.velocity"));
        modules.register(module("combat.aura"));

        final ModulePresentationRegistry presentations =
                new ModulePresentationRegistry();
        presentations.register(
                new ModuleDescriptor(
                        "render.esp",
                        "ESP",
                        "Entity overlay",
                        "render",
                        0));
        presentations.register(
                new ModuleDescriptor(
                        "combat.velocity",
                        "Velocity",
                        "Knockback controls",
                        "combat",
                        20));
        presentations.register(
                new ModuleDescriptor(
                        "combat.aura",
                        "Kill Aura",
                        "Targeting controls",
                        "combat",
                        10));

        final ModuleCategoryRegistry categories =
                new ModuleCategoryRegistry();
        categories.register(
                new ModuleCategoryDescriptor(
                        "combat",
                        "Combat",
                        10));
        categories.register(
                new ModuleCategoryDescriptor(
                        "render",
                        "Visuals",
                        20));

        final List<String> text =
                texts(
                        content(
                                modules,
                                presentations,
                                categories)
                                .compose(
                                        context("")));

        assertBefore(text, "Combat", "Kill Aura");
        assertBefore(text, "Kill Aura", "Velocity");
        assertBefore(text, "Velocity", "Visuals");
        assertBefore(text, "Visuals", "ESP");
    }

    @Test
    void categoryDisplayNameParticipatesInSearch() {
        final ModuleRegistry modules =
                new ModuleRegistry();
        modules.register(module("combat.aura"));
        modules.register(module("render.esp"));

        final ModulePresentationRegistry presentations =
                new ModulePresentationRegistry();
        presentations.register(
                new ModuleDescriptor(
                        "combat.aura",
                        "Kill Aura",
                        "Targeting controls",
                        "combat",
                        0));
        presentations.register(
                new ModuleDescriptor(
                        "render.esp",
                        "ESP",
                        "Entity overlay",
                        "render",
                        0));

        final ModuleCategoryRegistry categories =
                new ModuleCategoryRegistry();
        categories.register(
                new ModuleCategoryDescriptor(
                        "combat",
                        "Combat",
                        10));
        categories.register(
                new ModuleCategoryDescriptor(
                        "render",
                        "Visuals",
                        20));

        final List<String> text =
                texts(
                        content(
                                modules,
                                presentations,
                                categories)
                                .compose(
                                        context("visuals")));

        assertTrue(text.contains("Visuals"));
        assertTrue(text.contains("ESP"));
        assertFalse(text.contains("Combat"));
        assertFalse(text.contains("Kill Aura"));
    }

    private static ModuleListPageContent content(
            final ModuleRegistry modules,
            final ModulePresentationRegistry presentations,
            final ModuleCategoryRegistry categories) {
        return new ModuleListPageContent(
                modules,
                new ModuleController(modules),
                presentations,
                categories);
    }

    private static ClickGuiContentContext context(
            final String query) {
        final ClickGuiPage page =
                new ClickGuiPage(
                        "modules",
                        "Modules",
                        0);
        return new ClickGuiContentContext(
                new ClickGuiSnapshot(
                        true,
                        "modules",
                        query,
                        Arrays.asList(page)),
                page,
                new UiBounds(
                        100.0F,
                        100.0F,
                        700.0F,
                        500.0F),
                UiThemes.darkDefault());
    }

    private static List<String> texts(
            final List<UiDrawCommand> commands) {
        final List<String> text =
                new ArrayList<String>();
        for (UiDrawCommand command : commands) {
            if (command instanceof UiTextCommand) {
                text.add(
                        ((UiTextCommand) command)
                                .text());
            }
        }
        return text;
    }

    private static void assertBefore(
            final List<String> text,
            final String left,
            final String right) {
        final int leftIndex =
                text.indexOf(left);
        final int rightIndex =
                text.indexOf(right);
        assertTrue(
                leftIndex >= 0,
                "missing text: " + left);
        assertTrue(
                rightIndex >= 0,
                "missing text: " + right);
        assertTrue(
                leftIndex < rightIndex,
                left + " should appear before " + right);
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
