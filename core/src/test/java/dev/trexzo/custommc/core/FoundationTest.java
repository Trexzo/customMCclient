package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.event.EventBus;
import dev.trexzo.custommc.core.module.Module;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.setting.Setting;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class FoundationTest {
    @Test
    void eventSubscriptionHasExplicitLifetime() {
        final EventBus bus = new EventBus();
        final AtomicInteger count = new AtomicInteger();

        final EventBus.Subscription subscription =
                bus.subscribe(String.class, value -> count.incrementAndGet());

        bus.publish("first");
        subscription.close();
        bus.publish("second");

        assertEquals(1, count.get());
    }

    @Test
    void moduleIdsAreUnique() {
        final ModuleRegistry registry = new ModuleRegistry();
        registry.register(module("combat"));

        assertThrows(
                IllegalArgumentException.class,
                () -> registry.register(module("combat")));
    }

    @Test
    void settingsValidateAndReset() {
        final Setting<Integer> range =
                new Setting<Integer>("range", 3, value -> value >= 1 && value <= 6);

        range.set(5);
        assertEquals(5, range.get());
        assertThrows(IllegalArgumentException.class, () -> range.set(10));

        range.reset();
        assertEquals(3, range.get());
    }

    private static Module module(final String id) {
        return new Module() {
            @Override
            public String id() {
                return id;
            }
        };
    }
}
