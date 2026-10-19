package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Actual player hurtTime source and original mapped motionX/Y/Z adapter.
 * Offline finite arithmetic and synchronous original setter callback tests.
 */
final class Minecraft189DamageBoostFiniteNativeOwnerTest {
    private static final class NativePlayer implements Minecraft189PlayerMotionControl {
        double x=.4D,y=.2D,z=.3D;
        int xWrites,yWrites,zWrites,depth,maxDepth;
        Runnable afterX,afterY,afterZ;
        boolean throwAfterX;
        @Override public double customMcMotionX(){return x;}
        @Override public double customMcMotionY(){return y;}
        @Override public double customMcMotionZ(){return z;}
        @Override public void customMcSetMotionX(double value) {
            x=value;xWrites++;depth++;maxDepth=Math.max(maxDepth,depth);
            try {
                Runnable callback=afterX;afterX=null;
                if(callback!=null)callback.run();
                if(throwAfterX) {
                    throwAfterX=false;
                    throw new IllegalStateException("real X setter mutated then failed");
                }
            } finally {depth--;}
        }
        @Override public void customMcSetMotionY(double value) {
            y=value;yWrites++;
            Runnable callback=afterY;afterY=null;
            if(callback!=null)callback.run();
        }
        @Override public void customMcSetMotionZ(double value) {
            z=value;zWrites++;
            Runnable callback=afterZ;afterZ=null;
            if(callback!=null)callback.run();
        }
    }
    private static final class Rig {
        final Minecraft189DamageBoostModule module =
                new Minecraft189DamageBoostModule();
        final Minecraft189PlayerHurtTimeState hurt =
                new Minecraft189PlayerHurtTimeState();
        final NativePlayer player = new NativePlayer();
        Rig() {
            module.onEnable();
            hurt.update(0);
            assertFalse(tick());
        }
        boolean tick() {
            return module.apply(player,hurt.snapshot(),false);
        }
        boolean hit() {
            hurt.update(10);
            return tick();
        }
        boolean freshHit() {
            hurt.update(0);
            assertFalse(tick());
            return hit();
        }
    }
    @Test void defaultActualHurtBoostUpdatesOnlyFiniteHorizontalAxes() {
        Rig r=new Rig();
        assertTrue(r.hit());
        assertEquals(.5D,r.player.x,1e-12);
        assertEquals(.375D,r.player.z,1e-12);
        assertEquals(.2D,r.player.y,1e-12);
        assertEquals(1,r.player.xWrites);
        assertEquals(1,r.player.zWrites);
        assertEquals(0,r.player.yWrites);
        assertFalse(r.tick());
    }
    @Test void hugeValidNativeXThatOverflowsMultiplierNeverWritesInfinity() {
        Rig r=new Rig();
        r.module.multiplierSetting().set(3D);
        r.module.verticalMultiplierSetting().set(2D);
        r.player.x=1e308;r.player.z=.1D;
        assertTrue(r.hit(),"independent source vertical boost may remain valid");
        assertEquals(1e308,r.player.x,0D);
        assertEquals(.1D,r.player.z,0D);
        assertEquals(0,r.player.xWrites+r.player.zWrites);
        assertEquals(.4D,r.player.y,1e-12);
        assertEquals(1,r.player.yWrites);
    }
    @Test void hugeFiniteOriginalYThatOverflowsVerticalMultiplierIsNotWritten() {
        Rig r=new Rig();
        r.module.verticalMultiplierSetting().set(3D);
        r.player.y=1e308;
        assertTrue(r.hit());
        assertEquals(1e308,r.player.y,0D);
        assertEquals(0,r.player.yWrites);
        assertEquals(1,r.player.xWrites+r.player.zWrites-1);
    }
    @Test void nanNativeHorizontalAxisDoesNotPoisonOtherHorizontalSource() {
        Rig r=new Rig();
        r.player.x=Double.NaN;
        r.module.verticalMultiplierSetting().set(2D);
        assertTrue(r.hit());
        assertTrue(Double.isNaN(r.player.x));
        assertEquals(.3D,r.player.z,0D);
        assertEquals(0,r.player.xWrites+r.player.zWrites);
        assertEquals(.4D,r.player.y,1e-12);
    }
    @Test void infinityNativeHorizontalAxisNeverWritesInvalidMomentum() {
        Rig r=new Rig();
        r.player.x=Double.POSITIVE_INFINITY;
        r.player.z=.3D;
        assertFalse(r.hit());
        assertTrue(Double.isInfinite(r.player.x));
        assertEquals(0,r.player.xWrites+r.player.zWrites);
    }
    @Test void originalHorizontalCapStillPreservesExistingAboveCapMomentum() {
        Rig r=new Rig();
        r.module.multiplierSetting().set(2D);
        r.module.capHorizontalSetting().set(true);
        r.module.maxHorizontalSpeedSetting().set(.7D);
        r.player.x=.8D;r.player.z=.6D;
        assertFalse(r.hit());
        assertEquals(.8D,r.player.x,0D);
        assertEquals(.6D,r.player.z,0D);
    }
    @Test void originalCapStillLimitsNewFiniteImpulseWithoutSlowdown() {
        Rig r=new Rig();
        r.module.multiplierSetting().set(2D);
        r.module.capHorizontalSetting().set(true);
        r.module.maxHorizontalSpeedSetting().set(.75D);
        r.player.x=.3D;r.player.z=.4D;
        assertTrue(r.hit());
        assertEquals(.75D,Math.hypot(r.player.x,r.player.z),1e-12);
    }
    @Test void originalXSetterDisablePreventsYAndZFromOldOwner() {
        Rig r=new Rig();
        r.module.verticalMultiplierSetting().set(2D);
        r.player.afterX=r.module::onDisable;
        assertFalse(r.hit());
        assertEquals(1,r.player.xWrites);
        assertEquals(0,r.player.yWrites+r.player.zWrites);
        assertFalse(r.module.active());
    }
    @Test void originalXSetterOffOnCannotApplyStaleVerticalAndZ() {
        Rig r=new Rig();
        r.module.verticalMultiplierSetting().set(2D);
        r.player.afterX=()->{r.module.onDisable();r.module.onEnable();};
        assertFalse(r.hit());
        assertEquals(1,r.player.xWrites);
        assertEquals(0,r.player.yWrites+r.player.zWrites);
        assertFalse(r.tick());
        assertTrue(r.freshHit());
    }
    @Test void originalXSetterGuiMultiplierEditRevokesOtherOriginalAxes() {
        Rig r=new Rig();
        r.module.verticalMultiplierSetting().set(2D);
        r.player.afterX=()->r.module.multiplierSetting().set(2D);
        assertFalse(r.hit());
        assertEquals(0,r.player.yWrites+r.player.zWrites);
        assertTrue(r.freshHit());
    }
    @Test void originalYSetterGuiCapEditRevokesOriginalZ() {
        Rig r=new Rig();
        r.module.verticalMultiplierSetting().set(2D);
        r.player.afterY=()->r.module.capHorizontalSetting().set(true);
        assertFalse(r.hit());
        assertEquals(1,r.player.xWrites);
        assertEquals(1,r.player.yWrites);
        assertEquals(0,r.player.zWrites);
    }
    @Test void originalZSetterDisableDoesNotReportStaleOwnerSuccess() {
        Rig r=new Rig();
        r.player.afterZ=r.module::onDisable;
        assertFalse(r.hit());
        assertEquals(1,r.player.xWrites);
        assertEquals(1,r.player.zWrites);
        assertFalse(r.module.active());
    }
    @Test void originalSetterNestedNewHurtMustNotDispatchAnotherImpulse() {
        Rig r=new Rig();
        r.player.afterX=()->{
            r.hurt.update(11);
            assertFalse(r.tick());
        };
        assertTrue(r.hit());
        assertEquals(1,r.player.xWrites);
        assertEquals(1,r.player.zWrites);
        assertEquals(1,r.player.maxDepth);
    }
    @Test void originalXSetterThrowAfterPhysicalMutationCannotReplaySameHit() {
        Rig r=new Rig();
        r.player.throwAfterX=true;
        assertThrows(IllegalStateException.class,r::hit);
        assertEquals(.5D,r.player.x,1e-12);
        assertEquals(0,r.player.zWrites);
        assertFalse(r.tick());
        assertEquals(1,r.player.xWrites);
        assertTrue(r.freshHit(),"a new genuine hurt may boost normally");
        assertEquals(2,r.player.xWrites);
    }
    @Test void invalidVerticalSourceCannotBlockIndependentFiniteHorizontalBoost() {
        Rig r=new Rig();
        r.player.y=Double.NaN;
        r.module.verticalMultiplierSetting().set(3D);
        assertTrue(r.hit());
        assertTrue(Double.isNaN(r.player.y));
        assertEquals(0,r.player.yWrites);
        assertEquals(1,r.player.xWrites);
        assertEquals(1,r.player.zWrites);
    }
}
