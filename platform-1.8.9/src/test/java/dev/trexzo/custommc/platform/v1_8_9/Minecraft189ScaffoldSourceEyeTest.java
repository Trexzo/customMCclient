package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189ScaffoldSourceEyeTest {
    private static final class Player implements Minecraft189PlayerPositionAccess,
            Minecraft189LocalPlayerSafeWalkMarker, Minecraft189ScaffoldNativeEyeAccess {
        double x=.5,y=100,z=.5;
        float eye=1.62F;
        boolean throwEye;
        public double customMcPositionX(){return x;}
        public double customMcPositionY(){return y;}
        public double customMcPositionZ(){return z;}
        public float customMcOriginalEyeHeight(){
            if (throwEye) throw new IllegalStateException("original method unavailable");
            return eye;
        }
    }
    private static final class Foreign implements Minecraft189PlayerPositionAccess,
            Minecraft189ScaffoldNativeEyeAccess {
        public double customMcPositionX(){return .5;}
        public double customMcPositionY(){return 100;}
        public double customMcPositionZ(){return .5;}
        public float customMcOriginalEyeHeight(){return 1.62F;}
    }
    @Test void sourceNativeEyeChangesWithOriginalMethodOutput() {
        Player player=new Player();
        assertEquals(100.0D + (double)1.62F,
                Minecraft189ScaffoldSourceEye.currentEyeY(player),0.000001D);
        player.eye=1.54F;
        assertEquals(100.0D + (double)1.54F,
                Minecraft189ScaffoldSourceEye.currentEyeY(player),0.000001D,
                "sneaking/sleeping must use source result, not fixed 1.62");
        player.y=102;
        assertEquals(102.0D + (double)1.54F,
                Minecraft189ScaffoldSourceEye.currentEyeY(player),0.000001D);
    }
    @Test void missingUntrustedOrMalformedOriginalEyeFailsClosed(){
        assertTrue(Double.isNaN(Minecraft189ScaffoldSourceEye.currentEyeY(null)));
        assertTrue(Double.isNaN(Minecraft189ScaffoldSourceEye.currentEyeY(new Foreign())));
        Player player=new Player();
        player.eye=Float.NaN;
        assertTrue(Double.isNaN(Minecraft189ScaffoldSourceEye.currentEyeY(player)));
        player.eye=Float.POSITIVE_INFINITY;
        assertTrue(Double.isNaN(Minecraft189ScaffoldSourceEye.currentEyeY(player)));
        player.eye=0;
        assertTrue(Double.isNaN(Minecraft189ScaffoldSourceEye.currentEyeY(player)));
        player.eye=2.3F;
        assertTrue(Double.isNaN(Minecraft189ScaffoldSourceEye.currentEyeY(player)));
        player.eye=1.62F;
        player.z=Double.NaN;
        assertTrue(Double.isNaN(Minecraft189ScaffoldSourceEye.currentEyeY(player)));
        player.z=.5;
        player.x=30000000.0D;
        assertTrue(Double.isNaN(Minecraft189ScaffoldSourceEye.currentEyeY(player)));
        player.x=.5;
        player.throwEye=true;
        assertTrue(Double.isNaN(Minecraft189ScaffoldSourceEye.currentEyeY(player)));
    }
}
