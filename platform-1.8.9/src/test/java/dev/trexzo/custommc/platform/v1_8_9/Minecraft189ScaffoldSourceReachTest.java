package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189ScaffoldSourceReachTest {
    private static final class Vanilla implements Minecraft189VanillaReachAccess {
        float distance=4.5F;
        boolean extended;
        boolean throwsRead;
        int queries;
        public float customMcVanillaBlockReachDistance(){
            queries++;
            if (throwsRead) throw new IllegalStateException("native controller missing");
            return distance;
        }
        public boolean customMcVanillaExtendedReach(){queries++;return extended;}
    }
    private static final class Original implements Minecraft189ScaffoldNativeControllerAccess {
        Object owner;
        boolean throwOnRead;
        public Object customMcScaffoldNativeController(){
            if(throwOnRead) throw new IllegalStateException("Minecraft source gone");
            return owner;
        }
    }
    @Test void readsNativeControllerWithoutExtendingReach(){
        Vanilla controller=new Vanilla();
        Original game=new Original();
        game.owner=controller;
        assertEquals(4.5D,Minecraft189ScaffoldSourceReach.conservativeReach(game),1e-8);
        controller.distance=5.0F;controller.extended=true;
        assertEquals(4.5D,Minecraft189ScaffoldSourceReach.conservativeReach(game),1e-8,
                "Creative native reach may not exceed planner's 4.5 cap");
        controller.distance=3.0F;controller.extended=false;
        assertEquals(3.0D,Minecraft189ScaffoldSourceReach.conservativeReach(game),1e-8);
        assertTrue(controller.queries>=6,"read both original reach facts");
    }
    @Test void missingMalformedNativeControllerAlwaysFailsClosed(){
        assertTrue(Double.isNaN(Minecraft189ScaffoldSourceReach.conservativeReach(null)));
        assertTrue(Double.isNaN(Minecraft189ScaffoldSourceReach.conservativeReach(new Object())));
        Original game=new Original();
        assertTrue(Double.isNaN(Minecraft189ScaffoldSourceReach.conservativeReach(game)));
        game.owner=new Object();
        assertTrue(Double.isNaN(Minecraft189ScaffoldSourceReach.conservativeReach(game)));
        Vanilla controller=new Vanilla();
        game.owner=controller;
        controller.distance=Float.NaN;
        assertTrue(Double.isNaN(Minecraft189ScaffoldSourceReach.conservativeReach(game)));
        controller.distance=Float.POSITIVE_INFINITY;
        assertTrue(Double.isNaN(Minecraft189ScaffoldSourceReach.conservativeReach(game)));
        controller.distance=0.0F;
        assertTrue(Double.isNaN(Minecraft189ScaffoldSourceReach.conservativeReach(game)));
        controller.distance=-1.0F;
        assertTrue(Double.isNaN(Minecraft189ScaffoldSourceReach.conservativeReach(game)));
        controller.distance=7.0F;
        assertTrue(Double.isNaN(Minecraft189ScaffoldSourceReach.conservativeReach(game)));
        controller.distance=5.0F;
        controller.throwsRead=true;
        assertTrue(Double.isNaN(Minecraft189ScaffoldSourceReach.conservativeReach(game)));
        controller.throwsRead=false;
        game.throwOnRead=true;
        assertTrue(Double.isNaN(Minecraft189ScaffoldSourceReach.conservativeReach(game)));
    }
}
