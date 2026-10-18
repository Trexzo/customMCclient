package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Original Java native movement adapter reentrancy policy, not a game stub. */
final class Minecraft189LowLongJumpNativeLeaseTest {
    private static final class NativePlayer
            implements Minecraft189PlayerJumpControl, Minecraft189PlayerMotionControl {
        double x, y = 0.42D, z;
        int jumps, rx, ry, rz, wx, wy, wz;
        Runnable onJump, onXRead, onYRead, onZRead, onXWrite;
        @Override public void customMcJump() {
            jumps++;
            if (onJump != null) {
                final Runnable a=onJump; onJump=null; a.run();
            }
        }
        @Override public double customMcMotionX() {
            rx++;
            final double old=x;
            if (onXRead != null) {
                final Runnable a=onXRead; onXRead=null; a.run();
            }
            return old;
        }
        @Override public double customMcMotionY() {
            ry++;
            final double old=y;
            if (onYRead != null) {
                final Runnable a=onYRead; onYRead=null; a.run();
            }
            return old;
        }
        @Override public double customMcMotionZ() {
            rz++;
            final double old=z;
            if (onZRead != null) {
                final Runnable a=onZRead; onZRead=null; a.run();
            }
            return old;
        }
        @Override public void customMcSetMotionX(double value) {
            wx++;x=value;
            if (onXWrite != null) {
                final Runnable a=onXWrite;onXWrite=null;a.run();
            }
        }
        @Override public void customMcSetMotionY(double value) { wy++;y=value; }
        @Override public void customMcSetMotionZ(double value) { wz++;z=value; }
    }
    private static Minecraft189PlayerMovementState.Snapshot grounded() {
        final Minecraft189PlayerMovementState state=new Minecraft189PlayerMovementState();
        state.update(true,false,false);
        return state.snapshot();
    }
    private static Minecraft189PlayerRotationState.Snapshot yawZero() {
        final Minecraft189PlayerRotationState state=new Minecraft189PlayerRotationState();
        state.update(0F);
        return state.snapshot();
    }

    @Test void lowHopReentrantJumpOffOnCannotQueueOldYAdjustment() {
        final Minecraft189InputState input=new Minecraft189InputState();
        final Minecraft189LowHopModule m=new Minecraft189LowHopModule(input);
        final NativePlayer p=new NativePlayer();
        m.onEnable();input.key(LegacyKeyboardCodes.SPACE,true);
        p.onJump=()->{m.onDisable();m.onEnable();};
        assertFalse(m.applyJump(p,grounded(),false));
        assertEquals(1,p.jumps);
        assertFalse(m.applyMotion(p,false));
        assertEquals(0,p.ry);
        m.onDisable();
    }

    @Test void lowHopMidNativeYGetterRestartCannotLowerNewOwnersVelocity() {
        final Minecraft189InputState input=new Minecraft189InputState();
        final Minecraft189LowHopModule m=new Minecraft189LowHopModule(input);
        final NativePlayer p=new NativePlayer();
        m.onEnable();input.key(LegacyKeyboardCodes.SPACE,true);
        assertTrue(m.applyJump(p,grounded(),false));
        p.onYRead=()->{m.onDisable();m.onEnable();};
        assertFalse(m.applyMotion(p,false));
        assertEquals(0,p.wy);
        assertFalse(m.applyMotion(p,false));
        m.onDisable();
    }

    @Test void lowHopLiveVerticalSpeedChangeDuringGetterRevokesStaleY() {
        final Minecraft189InputState input=new Minecraft189InputState();
        final Minecraft189LowHopModule m=new Minecraft189LowHopModule(input);
        final NativePlayer p=new NativePlayer();
        m.onEnable();input.key(LegacyKeyboardCodes.SPACE,true);
        assertTrue(m.applyJump(p,grounded(),false));
        p.onYRead=()->m.verticalSpeedSetting().set(0.30D);
        assertFalse(m.applyMotion(p,false));
        assertEquals(0,p.wy);
        m.onDisable();
    }

    @Test void lowHopJumpReleasesOriginalSpaceNewRealPressCanJumpAgain() {
        final Minecraft189InputState input=new Minecraft189InputState();
        final Minecraft189LowHopModule m=new Minecraft189LowHopModule(input);
        final NativePlayer p=new NativePlayer();
        m.onEnable();input.key(LegacyKeyboardCodes.SPACE,true);
        p.onJump=()->input.key(LegacyKeyboardCodes.SPACE,false);
        assertTrue(m.applyJump(p,grounded(),false));
        input.key(LegacyKeyboardCodes.SPACE,true);
        assertTrue(m.applyJump(p,grounded(),false));
        assertEquals(2,p.jumps);
        m.onDisable();
    }

    @Test void lowHopValidOriginalYAppliesOnceAndKeepsVanillaLowerValue() {
        final Minecraft189InputState input=new Minecraft189InputState();
        final Minecraft189LowHopModule m=new Minecraft189LowHopModule(input);
        final NativePlayer p=new NativePlayer();
        m.onEnable();input.key(LegacyKeyboardCodes.SPACE,true);
        assertTrue(m.applyJump(p,grounded(),false));
        assertTrue(m.applyMotion(p,false));
        assertEquals(0.25D,p.y,0.00000001D);
        assertEquals(1,p.wy);
        assertFalse(m.applyMotion(p,false));
        m.onDisable();
    }

    @Test void longJumpReentrantOriginalJumpCannotQueueOldXZ() {
        final Minecraft189InputState input=new Minecraft189InputState();
        final Minecraft189LongJumpModule m=new Minecraft189LongJumpModule(input);
        final NativePlayer p=new NativePlayer();
        m.onEnable();input.key(LegacyKeyboardCodes.W,true);
        input.key(LegacyKeyboardCodes.SPACE,true);
        p.onJump=()->{m.onDisable();m.onEnable();};
        m.applyJump(p,grounded(),false);
        assertEquals(1,p.jumps);
        assertFalse(m.applyMotion(p,yawZero(),false));
        assertEquals(0,p.rx+p.rz);
        m.onDisable();
    }

    @Test void longJumpXGetterReentrantOffOnSkipsZAndAllOldWrites() {
        final Minecraft189InputState input=new Minecraft189InputState();
        final Minecraft189LongJumpModule m=new Minecraft189LongJumpModule(input);
        final NativePlayer p=new NativePlayer();
        m.onEnable();input.key(LegacyKeyboardCodes.W,true);
        input.key(LegacyKeyboardCodes.SPACE,true);
        m.applyJump(p,grounded(),false);
        p.onXRead=()->{m.onDisable();m.onEnable();};
        assertFalse(m.applyMotion(p,yawZero(),false));
        assertEquals(1,p.rx);
        assertEquals(0,p.rz+p.wx+p.wz);
        m.onDisable();
    }

    @Test void longJumpZGetterSpeedPolicyEditRejectsBothSourceWrites() {
        final Minecraft189InputState input=new Minecraft189InputState();
        final Minecraft189LongJumpModule m=new Minecraft189LongJumpModule(input);
        final NativePlayer p=new NativePlayer();
        m.onEnable();input.key(LegacyKeyboardCodes.W,true);
        input.key(LegacyKeyboardCodes.A,true);
        input.key(LegacyKeyboardCodes.SPACE,true);
        m.applyJump(p,grounded(),false);
        p.onZRead=()->m.speedSetting().set(0.85D);
        assertFalse(m.applyMotion(p,yawZero(),false));
        assertEquals(1,p.rx);
        assertEquals(1,p.rz);
        assertEquals(0,p.wx+p.wz);
        m.onDisable();
    }

    @Test void longJumpXGetterPhysicalWReleaseRejectsStaleXZ() {
        final Minecraft189InputState input=new Minecraft189InputState();
        final Minecraft189LongJumpModule m=new Minecraft189LongJumpModule(input);
        final NativePlayer p=new NativePlayer();
        m.onEnable();input.key(LegacyKeyboardCodes.W,true);
        input.key(LegacyKeyboardCodes.SPACE,true);
        m.applyJump(p,grounded(),false);
        p.onXRead=()->input.key(LegacyKeyboardCodes.W,false);
        assertFalse(m.applyMotion(p,yawZero(),false));
        assertEquals(0,p.rz+p.wx+p.wz);
        m.onDisable();
    }

    @Test void longJumpXSetterDisableDoesNotWriteOldZ() {
        final Minecraft189InputState input=new Minecraft189InputState();
        final Minecraft189LongJumpModule m=new Minecraft189LongJumpModule(input);
        final NativePlayer p=new NativePlayer();
        m.onEnable();input.key(LegacyKeyboardCodes.W,true);
        input.key(LegacyKeyboardCodes.A,true);
        input.key(LegacyKeyboardCodes.SPACE,true);
        m.applyJump(p,grounded(),false);
        p.onXWrite=m::onDisable;
        assertFalse(m.applyMotion(p,yawZero(),false));
        assertEquals(1,p.wx);
        assertEquals(0,p.wz);
        assertFalse(m.active());
    }

    @Test void longJumpPreserveRealHigherMomentumRequiresNoSetter() {
        final Minecraft189InputState input=new Minecraft189InputState();
        final Minecraft189LongJumpModule m=new Minecraft189LongJumpModule(input);
        final NativePlayer p=new NativePlayer();
        m.onEnable();m.preserveHigherMomentumSetting().set(Boolean.TRUE);
        input.key(LegacyKeyboardCodes.W,true);
        input.key(LegacyKeyboardCodes.SPACE,true);
        p.x=1.1D; p.z=0D;
        m.applyJump(p,grounded(),false);
        assertTrue(m.applyMotion(p,yawZero(),false));
        assertEquals(0,p.wx+p.wz);
        assertEquals(1.1D,p.x,0.00000001D);
        m.onDisable();
    }

    @Test void longJumpValidNativeWASDDiagonalNormalizesAndWritesXZ() {
        final Minecraft189InputState input=new Minecraft189InputState();
        final Minecraft189LongJumpModule m=new Minecraft189LongJumpModule(input);
        final NativePlayer p=new NativePlayer();
        m.onEnable();input.key(LegacyKeyboardCodes.W,true);
        input.key(LegacyKeyboardCodes.A,true);
        input.key(LegacyKeyboardCodes.SPACE,true);
        m.applyJump(p,grounded(),false);
        assertTrue(m.applyMotion(p,yawZero(),false));
        assertEquals(0.65D/Math.sqrt(2D),p.x,0.00000001D);
        assertEquals(0.65D/Math.sqrt(2D),p.z,0.00000001D);
        assertEquals(1,p.wx);
        assertEquals(1,p.wz);
        assertFalse(m.applyMotion(p,yawZero(),false));
        m.onDisable();
    }
}
