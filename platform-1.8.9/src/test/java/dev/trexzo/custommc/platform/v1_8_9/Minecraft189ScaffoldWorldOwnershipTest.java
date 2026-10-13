package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189ScaffoldWorldOwnershipTest {
    private static final class NativeWorld
            implements Minecraft189ScaffoldWorldReadAccess {
        final int cell;
        int reads;
        NativeWorld(int code) { cell = code; }
        public int customMcScaffoldCellCode(int x, int y, int z) {
            reads++;
            return cell;
        }
    }

    @Test void identityPreservesEpochAcrossTicksAndRevokesOldWorld() {
        Minecraft189ScaffoldWorldOwnership owner =
                new Minecraft189ScaffoldWorldOwnership();
        assertNull(owner.current());
        assertEquals(-1L, owner.generation());
        assertFalse(owner.owns(new Object()));
        NativeWorld first = new NativeWorld(Minecraft189ScaffoldWorldReadAccess.AIR);
        owner.observe(first);
        Minecraft189ScaffoldNativeWorldEvidence firstEvidence = owner.current();
        assertNotNull(firstEvidence);
        assertEquals(1L, owner.generation());
        assertTrue(owner.owns(first));
        assertEquals(Minecraft189ScaffoldSupportPlanner.Cell.REPLACEABLE,
                firstEvidence.cell(1, 99, 0));
        owner.observe(first);
        assertSame(firstEvidence, owner.current(), "same source tick cannot mint a new epoch");
        assertEquals(1L, owner.generation());

        NativeWorld second = new NativeWorld(
                Minecraft189ScaffoldWorldReadAccess.FULL_SOLID);
        owner.observe(second);
        assertTrue(owner.owns(second));
        assertFalse(owner.owns(first));
        assertEquals(2L, owner.generation());
        assertEquals(-1L, firstEvidence.generation(),
                "prior world evidence must be invalid immediately");
        int oldReads = first.reads;
        assertEquals(Minecraft189ScaffoldSupportPlanner.Cell.UNKNOWN,
                firstEvidence.cell(1, 99, 0));
        assertEquals(oldReads, first.reads, "revoked old source cannot be read");
        assertEquals(Minecraft189ScaffoldSupportPlanner.Cell.FULL_SOLID,
                owner.current().cell(1, 99, 0));
        owner.close();
    }

    @Test void disconnectAndReconnectAlwaysMintDistinctEpochs() {
        Minecraft189ScaffoldWorldOwnership owner =
                new Minecraft189ScaffoldWorldOwnership();
        NativeWorld first = new NativeWorld(1);
        owner.observe(first);
        Minecraft189ScaffoldNativeWorldEvidence original = owner.current();
        owner.observe(null);
        assertNull(owner.current());
        assertEquals(-1L, owner.generation());
        assertFalse(owner.owns(first));
        assertEquals(-1L, original.generation());
        owner.observe(new Object()); // untransformed source is never authority
        assertNull(owner.current());
        owner.observe(first); // same object after disconnect is a NEW generation
        assertEquals(2L, owner.generation());
        assertNotSame(original, owner.current());
        assertTrue(owner.owns(first));
        owner.close();
        assertEquals(-1L, owner.generation());
        assertNull(owner.current());
        owner.observe(first);
        assertNull(owner.current(), "closed host cannot recapture any world source");
    }

    @Test void nativeReadErrorsStayUnknownWithoutReplacingOwner() {
        Minecraft189ScaffoldWorldOwnership owner =
                new Minecraft189ScaffoldWorldOwnership();
        Minecraft189ScaffoldWorldReadAccess failing =
                new Minecraft189ScaffoldWorldReadAccess() {
                    public int customMcScaffoldCellCode(int x, int y, int z) {
                        throw new IllegalStateException("world invalid");
                    }
                };
        owner.observe(failing);
        assertTrue(owner.owns(failing));
        assertEquals(Minecraft189ScaffoldSupportPlanner.Cell.UNKNOWN,
                owner.current().cell(1, 99, 0));
        owner.close();
        assertFalse(owner.owns(failing));
    }
}
