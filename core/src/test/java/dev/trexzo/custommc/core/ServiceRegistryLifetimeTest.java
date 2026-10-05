package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.service.ServiceRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ServiceRegistryLifetimeTest {
    @Test
    void closingRegistrationRemovesOnlyItsService() {
        final ServiceRegistry services =
                new ServiceRegistry();
        final Clock first = new Clock();

        final ServiceRegistry.Registration<Ticker>
                registration =
                services.register(
                        Ticker.class,
                        first);

        assertTrue(registration.active());
        assertSame(
                first,
                services.require(Ticker.class));

        registration.close();
        registration.close();

        assertFalse(registration.active());
        assertFalse(services.contains(Ticker.class));

        final Clock second = new Clock();
        services.register(Ticker.class, second);
        assertSame(
                second,
                services.require(Ticker.class));
    }

    private interface Ticker {
    }

    private static final class Clock implements Ticker {
    }
}
