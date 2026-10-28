package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.ui.clickgui.ClickGuiModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189OverlayFocusGuardianTest {
    @Test
    void realVanillaScreenWinsAndClosesOverlayWithoutChangingNativeState() {
        final ClickGuiModel model = new ClickGuiModel();
        model.open();
        final Minecraft189OverlayFocusGuardian.Snapshot vanilla =
                Minecraft189OverlayFocusGuardian.Snapshot.known(true, false);
        assertEquals(Minecraft189OverlayFocusGuardian.Mode.VANILLA_SCREEN,
                Minecraft189OverlayFocusGuardian.classify(vanilla));
        assertFalse(Minecraft189OverlayFocusGuardian.permitsMouseInput(vanilla));
        assertFalse(Minecraft189OverlayFocusGuardian.reconcile(model, vanilla));
        assertFalse(model.snapshot().open());
    }

    @Test
    void gameplayOwnedCursorMayKeepKeyboardOverlayButCannotEditWithMouse() {
        final ClickGuiModel model = new ClickGuiModel();
        model.open();
        final Minecraft189OverlayFocusGuardian.Snapshot gameplay =
                Minecraft189OverlayFocusGuardian.Snapshot.known(false, true);
        assertEquals(Minecraft189OverlayFocusGuardian.Mode.GAMEPLAY_CAPTURED,
                Minecraft189OverlayFocusGuardian.classify(gameplay));
        assertTrue(Minecraft189OverlayFocusGuardian.reconcile(model, gameplay));
        assertFalse(Minecraft189OverlayFocusGuardian.permitsMouseInput(gameplay));
        assertTrue(model.snapshot().open());
    }

    @Test
    void ungrabbedNativeWithoutVanillaScreenPermitsUiAndNeverMutatesFocus() {
        final ClickGuiModel model = new ClickGuiModel();
        model.open();
        final Minecraft189OverlayFocusGuardian.Snapshot ready =
                Minecraft189OverlayFocusGuardian.Snapshot.known(false, false);
        assertEquals(Minecraft189OverlayFocusGuardian.Mode.GUI_READY,
                Minecraft189OverlayFocusGuardian.classify(ready));
        assertTrue(Minecraft189OverlayFocusGuardian.reconcile(model, ready));
        assertTrue(Minecraft189OverlayFocusGuardian.permitsMouseInput(ready));
        assertFalse(ready.gameplayFocus());
    }

    @Test
    void unavailableOriginalGameStateDoesNotFakeNativeFocusEvidence() {
        final Minecraft189OverlayFocusGuardian.Snapshot unknown =
                Minecraft189OverlayFocusGuardian.Snapshot.unknown();
        assertEquals(Minecraft189OverlayFocusGuardian.Mode.UNKNOWN,
                Minecraft189OverlayFocusGuardian.classify(unknown));
        final ClickGuiModel model = new ClickGuiModel();
        model.open();
        assertTrue(Minecraft189OverlayFocusGuardian.reconcile(model, unknown));
        // The mock-host bridge must still work without class ave present.
        // Production LWJGL additionally checks isCreated/notGrabbed/insideWindow.
        assertTrue(Minecraft189OverlayFocusGuardian.permitsMouseInput(unknown));
        assertTrue(model.snapshot().open());
    }

    @Test
    void closedOverlayRemainsClosedEvenIfNativeFocusChanges() {
        final ClickGuiModel model = new ClickGuiModel();
        assertFalse(Minecraft189OverlayFocusGuardian.reconcile(
                model, Minecraft189OverlayFocusGuardian.Snapshot.known(
                        false, false)));
        assertFalse(model.snapshot().open());
    }

    @Test
    void graphicalMouseGatesRequireOriginalFocusProofAndPhysicalCursor() {
        final Minecraft189OverlayFocusGuardian.Snapshot unknown =
                Minecraft189OverlayFocusGuardian.Snapshot.unknown();
        final Minecraft189OverlayFocusGuardian.Snapshot ready =
                Minecraft189OverlayFocusGuardian.Snapshot.known(false, false);
        final Minecraft189OverlayFocusGuardian.Snapshot gameplay =
                Minecraft189OverlayFocusGuardian.Snapshot.known(false, true);
        assertFalse(Minecraft189OverlayFocusGuardian.permitsPhysicalMouse(
                unknown, true, true, false, true));
        assertFalse(Minecraft189OverlayFocusGuardian.permitsPhysicalMouse(
                ready, true, false, false, true));
        assertFalse(Minecraft189OverlayFocusGuardian.permitsPhysicalMouse(
                ready, true, true, true, true));
        assertFalse(Minecraft189OverlayFocusGuardian.permitsPhysicalMouse(
                ready, true, true, false, false));
        assertFalse(Minecraft189OverlayFocusGuardian.permitsPhysicalMouse(
                gameplay, true, true, false, true));
        assertTrue(Minecraft189OverlayFocusGuardian.permitsPhysicalMouse(
                ready, true, true, false, true));
        // Mock-host tests have no original LWJGL Display and still
        // exercise the UI input contract without fabricating game focus.
        assertTrue(Minecraft189OverlayFocusGuardian.permitsPhysicalMouse(
                unknown, false, false, false, false));
    }

    @Test
    void neverAcceptsArbitraryObjectAsOriginalNativeTickInstance() {
        Minecraft189OverlayFocusGuardian.clearOriginalTickInstance();
        assertFalse(Minecraft189OverlayFocusGuardian.observeOriginalTickInstance(
                new Object()));
        assertFalse(Minecraft189OverlayFocusGuardian.observeOriginalTickInstance(
                null));
        assertFalse(Minecraft189OverlayFocusGuardian.observeNative().available());
        assertEquals(null,
                Minecraft189OverlayFocusGuardian.findOriginalMinecraftInstance());
        Minecraft189OverlayFocusGuardian.clearOriginalTickInstance();
    }

    @Test
    void unavailableOriginalClassNeverGrantsGuiFocusFromMouseAlone() {
        // Mock JVMs do not load the original Mojang class ave, even if
        // later code supplies a synthetic physical mouse state.
        final Minecraft189OverlayFocusGuardian.Snapshot source =
                Minecraft189OverlayFocusGuardian.observeNative();
        assertFalse(source.available());
        assertEquals(Minecraft189OverlayFocusGuardian.Mode.UNKNOWN,
                Minecraft189OverlayFocusGuardian.classify(source));
        assertFalse(Minecraft189OverlayFocusGuardian.permitsPhysicalMouse(
                source, true, true, false, true));
        assertEquals(null,
                Minecraft189OverlayFocusGuardian.findOriginalMinecraftInstance());
    }

    @Test
    void pinnedOriginalScreenAndFocusMappingsAreExactAndReadOnly() {
        assertEquals("ave",
                Minecraft189Mappings.MINECRAFT.obfuscatedInternalName());
        assertEquals("m",
                Minecraft189Mappings.MINECRAFT_CURRENT_SCREEN.obfuscatedName());
        assertEquals("w",
                Minecraft189Mappings.MINECRAFT_INGAME_HAS_FOCUS.obfuscatedName());
        assertEquals("Laxu;",
                Minecraft189Mappings.MINECRAFT_CURRENT_SCREEN.descriptor());
        assertEquals("Z",
                Minecraft189Mappings.MINECRAFT_INGAME_HAS_FOCUS.descriptor());
    }
}
