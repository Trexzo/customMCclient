package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Adversarial Java mapped player callbacks, not original Mojang runtime. */
final class Minecraft189StrafeFreezeNativeEpochTest {
    private static final class Motion implements Minecraft189PlayerMotionControl {
        double x, y, z;
        int rx, ry, rz, wx, wy, wz;
        Runnable readX, readY, readZ, writeX, writeY;
        @Override public double customMcMotionX() {
            rx++; final double old=x;
            if (readX != null) { final Runnable a=readX;readX=null;a.run(); }
            return old;
        }
        @Override public double customMcMotionY() {
            ry++; final double old=y;
            if (readY != null) { final Runnable a=readY;readY=null;a.run(); }
            return old;
        }
        @Override public double customMcMotionZ() {
            rz++; final double old=z;
            if (readZ != null) { final Runnable a=readZ;readZ=null;a.run(); }
            return old;
        }
        @Override public void customMcSetMotionX(double value) {
            wx++;x=value;
            if (writeX != null) { final Runnable a=writeX;writeX=null;a.run(); }
        }
        @Override public void customMcSetMotionY(double value) {
            wy++;y=value;
            if (writeY != null) { final Runnable a=writeY;writeY=null;a.run(); }
        }
        @Override public void customMcSetMotionZ(double value) {wz++;z=value;}
    }

    private static Minecraft189PlayerRotationState.Snapshot yawZero() {
        final Minecraft189PlayerRotationState rotation =
                new Minecraft189PlayerRotationState();
        rotation.update(0F);
        return rotation.snapshot();
    }
    private static Minecraft189PlayerMovementState.Snapshot ground(boolean sneak) {
        final Minecraft189PlayerMovementState state =
                new Minecraft189PlayerMovementState();
        state.update(true,sneak,false);
        return state.snapshot();
    }

    @Test void strafeXGetterOffOnCannotSpendPreviousSourceRead() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189StrafeModule m = new Minecraft189StrafeModule(input);
        final Motion p = new Motion();
        input.key(LegacyKeyboardCodes.W,true);m.onEnable();
        p.readX=()->{m.onDisable();m.onEnable();};
        m.apply(p,yawZero(),false);
        assertEquals(1,p.rx);
        assertEquals(0,p.rz+p.wx+p.wz);
        m.apply(p,yawZero(),false);
        assertEquals(0.30D,p.z,0.000001D);
        m.onDisable();
    }

    @Test void strafePhysicalWReleaseMidXSkipsZGetterAndStaleWrite() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189StrafeModule m = new Minecraft189StrafeModule(input);
        final Motion p = new Motion();
        input.key(LegacyKeyboardCodes.W,true);m.onEnable();
        p.readX=()->input.key(LegacyKeyboardCodes.W,false);
        m.apply(p,yawZero(),false);
        assertEquals(0,p.rz+p.wx+p.wz);
        m.onDisable();
    }

    @Test void strafeOriginalZGetterSpeedEditVetoesBothSetters() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189StrafeModule m = new Minecraft189StrafeModule(input);
        final Motion p = new Motion();
        input.key(LegacyKeyboardCodes.W,true);
        input.key(LegacyKeyboardCodes.A,true);
        m.onEnable();
        p.readZ=()->m.speedSetting().set(0.65D);
        m.apply(p,yawZero(),false);
        assertEquals(1,p.rz);
        assertEquals(0,p.wx+p.wz);
        m.apply(p,yawZero(),false);
        assertEquals(0.65D/Math.sqrt(2D),p.x,0.000001D);
        m.onDisable();
    }

    @Test void strafeGroundPolicyEditedInsideNativeZRevokesOldEligibility() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189StrafeModule m = new Minecraft189StrafeModule(input);
        final Motion p = new Motion();
        input.key(LegacyKeyboardCodes.W,true);m.onEnable();
        p.readZ=()->m.groundOnlySetting().set(Boolean.TRUE);
        m.apply(p,yawZero(),false);
        assertEquals(0,p.wx+p.wz);
        m.apply(p,yawZero(),false);
        assertEquals(0,p.wx+p.wz); // no movement authority
        m.apply(p,yawZero(),false,ground(false));
        assertEquals(0.30D,p.z,0.000001D);
        m.onDisable();
    }

    @Test void strafeXSetterDisablePreventsSubsequentZWrite() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189StrafeModule m = new Minecraft189StrafeModule(input);
        final Motion p = new Motion();
        input.key(LegacyKeyboardCodes.W,true);
        input.key(LegacyKeyboardCodes.A,true);m.onEnable();
        p.writeX=m::onDisable;
        m.apply(p,yawZero(),false);
        assertEquals(1,p.wx);
        assertEquals(0,p.wz);
        assertFalse(m.active());
    }

    @Test void strafeXSetterSmoothPolicyEditVetoesOldZ() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189StrafeModule m = new Minecraft189StrafeModule(input);
        final Motion p = new Motion();
        input.key(LegacyKeyboardCodes.W,true);
        input.key(LegacyKeyboardCodes.A,true);m.onEnable();
        p.writeX=()->m.smoothAccelerationSetting().set(Boolean.TRUE);
        m.apply(p,yawZero(),false);
        assertEquals(1,p.wx);
        assertEquals(0,p.wz);
        m.onDisable();
    }

    @Test void strafeGenuineDiagonalAndSourceSmoothingStillWork() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189StrafeModule m = new Minecraft189StrafeModule(input);
        final Motion p = new Motion();
        input.key(LegacyKeyboardCodes.W,true);
        input.key(LegacyKeyboardCodes.A,true);m.onEnable();
        m.apply(p,yawZero(),false);
        assertEquals(0.30D/Math.sqrt(2D),p.x,0.000001D);
        assertEquals(0.30D/Math.sqrt(2D),p.z,0.000001D);
        p.x=0D;p.z=0D;
        m.smoothAccelerationSetting().set(Boolean.TRUE);
        m.apply(p,yawZero(),false);
        assertEquals(0.15D/Math.sqrt(2D),p.x,0.000001D);
        assertEquals(0.15D/Math.sqrt(2D),p.z,0.000001D);
        m.onDisable();
    }

    @Test void strafeUnknownSourceOrNoPhysicalInputNeverReadsPlayerAxes() {
        final Minecraft189InputState input = new Minecraft189InputState();
        final Minecraft189StrafeModule m = new Minecraft189StrafeModule(input);
        final Motion p = new Motion();m.onEnable();
        m.apply(p,yawZero(),false);
        assertEquals(0,p.rx+p.rz);
        input.key(LegacyKeyboardCodes.W,true);
        m.apply(p,yawZero(),true);
        assertEquals(0,p.rx+p.rz);
        m.onDisable();
    }

    @Test void freezeXGetterOffOnVetoesAllOldAxes() {
        final Minecraft189FreezeModule m=new Minecraft189FreezeModule();
        final Motion p=new Motion();p.x=.1;p.y=.2;p.z=.3;
        m.onEnable();p.readX=()->{m.onDisable();m.onEnable();};
        m.apply(p);
        assertEquals(1,p.rx);
        assertEquals(0,p.ry+p.rz+p.wx+p.wy+p.wz);
        m.apply(p);
        assertEquals(0D,p.x,0.000001D);
        assertEquals(0D,p.y,0.000001D);
        assertEquals(0D,p.z,0.000001D);
        m.onDisable();
    }

    @Test void freezeXGetterAxisEditStopsAllOldWrites() {
        final Minecraft189FreezeModule m=new Minecraft189FreezeModule();
        final Motion p=new Motion();p.x=.1;p.y=.2;p.z=.3;
        m.onEnable();
        p.readX=()->m.freezeHorizontalSetting().set(Boolean.FALSE);
        m.apply(p);
        assertEquals(0,p.wx+p.wy+p.wz);
        m.apply(p);
        assertEquals(.1,p.x,0.000001D);
        assertEquals(0D,p.y,0.000001D);
        assertEquals(.3,p.z,0.000001D);
        m.onDisable();
    }

    @Test void freezeXSetterDisablePreventsYAndZReads() {
        final Minecraft189FreezeModule m=new Minecraft189FreezeModule();
        final Motion p=new Motion();p.x=.1;p.y=.2;p.z=.3;
        m.onEnable();p.writeX=m::onDisable;
        m.apply(p);
        assertEquals(1,p.wx);
        assertEquals(0,p.ry+p.rz+p.wy+p.wz);
        assertEquals(.2,p.y,0.000001D);
    }

    @Test void freezeYGetterDisablePreventsYSetterAndZRead() {
        final Minecraft189FreezeModule m=new Minecraft189FreezeModule();
        final Motion p=new Motion();p.y=.2;p.z=.3;
        m.onEnable();p.readY=m::onDisable;
        m.apply(p);
        assertEquals(1,p.ry);
        assertEquals(0,p.wy+p.rz+p.wz);
    }

    @Test void freezeYSetterAxisEditStopsZAndRetainsRealXWrite() {
        final Minecraft189FreezeModule m=new Minecraft189FreezeModule();
        final Motion p=new Motion();p.x=.1;p.y=.2;p.z=.3;
        m.onEnable();p.writeY=()->m.freezeHorizontalSetting().set(Boolean.FALSE);
        m.apply(p);
        assertEquals(1,p.wx);
        assertEquals(1,p.wy);
        assertEquals(0,p.rz+p.wz);
        m.onDisable();
    }

    @Test void freezeOnlyVerticalReadsAndWritesActualY() {
        final Minecraft189FreezeModule m=new Minecraft189FreezeModule();
        final Motion p=new Motion();p.x=.2;p.y=-.35;p.z=.4;
        m.freezeHorizontalSetting().set(Boolean.FALSE);
        m.onEnable();m.apply(p);
        assertEquals(0,p.rx+p.rz+p.wx+p.wz);
        assertEquals(1,p.ry);
        assertEquals(1,p.wy);
        assertEquals(0D,p.y,0.000001D);
        assertEquals(.2,p.x,0.000001D);
        m.onDisable();
    }

    @Test void freezeGuiPauseVetoRetainsOriginalMotionAndSkipsGetters() {
        final Minecraft189FreezeModule m=new Minecraft189FreezeModule();
        final Motion p=new Motion();p.x=.2;p.y=.3;p.z=.4;
        m.pauseWhileClickGuiSetting().set(Boolean.TRUE);
        m.onEnable();m.apply(p,true);
        assertEquals(0,p.rx+p.ry+p.rz+p.wx+p.wy+p.wz);
        assertEquals(.2,p.x,0.000001D);
        assertEquals(.3,p.y,0.000001D);
        assertEquals(.4,p.z,0.000001D);
        m.apply(p,false);
        assertEquals(0D,p.x,0.000001D);
        assertEquals(0D,p.y,0.000001D);
        assertEquals(0D,p.z,0.000001D);
        m.onDisable();
    }
}
