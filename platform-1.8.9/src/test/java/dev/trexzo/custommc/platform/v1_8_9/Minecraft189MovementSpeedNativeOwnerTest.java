package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Offline genuine-shaped adapter. Full Mojang original suite is separate. */
final class Minecraft189MovementSpeedNativeOwnerTest {
    private static Minecraft189PlayerMovementState.Snapshot ground(boolean yes) {
        final Minecraft189PlayerMovementState s = new Minecraft189PlayerMovementState();
        s.update(yes,false,false);return s.snapshot();
    }
    private static Minecraft189PlayerRotationState.Snapshot rotation(float yaw) {
        final Minecraft189PlayerRotationState r=new Minecraft189PlayerRotationState();
        r.update(yaw);return r.snapshot();
    }
    private static final class Player implements Minecraft189PlayerMotionControl {
        double x,z;
        int rx,rz,wx,wz;
        Runnable onReadX,onReadZ,onWriteX,onWriteZ;
        boolean throwReadX,throwWriteX;
        @Override public double customMcMotionY(){return 0D;}
        @Override public void customMcSetMotionY(double y){}
        @Override public double customMcMotionX() {
            rx++;if(rx>8)throw new AssertionError("recursive native X read");
            final Runnable r=onReadX;onReadX=null;if(r!=null)r.run();
            if(throwReadX)throw new IllegalStateException("native X getter");
            return x;
        }
        @Override public double customMcMotionZ() {
            rz++;if(rz>8)throw new AssertionError("recursive native Z read");
            final Runnable r=onReadZ;onReadZ=null;if(r!=null)r.run();
            return z;
        }
        @Override public void customMcSetMotionX(double value) {
            wx++;if(wx>8)throw new AssertionError("recursive native X setter");
            x=value;
            final Runnable r=onWriteX;onWriteX=null;if(r!=null)r.run();
            if(throwWriteX)throw new IllegalStateException("native X setter");
        }
        @Override public void customMcSetMotionZ(double value) {
            wz++;if(wz>8)throw new AssertionError("recursive native Z setter");
            z=value;
            final Runnable r=onWriteZ;onWriteZ=null;if(r!=null)r.run();
        }
    }
    private static Minecraft189MovementSpeedModule module(Minecraft189InputState input) {
        input.key(LegacyKeyboardCodes.W,true);
        final Minecraft189MovementSpeedModule m=new Minecraft189MovementSpeedModule(input);
        m.onEnable();return m;
    }
    private static boolean go(Minecraft189MovementSpeedModule m,Player p) {
        return m.apply(p,ground(true),rotation(0F),false);
    }
    @Test void originalGroundForwardAppliesExactNativeMotionZ() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189MovementSpeedModule m=module(i);final Player p=new Player();
        assertTrue(go(m,p));
        assertEquals(.45D,p.z,1E-12);
        assertEquals(1,p.rx);assertEquals(1,p.rz);assertEquals(1,p.wz);
    }
    @Test void nativeXGetterReentrySamePlayerCannotDoubleRead() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189MovementSpeedModule m=module(i);final Player p=new Player();
        p.onReadX=()->assertFalse(go(m,p));
        assertTrue(go(m,p));assertEquals(1,p.rx);assertEquals(1,p.rz);
    }
    @Test void nativeZGetterReentrySamePlayerCannotDoubleRead() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189MovementSpeedModule m=module(i);final Player p=new Player();
        p.onReadZ=()->assertFalse(go(m,p));
        assertTrue(go(m,p));assertEquals(1,p.rx);assertEquals(1,p.rz);
    }
    @Test void nativeXSetterReentrySamePlayerCannotDoubleWrite() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189MovementSpeedModule m=module(i);final Player p=new Player();
        p.x=1D;p.onWriteX=()->assertFalse(go(m,p));
        assertTrue(go(m,p));assertEquals(1,p.wx);assertEquals(1,p.wz);
    }
    @Test void nativeZSetterReentrySamePlayerCannotDoubleWrite() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189MovementSpeedModule m=module(i);final Player p=new Player();
        p.onWriteZ=()->assertFalse(go(m,p));
        assertTrue(go(m,p));assertEquals(1,p.wz);assertEquals(1,p.rx);
    }
    @Test void anotherOriginalPlayerInsideXGetterRevokesBothAxes() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189MovementSpeedModule m=module(i);
        final Player a=new Player(),b=new Player();
        a.onReadX=()->assertFalse(go(m,b));
        assertFalse(go(m,a));
        assertEquals(0,a.rz+a.wx+a.wz+b.rx+b.rz+b.wx+b.wz);
    }
    @Test void anotherOriginalPlayerInsideZGetterRevokesWrites() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189MovementSpeedModule m=module(i);
        final Player a=new Player(),b=new Player();
        a.onReadZ=()->assertFalse(go(m,b));
        assertFalse(go(m,a));
        assertEquals(0,a.wx+a.wz+b.rx+b.rz+b.wx+b.wz);
    }
    @Test void anotherOriginalPlayerInsideXSetterPreventsStaleZ() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189MovementSpeedModule m=module(i);
        final Player a=new Player(),b=new Player();a.x=1D;
        a.onWriteX=()->assertFalse(go(m,b));
        assertFalse(go(m,a));
        assertEquals(1,a.wx);assertEquals(0,a.wz+b.rx+b.rz+b.wx+b.wz);
    }
    @Test void anotherOriginalPlayerInsideZSetterRevokesCompletion() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189MovementSpeedModule m=module(i);
        final Player a=new Player(),b=new Player();
        a.onWriteZ=()->assertFalse(go(m,b));
        assertFalse(go(m,a));
        assertEquals(1,a.wz);assertEquals(0,b.rx+b.rz+b.wx+b.wz);
    }
    @Test void nestedSuspendedHostDuringXGetterRevokesGroundedSpeed() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189MovementSpeedModule m=module(i);final Player p=new Player();
        p.onReadX=()->assertFalse(m.apply(p,ground(true),rotation(0F),true));
        assertFalse(go(m,p));assertEquals(0,p.rz+p.wx+p.wz);
    }
    @Test void nestedAirborneStateDuringXGetterRevokesGroundedSpeed() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189MovementSpeedModule m=module(i);final Player p=new Player();
        p.onReadX=()->assertFalse(m.apply(p,ground(false),rotation(0F),false));
        assertFalse(go(m,p));assertEquals(0,p.rz+p.wx+p.wz);
    }
    @Test void nestedUnavailableGroundEvidenceRevokesNativeMotion() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189MovementSpeedModule m=module(i);final Player p=new Player();
        final Minecraft189PlayerMovementState absent=new Minecraft189PlayerMovementState();
        p.onReadX=()->assertFalse(m.apply(p,absent.snapshot(),rotation(0F),false));
        assertFalse(go(m,p));assertEquals(0,p.rz+p.wx+p.wz);
    }
    @Test void nestedMissingRotationRevokesNativeMotion() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189MovementSpeedModule m=module(i);final Player p=new Player();
        final Minecraft189PlayerRotationState absent=new Minecraft189PlayerRotationState();
        p.onReadX=()->assertFalse(m.apply(p,ground(true),absent.snapshot(),false));
        assertFalse(go(m,p));assertEquals(0,p.rz+p.wx+p.wz);
    }
    @Test void nativeGetterDisableVetoesOldNativeWrite() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189MovementSpeedModule m=module(i);final Player p=new Player();
        p.onReadX=m::onDisable;
        assertFalse(go(m,p));assertEquals(0,p.rz+p.wx+p.wz);
    }
    @Test void nativeGetterRestartCannotSpendNewActivation() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189MovementSpeedModule m=module(i);final Player p=new Player();
        p.onReadX=()->{m.onDisable();m.onEnable();assertFalse(go(m,p));};
        assertFalse(go(m,p));assertEquals(0,p.rz+p.wx+p.wz);
        assertTrue(go(m,p));assertEquals(1,p.wz);
    }
    @Test void speedGuiEditInsideXGetterCannotAuthorizeOldZ() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189MovementSpeedModule m=module(i);final Player p=new Player();
        p.onReadX=()->m.speedSetting().set(.7D);
        assertFalse(go(m,p));assertEquals(0,p.rz+p.wx+p.wz);
    }
    @Test void smoothingGuiEditInsideZGetterRevokesOldTarget() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189MovementSpeedModule m=module(i);final Player p=new Player();
        p.onReadZ=()->m.smoothAccelerationSetting().set(Boolean.TRUE);
        assertFalse(go(m,p));assertEquals(0,p.wx+p.wz);
    }
    @Test void physicalWReleasedDuringGetterRevokesOldTarget() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189MovementSpeedModule m=module(i);final Player p=new Player();
        p.onReadX=()->i.key(LegacyKeyboardCodes.W,false);
        assertFalse(go(m,p));assertEquals(0,p.rz+p.wx+p.wz);
    }
    @Test void mappedGetterExceptionReleasesNativeOwnership() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189MovementSpeedModule m=module(i);final Player p=new Player();
        p.throwReadX=true;
        assertThrows(IllegalStateException.class,()->go(m,p));
        p.throwReadX=false;assertTrue(go(m,p));assertEquals(1,p.wz);
    }
    @Test void mappedXSetterExceptionPreservesRealPartialWrite() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189MovementSpeedModule m=module(i);final Player p=new Player();
        p.x=1D;p.throwWriteX=true;
        assertThrows(IllegalStateException.class,()->go(m,p));
        assertEquals(0D,p.x,1E-12);assertEquals(0,p.wz);
        p.throwWriteX=false;assertTrue(go(m,p));assertEquals(.45D,p.z,1E-12);
    }
    @Test void originalSourceSmoothingUsesCurrentVanillaVelocity() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189MovementSpeedModule m=module(i);final Player p=new Player();
        m.smoothAccelerationSetting().set(Boolean.TRUE);
        m.accelerationPercentSetting().set(50);
        assertTrue(go(m,p));assertEquals(.225D,p.z,1E-12);
        assertTrue(go(m,p));assertEquals(.3375D,p.z,1E-12);
    }
    @Test void invalidOriginalVelocityRecoversToConfiguredTarget() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189MovementSpeedModule m=module(i);final Player p=new Player();
        m.smoothAccelerationSetting().set(Boolean.TRUE);
        p.x=Double.NaN;p.z=Double.POSITIVE_INFINITY;
        assertTrue(go(m,p));assertEquals(0D,p.x,1E-12);
        assertEquals(.45D,p.z,1E-12);
    }
    @Test void originalAirborneGroundGateNeverReadsNativeVelocity() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189MovementSpeedModule m=module(i);final Player p=new Player();
        assertFalse(m.apply(p,ground(false),rotation(0F),false));
        assertEquals(0,p.rx+p.rz+p.wx+p.wz);
    }
    @Test void absentRealWASDNeverCreatesSyntheticOriginalVelocity() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189MovementSpeedModule m=module(i);final Player p=new Player();
        i.key(LegacyKeyboardCodes.W,false);
        assertFalse(go(m,p));assertEquals(0,p.rx+p.rz+p.wx+p.wz);
    }
    @Test void realRotationSourceRejectsNonfiniteYawBeforeNativeMotion() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189MovementSpeedModule m=module(i);final Player p=new Player();
        // Minecraft189PlayerRotationState is the already-verified original
        // source boundary. It rejects NaN before a Snapshot exists.
        assertThrows(IllegalArgumentException.class,
                () -> rotation(Float.NaN));
        final Minecraft189PlayerRotationState unknown =
                new Minecraft189PlayerRotationState();
        assertFalse(m.apply(p,ground(true),unknown.snapshot(),false));
        assertEquals(0,p.rx+p.rz+p.wx+p.wz);
    }
    @Test void diagonalWASDStillNormalizesOriginalSpeed() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189MovementSpeedModule m=module(i);final Player p=new Player();
        i.key(LegacyKeyboardCodes.A,true);
        assertTrue(go(m,p));
        assertEquals(.45D/Math.sqrt(2D),p.x,1E-10);
        assertEquals(.45D/Math.sqrt(2D),p.z,1E-10);
    }
}
