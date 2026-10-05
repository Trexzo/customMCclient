package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.event.EventBus;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.service.ServiceRegistry;
import dev.trexzo.custommc.platform.PlatformContext;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189PlatformTest {
    @Test
    void lifecycleIsExplicitAndSingleAttach() {
        final Minecraft189Platform platform =
                new Minecraft189Platform();
        final PlatformContext context = context();

        assertFalse(platform.attached());
        platform.attach(context);
        assertTrue(platform.attached());

        assertThrows(
                IllegalStateException.class,
                () -> platform.attach(context));

        platform.detach();
        platform.detach();
        assertFalse(platform.attached());
    }

    @Test
    void hooksPublishThroughPlatformContext() {
        final EventBus events = new EventBus();
        final PlatformContext context = context(events);
        final Minecraft189Platform platform =
                new Minecraft189Platform();
        final AtomicLong seen = new AtomicLong(-1L);

        events.subscribe(
                Minecraft189Hooks.TickEvent.class,
                event -> seen.set(event.tickIndex()));

        platform.attach(context);
        new Minecraft189Hooks(platform).publishTick(42L);

        assertEquals(42L, seen.get());
    }

    @Test
    void hooksRejectUseBeforeAttach() {
        final Minecraft189Platform platform =
                new Minecraft189Platform();

        assertThrows(
                IllegalStateException.class,
                () -> new Minecraft189Hooks(platform)
                        .publishTick(0L));
    }

    private static PlatformContext context() {
        return context(new EventBus());
    }

    private static PlatformContext context(
            final EventBus events) {
        final ModuleRegistry modules =
                new ModuleRegistry();
        return new PlatformContext(
                events,
                modules,
                new ModuleController(modules),
                new ServiceRegistry());
    }
}
