package dev.trexzo.custommc.core;

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
    void descriptorNormalizesPresentationText() {
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
    void descriptorRejectsBlankIdentityAndDisplayName() {
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
    }
}
