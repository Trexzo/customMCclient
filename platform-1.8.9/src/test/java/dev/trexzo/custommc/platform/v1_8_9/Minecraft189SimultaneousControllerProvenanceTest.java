package dev.trexzo.custommc.platform.v1_8_9;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * V320 original Mojang controller-argument provenance is DIAGNOSTIC ONLY.
 * The original host's vanilla PlayerControllerMP attackEntity argument
 * must be the sole UUID source, never server coordinates or synthetic ids.
 */
final class Minecraft189SimultaneousControllerProvenanceTest {
    private static final String FLAG = "custommc.acceptance.reportKillAuraSimultaneous";

    @AfterEach void cleanup() {
        System.clearProperty(FLAG);
        Minecraft189SimultaneousTargetAcceptance.reset();
    }

    @Test void classifiesOnlyAnActualNativeUuidAgainstOriginalOwners() {
        UUID a = UUID.fromString("11111111-1111-3111-8111-111111111111");
        UUID b = UUID.fromString("22222222-2222-3222-8222-222222222222");
        UUID other = UUID.fromString("33333333-3333-3333-8333-333333333333");
        assertEquals("A", Minecraft189SimultaneousTargetAcceptance
                .nativeControllerOwner(a, a, b));
        assertEquals("B", Minecraft189SimultaneousTargetAcceptance
                .nativeControllerOwner(b, a, b));
        assertEquals("OTHER", Minecraft189SimultaneousTargetAcceptance
                .nativeControllerOwner(other, a, b));
        assertEquals("UNAVAILABLE", Minecraft189SimultaneousTargetAcceptance
                .nativeControllerOwner(null, a, b));
        assertEquals("OTHER", Minecraft189SimultaneousTargetAcceptance
                .nativeControllerOwner(a, null, b));
    }

    private static String capture(final Object argument, final int repeat) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream previous = System.out;
        PrintStream copy = new PrintStream(bytes, true, "UTF-8");
        try {
            System.setOut(copy);
            for (int n = 0; n < repeat; n++) {
                Minecraft189SimultaneousTargetAcceptance
                        .observeOriginalControllerArgument(argument);
            }
        } finally {
            System.setOut(previous);
            copy.close();
        }
        return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
    }

    @Test void disabledOptInCannotEmitNativeControllerEvidence() throws Exception {
        System.clearProperty(FLAG);
        Minecraft189SimultaneousTargetAcceptance.reset();
        final UUID nativeId = UUID.fromString("456789ab-1234-1234-8234-456789abcdef");
        Object entity = new Minecraft189NativeEntityUuidAccess() {
            public UUID customMcNativeEntityUuid() { return nativeId; }
        };
        assertEquals("", capture(entity, 1));
    }

    @Test void acceptsOnlyOriginalMappedEntityUuidInterfaceAsEvidence()
            throws Exception {
        System.setProperty(FLAG, "true");
        Minecraft189SimultaneousTargetAcceptance.reset();
        UUID realSource = UUID.fromString("456789ab-1234-1234-8234-456789abcdef");
        Object entity = new Minecraft189NativeEntityUuidAccess() {
            public UUID customMcNativeEntityUuid() { return realSource; }
        };
        String original = capture(entity, 1);
        assertTrue(original.contains(
                "CUSTOMMC_189_SIMULTANEOUS_ORIGINAL_CONTROLLER_ARGUMENT_DIAG"));
        assertTrue(original.contains("nativeUuidAccess=true"));
        assertTrue(original.contains("nativeUuid=" + realSource));
        assertTrue(original.contains("owner=OTHER"));
        assertTrue(original.contains("firstOwnedClick=false"));
        assertTrue(original.contains("phase=0"));
        assertFalse(original.contains("PASS=YES"));
        assertFalse(original.contains("attackEntity("));
    }

    @Test void missingMappedOriginalEntityCannotInventAPlayerUuid()
            throws Exception {
        System.setProperty(FLAG, "true");
        Minecraft189SimultaneousTargetAcceptance.reset();
        String original = capture(new Object(), 1);
        assertTrue(original.contains("nativeUuidAccess=false"));
        assertTrue(original.contains("nativeUuid=null"));
        assertTrue(original.contains("owner=UNAVAILABLE"));
        assertFalse(original.contains("PASS=YES"));
    }

    @Test void limitRealControllerSourceTo32EventsPerOriginalJvm()
            throws Exception {
        System.setProperty(FLAG, "true");
        Minecraft189SimultaneousTargetAcceptance.reset();
        String original = capture(new Object(), 36);
        String[] lines = original.trim().split("\\r?\\n");
        assertEquals(32, lines.length);
        assertTrue(lines[0].contains("event=1 "));
        assertTrue(lines[31].contains("event=32 "));
        assertFalse(original.contains("event=33 "));
        assertFalse(original.contains("PASS=YES"));
        assertTrue(capture(new Object(), 1).isEmpty());
        Minecraft189SimultaneousTargetAcceptance.reset();
        assertTrue(capture(new Object(), 1).contains("event=1 "));
    }
}
