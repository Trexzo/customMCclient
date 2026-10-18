package dev.trexzo.custommc.platform.v1_8_9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Original mapped player yaw/pitch setter reentrancy.
 * Offline Java adapter policy, not actual Mojang 1.8.9 X11 proof.
 */
final class Minecraft189AimAssistNativeAxisEpochTest {
    private static final class NativeRotation
            implements Minecraft189PlayerRotationControl {
        float yaw=30F, pitch=15F;
        int yawWrites,pitchWrites;
        Runnable afterYaw,afterPitch;
        boolean throwAfterYaw;
        @Override public float customMcRotationYaw(){return yaw;}
        @Override public float customMcRotationPitch(){return pitch;}
        @Override public void customMcSetRotationYaw(float value){
            yaw=value;yawWrites++;
            Runnable hook=afterYaw;afterYaw=null;
            if(hook!=null)hook.run();
            if(throwAfterYaw) {
                throwAfterYaw=false;
                throw new IllegalStateException("native yaw setter mutated then failed");
            }
        }
        @Override public void customMcSetRotationPitch(float value){
            pitch=value;pitchWrites++;
            Runnable hook=afterPitch;afterPitch=null;
            if(hook!=null)hook.run();
        }
    }
    private static final class Fixture {
        final Minecraft189AimAssistModule m = new Minecraft189AimAssistModule();
        final NativeRotation player = new NativeRotation();
        final Minecraft189PlayerPositionState local =
                new Minecraft189PlayerPositionState();
        final Minecraft189WorldEntityPositionState positions =
                new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds =
                new Minecraft189WorldEntityKindState();
        final Minecraft189NearestPlayerTargetState nearest =
                new Minecraft189NearestPlayerTargetState();
        final Minecraft189TargetRotationState target =
                new Minecraft189TargetRotationState();
        final Minecraft189PlayerRotationState source =
                new Minecraft189PlayerRotationState();
        Fixture() {
            local.update(0D,0D,0D);
            positions.update(new double[]{0D,3D,10D});
            kinds.update(new int[]{
                    Minecraft189WorldEntityKindState.PLAYER
                        | Minecraft189WorldEntityKindState.LIVING});
            nearest.update(local.snapshot(),positions.snapshot(),kinds.snapshot());
            target.update(local.snapshot(),nearest.snapshot());
            m.onEnable();
        }
        boolean apply() {
            source.update(player.yaw,player.pitch);
            return m.apply(player,source.snapshot(),target.snapshot(),true);
        }
    }

    @Test void stableOriginalYawAndPitchBothWriteRealMappedPlayer() {
        Fixture f=new Fixture();
        assertTrue(f.apply());
        assertEquals(1,f.player.yawWrites);
        assertEquals(1,f.player.pitchWrites);
        assertEquals(0F,f.player.yaw,.0001F);
        assertTrue(f.player.pitch<0F);
    }
    @Test void nativeYawSetterDisableCancelsSecondOriginalPitchSetter() {
        Fixture f=new Fixture();
        f.player.afterYaw=f.m::onDisable;
        assertFalse(f.apply());
        assertEquals(1,f.player.yawWrites);
        assertEquals(0,f.player.pitchWrites);
        assertFalse(f.m.active());
    }
    @Test void nativeYawSetterOffOnCannotSpendNewLifePitchAuthority() {
        Fixture f=new Fixture();
        f.player.afterYaw=()->{f.m.onDisable();f.m.onEnable();};
        assertFalse(f.apply());
        assertEquals(0,f.player.pitchWrites);
        assertTrue(f.m.active());
        assertTrue(f.apply());
        assertEquals(1,f.player.pitchWrites);
    }
    @Test void sourceYawCallbackDisablingPitchPreservesUserPolicy() {
        Fixture f=new Fixture();
        f.player.afterYaw=()->f.m.pitchEnabledSetting().set(false);
        assertFalse(f.apply());
        assertEquals(0,f.player.pitchWrites);
    }
    @Test void sourceYawCallbackEditingPitchSpeedDoesNotWriteOldPitch() {
        Fixture f=new Fixture();
        f.player.afterYaw=()->f.m.pitchSpeedSetting().set(2D);
        assertFalse(f.apply());
        assertEquals(0,f.player.pitchWrites);
    }
    @Test void sourceYawCallbackEditingFovRejectsObsoleteSecondAxis() {
        Fixture f=new Fixture();
        f.player.afterYaw=()->f.m.maxFovSetting().set(40D);
        assertFalse(f.apply());
        assertEquals(0,f.player.pitchWrites);
    }
    @Test void nativeYawCallbackEditingHeldInputPolicyRevokesOldPitch() {
        Fixture f=new Fixture();
        f.player.afterYaw=()->f.m.requireHoldSetting().set(false);
        assertFalse(f.apply());
        assertEquals(0,f.player.pitchWrites);
    }
    @Test void nativeYawCallbackEditingCorrectionIntervalRevokesCadence() {
        Fixture f=new Fixture();
        f.m.correctionIntervalTicksSetting().set(2);
        f.player.afterYaw=()->f.m.correctionIntervalTicksSetting().set(3);
        assertFalse(f.apply());
        assertEquals(0,f.player.pitchWrites);
    }
    @Test void nativePitchSetterDisableInvalidatesCompletedDualAxisOwner() {
        Fixture f=new Fixture();
        f.player.afterPitch=f.m::onDisable;
        assertFalse(f.apply());
        assertEquals(1,f.player.yawWrites);
        assertEquals(1,f.player.pitchWrites);
    }
    @Test void nativePitchSetterGuiEditInvalidatesOldSourceDecision() {
        Fixture f=new Fixture();
        f.player.afterPitch=()->f.m.pitchOffsetSetting().set(6D);
        assertFalse(f.apply());
        assertEquals(1,f.player.pitchWrites);
    }
    @Test void onlyYawSourceAxisNeverInventsOriginalPitchWrite() {
        Fixture f=new Fixture();
        f.m.pitchEnabledSetting().set(false);
        assertTrue(f.apply());
        assertEquals(1,f.player.yawWrites);
        assertEquals(0,f.player.pitchWrites);
    }
    @Test void onlyPitchSourceAxisNeverInventsOriginalYawWrite() {
        Fixture f=new Fixture();
        f.m.yawEnabledSetting().set(false);
        assertTrue(f.apply());
        assertEquals(0,f.player.yawWrites);
        assertEquals(1,f.player.pitchWrites);
    }
    @Test void genuineYawSetterThrowsAfterMutationBeforeAnyPitchWrite() {
        Fixture f=new Fixture();
        f.player.throwAfterYaw=true;
        assertThrows(IllegalStateException.class,f::apply);
        assertEquals(1,f.player.yawWrites);
        assertEquals(0,f.player.pitchWrites);
    }
    @Test void disabledSourceModuleDoesNotCallEitherOriginalSetter() {
        Fixture f=new Fixture();
        f.m.onDisable();
        assertFalse(f.apply());
        assertEquals(0,f.player.yawWrites);
        assertEquals(0,f.player.pitchWrites);
    }
    @Test void genuineSourceSameRotationDoesNotIssueDuplicateNativeWrites() {
        Fixture f=new Fixture();
        assertTrue(f.apply());
        assertTrue(f.apply());
        assertEquals(1,f.player.yawWrites);
        assertEquals(1,f.player.pitchWrites);
    }
}
