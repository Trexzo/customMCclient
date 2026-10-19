package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Original-shaped mapped native host regression; genuine Mojang suite separate. */
final class Minecraft189TargetStrafeNativeOrbitOwnerTest {
    private static final class Scene {
        final Minecraft189PlayerPositionState.Snapshot local;
        final Minecraft189NearestPlayerTargetState.Snapshot target;
        final Minecraft189PlayerMovementState.Snapshot movement;
        Scene(double localX, double targetX, double targetZ, boolean ground) {
            final Minecraft189PlayerPositionState pos =
                    new Minecraft189PlayerPositionState();
            pos.update(localX,0D,0D);
            local=pos.snapshot();
            final Minecraft189WorldEntityPositionState positions =
                    new Minecraft189WorldEntityPositionState();
            positions.update(new double[]{targetX,0D,targetZ});
            final Minecraft189WorldEntityKindState kinds =
                    new Minecraft189WorldEntityKindState();
            kinds.update(new int[]{Minecraft189WorldEntityKindState.LIVING
                    | Minecraft189WorldEntityKindState.PLAYER});
            final Minecraft189NearestPlayerTargetState nearest =
                    new Minecraft189NearestPlayerTargetState();
            nearest.update(pos.snapshot(),positions.snapshot(),kinds.snapshot());
            target=nearest.snapshot();
            final Minecraft189PlayerMovementState move =
                    new Minecraft189PlayerMovementState();
            move.update(ground,false,false);
            movement=move.snapshot();
        }
    }
    private static final class Player implements Minecraft189PlayerMotionControl {
        double x=1D,z=1D;int rx,rz,wx,wz;
        Runnable onReadX,onReadZ,onWriteX,onWriteZ;
        boolean failReadX,failWriteX;
        @Override public double customMcMotionY(){return 0D;}
        @Override public void customMcSetMotionY(double y){}
        @Override public double customMcMotionX(){
            rx++;if(rx>9)throw new AssertionError("recursive source X getter");
            final Runnable c=onReadX;onReadX=null;if(c!=null)c.run();
            if(failReadX)throw new IllegalStateException("native X getter");
            return x;
        }
        @Override public double customMcMotionZ(){
            rz++;if(rz>9)throw new AssertionError("recursive source Z getter");
            final Runnable c=onReadZ;onReadZ=null;if(c!=null)c.run();
            return z;
        }
        @Override public void customMcSetMotionX(double v){
            wx++;if(wx>9)throw new AssertionError("recursive source X setter");
            x=v;final Runnable c=onWriteX;onWriteX=null;if(c!=null)c.run();
            if(failWriteX)throw new IllegalStateException("native X setter");
        }
        @Override public void customMcSetMotionZ(double v){
            wz++;if(wz>9)throw new AssertionError("recursive source Z setter");
            z=v;final Runnable c=onWriteZ;onWriteZ=null;if(c!=null)c.run();
        }
        int reads(){return rx+rz;}
        int writes(){return wx+wz;}
    }
    private static final Scene SCENE = new Scene(3D,0D,0D,true);
    private static Minecraft189TargetStrafeModule module(){
        final Minecraft189TargetStrafeModule m=new Minecraft189TargetStrafeModule();
        m.radiusSetting().set(2D);
        m.onEnable();return m;
    }
    private static boolean apply(Minecraft189TargetStrafeModule m,Player p){
        return m.apply(p,SCENE.local,SCENE.target,SCENE.movement,true,false);
    }
    @Test void genuineOriginalNearestPlayerOrbitWritesNormalizedXZ(){
        final Minecraft189TargetStrafeModule m=module();final Player p=new Player();
        assertTrue(apply(m,p));assertEquals(2,p.reads());assertEquals(2,p.writes());
        assertEquals(.28D,Math.hypot(p.x,p.z),1E-12);
        assertTrue(p.x<0D);assertTrue(p.z>0D);
    }
    @Test void secondGenuineFrameAtCorrectNativeOrbitIsNoop(){
        final Minecraft189TargetStrafeModule m=module();final Player p=new Player();
        assertTrue(apply(m,p));assertTrue(apply(m,p));
        assertEquals(2,p.writes());assertEquals(4,p.reads());
    }
    @Test void originalXGetterSamePlayerReentryNeverReadsTwice(){
        final Minecraft189TargetStrafeModule m=module();final Player p=new Player();
        p.onReadX=()->assertFalse(apply(m,p));
        assertTrue(apply(m,p));assertEquals(1,p.rx);assertEquals(1,p.rz);
    }
    @Test void originalZGetterSamePlayerReentryNeverReadsTwice(){
        final Minecraft189TargetStrafeModule m=module();final Player p=new Player();
        p.onReadZ=()->assertFalse(apply(m,p));
        assertTrue(apply(m,p));assertEquals(1,p.rx);assertEquals(1,p.rz);
    }
    @Test void originalXSetterSamePlayerReentryNeverDoubleWrites(){
        final Minecraft189TargetStrafeModule m=module();final Player p=new Player();
        p.onWriteX=()->assertFalse(apply(m,p));
        assertTrue(apply(m,p));assertEquals(1,p.wx);assertEquals(1,p.wz);
    }
    @Test void originalZSetterSamePlayerReentryNeverDoubleWrites(){
        final Minecraft189TargetStrafeModule m=module();final Player p=new Player();
        p.onWriteZ=()->assertFalse(apply(m,p));
        assertTrue(apply(m,p));assertEquals(1,p.wx);assertEquals(1,p.wz);
    }
    @Test void anotherOriginalPlayerInXGetterVetoesOldOrbit(){
        final Minecraft189TargetStrafeModule m=module();
        final Player a=new Player(),b=new Player();
        a.onReadX=()->assertFalse(apply(m,b));
        assertFalse(apply(m,a));assertEquals(0,a.rz+a.writes()+b.reads()+b.writes());
    }
    @Test void anotherOriginalPlayerInZGetterVetoesOldWrites(){
        final Minecraft189TargetStrafeModule m=module();
        final Player a=new Player(),b=new Player();
        a.onReadZ=()->assertFalse(apply(m,b));
        assertFalse(apply(m,a));assertEquals(0,a.writes()+b.reads()+b.writes());
    }
    @Test void anotherOriginalPlayerInXSetterVetoesStaleZ(){
        final Minecraft189TargetStrafeModule m=module();
        final Player a=new Player(),b=new Player();
        a.onWriteX=()->assertFalse(apply(m,b));
        assertFalse(apply(m,a));assertEquals(1,a.wx);
        assertEquals(0,a.wz+b.reads()+b.writes());
    }
    @Test void anotherOriginalPlayerInZSetterCannotBorrowOldStack(){
        final Minecraft189TargetStrafeModule m=module();
        final Player a=new Player(),b=new Player();
        a.onWriteZ=()->assertFalse(apply(m,b));
        assertFalse(apply(m,a));assertEquals(1,a.wz);
        assertEquals(0,b.reads()+b.writes());
    }
    @Test void changedTargetEntityGeometryDuringXGetterRevokesOrbit(){
        final Minecraft189TargetStrafeModule m=module();final Player p=new Player();
        final Scene newTarget=new Scene(3D,0D,1D,true);
        p.onReadX=()->assertFalse(m.apply(p,newTarget.local,newTarget.target,
                newTarget.movement,true,false));
        assertFalse(apply(m,p));assertEquals(0,p.rz+p.writes());
    }
    @Test void changedTargetGeometryDuringXSetterVetoesOldZ(){
        final Minecraft189TargetStrafeModule m=module();final Player p=new Player();
        final Scene newTarget=new Scene(3D,0D,1D,true);
        p.onWriteX=()->assertFalse(m.apply(p,newTarget.local,newTarget.target,
                newTarget.movement,true,false));
        assertFalse(apply(m,p));assertEquals(1,p.wx);assertEquals(0,p.wz);
    }
    @Test void changedLocalPlayerPositionDuringXGetterRevokesOrbit(){
        final Minecraft189TargetStrafeModule m=module();final Player p=new Player();
        final Scene newLocal=new Scene(4D,0D,0D,true);
        p.onReadX=()->assertFalse(m.apply(p,newLocal.local,newLocal.target,
                newLocal.movement,true,false));
        assertFalse(apply(m,p));assertEquals(0,p.rz+p.writes());
    }
    @Test void lostCertifiedNearestTargetInsideGetterRevokesOldOrbit(){
        final Minecraft189TargetStrafeModule m=module();final Player p=new Player();
        final Minecraft189NearestPlayerTargetState missing =
                new Minecraft189NearestPlayerTargetState();
        p.onReadX=()->assertFalse(m.apply(p,SCENE.local,missing.snapshot(),
                SCENE.movement,true,false));
        assertFalse(apply(m,p));assertEquals(0,p.rz+p.writes());
    }
    @Test void nativeHostSuspensionDuringGetterRevokesOldOrbit(){
        final Minecraft189TargetStrafeModule m=module();final Player p=new Player();
        p.onReadX=()->assertFalse(m.apply(p,SCENE.local,SCENE.target,
                SCENE.movement,true,true));
        assertFalse(apply(m,p));assertEquals(0,p.rz+p.writes());
    }
    @Test void physicalForwardLossDuringNativeGetterVetoesOldOrbit(){
        final Minecraft189TargetStrafeModule m=module();final Player p=new Player();
        p.onReadX=()->assertFalse(m.apply(p,SCENE.local,SCENE.target,
                SCENE.movement,false,false));
        assertFalse(apply(m,p));assertEquals(0,p.rz+p.writes());
    }
    @Test void optedInGroundLossDuringNativeGetterVetoesOldOrbit(){
        final Minecraft189TargetStrafeModule m=module();final Player p=new Player();
        final Scene air=new Scene(3D,0D,0D,false);
        m.groundOnlySetting().set(Boolean.TRUE);
        p.onReadX=()->assertFalse(m.apply(p,air.local,air.target,
                air.movement,true,false));
        assertFalse(apply(m,p));assertEquals(0,p.rz+p.writes());
    }
    @Test void nativeGetterModuleDisableVetoesOldOrbit(){
        final Minecraft189TargetStrafeModule m=module();final Player p=new Player();
        p.onReadX=m::onDisable;
        assertFalse(apply(m,p));assertEquals(0,p.rz+p.writes());
    }
    @Test void getterOffOnCannotBorrowNewOrbitActivation(){
        final Minecraft189TargetStrafeModule m=module();final Player p=new Player();
        p.onReadX=()->{m.onDisable();m.onEnable();assertFalse(apply(m,p));};
        assertFalse(apply(m,p));assertEquals(0,p.writes());
        assertTrue(apply(m,p));assertEquals(2,p.writes());
    }
    @Test void nativeXGetterRadiusEditRevokesOldOrbitGeometry(){
        final Minecraft189TargetStrafeModule m=module();final Player p=new Player();
        p.onReadX=()->m.radiusSetting().set(3D);
        assertFalse(apply(m,p));assertEquals(0,p.rz+p.writes());
    }
    @Test void nativeZGetterClockwiseEditRevokesOldTarget(){
        final Minecraft189TargetStrafeModule m=module();final Player p=new Player();
        p.onReadZ=()->m.clockwiseSetting().set(Boolean.FALSE);
        assertFalse(apply(m,p));assertEquals(0,p.writes());
    }
    @Test void originalGetterThrowReleasesOrbitOwnerForNextTick(){
        final Minecraft189TargetStrafeModule m=module();final Player p=new Player();
        p.failReadX=true;
        assertThrows(IllegalStateException.class,()->apply(m,p));
        p.failReadX=false;assertTrue(apply(m,p));assertEquals(2,p.writes());
    }
    @Test void nativeXSetterThrowPreservesPartialVanillaMotion(){
        final Minecraft189TargetStrafeModule m=module();final Player p=new Player();
        p.failWriteX=true;
        assertThrows(IllegalStateException.class,()->apply(m,p));
        assertTrue(p.x<0D);assertEquals(1D,p.z,1E-12);
        p.failWriteX=false;assertTrue(apply(m,p));assertEquals(1,p.wz);
    }
    @Test void genuineTargetUnavailableCannotReadVanillaVelocity(){
        final Minecraft189TargetStrafeModule m=module();final Player p=new Player();
        final Minecraft189NearestPlayerTargetState missing =
                new Minecraft189NearestPlayerTargetState();
        assertFalse(m.apply(p,SCENE.local,missing.snapshot(),
                SCENE.movement,true,false));
        assertEquals(0,p.reads()+p.writes());
    }
    @Test void disabledModuleCannotWriteGenuineOrbit(){
        final Minecraft189TargetStrafeModule m=module();final Player p=new Player();
        m.onDisable();assertFalse(apply(m,p));
        assertEquals(0,p.reads()+p.writes());
    }
    @Test void counterClockwiseOriginalOrbitReversesTangentDirection(){
        final Minecraft189TargetStrafeModule m=module();final Player p=new Player();
        m.clockwiseSetting().set(Boolean.FALSE);
        assertTrue(apply(m,p));assertTrue(p.z<0D);
        assertEquals(.28D,Math.hypot(p.x,p.z),1E-12);
    }
}
