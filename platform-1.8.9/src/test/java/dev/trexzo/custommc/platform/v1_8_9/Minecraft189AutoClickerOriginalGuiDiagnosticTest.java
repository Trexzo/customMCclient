package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** No fabricated original Minecraft class or authority is used. */
final class Minecraft189AutoClickerOriginalGuiDiagnosticTest {
    private static final String REPORT = "custommc.acceptance.reportAutoClicker";
    private static final String EXACT =
            "custommc.acceptance.requireAutoClickerExactRemoteUuid";

    @Test
    void unavailableMockCannotBeMistakenForARealGuiOrAuthorizeDamage()
            throws Exception {
        final String beforeReport = System.getProperty(REPORT);
        final String beforeExact = System.getProperty(EXACT);
        final PrintStream output = System.out;
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
            System.setProperty(REPORT, "true");
            System.setProperty(EXACT, "true");
            System.setOut(new PrintStream(bytes, true, "UTF-8"));
            Minecraft189VanillaMeleeAcceptance.reset();
            Minecraft189VanillaMeleeAcceptance.syntheticClickStarted();
            Minecraft189VanillaMeleeAcceptance.onNativeClick(null);
            Minecraft189VanillaMeleeAcceptance.onNativeClick(null);
            Minecraft189VanillaMeleeAcceptance.syntheticClickFinished();

            final String logged =
                    new String(bytes.toByteArray(), StandardCharsets.UTF_8);
            assertEquals(1, occurrences(logged,
                    "CUSTOMMC_189_V330_AUTOCLICKER_ORIGINAL_GUI_SCREEN=UNAVAILABLE"));
            assertFalse(logged.contains("ORIGINAL_GUI_SCREEN=CLEAR"));
            assertFalse(logged.contains("ORIGINAL_GUI_SCREEN=OPEN"));
            assertFalse(logged.contains("NATIVE_CONTROLLER"));
            assertFalse(logged.contains("SERVER_ACCEPTED"));
        } finally {
            Minecraft189VanillaMeleeAcceptance.syntheticClickFinished();
            Minecraft189VanillaMeleeAcceptance.reset();
            restore(REPORT, beforeReport);
            restore(EXACT, beforeExact);
            System.setOut(output);
        }
    }

    @Test
    void originalGuiDiagnosticIsStrictRemoteOptIn() throws Exception {
        final String oldReport = System.getProperty(REPORT);
        final String oldExact = System.getProperty(EXACT);
        final PrintStream output = System.out;
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
            System.setProperty(REPORT, "true");
            System.clearProperty(EXACT);
            System.setOut(new PrintStream(bytes, true, "UTF-8"));
            Minecraft189VanillaMeleeAcceptance.reset();
            Minecraft189VanillaMeleeAcceptance.syntheticClickStarted();
            Minecraft189VanillaMeleeAcceptance.onNativeClick(null);
            Minecraft189VanillaMeleeAcceptance.syntheticClickFinished();
            assertFalse(new String(bytes.toByteArray(), StandardCharsets.UTF_8)
                    .contains("V330_AUTOCLICKER_ORIGINAL_GUI_SCREEN"));
        } finally {
            Minecraft189VanillaMeleeAcceptance.syntheticClickFinished();
            Minecraft189VanillaMeleeAcceptance.reset();
            restore(REPORT, oldReport);
            restore(EXACT, oldExact);
            System.setOut(output);
        }
    }

    private static int occurrences(final String input, final String fragment) {
        int count = 0;
        int from = 0;
        while ((from = input.indexOf(fragment, from)) >= 0) {
            ++count;
            from += fragment.length();
        }
        return count;
    }

    private static void restore(final String property, final String previous) {
        if (previous == null) System.clearProperty(property);
        else System.setProperty(property, previous);
    }
}
