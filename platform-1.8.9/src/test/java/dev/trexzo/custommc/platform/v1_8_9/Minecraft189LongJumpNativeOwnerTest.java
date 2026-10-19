package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Offline mapped original adapter regressions, not authentic Mojang CI. */
final class Minecraft189LongJumpNativeOwnerTest {
    private static Minecraft189PlayerMovementState.Snapshot ground() {
        final Minecraft189PlayerMovementState s = new Minecraft189PlayerMovementState();
        s.update(true, false, false); return s.snapshot();
    }
    private static Minecraft189PlayerRotationState.Snapshot yaw(float value) {
        final Minecraft189PlayerRotationState s = new Minecraft189PlayerRotationState();
        s.update(value); return s.snapshot();
    }
    private static final class Player implements Minecraft189PlayerJumpControl,
            Minecraft189PlayerMotionControl {
        double x, y, z;
        int jumps, rx, rz, wx, wz;
        Runnable jumpCallback, readXCallback, readZCallback, writeXCallback,
                writeZCallback;
        boolean throwJump, throwReadX, throwWriteX;
        @Override public void customMcJump() {
            jumps++;
            if (jumps > 8) throw new AssertionError("recursive native jump");
            y = .42D;
            if (jumpCallback != null) {
                final Runnable c=jumpCallback; jumpCallback=null;c.run();
            }
            if (throwJump) throw new IllegalStateException("original jump");
        }
        @Override public double customMcMotionX() {
            rx++;
            if (rx > 8) throw new AssertionError("recursive original X read");
            if (readXCallback != null) {
                final Runnable c=readXCallback;readXCallback=null;c.run();
            }
            if (throwReadX) throw new IllegalStateException("original X read");
            return x;
        }
        @Override public double customMcMotionZ() {
            rz++;
            if (rz > 8) throw new AssertionError("recursive original Z read");
            if (readZCallback != null) {
                final Runnable c=readZCallback;readZCallback=null;c.run();
            }
            return z;
        }
        @Override public void customMcSetMotionX(double value) {
            wx++;
            if (wx > 8) throw new AssertionError("recursive original X setter");
            x=value;
            if (writeXCallback != null) {
                final Runnable c=writeXCallback;writeXCallback=null;c.run();
            }
            if (throwWriteX) throw new IllegalStateException("original X setter");
        }
        @Override public void customMcSetMotionZ(double value) {
            wz++;
            if (wz > 8) throw new AssertionError("recursive original Z setter");
            z=value;
            if (writeZCallback != null) {
                final Runnable c=writeZCallback;writeZCallback=null;c.run();
            }
        }
        @Override public double customMcMotionY(){return y;}
        @Override public void customMcSetMotionY(double value){y=value;}
    }
    private static void key(Minecraft189InputState input,int code,boolean state) {
        input.key(code,state);
    }
    private static void arm(Minecraft189InputState input,
            Minecraft189LongJumpModule m, Player player) {
        m.onEnable();
        key(input,LegacyKeyboardCodes.W,true);
        key(input,LegacyKeyboardCodes.SPACE,true);
        m.applyJump(player,ground(),false);
    }
    @Test void genuineSpaceEdgeWritesOriginalForwardZOnce() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LongJumpModule m=new Minecraft189LongJumpModule(i);
        final Player p=new Player();arm(i,m,p);
        assertEquals(1,p.jumps);
        assertTrue(m.applyMotion(p,yaw(0F),false));
        assertEquals(1,p.rx);assertEquals(1,p.rz);assertEquals(1,p.wz);
        assertEquals(0D,p.x,1E-12);assertEquals(.65D,p.z,1E-12);
    }
    @Test void nestedJumpSamePlayerCannotBorrowSpaceEdge() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LongJumpModule m=new Minecraft189LongJumpModule(i);
        final Player p=new Player();m.onEnable();
        key(i,LegacyKeyboardCodes.W,true);key(i,LegacyKeyboardCodes.SPACE,true);
        p.jumpCallback=()->m.applyJump(p,ground(),false);
        m.applyJump(p,ground(),false);
        assertEquals(1,p.jumps);assertTrue(m.applyMotion(p,yaw(0F),false));
    }
    @Test void nestedJumpOtherPlayerCannotBorrowSpaceEdge() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LongJumpModule m=new Minecraft189LongJumpModule(i);
        final Player a=new Player(),b=new Player();m.onEnable();
        key(i,LegacyKeyboardCodes.W,true);key(i,LegacyKeyboardCodes.SPACE,true);
        a.jumpCallback=()->m.applyJump(b,ground(),false);
        m.applyJump(a,ground(),false);
        assertEquals(1,a.jumps);assertEquals(0,b.jumps);
    }
    @Test void nativeJumpModuleRestartRevokesOldBoostAndNestedJump() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LongJumpModule m=new Minecraft189LongJumpModule(i);
        final Player p=new Player();m.onEnable();
        key(i,LegacyKeyboardCodes.W,true);key(i,LegacyKeyboardCodes.SPACE,true);
        p.jumpCallback=()->{m.onDisable();m.onEnable();
            m.applyJump(p,ground(),false);};
        m.applyJump(p,ground(),false);
        assertEquals(1,p.jumps);assertFalse(m.applyMotion(p,yaw(0F),false));
    }
    @Test void nativeJumpSpaceReleaseKeepsAcceptedHopAndNextEdge() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LongJumpModule m=new Minecraft189LongJumpModule(i);
        final Player p=new Player();m.onEnable();
        key(i,LegacyKeyboardCodes.W,true);key(i,LegacyKeyboardCodes.SPACE,true);
        p.jumpCallback=()->key(i,LegacyKeyboardCodes.SPACE,false);
        m.applyJump(p,ground(),false);
        assertEquals(1,p.jumps);assertTrue(m.applyMotion(p,yaw(0F),false));
        key(i,LegacyKeyboardCodes.SPACE,true);m.applyJump(p,ground(),false);
        assertEquals(2,p.jumps);
    }
    @Test void originalJumpThrowReleasesGuardWithNoSyntheticUnjump() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LongJumpModule m=new Minecraft189LongJumpModule(i);
        final Player p=new Player();m.onEnable();
        key(i,LegacyKeyboardCodes.W,true);key(i,LegacyKeyboardCodes.SPACE,true);
        p.throwJump=true;
        assertThrows(IllegalStateException.class,
            ()->m.applyJump(p,ground(),false));
        assertEquals(.42D,p.y,1E-12);
        p.throwJump=false;
        key(i,LegacyKeyboardCodes.SPACE,false);m.applyJump(p,ground(),false);
        key(i,LegacyKeyboardCodes.SPACE,true);m.applyJump(p,ground(),false);
        assertEquals(2,p.jumps);
    }
    @Test void nestedXGetterSamePlayerCannotReadAgain() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LongJumpModule m=new Minecraft189LongJumpModule(i);
        final Player p=new Player();arm(i,m,p);
        p.readXCallback=()->assertFalse(m.applyMotion(p,yaw(0F),false));
        assertTrue(m.applyMotion(p,yaw(0F),false));
        assertEquals(1,p.rx);assertEquals(1,p.rz);
    }
    @Test void nestedZGetterSamePlayerCannotReadAgain() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LongJumpModule m=new Minecraft189LongJumpModule(i);
        final Player p=new Player();arm(i,m,p);
        p.readZCallback=()->assertFalse(m.applyMotion(p,yaw(0F),false));
        assertTrue(m.applyMotion(p,yaw(0F),false));
        assertEquals(1,p.rx);assertEquals(1,p.rz);
    }
    @Test void nestedXSetterSamePlayerCannotWriteAgain() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LongJumpModule m=new Minecraft189LongJumpModule(i);
        final Player p=new Player();arm(i,m,p);p.x=1D;
        p.writeXCallback=()->assertFalse(m.applyMotion(p,yaw(0F),false));
        assertTrue(m.applyMotion(p,yaw(0F),false));
        assertEquals(1,p.wx);assertEquals(1,p.wz);
    }
    @Test void nestedZSetterSamePlayerCannotWriteAgain() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LongJumpModule m=new Minecraft189LongJumpModule(i);
        final Player p=new Player();arm(i,m,p);
        p.writeZCallback=()->assertFalse(m.applyMotion(p,yaw(0F),false));
        assertTrue(m.applyMotion(p,yaw(0F),false));
        assertEquals(1,p.wz);assertEquals(1,p.rx);
    }
    @Test void changedPlayerInOriginalXGetterCancelsXZ() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LongJumpModule m=new Minecraft189LongJumpModule(i);
        final Player a=new Player(),b=new Player();arm(i,m,a);
        a.readXCallback=()->assertFalse(m.applyMotion(b,yaw(0F),false));
        assertFalse(m.applyMotion(a,yaw(0F),false));
        assertEquals(0,a.rz+a.wx+a.wz+b.rx+b.rz+b.wx+b.wz);
    }
    @Test void changedPlayerInOriginalZGetterCancelsXZ() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LongJumpModule m=new Minecraft189LongJumpModule(i);
        final Player a=new Player(),b=new Player();arm(i,m,a);
        a.readZCallback=()->assertFalse(m.applyMotion(b,yaw(0F),false));
        assertFalse(m.applyMotion(a,yaw(0F),false));
        assertEquals(0,a.wx+a.wz+b.rx+b.rz+b.wx+b.wz);
    }
    @Test void changedPlayerInOriginalXSetterPreventsStaleZ() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LongJumpModule m=new Minecraft189LongJumpModule(i);
        final Player a=new Player(),b=new Player();arm(i,m,a);a.x=1D;
        a.writeXCallback=()->assertFalse(m.applyMotion(b,yaw(0F),false));
        assertFalse(m.applyMotion(a,yaw(0F),false));
        assertEquals(1,a.wx);assertEquals(0,a.wz+b.rx+b.rz+b.wx+b.wz);
    }
    @Test void changedPlayerInOriginalZSetterReturnsNotOwned() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LongJumpModule m=new Minecraft189LongJumpModule(i);
        final Player a=new Player(),b=new Player();arm(i,m,a);
        a.writeZCallback=()->assertFalse(m.applyMotion(b,yaw(0F),false));
        assertFalse(m.applyMotion(a,yaw(0F),false));
        assertEquals(1,a.wz);assertEquals(0,b.rx+b.rz+b.wx+b.wz);
    }
    @Test void originalHostSuspensionDuringXReadRevokesOldBoost() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LongJumpModule m=new Minecraft189LongJumpModule(i);
        final Player p=new Player();arm(i,m,p);
        p.readXCallback=()->assertFalse(m.applyMotion(p,yaw(0F),true));
        assertFalse(m.applyMotion(p,yaw(0F),false));
        assertEquals(0,p.wx+p.wz);
    }
    @Test void originalHostRotationLossDuringXReadRevokesOldBoost() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LongJumpModule m=new Minecraft189LongJumpModule(i);
        final Player p=new Player();arm(i,m,p);
        final Minecraft189PlayerRotationState none=new Minecraft189PlayerRotationState();
        p.readXCallback=()->assertFalse(m.applyMotion(p,none.snapshot(),false));
        assertFalse(m.applyMotion(p,yaw(0F),false));
        assertEquals(0,p.wx+p.wz);
    }
    @Test void originalXGetterDisableEnableCannotSpendOldLifetime() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LongJumpModule m=new Minecraft189LongJumpModule(i);
        final Player p=new Player();arm(i,m,p);
        p.readXCallback=()->{m.onDisable();m.onEnable();};
        assertFalse(m.applyMotion(p,yaw(0F),false));
        assertEquals(0,p.rz+p.wx+p.wz);
    }
    @Test void originalZGetterGuiSpeedEditVetoesWrites() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LongJumpModule m=new Minecraft189LongJumpModule(i);
        final Player p=new Player();arm(i,m,p);
        p.readZCallback=()->m.speedSetting().set(.9D);
        assertFalse(m.applyMotion(p,yaw(0F),false));
        assertEquals(0,p.wx+p.wz);
    }
    @Test void originalXGetterPhysicalWReleaseVetoesOldZ() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LongJumpModule m=new Minecraft189LongJumpModule(i);
        final Player p=new Player();arm(i,m,p);
        p.readXCallback=()->key(i,LegacyKeyboardCodes.W,false);
        assertFalse(m.applyMotion(p,yaw(0F),false));
        assertEquals(0,p.rz+p.wx+p.wz);
    }
    @Test void originalXGetterThrowReleasesGuardForNextActualJump() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LongJumpModule m=new Minecraft189LongJumpModule(i);
        final Player p=new Player();arm(i,m,p);p.throwReadX=true;
        assertThrows(IllegalStateException.class,
            ()->m.applyMotion(p,yaw(0F),false));
        p.throwReadX=false;
        key(i,LegacyKeyboardCodes.SPACE,false);m.applyJump(p,ground(),false);
        key(i,LegacyKeyboardCodes.SPACE,true);m.applyJump(p,ground(),false);
        assertEquals(2,p.jumps);assertTrue(m.applyMotion(p,yaw(0F),false));
    }
    @Test void originalXSetterPartialWriteThrowNeverForgedRollback() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LongJumpModule m=new Minecraft189LongJumpModule(i);
        final Player p=new Player();arm(i,m,p);p.x=1D;p.throwWriteX=true;
        assertThrows(IllegalStateException.class,
            ()->m.applyMotion(p,yaw(0F),false));
        assertEquals(0D,p.x,1E-12);assertEquals(0,p.wz);
        p.throwWriteX=false;
        key(i,LegacyKeyboardCodes.SPACE,false);m.applyJump(p,ground(),false);
        key(i,LegacyKeyboardCodes.SPACE,true);m.applyJump(p,ground(),false);
        assertTrue(m.applyMotion(p,yaw(0F),false));
        assertEquals(.65D,p.z,1E-12);
    }
    @Test void preserveFasterGenuineMomentumDoesNotOverwriteXZ() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LongJumpModule m=new Minecraft189LongJumpModule(i);
        final Player p=new Player();m.preserveHigherMomentumSetting().set(Boolean.TRUE);
        p.x=.8D;p.z=.6D;arm(i,m,p);
        assertTrue(m.applyMotion(p,yaw(0F),false));
        assertEquals(.8D,p.x,1E-12);assertEquals(.6D,p.z,1E-12);
        assertEquals(0,p.wx+p.wz);
    }
    @Test void preserveNonfiniteGenuineMomentumFailsClosedWithoutWrite() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LongJumpModule m=new Minecraft189LongJumpModule(i);
        final Player p=new Player();m.preserveHigherMomentumSetting().set(Boolean.TRUE);
        p.x=Double.NaN;p.z=.2D;arm(i,m,p);
        assertTrue(m.applyMotion(p,yaw(0F),false));
        assertTrue(Double.isNaN(p.x));assertEquals(0,p.wx+p.wz);
    }
    @Test void originalDiagonalMovementPreservesNormalizedSpeed() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LongJumpModule m=new Minecraft189LongJumpModule(i);
        final Player p=new Player();arm(i,m,p);
        key(i,LegacyKeyboardCodes.A,true);
        assertTrue(m.applyMotion(p,yaw(0F),false));
        assertEquals(.65D/Math.sqrt(2),p.x,1E-10);
        assertEquals(.65D/Math.sqrt(2),p.z,1E-10);
    }
    @Test void noPhysicalHorizontalInputCannotForgeNativeBoost() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LongJumpModule m=new Minecraft189LongJumpModule(i);
        final Player p=new Player();arm(i,m,p);
        key(i,LegacyKeyboardCodes.W,false);
        assertFalse(m.applyMotion(p,yaw(0F),false));
        assertEquals(0,p.rx+p.rz+p.wx+p.wz);
    }
    @Test void originalJumpInsideHorizontalSetterCannotBorrowNewEdge() {
        final Minecraft189InputState i=new Minecraft189InputState();
        final Minecraft189LongJumpModule m=new Minecraft189LongJumpModule(i);
        final Player p=new Player();arm(i,m,p);p.x=1D;
        p.writeXCallback=()->{
            key(i,LegacyKeyboardCodes.SPACE,false);
            key(i,LegacyKeyboardCodes.SPACE,true);
            m.applyJump(p,ground(),false);
        };
        assertTrue(m.applyMotion(p,yaw(0F),false));
        assertEquals(1,p.jumps);
    }
}
