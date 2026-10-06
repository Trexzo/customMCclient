package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ModuleRegistryOwnershipTest {
    @Test
    void closingRegistrationRemovesOnlyExactOwnedModule() {
        final ModuleRegistry registry =
                new ModuleRegistry();
        final Module first =
                module("render.watermark");

        final ModuleRegistry.Registration registration =
                registry.register(first);

        assertTrue(registration.active());
        assertEquals(
                first,
                registry.find("render.watermark"));

        registration.close();

        assertFalse(registration.active());
        assertNull(
                registry.find("render.watermark"));

        registration.close();
        assertNull(
                registry.find("render.watermark"));
    }

    @Test
    void reusingModuleIdDoesNotReusePreviousLifecycleState() {
        final ModuleRegistry registry =
                new ModuleRegistry();
        final ModuleController controller =
                new ModuleController(registry);

        final ModuleRegistry.Registration first =
                registry.register(
                        module("render.watermark"));
        controller.enable(
                "render.watermark");
        assertEquals(
                ModuleState.ENABLED,
                controller.stateOf(
                        "render.watermark"));

        first.close();

        final ModuleRegistry.Registration second =
                registry.register(
                        module("render.watermark"));

        assertEquals(
                ModuleState.DISABLED,
                controller.stateOf(
                        "render.watermark"));

        second.close();
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
