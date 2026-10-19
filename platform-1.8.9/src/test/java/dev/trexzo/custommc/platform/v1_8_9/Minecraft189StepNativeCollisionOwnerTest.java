package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Original mapped-player adapter evidence; genuine Mojang CI is separate. */
final class Minecraft189StepNativeCollisionOwnerTest {
    private static Minecraft189PlayerMovementState.Snapshot movement(
            boolean ground, boolean sneak) {
        final Minecraft189PlayerMovementState state =
                new Minecraft189PlayerMovementState();
        state.update(ground, sneak, false);
        return state.snapshot();
    }
    private static final class Player implements Minecraft189PlayerStepControl {
        float height = .6F;
        int reads, writes;
        Runnable onRead,onWrite;
        boolean failRead,failWrite;
        @Override public float customMcStepHeight() {
            reads++;
            if(reads>10)throw new AssertionError("recursive original step getter");
            final Runnable callback=onRead;onRead=null;
            if(callback!=null)callback.run();
            if(failRead)throw new IllegalStateException("original step getter");
            return height;
        }
        @Override public void customMcSetStepHeight(float value) {
            writes++;
            if(writes>10)throw new AssertionError("recursive original step setter");
            height=value;
            final Runnable callback=onWrite;onWrite=null;
            if(callback!=null)callback.run();
            if(failWrite)throw new IllegalStateException("original step setter");
        }
    }
    private static Minecraft189StepModule enabled() {
        final Minecraft189StepModule m=new Minecraft189StepModule();
        m.onEnable();return m;
    }
    @Test void ordinaryOriginalStepSetsLegacyOneBlock() {
        final Minecraft189StepModule m=enabled();final Player p=new Player();
        m.apply(p);assertEquals(1F,p.height,1E-6F);
        assertEquals(1,p.reads);assertEquals(1,p.writes);
        m.apply(p);assertEquals(1,p.writes);
    }
    @Test void nestedSamePlayerGetterNeverReadsTwice() {
        final Minecraft189StepModule m=enabled();final Player p=new Player();
        p.onRead=()->m.apply(p);
        m.apply(p);assertEquals(1,p.reads);assertEquals(1,p.writes);
        assertEquals(1F,p.height,1E-6F);
    }
    @Test void nestedSamePlayerSetterNeverDoubleWrites() {
        final Minecraft189StepModule m=enabled();final Player p=new Player();
        p.onWrite=()->m.apply(p);
        m.apply(p);assertEquals(1,p.reads);assertEquals(1,p.writes);
    }
    @Test void differentOriginalPlayerGetterRevokesOldStepBoost() {
        final Minecraft189StepModule m=enabled();
        final Player a=new Player(),b=new Player();
        a.onRead=()->m.apply(b);
        m.apply(a);assertEquals(.6F,a.height,1E-6F);
        assertEquals(0,a.writes+b.reads+b.writes);
    }
    @Test void differentOriginalPlayerSetterCannotBorrowOldStack() {
        final Minecraft189StepModule m=enabled();
        final Player a=new Player(),b=new Player();
        a.onWrite=()->m.apply(b);
        m.apply(a);assertEquals(1F,a.height,1E-6F);
        assertEquals(.6F,b.height,1E-6F);
        assertEquals(1,a.writes);assertEquals(0,b.reads+b.writes);
    }
    @Test void nativeGuiFocusInsideGetterRestoresVanillaFromBoost() {
        final Minecraft189StepModule m=enabled();final Player p=new Player();
        p.height=1.8F;
        p.onRead=()->m.apply(p,movement(true,false),true);
        m.apply(p,movement(true,false),false);
        assertEquals(.6F,p.height,1E-6F);assertEquals(1,p.writes);
    }
    @Test void nativeGuiFocusInsideSetterRestoresVanillaAfterBoost() {
        final Minecraft189StepModule m=enabled();final Player p=new Player();
        p.onWrite=()->m.apply(p,movement(true,false),true);
        m.apply(p,movement(true,false),false);
        assertEquals(.6F,p.height,1E-6F);assertEquals(2,p.writes);
    }
    @Test void originalGetterDisableRestoresVanillaCollision() {
        final Minecraft189StepModule m=enabled();final Player p=new Player();
        p.height=1.7F;
        p.onRead=m::onDisable;
        m.apply(p);assertEquals(.6F,p.height,1E-6F);
        assertEquals(1,p.writes);assertFalse(m.active());
    }
    @Test void originalSetterDisableRestoresVanillaCollision() {
        final Minecraft189StepModule m=enabled();final Player p=new Player();
        p.onWrite=m::onDisable;
        m.apply(p);assertEquals(.6F,p.height,1E-6F);
        assertEquals(2,p.writes);assertFalse(m.active());
    }
    @Test void getterOffOnNeverWritesOldStepIntoNewOwner() {
        final Minecraft189StepModule m=enabled();final Player p=new Player();
        p.onRead=()->{m.onDisable();m.onEnable();m.apply(p);};
        m.apply(p);assertEquals(.6F,p.height,1E-6F);
        assertEquals(0,p.writes);
        m.heightPercentSetting().set(150);
        m.apply(p);assertEquals(1.5F,p.height,1E-6F);
    }
    @Test void setterOffOnDoesNotClobberNewOwnerVanillaHeight() {
        final Minecraft189StepModule m=enabled();final Player p=new Player();
        p.onWrite=()->{m.onDisable();m.onEnable();m.apply(p);};
        m.apply(p);assertEquals(1F,p.height,1E-6F);
        assertEquals(1,p.writes);
        m.heightPercentSetting().set(150);
        m.apply(p);assertEquals(1.5F,p.height,1E-6F);
    }
    @Test void groundGateLostInsideGetterRestoresOldBoostToVanilla() {
        final Minecraft189StepModule m=enabled();final Player p=new Player();
        m.groundOnlySetting().set(Boolean.TRUE);p.height=2F;
        p.onRead=()->m.apply(p,movement(false,false),false);
        m.apply(p,movement(true,false),false);
        assertEquals(.6F,p.height,1E-6F);assertEquals(1,p.writes);
    }
    @Test void sneakGateLostInsideGetterRestoresVanilla() {
        final Minecraft189StepModule m=enabled();final Player p=new Player();
        m.pauseWhileSneakingSetting().set(Boolean.TRUE);p.height=2F;
        p.onRead=()->m.apply(p,movement(true,true),false);
        m.apply(p,movement(true,false),false);
        assertEquals(.6F,p.height,1E-6F);assertEquals(1,p.writes);
    }
    @Test void unknownOriginalMovementInvalidatesOptInGroundStep() {
        final Minecraft189StepModule m=enabled();final Player p=new Player();
        m.groundOnlySetting().set(Boolean.TRUE);p.height=2F;
        p.onRead=()->m.apply(p,new Minecraft189PlayerMovementState().snapshot(),false);
        m.apply(p,movement(true,false),false);
        assertEquals(.6F,p.height,1E-6F);
    }
    @Test void nativeOriginalGetterPolicyEditNeverWritesOldBoost() {
        final Minecraft189StepModule m=enabled();final Player p=new Player();
        p.onRead=()->m.heightPercentSetting().set(180);
        m.apply(p);assertEquals(.6F,p.height,1E-6F);
        assertEquals(0,p.writes);m.apply(p);
        assertEquals(1.8F,p.height,1E-6F);
    }
    @Test void nativeOriginalGetterThrowReleasesGuard() {
        final Minecraft189StepModule m=enabled();final Player p=new Player();
        p.failRead=true;
        assertThrows(IllegalStateException.class,()->m.apply(p));
        p.failRead=false;m.apply(p);
        assertEquals(1F,p.height,1E-6F);assertEquals(1,p.writes);
    }
    @Test void nativeSetterThrowRetainsActualPartialVanillaWrite() {
        final Minecraft189StepModule m=enabled();final Player p=new Player();
        p.failWrite=true;
        assertThrows(IllegalStateException.class,()->m.apply(p));
        assertEquals(1F,p.height,1E-6F);
        p.failWrite=false;m.heightPercentSetting().set(140);
        m.apply(p);assertEquals(1.4F,p.height,1E-6F);
    }
    @Test void originalGuiFocusRestoresActualVanillaWithoutSourceRecursion() {
        final Minecraft189StepModule m=enabled();final Player p=new Player();
        p.height=2F;
        m.apply(p,movement(true,false),true);
        assertEquals(.6F,p.height,1E-6F);assertEquals(1,p.writes);
    }
    @Test void airOverrideRetainsGenuineMappedAirborneHeight() {
        final Minecraft189StepModule m=enabled();final Player p=new Player();
        m.airborneOverrideSetting().set(Boolean.TRUE);
        m.airborneHeightPercentSetting().set(70);
        m.apply(p,movement(false,false),false);
        assertEquals(.7F,p.height,1E-6F);
        m.apply(p,movement(true,false),false);
        assertEquals(1F,p.height,1E-6F);
    }
    @Test void disconnectedMovementAndGroundGateRestoreVanilla() {
        final Minecraft189StepModule m=enabled();final Player p=new Player();
        m.groundOnlySetting().set(Boolean.TRUE);
        p.height=2F;
        m.apply(p,new Minecraft189PlayerMovementState().snapshot(),false);
        assertEquals(.6F,p.height,1E-6F);
    }
    @Test void corruptedOriginalStepHeightIsRepairedToVanillaDisabled() {
        final Minecraft189StepModule m=enabled();final Player p=new Player();
        p.height=Float.NaN;m.onDisable();m.apply(p);
        assertEquals(.6F,p.height,1E-6F);
    }
    @Test void nestedNullOriginalPlayerDoesNotInventSecondStepOwner() {
        final Minecraft189StepModule m=enabled();final Player p=new Player();
        p.onRead=()->m.apply((Minecraft189PlayerStepControl)null);
        m.apply(p);assertEquals(.6F,p.height,1E-6F);
        assertEquals(0,p.writes);
    }
}
