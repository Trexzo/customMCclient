package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Mapped player yaw/pitch source setters, offline policy adapter. */
final class Minecraft189JitterNativeAxisEpochTest {
    private static final class NativePlayer
            implements Minecraft189PlayerRotationControl {
        float yaw, pitch;
        int yawWrites, pitchWrites;
        Runnable yawCallback, pitchCallback;
        boolean throwAfterYaw;
        @Override public float customMcRotationYaw() { return yaw; }
        @Override public float customMcRotationPitch() { return pitch; }
        @Override public void customMcSetRotationYaw(final float value) {
            yaw=value; yawWrites++;
            Runnable callback=yawCallback;
            yawCallback=null;
            if(callback!=null) callback.run();
            if(throwAfterYaw) {
                throwAfterYaw=false;
                throw new IllegalStateException("original yaw mutation then failure");
            }
        }
        @Override public void customMcSetRotationPitch(final float value) {
            pitch=value; pitchWrites++;
            Runnable callback=pitchCallback;
            pitchCallback=null;
            if(callback!=null) callback.run();
        }
    }
    private static final class Rig {
        final Minecraft189JitterModule module =
                new Minecraft189JitterModule(() -> .25D);
        final NativePlayer player = new NativePlayer();
        final Minecraft189PlayerRotationState source =
                new Minecraft189PlayerRotationState();
        Rig() { module.onEnable(); }
        boolean apply() {
            source.update(player.yaw,player.pitch);
            return module.apply(player,source.snapshot(),true);
        }
    }
    @Test void actualMappedYawPitchApplyOneStableOriginalStroke() {
        Rig r=new Rig();
        assertTrue(r.apply());
        assertEquals(1,r.player.yawWrites);
        assertEquals(1,r.player.pitchWrites);
        assertEquals(.5F,r.player.yaw,.00001F);
        assertEquals(.5F,r.player.pitch,.00001F);
        assertTrue(r.apply());
        assertEquals(0F,r.player.yaw,.00001F);
        assertEquals(0F,r.player.pitch,.00001F);
    }
    @Test void originalYawDisableCannotSpendPitchOfOldActivation() {
        Rig r=new Rig();
        r.player.yawCallback=r.module::onDisable;
        assertFalse(r.apply());
        assertEquals(1,r.player.yawWrites);
        assertEquals(0,r.player.pitchWrites);
        assertFalse(r.module.active());
    }
    @Test void originalYawOffOnCannotWriteOldPitchInNewActivation() {
        Rig r=new Rig();
        r.player.yawCallback=()->{r.module.onDisable();r.module.onEnable();};
        assertFalse(r.apply());
        assertEquals(0,r.player.pitchWrites);
        assertTrue(r.apply());
        assertEquals(1,r.player.pitchWrites);
    }
    @Test void originalYawSetterDisablingPitchStopsSecondSourceSetter() {
        Rig r=new Rig();
        r.player.yawCallback=()->r.module.pitchEnabledSetting().set(false);
        assertFalse(r.apply());
        assertEquals(0,r.player.pitchWrites);
    }
    @Test void originalYawChangingPitchStrengthRejectsObsoleteSecondAxis() {
        Rig r=new Rig();
        r.player.yawCallback=()->r.module.pitchDegreesSetting().set(1D);
        assertFalse(r.apply());
        assertEquals(0,r.player.pitchWrites);
    }
    @Test void originalYawChangingRandomIntervalPolicyRevokesOldPair() {
        Rig r=new Rig();
        r.player.yawCallback=()->r.module.randomIntervalSetting().set(true);
        assertFalse(r.apply());
        assertEquals(0,r.player.pitchWrites);
    }
    @Test void originalYawChangingPhysicalHoldPolicyRevokesOldPair() {
        Rig r=new Rig();
        r.player.yawCallback=()->r.module.requireHoldSetting().set(false);
        assertFalse(r.apply());
        assertEquals(0,r.player.pitchWrites);
    }
    @Test void originalYawChangingGuiRightPauseRevokesOldPair() {
        Rig r=new Rig();
        r.player.yawCallback=()->r.module.pauseWhileRightClickingSetting().set(true);
        assertFalse(r.apply());
        assertEquals(0,r.player.pitchWrites);
    }
    @Test void originalYawChangingVariationStrengthCannotKeepOldPhase() {
        Rig r=new Rig();
        r.module.variableStrengthSetting().set(true);
        r.player.yawCallback=()->r.module.strengthVariationPercentSetting().set(50);
        assertFalse(r.apply());
        assertEquals(0,r.player.pitchWrites);
        assertTrue(r.apply());
        assertEquals(1,r.player.pitchWrites);
    }
    @Test void originalPitchSetterDisableCannotReportOldPairSuccess() {
        Rig r=new Rig();
        r.player.pitchCallback=r.module::onDisable;
        assertFalse(r.apply());
        assertEquals(1,r.player.yawWrites);
        assertEquals(1,r.player.pitchWrites);
    }
    @Test void originalPitchSetterChangingIntervalCannotRetainOldPhase() {
        Rig r=new Rig();
        r.player.pitchCallback=()->r.module.intervalTicksSetting().set(3);
        assertFalse(r.apply());
        assertEquals(1,r.player.pitchWrites);
        assertTrue(r.apply());
        assertEquals(2,r.player.yawWrites);
    }
    @Test void nativeOriginalYawSetterThrowNeverWritesPitch() {
        Rig r=new Rig();
        r.player.throwAfterYaw=true;
        assertThrows(IllegalStateException.class,r::apply);
        assertEquals(1,r.player.yawWrites);
        assertEquals(0,r.player.pitchWrites);
    }
    @Test void realYawOnlyModeNeverInventsPitchSetter() {
        Rig r=new Rig();
        r.module.pitchEnabledSetting().set(false);
        assertTrue(r.apply());
        assertEquals(1,r.player.yawWrites);
        assertEquals(0,r.player.pitchWrites);
    }
    @Test void realPitchOnlyModeNeverInventsYawSetter() {
        Rig r=new Rig();
        r.module.yawEnabledSetting().set(false);
        assertTrue(r.apply());
        assertEquals(0,r.player.yawWrites);
        assertEquals(1,r.player.pitchWrites);
    }
    @Test void disabledModuleCannotChangeOriginalPlayerRotation() {
        Rig r=new Rig();
        r.module.onDisable();
        assertFalse(r.apply());
        assertEquals(0,r.player.yawWrites+r.player.pitchWrites);
    }
    @Test void unchangedRandomPairedStrokeStillReturnsToOriginalRotation() {
        Rig r=new Rig();
        r.module.variableStrengthSetting().set(true);
        r.module.strengthVariationPercentSetting().set(50);
        assertTrue(r.apply());
        float outboundYaw=r.player.yaw;
        assertTrue(outboundYaw>0F && outboundYaw<.5F);
        assertTrue(r.apply());
        assertEquals(0F,r.player.yaw,.00001F);
        assertEquals(0F,r.player.pitch,.00001F);
    }
}
