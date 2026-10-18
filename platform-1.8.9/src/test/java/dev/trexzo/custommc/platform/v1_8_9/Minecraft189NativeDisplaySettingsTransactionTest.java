package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.platform.v1_8_9.ui.Minecraft189GuiSettingsAccess;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Actual mapped settings access interface, offline callbacks; not vanilla GUI. */
final class Minecraft189NativeDisplaySettingsTransactionTest {
    private static final class Settings implements Minecraft189GuiSettingsAccess {
        float gamma = .42F;
        boolean bobbing = true;
        float fov = 70F;
        Runnable onGammaRead, onGammaWrite, onBobbingRead, onBobbingWrite;
        boolean throwAfterGamma, throwAfterBobbing;
        int gammaWrites, bobbingWrites;

        @Override public float gammaSetting() {
            float old = gamma;
            if (onGammaRead != null) {
                Runnable callback=onGammaRead;onGammaRead=null;callback.run();
            }
            return old;
        }
        @Override public void gammaSetting(float next) {
            gamma=next;gammaWrites++;
            if(onGammaWrite!=null){
                Runnable callback=onGammaWrite;onGammaWrite=null;callback.run();
            }
            if(throwAfterGamma) {
                throwAfterGamma=false;
                throw new IllegalStateException("native Gamma setter wrote then failed");
            }
        }
        @Override public boolean viewBobbing() {
            boolean old=bobbing;
            if(onBobbingRead!=null){
                Runnable callback=onBobbingRead;onBobbingRead=null;callback.run();
            }
            return old;
        }
        @Override public void viewBobbing(boolean next) {
            bobbing=next;bobbingWrites++;
            if(onBobbingWrite!=null){
                Runnable callback=onBobbingWrite;onBobbingWrite=null;callback.run();
            }
            if(throwAfterBobbing){
                throwAfterBobbing=false;
                throw new IllegalStateException("native bobbing setter wrote then failed");
            }
        }
        @Override public float fovSetting() { return fov; }
        @Override public void fovSetting(float value) { fov=value; }
        @Override public int configuredGuiScale() { return 1; }
        @Override public boolean unicode() { return false; }
    }

    @Test void gammaOriginalGetterReentrantDisableCancelsBeforeNativeSetter() {
        Settings s=new Settings();Minecraft189FullbrightModule m=new Minecraft189FullbrightModule(s);
        s.onGammaRead=m::onDisable;
        m.onEnable();
        assertFalse(m.active());
        assertEquals(.42F,s.gamma,1E-6F);
        assertEquals(0,s.gammaWrites);
    }
    @Test void gammaSetterDisableAfterMutationRollsBackOriginalPreference() {
        Settings s=new Settings();Minecraft189FullbrightModule m=new Minecraft189FullbrightModule(s);
        s.onGammaWrite=m::onDisable;
        m.onEnable();
        assertFalse(m.active());
        assertEquals(.42F,s.gamma,1E-6F);
        assertEquals(2,s.gammaWrites);
        m.onEnable();
        assertTrue(m.active());
        m.onDisable();
        assertEquals(.42F,s.gamma,1E-6F);
    }
    @Test void gammaSetterUserEditIsNotClobberedByCancelledOwner() {
        Settings s=new Settings();Minecraft189FullbrightModule m=new Minecraft189FullbrightModule(s);
        s.onGammaWrite=()->{
            m.onDisable();
            s.gamma=.95F; // user changed native slider after override
        };
        m.onEnable();
        assertFalse(m.active());
        assertEquals(.95F,s.gamma,1E-6F);
    }
    @Test void gammaNativePartialWriteFailureRollsBackAndModuleRecovers() {
        Settings s=new Settings();Minecraft189FullbrightModule m=new Minecraft189FullbrightModule(s);
        s.throwAfterGamma=true;
        assertThrows(IllegalStateException.class,m::onEnable);
        assertFalse(m.active());
        assertEquals(.42F,s.gamma,1E-6F);
        m.onEnable();
        assertTrue(m.active());
        m.onDisable();
        assertEquals(.42F,s.gamma,1E-6F);
    }
    @Test void gammaRecursiveEnableIsRejectedBeforeCapturingDifferentBaseline() {
        Settings s=new Settings();Minecraft189FullbrightModule m=new Minecraft189FullbrightModule(s);
        s.onGammaRead=()->assertThrows(IllegalStateException.class,m::onEnable);
        m.onEnable();
        assertTrue(m.active());
        assertEquals(.42F,m.originalGamma(),1E-6F);
        m.onDisable();
        assertEquals(.42F,s.gamma,1E-6F);
    }
    @Test void gammaOriginalNativeUserEditAfterEnableSurvivesDisable() {
        Settings s=new Settings();Minecraft189FullbrightModule m=new Minecraft189FullbrightModule(s);
        m.onEnable();s.gamma=1.23F;m.onDisable();
        assertEquals(1.23F,s.gamma,1E-6F);
        assertFalse(m.active());
    }
    @Test void gammaNonfiniteReadFailsWithoutAttemptedSetter() {
        Settings s=new Settings();s.gamma=Float.NaN;
        Minecraft189FullbrightModule m=new Minecraft189FullbrightModule(s);
        assertThrows(IllegalArgumentException.class,m::onEnable);
        assertEquals(0,s.gammaWrites);
        assertFalse(m.active());
        s.gamma=.2F;m.onEnable();m.onDisable();
        assertEquals(.2F,s.gamma,1E-6F);
    }

    @Test void bobbingOriginalGetterReentrantDisableCancelsWithoutSetter() {
        Settings s=new Settings();Minecraft189NoBobbingModule m=new Minecraft189NoBobbingModule(s);
        s.onBobbingRead=m::onDisable;
        m.onEnable();
        assertFalse(m.active());
        assertTrue(s.bobbing);
        assertEquals(0,s.bobbingWrites);
    }
    @Test void bobbingNativeSetterDisableAfterMutationRestoresTrue() {
        Settings s=new Settings();Minecraft189NoBobbingModule m=new Minecraft189NoBobbingModule(s);
        s.onBobbingWrite=m::onDisable;
        m.onEnable();
        assertFalse(m.active());
        assertTrue(s.bobbing);
        assertEquals(2,s.bobbingWrites);
        m.onEnable();m.onDisable();assertTrue(s.bobbing);
    }
    @Test void bobbingSetterUserEditSurvivesCancelledOwner() {
        Settings s=new Settings();Minecraft189NoBobbingModule m=new Minecraft189NoBobbingModule(s);
        s.onBobbingWrite=()->{
            m.onDisable();
            s.bobbing=true;
        };
        m.onEnable();
        assertFalse(m.active());
        assertTrue(s.bobbing);
        assertEquals(1,s.bobbingWrites);
    }
    @Test void bobbingPartialNativeFailureRollsBackAndCanReenable() {
        Settings s=new Settings();Minecraft189NoBobbingModule m=new Minecraft189NoBobbingModule(s);
        s.throwAfterBobbing=true;
        assertThrows(IllegalStateException.class,m::onEnable);
        assertTrue(s.bobbing);
        assertFalse(m.active());
        m.onEnable();assertFalse(s.bobbing);
        m.onDisable();assertTrue(s.bobbing);
    }
    @Test void bobbingOriginalFalseRemainsUserOwnedAcrossEnableAndDisable() {
        Settings s=new Settings();s.bobbing=false;
        Minecraft189NoBobbingModule m=new Minecraft189NoBobbingModule(s);
        m.onEnable();assertFalse(m.originalViewBobbing());
        m.onDisable();assertFalse(s.bobbing);
        assertFalse(m.active());
    }
    @Test void bobbingNestedEnableCannotCorruptCapturedPreference() {
        Settings s=new Settings();Minecraft189NoBobbingModule m=new Minecraft189NoBobbingModule(s);
        s.onBobbingRead=()->assertThrows(IllegalStateException.class,m::onEnable);
        m.onEnable();
        assertTrue(m.originalViewBobbing());
        m.onDisable();
        assertTrue(s.bobbing);
    }
}
