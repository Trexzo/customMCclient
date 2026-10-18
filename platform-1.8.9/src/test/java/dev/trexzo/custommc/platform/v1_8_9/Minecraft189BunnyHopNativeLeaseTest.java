package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Reentrant mapped source callback policy; real Mojang runtime separately. */
final class Minecraft189BunnyHopNativeLeaseTest {
    private static final class Player
            implements Minecraft189PlayerJumpControl, Minecraft189PlayerMotionControl {
        double x, y, z;
        int jumps, rx, rz, wx, wz;
        Runnable onJump, onReadX, onReadZ, onWriteX;
        boolean failJump;
        @Override public void customMcJump() {
            jumps++;
            if (onJump != null) {
                final Runnable f=onJump;onJump=null;f.run();
            }
            if (failJump) throw new IllegalStateException("mapped native jump");
        }
        @Override public double customMcMotionX() {
            rx++;
            final double v=x;
            if (onReadX != null) {
                final Runnable f=onReadX;onReadX=null;f.run();
            }
            return v;
        }
        @Override public double customMcMotionY() { return y; }
        @Override public double customMcMotionZ() {
            rz++;
            final double v=z;
            if (onReadZ != null) {
                final Runnable f=onReadZ;onReadZ=null;f.run();
            }
            return v;
        }
        @Override public void customMcSetMotionX(double v) {
            wx++; x=v;
            if (onWriteX != null) {
                final Runnable f=onWriteX;onWriteX=null;f.run();
            }
        }
        @Override public void customMcSetMotionY(double v) { y=v; }
        @Override public void customMcSetMotionZ(double v) { wz++;z=v; }
    }
    private static Minecraft189PlayerRotationState.Snapshot facingNorth() {
        final Minecraft189PlayerRotationState r=new Minecraft189PlayerRotationState();
        r.update(0F);
        return r.snapshot();
    }
    private static Minecraft189PlayerMovementState.Snapshot state(boolean ground) {
        final Minecraft189PlayerMovementState m=new Minecraft189PlayerMovementState();
        m.update(ground,false,false);
        return m.snapshot();
    }

    @Test void originalJumpOffOnDoesNotDisarmFreshBunnyHopOwner() {
        final Minecraft189InputState input=new Minecraft189InputState();
        final Minecraft189BunnyHopModule m=new Minecraft189BunnyHopModule(input);
        final Player p=new Player();
        input.key(LegacyKeyboardCodes.W,true);
        m.onEnable();
        p.onJump=()->{m.onDisable();m.onEnable();};
        m.applyJump(p,state(true),false);
        assertEquals(1,p.jumps);
        assertTrue(m.active());
        // Fresh module owner retains its own rearmed ground lease; an
        // obsolete old jump cannot set its armed=false after OFF->ON.
        m.applyJump(p,state(true),false);
        assertEquals(2,p.jumps);
        m.onDisable();
    }

    @Test void throwingOriginalJumpConsumesGroundLeaseWithoutReplay() {
        final Minecraft189InputState input=new Minecraft189InputState();
        final Minecraft189BunnyHopModule m=new Minecraft189BunnyHopModule(input);
        final Player p=new Player();
        input.key(LegacyKeyboardCodes.W,true);
        m.onEnable();
        p.failJump=true;
        assertThrows(IllegalStateException.class,
                () -> m.applyJump(p,state(true),false));
        p.failJump=false;
        m.applyJump(p,state(true),false);
        assertEquals(1,p.jumps);
        m.applyJump(p,state(false),false);
        m.applyJump(p,state(true),false);
        assertEquals(2,p.jumps);
        m.onDisable();
    }

    @Test void originalXGetterRestartSkipsZReadAndBothOldSetters() {
        final Minecraft189InputState input=new Minecraft189InputState();
        final Minecraft189BunnyHopModule m=new Minecraft189BunnyHopModule(input);
        final Player p=new Player();
        input.key(LegacyKeyboardCodes.W,true);
        m.onEnable();
        p.onReadX=()->{m.onDisable();m.onEnable();};
        assertFalse(m.applyMotion(p,facingNorth(),false));
        assertEquals(1,p.rx);
        assertEquals(0,p.rz+p.wx+p.wz);
        m.onDisable();
    }

    @Test void originalXGetterPhysicalKeyReleaseVetoesOldVelocity() {
        final Minecraft189InputState input=new Minecraft189InputState();
        final Minecraft189BunnyHopModule m=new Minecraft189BunnyHopModule(input);
        final Player p=new Player();
        input.key(LegacyKeyboardCodes.W,true);
        m.onEnable();
        p.onReadX=()->input.key(LegacyKeyboardCodes.W,false);
        assertFalse(m.applyMotion(p,facingNorth(),false));
        assertEquals(0,p.rz+p.wx+p.wz);
        m.onDisable();
    }

    @Test void originalZGetterSpeedEditRejectsBothOriginalWrites() {
        final Minecraft189InputState input=new Minecraft189InputState();
        final Minecraft189BunnyHopModule m=new Minecraft189BunnyHopModule(input);
        final Player p=new Player();
        input.key(LegacyKeyboardCodes.W,true);
        input.key(LegacyKeyboardCodes.A,true);
        m.onEnable();
        p.onReadZ=()->m.speedSetting().set(0.65D);
        assertFalse(m.applyMotion(p,facingNorth(),false));
        assertEquals(1,p.rx);
        assertEquals(1,p.rz);
        assertEquals(0,p.wx+p.wz);
        m.onDisable();
    }

    @Test void originalZGetterAccelerationPolicyEditRevokesOldSmoothing() {
        final Minecraft189InputState input=new Minecraft189InputState();
        final Minecraft189BunnyHopModule m=new Minecraft189BunnyHopModule(input);
        final Player p=new Player();
        input.key(LegacyKeyboardCodes.W,true);
        m.onEnable();
        m.smoothAccelerationSetting().set(Boolean.TRUE);
        p.onReadZ=()->m.accelerationPercentSetting().set(75);
        assertFalse(m.applyMotion(p,facingNorth(),false));
        assertEquals(0,p.wx+p.wz);
        m.onDisable();
    }

    @Test void originalXSetterDisableDoesNotIssueStaleZWrite() {
        final Minecraft189InputState input=new Minecraft189InputState();
        final Minecraft189BunnyHopModule m=new Minecraft189BunnyHopModule(input);
        final Player p=new Player();
        input.key(LegacyKeyboardCodes.W,true);
        input.key(LegacyKeyboardCodes.A,true);
        m.onEnable();
        p.onWriteX=m::onDisable;
        assertFalse(m.applyMotion(p,facingNorth(),false));
        assertEquals(1,p.wx);
        assertEquals(0,p.wz);
        assertFalse(m.active());
    }

    @Test void originalXSetterSmoothingEditSuppressesOldZ() {
        final Minecraft189InputState input=new Minecraft189InputState();
        final Minecraft189BunnyHopModule m=new Minecraft189BunnyHopModule(input);
        final Player p=new Player();
        input.key(LegacyKeyboardCodes.W,true);
        input.key(LegacyKeyboardCodes.A,true);
        m.onEnable();
        p.onWriteX=()->m.smoothAccelerationSetting().set(Boolean.TRUE);
        assertFalse(m.applyMotion(p,facingNorth(),false));
        assertEquals(1,p.wx);
        assertEquals(0,p.wz);
        m.onDisable();
    }

    @Test void validSourceDiagonalWritesNormalizedXZOnce() {
        final Minecraft189InputState input=new Minecraft189InputState();
        final Minecraft189BunnyHopModule m=new Minecraft189BunnyHopModule(input);
        final Player p=new Player();
        input.key(LegacyKeyboardCodes.W,true);
        input.key(LegacyKeyboardCodes.A,true);
        m.onEnable();
        assertTrue(m.applyMotion(p,facingNorth(),false));
        assertEquals(0.38D/Math.sqrt(2D),p.x,0.00000001D);
        assertEquals(0.38D/Math.sqrt(2D),p.z,0.00000001D);
        assertEquals(1,p.wx);
        assertEquals(1,p.wz);
        m.onDisable();
    }

    @Test void validSmoothAccelerationUsesCurrentMappedSourceMotion() {
        final Minecraft189InputState input=new Minecraft189InputState();
        final Minecraft189BunnyHopModule m=new Minecraft189BunnyHopModule(input);
        final Player p=new Player();
        input.key(LegacyKeyboardCodes.W,true);
        m.onEnable();
        m.smoothAccelerationSetting().set(Boolean.TRUE);
        m.accelerationPercentSetting().set(25);
        assertTrue(m.applyMotion(p,facingNorth(),false));
        assertEquals(0.095D,p.z,0.00000001D);
        assertTrue(m.applyMotion(p,facingNorth(),false));
        assertEquals(0.16625D,p.z,0.00000001D);
        m.onDisable();
    }

    @Test void realGuiSuspensionAndUnavailableRotationNeverTouchNativeVelocity() {
        final Minecraft189InputState input=new Minecraft189InputState();
        final Minecraft189BunnyHopModule m=new Minecraft189BunnyHopModule(input);
        final Player p=new Player();
        final Minecraft189PlayerRotationState none=new Minecraft189PlayerRotationState();
        input.key(LegacyKeyboardCodes.W,true);
        m.onEnable();
        assertFalse(m.applyMotion(p,facingNorth(),true));
        assertFalse(m.applyMotion(p,none.snapshot(),false));
        assertEquals(0,p.rx+p.rz+p.wx+p.wz);
        m.onDisable();
    }
}
