package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Original mapped player-motion adapter policy; not genuine game runtime. */
final class Minecraft189DamageOrbitNativeReadEpochTest {
    private static final class Motion implements Minecraft189PlayerMotionControl {
        double x, y, z; int rx, ry, rz, wx, wy, wz;
        Runnable readX, readY, readZ, writeX, writeY;
        @Override public double customMcMotionX() {
            rx++;double v=x;
            if(readX!=null){Runnable a=readX;readX=null;a.run();}
            return v;
        }
        @Override public double customMcMotionY() {
            ry++;double v=y;
            if(readY!=null){Runnable a=readY;readY=null;a.run();}
            return v;
        }
        @Override public double customMcMotionZ() {
            rz++;double v=z;
            if(readZ!=null){Runnable a=readZ;readZ=null;a.run();}
            return v;
        }
        @Override public void customMcSetMotionX(double v) {
            wx++;x=v;if(writeX!=null){Runnable a=writeX;writeX=null;a.run();}
        }
        @Override public void customMcSetMotionY(double v) {
            wy++;y=v;if(writeY!=null){Runnable a=writeY;writeY=null;a.run();}
        }
        @Override public void customMcSetMotionZ(double v){wz++;z=v;}
    }
    private static final class Source {
        final Minecraft189PlayerPositionState local = new Minecraft189PlayerPositionState();
        final Minecraft189WorldEntityPositionState positions =
                new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds =
                new Minecraft189WorldEntityKindState();
        final Minecraft189NearestPlayerTargetState target =
                new Minecraft189NearestPlayerTargetState();
        Source(double distance) {
            local.update(distance,0D,0D);
            positions.update(new double[]{0D,0D,0D});
            kinds.update(new int[]{Minecraft189WorldEntityKindState.LIVING
                    | Minecraft189WorldEntityKindState.PLAYER});
            target.update(local.snapshot(),positions.snapshot(),kinds.snapshot());
        }
    }
    private static Minecraft189PlayerMovementState.Snapshot ground(){
        Minecraft189PlayerMovementState s=new Minecraft189PlayerMovementState();
        s.update(true,false,false);return s.snapshot();
    }
    private static Minecraft189PlayerHurtTimeState ready(Minecraft189DamageBoostModule m, Motion p) {
        Minecraft189PlayerHurtTimeState hurt=new Minecraft189PlayerHurtTimeState();
        hurt.update(8);assertFalse(m.apply(p,hurt.snapshot(),false));
        hurt.update(10);
        return hurt;
    }

    @Test void damageBoostOriginalXReadRestartPreventsLaterAxesAndOldImpulse(){
        Minecraft189DamageBoostModule m=new Minecraft189DamageBoostModule();
        Motion p=new Motion();p.x=.4D;p.y=.2D;p.z=.1D;m.onEnable();
        Minecraft189PlayerHurtTimeState h=ready(m,p);
        p.readX=()->{m.onDisable();m.onEnable();};
        assertFalse(m.apply(p,h.snapshot(),false));
        assertEquals(1,p.rx);assertEquals(0,p.ry+p.rz+p.wx+p.wy+p.wz);
        assertFalse(m.apply(p,h.snapshot(),false));
        m.onDisable();
    }
    @Test void damageBoostNativeYGetterVerticalMultiplierEditVetoesZAndAllWrites(){
        Minecraft189DamageBoostModule m=new Minecraft189DamageBoostModule();
        Motion p=new Motion();p.x=.4D;p.y=.2D;p.z=.1D;m.onEnable();
        Minecraft189PlayerHurtTimeState h=ready(m,p);
        p.readY=()->m.verticalMultiplierSetting().set(2D);
        assertFalse(m.apply(p,h.snapshot(),false));
        assertEquals(1,p.rx);assertEquals(1,p.ry);assertEquals(0,p.rz);
        assertEquals(0,p.wx+p.wy+p.wz);
        m.onDisable();
    }
    @Test void damageBoostOriginalZGetterCapPolicyEditVetoesAllAxes(){
        Minecraft189DamageBoostModule m=new Minecraft189DamageBoostModule();
        Motion p=new Motion();p.x=.4D;p.y=.2D;p.z=.1D;m.onEnable();
        Minecraft189PlayerHurtTimeState h=ready(m,p);
        p.readZ=()->m.capHorizontalSetting().set(Boolean.TRUE);
        assertFalse(m.apply(p,h.snapshot(),false));
        assertEquals(1,p.rz);assertEquals(0,p.wx+p.wy+p.wz);
        m.onDisable();
    }
    @Test void damageBoostXSetterDisableCannotIssueOldYOrZ(){
        Minecraft189DamageBoostModule m=new Minecraft189DamageBoostModule();
        Motion p=new Motion();p.x=.4D;p.y=.2D;p.z=.1D;
        m.verticalMultiplierSetting().set(2D);m.onEnable();
        Minecraft189PlayerHurtTimeState h=ready(m,p);
        p.writeX=m::onDisable;
        assertTrue(m.apply(p,h.snapshot(),false));
        assertEquals(1,p.wx);assertEquals(0,p.wy+p.wz);
        assertFalse(m.active());
    }
    @Test void damageBoostYSetterGuiEditCannotIssueOldZ(){
        Minecraft189DamageBoostModule m=new Minecraft189DamageBoostModule();
        Motion p=new Motion();p.x=.4D;p.y=.2D;p.z=.1D;
        m.verticalMultiplierSetting().set(2D);m.onEnable();
        Minecraft189PlayerHurtTimeState h=ready(m,p);
        p.writeY=()->m.multiplierSetting().set(1.50D);
        assertTrue(m.apply(p,h.snapshot(),false));
        assertEquals(1,p.wx);assertEquals(1,p.wy);assertEquals(0,p.wz);
        m.onDisable();
    }
    @Test void damageBoostRealFreshHurtScalesXZYOnce(){
        Minecraft189DamageBoostModule m=new Minecraft189DamageBoostModule();
        Motion p=new Motion();p.x=.4D;p.y=.2D;p.z=.1D;
        m.verticalMultiplierSetting().set(2D);m.onEnable();
        Minecraft189PlayerHurtTimeState h=ready(m,p);
        assertTrue(m.apply(p,h.snapshot(),false));
        assertEquals(.5D,p.x,1E-9);assertEquals(.4D,p.y,1E-9);
        assertEquals(.125D,p.z,1E-9);
        assertEquals(1,p.wx);assertEquals(1,p.wy);assertEquals(1,p.wz);
        assertFalse(m.apply(p,h.snapshot(),false));
        assertEquals(3,p.wx+p.wy+p.wz);
        m.onDisable();
    }
    @Test void damageBoostCapNeverReducesOriginalHigherHorizontalMomentum(){
        Minecraft189DamageBoostModule m=new Minecraft189DamageBoostModule();
        Motion p=new Motion();p.x=.8D;p.y=.2D;p.z=.6D;
        m.capHorizontalSetting().set(Boolean.TRUE);
        m.maxHorizontalSpeedSetting().set(.7D);
        m.verticalMultiplierSetting().set(1.5D);m.onEnable();
        Minecraft189PlayerHurtTimeState h=ready(m,p);
        assertTrue(m.apply(p,h.snapshot(),false));
        assertEquals(.8D,p.x,1E-9);assertEquals(.6D,p.z,1E-9);
        assertEquals(0,p.wx+p.wz);
        assertEquals(.3D,p.y,1E-9);
        m.onDisable();
    }

    @Test void targetStrafeXGetterOffOnRevokesOriginalOrbit(){
        Minecraft189TargetStrafeModule m=new Minecraft189TargetStrafeModule();
        Motion p=new Motion();Source src=new Source(4D);m.onEnable();
        p.readX=()->{m.onDisable();m.onEnable();};
        assertFalse(m.apply(p,src.local.snapshot(),src.target.snapshot(),
                ground(),true,false));
        assertEquals(1,p.rx);assertEquals(0,p.rz+p.wx+p.wz);
        assertTrue(m.apply(p,src.local.snapshot(),src.target.snapshot(),
                ground(),true,false));
        assertTrue(p.z>0D);
        m.onDisable();
    }
    @Test void targetStrafeZGetterRadiusEditVetoesBothOldSetters(){
        Minecraft189TargetStrafeModule m=new Minecraft189TargetStrafeModule();
        Motion p=new Motion();Source src=new Source(4D);m.onEnable();
        p.readZ=()->m.radiusSetting().set(2D);
        assertFalse(m.apply(p,src.local.snapshot(),src.target.snapshot(),
                ground(),true,false));
        assertEquals(0,p.wx+p.wz);
        m.onDisable();
    }
    @Test void targetStrafeXSetterDisableBlocksOldZ(){
        Minecraft189TargetStrafeModule m=new Minecraft189TargetStrafeModule();
        Motion p=new Motion();Source src=new Source(4D);m.onEnable();
        p.writeX=m::onDisable;
        assertFalse(m.apply(p,src.local.snapshot(),src.target.snapshot(),
                ground(),true,false));
        assertEquals(1,p.wx);assertEquals(0,p.wz);
        assertFalse(m.active());
    }
    @Test void targetStrafeGetterRequireGroundEditRevokesOldUngatedOrbit(){
        Minecraft189TargetStrafeModule m=new Minecraft189TargetStrafeModule();
        Motion p=new Motion();Source src=new Source(4D);m.onEnable();
        p.readX=()->m.groundOnlySetting().set(Boolean.TRUE);
        assertFalse(m.apply(p,src.local.snapshot(),src.target.snapshot(),
                null,true,false));
        assertEquals(0,p.rz+p.wx+p.wz);
        assertTrue(m.apply(p,src.local.snapshot(),src.target.snapshot(),
                ground(),true,false));
        m.onDisable();
    }
    @Test void targetStrafeClockwiseChangesRealSourceOrbitAndMissingTargetVetoes(){
        Minecraft189TargetStrafeModule m=new Minecraft189TargetStrafeModule();
        Motion p=new Motion();Source src=new Source(3D);m.onEnable();
        assertTrue(m.apply(p,src.local.snapshot(),src.target.snapshot(),
                ground(),true,false));
        assertEquals(0D,p.x,1E-9);assertEquals(.28D,p.z,1E-9);
        m.clockwiseSetting().set(Boolean.FALSE);
        assertTrue(m.apply(p,src.local.snapshot(),src.target.snapshot(),
                ground(),true,false));
        assertEquals(-.28D,p.z,1E-9);
        src.target.clear();
        assertFalse(m.apply(p,src.local.snapshot(),src.target.snapshot(),
                ground(),true,false));
        m.onDisable();
    }
}
