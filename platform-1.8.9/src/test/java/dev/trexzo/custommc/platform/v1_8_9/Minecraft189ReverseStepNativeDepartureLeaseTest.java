package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The real mapped original player motionY getter/setter and an observed
 * original grounded-to-air transition. Offline Java source regressions.
 */
final class Minecraft189ReverseStepNativeDepartureLeaseTest {
    private static final class NativePlayer implements Minecraft189PlayerMotionControl {
        double x, y, z;
        int writes, maximumDepth, depth;
        Runnable onWrite, onRead;
        boolean throwAfterWrite;
        @Override public double customMcMotionX() { return x; }
        @Override public void customMcSetMotionX(double v) { x=v; }
        @Override public double customMcMotionY() {
            Runnable hook=onRead;onRead=null;
            if(hook!=null)hook.run();
            return y;
        }
        @Override public void customMcSetMotionY(double v) {
            writes++;y=v;
            depth++;
            maximumDepth=Math.max(depth,maximumDepth);
            try {
                Runnable callback=onWrite;onWrite=null;
                if(callback!=null)callback.run();
                if(throwAfterWrite) {
                    throwAfterWrite=false;
                    throw new IllegalStateException("genuine mapped Y setter mutated then threw");
                }
            } finally {depth--;}
        }
        @Override public double customMcMotionZ() { return z; }
        @Override public void customMcSetMotionZ(double v) { z=v; }
    }
    private static final class Rig {
        final Minecraft189ReverseStepModule module = new Minecraft189ReverseStepModule();
        final Minecraft189PlayerMovementState movement = new Minecraft189PlayerMovementState();
        final NativePlayer player = new NativePlayer();
        Rig() { module.onEnable(); }
        boolean ground() {
            movement.update(true,false,false);
            return tick();
        }
        boolean air() {
            movement.update(false,false,false);
            return tick();
        }
        boolean tick() {
            return module.apply(player,movement.snapshot(),false,true,false);
        }
        void depart() {
            assertFalse(ground());
            assertFalse(air());
        }
    }
    @Test void immediateGenuineGroundToAirWritesExactlyOnce() {
        Rig r=new Rig();
        r.player.y=-.1D;
        assertFalse(r.ground());
        assertTrue(r.air());
        assertEquals(-.5D,r.player.y,1e-12);
        assertEquals(1,r.player.writes);
        assertFalse(r.tick());
        assertEquals(1,r.player.writes);
    }
    @Test void stableDelayedDepartureRequiresExactlyTwoSkippedNativeCallbacks() {
        Rig r=new Rig();
        r.module.delayTicksSetting().set(2);
        r.player.y=-.1D;
        r.depart();
        assertFalse(r.tick());
        assertTrue(r.tick());
        assertEquals(1,r.player.writes);
    }
    @Test void speedGuiEditInDelayedAirContactRevokesOldSource() {
        Rig r=new Rig();
        r.module.delayTicksSetting().set(2);
        r.depart();
        r.module.speedSetting().set(.9D);
        assertFalse(r.tick());assertFalse(r.tick());assertFalse(r.tick());
        assertEquals(0,r.player.writes);
        assertFalse(r.ground());assertFalse(r.air());assertFalse(r.tick());
        assertTrue(r.tick());
        assertEquals(-.9D,r.player.y,1e-12);
    }
    @Test void delayGuiEditCannotAccelerateOldPendingNativeDeparture() {
        Rig r=new Rig();
        r.module.delayTicksSetting().set(3);
        r.depart();
        r.module.delayTicksSetting().set(0);
        assertFalse(r.tick());assertFalse(r.tick());assertFalse(r.tick());
        assertEquals(0,r.player.writes);
        assertFalse(r.ground());
        assertTrue(r.air());
        assertEquals(1,r.player.writes);
    }
    @Test void enablingSneakGateDuringDelayedAirRequiresNewContact() {
        Rig r=new Rig();
        r.module.delayTicksSetting().set(1);
        r.depart();
        r.module.requireSneakingSetting().set(true);
        assertFalse(r.tick());
        r.movement.update(false,true,false);
        assertFalse(r.tick());
        assertEquals(0,r.player.writes);
        r.movement.update(true,true,false);assertFalse(r.tick());
        r.movement.update(false,true,false);assertFalse(r.tick());
        assertTrue(r.tick());
    }
    @Test void enablingMovementRequirementDuringPendingDelayCancelsContact() {
        Rig r=new Rig();
        r.module.delayTicksSetting().set(1);
        r.depart();
        r.module.requireMovementSetting().set(true);
        assertFalse(r.tick());assertFalse(r.tick());
        assertEquals(0,r.player.writes);
        assertFalse(r.ground());assertFalse(r.air());
        assertTrue(r.tick());
    }
    @Test void genuineNativeYSetterDisableDoesNotReportNewOwnerSuccess() {
        Rig r=new Rig();
        r.player.onWrite=r.module::onDisable;
        assertFalse(r.ground());
        assertFalse(r.air());
        assertEquals(1,r.player.writes);
        assertEquals(-.5D,r.player.y,1e-12);
        assertFalse(r.module.active());
    }
    @Test void genuineYSetterOffOnDoesNotTransferOldDeparture() {
        Rig r=new Rig();
        r.player.onWrite=()->{r.module.onDisable();r.module.onEnable();};
        assertFalse(r.ground());
        assertFalse(r.air());
        assertEquals(1,r.player.writes);
        assertFalse(r.tick());
        assertFalse(r.ground());
        r.player.y=0D;
        assertTrue(r.air());
        assertEquals(2,r.player.writes);
    }
    @Test void originalNativeYSetterGuiSpeedChangeRevokesOldResult() {
        Rig r=new Rig();
        r.player.onWrite=()->r.module.speedSetting().set(.8D);
        assertFalse(r.ground());
        assertFalse(r.air());
        assertEquals(1,r.player.writes);
        assertEquals(-.5D,r.player.y,1e-12);
        assertFalse(r.tick());
        assertFalse(r.ground());
        r.player.y=0D;
        assertTrue(r.air());
        assertEquals(-.8D,r.player.y,1e-12);
    }
    @Test void sourceYSetterNestedOriginalMovementDoesNotRecurse() {
        Rig r=new Rig();
        r.player.onWrite=()->{
            r.movement.update(true,false,false);
            assertFalse(r.tick());
            r.movement.update(false,false,false);
            assertFalse(r.tick());
        };
        assertFalse(r.ground());
        assertTrue(r.air());
        assertEquals(1,r.player.writes);
        assertEquals(1,r.player.maximumDepth);
    }
    @Test void offOnInsideNativeSetterDoesNotAllowNestedSecondYWrite() {
        Rig r=new Rig();
        r.player.onWrite=()->{
            r.module.onDisable();
            r.module.onEnable();
            assertFalse(r.ground());
            assertFalse(r.air());
        };
        assertFalse(r.ground());
        assertFalse(r.air());
        assertEquals(1,r.player.writes);
    }
    @Test void partialNativeYFailureCannotReplayConsumedOriginalDeparture() {
        Rig r=new Rig();
        r.player.throwAfterWrite=true;
        assertFalse(r.ground());
        assertThrows(IllegalStateException.class,r::air);
        assertEquals(1,r.player.writes);
        assertFalse(r.tick());
        assertFalse(r.ground());
        r.player.y=0D;
        assertTrue(r.air());
        assertEquals(2,r.player.writes);
    }
    @Test void actualUpwardMotionStillCannotBeOverwritten() {
        Rig r=new Rig();
        r.player.y=.42D;
        assertFalse(r.ground());
        assertFalse(r.air());
        assertEquals(0,r.player.writes);
        r.player.y=-.1D;
        assertFalse(r.tick());
        assertEquals(0,r.player.writes);
    }
    @Test void physicalMovementReleaseCancelsDelayedLease() {
        Rig r=new Rig();
        r.module.delayTicksSetting().set(2);
        r.module.requireMovementSetting().set(true);
        r.depart();
        assertFalse(r.module.apply(r.player,r.movement.snapshot(),false,false,false));
        assertFalse(r.tick());assertFalse(r.tick());assertFalse(r.tick());
        assertEquals(0,r.player.writes);
    }
    @Test void realClickGuiFocusCancelsDelayedAirborneDeparture() {
        Rig r=new Rig();
        r.module.delayTicksSetting().set(1);
        r.depart();
        assertFalse(r.module.apply(r.player,r.movement.snapshot(),false,true,true));
        assertFalse(r.tick());assertFalse(r.tick());
        assertEquals(0,r.player.writes);
    }
}
