package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Diagnostic-only reflection tests. A fake object may never be mistaken
 * for original Minecraft 'ave', nor certify any native player combat.
 */
final class Minecraft189OriginalGuiClassProbeTest {
    private static final class OneDeclaredScreen {
        Object screen;
    }

    private static final class TwoDeclaredScreens {
        Object first;
        Object second;
    }

    private static final class Unrelated {
        String screen;
    }

    @Test
    void genuineOriginalClassGateRejectsAllOrdinaryTestDoubles() {
        assertEquals("UNAVAILABLE",
                Minecraft189OriginalGuiClassProbe.inspect(new OneDeclaredScreen()));
        assertEquals("UNAVAILABLE",
                Minecraft189OriginalGuiClassProbe.inspect(null));
    }

    @Test
    void exactUniqueDeclaredScreenReflectsOnlyTypeNeverGuiContents() {
        final OneDeclaredScreen host = new OneDeclaredScreen();
        assertEquals("CLEAR",
                Minecraft189OriginalGuiClassProbe.inspectFieldOfType(
                        host, "java.lang.Object"));
        host.screen = new StringBuilder("private contents not logged");
        assertEquals("java.lang.StringBuilder",
                Minecraft189OriginalGuiClassProbe.inspectFieldOfType(
                        host, "java.lang.Object"));
    }

    @Test
    void unknownAmbiguousOrInvalidSourcesNeverImplyClear() {
        assertEquals("UNAVAILABLE",
                Minecraft189OriginalGuiClassProbe.inspectFieldOfType(
                        new TwoDeclaredScreens(), "java.lang.Object"));
        assertEquals("UNAVAILABLE",
                Minecraft189OriginalGuiClassProbe.inspectFieldOfType(
                        new Unrelated(), "java.lang.Object"));
        assertEquals("UNAVAILABLE",
                Minecraft189OriginalGuiClassProbe.inspectFieldOfType(
                        new OneDeclaredScreen(), ""));
        assertEquals("UNAVAILABLE",
                Minecraft189OriginalGuiClassProbe.inspectFieldOfType(
                        null, "java.lang.Object"));
    }
}
