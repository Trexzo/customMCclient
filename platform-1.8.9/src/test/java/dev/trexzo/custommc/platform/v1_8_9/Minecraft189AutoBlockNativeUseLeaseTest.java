package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Real right-click completion policy, not Mojang original runtime proof. */
final class Minecraft189AutoBlockNativeUseLeaseTest {
    private static Minecraft189AutoBlockModule enabled() {
        final Minecraft189AutoBlockModule module = new Minecraft189AutoBlockModule();
        module.onEnable();
        return module;
    }
    private static long admitted(final Minecraft189AutoBlockModule module) {
        assertTrue(module.mayStart(true, true, false, false, false, false));
        final long token = module.beginNativeUse();
        assertNotEquals(0, token);
        return token;
    }

    @Test void disabledModuleCannotAcquireOriginalRightClickLease() {
        final Minecraft189AutoBlockModule module = new Minecraft189AutoBlockModule();
        assertEquals(0, module.beginNativeUse());
        assertFalse(module.completeNativeUse(0, true, false, false));
        assertFalse(module.ownsBlock());
    }
    @Test void verifiedOriginalSwordRightClickCommitsOnlyOnce() {
        final Minecraft189AutoBlockModule module = enabled();
        final long token = admitted(module);
        assertTrue(module.completeNativeUse(token, true, false, false));
        assertTrue(module.ownsBlock());
        assertFalse(module.completeNativeUse(token, true, false, false));
        assertEquals(0, module.beginNativeUse());
        assertFalse(module.shouldStop(false, false, true, true));
        assertTrue(module.shouldStop(false, false, true, true));
        assertFalse(module.ownsBlock());
    }
    @Test void disablingDuringNativeRightClickRevokesOldOwner() {
        final Minecraft189AutoBlockModule module = enabled();
        final long token = admitted(module);
        module.onDisable();
        assertFalse(module.completeNativeUse(token, true, false, false));
        assertFalse(module.ownsBlock());
    }
    @Test void offOnBeforeNativeCompletionCannotClaimNewActivation() {
        final Minecraft189AutoBlockModule module = enabled();
        final long old = admitted(module);
        module.onDisable();
        module.onEnable();
        assertFalse(module.completeNativeUse(old, true, false, false));
        assertFalse(module.ownsBlock());
        final long fresh = admitted(module);
        assertNotEquals(old, fresh);
        assertTrue(module.completeNativeUse(fresh, true, false, false));
    }
    @Test void persistedHoldDurationEditedInNativeCallbackRevokesToken() {
        final Minecraft189AutoBlockModule module = enabled();
        final long token = admitted(module);
        module.holdTicksSetting().set(5);
        assertFalse(module.completeNativeUse(token, true, false, false));
        assertFalse(module.ownsBlock());
        assertTrue(module.completeNativeUse(admitted(module), true, false, false));
    }
    @Test void persistedManualPolicyEditedDuringNativeCallbackRevokesToken() {
        final Minecraft189AutoBlockModule module = enabled();
        final long token = admitted(module);
        module.pauseManualRightSetting().set(false);
        assertFalse(module.completeNativeUse(token, true, false, false));
        assertFalse(module.ownsBlock());
    }
    @Test void originalSwordReplacedDuringNativeCallbackMustNotClaimUse() {
        final Minecraft189AutoBlockModule module = enabled();
        final long token = admitted(module);
        assertFalse(module.completeNativeUse(token, false, false, false));
        assertFalse(module.ownsBlock());
    }
    @Test void originalClickGuiOpeningDuringUseRevokesOldToken() {
        final Minecraft189AutoBlockModule module = enabled();
        final long token = admitted(module);
        assertFalse(module.completeNativeUse(token, true, true, false));
        assertFalse(module.ownsBlock());
    }
    @Test void actualManualRightPressDuringCallbackOwnsInput() {
        final Minecraft189AutoBlockModule module = enabled();
        final long token = admitted(module);
        assertFalse(module.completeNativeUse(token, true, false, true));
        assertFalse(module.ownsBlock());
    }
    @Test void newerUseTokenCancelsOlderRightClickAdmission() {
        final Minecraft189AutoBlockModule module = enabled();
        final long a = admitted(module);
        final long b = admitted(module);
        assertNotEquals(a, b);
        assertFalse(module.completeNativeUse(a, true, false, false));
        assertTrue(module.completeNativeUse(b, true, false, false));
    }
    @Test void failedCompletionCannotBeReplayedAfterGuiCloses() {
        final Minecraft189AutoBlockModule module = enabled();
        final long token = admitted(module);
        assertFalse(module.completeNativeUse(token, true, true, false));
        assertFalse(module.completeNativeUse(token, true, false, false));
        assertFalse(module.ownsBlock());
    }
    @Test void genuineManualRightOwnershipNeverRequiresModuleSyntheticRelease() {
        final Minecraft189AutoBlockModule module = enabled();
        assertTrue(module.completeNativeUse(admitted(module), true, false, false));
        assertFalse(module.shouldStop(false, true, true, true));
        assertFalse(module.ownsBlock());
        assertFalse(module.releaseForAction(false));
    }
    @Test void alreadyOwnedNativeBlockSurvivesDisableUntilGenuineRelease() {
        final Minecraft189AutoBlockModule module = enabled();
        assertTrue(module.completeNativeUse(admitted(module), true, false, false));
        module.onDisable();
        assertTrue(module.ownsBlock());
        assertTrue(module.releaseForAction(false));
        assertFalse(module.ownsBlock());
    }
    @Test void shutdownRevokesNativeTokenWithoutLeakingOwnUse() {
        final Minecraft189AutoBlockModule module = enabled();
        final long old = admitted(module);
        module.forgetForShutdown();
        module.onEnable();
        assertFalse(module.completeNativeUse(old, true, false, false));
        assertFalse(module.ownsBlock());
    }
}
