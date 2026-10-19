package dev.trexzo.custommc.platform.v1_8_9;

import java.util.function.DoubleSupplier;
import java.util.function.IntConsumer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Source Jitter's external random interval/strength callbacks may reenter
 * module lifecycle before the first genuine mapped player rotation setter.
 * Offline Java interface policy test, NOT pinned Mojang game runtime proof.
 */
final class Minecraft189JitterNativeRandomOwnerTest {
    private static final class RandomSource implements DoubleSupplier {
        int samples;
        IntConsumer callback;
        @Override public double getAsDouble() {
            samples++;
            if(callback!=null) callback.accept(samples);
            return .25D;
        }
    }
    private static final class NativePlayer implements Minecraft189PlayerRotationControl {
        float yaw, pitch;
        int yawWrites, pitchWrites;
        @Override public float customMcRotationYaw() { return yaw; }
        @Override public float customMcRotationPitch() { return pitch; }
        @Override public void customMcSetRotationYaw(final float x) {
            yaw=x; yawWrites++;
        }
        @Override public void customMcSetRotationPitch(final float x) {
            pitch=x; pitchWrites++;
        }
    }
    private static final class Rig {
        final RandomSource random = new RandomSource();
        final Minecraft189JitterModule module = new Minecraft189JitterModule(random);
        final NativePlayer player = new NativePlayer();
        final Minecraft189PlayerRotationState rotation =
                new Minecraft189PlayerRotationState();
        Rig() { module.onEnable(); }
        boolean tick() {
            rotation.update(player.yaw,player.pitch);
            return module.apply(player,rotation.snapshot(),true);
        }
        void variable() {
            module.variableStrengthSetting().set(true);
            module.strengthVariationPercentSetting().set(50);
        }
        void timed() {
            module.randomIntervalSetting().set(true);
            module.intervalTicksSetting().set(1);
            module.intervalVariationTicksSetting().set(2);
        }
        void untouched() {
            assertEquals(0,player.yawWrites+player.pitchWrites,
                    "invalid RNG owner must never rotate original player");
        }
    }

    @Test void randomIntervalCallbackDisablePreventsAnyOriginalYawOrPitch() {
        Rig r=new Rig();r.timed();
        r.random.callback=(n)->r.module.onDisable();
        assertFalse(r.tick());r.untouched();
        assertEquals(1,r.random.samples);
    }
    @Test void randomIntervalCallbackOffOnCannotBorrowNewModuleActivation() {
        Rig r=new Rig();r.timed();
        r.random.callback=(n)->{r.module.onDisable();r.module.onEnable();};
        assertFalse(r.tick());r.untouched();
        r.random.callback=null;
        assertTrue(r.tick());
        assertEquals(2,r.player.yawWrites+r.player.pitchWrites);
    }
    @Test void randomIntervalCallbackChangingTimingPolicyBlocksRotation() {
        Rig r=new Rig();r.timed();
        r.random.callback=(n)->r.module.intervalVariationTicksSetting().set(1);
        assertFalse(r.tick());r.untouched();
        assertEquals(1,r.random.samples);
    }
    @Test void randomIntervalCallbackDisablingPitchAxisBlocksBothOldAxes() {
        Rig r=new Rig();r.timed();
        r.random.callback=(n)->r.module.pitchEnabledSetting().set(false);
        assertFalse(r.tick());r.untouched();
    }
    @Test void variableYawStrengthCallbackDisableBlocksPitchSampleAndWrites() {
        Rig r=new Rig();r.variable();
        r.random.callback=(n)->r.module.onDisable();
        assertFalse(r.tick());r.untouched();
        assertEquals(1,r.random.samples,"do not sample old pitch after owner revoked");
    }
    @Test void variableYawStrengthCallbackOffOnBlocksOldSourceStroke() {
        Rig r=new Rig();r.variable();
        r.random.callback=(n)->{r.module.onDisable();r.module.onEnable();};
        assertFalse(r.tick());r.untouched();
        assertEquals(1,r.random.samples);
    }
    @Test void variableYawStrengthCallbackGuiPitchDisableBlocksOldPair() {
        Rig r=new Rig();r.variable();
        r.random.callback=(n)->r.module.pitchEnabledSetting().set(false);
        assertFalse(r.tick());r.untouched();
        assertEquals(1,r.random.samples);
    }
    @Test void variablePitchStrengthCallbackDisablesBeforeNativeYaw() {
        Rig r=new Rig();r.variable();
        r.random.callback=(n)->{if(n==2)r.module.onDisable();};
        assertFalse(r.tick());r.untouched();
        assertEquals(2,r.random.samples);
    }
    @Test void variablePitchStrengthCallbackGuiEditBlocksBothOriginalSetters() {
        Rig r=new Rig();r.variable();
        r.random.callback=(n)->{
            if(n==2)r.module.strengthVariationPercentSetting().set(10);
        };
        assertFalse(r.tick());r.untouched();
        assertEquals(2,r.random.samples);
    }
    @Test void bothIntervalAndAmplitudeRandomNestedCallbackCannotRotate() {
        Rig r=new Rig();r.timed();r.variable();
        r.random.callback=(n)->{
            if(n==2)r.module.requireHoldSetting().set(false);
        };
        assertFalse(r.tick());r.untouched();
        assertEquals(2,r.random.samples,
                "do not consume pitch RNG after yaw sample changed source GUI");
    }
    @Test void fixedAmplitudeAndTimingNeverInvokeOptionalRng() {
        Rig r=new Rig();
        r.random.callback=(n)->{throw new AssertionError("no RNG in fixed mode");};
        assertTrue(r.tick());
        assertEquals(0,r.random.samples);
        assertEquals(.5F,r.player.yaw,.0001F);
        assertEquals(.5F,r.player.pitch,.0001F);
    }
    @Test void stableVariablePairedReturnKeepsOriginalVanillaYawPitch() {
        Rig r=new Rig();r.variable();
        assertTrue(r.tick());
        assertEquals(2,r.random.samples);
        assertTrue(r.player.yaw>0F&&r.player.yaw<.5F);
        assertTrue(r.tick());
        assertEquals(2,r.random.samples,"paired return reuses original deltas");
        assertEquals(0F,r.player.yaw,.00001F);
        assertEquals(0F,r.player.pitch,.00001F);
    }
    @Test void stableRandomizedIntervalAndStrengthCanPerformGenuineStroke() {
        Rig r=new Rig();r.timed();r.variable();
        assertTrue(r.tick());
        assertEquals(3,r.random.samples);
        assertEquals(1,r.player.yawWrites);
        assertEquals(1,r.player.pitchWrites);
    }
    @Test void originalModuleMayRestartThenUseNewAuthorizedRngAndSource() {
        Rig r=new Rig();r.variable();
        r.random.callback=(n)->{
            if(n==1){r.module.onDisable();r.module.onEnable();}
        };
        assertFalse(r.tick());r.untouched();
        r.random.callback=null;
        assertTrue(r.tick());
        assertEquals(2,r.player.yawWrites+r.player.pitchWrites);
    }
}
