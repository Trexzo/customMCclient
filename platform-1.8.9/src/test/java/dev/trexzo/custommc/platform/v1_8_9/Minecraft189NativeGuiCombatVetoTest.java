package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Original vanilla GUI screen admission is independent of click scheduling.
 * Non-original doubles are NEVER accepted as Mojang native-screen evidence.
 * These tests do not certify native controller UUID or server-owned damage.
 */
final class Minecraft189NativeGuiCombatVetoTest {
    @Test
    void openUnavailableAndMissingNativeEvidenceAlwaysVeto() {
        assertTrue(Minecraft189NativeGuiCombatVeto.blocksOriginalVerdict(
                Minecraft189ScaffoldScreenGate.Verdict.OPEN));
        assertTrue(Minecraft189NativeGuiCombatVeto.blocksOriginalVerdict(
                Minecraft189ScaffoldScreenGate.Verdict.UNAVAILABLE));
        assertTrue(Minecraft189NativeGuiCombatVeto.blocksOriginalVerdict(null));
    }

    @Test
    void sourceVerifiedClearOriginalScreenCanProceedToOtherGates() {
        assertFalse(Minecraft189NativeGuiCombatVeto.blocksOriginalVerdict(
                Minecraft189ScaffoldScreenGate.Verdict.CLEAR));
    }

    @Test
    void ordinarySourceDoublesRetainTheirExistingUnitTestContract() {
        assertFalse(Minecraft189NativeGuiCombatVeto.blocks(null));
        assertFalse(Minecraft189NativeGuiCombatVeto.blocks(new Object()));
    }
}
