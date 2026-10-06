package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.module.ModuleKeyChord;
import dev.trexzo.custommc.core.module.ModuleKeybind;
import dev.trexzo.custommc.core.module.ModuleKeybindAssignments;
import dev.trexzo.custommc.core.module.ModuleKeybindRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ModuleKeybindAssignmentsTest {
    @Test
    void ownedBindCanRebindUnbindAndCloseWithoutParallelState() {
        final ModuleRegistry modules =
                modules(
                        "combat.aura",
                        "render.esp");
        final ModuleKeybindRegistry registry =
                new ModuleKeybindRegistry(modules);
        final ModuleKeybindAssignments assignments =
                new ModuleKeybindAssignments(registry);

        final ModuleKeyChord first =
                ModuleKeyChord.key(
                        "legacy-key-37");
        final ModuleKeyChord second =
                new ModuleKeyChord(
                        "legacy-key-38",
                        false,
                        true,
                        false);

        assertTrue(
                assignments.bind(
                        " combat.aura ",
                        first));
        assertTrue(
                assignments.owns(
                        "combat.aura"));
        assertEquals(
                first,
                registry.findByModule(
                        "combat.aura")
                        .chord());

        assertFalse(
                assignments.bind(
                        "combat.aura",
                        first));

        assertTrue(
                assignments.bind(
                        "combat.aura",
                        second));
        assertNull(
                registry.findByChord(first));
        assertEquals(
                second,
                registry.findByModule(
                        "combat.aura")
                        .chord());

        assertTrue(
                assignments.unbind(
                        "combat.aura"));
        assertFalse(
                assignments.unbind(
                        "combat.aura"));
        assertNull(
                registry.findByModule(
                        "combat.aura"));

        assignments.bind(
                "render.esp",
                first);
        assignments.close();

        assertTrue(assignments.closed());
        assertNull(
                registry.findByModule(
                        "render.esp"));
        assertThrows(
                IllegalStateException.class,
                () -> assignments.bind(
                        "combat.aura",
                        first));

        assignments.close();
        assertTrue(assignments.closed());
    }

    @Test
    void externalOwnershipCannotBeTakenOverOrRemoved() {
        final ModuleRegistry modules =
                modules(
                        "combat.aura");
        final ModuleKeybindRegistry registry =
                new ModuleKeybindRegistry(modules);
        final ModuleKeybindRegistry.Registration external =
                registry.register(
                        new ModuleKeybind(
                                "combat.aura",
                                ModuleKeyChord.key(
                                        "legacy-key-37")));
        final ModuleKeybindAssignments assignments =
                new ModuleKeybindAssignments(registry);

        assertThrows(
                IllegalStateException.class,
                () -> assignments.bind(
                        "combat.aura",
                        ModuleKeyChord.key(
                                "legacy-key-38")));
        assertThrows(
                IllegalStateException.class,
                () -> assignments.unbind(
                        "combat.aura"));
        assertFalse(
                assignments.owns(
                        "combat.aura"));

        assertEquals(
                "legacy-key-37",
                registry.findByModule(
                        "combat.aura")
                        .chord()
                        .keyId());

        assignments.close();

        assertEquals(
                "legacy-key-37",
                registry.findByModule(
                        "combat.aura")
                        .chord()
                        .keyId());

        external.close();
        assertNull(
                registry.findByModule(
                        "combat.aura"));
    }

    @Test
    void conflictingChordRejectsBeforeExistingOwnedBindChanges() {
        final ModuleRegistry modules =
                modules(
                        "combat.aura",
                        "render.esp");
        final ModuleKeybindRegistry registry =
                new ModuleKeybindRegistry(modules);
        final ModuleKeybindAssignments assignments =
                new ModuleKeybindAssignments(registry);

        final ModuleKeyChord auraChord =
                ModuleKeyChord.key(
                        "legacy-key-37");
        final ModuleKeyChord espChord =
                ModuleKeyChord.key(
                        "legacy-key-38");

        assignments.bind(
                "combat.aura",
                auraChord);
        assignments.bind(
                "render.esp",
                espChord);

        assertThrows(
                IllegalArgumentException.class,
                () -> assignments.bind(
                        "combat.aura",
                        espChord));

        assertEquals(
                auraChord,
                registry.findByModule(
                        "combat.aura")
                        .chord());
        assertEquals(
                espChord,
                registry.findByModule(
                        "render.esp")
                        .chord());
        assertTrue(
                assignments.owns(
                        "combat.aura"));
        assertTrue(
                assignments.owns(
                        "render.esp"));
    }

    @Test
    void unknownModuleAndBlankIdentityStillUseRegistryAuthority() {
        final ModuleRegistry modules =
                modules(
                        "combat.aura");
        final ModuleKeybindAssignments assignments =
                new ModuleKeybindAssignments(
                        new ModuleKeybindRegistry(
                                modules));

        assertThrows(
                IllegalArgumentException.class,
                () -> assignments.bind(
                        "missing.module",
                        ModuleKeyChord.key(
                                "legacy-key-37")));
        assertThrows(
                IllegalArgumentException.class,
                () -> assignments.bind(
                        " ",
                        ModuleKeyChord.key(
                                "legacy-key-37")));
    }

    private static ModuleRegistry modules(
            final String... ids) {
        final ModuleRegistry modules =
                new ModuleRegistry();
        for (String id : ids) {
            modules.register(
                    new Module() {
                        @Override
                        public String id() {
                            return id;
                        }
                    });
        }
        return modules;
    }
}
