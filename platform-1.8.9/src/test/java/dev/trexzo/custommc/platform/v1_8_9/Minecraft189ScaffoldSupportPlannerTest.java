package dev.trexzo.custommc.platform.v1_8_9;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189ScaffoldSupportPlannerTest {
    private static final class World implements Minecraft189ScaffoldSupportPlanner.WorldEvidence {
        long generation = 17;
        boolean unloaded;
        boolean throwsOnRead;
        final Map<String, Minecraft189ScaffoldSupportPlanner.Cell> cells =
                new HashMap<String, Minecraft189ScaffoldSupportPlanner.Cell>();
        public long generation() { return generation; }
        public Minecraft189ScaffoldSupportPlanner.Cell cell(int x, int y, int z) {
            if (throwsOnRead) throw new IllegalStateException("unloaded original world");
            return unloaded ? Minecraft189ScaffoldSupportPlanner.Cell.UNKNOWN
                    : cells.getOrDefault(x + ":" + y + ":" + z,
                            Minecraft189ScaffoldSupportPlanner.Cell.UNKNOWN);
        }
        World set(int x, int y, int z, Minecraft189ScaffoldSupportPlanner.Cell cell) {
            cells.put(x + ":" + y + ":" + z, cell);
            return this;
        }
    }

    private static Minecraft189ScaffoldSupportPlanner.Candidate choose(World world) {
        return Minecraft189ScaffoldSupportPlanner.choose(world, 17, true, true,
                4.5D, 65.0D, 7.5D, 66.62D, true, 16, 4.5D);
    }

    @Test void exactNativeBelowSupportReturnsUpFaceWithPositiveWorldCoordinates() {
        World w = new World()
                .set(4, 64, 7, Minecraft189ScaffoldSupportPlanner.Cell.REPLACEABLE)
                .set(4, 63, 7, Minecraft189ScaffoldSupportPlanner.Cell.FULL_SOLID);
        Minecraft189ScaffoldSupportPlanner.Candidate c = choose(w);
        assertNotNull(c);
        assertEquals(17, c.generation);
        assertEquals(4, c.targetX);
        assertEquals(64, c.targetY);
        assertEquals(7, c.targetZ);
        assertEquals(4, c.supportX);
        assertEquals(63, c.supportY);
        assertEquals(7, c.supportZ);
        assertEquals(Minecraft189ScaffoldSupportPlanner.Face.UP, c.face);
        assertEquals(4.5D, c.hitX);
        assertEquals(64.0D, c.hitY);
        assertEquals(7.5D, c.hitZ);
    }

    @Test void eastFaceMatchesWestNativeSupportWithoutInventedSolidBlock() {
        World w = new World()
                .set(4, 64, 7, Minecraft189ScaffoldSupportPlanner.Cell.REPLACEABLE)
                .set(3, 64, 7, Minecraft189ScaffoldSupportPlanner.Cell.FULL_SOLID);
        Minecraft189ScaffoldSupportPlanner.Candidate c = choose(w);
        assertNotNull(c);
        assertEquals(Minecraft189ScaffoldSupportPlanner.Face.EAST, c.face);
        assertEquals(3, c.supportX);
        assertEquals(4.0D, c.hitX);
        assertEquals(64.5D, c.hitY);
        assertEquals(7.5D, c.hitZ);
        w.set(3, 64, 7, Minecraft189ScaffoldSupportPlanner.Cell.OTHER);
        assertNull(choose(w), "slab/fluid/non-full-block must not become a solid support");
    }

    @Test void negativeCoordinatesAreFlooredRatherThanTruncatedTowardZero() {
        World w = new World()
                .set(-1, 64, -2, Minecraft189ScaffoldSupportPlanner.Cell.REPLACEABLE)
                .set(-1, 63, -2, Minecraft189ScaffoldSupportPlanner.Cell.FULL_SOLID);
        Minecraft189ScaffoldSupportPlanner.Candidate c =
                Minecraft189ScaffoldSupportPlanner.choose(w,17,true,true,
                        -0.25D,65.0D,-1.25D,66.62D,true,3,4.5D);
        assertNotNull(c);
        assertEquals(-1, c.targetX);
        assertEquals(-2, c.targetZ);
    }

    @Test void noFullSolidNeighbourMeansNoPlacementAuthority() {
        World w = new World()
                .set(4, 64, 7, Minecraft189ScaffoldSupportPlanner.Cell.REPLACEABLE);
        assertNull(choose(w), "air-only empty space has no placement support");
        w.set(4,63,7,Minecraft189ScaffoldSupportPlanner.Cell.OTHER);
        assertNull(choose(w), "non-full blocks are not accepted as supported yet");
        w.set(4,64,7,Minecraft189ScaffoldSupportPlanner.Cell.FULL_SOLID)
                .set(4,63,7,Minecraft189ScaffoldSupportPlanner.Cell.FULL_SOLID);
        assertNull(choose(w), "occupied target must not be replaced");
    }

    @Test void unloadedStaleOrFailedNativeReadsFailClosed() {
        World w = new World()
                .set(4,64,7,Minecraft189ScaffoldSupportPlanner.Cell.REPLACEABLE)
                .set(4,63,7,Minecraft189ScaffoldSupportPlanner.Cell.FULL_SOLID);
        w.generation = 18;
        assertNull(choose(w), "world generation mismatch must reject");
        w.generation = 17;
        w.unloaded = true;
        assertNull(choose(w), "unloaded chunk must reject");
        w.unloaded = false;
        w.throwsOnRead = true;
        assertNull(choose(w), "world unload exception must reject");
        assertNull(choose(null), "world absent must reject");
    }

    @Test void unknownItemAirborneRemoteAndOutOfRangeNeverProduceCandidates() {
        World w = new World()
                .set(4,64,7,Minecraft189ScaffoldSupportPlanner.Cell.REPLACEABLE)
                .set(4,63,7,Minecraft189ScaffoldSupportPlanner.Cell.FULL_SOLID);
        assertNull(Minecraft189ScaffoldSupportPlanner.choose(w,17,false,true,
                4.5D,65,7.5D,66.62D,true,16,4.5D), "remote player");
        assertNull(Minecraft189ScaffoldSupportPlanner.choose(w,17,true,false,
                4.5D,65,7.5D,66.62D,true,16,4.5D), "unsupported tower/airborne");
        assertNull(Minecraft189ScaffoldSupportPlanner.choose(w,17,true,true,
                4.5D,65,7.5D,66.62D,false,16,4.5D), "unknown ItemBlock identity");
        assertNull(Minecraft189ScaffoldSupportPlanner.choose(w,17,true,true,
                4.5D,65,7.5D,66.62D,true,0,4.5D), "empty stack");
        assertNull(Minecraft189ScaffoldSupportPlanner.choose(w,17,true,true,
                4.5D,65,7.5D,66.62D,true,16,0.5D), "native reach too short");
        assertNull(Minecraft189ScaffoldSupportPlanner.choose(w,17,true,true,
                Double.NaN,65,7.5D,66.62D,true,16,4.5D), "non-finite position");
        assertNull(Minecraft189ScaffoldSupportPlanner.choose(w,17,true,true,
                4.5D,65,7.5D,Double.POSITIVE_INFINITY,true,16,4.5D), "invalid eye position");
    }

    @Test void fixedDeterministicOrderNeverChoosesUnknownBeforeRealFullSupport() {
        World w = new World()
                .set(4,64,7,Minecraft189ScaffoldSupportPlanner.Cell.REPLACEABLE)
                .set(5,64,7,Minecraft189ScaffoldSupportPlanner.Cell.FULL_SOLID)
                .set(4,64,6,Minecraft189ScaffoldSupportPlanner.Cell.FULL_SOLID);
        Minecraft189ScaffoldSupportPlanner.Candidate c=choose(w);
        assertNotNull(c);
        assertEquals(Minecraft189ScaffoldSupportPlanner.Face.WEST,c.face);
        assertEquals(5,c.supportX);
    }
}
