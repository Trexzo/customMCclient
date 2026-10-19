package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Real mapped player motionX/Z setter callbacks and true target snapshot. */
final class Minecraft189TargetStrafeNativeOrbitWriteTest {
    private static final class Player implements Minecraft189PlayerMotionControl {
        double x,y,z;
        int xWrites,zWrites,depth,maxDepth;
        Runnable onX,onZ;
        boolean throwAfterX;
        @Override public double customMcMotionX(){return x;}
        @Override public double customMcMotionY(){return y;}
        @Override public double customMcMotionZ(){return z;}
        @Override public void customMcSetMotionX(double v) {
            x=v;xWrites++;depth++;maxDepth=Math.max(maxDepth,depth);
            try {
                Runnable c=onX;onX=null;
                if(c!=null)c.run();
                if(throwAfterX) {
                    throwAfterX=false;
                    throw new IllegalStateException("actual native X setter already mutated player");
                }
            } finally {depth--;}
        }
        @Override public void customMcSetMotionY(double v) {
            y=v;
            throw new AssertionError("TargetStrafe has no source Y authority");
        }
        @Override public void customMcSetMotionZ(double v) {
            z=v;zWrites++;
            Runnable c=onZ;onZ=null;
            if(c!=null)c.run();
        }
    }
    private static final class Rig {
        final Minecraft189TargetStrafeModule module =
                new Minecraft189TargetStrafeModule();
        final Minecraft189PlayerPositionState local =
                new Minecraft189PlayerPositionState();
        final Minecraft189WorldEntityPositionState positions =
                new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds =
                new Minecraft189WorldEntityKindState();
        final Minecraft189NearestPlayerTargetState nearest =
                new Minecraft189NearestPlayerTargetState();
        final Minecraft189PlayerMovementState move =
                new Minecraft189PlayerMovementState();
        final Player player=new Player();
        Rig() {
            module.radiusSetting().set(2D);
            module.onEnable();
            local.update(3D,0D,0D);
            positions.update(new double[]{0D,0D,0D});
            kinds.update(new int[]{Minecraft189WorldEntityKindState.PLAYER
                    | Minecraft189WorldEntityKindState.LIVING});
            nearest.update(local.snapshot(),positions.snapshot(),kinds.snapshot());
            move.update(true,false,false);
        }
        boolean apply() {
            return module.apply(player,local.snapshot(),nearest.snapshot(),
                    move.snapshot(),true,false);
        }
    }
    @Test void realSourceOrbitWritesBothOriginalHorizontalAxes() {
        Rig r=new Rig();
        assertTrue(r.apply());
        assertEquals(1,r.player.xWrites);
        assertEquals(1,r.player.zWrites);
        assertTrue(r.player.x<0D);
        assertTrue(r.player.z>0D);
        assertEquals(.28D,Math.hypot(r.player.x,r.player.z),1e-12);
    }
    @Test void stableOriginalOrbitNoOpStillOwnsHorizontalMovement() {
        Rig r=new Rig();
        assertTrue(r.apply());
        assertTrue(r.apply());
        assertEquals(1,r.player.xWrites);
        assertEquals(1,r.player.zWrites);
    }
    @Test void nativeXSetterDisableVetoesObsoleteOriginalZ() {
        Rig r=new Rig();
        r.player.onX=r.module::onDisable;
        assertFalse(r.apply());
        assertEquals(1,r.player.xWrites);
        assertEquals(0,r.player.zWrites);
        assertFalse(r.module.active());
    }
    @Test void originalXSetterOffOnCannotSpendNewActivationZ() {
        Rig r=new Rig();
        r.player.onX=()->{r.module.onDisable();r.module.onEnable();};
        assertFalse(r.apply());
        assertEquals(1,r.player.xWrites);
        assertEquals(0,r.player.zWrites);
        assertTrue(r.apply());
        assertEquals(1,r.player.zWrites);
    }
    @Test void nativeXSetterSpeedGuiEditRevokesOldZ() {
        Rig r=new Rig();
        r.player.onX=()->r.module.speedSetting().set(.4D);
        assertFalse(r.apply());
        assertEquals(0,r.player.zWrites);
        assertTrue(r.apply());
    }
    @Test void nativeXSetterRadiusGuiEditRevokesOldZ() {
        Rig r=new Rig();
        r.player.onX=()->r.module.radiusSetting().set(3D);
        assertFalse(r.apply());
        assertEquals(0,r.player.zWrites);
        assertTrue(r.apply());
    }
    @Test void nativeXSetterClockwiseGuiEditRevokesOldZ() {
        Rig r=new Rig();
        r.player.onX=()->r.module.clockwiseSetting().set(false);
        assertFalse(r.apply());
        assertEquals(0,r.player.zWrites);
        assertTrue(r.apply());
        assertTrue(r.player.z<0D);
    }
    @Test void originalXSetterForwardRequirementEditRevokesOldZ() {
        Rig r=new Rig();
        r.player.onX=()->r.module.requireForwardSetting().set(false);
        assertFalse(r.apply());
        assertEquals(0,r.player.zWrites);
    }
    @Test void originalZSetterDisableCannotReportOldOrbitSuccess() {
        Rig r=new Rig();
        r.player.onZ=r.module::onDisable;
        assertFalse(r.apply());
        assertEquals(1,r.player.xWrites);
        assertEquals(1,r.player.zWrites);
        assertFalse(r.module.active());
    }
    @Test void originalZSetterRangePolicyChangeRevokesOldSuccess() {
        Rig r=new Rig();
        r.player.onZ=()->r.module.maxDistanceSetting().set(5D);
        assertFalse(r.apply());
        assertEquals(1,r.player.zWrites);
    }
    @Test void originalXSetterNestedHostCannotDispatchAnotherOrbit() {
        Rig r=new Rig();
        r.player.onX=()->assertFalse(r.apply());
        assertTrue(r.apply());
        assertEquals(1,r.player.xWrites);
        assertEquals(1,r.player.zWrites);
        assertEquals(1,r.player.maxDepth);
    }
    @Test void originalZSetterNestedHostCannotRewriteSourceX() {
        Rig r=new Rig();
        r.player.onZ=()->assertFalse(r.apply());
        assertTrue(r.apply());
        assertEquals(1,r.player.xWrites);
        assertEquals(1,r.player.zWrites);
    }
    @Test void originalXSetterThrowAfterRealMotionKeepsActualXAndReleasesGuard() {
        Rig r=new Rig();
        r.player.throwAfterX=true;
        assertThrows(IllegalStateException.class,r::apply);
        assertTrue(r.player.x<0D);
        assertEquals(0,r.player.zWrites);
        assertTrue(r.apply());
        assertEquals(1,r.player.zWrites);
    }
    @Test void trueSourceForwardKeyAbsenceDisallowsSyntheticOrbit() {
        Rig r=new Rig();
        assertFalse(r.module.apply(r.player,r.local.snapshot(),
                r.nearest.snapshot(),r.move.snapshot(),false,false));
        assertEquals(0,r.player.xWrites+r.player.zWrites);
    }
    @Test void trueOriginalAirborneGroundOnlyDisallowsOrbit() {
        Rig r=new Rig();
        r.module.groundOnlySetting().set(true);
        r.move.update(false,false,false);
        assertFalse(r.apply());
        assertEquals(0,r.player.xWrites+r.player.zWrites);
        r.move.update(true,false,false);
        assertTrue(r.apply());
    }
    @Test void missingOriginalNearestPlayerCannotInventOrbit() {
        Rig r=new Rig();
        r.nearest.clear();
        assertFalse(r.apply());
        assertEquals(0,r.player.xWrites+r.player.zWrites);
    }
}
