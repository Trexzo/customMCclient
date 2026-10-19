package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Original PlayerControllerMP native adapter; Mojang X11 suite is separate. */
final class Minecraft189SpeedMineNativeControllerOwnerTest {
    private static final class Mine implements Minecraft189BlockMiningControl {
        boolean hitting=true;
        float progress=.1F;
        int hits,reads,writes;
        Runnable onHit,onRead,onWrite;
        boolean throwHit,throwRead,throwWrite;
        @Override public boolean customMcIsHittingBlock() {
            hits++;if(hits>12)throw new AssertionError("native hit recursion");
            final Runnable c=onHit;onHit=null;if(c!=null)c.run();
            if(throwHit)throw new IllegalStateException("original hit getter");
            return hitting;
        }
        @Override public float customMcBlockDamageProgress() {
            reads++;if(reads>12)throw new AssertionError("native progress recursion");
            final Runnable c=onRead;onRead=null;if(c!=null)c.run();
            if(throwRead)throw new IllegalStateException("original progress getter");
            return progress;
        }
        @Override public void customMcSetBlockDamageProgress(float value) {
            writes++;if(writes>12)throw new AssertionError("native progress setter recursion");
            progress=value;
            final Runnable c=onWrite;onWrite=null;if(c!=null)c.run();
            if(throwWrite)throw new IllegalStateException("original progress setter");
        }
    }
    private static Minecraft189PlayerMovementState.Snapshot movement(boolean ground, boolean sneak) {
        final Minecraft189PlayerMovementState m=new Minecraft189PlayerMovementState();
        m.update(ground,sneak,false);return m.snapshot();
    }
    private static Minecraft189SpeedMineModule module() {
        final Minecraft189SpeedMineModule m=new Minecraft189SpeedMineModule();
        m.onEnable();return m;
    }
    private static void apply(Minecraft189SpeedMineModule m,Mine x) {
        m.apply(x,false,null,false);
    }
    @Test void originalControllerRequiresTwoHitsBeforeOneProgressWrite() {
        final Minecraft189SpeedMineModule m=module();final Mine c=new Mine();
        apply(m,c);assertEquals(.7F,c.progress,1E-6F);
        assertEquals(2,c.hits);assertEquals(1,c.reads);assertEquals(1,c.writes);
    }
    @Test void sameControllerNestedFirstHitGetterCannotDoubleSpend() {
        final Minecraft189SpeedMineModule m=module();final Mine c=new Mine();
        c.onHit=()->apply(m,c);
        apply(m,c);assertEquals(2,c.hits);assertEquals(1,c.reads);assertEquals(1,c.writes);
    }
    @Test void sameControllerNestedProgressGetterCannotDoubleSpend() {
        final Minecraft189SpeedMineModule m=module();final Mine c=new Mine();
        c.onRead=()->apply(m,c);
        apply(m,c);assertEquals(2,c.hits);assertEquals(1,c.reads);assertEquals(1,c.writes);
    }
    @Test void sameControllerNestedSecondHitGetterCannotDoubleSpend() {
        final Minecraft189SpeedMineModule m=module();final Mine c=new Mine();
        c.onHit=()->{c.onHit=()->apply(m,c);};
        apply(m,c);assertEquals(2,c.hits);assertEquals(1,c.writes);
    }
    @Test void sameControllerNestedSetterCannotWriteRepeatedProgress() {
        final Minecraft189SpeedMineModule m=module();final Mine c=new Mine();
        c.onWrite=()->apply(m,c);
        apply(m,c);assertEquals(1,c.writes);assertEquals(2,c.hits);
    }
    @Test void differentControllerDuringFirstHitGetterRevokesOldRead() {
        final Minecraft189SpeedMineModule m=module();
        final Mine a=new Mine(),b=new Mine();
        a.onHit=()->apply(m,b);
        apply(m,a);assertEquals(0,a.reads+a.writes+b.hits+b.reads+b.writes);
    }
    @Test void differentControllerDuringProgressGetterRevokesOldWrite() {
        final Minecraft189SpeedMineModule m=module();
        final Mine a=new Mine(),b=new Mine();
        a.onRead=()->apply(m,b);
        apply(m,a);assertEquals(0,a.writes+b.hits+b.reads+b.writes);
        assertEquals(1,a.hits);
    }
    @Test void differentControllerDuringSecondHitGetterRevokesOldProgress() {
        final Minecraft189SpeedMineModule m=module();
        final Mine a=new Mine(),b=new Mine();
        a.onHit=()->{a.onHit=()->apply(m,b);};
        apply(m,a);assertEquals(0,a.writes+b.hits+b.reads+b.writes);
    }
    @Test void differentControllerDuringProgressSetterCannotBorrowOtherSource() {
        final Minecraft189SpeedMineModule m=module();
        final Mine a=new Mine(),b=new Mine();
        a.onWrite=()->apply(m,b);
        apply(m,a);assertEquals(.7F,a.progress,1E-6F);
        assertEquals(1,a.writes);assertEquals(0,b.hits+b.reads+b.writes);
    }
    @Test void nullControllerDuringGetterCancelsOriginalProgress() {
        final Minecraft189SpeedMineModule m=module();final Mine c=new Mine();
        c.onHit=()->m.apply((Minecraft189BlockMiningControl)null);
        apply(m,c);assertEquals(0,c.reads+c.writes);
    }
    @Test void originalGuiFocusDuringHitGetterVetoesNativeProgress() {
        final Minecraft189SpeedMineModule m=module();final Mine c=new Mine();
        c.onHit=()->m.apply(c,true,movement(true,false),true);
        apply(m,c);assertEquals(0,c.reads+c.writes);
    }
    @Test void sourceDisabledDuringHitGetterCannotWriteProgress() {
        final Minecraft189SpeedMineModule m=module();final Mine c=new Mine();
        c.onHit=m::onDisable;
        apply(m,c);assertEquals(0,c.reads+c.writes);assertFalse(m.active());
    }
    @Test void offOnInsideProgressGetterDoesNotBorrowNewLifetime() {
        final Minecraft189SpeedMineModule m=module();final Mine c=new Mine();
        c.onRead=()->{m.onDisable();m.onEnable();apply(m,c);};
        apply(m,c);assertEquals(0,c.writes);
        apply(m,c);assertEquals(.7F,c.progress,1E-6F);
    }
    @Test void realAttackReleasedDuringGetterVetoesOptedInSource() {
        final Minecraft189SpeedMineModule m=module();final Mine c=new Mine();
        m.requireAttackHeldSetting().set(Boolean.TRUE);
        c.onHit=()->m.apply(c,false,movement(true,false),false);
        m.apply(c,true,movement(true,false),false);
        assertEquals(0,c.reads+c.writes);
    }
    @Test void originalSneakDuringProgressGetterVetoesOptedInSource() {
        final Minecraft189SpeedMineModule m=module();final Mine c=new Mine();
        m.pauseWhileSneakingSetting().set(Boolean.TRUE);
        c.onRead=()->m.apply(c,true,movement(true,true),false);
        m.apply(c,true,movement(true,false),false);
        assertEquals(0,c.writes);
    }
    @Test void originalGroundLossDuringHitGetterVetoesOptedInSource() {
        final Minecraft189SpeedMineModule m=module();final Mine c=new Mine();
        m.groundOnlySetting().set(Boolean.TRUE);
        c.onHit=()->m.apply(c,true,movement(false,false),false);
        m.apply(c,true,movement(true,false),false);
        assertEquals(0,c.reads+c.writes);
    }
    @Test void absentOriginalGroundEvidenceFailsClosedWhenRequired() {
        final Minecraft189SpeedMineModule m=module();final Mine c=new Mine();
        m.groundOnlySetting().set(Boolean.TRUE);
        m.apply(c,true,new Minecraft189PlayerMovementState().snapshot(),false);
        assertEquals(0,c.hits+c.reads+c.writes);
    }
    @Test void originalHitFlagLossBetweenTwoReadsBlocksProgressSetter() {
        final Minecraft189SpeedMineModule m=module();final Mine c=new Mine();
        c.onRead=()->c.hitting=false;
        apply(m,c);assertEquals(2,c.hits);assertEquals(0,c.writes);
    }
    @Test void originalProgressSettingEditVetoesStaleNativeSource() {
        final Minecraft189SpeedMineModule m=module();final Mine c=new Mine();
        c.onRead=()->m.progressPercentSetting().set(90);
        apply(m,c);assertEquals(0,c.writes);
        apply(m,c);assertEquals(.9F,c.progress,1E-6F);
    }
    @Test void originalProgressiveStepUsesGenuineCurrentControllerState() {
        final Minecraft189SpeedMineModule m=module();final Mine c=new Mine();
        m.progressiveSetting().set(Boolean.TRUE);
        m.stepPercentSetting().set(15);
        apply(m,c);assertEquals(.25F,c.progress,1E-6F);
        apply(m,c);assertEquals(.4F,c.progress,1E-6F);
    }
    @Test void originalAirborneOverrideUsesKnownMovementOnly() {
        final Minecraft189SpeedMineModule m=module();final Mine c=new Mine();
        m.airborneOverrideSetting().set(Boolean.TRUE);
        m.airborneProgressPercentSetting().set(45);
        m.apply(c,false,movement(false,false),false);
        assertEquals(.45F,c.progress,1E-6F);
        c.progress=.1F;apply(m,c);assertEquals(.7F,c.progress,1E-6F);
    }
    @Test void nonfiniteNativeProgressNeverCreatesSyntheticBreak() {
        final Minecraft189SpeedMineModule m=module();final Mine c=new Mine();
        c.progress=Float.NaN;apply(m,c);assertEquals(0,c.writes);
        c.progress=Float.POSITIVE_INFINITY;apply(m,c);assertEquals(0,c.writes);
    }
    @Test void nativeHitGetterExceptionReleasesControllerLease() {
        final Minecraft189SpeedMineModule m=module();final Mine c=new Mine();
        c.throwHit=true;assertThrows(IllegalStateException.class,()->apply(m,c));
        c.throwHit=false;apply(m,c);assertEquals(1,c.writes);
    }
    @Test void nativeProgressGetterExceptionReleasesControllerLease() {
        final Minecraft189SpeedMineModule m=module();final Mine c=new Mine();
        c.throwRead=true;assertThrows(IllegalStateException.class,()->apply(m,c));
        c.throwRead=false;apply(m,c);assertEquals(1,c.writes);
    }
    @Test void nativeSetterExceptionPreservesRealPartialBlockProgress() {
        final Minecraft189SpeedMineModule m=module();final Mine c=new Mine();
        c.throwWrite=true;assertThrows(IllegalStateException.class,()->apply(m,c));
        assertEquals(.7F,c.progress,1E-6F);
        c.throwWrite=false;c.progress=.1F;apply(m,c);assertEquals(2,c.writes);
    }
    @Test void disabledModuleNeverTouchesOriginalController() {
        final Minecraft189SpeedMineModule m=module();final Mine c=new Mine();
        m.onDisable();apply(m,c);assertEquals(0,c.hits+c.reads+c.writes);
    }
}
