package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Offline diagnostic-provenance regression only. No original controller
 * invocation, UUID, hurtTime or server damage can be certified here.
 */
final class Minecraft189AutoClickerNativeTraceTest {
    private static final String REPORT =
            "custommc.acceptance.reportAutoClicker";
    private static final String EXACT =
            "custommc.acceptance.requireAutoClickerExactRemoteUuid";

    @Test
    void boundedTraceSeparatesEntryFromGuardAndReportsCounterWithoutGrantingClick()
            throws Exception {
        final String oldReport = System.getProperty(REPORT);
        final String oldExact = System.getProperty(EXACT);
        final PrintStream originalOutput = System.out;
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
            System.setProperty(REPORT, "true");
            System.setProperty(EXACT, "true");
            System.setOut(new PrintStream(bytes, true, "UTF-8"));
            Minecraft189VanillaMeleeAcceptance.reset();
            Minecraft189VanillaMeleeAcceptance.syntheticClickStarted();
            Minecraft189VanillaMeleeAcceptance.onNativeClick(null);
            Minecraft189VanillaMeleeAcceptance.onNativeClickGuard(42);
            Minecraft189VanillaMeleeAcceptance.onNativeClickGuard(42);
            Minecraft189VanillaMeleeAcceptance.onNativeClickGuard(0);
            Minecraft189VanillaMeleeAcceptance.syntheticClickFinished();

            final String logged = new String(
                    bytes.toByteArray(), StandardCharsets.UTF_8);
            assertTrue(logged.contains(
                    "CUSTOMMC_189_V329_SYNTHETIC_CLICK_ENTRY exactRemoteProperty=true"));
            assertTrue(logged.contains(
                    "CUSTOMMC_189_V329_FIRST_GUARD_CALLBACK synthetic=true"
                    + " exactRemoteProperty=true sourceCounter=42"));
            assertTrue(logged.contains(
                    "CUSTOMMC_189_V329_SYNTHETIC_GUARD_CALLBACK"
                    + " exactRemoteProperty=true sourceCounter=42"));
            assertTrue(logged.contains(
                    "CUSTOMMC_189_AUTOCLICKER_MOJANG_LEFT_CLICK_COUNTER_BLOCKED=42"));
            assertTrue(logged.contains(
                    "CUSTOMMC_189_AUTOCLICKER_MOJANG_LEFT_CLICK_COUNTER_READY=YES"));
            assertEquals(1, occurrences(logged, "V329_SYNTHETIC_CLICK_ENTRY"));
            assertEquals(1, occurrences(logged, "V329_FIRST_GUARD_CALLBACK"));
            assertEquals(1, occurrences(logged, "V329_SYNTHETIC_GUARD_CALLBACK"));
            assertFalse(logged.contains("NATIVE_CONTROLLER_"));
        } finally {
            Minecraft189VanillaMeleeAcceptance.syntheticClickFinished();
            Minecraft189VanillaMeleeAcceptance.reset();
            restore(REPORT, oldReport);
            restore(EXACT, oldExact);
            System.setOut(originalOutput);
        }
    }

    @Test
    void traceDoesNotRunWhenAcceptanceReportingDisabled() throws Exception {
        final String oldReport = System.getProperty(REPORT);
        final String oldExact = System.getProperty(EXACT);
        final PrintStream originalOutput = System.out;
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
            System.clearProperty(REPORT);
            System.setProperty(EXACT, "true");
            System.setOut(new PrintStream(bytes, true, "UTF-8"));
            Minecraft189VanillaMeleeAcceptance.reset();
            Minecraft189VanillaMeleeAcceptance.syntheticClickStarted();
            Minecraft189VanillaMeleeAcceptance.onNativeClick(null);
            Minecraft189VanillaMeleeAcceptance.onNativeClickGuard(42);
            assertEquals("", new String(
                    bytes.toByteArray(), StandardCharsets.UTF_8));
        } finally {
            Minecraft189VanillaMeleeAcceptance.syntheticClickFinished();
            Minecraft189VanillaMeleeAcceptance.reset();
            restore(REPORT, oldReport);
            restore(EXACT, oldExact);
            System.setOut(originalOutput);
        }
    }

    private static void restore(final String key, final String value) {
        if (value == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
        }
    }

    private static int occurrences(final String haystack, final String needle) {
        int count = 0;
        int from = 0;
        while ((from = haystack.indexOf(needle, from)) >= 0) {
            count++;
            from += needle.length();
        }
        return count;
    }
}
