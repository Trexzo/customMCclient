package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189ScaffoldWorldLeaseTest {
    private static final class NativeWorld implements Minecraft189ScaffoldWorldReadAccess {
        int reads;
        boolean unavailable;
        @Override public int customMcScaffoldCellCode(int x, int y, int z) {
            reads++;
            if (unavailable) throw new IllegalStateException("native world detached");
            if (x == 4 && y == 64 && z == 7) return AIR;
            if (x == 4 && y == 63 && z == 7) return FULL_SOLID;
            return UNKNOWN;
        }
    }

    private static Minecraft189ScaffoldSupportPlanner.Candidate choose(
            Minecraft189ScaffoldWorldLease lease, NativeWorld source) {
        return lease.choose(source,true,true,4.5,65,7.5,66.62,true,16,4.5);
    }

    @Test void sourceOriginalWorldCanProposeOnlyDuringItsFreshOwnedLease() {
        NativeWorld world = new NativeWorld();
        Minecraft189ScaffoldWorldLease lease = new Minecraft189ScaffoldWorldLease();
        assertNull(choose(lease,world));
        Minecraft189ScaffoldSupportPlanner.WorldEvidence first = lease.open(world);
        assertNotNull(first);
        assertEquals(1L,first.generation());
        Minecraft189ScaffoldSupportPlanner.Candidate candidate = choose(lease,world);
        assertNotNull(candidate);
        assertEquals(first.generation(),candidate.generation);
        assertEquals(Minecraft189ScaffoldSupportPlanner.Face.UP,candidate.face);
        assertEquals(4,candidate.targetX);
        assertEquals(64,candidate.targetY);
        assertEquals(7,candidate.targetZ);
        lease.invalidate();
        assertEquals(-1L,first.generation());
        int before=world.reads;
        assertNull(choose(lease,world));
        assertEquals(before,world.reads,"ended callback cannot read source World");
    }

    @Test void replacementWorldCannotBorrowPriorNativeEvidenceOrEpoch() {
        NativeWorld one = new NativeWorld(), two = new NativeWorld();
        Minecraft189ScaffoldWorldLease lease = new Minecraft189ScaffoldWorldLease();
        Minecraft189ScaffoldSupportPlanner.WorldEvidence old = lease.open(one);
        assertNotNull(choose(lease,one));
        assertNull(choose(lease,two));
        assertEquals(0,two.reads,"wrong original World reference must not be accessed");
        Minecraft189ScaffoldSupportPlanner.WorldEvidence fresh = lease.open(two);
        assertEquals(-1L,old.generation());
        assertEquals(2L,fresh.generation());
        assertNull(choose(lease,one));
        assertNotNull(choose(lease,two));
        assertEquals(2L,choose(lease,two).generation);
    }

    @Test void disconnectedAndExceptionWorldsAreFailClosed() {
        NativeWorld original = new NativeWorld();
        Minecraft189ScaffoldWorldLease lease = new Minecraft189ScaffoldWorldLease();
        lease.open(original);
        original.unavailable=true;
        assertNull(choose(lease,original),"native unloaded chunk/World exception");
        original.unavailable=false;
        assertNull(lease.choose(original,false,true,4.5,65,7.5,66.62,true,16,4.5));
        assertNull(lease.choose(original,true,false,4.5,65,7.5,66.62,true,16,4.5));
        assertNull(lease.choose(original,true,true,4.5,65,7.5,66.62,false,16,4.5));
        assertNull(lease.choose(original,true,true,4.5,65,7.5,66.62,true,0,4.5));
        lease.close();
        assertNull(choose(lease,original));
        assertNull(lease.open(original));
        lease.invalidate();
        assertNull(choose(lease,original));
    }

    @Test void nullWorldNeverCreatesAuthority() {
        Minecraft189ScaffoldWorldLease lease = new Minecraft189ScaffoldWorldLease();
        assertNull(lease.open(null));
        assertNull(lease.choose(null,true,true,4.5,65,7.5,66.62,true,16,4.5));
    }
}
