package dev.trexzo.custommc.core;

import dev.trexzo.custommc.core.render.RenderResource;
import dev.trexzo.custommc.core.render.RenderResourceException;
import dev.trexzo.custommc.core.render.RenderResourceRegistry;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RenderResourceRegistryTest {
    @Test
    void registrationOwnsExactlyOneResourceLifetime() {
        final RenderResourceRegistry registry =
                new RenderResourceRegistry();
        final List<String> closes = new ArrayList<String>();
        final RenderResourceRegistry.Registration registration =
                registry.register(resource("shader", closes, false));

        registration.close();
        registration.close();

        assertFalse(registration.active());
        assertEquals(Arrays.asList("shader"), closes);
        assertEquals(0, registry.snapshot().size());
    }

    @Test
    void registryClosesResourcesInReverseRegistrationOrder() {
        final RenderResourceRegistry registry =
                new RenderResourceRegistry();
        final List<String> closes = new ArrayList<String>();

        registry.register(resource("framebuffer", closes, false));
        registry.register(resource("shader", closes, false));
        registry.register(resource("vertex-buffer", closes, false));

        registry.close();
        registry.close();

        assertTrue(registry.closed());
        assertEquals(
                Arrays.asList(
                        "vertex-buffer",
                        "shader",
                        "framebuffer"),
                closes);
        assertThrows(
                IllegalStateException.class,
                () -> registry.register(
                        resource("late", closes, false)));
    }

    @Test
    void closeContinuesAfterIndividualResourceFailure() {
        final RenderResourceRegistry registry =
                new RenderResourceRegistry();
        final List<String> closes = new ArrayList<String>();

        registry.register(resource("good-first", closes, false));
        registry.register(resource("broken", closes, true));
        registry.register(resource("good-last", closes, false));

        assertThrows(
                RenderResourceException.class,
                registry::close);

        assertEquals(
                Arrays.asList(
                        "good-last",
                        "broken",
                        "good-first"),
                closes);
        assertTrue(registry.closed());
    }

    @Test
    void duplicateResourceIdsAreRejected() {
        final RenderResourceRegistry registry =
                new RenderResourceRegistry();
        final List<String> closes = new ArrayList<String>();

        registry.register(resource("shader", closes, false));

        assertThrows(
                IllegalArgumentException.class,
                () -> registry.register(
                        resource("shader", closes, false)));
    }

    private static RenderResource resource(
            final String id,
            final List<String> closes,
            final boolean fail) {
        return new RenderResource() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public void close() {
                closes.add(id);
                if (fail) {
                    throw new IllegalStateException(
                            "close failure: " + id);
                }
            }
        };
    }
}
