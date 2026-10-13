package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189ScaffoldNativeFaceGateTest {
    private static final class World
            implements Minecraft189ScaffoldSupportPlanner.WorldEvidence {
        public long generation() { return 12; }
        public Minecraft189ScaffoldSupportPlanner.Cell cell(int x,int y,int z) {
            if (x==0 && y==99 && z==0)
                return Minecraft189ScaffoldSupportPlanner.Cell.REPLACEABLE;
            if (x==0 && y==98 && z==0)
                return Minecraft189ScaffoldSupportPlanner.Cell.FULL_SOLID;
            return Minecraft189ScaffoldSupportPlanner.Cell.UNKNOWN;
        }
    }
    private static final class Hit implements Minecraft189ScaffoldNativeBlockHitAccess {
        double[] values;
        boolean throwsRead;
        Hit(double... values) { this.values=values; }
        @Override public double[] customMcScaffoldNativeBlockHit() {
            if (throwsRead) throw new IllegalStateException("native cursor unavailable");
            return values;
        }
    }
    private static Minecraft189ScaffoldSupportPlanner.Candidate candidate() {
        final Minecraft189ScaffoldSupportPlanner.Candidate c =
                Minecraft189ScaffoldSupportPlanner.choose(
                        new World(),12,true,true,0.5,100,0.5,101.62,
                        true,8,4.5);
        assertNotNull(c,"unit fixture must be a real upward support plan");
        assertEquals(Minecraft189ScaffoldSupportPlanner.Face.UP,c.face);
        return c;
    }
    private static boolean matching(Hit hit) {
        return Minecraft189ScaffoldNativeFaceGate.matches(
                hit,candidate(),0.5,101.62,0.5,4.5);
    }

    @Test void realBlockFacePositionVectorCanMatchWithinNativeReach() {
        assertTrue(matching(new Hit(1,0,98,0,1,0.5,99,0.5)));
        assertTrue(matching(new Hit(1,0,98,0,1,0.2,99,0.8)),
                "real camera hit need not be exactly synthetic face-center");
    }
    @Test void missedRayEntityRayWrongFaceAndUnrelatedSupportAreRejected() {
        assertFalse(matching(new Hit(0,0,98,0,1,0.5,99,0.5)), "MISS/ENTITY is not BLOCK");
        assertFalse(matching(new Hit(1,0,98,0,0,0.5,99,0.5)), "DOWN is not UP");
        assertFalse(matching(new Hit(1,0,98,0,2,0.5,99,0.5)), "NORTH is not UP");
        assertFalse(matching(new Hit(1,1,98,0,1,1.5,99,0.5)), "wrong BlockPos");
        assertFalse(matching(new Hit(1,0,98,-1,1,0.5,99,-0.5)), "wrong Z BlockPos");
    }
    @Test void planeBoundsReachAndMalformedNativeRayFailClosed() {
        assertFalse(matching(new Hit(1,0,98,0,1,0.5,98.5,0.5)),
                "wrong face plane, despite a matching face ordinal");
        assertFalse(matching(new Hit(1,0,98,0,1,1.8,99,0.5)),
                "outside original solid face");
        assertFalse(matching(new Hit(1,0,98,0,1,0.5,99,Double.NaN)));
        assertFalse(matching(new Hit(1,0,98,0,1,0.5,99,Double.POSITIVE_INFINITY)));
        assertFalse(matching(new Hit(1,0,98,0,1,0.5,99)),
                "truncated source-native sample");
        assertFalse(matching(new Hit(1,0,98,0,1,0.5,99,0.5,42)));
        assertFalse(matching(new Hit()));
        Hit thrown=new Hit(1,0,98,0,1,0.5,99,0.5);
        thrown.throwsRead=true;
        assertFalse(matching(thrown));
        assertFalse(Minecraft189ScaffoldNativeFaceGate.matches(
                null,candidate(),0.5,101.62,0.5,4.5));
        assertFalse(Minecraft189ScaffoldNativeFaceGate.matches(
                new Hit(1,0,98,0,1,0.5,99,0.5),
                null,0.5,101.62,0.5,4.5));
        assertFalse(Minecraft189ScaffoldNativeFaceGate.matches(
                new Hit(1,0,98,0,1,0.5,99,0.5),
                candidate(),0.5,101.62,0.5,0.5));
        assertFalse(Minecraft189ScaffoldNativeFaceGate.matches(
                new Hit(1,0,98,0,1,0.5,99,0.5),
                candidate(),0.5,Double.NaN,0.5,4.5));
    }
}
