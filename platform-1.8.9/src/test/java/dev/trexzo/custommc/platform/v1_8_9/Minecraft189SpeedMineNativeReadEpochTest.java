package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Real controller Java interface callbacks, not Mojang graphical proof. */
final class Minecraft189SpeedMineNativeReadEpochTest {
    private static final class Mining implements Minecraft189BlockMiningControl {
        boolean hitting = true;
        float progress = .1F;
        int hitReads, progressReads, writes;
        Runnable onFirstHit, onSecondHit, onProgressRead;
        @Override public boolean customMcIsHittingBlock() {
            hitReads++;
            final boolean old=hitting;
            Runnable callback=hitReads==1?onFirstHit:onSecondHit;
            if (hitReads==1) onFirstHit=null;
            if (hitReads==2) onSecondHit=null;
            if(callback!=null)callback.run();
            return old;
        }
        @Override public float customMcBlockDamageProgress() {
            progressReads++;
            final float old=progress;
            if(onProgressRead!=null){
                Runnable callback=onProgressRead;onProgressRead=null;callback.run();
            }
            return old;
        }
        @Override public void customMcSetBlockDamageProgress(float value) {
            writes++;progress=value;
        }
    }
    private static Minecraft189PlayerMovementState.Snapshot source(
            boolean ground, boolean sneaking) {
        Minecraft189PlayerMovementState st=new Minecraft189PlayerMovementState();
        st.update(ground,sneaking,false);
        return st.snapshot();
    }

    @Test void originalHitGetterRestartDoesNotReadProgressOrWrite() {
        Minecraft189SpeedMineModule m=new Minecraft189SpeedMineModule();
        Mining p=new Mining();m.onEnable();
        p.onFirstHit=()->{m.onDisable();m.onEnable();};
        m.apply(p,true,source(true,false),false);
        assertEquals(1,p.hitReads);
        assertEquals(0,p.progressReads+p.writes);
        m.onDisable();
    }
    @Test void originalProgressGetterRestartRevokesOldMiningWrite() {
        Minecraft189SpeedMineModule m=new Minecraft189SpeedMineModule();
        Mining p=new Mining();m.onEnable();
        p.onProgressRead=()->{m.onDisable();m.onEnable();};
        m.apply(p,true,source(true,false),false);
        assertEquals(1,p.progressReads);
        assertEquals(0,p.writes);
        m.onDisable();
    }
    @Test void originalProgressGetterSettingChangeCannotSpendStaleMinimum() {
        Minecraft189SpeedMineModule m=new Minecraft189SpeedMineModule();
        Mining p=new Mining();m.onEnable();
        p.onProgressRead=()->m.progressPercentSetting().set(90);
        m.apply(p,true,source(true,false),false);
        assertEquals(0,p.writes);
        m.apply(p,true,source(true,false),false);
        assertEquals(.90F,p.progress,.00001F);
        m.onDisable();
    }
    @Test void sourceMiningStopsDuringActualProgressGetterSuppressesSetter() {
        Minecraft189SpeedMineModule m=new Minecraft189SpeedMineModule();
        Mining p=new Mining();m.onEnable();
        p.onProgressRead=()->p.hitting=false;
        m.apply(p,true,source(true,false),false);
        assertEquals(2,p.hitReads);
        assertEquals(0,p.writes);
        m.onDisable();
    }
    @Test void secondSourceHitGetterDisableBlocksOldProgressWrite() {
        Minecraft189SpeedMineModule m=new Minecraft189SpeedMineModule();
        Mining p=new Mining();m.onEnable();
        p.onSecondHit=m::onDisable;
        m.apply(p,true,source(true,false),false);
        assertEquals(2,p.hitReads);
        assertEquals(0,p.writes);
    }
    @Test void secondSourceHitGetterSmoothModeEditRevokesPreviousPolicy() {
        Minecraft189SpeedMineModule m=new Minecraft189SpeedMineModule();
        Mining p=new Mining();m.onEnable();
        p.onSecondHit=()->m.progressiveSetting().set(Boolean.TRUE);
        m.apply(p,true,source(true,false),false);
        assertEquals(0,p.writes);
        m.apply(p,true,source(true,false),false);
        assertEquals(.20F,p.progress,.00001F);
        m.onDisable();
    }
    @Test void originalProgressiveNativeControllerStepsOnlyUpToConfiguredLimit() {
        Minecraft189SpeedMineModule m=new Minecraft189SpeedMineModule();
        Mining p=new Mining();m.onEnable();
        m.progressiveSetting().set(Boolean.TRUE);
        m.progressPercentSetting().set(30);
        m.stepPercentSetting().set(15);
        m.apply(p,true,source(true,false),false);
        assertEquals(.25F,p.progress,.00001F);
        p.hitReads=0;
        m.apply(p,true,source(true,false),false);
        assertEquals(.30F,p.progress,.00001F);
        assertEquals(2,p.writes);
        m.onDisable();
    }
    @Test void genuineAirbornePolicyChangesSourceNativeProgressTarget() {
        Minecraft189SpeedMineModule m=new Minecraft189SpeedMineModule();
        Mining p=new Mining();m.onEnable();
        m.airborneOverrideSetting().set(Boolean.TRUE);
        m.airborneProgressPercentSetting().set(55);
        m.apply(p,true,source(false,false),false);
        assertEquals(.55F,p.progress,.00001F);
        m.onDisable();
    }
    @Test void groundGateAndSneakVetoAvoidNativeGetterCalls() {
        Minecraft189SpeedMineModule m=new Minecraft189SpeedMineModule();
        Mining p=new Mining();m.onEnable();
        m.groundOnlySetting().set(Boolean.TRUE);
        m.apply(p,true,source(false,false),false);
        assertEquals(0,p.hitReads+p.progressReads+p.writes);
        m.pauseWhileSneakingSetting().set(Boolean.TRUE);
        m.apply(p,true,source(true,true),false);
        assertEquals(0,p.hitReads+p.progressReads+p.writes);
        m.onDisable();
    }
    @Test void realClickGuiAndDisabledModuleNeverQueryNativeBlock() {
        Minecraft189SpeedMineModule m=new Minecraft189SpeedMineModule();
        Mining p=new Mining();m.onEnable();
        m.apply(p,true,source(true,false),true);
        assertEquals(0,p.hitReads+p.progressReads+p.writes);
        m.onDisable();
        m.apply(p,true,source(true,false),false);
        assertEquals(0,p.hitReads+p.progressReads+p.writes);
    }
    @Test void originalControllerNotHittingPreventsProgressReads() {
        Minecraft189SpeedMineModule m=new Minecraft189SpeedMineModule();
        Mining p=new Mining();p.hitting=false;m.onEnable();
        m.apply(p,true,source(true,false),false);
        assertEquals(1,p.hitReads);
        assertEquals(0,p.progressReads+p.writes);
        m.onDisable();
    }
}
