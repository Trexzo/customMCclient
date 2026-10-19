package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Original-shaped offline native adapter, not genuine Minecraft binary. */
final class Minecraft189BunnyHopNativeStackTest {
    private static Minecraft189PlayerMovementState.Snapshot state(boolean ground) {
        final Minecraft189PlayerMovementState s=new Minecraft189PlayerMovementState();
        s.update(ground,false,false);return s.snapshot();
    }
    private static Minecraft189PlayerRotationState.Snapshot yaw() {
        final Minecraft189PlayerRotationState s=new Minecraft189PlayerRotationState();
        s.update(0F);return s.snapshot();
    }
    private static final class Player implements Minecraft189PlayerJumpControl,
            Minecraft189PlayerMotionControl {
        int jumps,rx,rz,wx,wz;
        double x,z;
        Runnable onJump,onReadX,onReadZ,onWriteX,onWriteZ;
        boolean throwJump,throwXRead,throwXWrite;
        @Override public void customMcJump() {
            jumps++;if(jumps>8)throw new AssertionError("recursive native jump");
            if(onJump!=null){final Runnable r=onJump;onJump=null;r.run();}
            if(throwJump)throw new IllegalStateException("native jump after source action");
        }
        @Override public double customMcMotionX() {
            rx++;if(rx>12)throw new AssertionError("recursive original X read");
            if(onReadX!=null){final Runnable r=onReadX;onReadX=null;r.run();}
            if(throwXRead)throw new IllegalStateException("native X getter");
            return x;
        }
        @Override public double customMcMotionZ() {
            rz++;if(rz>12)throw new AssertionError("recursive original Z read");
            if(onReadZ!=null){final Runnable r=onReadZ;onReadZ=null;r.run();}
            return z;
        }
        @Override public void customMcSetMotionX(double v) {
            wx++;if(wx>12)throw new AssertionError("recursive original X setter");
            x=v;
            if(onWriteX!=null){final Runnable r=onWriteX;onWriteX=null;r.run();}
            if(throwXWrite)throw new IllegalStateException("native X setter");
        }
        @Override public void customMcSetMotionZ(double v) {
            wz++;if(wz>12)throw new AssertionError("recursive original Z setter");
            z=v;
            if(onWriteZ!=null){final Runnable r=onWriteZ;onWriteZ=null;r.run();}
        }
        @Override public double customMcMotionY(){return 0D;}
        @Override public void customMcSetMotionY(double y){}
    }
    private static void move(Minecraft189InputState i) {
        i.key(LegacyKeyboardCodes.W,true);
    }
    private static void activate(Minecraft189InputState i,Minecraft189BunnyHopModule m) {
        move(i);m.onEnable();
    }
    @Test void originalContactAndHorizontalForwardMotionStillWork() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189BunnyHopModule m=new Minecraft189BunnyHopModule(i);
        final Player p=new Player();activate(i,m);
        m.applyJump(p,state(true),false);
        assertEquals(1,p.jumps);
        assertTrue(m.applyMotion(p,yaw(),false));
        assertEquals(.38D,p.z,1E-12);assertEquals(1,p.wz);
    }
    @Test void samePlayerReentrantNativeJumpCannotDispatchTwice() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189BunnyHopModule m=new Minecraft189BunnyHopModule(i);
        final Player p=new Player();activate(i,m);
        p.onJump=()->m.applyJump(p,state(true),false);
        m.applyJump(p,state(true),false);assertEquals(1,p.jumps);
    }
    @Test void restartInsideNativeJumpCannotSpendNewOwnerGroundTwice() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189BunnyHopModule m=new Minecraft189BunnyHopModule(i);
        final Player p=new Player();activate(i,m);
        p.onJump=()->{m.onDisable();m.onEnable();
            m.applyJump(p,state(true),false);};
        m.applyJump(p,state(true),false);assertEquals(1,p.jumps);
        // After unwinding, the genuinely new owner remains available.
        m.applyJump(p,state(true),false);assertEquals(2,p.jumps);
    }
    @Test void secondOriginalPlayerCannotBorrowNativeJumpStack() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189BunnyHopModule m=new Minecraft189BunnyHopModule(i);
        final Player a=new Player(),b=new Player();activate(i,m);
        a.onJump=()->{m.onDisable();m.onEnable();
            m.applyJump(b,state(true),false);};
        m.applyJump(a,state(true),false);
        assertEquals(1,a.jumps);assertEquals(0,b.jumps);
    }
    @Test void originalJumpExceptionCannotPoisonFreshAirRearm() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189BunnyHopModule m=new Minecraft189BunnyHopModule(i);
        final Player p=new Player();activate(i,m);p.throwJump=true;
        assertThrows(IllegalStateException.class,
            ()->m.applyJump(p,state(true),false));
        assertEquals(1,p.jumps);
        p.throwJump=false;m.applyJump(p,state(true),false);
        assertEquals(1,p.jumps);
        m.applyJump(p,state(false),false);
        m.applyJump(p,state(true),false);assertEquals(2,p.jumps);
    }
    @Test void reentrantNativeXGetterDoesNotReadAgain() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189BunnyHopModule m=new Minecraft189BunnyHopModule(i);
        final Player p=new Player();activate(i,m);
        p.onReadX=()->assertFalse(m.applyMotion(p,yaw(),false));
        assertTrue(m.applyMotion(p,yaw(),false));
        assertEquals(1,p.rx);assertEquals(1,p.rz);
    }
    @Test void reentrantNativeZGetterDoesNotReadAgain() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189BunnyHopModule m=new Minecraft189BunnyHopModule(i);
        final Player p=new Player();activate(i,m);
        p.onReadZ=()->assertFalse(m.applyMotion(p,yaw(),false));
        assertTrue(m.applyMotion(p,yaw(),false));
        assertEquals(1,p.rx);assertEquals(1,p.rz);
    }
    @Test void originalXSetterNestedSamePlayerCannotWriteAgain() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189BunnyHopModule m=new Minecraft189BunnyHopModule(i);
        final Player p=new Player();activate(i,m);p.x=1D;
        p.onWriteX=()->assertFalse(m.applyMotion(p,yaw(),false));
        assertTrue(m.applyMotion(p,yaw(),false));
        assertEquals(1,p.wx);assertEquals(1,p.wz);
    }
    @Test void originalZSetterNestedSamePlayerCannotWriteAgain() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189BunnyHopModule m=new Minecraft189BunnyHopModule(i);
        final Player p=new Player();activate(i,m);
        p.onWriteZ=()->assertFalse(m.applyMotion(p,yaw(),false));
        assertTrue(m.applyMotion(p,yaw(),false));assertEquals(1,p.wz);
    }
    @Test void anotherPlayerInXGetterRevokesOldNativeXZ() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189BunnyHopModule m=new Minecraft189BunnyHopModule(i);
        final Player a=new Player(),b=new Player();activate(i,m);
        a.onReadX=()->assertFalse(m.applyMotion(b,yaw(),false));
        assertFalse(m.applyMotion(a,yaw(),false));
        assertEquals(0,a.rz+a.wx+a.wz+b.rx+b.rz+b.wx+b.wz);
    }
    @Test void anotherPlayerInZGetterRevokesNativeWrites() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189BunnyHopModule m=new Minecraft189BunnyHopModule(i);
        final Player a=new Player(),b=new Player();activate(i,m);
        a.onReadZ=()->assertFalse(m.applyMotion(b,yaw(),false));
        assertFalse(m.applyMotion(a,yaw(),false));
        assertEquals(0,a.wx+a.wz+b.rx+b.rz+b.wx+b.wz);
    }
    @Test void anotherPlayerInOriginalXSetterVetoesStaleZ() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189BunnyHopModule m=new Minecraft189BunnyHopModule(i);
        final Player a=new Player(),b=new Player();activate(i,m);a.x=1D;
        a.onWriteX=()->assertFalse(m.applyMotion(b,yaw(),false));
        assertFalse(m.applyMotion(a,yaw(),false));
        assertEquals(1,a.wx);assertEquals(0,a.wz+b.rx+b.rz+b.wx+b.wz);
    }
    @Test void anotherPlayerInOriginalZSetterRevokesCompletion() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189BunnyHopModule m=new Minecraft189BunnyHopModule(i);
        final Player a=new Player(),b=new Player();activate(i,m);
        a.onWriteZ=()->assertFalse(m.applyMotion(b,yaw(),false));
        assertFalse(m.applyMotion(a,yaw(),false));
        assertEquals(1,a.wz);assertEquals(0,b.rx+b.rz+b.wx+b.wz);
    }
    @Test void nativeXGetterSuspensionRevokesOldSourceOwner() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189BunnyHopModule m=new Minecraft189BunnyHopModule(i);
        final Player p=new Player();activate(i,m);
        p.onReadX=()->assertFalse(m.applyMotion(p,yaw(),true));
        assertFalse(m.applyMotion(p,yaw(),false));
        assertEquals(0,p.wx+p.wz);
    }
    @Test void nativeXGetterRotationLossRevokesOldSourceOwner() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189BunnyHopModule m=new Minecraft189BunnyHopModule(i);
        final Player p=new Player();activate(i,m);
        final Minecraft189PlayerRotationState missing=new Minecraft189PlayerRotationState();
        p.onReadX=()->assertFalse(m.applyMotion(p,missing.snapshot(),false));
        assertFalse(m.applyMotion(p,yaw(),false));
        assertEquals(0,p.wx+p.wz);
    }
    @Test void getterLifecycleRestartCancelsOriginalOldXZ() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189BunnyHopModule m=new Minecraft189BunnyHopModule(i);
        final Player p=new Player();activate(i,m);
        p.onReadX=()->{m.onDisable();m.onEnable();};
        assertFalse(m.applyMotion(p,yaw(),false));
        assertEquals(0,p.rz+p.wx+p.wz);
    }
    @Test void getterPolicyEditCancelsOriginalOldXZ() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189BunnyHopModule m=new Minecraft189BunnyHopModule(i);
        final Player p=new Player();activate(i,m);
        p.onReadZ=()->m.speedSetting().set(.7D);
        assertFalse(m.applyMotion(p,yaw(),false));
        assertEquals(0,p.wx+p.wz);
    }
    @Test void nativeXSetterThrowNeverFabricatesMotionRollback() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189BunnyHopModule m=new Minecraft189BunnyHopModule(i);
        final Player p=new Player();activate(i,m);p.x=1D;
        p.throwXWrite=true;
        assertThrows(IllegalStateException.class,()->m.applyMotion(p,yaw(),false));
        assertEquals(0D,p.x,1E-12);assertEquals(0,p.wz);
        p.throwXWrite=false;
        assertTrue(m.applyMotion(p,yaw(),false));
        assertEquals(.38D,p.z,1E-12);
    }
    @Test void genuineSourceSmoothAccelerationStillInterpolates() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189BunnyHopModule m=new Minecraft189BunnyHopModule(i);
        final Player p=new Player();m.smoothAccelerationSetting().set(Boolean.TRUE);
        m.accelerationPercentSetting().set(50);activate(i,m);
        assertTrue(m.applyMotion(p,yaw(),false));
        assertEquals(.19D,p.z,1E-12);
        assertTrue(m.applyMotion(p,yaw(),false));
        assertEquals(.285D,p.z,1E-12);
    }
    @Test void originalLandingDelayStillCountsOnlyEligibleGroundCallbacks() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189BunnyHopModule m=new Minecraft189BunnyHopModule(i);
        final Player p=new Player();m.landingDelayTicksSetting().set(2);
        activate(i,m);
        m.applyJump(p,state(true),false);m.applyJump(p,state(true),false);
        assertEquals(0,p.jumps);
        m.applyJump(p,state(true),false);assertEquals(1,p.jumps);
    }
    @Test void actualGuiOpeningDiscardsOldGroundedJumpLease() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189BunnyHopModule m=new Minecraft189BunnyHopModule(i);
        final Player p=new Player();activate(i,m);
        m.applyJump(p,state(true),false,true);
        m.applyJump(p,state(true),false,false);
        assertEquals(0,p.jumps);
        m.applyJump(p,state(false),false,false);
        m.applyJump(p,state(true),false,false);
        assertEquals(1,p.jumps);
    }
    @Test void noPhysicalWASDNeverForgesBunnyHopMotion() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189BunnyHopModule m=new Minecraft189BunnyHopModule(i);
        final Player p=new Player();m.onEnable();
        assertFalse(m.applyMotion(p,yaw(),false));
        assertEquals(0,p.rx+p.rz+p.wx+p.wz);
    }
}
