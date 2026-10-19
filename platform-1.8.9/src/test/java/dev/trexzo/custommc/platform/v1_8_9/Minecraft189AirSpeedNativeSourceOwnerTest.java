package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Original-shaped native adapter: offline, not genuine game evidence. */
final class Minecraft189AirSpeedNativeSourceOwnerTest {
    private static Minecraft189PlayerMovementState.Snapshot movement(boolean ground) {
        final Minecraft189PlayerMovementState m=new Minecraft189PlayerMovementState();
        m.update(ground,false,false);return m.snapshot();
    }
    private static Minecraft189PlayerRotationState.Snapshot rotation() {
        final Minecraft189PlayerRotationState r=new Minecraft189PlayerRotationState();
        r.update(0F);return r.snapshot();
    }
    private static final class Player implements Minecraft189PlayerMotionControl {
        double x,z; int rx,rz,wx,wz;
        Runnable onReadX,onReadZ,onWriteX,onWriteZ;
        boolean throwReadX,throwWriteX;
        @Override public double customMcMotionY(){return 0D;}
        @Override public void customMcSetMotionY(double v){}
        @Override public double customMcMotionX() {
            rx++;if(rx>8)throw new AssertionError("recursively sampled X");
            final Runnable c=onReadX;onReadX=null;if(c!=null)c.run();
            if(throwReadX)throw new IllegalStateException("native X read");
            return x;
        }
        @Override public double customMcMotionZ() {
            rz++;if(rz>8)throw new AssertionError("recursively sampled Z");
            final Runnable c=onReadZ;onReadZ=null;if(c!=null)c.run();
            return z;
        }
        @Override public void customMcSetMotionX(double v) {
            wx++;if(wx>8)throw new AssertionError("recursively wrote X");
            x=v;
            final Runnable c=onWriteX;onWriteX=null;if(c!=null)c.run();
            if(throwWriteX)throw new IllegalStateException("native X setter");
        }
        @Override public void customMcSetMotionZ(double v) {
            wz++;if(wz>8)throw new AssertionError("recursively wrote Z");
            z=v;
            final Runnable c=onWriteZ;onWriteZ=null;if(c!=null)c.run();
        }
    }
    private static Minecraft189AirSpeedModule module(Minecraft189InputState input) {
        input.key(LegacyKeyboardCodes.W,true);
        final Minecraft189AirSpeedModule m=new Minecraft189AirSpeedModule(input);
        m.onEnable();return m;
    }
    private static boolean apply(Minecraft189AirSpeedModule m, Player p) {
        return m.apply(p,movement(false),rotation(),false);
    }
    @Test void originalAirborneWProducesVanillaForwardZ() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189AirSpeedModule m=module(i);final Player p=new Player();
        assertTrue(apply(m,p));assertEquals(.35D,p.z,1E-12);
        assertEquals(1,p.rx);assertEquals(1,p.rz);assertEquals(1,p.wz);
    }
    @Test void nestedSamePlayerXGetterDoesNotSpendSecondSource() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189AirSpeedModule m=module(i);final Player p=new Player();
        p.onReadX=()->assertFalse(apply(m,p));
        assertTrue(apply(m,p));assertEquals(1,p.rx);assertEquals(1,p.rz);
    }
    @Test void nestedSamePlayerZGetterDoesNotSpendSecondSource() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189AirSpeedModule m=module(i);final Player p=new Player();
        p.onReadZ=()->assertFalse(apply(m,p));
        assertTrue(apply(m,p));assertEquals(1,p.rx);assertEquals(1,p.rz);
    }
    @Test void nestedSamePlayerXSetterCannotDoubleWrite() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189AirSpeedModule m=module(i);final Player p=new Player();p.x=1D;
        p.onWriteX=()->assertFalse(apply(m,p));
        assertTrue(apply(m,p));assertEquals(1,p.wx);assertEquals(1,p.wz);
    }
    @Test void nestedSamePlayerZSetterCannotDoubleWrite() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189AirSpeedModule m=module(i);final Player p=new Player();
        p.onWriteZ=()->assertFalse(apply(m,p));
        assertTrue(apply(m,p));assertEquals(1,p.wz);assertEquals(1,p.rx);
    }
    @Test void anotherOriginalPlayerDuringXGetterRevokesBothAxes() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189AirSpeedModule m=module(i);
        final Player a=new Player(),b=new Player();
        a.onReadX=()->assertFalse(apply(m,b));
        assertFalse(apply(m,a));
        assertEquals(0,a.rz+a.wx+a.wz+b.rx+b.rz+b.wx+b.wz);
    }
    @Test void anotherOriginalPlayerDuringZGetterRevokesBothWrites() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189AirSpeedModule m=module(i);
        final Player a=new Player(),b=new Player();
        a.onReadZ=()->assertFalse(apply(m,b));
        assertFalse(apply(m,a));
        assertEquals(0,a.wx+a.wz+b.rx+b.rz+b.wx+b.wz);
    }
    @Test void anotherOriginalPlayerDuringXSetterCannotWriteStaleZ() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189AirSpeedModule m=module(i);
        final Player a=new Player(),b=new Player();a.x=1D;
        a.onWriteX=()->assertFalse(apply(m,b));
        assertFalse(apply(m,a));
        assertEquals(1,a.wx);assertEquals(0,a.wz+b.rx+b.rz+b.wx+b.wz);
    }
    @Test void anotherOriginalPlayerDuringZSetterRevokesCompletion() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189AirSpeedModule m=module(i);
        final Player a=new Player(),b=new Player();
        a.onWriteZ=()->assertFalse(apply(m,b));
        assertFalse(apply(m,a));
        assertEquals(1,a.wz);assertEquals(0,b.rx+b.rz+b.wx+b.wz);
    }
    @Test void suspendedNestedHostDuringXGetterRevokesWrite() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189AirSpeedModule m=module(i);final Player p=new Player();
        p.onReadX=()->assertFalse(m.apply(p,movement(false),rotation(),true));
        assertFalse(apply(m,p));assertEquals(0,p.rz+p.wx+p.wz);
    }
    @Test void originalLandingDuringXGetterRevokesAirborneAuthority() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189AirSpeedModule m=module(i);final Player p=new Player();
        p.onReadX=()->assertFalse(m.apply(p,movement(true),rotation(),false));
        assertFalse(apply(m,p));assertEquals(0,p.rz+p.wx+p.wz);
    }
    @Test void unknownOriginalMovementDuringXGetterRevokesAuthority() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189AirSpeedModule m=module(i);final Player p=new Player();
        final Minecraft189PlayerMovementState unknown=new Minecraft189PlayerMovementState();
        p.onReadX=()->assertFalse(m.apply(p,unknown.snapshot(),rotation(),false));
        assertFalse(apply(m,p));assertEquals(0,p.rz+p.wx+p.wz);
    }
    @Test void lostMappedRotationDuringXGetterRevokesAuthority() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189AirSpeedModule m=module(i);final Player p=new Player();
        final Minecraft189PlayerRotationState unknown=new Minecraft189PlayerRotationState();
        p.onReadX=()->assertFalse(m.apply(p,movement(false),unknown.snapshot(),false));
        assertFalse(apply(m,p));assertEquals(0,p.rz+p.wx+p.wz);
    }
    @Test void originalModuleOffOnInGetterInvalidatesOldSource() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189AirSpeedModule m=module(i);final Player p=new Player();
        p.onReadX=()->{m.onDisable();m.onEnable();assertFalse(apply(m,p));};
        assertFalse(apply(m,p));assertEquals(0,p.rz+p.wx+p.wz);
        assertTrue(apply(m,p));assertEquals(1,p.wz);
    }
    @Test void nativeGetterLiveSpeedEditRevokesOldAxis() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189AirSpeedModule m=module(i);final Player p=new Player();
        p.onReadX=()->m.speedSetting().set(.6D);
        assertFalse(apply(m,p));assertEquals(0,p.rz+p.wx+p.wz);
    }
    @Test void nativeGetterRealWReleaseRevokesOldAxis() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189AirSpeedModule m=module(i);final Player p=new Player();
        p.onReadX=()->i.key(LegacyKeyboardCodes.W,false);
        assertFalse(apply(m,p));assertEquals(0,p.rz+p.wx+p.wz);
    }
    @Test void nativeGetterThrowReleasesGuardForNextSource() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189AirSpeedModule m=module(i);final Player p=new Player();
        p.throwReadX=true;
        assertThrows(IllegalStateException.class,()->apply(m,p));
        p.throwReadX=false;
        assertTrue(apply(m,p));assertEquals(1,p.wz);
    }
    @Test void nativeSetterThrowDoesNotUndoOriginalPartialX() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189AirSpeedModule m=module(i);final Player p=new Player();
        p.x=1D;p.throwWriteX=true;
        assertThrows(IllegalStateException.class,()->apply(m,p));
        assertEquals(0D,p.x,1E-12);assertEquals(0,p.wz);
        p.throwWriteX=false;assertTrue(apply(m,p));
        assertEquals(.35D,p.z,1E-12);
    }
    @Test void originalSmoothingPreservesRealVelocityInterpolation() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189AirSpeedModule m=module(i);final Player p=new Player();
        m.smoothAccelerationSetting().set(Boolean.TRUE);
        m.accelerationPercentSetting().set(50);
        assertTrue(apply(m,p));assertEquals(.175D,p.z,1E-12);
        assertTrue(apply(m,p));assertEquals(.2625D,p.z,1E-12);
    }
    @Test void nonfiniteOriginalVelocityPreservesEstablishedRepairContract() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189AirSpeedModule m=module(i);final Player p=new Player();
        m.smoothAccelerationSetting().set(Boolean.TRUE);
        p.z=Double.POSITIVE_INFINITY;p.x=Double.NaN;
        assertTrue(apply(m,p));assertEquals(0D,p.x,1E-12);
        assertEquals(.35D,p.z,1E-12);
    }
    @Test void originalKnownGroundForbidsAnyAirSpeedRead() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189AirSpeedModule m=module(i);final Player p=new Player();
        assertFalse(m.apply(p,movement(true),rotation(),false));
        assertEquals(0,p.rx+p.rz+p.wx+p.wz);
    }
    @Test void noPhysicalWasdForbidsSyntheticAirMovement() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189AirSpeedModule m=module(i);final Player p=new Player();
        i.key(LegacyKeyboardCodes.W,false);
        assertFalse(apply(m,p));
        assertEquals(0,p.rx+p.rz+p.wx+p.wz);
    }
}
