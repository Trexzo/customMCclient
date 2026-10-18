package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.event.EventBus;
import dev.trexzo.custommc.platform.v1_8_9.ui.Minecraft189GuiSettingsAccess;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Genuine original mapped FOV adapter policy, not Mojang binary proof. */
final class Minecraft189FovNativeSettingsEpochTest {
    private static final class NativeSettings implements Minecraft189GuiSettingsAccess {
        float fov=70F;
        float gamma=.4F;
        boolean bobbing=true;
        int reads,writes;
        Runnable onFovRead,onFovWrite;
        boolean throwAfterFovWrite;
        @Override public float fovSetting() {
            reads++;
            final float original=fov;
            if(onFovRead!=null) {
                final Runnable action=onFovRead;onFovRead=null;action.run();
            }
            return original;
        }
        @Override public void fovSetting(float value) {
            fov=value;writes++;
            if(onFovWrite!=null){
                final Runnable action=onFovWrite;onFovWrite=null;action.run();
            }
            if(throwAfterFovWrite){
                throwAfterFovWrite=false;
                throw new IllegalStateException("real FOV setter mutated then failed");
            }
        }
        @Override public boolean viewBobbing(){return bobbing;}
        @Override public void viewBobbing(boolean v){bobbing=v;}
        @Override public float gammaSetting(){return gamma;}
        @Override public void gammaSetting(float v){gamma=v;}
        @Override public int configuredGuiScale(){return 1;}
        @Override public boolean unicode(){return false;}
    }

    @Test void reentrantDisableInsideNativeFovGetterCancelsBeforeSetter(){
        final NativeSettings settings=new NativeSettings();
        final Minecraft189FovModule module=new Minecraft189FovModule(settings,new EventBus());
        settings.onFovRead=module::onDisable;
        module.onEnable();
        assertFalse(module.active());
        assertEquals(70F,settings.fov,0.000001F);
        assertEquals(0,settings.writes);
        module.onEnable();
        assertTrue(module.active());
        module.onDisable();
        assertEquals(70F,settings.fov,0.000001F);
    }

    @Test void reentrantDisableInsideNativeSetterRollsBackUnfinishedOverride(){
        final NativeSettings settings=new NativeSettings();
        final Minecraft189FovModule module=new Minecraft189FovModule(settings,new EventBus());
        settings.onFovWrite=module::onDisable;
        module.onEnable();
        assertFalse(module.active());
        assertEquals(70F,settings.fov,0.000001F);
        assertEquals(2,settings.writes);
        module.onEnable();
        assertTrue(module.active());
        module.onDisable();
    }

    @Test void thrownOriginalSetterAfterMutatingRestoresExactOriginalFov(){
        final NativeSettings settings=new NativeSettings();
        final Minecraft189FovModule module=new Minecraft189FovModule(settings,new EventBus());
        settings.throwAfterFovWrite=true;
        assertThrows(IllegalStateException.class,module::onEnable);
        assertFalse(module.active());
        assertEquals(70F,settings.fov,0.000001F);
        module.onEnable();
        assertTrue(module.active());
        module.onDisable();
        assertEquals(70F,settings.fov,0.000001F);
    }

    @Test void userNativeFovEditAfterOverrideSurvivesDisable(){
        final NativeSettings settings=new NativeSettings();
        final Minecraft189FovModule module=new Minecraft189FovModule(settings,new EventBus());
        module.onEnable();
        assertEquals(110F,settings.fov,0.000001F);
        settings.fov=84F;
        module.onDisable();
        assertEquals(84F,settings.fov,0.000001F);
        assertFalse(module.active());
    }

    @Test void changedUserFovInCancelledSetterIsPreserved(){
        final NativeSettings settings=new NativeSettings();
        final Minecraft189FovModule module=new Minecraft189FovModule(settings,new EventBus());
        settings.onFovWrite=()->{
            module.onDisable();
            settings.fov=82F;
        };
        module.onEnable();
        assertFalse(module.active());
        assertEquals(82F,settings.fov,0.000001F);
    }

    @Test void nestedEnableDuringOriginalGetterCannotOverwriteBaseline(){
        final NativeSettings settings=new NativeSettings();
        final Minecraft189FovModule module=new Minecraft189FovModule(settings,new EventBus());
        settings.onFovRead=()->assertThrows(IllegalStateException.class,module::onEnable);
        module.onEnable();
        assertEquals(70F,module.originalFov(),0.000001F);
        module.onDisable();
        assertEquals(70F,settings.fov,0.000001F);
    }

    @Test void nativeNonfiniteFovGetterFailsClosedAndCanRecover(){
        final NativeSettings settings=new NativeSettings();
        final Minecraft189FovModule module=new Minecraft189FovModule(settings,new EventBus());
        settings.fov=Float.NaN;
        assertThrows(IllegalArgumentException.class,module::onEnable);
        assertEquals(0,settings.writes);
        settings.fov=75F;
        module.onEnable();
        module.onDisable();
        assertEquals(75F,settings.fov,0.000001F);
    }

    @Test void latestLiveTargetIsOwnedAcrossOriginalTickAndDisable(){
        final EventBus events=new EventBus();
        final NativeSettings settings=new NativeSettings();
        final Minecraft189FovModule module=new Minecraft189FovModule(settings,events);
        module.onEnable();
        module.targetFovSetting().set(125);
        events.publish(new Minecraft189Hooks.TickEvent(1L));
        assertEquals(125F,settings.fov,0.000001F);
        module.onDisable();
        assertEquals(70F,settings.fov,0.000001F);
        final int writes=settings.writes;
        events.publish(new Minecraft189Hooks.TickEvent(2L));
        assertEquals(writes,settings.writes);
    }

    @Test void nativeTickSetterReentrantDisableRestoresNewestOwnTarget(){
        final EventBus events=new EventBus();
        final NativeSettings settings=new NativeSettings();
        final Minecraft189FovModule module=new Minecraft189FovModule(settings,events);
        module.onEnable();
        module.targetFovSetting().set(135);
        settings.onFovWrite=module::onDisable;
        events.publish(new Minecraft189Hooks.TickEvent(3L));
        assertFalse(module.active());
        assertEquals(70F,settings.fov,0.000001F);
    }

    @Test void restoringExactNativeOverrideOnlyDoesNotClobberUserPreference(){
        final NativeSettings settings=new NativeSettings();
        final Minecraft189FovModule module=new Minecraft189FovModule(settings,new EventBus());
        module.onEnable();
        assertEquals(110F,settings.fov,0.000001F);
        module.onDisable();
        assertEquals(70F,settings.fov,0.000001F);
        module.onEnable();
        settings.fov=90F;
        module.onDisable();
        assertEquals(90F,settings.fov,0.000001F);
    }

    @Test void rejectedNestedEnableDuringSetterDoesNotSpawnDuplicateTickSubscription(){
        final EventBus events=new EventBus();
        final NativeSettings settings=new NativeSettings();
        final Minecraft189FovModule module=new Minecraft189FovModule(settings,events);
        settings.onFovWrite=()->assertThrows(IllegalStateException.class,module::onEnable);
        module.onEnable();
        assertEquals(1,settings.writes);
        module.targetFovSetting().set(120);
        events.publish(new Minecraft189Hooks.TickEvent(9L));
        assertEquals(120F,settings.fov,0.000001F);
        assertEquals(2,settings.writes);
        module.onDisable();
    }
}
