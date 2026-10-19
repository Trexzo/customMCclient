package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Genuine-shaped adapter: offline regression, not Mojang binary proof. */
final class Minecraft189FlightNativeThreeAxisTest {
    private static Minecraft189PlayerRotationState.Snapshot yaw() {
        final Minecraft189PlayerRotationState s=new Minecraft189PlayerRotationState();
        s.update(0F);return s.snapshot();
    }
    private static final class Player implements Minecraft189PlayerMotionControl {
        double x=0D,y=.42D,z=0D;
        int ry,rx,rz,wy,wx,wz;
        Runnable onReadY,onWriteY,onReadX,onReadZ,onWriteX,onWriteZ;
        boolean throwYRead,throwYWrite,throwXRead,throwXWrite;
        private static void callback(Runnable r) {if(r!=null)r.run();}
        @Override public double customMcMotionY() {
            ry++;if(ry>10)throw new AssertionError("recursive Y read");
            final Runnable c=onReadY;onReadY=null;callback(c);
            if(throwYRead)throw new IllegalStateException("native Y read");
            return y;
        }
        @Override public void customMcSetMotionY(double v) {
            wy++;y=v;
            final Runnable c=onWriteY;onWriteY=null;callback(c);
            if(throwYWrite)throw new IllegalStateException("native Y setter");
        }
        @Override public double customMcMotionX() {
            rx++;if(rx>10)throw new AssertionError("recursive X read");
            final Runnable c=onReadX;onReadX=null;callback(c);
            if(throwXRead)throw new IllegalStateException("native X read");
            return x;
        }
        @Override public double customMcMotionZ() {
            rz++;if(rz>10)throw new AssertionError("recursive Z read");
            final Runnable c=onReadZ;onReadZ=null;callback(c);
            return z;
        }
        @Override public void customMcSetMotionX(double v) {
            wx++;x=v;
            final Runnable c=onWriteX;onWriteX=null;callback(c);
            if(throwXWrite)throw new IllegalStateException("native X setter");
        }
        @Override public void customMcSetMotionZ(double v) {
            wz++;z=v;
            final Runnable c=onWriteZ;onWriteZ=null;callback(c);
        }
    }
    private static Minecraft189FlightModule flight(Minecraft189InputState i) {
        i.key(LegacyKeyboardCodes.W,true);
        final Minecraft189FlightModule m=new Minecraft189FlightModule(i);
        m.onEnable();return m;
    }
    @Test void originalFlightHoverAndForwardWritesYThenZOnce() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189FlightModule m=flight(i);final Player p=new Player();
        m.apply(p,yaw());
        assertEquals(1,p.ry);assertEquals(1,p.rx);assertEquals(1,p.rz);
        assertEquals(1,p.wy);assertEquals(1,p.wz);
        assertEquals(0D,p.y,1E-12);assertEquals(.3D,p.z,1E-12);
    }
    @Test void originalYGetterNestedSamePlayerCannotSpendSecondTick() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189FlightModule m=flight(i);final Player p=new Player();
        p.onReadY=()->m.apply(p,yaw());
        m.apply(p,yaw());
        assertEquals(1,p.ry);assertEquals(1,p.wy);
        assertEquals(1,p.rx);assertEquals(1,p.rz);
    }
    @Test void originalYSetterNestedSamePlayerCannotRepeatYOrXZ() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189FlightModule m=flight(i);final Player p=new Player();
        p.onWriteY=()->m.apply(p,yaw());
        m.apply(p,yaw());
        assertEquals(1,p.ry);assertEquals(1,p.wy);assertEquals(1,p.rz);
    }
    @Test void anotherOriginalPlayerDuringYGetterCancelsAllAxes() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189FlightModule m=flight(i);
        final Player a=new Player(),b=new Player();
        a.onReadY=()->m.apply(b,yaw());
        m.apply(a,yaw());
        assertEquals(0,a.wy+a.rx+a.rz+a.wx+a.wz+b.ry+b.rx+b.rz+b.wy+b.wx+b.wz);
    }
    @Test void anotherOriginalPlayerDuringYSetterCancelsLaterXZ() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189FlightModule m=flight(i);
        final Player a=new Player(),b=new Player();
        a.onWriteY=()->m.apply(b,yaw());
        m.apply(a,yaw());
        assertEquals(1,a.wy);assertEquals(0,a.rx+a.rz+a.wx+a.wz+b.ry+b.wx+b.wz);
    }
    @Test void genuineGuiFocusDuringYGetterRevokesMotion() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189FlightModule m=flight(i);final Player p=new Player();
        p.onReadY=()->m.apply(p,yaw(),null,true);
        m.apply(p,yaw());
        assertEquals(0,p.wy+p.rx+p.rz+p.wx+p.wz);
    }
    @Test void sourceDisabledDuringYGetterCancelsBothStages() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189FlightModule m=flight(i);final Player p=new Player();
        p.onReadY=m::onDisable;
        m.apply(p,yaw());
        assertEquals(0,p.wy+p.rx+p.rz+p.wx+p.wz);
    }
    @Test void offOnDuringYGetterNeverBorrowsOldEpoch() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189FlightModule m=flight(i);final Player p=new Player();
        p.onReadY=()->{m.onDisable();m.onEnable();m.apply(p,yaw());};
        m.apply(p,yaw());
        assertEquals(0,p.wy+p.rx+p.rz+p.wx+p.wz);
        m.apply(p,yaw());assertEquals(1,p.wz);
    }
    @Test void nestedOriginalXGetterSamePlayerNeverReadsAgain() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189FlightModule m=flight(i);final Player p=new Player();
        p.onReadX=()->m.apply(p,yaw());
        m.apply(p,yaw());
        assertEquals(1,p.ry);assertEquals(1,p.rx);assertEquals(1,p.rz);
    }
    @Test void nestedOriginalZGetterSamePlayerNeverReadsAgain() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189FlightModule m=flight(i);final Player p=new Player();
        p.onReadZ=()->m.apply(p,yaw());
        m.apply(p,yaw());
        assertEquals(1,p.ry);assertEquals(1,p.rx);assertEquals(1,p.rz);
    }
    @Test void anotherOriginalPlayerDuringXGetterCancelsXZ() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189FlightModule m=flight(i);
        final Player a=new Player(),b=new Player();
        a.onReadX=()->m.apply(b,yaw());
        m.apply(a,yaw());
        assertEquals(0,a.rz+a.wx+a.wz+b.ry+b.rx+b.rz+b.wy+b.wx+b.wz);
    }
    @Test void anotherOriginalPlayerDuringZGetterCancelsNativeWrites() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189FlightModule m=flight(i);
        final Player a=new Player(),b=new Player();
        a.onReadZ=()->m.apply(b,yaw());
        m.apply(a,yaw());
        assertEquals(0,a.wx+a.wz+b.ry+b.rx+b.rz+b.wy+b.wx+b.wz);
    }
    @Test void originalXSetterReentrantSamePlayerCannotDoubleWrite() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189FlightModule m=flight(i);final Player p=new Player();
        p.x=1D;
        p.onWriteX=()->m.apply(p,yaw());
        m.apply(p,yaw());
        assertEquals(1,p.wx);assertEquals(1,p.wz);
    }
    @Test void differentPlayerInsideXSetterPreventsStaleZWrite() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189FlightModule m=flight(i);
        final Player a=new Player(),b=new Player();a.x=1D;
        a.onWriteX=()->m.apply(b,yaw());
        m.apply(a,yaw());
        assertEquals(1,a.wx);assertEquals(0,a.wz+b.ry+b.rx+b.rz+b.wy+b.wx+b.wz);
    }
    @Test void originalZSetterReentrantSamePlayerNeverWritesAgain() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189FlightModule m=flight(i);final Player p=new Player();
        p.onWriteZ=()->m.apply(p,yaw());
        m.apply(p,yaw());
        assertEquals(1,p.wz);assertEquals(1,p.rx);
    }
    @Test void originalZSetterOtherPlayerNeverBorrowsNativeAxes() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189FlightModule m=flight(i);
        final Player a=new Player(),b=new Player();
        a.onWriteZ=()->m.apply(b,yaw());
        m.apply(a,yaw());
        assertEquals(1,a.wz);assertEquals(0,b.ry+b.rx+b.rz+b.wy+b.wx+b.wz);
    }
    @Test void nativeYReadThrowReleasesGuardForNextRealSourceCallback() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189FlightModule m=flight(i);final Player p=new Player();
        p.throwYRead=true;
        assertThrows(IllegalStateException.class,()->m.apply(p,yaw()));
        p.throwYRead=false;m.apply(p,yaw());
        assertEquals(1,p.wy);assertEquals(1,p.wz);
    }
    @Test void nativeYSetterThrowPreservesRealWriteAndReleasesGuard() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189FlightModule m=flight(i);final Player p=new Player();
        p.throwYWrite=true;
        assertThrows(IllegalStateException.class,()->m.apply(p,yaw()));
        assertEquals(0D,p.y,1E-12);assertEquals(0,p.wz);
        p.throwYWrite=false;p.y=.42D;
        m.apply(p,yaw());assertEquals(2,p.wy);assertEquals(1,p.wz);
    }
    @Test void nativeXGetterThrowReleasesGuardForNextRealMotion() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189FlightModule m=flight(i);final Player p=new Player();
        p.throwXRead=true;
        assertThrows(IllegalStateException.class,()->m.apply(p,yaw()));
        p.throwXRead=false;m.apply(p,yaw());
        assertEquals(1,p.wz);
    }
    @Test void nativeXSetterThrowNeverRollsBackActualPlayerX() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189FlightModule m=flight(i);final Player p=new Player();
        p.x=1D;p.throwXWrite=true;
        assertThrows(IllegalStateException.class,()->m.apply(p,yaw()));
        assertEquals(0D,p.x,1E-12);assertEquals(0,p.wz);
        p.throwXWrite=false;m.apply(p,yaw());assertEquals(1,p.wz);
    }
    @Test void originalGuiOwnsAllMotionWithoutEvenReadingY() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189FlightModule m=flight(i);final Player p=new Player();
        m.apply(p,yaw(),null,true);
        assertEquals(0,p.ry+p.rx+p.rz+p.wy+p.wx+p.wz);
    }
    @Test void physicalSpaceAndShiftStillSetVanillaVertical() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189FlightModule m=flight(i);final Player p=new Player();
        i.key(LegacyKeyboardCodes.SPACE,true);
        m.apply(p,yaw());assertEquals(.3D,p.y,1E-12);
        i.key(LegacyKeyboardCodes.SPACE,false);
        i.key(LegacyKeyboardCodes.LEFT_SHIFT,true);
        m.apply(p,yaw());assertEquals(-.3D,p.y,1E-12);
    }
    @Test void directAndSmoothVerticalStillUseOriginalSourceSpeed() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189FlightModule m=flight(i);final Player p=new Player();
        m.smoothVerticalSetting().set(Boolean.TRUE);
        m.verticalStepSetting().set(.10D);
        m.apply(p,yaw());assertEquals(.32D,p.y,1E-12);
        m.apply(p,yaw());assertEquals(.22D,p.y,1E-12);
    }
    @Test void sprintBoostUsesKnownGenuineSprintingOnly() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189FlightModule m=flight(i);final Player p=new Player();
        m.sprintBoostSetting().set(Boolean.TRUE);
        m.sprintMultiplierSetting().set(2D);
        final Minecraft189PlayerMovementState state=new Minecraft189PlayerMovementState();
        m.apply(p,yaw(),state.snapshot());
        assertEquals(.3D,p.z,1E-12);
        state.update(true,false,true);
        m.apply(p,yaw(),state.snapshot());assertEquals(.6D,p.z,1E-12);
    }
    @Test void YSourcePolicyEditedInsideNativeGetterVetoesStaleWrite() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189FlightModule m=flight(i);final Player p=new Player();
        p.onReadY=()->m.verticalSpeedSetting().set(.45D);
        m.apply(p,yaw());
        assertEquals(0,p.wy);
    }
    @Test void XSourcePolicyEditedInsideNativeGetterVetoesXZ() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189FlightModule m=flight(i);final Player p=new Player();
        p.onReadX=()->m.horizontalSpeedSetting().set(.45D);
        m.apply(p,yaw());
        assertEquals(0,p.wx+p.wz);
    }
}
