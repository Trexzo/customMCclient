package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Primitive policy negatives only. These fake world snapshots NEVER certify
 * that the real Mojang mouse aimed at a real edge or that the server placed it.
 */
final class Minecraft189ScaffoldNativeAdjacentEdgeTest {
    private static final class World
            implements Minecraft189ScaffoldSupportPlanner.WorldEvidence {
        long epoch=1;
        boolean loaded=true, supportSolid=true, targetAir=true;
        public long generation() { return epoch; }
        public Minecraft189ScaffoldSupportPlanner.Cell cell(int x,int y,int z) {
            if (!loaded) return Minecraft189ScaffoldSupportPlanner.Cell.UNKNOWN;
            if (x==-2 && y==99 && z==0 && supportSolid)
                return Minecraft189ScaffoldSupportPlanner.Cell.FULL_SOLID;
            if (x==-1 && y==99 && z==0 && targetAir)
                return Minecraft189ScaffoldSupportPlanner.Cell.REPLACEABLE;
            return Minecraft189ScaffoldSupportPlanner.Cell.OTHER;
        }
    }
    private static final double[] EAST_HIT =
            {1,-2,99,0,5,-1,99.5,.5};
    private static Minecraft189ScaffoldSupportPlanner.Candidate
            choose(World w,double feetX,double[] hit) {
        return Minecraft189ScaffoldSupportPlanner.chooseNativeAdjacent(
                w,1,true,true,feetX,100,.5,101.62,true,8,4.5,hit);
    }

    @Test void onlyNativeEastFaceCanProposeDistinctForwardAir() {
        World w=new World();
        assertNull(Minecraft189ScaffoldSupportPlanner.choose(
                w,1,true,true,-1.29,100,.5,101.62,true,8,4.5),
                "old underfoot candidate rejects standing on existing stone");
        final Minecraft189ScaffoldSupportPlanner.Candidate next =
                choose(w,-1.29,EAST_HIT);
        assertNotNull(next);
        assertEquals(-1,next.targetX);
        assertEquals(99,next.targetY);
        assertEquals(0,next.targetZ);
        assertEquals(-2,next.supportX);
        assertEquals(Minecraft189ScaffoldSupportPlanner.Face.EAST,next.face);
        assertEquals(-1.0,next.hitX);
    }

    @Test void playerMustRemainGroundedOnRealSupportNearSameEdge() {
        World w=new World();
        assertNull(choose(w,-1.53,EAST_HIT),
                "center must be physically near the support's actual east edge");
        assertNotNull(choose(w,-1.37,EAST_HIT));
        assertNull(Minecraft189ScaffoldSupportPlanner.chooseNativeAdjacent(
                w,1,true,false,-1.29,100,.5,101.62,true,8,4.5,EAST_HIT));
        assertNull(Minecraft189ScaffoldSupportPlanner.chooseNativeAdjacent(
                w,1,false,true,-1.29,100,.5,101.62,true,8,4.5,EAST_HIT));
        assertNull(Minecraft189ScaffoldSupportPlanner.chooseNativeAdjacent(
                w,1,true,true,-1.29,100,.5,101.62,false,8,4.5,EAST_HIT));
        assertNull(Minecraft189ScaffoldSupportPlanner.chooseNativeAdjacent(
                w,1,true,true,-1.29,100,.5,101.62,true,0,4.5,EAST_HIT));
    }

    @Test void sourceFacePlaneSupportAndDirectionCannotBeFabricated() {
        World w=new World();
        double[] wrong=EAST_HIT.clone();
        wrong[4]=1; assertNull(choose(w,-1.29,wrong),
                "original UP hit cannot certify a forward side placement");
        wrong=EAST_HIT.clone();
        wrong[1]=-3; assertNull(choose(w,-1.29,wrong),
                "cursor must report the solid block physically underfoot");
        wrong=EAST_HIT.clone();
        wrong[2]=98; assertNull(choose(w,-1.29,wrong),
                "different Y support rejected");
        wrong=EAST_HIT.clone();
        wrong[1]=-1.5; assertNull(choose(w,-1.29,wrong),
                "nonintegral support coordinate rejected");
        wrong=EAST_HIT.clone();
        wrong[4]=Double.NaN; assertNull(choose(w,-1.29,wrong));
        wrong=EAST_HIT.clone();
        wrong[0]=0; assertNull(choose(w,-1.29,wrong),
                "MISS/ENTITY can never authorize native BLOCK");
    }

    @Test void missingOrChangedOriginalWorldAndOccupiedTargetVeto() {
        World w=new World();
        w.loaded=false; assertNull(choose(w,-1.29,EAST_HIT));
        w.loaded=true; w.supportSolid=false; assertNull(choose(w,-1.29,EAST_HIT));
        w.supportSolid=true; w.targetAir=false;
        assertNull(choose(w,-1.29,EAST_HIT));
        w.targetAir=true; w.epoch=2;
        assertNull(choose(w,-1.29,EAST_HIT),
                "stale activation World generation forbidden");
        w.epoch=1;
        assertNotNull(choose(w,-1.29,EAST_HIT));
    }

    @Test void boundsInvalidEyesAndReachFailShut() {
        World w=new World();
        assertNull(Minecraft189ScaffoldSupportPlanner.chooseNativeAdjacent(
                w,1,true,true,-1.29,100,.5,Double.NaN,true,8,4.5,EAST_HIT));
        assertNull(Minecraft189ScaffoldSupportPlanner.chooseNativeAdjacent(
                w,1,true,true,-1.29,100,.5,101.62,true,8,5.0,EAST_HIT));
        assertNull(Minecraft189ScaffoldSupportPlanner.chooseNativeAdjacent(
                w,1,true,true,-1.29,100,.5,100-1,true,8,4.5,EAST_HIT));
        assertNull(choose(w,-1.29,null));
        assertNull(choose(w,-1.29,new double[]{1,-2,99,0,5}));
    }
}
