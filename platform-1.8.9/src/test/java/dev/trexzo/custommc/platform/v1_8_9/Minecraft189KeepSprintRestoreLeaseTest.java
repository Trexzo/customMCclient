package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Original-player one-shot KeepSprint lease, no synthetic source grants. */
final class Minecraft189KeepSprintRestoreLeaseTest {
    private static final class Player implements Minecraft189PlayerSprintControl {
        int writes;
        @Override public void customMcSetSprinting(boolean sprint) { writes++; }
    }

    @Test void oneOriginalPlayerCanCompleteOneRestoreScope() {
        final Minecraft189KeepSprintRestoreLease lease =
                new Minecraft189KeepSprintRestoreLease();
        final Player p = new Player();
        assertFalse(lease.enter(null));
        assertTrue(lease.enter(p));
        assertTrue(lease.stillOwned(p));
        lease.exit();
        assertFalse(lease.stillOwned(p));
        assertTrue(lease.enter(p));
        assertTrue(lease.stillOwned(p));
        lease.exit();
    }

    @Test void nestedOriginalGetterRevokesEvenSamePlayerOwner() {
        final Minecraft189KeepSprintRestoreLease lease =
                new Minecraft189KeepSprintRestoreLease();
        final Player p = new Player();
        assertTrue(lease.enter(p));
        assertFalse(lease.enter(p));
        assertFalse(lease.stillOwned(p));
        lease.exit();
        assertTrue(lease.enter(p));
        assertTrue(lease.stillOwned(p));
        lease.exit();
    }

    @Test void differentNativePlayerCannotImpersonateCapturedRestore() {
        final Minecraft189KeepSprintRestoreLease lease =
                new Minecraft189KeepSprintRestoreLease();
        final Player a = new Player(), b = new Player();
        assertTrue(lease.enter(a));
        assertFalse(lease.stillOwned(b));
        assertFalse(lease.enter(b));
        assertFalse(lease.stillOwned(a));
        lease.exit();
    }

    @Test void guiAndWorldRevocationRemainStickyUntilExit() {
        final Minecraft189KeepSprintRestoreLease lease =
                new Minecraft189KeepSprintRestoreLease();
        final Player p = new Player();
        assertTrue(lease.enter(p));
        lease.revoke();
        assertFalse(lease.stillOwned(p));
        assertFalse(lease.enter(p));
        lease.exit();
        assertTrue(lease.enter(p));
        assertTrue(lease.stillOwned(p));
        lease.exit();
    }
}
