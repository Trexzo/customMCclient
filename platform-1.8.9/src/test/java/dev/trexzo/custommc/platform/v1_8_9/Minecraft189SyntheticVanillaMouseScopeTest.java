package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * R8: a clickMouse callback alone is never packet/damage proof.
 * These tests cover original-controller scope ownership only.
 */
final class Minecraft189SyntheticVanillaMouseScopeTest {
    @Test
    void genuineManualVanillaMouseStartsUnscoped() {
        assertFalse(Minecraft189SyntheticVanillaMouseScope.active());
    }

    @Test
    void syntheticVanillaCallbackHasExactlyOneFrameAndClearsOnExit() {
        assertFalse(Minecraft189SyntheticVanillaMouseScope.active());
        try (Minecraft189SyntheticVanillaMouseScope.Scope ignored =
                Minecraft189SyntheticVanillaMouseScope.enter()) {
            assertTrue(Minecraft189SyntheticVanillaMouseScope.active());
        }
        assertFalse(Minecraft189SyntheticVanillaMouseScope.active());
    }

    @Test
    void nestedOriginalCallbacksPreserveOuterScopeAndRevokeInner() {
        try (Minecraft189SyntheticVanillaMouseScope.Scope outer =
                Minecraft189SyntheticVanillaMouseScope.enter()) {
            assertTrue(Minecraft189SyntheticVanillaMouseScope.active());
            try (Minecraft189SyntheticVanillaMouseScope.Scope inner =
                    Minecraft189SyntheticVanillaMouseScope.enter()) {
                assertTrue(Minecraft189SyntheticVanillaMouseScope.active());
            }
            assertTrue(Minecraft189SyntheticVanillaMouseScope.active());
        }
        assertFalse(Minecraft189SyntheticVanillaMouseScope.active());
    }

    @Test
    void nativeGetterExceptionCannotLeakOldControllerPermission() {
        assertThrows(IllegalStateException.class, () -> {
            try (Minecraft189SyntheticVanillaMouseScope.Scope ignored =
                    Minecraft189SyntheticVanillaMouseScope.enter()) {
                assertTrue(Minecraft189SyntheticVanillaMouseScope.active());
                throw new IllegalStateException("mapped entity getter failed");
            }
        });
        assertFalse(Minecraft189SyntheticVanillaMouseScope.active());
    }

    @Test
    void outOfOrderClosureFailsClosedAndNeverRestoresOldScope() {
        final Minecraft189SyntheticVanillaMouseScope.Scope outer =
                Minecraft189SyntheticVanillaMouseScope.enter();
        final Minecraft189SyntheticVanillaMouseScope.Scope inner =
                Minecraft189SyntheticVanillaMouseScope.enter();
        outer.close();
        assertFalse(Minecraft189SyntheticVanillaMouseScope.active());
        inner.close();
        assertFalse(Minecraft189SyntheticVanillaMouseScope.active());
        outer.close();
        assertFalse(Minecraft189SyntheticVanillaMouseScope.active());
    }

    @Test
    void originalControllerScopeDoesNotCrossThreads() throws Exception {
        assertFalse(Minecraft189SyntheticVanillaMouseScope.active());
        final java.util.concurrent.atomic.AtomicReference<Throwable> failure =
                new java.util.concurrent.atomic.AtomicReference<Throwable>();
        try (Minecraft189SyntheticVanillaMouseScope.Scope ignored =
                Minecraft189SyntheticVanillaMouseScope.enter()) {
            final Thread observer = new Thread(() -> {
                try {
                    assertFalse(Minecraft189SyntheticVanillaMouseScope.active(),
                            "original controller admission is thread-local");
                    try (Minecraft189SyntheticVanillaMouseScope.Scope local =
                            Minecraft189SyntheticVanillaMouseScope.enter()) {
                        assertTrue(Minecraft189SyntheticVanillaMouseScope.active());
                    }
                    assertFalse(Minecraft189SyntheticVanillaMouseScope.active());
                } catch (Throwable testFailure) {
                    failure.set(testFailure);
                }
            }, "r8-test-foreign-controller");
            observer.start();
            observer.join(2000L);
            assertFalse(observer.isAlive(), "scope test thread failed to finish");
            if (failure.get() != null)
                fail("foreign thread observed or leaked a synthetic scope",
                        failure.get());
            assertTrue(Minecraft189SyntheticVanillaMouseScope.active());
        }
        assertFalse(Minecraft189SyntheticVanillaMouseScope.active());
    }
}
