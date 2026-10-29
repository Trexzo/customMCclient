package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Exact original HeldItem generation at the native right-click boundary. */
final class Minecraft189AutoBlockPreDispatchTest {
    @Test void matchingOriginalSwordTokenAllowsNativeDispatchAndCompletion() {
        final Minecraft189AutoBlockModule block = new Minecraft189AutoBlockModule();
        block.onEnable();
        final long token = block.beginNativeUse(18L);
        assertTrue(token > 0L);
        assertTrue(block.permitsNativeUseDispatch(token, true, false, false, 18L));
        // Verification alone must not consume the token or fake a real block.
        assertTrue(block.permitsNativeUseDispatch(token, true, false, false, 18L));
        assertFalse(block.ownsBlock());
        assertTrue(block.completeNativeUse(token, true, false, false, 18L));
        assertTrue(block.ownsBlock());
        assertFalse(block.permitsNativeUseDispatch(token, true, false, false, 18L));
    }

    @Test void newHeldItemGenerationOrMissingRealSwordVetoesBeforeNativeClick() {
        final Minecraft189AutoBlockModule block = new Minecraft189AutoBlockModule();
        block.onEnable();
        final long token = block.beginNativeUse(33L);
        assertFalse(block.permitsNativeUseDispatch(token, true, false, false, 34L));
        assertFalse(block.permitsNativeUseDispatch(token, false, false, false, 33L));
        assertTrue(block.permitsNativeUseDispatch(token, true, false, false, 33L));
        assertTrue(block.cancelPendingNativeUse(token));
        assertFalse(block.permitsNativeUseDispatch(token, true, false, false, 33L));
    }

    @Test void guiRightClickAndLivePolicyChangesVetoOriginalReblock() {
        final Minecraft189AutoBlockModule block = new Minecraft189AutoBlockModule();
        block.onEnable();
        long token = block.beginNativeUse(44L);
        assertFalse(block.permitsNativeUseDispatch(token, true, true, false, 44L));
        assertFalse(block.permitsNativeUseDispatch(token, true, false, true, 44L));
        block.holdTicksSetting().set(7);
        assertFalse(block.permitsNativeUseDispatch(token, true, false, false, 44L));
        block.cancelPendingNativeUse(token);
        token = block.beginNativeUse(44L);
        block.pauseManualRightSetting().set(false);
        assertFalse(block.permitsNativeUseDispatch(token, true, false, false, 44L));
    }

    @Test void disableReenableAndStaleTokenCannotBorrowNewUseReservation() {
        final Minecraft189AutoBlockModule block = new Minecraft189AutoBlockModule();
        block.onEnable();
        final long stale = block.beginNativeUse(7L);
        block.onDisable();
        assertFalse(block.permitsNativeUseDispatch(stale, true, false, false, 7L));
        block.onEnable();
        final long fresh = block.beginNativeUse(7L);
        assertTrue(fresh != stale);
        assertFalse(block.permitsNativeUseDispatch(stale, true, false, false, 7L));
        assertTrue(block.permitsNativeUseDispatch(fresh, true, false, false, 7L));
        assertFalse(block.cancelPendingNativeUse(stale));
        assertTrue(block.permitsNativeUseDispatch(fresh, true, false, false, 7L));
    }

    @Test void legacySourceIndependentReservationCannotAuthorizeNativeUse() {
        final Minecraft189AutoBlockModule block = new Minecraft189AutoBlockModule();
        block.onEnable();
        final long testOnly = block.beginNativeUse();
        assertFalse(block.permitsNativeUseDispatch(testOnly, true, false, false, 0L));
        assertTrue(block.completeNativeUse(testOnly, true, false, false));
    }

    @Test void manualRightClickStillTakesOverWithoutSyntheticRelease() {
        final Minecraft189AutoBlockModule block = new Minecraft189AutoBlockModule();
        block.onEnable();
        final long token = block.beginNativeUse(9L);
        assertTrue(block.permitsNativeUseDispatch(token, true, false, false, 9L));
        assertTrue(block.completeNativeUse(token, true, false, false, 9L));
        assertFalse(block.shouldStop(false, true, true, true));
        assertFalse(block.ownsBlock());
        assertFalse(block.releaseForAction(true));
    }
}
