package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * V340: a second valid native sword is not the original HeldItem authority.
 * A native right-click may call back through the Minecraft JVM before
 * isUsingItem and the post-use source checks complete.
 */
final class Minecraft189AutoBlockHeldGenerationTest {
    private static Minecraft189AutoBlockModule enabled() {
        final Minecraft189AutoBlockModule module =
                new Minecraft189AutoBlockModule();
        module.onEnable();
        assertTrue(module.mayStart(true, true, false, false, false, false));
        return module;
    }

    @Test
    void sourceExactNativeSwordCompletesAndRetainsNormalHold() {
        final Minecraft189AutoBlockModule module = enabled();
        final long token = module.beginNativeUse(42L);
        assertNotEquals(0L, token);
        assertTrue(module.completeNativeUse(token, true, false, false, 42L));
        assertTrue(module.ownsBlock());
        assertFalse(module.shouldStop(false, false, true, true));
        assertTrue(module.shouldStop(false, false, true, true));
        assertFalse(module.ownsBlock());
    }

    @Test
    void replacedWithAnIdenticalSwordCannotClaimOldNativeUse() {
        final Minecraft189AutoBlockModule module = enabled();
        final long token = module.beginNativeUse(99L);
        // Fresh HeldItem callback reentered during the original right-click,
        // even if it ultimately reports another ordinary sword.
        assertFalse(module.completeNativeUse(token, true, false, false, 100L));
        assertFalse(module.ownsBlock());
        // Consumed; restoring the original number is not a time machine.
        assertFalse(module.completeNativeUse(token, true, false, false, 99L));
        assertTrue(module.completeNativeUse(
                module.beginNativeUse(100L), true, false, false, 100L));
    }

    @Test
    void legacyCompletionCannotForgeNativeHeldGeneration() {
        final Minecraft189AutoBlockModule module = enabled();
        final long nativeToken = module.beginNativeUse(2L);
        // Legacy fixtures know nothing about genuine HeldItem generation.
        assertFalse(module.completeNativeUse(
                nativeToken, true, false, false));
        assertFalse(module.completeNativeUse(
                nativeToken, true, false, false, 2L));
        assertFalse(module.ownsBlock());

        // Existing source-independent tests retain their old contract.
        final long legacyToken = module.beginNativeUse();
        assertTrue(module.completeNativeUse(
                legacyToken, true, false, false));
        assertTrue(module.ownsBlock());
    }

    @Test
    void newerNativeTokenWinsAndOldOwnerCannotOverrideIt() {
        final Minecraft189AutoBlockModule module = enabled();
        final long old = module.beginNativeUse(15L);
        final long current = module.beginNativeUse(16L);
        assertNotEquals(old, current);
        assertFalse(module.completeNativeUse(old, true, false, false, 15L));
        assertTrue(module.completeNativeUse(current, true, false, false, 16L));
        assertFalse(module.completeNativeUse(old, true, false, false, 16L));
    }

    @Test
    void disableReenableAndPolicyMutationCannotReplayOldSource() {
        final Minecraft189AutoBlockModule module = enabled();
        final long old = module.beginNativeUse(1L);
        module.onDisable();
        module.onEnable();
        assertFalse(module.completeNativeUse(old, true, false, false, 1L));
        final long edited = module.beginNativeUse(5L);
        module.holdTicksSetting().set(6);
        assertFalse(module.completeNativeUse(edited, true, false, false, 5L));
        assertFalse(module.ownsBlock());
        final long current = module.beginNativeUse(6L);
        assertFalse(module.completeNativeUse(current, true, false, true, 6L));
        assertFalse(module.ownsBlock());
        assertTrue(module.completeNativeUse(
                module.beginNativeUse(6L), true, false, false, 6L));
        module.onDisable();
        assertTrue(module.releaseForAction(false));
        assertFalse(module.ownsBlock());
    }

    @Test
    void unavailableGuiOrSwordCannotCompleteSameOwnerToken() {
        final Minecraft189AutoBlockModule module = enabled();
        assertFalse(module.completeNativeUse(
                module.beginNativeUse(77L), true, true, false, 77L));
        assertFalse(module.completeNativeUse(
                module.beginNativeUse(77L), false, false, false, 77L));
        assertFalse(module.ownsBlock());
    }
}
