package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Adversarial Java tests for original held sword use ownership.
 * These are NOT a substitute for native Minecraft 1.8.9 GUI proof.
 */
final class Minecraft189AutoBlockHeldUsePolicyTest {
    private static Minecraft189AutoBlockModule enabled() {
        final Minecraft189AutoBlockModule m =
                new Minecraft189AutoBlockModule();
        m.onEnable();
        return m;
    }
    private static void sourceUse(final Minecraft189AutoBlockModule m) {
        assertTrue(m.mayStart(true,true,false,false,false,false));
        final long ticket=m.beginNativeUse();
        assertNotEquals(0,ticket);
        assertTrue(m.completeNativeUse(ticket,true,false,false));
        assertTrue(m.ownsBlock());
    }

    @Test void originalRealSwordUseRespectsExactCapturedHoldDuration() {
        final Minecraft189AutoBlockModule m=enabled();
        m.holdTicksSetting().set(4);
        sourceUse(m);
        for(int i=0;i<3;i++)
            assertFalse(m.shouldStop(false,false,true,true));
        assertTrue(m.shouldStop(false,false,true,true));
        assertFalse(m.ownsBlock());
    }
    @Test void shorteningHoldAfterOriginalRightClickReleasesPreviousOwner() {
        final Minecraft189AutoBlockModule m=enabled();
        m.holdTicksSetting().set(8);
        sourceUse(m);
        m.holdTicksSetting().set(1);
        assertTrue(m.shouldStop(false,false,true,true));
        assertFalse(m.ownsBlock());
    }
    @Test void lengtheningHoldCannotProlongOldOriginalNativeUse() {
        final Minecraft189AutoBlockModule m=enabled();
        m.holdTicksSetting().set(1);
        sourceUse(m);
        m.holdTicksSetting().set(20);
        assertTrue(m.shouldStop(false,false,true,true));
        assertFalse(m.shouldStop(false,false,true,true));
    }
    @Test void editedManualPauseWhileSwordIsBlockingReleasesOldOwnership() {
        final Minecraft189AutoBlockModule m=enabled();
        sourceUse(m);
        m.pauseManualRightSetting().set(false);
        assertTrue(m.shouldStop(false,false,true,true));
        assertFalse(m.ownsBlock());
    }
    @Test void userActualManualRightInputAlwaysOwnsReleaseEvenAfterPolicyEdit() {
        final Minecraft189AutoBlockModule m=enabled();
        sourceUse(m);
        m.holdTicksSetting().set(20);
        assertFalse(m.shouldStop(false,true,true,true));
        assertFalse(m.ownsBlock());
        assertFalse(m.releaseForAction(true));
    }
    @Test void exactCurrentPolicyAfterOriginalUseKeepsLegacyTwoTickStop() {
        final Minecraft189AutoBlockModule m=enabled();
        sourceUse(m);
        assertFalse(m.shouldStop(false,false,true,true));
        assertTrue(m.shouldStop(false,false,true,true));
        assertFalse(m.ownsBlock());
    }
    @Test void configEditedAndRestoredBeforeOriginalTickDoesNotInventMutation() {
        final Minecraft189AutoBlockModule m=enabled();
        sourceUse(m);
        m.holdTicksSetting().set(12);
        m.holdTicksSetting().set(2);
        assertFalse(m.shouldStop(false,false,true,true));
        assertTrue(m.shouldStop(false,false,true,true));
    }
    @Test void subsequentSourceRightClickUsesNewLiveHoldPolicy() {
        final Minecraft189AutoBlockModule m=enabled();
        sourceUse(m);
        m.holdTicksSetting().set(3);
        assertTrue(m.shouldStop(false,false,true,true));
        sourceUse(m);
        assertFalse(m.shouldStop(false,false,true,true));
        assertFalse(m.shouldStop(false,false,true,true));
        assertTrue(m.shouldStop(false,false,true,true));
    }
    @Test void moduleDisableWhileNativeBlockOwnedStillRequiresProperRelease() {
        final Minecraft189AutoBlockModule m=enabled();
        sourceUse(m);
        m.onDisable();
        assertTrue(m.ownsBlock());
        assertTrue(m.shouldStop(false,false,true,true));
        assertFalse(m.ownsBlock());
    }
    @Test void competingSyntheticActionCanReleaseOldOriginalBlockOnce() {
        final Minecraft189AutoBlockModule m=enabled();
        sourceUse(m);
        assertTrue(m.releaseForAction(false));
        assertFalse(m.releaseForAction(false));
        assertFalse(m.ownsBlock());
    }
    @Test void manualRightTakeoverAfterGuiMutationNeverSyntheticReleases() {
        final Minecraft189AutoBlockModule m=enabled();
        sourceUse(m);
        m.pauseManualRightSetting().set(false);
        assertFalse(m.releaseForAction(true));
        assertFalse(m.ownsBlock());
    }
    @Test void shutdownCancelsOwnedNativeBlockAndNoDelayedStopReplays() {
        final Minecraft189AutoBlockModule m=enabled();
        sourceUse(m);
        m.forgetForShutdown();
        assertFalse(m.ownsBlock());
        assertFalse(m.shouldStop(false,false,true,true));
        m.onEnable();
        sourceUse(m);
        assertFalse(m.shouldStop(false,false,true,true));
        assertTrue(m.shouldStop(false,false,true,true));
    }
}
