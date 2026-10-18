package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Offline original mapped motion adapter regression; not a Mojang runtime. */
final class Minecraft189AirSpeedReverseStepEpochTest {
    private static final class Player implements Minecraft189PlayerMotionControl {
        double x, y, z;
        int rx, ry, rz, wx, wy, wz;
        Runnable readX, readY, readZ, writeX;
        @Override public double customMcMotionX() {
            rx++;double old=x;
            if(readX!=null){Runnable a=readX;readX=null;a.run();}
            return old;
        }
        @Override public double customMcMotionY() {
            ry++;double old=y;
            if(readY!=null){Runnable a=readY;readY=null;a.run();}
            return old;
        }
        @Override public double customMcMotionZ() {
            rz++;double old=z;
            if(readZ!=null){Runnable a=readZ;readZ=null;a.run();}
            return old;
        }
        @Override public void customMcSetMotionX(double v) {
            wx++;x=v;
            if(writeX!=null){Runnable a=writeX;writeX=null;a.run();}
        }
        @Override public void customMcSetMotionY(double v) {wy++;y=v;}
        @Override public void customMcSetMotionZ(double v) {wz++;z=v;}
    }
    private static Minecraft189PlayerMovementState.Snapshot movement(boolean grounded,boolean sneaking){
        final Minecraft189PlayerMovementState s=new Minecraft189PlayerMovementState();
        s.update(grounded,sneaking,false);
        return s.snapshot();
    }
    private static Minecraft189PlayerRotationState.Snapshot yawZero(){
        final Minecraft189PlayerRotationState r=new Minecraft189PlayerRotationState();
        r.update(0F);
        return r.snapshot();
    }
    @Test void airSpeedOriginalXGetterOffOnRevokesOldXZ(){
        Minecraft189InputState input=new Minecraft189InputState();
        Minecraft189AirSpeedModule m=new Minecraft189AirSpeedModule(input);
        Player p=new Player(); input.key(LegacyKeyboardCodes.W,true);m.onEnable();
        p.readX=()->{m.onDisable();m.onEnable();};
        assertFalse(m.apply(p,movement(false,false),yawZero(),false));
        assertEquals(1,p.rx);assertEquals(0,p.rz+p.wx+p.wz);
        assertTrue(m.apply(p,movement(false,false),yawZero(),false));
        assertEquals(.35,p.z,1E-9);
        m.onDisable();
    }
    @Test void airSpeedPhysicalWReleasedInOriginalGetterSkipsZ(){
        Minecraft189InputState input=new Minecraft189InputState();
        Minecraft189AirSpeedModule m=new Minecraft189AirSpeedModule(input);
        Player p=new Player();input.key(LegacyKeyboardCodes.W,true);m.onEnable();
        p.readX=()->input.key(LegacyKeyboardCodes.W,false);
        assertFalse(m.apply(p,movement(false,false),yawZero(),false));
        assertEquals(0,p.rz+p.wx+p.wz);
        m.onDisable();
    }
    @Test void airSpeedZGetterSpeedEditRevokesAllWrites(){
        Minecraft189InputState input=new Minecraft189InputState();
        Minecraft189AirSpeedModule m=new Minecraft189AirSpeedModule(input);
        Player p=new Player();input.key(LegacyKeyboardCodes.W,true);m.onEnable();
        p.readZ=()->m.speedSetting().set(.65D);
        assertFalse(m.apply(p,movement(false,false),yawZero(),false));
        assertEquals(0,p.wx+p.wz);
        assertTrue(m.apply(p,movement(false,false),yawZero(),false));
        assertEquals(.65D,p.z,1E-9);
        m.onDisable();
    }
    @Test void airSpeedZGetterSmoothingEditRejectsOldFraction(){
        Minecraft189InputState input=new Minecraft189InputState();
        Minecraft189AirSpeedModule m=new Minecraft189AirSpeedModule(input);
        Player p=new Player();input.key(LegacyKeyboardCodes.W,true);m.onEnable();
        m.smoothAccelerationSetting().set(Boolean.TRUE);
        p.readZ=()->m.accelerationPercentSetting().set(75);
        assertFalse(m.apply(p,movement(false,false),yawZero(),false));
        assertEquals(0,p.wx+p.wz);
        assertTrue(m.apply(p,movement(false,false),yawZero(),false));
        assertEquals(.35D*.75D,p.z,1E-9);
        m.onDisable();
    }
    @Test void airSpeedXSetterDisableDoesNotApplyOldZ(){
        Minecraft189InputState input=new Minecraft189InputState();
        Minecraft189AirSpeedModule m=new Minecraft189AirSpeedModule(input);
        Player p=new Player();input.key(LegacyKeyboardCodes.W,true);
        input.key(LegacyKeyboardCodes.A,true);m.onEnable();p.writeX=m::onDisable;
        assertFalse(m.apply(p,movement(false,false),yawZero(),false));
        assertEquals(1,p.wx);assertEquals(0,p.wz);
    }
    @Test void airSpeedValidDiagonalAndAirborneGateRemain(){
        Minecraft189InputState input=new Minecraft189InputState();
        Minecraft189AirSpeedModule m=new Minecraft189AirSpeedModule(input);
        Player p=new Player();input.key(LegacyKeyboardCodes.W,true);
        input.key(LegacyKeyboardCodes.A,true);m.onEnable();
        assertFalse(m.apply(p,movement(true,false),yawZero(),false));
        assertEquals(0,p.rx+p.rz);
        assertTrue(m.apply(p,movement(false,false),yawZero(),false));
        assertEquals(.35/Math.sqrt(2),p.x,1E-9);
        assertEquals(.35/Math.sqrt(2),p.z,1E-9);
        m.onDisable();
    }

    @Test void reverseStepYGetterModuleRestartRevokesOldDeparture(){
        Minecraft189ReverseStepModule m=new Minecraft189ReverseStepModule();
        Player p=new Player();p.y=-.1D;m.onEnable();
        m.apply(p,movement(true,false),false);
        p.readY=()->{m.onDisable();m.onEnable();};
        assertFalse(m.apply(p,movement(false,false),false));
        assertEquals(1,p.ry);assertEquals(0,p.wy);
        assertFalse(m.apply(p,movement(false,false),false));
        m.onDisable();
    }
    @Test void reverseStepYGetterSpeedEditRevokesPendingVelocity(){
        Minecraft189ReverseStepModule m=new Minecraft189ReverseStepModule();
        Player p=new Player();p.y=-.1D;m.onEnable();
        m.apply(p,movement(true,false),false);
        p.readY=()->m.speedSetting().set(.8D);
        assertFalse(m.apply(p,movement(false,false),false));
        assertEquals(0,p.wy);
        assertFalse(m.apply(p,movement(false,false),false));
        m.onDisable();
    }
    @Test void reverseStepMidGetterSneakPolicyEditRequiresFreshDeparture(){
        Minecraft189ReverseStepModule m=new Minecraft189ReverseStepModule();
        Player p=new Player();p.y=-.1D;m.onEnable();
        m.apply(p,movement(true,false),false);
        p.readY=()->m.requireSneakingSetting().set(Boolean.TRUE);
        assertFalse(m.apply(p,movement(false,false),false));
        assertEquals(0,p.wy);
        assertFalse(m.apply(p,movement(false,true),false));
        m.onDisable();
    }
    @Test void reverseStepDelayEditDuringSourceGetterCannotUseOldContact(){
        Minecraft189ReverseStepModule m=new Minecraft189ReverseStepModule();
        Player p=new Player();p.y=-.1D;m.onEnable();
        m.apply(p,movement(true,false),false);
        p.readY=()->m.delayTicksSetting().set(2);
        assertFalse(m.apply(p,movement(false,false),false));
        assertEquals(0,p.wy);
        assertFalse(m.apply(p,movement(false,false),false));
        m.onDisable();
    }
    @Test void reverseStepRealDepartureAllowsOneFiniteDescentOnly(){
        Minecraft189ReverseStepModule m=new Minecraft189ReverseStepModule();
        Player p=new Player();p.y=-.1D;m.onEnable();
        m.apply(p,movement(true,false),false);
        assertTrue(m.apply(p,movement(false,false),false));
        assertEquals(-.5D,p.y,1E-9);
        assertEquals(1,p.wy);
        assertFalse(m.apply(p,movement(false,false),false));
        assertEquals(1,p.wy);
        m.onDisable();
    }
    @Test void reverseStepRisingOrOriginalGuiNeverForcesNewDescent(){
        Minecraft189ReverseStepModule m=new Minecraft189ReverseStepModule();
        Player p=new Player();p.y=.1D;m.onEnable();
        m.apply(p,movement(true,false),false);
        assertFalse(m.apply(p,movement(false,false),false));
        assertEquals(0,p.wy);
        m.apply(p,movement(true,false),false);
        assertFalse(m.apply(p,movement(false,false),false,false,true));
        assertEquals(0,p.wy);
        m.onDisable();
    }
}
