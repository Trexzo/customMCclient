package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189ClickGuiNativeFocusLeaseTest {
    private static final Minecraft189OverlayFocusGuardian.Snapshot GAMEPLAY =
            Minecraft189OverlayFocusGuardian.Snapshot.known(false, true);
    private static final Minecraft189OverlayFocusGuardian.Snapshot READY =
            Minecraft189OverlayFocusGuardian.Snapshot.known(false, false);
    private static final Minecraft189OverlayFocusGuardian.Snapshot VANILLA =
            Minecraft189OverlayFocusGuardian.Snapshot.known(true, false);

    @Test
    void exactPinnedVanillaMethodsAreSeparateFromClickActions() {
        assertEquals("ave", Minecraft189Mappings.MINECRAFT.obfuscatedInternalName());
        assertEquals("o", Minecraft189Mappings
                .MINECRAFT_SET_INGAME_NOT_IN_FOCUS.obfuscatedName());
        assertEquals("func_71364_i", Minecraft189Mappings
                .MINECRAFT_SET_INGAME_NOT_IN_FOCUS.seargeName());
        assertEquals("()V", Minecraft189Mappings
                .MINECRAFT_SET_INGAME_NOT_IN_FOCUS.descriptor());
        assertEquals("n", Minecraft189Mappings
                .MINECRAFT_SET_INGAME_FOCUS.obfuscatedName());
        assertEquals("func_71381_h", Minecraft189Mappings
                .MINECRAFT_SET_INGAME_FOCUS.seargeName());
        assertEquals("()V", Minecraft189Mappings
                .MINECRAFT_SET_INGAME_FOCUS.descriptor());
    }

    @Test
    void onlyActualGameplayGrabCanRequestOriginalVanillaRelease() {
        assertEquals(Minecraft189ClickGuiNativeFocusLease.Decision.RELEASE_ORIGINAL,
                action(false, true, true, GAMEPLAY, true, true, false));
        assertEquals(Minecraft189ClickGuiNativeFocusLease.Decision.REJECT_OPEN,
                action(false, true, true, GAMEPLAY, true, false, false));
        assertEquals(Minecraft189ClickGuiNativeFocusLease.Decision.REJECT_OPEN,
                action(false, true, true, GAMEPLAY, false, false, false));
        assertEquals(Minecraft189ClickGuiNativeFocusLease.Decision.REJECT_OPEN,
                action(false, true, true, VANILLA, true, false, false));
        assertEquals(Minecraft189ClickGuiNativeFocusLease.Decision.REJECT_OPEN,
                action(false, true, true,
                        Minecraft189OverlayFocusGuardian.Snapshot.unknown(),
                        true, false, false));
    }

    @Test
    void readyMouseDoesNotGetInventedReleaseOrRegrab() {
        assertEquals(Minecraft189ClickGuiNativeFocusLease.Decision.NONE,
                action(false, true, true, READY, true, false, false));
        assertEquals(Minecraft189ClickGuiNativeFocusLease.Decision.REJECT_OPEN,
                action(false, true, true, READY, true, true, false));
        assertEquals(Minecraft189ClickGuiNativeFocusLease.Decision.NONE,
                action(false, true, false, GAMEPLAY, true, true, false));
    }

    @Test
    void restoreOnlyOwnedReleaseAndNeverDismissNativeVanillaScreen() {
        assertEquals(Minecraft189ClickGuiNativeFocusLease.Decision.RESTORE_ORIGINAL,
                action(true, false, true, READY, true, false, true));
        assertEquals(Minecraft189ClickGuiNativeFocusLease.Decision.NONE,
                action(true, false, true, READY, true, false, false));
        assertEquals(Minecraft189ClickGuiNativeFocusLease.Decision.NONE,
                action(true, false, true, VANILLA, true, false, true));
        assertEquals(Minecraft189ClickGuiNativeFocusLease.Decision.NONE,
                action(true, true, true, GAMEPLAY, true, true, true));
    }

    @Test
    void originalMouseSafetyGuardStillRejectsCapturedAndUnknownState() {
        assertFalse(Minecraft189OverlayFocusGuardian.permitsPhysicalMouse(
                GAMEPLAY, true, true, true, true));
        assertFalse(Minecraft189OverlayFocusGuardian.permitsPhysicalMouse(
                Minecraft189OverlayFocusGuardian.Snapshot.unknown(),
                true, true, false, true));
        assertTrue(Minecraft189OverlayFocusGuardian.permitsPhysicalMouse(
                READY, true, true, false, true));
    }

    private static Minecraft189ClickGuiNativeFocusLease.Decision action(
            boolean wasOpen, boolean nowOpen, boolean displayCreated,
            Minecraft189OverlayFocusGuardian.Snapshot snapshot,
            boolean mouseCreated, boolean grabbed, boolean ownsRelease) {
        return Minecraft189ClickGuiNativeFocusLease.decide(
                wasOpen, nowOpen, displayCreated, snapshot,
                mouseCreated, grabbed, ownsRelease);
    }
}
