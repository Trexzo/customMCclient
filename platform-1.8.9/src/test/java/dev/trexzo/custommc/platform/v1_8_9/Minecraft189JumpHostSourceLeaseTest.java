package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.event.EventBus;
import dev.trexzo.custommc.core.module.ModuleCategoryRegistry;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.render.RenderPipeline;
import dev.trexzo.custommc.core.service.ServiceRegistry;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.ui.UiFontHandle;
import dev.trexzo.custommc.core.ui.UiViewport;
import dev.trexzo.custommc.platform.PlatformContext;
import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Original EntityPlayer.jump() source callbacks; native Mojang acceptance is separate. */
final class Minecraft189JumpHostSourceLeaseTest {
    private static final class Jumper implements Minecraft189PlayerJumpControl {
        int jumps;
        Runnable onJump;
        boolean throwAfterJump;
        @Override public void customMcJump() {
            ++jumps;
            if (jumps > 8) throw new AssertionError("recursive native jump");
            final Runnable callback=onJump;
            onJump=null;
            if (callback!=null) callback.run();
            if (throwAfterJump) throw new IllegalStateException("native jump threw after Y");
        }
    }
    @Test void newNativeJumpSourceAcquiresAndUnwinds() {
        final Minecraft189JumpHostSourceLease l=new Minecraft189JumpHostSourceLease();
        final Jumper a=new Jumper();
        assertTrue(l.enter(a,false));assertTrue(l.stillOwned(false));
        l.exit();assertFalse(l.stillOwned(false));
        assertTrue(l.enter(a,false));l.exit();
    }
    @Test void sameOriginalJumpSourceCannotDispatchSecondHostCallback() {
        final Minecraft189JumpHostSourceLease l=new Minecraft189JumpHostSourceLease();
        final Jumper a=new Jumper();assertTrue(l.enter(a,false));
        assertFalse(l.enter(a,false));assertTrue(l.stillOwned(false));l.exit();
    }
    @Test void differentOriginalJumpSourceRevokesOldHostPermanently() {
        final Minecraft189JumpHostSourceLease l=new Minecraft189JumpHostSourceLease();
        final Jumper a=new Jumper(),b=new Jumper();
        assertTrue(l.enter(a,false));assertFalse(l.enter(b,false));
        assertFalse(l.stillOwned(false));assertFalse(l.enter(a,false));
        l.exit();assertTrue(l.enter(b,false));l.exit();
    }
    @Test void nullNestedNativeJumpSourceRevokesOldPlayer() {
        final Minecraft189JumpHostSourceLease l=new Minecraft189JumpHostSourceLease();
        assertTrue(l.enter(new Jumper(),false));
        assertFalse(l.enter(null,false));assertFalse(l.stillOwned(false));l.exit();
    }
    @Test void newGuiFocusVetoesOldOriginalJumpSource() {
        final Minecraft189JumpHostSourceLease l=new Minecraft189JumpHostSourceLease();
        final Jumper a=new Jumper();assertTrue(l.enter(a,false));
        assertFalse(l.stillOwned(true));assertFalse(l.stillOwned(false));l.exit();
    }
    @Test void nestedGuiFocusVetoesOldHostWithoutNewNativeJump() {
        final Minecraft189JumpHostSourceLease l=new Minecraft189JumpHostSourceLease();
        final Jumper a=new Jumper();assertTrue(l.enter(a,false));
        assertFalse(l.enter(a,true));assertFalse(l.stillOwned(false));l.exit();
    }
    @Test void guiAlreadyFocusedAtEntryDoesNotInventNewFocusChange() {
        final Minecraft189JumpHostSourceLease l=new Minecraft189JumpHostSourceLease();
        final Jumper a=new Jumper();assertTrue(l.enter(a,true));
        assertFalse(l.enter(a,true));assertTrue(l.stillOwned(true));l.exit();
    }
    @Test void hostVetoIsIrreversibleBeforeNativeFinally() {
        final Minecraft189JumpHostSourceLease l=new Minecraft189JumpHostSourceLease();
        final Jumper a=new Jumper();assertTrue(l.enter(a,false));
        assertFalse(l.enter(new Jumper(),false));
        assertFalse(l.enter(a,false));assertFalse(l.stillOwned(false));l.exit();
    }
    @Test void sourceGuardDelegatesExactlyOneVanillaJump() {
        final Minecraft189JumpHostSourceLease l=new Minecraft189JumpHostSourceLease();
        final Jumper a=new Jumper();assertTrue(l.enter(a,false));
        l.guardedPlayer(a,()->false).customMcJump();
        assertEquals(1,a.jumps);l.exit();
    }
    @Test void sourceGuardDropsJumpAfterChangedNativePlayer() {
        final Minecraft189JumpHostSourceLease l=new Minecraft189JumpHostSourceLease();
        final Jumper a=new Jumper(),b=new Jumper();assertTrue(l.enter(a,false));
        final Minecraft189PlayerJumpControl guarded=l.guardedPlayer(a,()->false);
        assertFalse(l.enter(b,false));
        guarded.customMcJump();assertEquals(0,a.jumps+b.jumps);l.exit();
    }
    @Test void sourceGuardDropsJumpWhenGuiGainsRealFocus() {
        final Minecraft189JumpHostSourceLease l=new Minecraft189JumpHostSourceLease();
        final Jumper a=new Jumper();assertTrue(l.enter(a,false));
        l.guardedPlayer(a,()->true).customMcJump();
        assertEquals(0,a.jumps);assertFalse(l.stillOwned(false));l.exit();
    }
    @Test void sourceGuardNeverCreatesPhantomNullPlayer() {
        final Minecraft189JumpHostSourceLease l=new Minecraft189JumpHostSourceLease();
        assertTrue(l.enter(null,false));
        assertNull(l.guardedPlayer(null,()->false));l.exit();
    }
    @Test void originalNativeJumpThrowPreservesRealPartialEffect() {
        final Minecraft189JumpHostSourceLease l=new Minecraft189JumpHostSourceLease();
        final Jumper a=new Jumper();a.throwAfterJump=true;assertTrue(l.enter(a,false));
        try {
            assertThrows(IllegalStateException.class,
                    ()->l.guardedPlayer(a,()->false).customMcJump());
            assertEquals(1,a.jumps);
        } finally {l.exit();}
        assertTrue(l.enter(new Jumper(),false));l.exit();
    }
    @Test void nativeSameSourceReentryThroughGuardDoesNotDoubleJump() {
        final Minecraft189JumpHostSourceLease l=new Minecraft189JumpHostSourceLease();
        final Jumper a=new Jumper();assertTrue(l.enter(a,false));
        a.onJump=()->assertFalse(l.enter(a,false));
        l.guardedPlayer(a,()->false).customMcJump();
        assertEquals(1,a.jumps);assertTrue(l.stillOwned(false));l.exit();
    }
    @Test void mappedNativeJumpChangedPlayerRevokesNextSourceJump() {
        final Minecraft189JumpHostSourceLease l=new Minecraft189JumpHostSourceLease();
        final Jumper a=new Jumper(),b=new Jumper();assertTrue(l.enter(a,false));
        final Minecraft189PlayerJumpControl guard=l.guardedPlayer(a,()->false);
        a.onJump=()->assertFalse(l.enter(b,false));
        guard.customMcJump();
        guard.customMcJump();
        assertEquals(1,a.jumps);assertEquals(0,b.jumps);l.exit();
    }
    @Test void subsequentOriginalNativeTickMayUseNewSource() {
        final Minecraft189JumpHostSourceLease l=new Minecraft189JumpHostSourceLease();
        final Jumper a=new Jumper(),b=new Jumper();assertTrue(l.enter(a,false));
        assertFalse(l.enter(b,false));l.exit();
        assertTrue(l.enter(b,false));
        l.guardedPlayer(b,()->false).customMcJump();
        assertEquals(1,b.jumps);l.exit();
    }

    private static final class Fixture implements AutoCloseable {
        final ModuleRegistry modules=new ModuleRegistry();
        final ModuleController controller=new ModuleController(modules);
        final SettingRegistry settings=new SettingRegistry();
        final ServiceRegistry services=new ServiceRegistry();
        final Minecraft189HostRuntime runtime;
        Fixture(){
            services.register(RenderPipeline.class,new RenderPipeline());
            final Minecraft189Platform platform=new Minecraft189Platform();
            platform.attach(new PlatformContext(new EventBus(),modules,controller,services));
            runtime=Minecraft189HostRuntime.install(
                    platform,new ModulePresentationRegistry(),
                    new ModuleCategoryRegistry(),new ModuleSettingRegistry(modules,settings),
                    null,null,settings,new SettingPresentationRegistry(),new NoOpHost());
            controller.enable(Minecraft189AutoJumpModule.ID);
        }
        void takeoff(Jumper p){
            runtime.playerMovementState().update(false,false,false);
            runtime.playerJumpControl(p);
        }
        void land(Jumper p){
            runtime.playerMovementState().update(true,false,false);
            runtime.playerJumpControl(p);
        }
        @Override public void close(){runtime.close();}
    }
    @Test void integratedOriginalHostSingleAirToGroundAutoJump() {
        try(Fixture f=new Fixture()){
            final Jumper p=new Jumper();
            f.takeoff(p);f.land(p);
            assertEquals(1,p.jumps);f.land(p);assertEquals(1,p.jumps);
        }
    }
    @Test void integratedNativeJumpReenteringSamePlayerNeverDoubleJumps() {
        try(Fixture f=new Fixture()){
            final Jumper p=new Jumper();
            p.onJump=()->f.runtime.playerJumpControl(p);
            f.takeoff(p);f.land(p);assertEquals(1,p.jumps);
        }
    }
    @Test void integratedNativeJumpNestedOtherPlayerCannotJumpOrStealContact() {
        try(Fixture f=new Fixture()){
            final Jumper a=new Jumper(),b=new Jumper();
            a.onJump=()->f.runtime.playerJumpControl(b);
            f.takeoff(a);f.land(a);
            assertEquals(1,a.jumps);assertEquals(0,b.jumps);
        }
    }
    @Test void integratedOriginalJumpThrowUnwindsHostAndAllowsNextContact() {
        try(Fixture f=new Fixture()){
            final Jumper a=new Jumper();a.throwAfterJump=true;
            f.takeoff(a);
            assertThrows(IllegalStateException.class,()->f.land(a));
            assertEquals(1,a.jumps);
            a.throwAfterJump=false;
            f.takeoff(a);f.land(a);
            assertEquals(2,a.jumps);
        }
    }
    @Test void integratedNewOriginalPlayerCanJumpOnNextRealContact() {
        try(Fixture f=new Fixture()){
            final Jumper a=new Jumper(),b=new Jumper();
            a.onJump=()->f.runtime.playerJumpControl(b);
            f.takeoff(a);f.land(a);
            f.takeoff(b);f.land(b);
            assertEquals(1,b.jumps);
        }
    }
    @Test void integratedDisabledAutoJumpCannotFabricateNativeJump() {
        try(Fixture f=new Fixture()){
            f.controller.disable(Minecraft189AutoJumpModule.ID);
            final Jumper a=new Jumper();f.takeoff(a);f.land(a);
            assertEquals(0,a.jumps);
        }
    }
    @Test void integratedNullPlayerDoesNotInventNativeJump() {
        try(Fixture f=new Fixture()){
            f.runtime.playerJumpControl(null);
            final Jumper a=new Jumper();f.takeoff(a);f.land(a);
            assertEquals(1,a.jumps);
        }
    }
    @Test void integratedInitialArmedGroundContactFiresOnlyOnce() {
        try(Fixture f=new Fixture()){
            // Existing AutoJump starts armed: the first grounded
            // callback is permitted once without a prior air sample.
            final Jumper a=new Jumper();f.land(a);f.land(a);
            assertEquals(1,a.jumps);
            f.land(a);assertEquals(1,a.jumps);
        }
    }
    private static final class NoOpHost implements LegacyUiHostCallbacks {
        @Override public int framebufferWidth(){return 1280;}
        @Override public int framebufferHeight(){return 720;}
        @Override public float uiScale(){return 1F;}
        @Override public void beginUi(UiViewport v){}
        @Override public void fillRect(float x,float y,float w,float h,int c){}
        @Override public void fillRoundedRect(float x,float y,float w,float h,float r,int c){}
        @Override public void strokeRect(float x,float y,float w,float h,float t,int c){}
        @Override public void pushClip(float x,float y,float w,float h){}
        @Override public void popClip(){}
        @Override public void drawText(UiFontHandle f,float x,float y,String s,int c){}
        @Override public void endUi(){}
    }
}
