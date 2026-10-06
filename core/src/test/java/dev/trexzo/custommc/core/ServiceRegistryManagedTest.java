package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.service.ServiceRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ServiceRegistryManagedTest {
    @Test
    void managedRegistrationRemovesOnlyItsExactService() {
        final ServiceRegistry services =
                new ServiceRegistry();
        final String service = "runtime";

        final ServiceRegistry.Registration registration =
                services.registerManaged(
                        CharSequence.class,
                        service);

        assertTrue(registration.active());
        assertSame(
                service,
                registration.service());
        assertSame(
                service,
                services.require(
                        CharSequence.class));

        registration.close();

        assertFalse(registration.active());
        assertFalse(
                services.contains(
                        CharSequence.class));
        assertThrows(
                IllegalStateException.class,
                () -> services.require(
                        CharSequence.class));

        registration.close();
        assertFalse(registration.active());
    }

    @Test
    void legacyUnmanagedRegistrationStillPersists() {
        final ServiceRegistry services =
                new ServiceRegistry();

        services.register(
                CharSequence.class,
                "persistent");

        assertTrue(
                services.contains(
                        CharSequence.class));
    }
}
