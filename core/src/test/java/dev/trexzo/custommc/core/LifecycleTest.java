package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleLifecycleException;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleState;
import dev.trexzo.custommc.core.service.ServiceRegistry;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class LifecycleTest {
    @Test
    void servicesAreExplicitAndUniqueByContractType() {
        final ServiceRegistry services = new ServiceRegistry();
        final Clock clock = new Clock();

        services.register(Ticker.class, clock);

        assertSame(clock, services.require(Ticker.class));
        assertThrows(
                IllegalArgumentException.class,
                () -> services.register(Ticker.class, new Clock()));
    }

    @Test
    void modulesTransitionThroughOwnedLifecycle() {
        final ModuleRegistry modules = new ModuleRegistry();
        final AtomicInteger enables = new AtomicInteger();
        final AtomicInteger disables = new AtomicInteger();

        modules.register(new Module() {
            @Override
            public String id() {
                return "hud";
            }

            @Override
            public void onEnable() {
                enables.incrementAndGet();
            }

            @Override
            public void onDisable() {
                disables.incrementAndGet();
            }
        });

        final ModuleController controller =
                new ModuleController(modules);

        assertEquals(ModuleState.DISABLED, controller.stateOf("hud"));

        controller.enable("hud");
        controller.enable("hud");
        assertEquals(ModuleState.ENABLED, controller.stateOf("hud"));
        assertEquals(1, enables.get());

        controller.disable("hud");
        controller.disable("hud");
        assertEquals(ModuleState.DISABLED, controller.stateOf("hud"));
        assertEquals(1, disables.get());
    }

    @Test
    void failedEnableIsVisibleAndCanBeCleanedUp() {
        final ModuleRegistry modules = new ModuleRegistry();
        final AtomicInteger cleanups = new AtomicInteger();

        modules.register(new Module() {
            @Override
            public String id() {
                return "broken";
            }

            @Override
            public void onEnable() {
                throw new IllegalStateException("boom");
            }

            @Override
            public void onDisable() {
                cleanups.incrementAndGet();
            }
        });

        final ModuleController controller =
                new ModuleController(modules);

        assertThrows(
                ModuleLifecycleException.class,
                () -> controller.enable("broken"));
        assertEquals(ModuleState.FAILED, controller.stateOf("broken"));

        controller.disable("broken");
        assertEquals(ModuleState.DISABLED, controller.stateOf("broken"));
        assertEquals(1, cleanups.get());
    }

    private interface Ticker {
    }

    private static final class Clock implements Ticker {
    }
}
