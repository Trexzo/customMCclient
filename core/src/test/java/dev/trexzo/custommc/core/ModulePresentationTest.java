package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.module.ModuleCategoryDescriptor;
import dev.trexzo.custommc.core.module.ModuleCategoryRegistry;
import dev.trexzo.custommc.core.module.ModuleDescriptor;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ModulePresentationTest {
    @Test
    void descriptorNormalizesPresentationTextAndDefaults() {
        final ModuleDescriptor descriptor =
                new ModuleDescriptor(
                        " combat.aura ",
                        " Kill Aura ",
                        " Targets nearby entities ");

        assertEquals(
                "combat.aura",
                descriptor.moduleId());
        assertEquals(
                "Kill Aura",
                descriptor.displayName());
        assertEquals(
                "Targets nearby entities",
                descriptor.description());
        assertEquals(
                ModuleDescriptor.DEFAULT_CATEGORY_ID,
                descriptor.categoryId());
        assertEquals(
                0,
                descriptor.priority());
    }

    @Test
    void descriptorCarriesExplicitCategoryAndPriority() {
        final ModuleDescriptor descriptor =
                new ModuleDescriptor(
                        "combat.aura",
                        "Kill Aura",
                        "Targets nearby entities",
                        " combat ",
                        -10);

        assertEquals(
                "combat",
                descriptor.categoryId());
        assertEquals(
                -10,
                descriptor.priority());
    }

    @Test
    void registryOwnsSingleDescriptorLifetimePerModule() {
        final ModulePresentationRegistry registry =
                new ModulePresentationRegistry();
        final ModuleDescriptor descriptor =
                new ModuleDescriptor(
                        "combat.aura",
                        "Kill Aura",
                        "Targets nearby entities");

        final ModulePresentationRegistry.Registration registration =
                registry.register(descriptor);

        assertTrue(registration.active());
        assertEquals(
                descriptor,
                registry.find("combat.aura"));

        assertThrows(
                IllegalArgumentException.class,
                () -> registry.register(
                        new ModuleDescriptor(
                                "combat.aura",
                                "Other",
                                "")));

        registration.close();

        assertFalse(registration.active());
        assertNull(registry.find("combat.aura"));

        registration.close();
        assertNull(registry.find("combat.aura"));
    }

    @Test
    void categoryRegistryOwnsSingleDescriptorLifetimePerCategory() {
        final ModuleCategoryRegistry registry =
                new ModuleCategoryRegistry();
        final ModuleCategoryDescriptor descriptor =
                new ModuleCategoryDescriptor(
                        "combat",
                        "Combat",
                        10);

        final ModuleCategoryRegistry.Registration registration =
                registry.register(descriptor);

        assertTrue(registration.active());
        assertEquals(
                descriptor,
                registry.find("combat"));

        assertThrows(
                IllegalArgumentException.class,
                () -> registry.register(
                        new ModuleCategoryDescriptor(
                                "combat",
                                "Other",
                                0)));

        registration.close();

        assertFalse(registration.active());
        assertNull(registry.find("combat"));
    }

    @Test
    void descriptorsRejectBlankIdentityAndDisplayNames() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ModuleDescriptor(
                        " ",
                        "Aura",
                        ""));
        assertThrows(
                IllegalArgumentException.class,
                () -> new ModuleDescriptor(
                        "combat.aura",
                        " ",
                        ""));
        assertThrows(
                IllegalArgumentException.class,
                () -> new ModuleDescriptor(
                        "combat.aura",
                        "Aura",
                        "",
                        " ",
                        0));
        assertThrows(
                IllegalArgumentException.class,
                () -> new ModuleCategoryDescriptor(
                        " ",
                        "Combat",
                        0));
        assertThrows(
                IllegalArgumentException.class,
                () -> new ModuleCategoryDescriptor(
                        "combat",
                        " ",
                        0));
    }
}
