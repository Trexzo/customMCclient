package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Combat-source veto is independent of synthetic click rate/counter.
 * No fake World, remote UUID or controller attack is accepted as evidence.
 */
final class Minecraft189NativeGuiCombatVetoTest {
    @Test
    void originalOpenAndUnavailableScreensAlwaysVetoAutomaticCombat() {
        assertTrue(Minecraft189NativeGuiCombatVeto.blocksOriginalVerdict(
                Minecraft189ScaffoldScreenGate.Verdict.OPEN));
        assertTrue(Minecraft189NativeGuiCombatVeto.blocksOriginalVerdict(
                Minecraft189ScaffoldScreenGate.Verdict.UNAVAILABLE));
        assertTrue(Minecraft189NativeGuiCombatVeto.blocksOriginalVerdict(null));
    }

    @Test
    void onlySourceVerifiedClearOriginalScreenPermitsScheduling() {
        assertFalse(Minecraft189NativeGuiCombatVeto.blocksOriginalVerdict(
                Minecraft189ScaffoldScreenGate.Verdict.CLEAR));
    }

    @Test
    void NonOriginalTestDoublesCannotClaimNativeScreenAuthority() {
        assertFalse(Minecraft189NativeGuiCombatVeto.blocks(null));
        assertFalse(Minecraft189NativeGuiCombatVeto.blocks(new Object()));
    }
}
