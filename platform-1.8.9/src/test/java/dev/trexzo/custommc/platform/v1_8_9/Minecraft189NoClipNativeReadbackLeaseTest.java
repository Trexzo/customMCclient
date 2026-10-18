package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Real Java mapped noClip interface reentrancy, not original Mojang runtime. */
final class Minecraft189NoClipNativeReadbackLeaseTest {
    private static final class Player implements Minecraft189PlayerNoClipControl {
        boolean noClip;
        int reads, writes;
        Runnable onRead, onWrite;
        @Override public boolean customMcNoClip() {
            reads++;
            final boolean prior = noClip;
            if (onRead != null) {
                final Runnable a = onRead;
                onRead = null;
                a.run();
            }
            return prior;
        }
        @Override public void customMcSetNoClip(boolean value) {
            writes++;
            noClip = value;
            if (onWrite != null) {
                final Runnable a = onWrite;
                onWrite = null;
                a.run();
            }
        }
    }

    private static Minecraft189PlayerMovementState.Snapshot moving(boolean sneak) {
        final Minecraft189PlayerMovementState state =
                new Minecraft189PlayerMovementState();
        state.update(false, sneak, false);
        return state.snapshot();
    }

    @Test void firstNativeGetterDisableCannotCaptureOrForceOldPlayer() {
        final Minecraft189NoClipModule m = new Minecraft189NoClipModule();
        final Player p = new Player();
        m.onEnable();
        p.onRead = m::onDisable;
        m.apply(p);
        assertEquals(1, p.reads);
        assertEquals(0, p.writes);
        assertFalse(p.noClip);
        assertFalse(m.active());
        assertFalse(m.restorePending());
        m.apply(p);
        assertEquals(0, p.writes);
    }

    @Test void firstNativeGetterOffOnNeverInheritsOldFalseBaseline() {
        final Minecraft189NoClipModule m = new Minecraft189NoClipModule();
        final Player p = new Player();
        m.onEnable();
        p.onRead = () -> { m.onDisable(); m.onEnable(); };
        m.apply(p);
        assertEquals(0, p.writes);
        m.apply(p);
        assertTrue(p.noClip);
        assertEquals(1, p.writes);
        m.onDisable();
        m.apply(p);
        assertFalse(p.noClip);
        assertEquals(2, p.writes);
    }

    @Test void secondNativeGetterDisableDoesNotForceAfterOwnerRevoked() {
        final Minecraft189NoClipModule m = new Minecraft189NoClipModule();
        final Player p = new Player();
        m.onEnable();
        // Own baseline true; current player was toggled false by others.
        p.noClip = true;
        m.apply(p);
        assertEquals(0, p.writes);
        p.noClip = false;
        p.onRead = m::onDisable;
        m.apply(p);
        assertEquals(0, p.writes);
        assertTrue(m.restorePending());
        m.apply(p);
        assertFalse(p.noClip);
        assertEquals(0, p.writes);
    }

    @Test void secondOriginalReadSneakPolicyChangeRevokesForceTrue() {
        final Minecraft189NoClipModule m = new Minecraft189NoClipModule();
        final Player p = new Player();
        m.onEnable();
        m.apply(p);
        assertTrue(p.noClip);
        // A true originally forced value is manually changed to false;
        // current GUI policy becomes require Sneak mid real getter.
        p.noClip = false;
        p.onRead = () -> m.requireSneakingSetting().set(Boolean.TRUE);
        m.apply(p,moving(false));
        assertFalse(p.noClip);
        assertEquals(1, p.writes);
        m.apply(p,moving(false));
        assertFalse(p.noClip);
        assertEquals(1, p.writes);
        m.onDisable();
    }

    @Test void restoreGetterReenabledModuleCannotClearNewActivationLease() {
        final Minecraft189NoClipModule m = new Minecraft189NoClipModule();
        final Player p = new Player();
        m.onEnable();
        m.apply(p);
        assertTrue(p.noClip);
        m.onDisable();
        assertTrue(m.restorePending());
        p.onRead = m::onEnable;
        m.apply(p); // Original read reenters ON, so old OFF cannot restore.
        assertTrue(m.active());
        assertTrue(p.noClip);
        assertEquals(1, p.writes);
        m.apply(p); // New genuine enabled source owns original baseline.
        assertEquals(1, p.writes);
        m.onDisable();
        m.apply(p);
        assertFalse(p.noClip);
        assertEquals(2, p.writes);
    }

    @Test void repeatedDisableDuringRestoreKeepsPendingOriginalOwner() {
        final Minecraft189NoClipModule m = new Minecraft189NoClipModule();
        final Player p = new Player();
        m.onEnable();
        m.apply(p);
        m.onDisable();
        p.onRead = m::onDisable;
        m.apply(p);
        assertTrue(m.restorePending());
        assertTrue(p.noClip);
        m.apply(p);
        assertFalse(m.restorePending());
        assertFalse(p.noClip);
    }

    @Test void nativeSetTrueThenDisableStillRestoresOnNextRealTick() {
        final Minecraft189NoClipModule m = new Minecraft189NoClipModule();
        final Player p = new Player();
        m.onEnable();
        p.onWrite = m::onDisable;
        m.apply(p);
        assertTrue(p.noClip);
        assertTrue(m.restorePending());
        m.apply(p);
        assertFalse(p.noClip);
        assertEquals(2, p.writes);
    }

    @Test void actualGuiFocusRestoresOriginalFalseAndFreshEnableUsesSamePlayer() {
        final Minecraft189NoClipModule m = new Minecraft189NoClipModule();
        final Player p = new Player();
        m.onEnable();
        m.apply(p);
        m.apply(p,moving(true),true);
        assertFalse(p.noClip);
        assertEquals(2, p.writes);
        m.apply(p,moving(true),true);
        assertEquals(2, p.writes);
        m.apply(p,moving(true),false);
        assertTrue(p.noClip);
        assertEquals(3, p.writes);
        m.onDisable();
        m.apply(p);
        assertFalse(p.noClip);
    }

    @Test void originalTruePlayerBaselineIsNeverForcedBackAfterUserEdit() {
        final Minecraft189NoClipModule m = new Minecraft189NoClipModule();
        final Player p = new Player();
        p.noClip = true;
        m.onEnable();
        m.apply(p);
        assertEquals(0, p.writes);
        p.noClip = false; // User/source owner explicitly changed true.
        m.onDisable();
        m.apply(p);
        assertFalse(p.noClip);
        assertEquals(0, p.writes);
    }

    @Test void respawnWithNativeTrueKeepsNewPlayersIndependentOwnership() {
        final Minecraft189NoClipModule m = new Minecraft189NoClipModule();
        final Player prior = new Player();
        final Player fresh = new Player();
        fresh.noClip = true;
        m.onEnable();
        m.apply(prior);
        assertTrue(prior.noClip);
        m.onDisable();
        m.apply(fresh);
        assertEquals(0, fresh.writes);
        m.onEnable();
        m.apply(fresh);
        m.onDisable();
        m.apply(fresh);
        assertTrue(fresh.noClip);
        assertEquals(0, fresh.writes);
    }
}
