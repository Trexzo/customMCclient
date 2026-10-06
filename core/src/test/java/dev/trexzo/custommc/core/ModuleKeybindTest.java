package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleKeyChord;
import dev.trexzo.custommc.core.module.ModuleKeybind;
import dev.trexzo.custommc.core.module.ModuleKeybindController;
import dev.trexzo.custommc.core.module.ModuleKeybindRegistry;
import dev.trexzo.custommc.core.module.ModuleLifecycleException;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleState;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ModuleKeybindTest {
    @Test
    void registryEnforcesOneModuleAndOneChordUntilRegistrationCloses() {
        final ModuleRegistry modules =
                new ModuleRegistry();
        modules.register(module("combat.aura"));
        modules.register(module("render.esp"));

        final ModuleKeybindRegistry keybinds =
                new ModuleKeybindRegistry(modules);
        final ModuleKeyChord chord =
                new ModuleKeyChord(
                        " legacy-key-37 ",
                        true,
                        false,
                        false);

        final ModuleKeybindRegistry.Registration registration =
                keybinds.register(
                        new ModuleKeybind(
                                " combat.aura ",
                                chord));

        assertTrue(registration.active());
        assertEquals(
                "combat.aura",
                registration.binding()
                        .moduleId());
        assertEquals(
                chord,
                keybinds.findByModule(
                        "combat.aura")
                        .chord());
        assertEquals(
                "combat.aura",
                keybinds.findByChord(
                        new ModuleKeyChord(
                                "legacy-key-37",
                                true,
                                false,
                                false))
                        .moduleId());

        assertThrows(
                IllegalArgumentException.class,
                () -> keybinds.register(
                        new ModuleKeybind(
                                "combat.aura",
                                ModuleKeyChord.key(
                                        "legacy-key-38"))));
        assertThrows(
                IllegalArgumentException.class,
                () -> keybinds.register(
                        new ModuleKeybind(
                                "render.esp",
                                new ModuleKeyChord(
                                        "legacy-key-37",
                                        true,
                                        false,
                                        false))));

        registration.close();

        assertFalse(registration.active());
        assertNull(
                keybinds.findByModule(
                        "combat.aura"));
        assertNull(
                keybinds.findByChord(chord));

        final ModuleKeybindRegistry.Registration rebound =
                keybinds.register(
                        new ModuleKeybind(
                                "render.esp",
                                chord));
        assertEquals(
                "render.esp",
                rebound.binding()
                        .moduleId());

        registration.close();
        assertFalse(registration.active());
    }

    @Test
    void registryRejectsUnknownModulesAndBlankIdentity() {
        final ModuleRegistry modules =
                new ModuleRegistry();
        modules.register(module("combat.aura"));

        final ModuleKeybindRegistry keybinds =
                new ModuleKeybindRegistry(modules);

        assertThrows(
                IllegalArgumentException.class,
                () -> keybinds.register(
                        new ModuleKeybind(
                                "missing.module",
                                ModuleKeyChord.key("k"))));
        assertThrows(
                IllegalArgumentException.class,
                () -> new ModuleKeybind(
                        " ",
                        ModuleKeyChord.key("k")));
        assertThrows(
                IllegalArgumentException.class,
                () -> ModuleKeyChord.key(" "));
    }

    @Test
    void exactChordPressTogglesOnlyThroughModuleController() {
        final AtomicInteger enables =
                new AtomicInteger();
        final AtomicInteger disables =
                new AtomicInteger();
        final ModuleRegistry modules =
                new ModuleRegistry();
        modules.register(
                module(
                        "combat.aura",
                        enables,
                        disables,
                        false));

        final ModuleController lifecycle =
                new ModuleController(modules);
        final ModuleKeybindRegistry keybinds =
                new ModuleKeybindRegistry(modules);
        keybinds.register(
                new ModuleKeybind(
                        "combat.aura",
                        new ModuleKeyChord(
                                "legacy-key-37",
                                false,
                                true,
                                false)));

        final ModuleKeybindController controller =
                new ModuleKeybindController(
                        keybinds,
                        lifecycle);

        assertFalse(
                controller.press(
                        ModuleKeyChord.key(
                                "legacy-key-37")));
        assertEquals(
                ModuleState.DISABLED,
                lifecycle.stateOf(
                        "combat.aura"));

        assertTrue(
                controller.press(
                        new ModuleKeyChord(
                                "legacy-key-37",
                                false,
                                true,
                                false)));
        assertEquals(
                ModuleState.ENABLED,
                lifecycle.stateOf(
                        "combat.aura"));
        assertEquals(
                1,
                enables.get());

        assertTrue(
                controller.press(
                        new ModuleKeyChord(
                                "legacy-key-37",
                                false,
                                true,
                                false)));
        assertEquals(
                ModuleState.DISABLED,
                lifecycle.stateOf(
                        "combat.aura"));
        assertEquals(
                1,
                disables.get());
    }

    @Test
    void failedEnableRemainsControllerOwnedAndNextPressRunsCleanup() {
        final AtomicInteger disables =
                new AtomicInteger();
        final ModuleRegistry modules =
                new ModuleRegistry();
        modules.register(
                module(
                        "broken",
                        new AtomicInteger(),
                        disables,
                        true));

        final ModuleController lifecycle =
                new ModuleController(modules);
        final ModuleKeybindRegistry keybinds =
                new ModuleKeybindRegistry(modules);
        final ModuleKeyChord chord =
                ModuleKeyChord.key(
                        "legacy-key-48");
        keybinds.register(
                new ModuleKeybind(
                        "broken",
                        chord));

        final ModuleKeybindController controller =
                new ModuleKeybindController(
                        keybinds,
                        lifecycle);

        assertThrows(
                ModuleLifecycleException.class,
                () -> controller.press(chord));
        assertEquals(
                ModuleState.FAILED,
                lifecycle.stateOf(
                        "broken"));

        assertTrue(
                controller.press(chord));
        assertEquals(
                ModuleState.DISABLED,
                lifecycle.stateOf(
                        "broken"));
        assertEquals(
                1,
                disables.get());
    }

    private static Module module(
            final String id) {
        return module(
                id,
                new AtomicInteger(),
                new AtomicInteger(),
                false);
    }

    private static Module module(
            final String id,
            final AtomicInteger enables,
            final AtomicInteger disables,
            final boolean failEnable) {
        return new Module() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public void onEnable() {
                enables.incrementAndGet();
                if (failEnable) {
                    throw new IllegalStateException(
                            "boom");
                }
            }

            @Override
            public void onDisable() {
                disables.incrementAndGet();
            }
        };
    }
}
