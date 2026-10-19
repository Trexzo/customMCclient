package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Original-shaped mapped source regression, not Mojang binary proof. */
final class Minecraft189StrafeNativeSourceOwnerTest {
    private static Minecraft189PlayerRotationState.Snapshot rotation() {
        final Minecraft189PlayerRotationState r=new Minecraft189PlayerRotationState();
        r.update(0F);return r.snapshot();
    }
    private static Minecraft189PlayerMovementState.Snapshot motion(
            boolean ground,boolean sneak) {
        final Minecraft189PlayerMovementState m=new Minecraft189PlayerMovementState();
        m.update(ground,sneak,false);return m.snapshot();
    }
    private static final class Player implements Minecraft189PlayerMotionControl {
        double x,z;int rx,rz,wx,wz;
        Runnable onReadX,onReadZ,onWriteX,onWriteZ;
        boolean failReadX,failWriteX;
        @Override public double customMcMotionY(){return 0D;}
        @Override public void customMcSetMotionY(double y){}
        @Override public double customMcMotionX(){
            rx++;if(rx>9)throw new AssertionError("recursive original X read");
            final Runnable c=onReadX;onReadX=null;if(c!=null)c.run();
            if(failReadX)throw new IllegalStateException("native getter");
            return x;
        }
        @Override public double customMcMotionZ(){
            rz++;if(rz>9)throw new AssertionError("recursive original Z read");
            final Runnable c=onReadZ;onReadZ=null;if(c!=null)c.run();
            return z;
        }
        @Override public void customMcSetMotionX(double v){
            wx++;if(wx>9)throw new AssertionError("recursive original X write");
            x=v;
            final Runnable c=onWriteX;onWriteX=null;if(c!=null)c.run();
            if(failWriteX)throw new IllegalStateException("native X setter");
        }
        @Override public void customMcSetMotionZ(double v){
            wz++;if(wz>9)throw new AssertionError("recursive original Z write");
            z=v;
            final Runnable c=onWriteZ;onWriteZ=null;if(c!=null)c.run();
        }
    }
    private static Minecraft189StrafeModule strafe(Minecraft189InputState i){
        i.key(LegacyKeyboardCodes.W,true);
        final Minecraft189StrafeModule m=new Minecraft189StrafeModule(i);
        m.onEnable();return m;
    }
    @Test void realForwardSourceWritesExactOriginalZ() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189StrafeModule m=strafe(i);final Player p=new Player();
        m.apply(p,rotation(),false);assertEquals(1,p.rx);
        assertEquals(1,p.rz);assertEquals(1,p.wz);
        assertEquals(.3D,p.z,1E-12);
    }
    @Test void mappedXGetterSamePlayerNestedCallbackCannotReadTwice(){
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189StrafeModule m=strafe(i);final Player p=new Player();
        p.onReadX=()->m.apply(p,rotation(),false);
        m.apply(p,rotation(),false);
        assertEquals(1,p.rx);assertEquals(1,p.rz);
    }
    @Test void mappedZGetterSamePlayerNestedCallbackCannotReadTwice(){
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189StrafeModule m=strafe(i);final Player p=new Player();
        p.onReadZ=()->m.apply(p,rotation(),false);
        m.apply(p,rotation(),false);
        assertEquals(1,p.rx);assertEquals(1,p.rz);
    }
    @Test void mappedXSetterSamePlayerNestedCallbackCannotDoubleWrite(){
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189StrafeModule m=strafe(i);final Player p=new Player();
        p.x=1D;p.onWriteX=()->m.apply(p,rotation(),false);
        m.apply(p,rotation(),false);assertEquals(1,p.wx);assertEquals(1,p.wz);
    }
    @Test void mappedZSetterSamePlayerNestedCallbackCannotDoubleWrite(){
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189StrafeModule m=strafe(i);final Player p=new Player();
        p.onWriteZ=()->m.apply(p,rotation(),false);
        m.apply(p,rotation(),false);assertEquals(1,p.wz);
    }
    @Test void secondOriginalPlayerDuringXGetterCancelsOldXZ(){
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189StrafeModule m=strafe(i);
        final Player a=new Player(),b=new Player();
        a.onReadX=()->m.apply(b,rotation(),false);
        m.apply(a,rotation(),false);
        assertEquals(0,a.rz+a.wx+a.wz+b.rx+b.rz+b.wx+b.wz);
    }
    @Test void secondOriginalPlayerDuringZGetterCancelsOldXZ(){
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189StrafeModule m=strafe(i);
        final Player a=new Player(),b=new Player();
        a.onReadZ=()->m.apply(b,rotation(),false);
        m.apply(a,rotation(),false);
        assertEquals(0,a.wx+a.wz+b.rx+b.rz+b.wx+b.wz);
    }
    @Test void secondOriginalPlayerDuringXSetterStopsStaleZ(){
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189StrafeModule m=strafe(i);
        final Player a=new Player(),b=new Player();a.x=1D;
        a.onWriteX=()->m.apply(b,rotation(),false);
        m.apply(a,rotation(),false);
        assertEquals(1,a.wx);assertEquals(0,a.wz+b.rx+b.rz+b.wx+b.wz);
    }
    @Test void secondOriginalPlayerDuringZSetterNeverBorrowsCurrentStack(){
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189StrafeModule m=strafe(i);
        final Player a=new Player(),b=new Player();
        a.onWriteZ=()->m.apply(b,rotation(),false);
        m.apply(a,rotation(),false);
        assertEquals(1,a.wz);assertEquals(0,b.rx+b.rz+b.wx+b.wz);
    }
    @Test void hostSuspensionDuringXGetterRevokesOriginalAxes(){
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189StrafeModule m=strafe(i);final Player p=new Player();
        p.onReadX=()->m.apply(p,rotation(),true);
        m.apply(p,rotation(),false);assertEquals(0,p.rz+p.wx+p.wz);
    }
    @Test void lostOriginalRotationDuringXGetterRevokesSource(){
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189StrafeModule m=strafe(i);final Player p=new Player();
        final Minecraft189PlayerRotationState none=new Minecraft189PlayerRotationState();
        p.onReadX=()->m.apply(p,none.snapshot(),false);
        m.apply(p,rotation(),false);assertEquals(0,p.rz+p.wx+p.wz);
    }
    @Test void optedInGroundOnlyVetoesNestedOriginalLandingLoss(){
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189StrafeModule m=strafe(i);final Player p=new Player();
        m.groundOnlySetting().set(Boolean.TRUE);
        p.onReadX=()->m.apply(p,rotation(),false,motion(false,false));
        m.apply(p,rotation(),false,motion(true,false));
        assertEquals(0,p.rz+p.wx+p.wz);
    }
    @Test void optedInPauseSneakVetoesNestedRealSneaking(){
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189StrafeModule m=strafe(i);final Player p=new Player();
        m.pauseWhileSneakingSetting().set(Boolean.TRUE);
        p.onReadX=()->m.apply(p,rotation(),false,motion(true,true));
        m.apply(p,rotation(),false,motion(true,false));
        assertEquals(0,p.rz+p.wx+p.wz);
    }
    @Test void onDisableInsideNativeXGetterRejectsOldMotion(){
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189StrafeModule m=strafe(i);final Player p=new Player();
        p.onReadX=m::onDisable;
        m.apply(p,rotation(),false);
        assertEquals(0,p.rz+p.wx+p.wz);
    }
    @Test void offOnInNativeXGetterDoesNotBorrowNewLifetime(){
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189StrafeModule m=strafe(i);final Player p=new Player();
        p.onReadX=()->{m.onDisable();m.onEnable();m.apply(p,rotation(),false);};
        m.apply(p,rotation(),false);
        assertEquals(0,p.rz+p.wx+p.wz);
        m.apply(p,rotation(),false);assertEquals(1,p.wz);
    }
    @Test void originalXGetterGuiSpeedEditVetoesOldXZ(){
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189StrafeModule m=strafe(i);final Player p=new Player();
        p.onReadX=()->m.speedSetting().set(.42D);
        m.apply(p,rotation(),false);assertEquals(0,p.rz+p.wx+p.wz);
    }
    @Test void physicalWReleaseInsideGetterCannotWriteOldAxis(){
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189StrafeModule m=strafe(i);final Player p=new Player();
        p.onReadX=()->i.key(LegacyKeyboardCodes.W,false);
        m.apply(p,rotation(),false);assertEquals(0,p.rz+p.wx+p.wz);
    }
    @Test void mappedGetterThrowReleasesNativeOwnerForNextTick(){
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189StrafeModule m=strafe(i);final Player p=new Player();
        p.failReadX=true;
        assertThrows(IllegalStateException.class,()->m.apply(p,rotation(),false));
        p.failReadX=false;m.apply(p,rotation(),false);
        assertEquals(1,p.wz);
    }
    @Test void mappedSetterThrowPreservesPartialVanillaXWrite(){
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189StrafeModule m=strafe(i);final Player p=new Player();
        p.x=1D;p.failWriteX=true;
        assertThrows(IllegalStateException.class,()->m.apply(p,rotation(),false));
        assertEquals(0D,p.x,1E-12);assertEquals(0,p.wz);
        p.failWriteX=false;m.apply(p,rotation(),false);
        assertEquals(1,p.wz);
    }
    @Test void realSmoothAccelerationUsesActualNativeMotion(){
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189StrafeModule m=strafe(i);final Player p=new Player();
        m.smoothAccelerationSetting().set(Boolean.TRUE);
        m.accelerationPercentSetting().set(50);
        m.apply(p,rotation(),false);assertEquals(.15D,p.z,1E-12);
        m.apply(p,rotation(),false);assertEquals(.225D,p.z,1E-12);
    }
    @Test void unknownMovementFailsClosedWhenGroundOnlyOptedIn(){
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189StrafeModule m=strafe(i);final Player p=new Player();
        m.groundOnlySetting().set(Boolean.TRUE);
        m.apply(p,rotation(),false,new Minecraft189PlayerMovementState().snapshot());
        assertEquals(0,p.rx+p.rz+p.wx+p.wz);
    }
    @Test void noPhysicalWASDNeverCreatesOriginalMotion(){
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189StrafeModule m=new Minecraft189StrafeModule(i);
        final Player p=new Player();m.onEnable();
        m.apply(p,rotation(),false);assertEquals(0,p.rx+p.rz+p.wx+p.wz);
    }
}
