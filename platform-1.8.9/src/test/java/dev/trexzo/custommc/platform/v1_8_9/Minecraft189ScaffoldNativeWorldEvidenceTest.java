package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189ScaffoldNativeWorldEvidenceTest {
    private static final class Source implements Minecraft189ScaffoldWorldReadAccess {
        int calls;
        boolean unloaded;
        boolean throwsOnRead;
        public int customMcScaffoldCellCode(int x, int y, int z) {
            calls++;
            if (throwsOnRead) throw new IllegalStateException("world closed");
            if (unloaded) return UNKNOWN;
            if (x == 4 && y == 64 && z == 7) return AIR;
            if (x == 4 && y == 63 && z == 7) return FULL_SOLID;
            if (x == 3 && y == 64 && z == 7) return OTHER;
            return UNKNOWN;
        }
    }
    @Test void exactMappedNativeCellsYieldCandidateWithoutWritingWorld() {
        Source source = new Source();
        Minecraft189ScaffoldNativeWorldEvidence world =
                new Minecraft189ScaffoldNativeWorldEvidence(source, 61);
        assertEquals(61, world.generation());
        assertEquals(Minecraft189ScaffoldSupportPlanner.Cell.REPLACEABLE,world.cell(4,64,7));
        assertEquals(Minecraft189ScaffoldSupportPlanner.Cell.FULL_SOLID,world.cell(4,63,7));
        assertEquals(Minecraft189ScaffoldSupportPlanner.Cell.OTHER,world.cell(3,64,7));
        assertEquals(Minecraft189ScaffoldSupportPlanner.Cell.UNKNOWN,world.cell(10,64,7));
        assertNotNull(Minecraft189ScaffoldSupportPlanner.choose(world, 61, true, true,
                4.5,65.0,7.5,66.62,true,16,4.5));
        assertTrue(source.calls > 0);
        world.close();
        int prior=source.calls;
        assertEquals(-1, world.generation());
        assertEquals(Minecraft189ScaffoldSupportPlanner.Cell.UNKNOWN,world.cell(4,64,7));
        assertEquals(prior,source.calls,"closed world must not call native World");
        assertNull(Minecraft189ScaffoldSupportPlanner.choose(world,61,true,true,
                4.5,65,7.5,66.62,true,16,4.5));
    }
    @Test void unavailableOrStaleNativeWorldFailsClosed() {
        Source source = new Source();
        Minecraft189ScaffoldNativeWorldEvidence world =
                new Minecraft189ScaffoldNativeWorldEvidence(source, 61);
        source.unloaded = true;
        assertNull(Minecraft189ScaffoldSupportPlanner.choose(world,61,true,true,
                4.5,65,7.5,66.62,true,16,4.5));
        source.unloaded = false;
        source.throwsOnRead = true;
        assertNull(Minecraft189ScaffoldSupportPlanner.choose(world,61,true,true,
                4.5,65,7.5,66.62,true,16,4.5));
        source.throwsOnRead = false;
        assertNull(Minecraft189ScaffoldSupportPlanner.choose(world,62,true,true,
                4.5,65,7.5,66.62,true,16,4.5));
        world.close();
        assertThrows(IllegalArgumentException.class,
                () -> new Minecraft189ScaffoldNativeWorldEvidence(source,0));
        assertThrows(IllegalArgumentException.class,
                () -> new Minecraft189ScaffoldNativeWorldEvidence(null,1));
    }
    @Test void invalidNativeCoordinateNeverInvokesOriginalWorld() {
        Source source = new Source();
        Minecraft189ScaffoldNativeWorldEvidence world =
                new Minecraft189ScaffoldNativeWorldEvidence(source,3);
        assertEquals(Minecraft189ScaffoldSupportPlanner.Cell.UNKNOWN,world.cell(0,-1,0));
        assertEquals(Minecraft189ScaffoldSupportPlanner.Cell.UNKNOWN,world.cell(0,256,0));
        assertEquals(Minecraft189ScaffoldSupportPlanner.Cell.UNKNOWN,world.cell(30000000,64,0));
        assertEquals(0,source.calls);
    }
}
