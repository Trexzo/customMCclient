package dev.trexzo.custommc.platform.v1_8_9;

import java.util.function.DoubleSupplier;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Original mapped yaw native callback and randomized source timing. */
final class Minecraft189SpinNativeYawEpochTest {
    private static final class NativeYaw
            implements Minecraft189PlayerRotationControl {
        float yaw;
        int writes;
        Runnable afterWrite;
        boolean failAfterWrite;
        @Override public float customMcRotationYaw() { return yaw; }
        @Override public float customMcRotationPitch() { return 0F; }
        @Override public void customMcSetRotationYaw(final float value) {
            yaw = value;
            writes++;
            Runnable callback=afterWrite;
            afterWrite=null;
            if(callback!=null)callback.run();
            if(failAfterWrite) {
                failAfterWrite=false;
                throw new IllegalStateException("vanilla yaw setter mutated");
            }
        }
        @Override public void customMcSetRotationPitch(final float value) {
            throw new AssertionError("Spin only owns native yaw");
        }
    }
    private static final class Rig {
        final Minecraft189SpinModule module;
        final NativeYaw player = new NativeYaw();
        final Minecraft189PlayerRotationState source =
                new Minecraft189PlayerRotationState();
        Rig() { this(() -> .50D); }
        Rig(DoubleSupplier random) {
            module = new Minecraft189SpinModule(random);
            module.onEnable();
        }
        boolean apply() {
            source.update(player.yaw,0F);
            return module.apply(player,source.snapshot(),true);
        }
    }
    @Test void stableOriginalYawHasExistingTwentyDegreeSourceStep() {
        Rig r=new Rig();
        assertTrue(r.apply());
        assertEquals(20F,r.player.yaw,.00001F);
        assertEquals(1,r.player.writes);
        assertTrue(r.apply());
        assertEquals(40F,r.player.yaw,.00001F);
        assertEquals(2,r.player.writes);
    }
    @Test void disabledModuleNeverWritesNativeSourceYaw() {
        Rig r=new Rig();
        r.module.onDisable();
        assertFalse(r.apply());
        assertEquals(0,r.player.writes);
    }
    @Test void nativeYawSetterDisableInvalidatesCompletionAndCadence() {
        Rig r=new Rig();
        r.player.afterWrite=r.module::onDisable;
        assertFalse(r.apply());
        assertEquals(1,r.player.writes);
        assertFalse(r.module.active());
        assertFalse(r.apply());
        assertEquals(1,r.player.writes);
    }
    @Test void nativeYawSetterOffOnCannotSpendOldActivationSuccess() {
        Rig r=new Rig();
        r.player.afterWrite=()->{r.module.onDisable();r.module.onEnable();};
        assertFalse(r.apply());
        assertEquals(1,r.player.writes);
        assertTrue(r.apply());
        assertEquals(2,r.player.writes);
    }
    @Test void nativeYawCallbackChangingReverseRevokesOldStep() {
        Rig r=new Rig();
        r.player.afterWrite=()->r.module.reverseSetting().set(true);
        assertFalse(r.apply());
        assertEquals(1,r.player.writes);
        assertTrue(r.apply());
        assertEquals(0F,r.player.yaw,.00001F);
    }
    @Test void nativeYawCallbackChangingSpeedRevokesOldStep() {
        Rig r=new Rig();
        r.player.afterWrite=()->r.module.yawSpeedSetting().set(10D);
        assertFalse(r.apply());
        assertTrue(r.apply());
        assertEquals(30F,r.player.yaw,.00001F);
    }
    @Test void nativeYawCallbackChangingHoldPolicyRevokesOldStep() {
        Rig r=new Rig();
        r.player.afterWrite=()->r.module.requireHoldSetting().set(true);
        assertFalse(r.apply());
        assertEquals(1,r.player.writes);
    }
    @Test void nativeYawCallbackChangingGroundPolicyRevokesOldStep() {
        Rig r=new Rig();
        r.player.afterWrite=()->r.module.groundOnlySetting().set(true);
        assertFalse(r.apply());
        assertEquals(1,r.player.writes);
        assertFalse(r.apply());
        assertEquals(1,r.player.writes);
    }
    @Test void nativeYawCallbackChangingSneakPolicyRevokesOldStep() {
        Rig r=new Rig();
        r.player.afterWrite=()->r.module.pauseWhileSneakingSetting().set(true);
        assertFalse(r.apply());
        assertEquals(1,r.player.writes);
    }
    @Test void nativeYawCallbackChangingBaseIntervalRevokesOldSchedule() {
        Rig r=new Rig();
        r.player.afterWrite=()->r.module.intervalTicksSetting().set(4);
        assertFalse(r.apply());
        assertEquals(1,r.player.writes);
    }
    @Test void nativeYawCallbackChangingRandomIntervalPolicyRevokesOldSchedule() {
        Rig r=new Rig();
        r.player.afterWrite=()->r.module.randomIntervalSetting().set(true);
        assertFalse(r.apply());
        assertEquals(1,r.player.writes);
    }
    @Test void injectedRandomizedSourceDisablingModuleCannotWriteYaw() {
        final Minecraft189SpinModule[] owner=new Minecraft189SpinModule[1];
        Rig r=new Rig(()->{owner[0].onDisable();return .25D;});
        owner[0]=r.module;
        r.module.randomIntervalSetting().set(true);
        assertFalse(r.apply());
        assertEquals(0,r.player.writes);
        assertFalse(r.module.active());
    }
    @Test void injectedRandomizedSourceGuiEditCannotWriteStaleYaw() {
        final Minecraft189SpinModule[] owner=new Minecraft189SpinModule[1];
        Rig r=new Rig(()->{owner[0].reverseSetting().set(true);return .25D;});
        owner[0]=r.module;
        r.module.randomIntervalSetting().set(true);
        assertFalse(r.apply());
        assertEquals(0,r.player.writes);
        assertTrue(r.apply());
        assertEquals(-20F,r.player.yaw,.00001F);
    }
    @Test void fixedOriginalIntervalStillSkipsExactlyTwoOutOfThreeTicks() {
        Rig r=new Rig();
        r.module.intervalTicksSetting().set(3);
        assertTrue(r.apply());
        assertFalse(r.apply());
        assertFalse(r.apply());
        assertTrue(r.apply());
        assertEquals(2,r.player.writes);
    }
    @Test void originalRandomIntervalRespectsBoundedSampleCadence() {
        Rig r=new Rig(()->.99D);
        r.module.randomIntervalSetting().set(true);
        r.module.intervalTicksSetting().set(3);
        r.module.intervalVariationTicksSetting().set(1);
        assertTrue(r.apply());
        assertFalse(r.apply());
        assertFalse(r.apply());
        assertFalse(r.apply());
        assertTrue(r.apply());
        assertEquals(2,r.player.writes);
    }
    @Test void originalYawSetterThrowsAfterRealMutationWithoutForgedRollback() {
        Rig r=new Rig();
        r.player.failAfterWrite=true;
        assertThrows(IllegalStateException.class,r::apply);
        assertEquals(1,r.player.writes);
        assertEquals(20F,r.player.yaw,.00001F);
    }
}
