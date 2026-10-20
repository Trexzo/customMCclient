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

/**
 * Verifies the exact outer original motion host chain, not only one
 * movement module's own getter/setter guard. Offline Java-8 source
 * adapters are NOT genuine pinned Mojang original-game evidence.
 */
final class Minecraft189MotionHostSourceLeaseTest {
    private static final class Player implements Minecraft189PlayerMotionControl {
        double x=1D,y=-.2D,z=1D;
        int rx,ry,rz,wx,wy,wz;
        Runnable onReadX,onWriteX,onReadY,onWriteY,onReadZ,onWriteZ;
        boolean throwX;
        private static void fire(Runnable r){if(r!=null)r.run();}
        @Override public double customMcMotionX(){
            rx++;if(rx>8)throw new AssertionError("recursive original X read");
            final Runnable r=onReadX;onReadX=null;fire(r);return x;
        }
        @Override public double customMcMotionY(){
            ry++;if(ry>8)throw new AssertionError("recursive original Y read");
            final Runnable r=onReadY;onReadY=null;fire(r);return y;
        }
        @Override public double customMcMotionZ(){
            rz++;if(rz>8)throw new AssertionError("recursive original Z read");
            final Runnable r=onReadZ;onReadZ=null;fire(r);return z;
        }
        @Override public void customMcSetMotionX(double v){
            wx++;if(wx>8)throw new AssertionError("recursive original X setter");
            x=v;final Runnable r=onWriteX;onWriteX=null;fire(r);
            if(throwX)throw new IllegalStateException("mapped original X setter");
        }
        @Override public void customMcSetMotionY(double v){
            wy++;y=v;final Runnable r=onWriteY;onWriteY=null;fire(r);
        }
        @Override public void customMcSetMotionZ(double v){
            wz++;z=v;final Runnable r=onWriteZ;onWriteZ=null;fire(r);
        }
        int nativeWrites(){return wx+wy+wz;}
        int nativeReads(){return rx+ry+rz;}
    }
    @Test void firstOriginalPlayerAcquiresLeaseAndReleases() {
        final Minecraft189MotionHostSourceLease lease=new Minecraft189MotionHostSourceLease();
        final Player p=new Player();
        assertTrue(lease.enter(p,false));assertTrue(lease.stillOwned(false));
        lease.exit();assertFalse(lease.stillOwned(false));
        assertTrue(lease.enter(p,false));lease.exit();
    }
    @Test void sameOriginalPlayerCannotBorrowSecondHostTick() {
        final Minecraft189MotionHostSourceLease lease=new Minecraft189MotionHostSourceLease();
        final Player p=new Player();assertTrue(lease.enter(p,false));
        assertFalse(lease.enter(p,false));assertTrue(lease.stillOwned(false));
        lease.exit();assertTrue(lease.enter(p,false));lease.exit();
    }
    @Test void differentOriginalPlayerIrrevocablyRevokesOuterMotionHost() {
        final Minecraft189MotionHostSourceLease lease=new Minecraft189MotionHostSourceLease();
        final Player a=new Player(),b=new Player();
        assertTrue(lease.enter(a,false));assertFalse(lease.enter(b,false));
        assertFalse(lease.stillOwned(false));assertFalse(lease.enter(a,false));
        assertFalse(lease.stillOwned(false));lease.exit();
        assertTrue(lease.enter(b,false));lease.exit();
    }
    @Test void nullNestedSourceCannotBorrowRealOriginalPlayer() {
        final Minecraft189MotionHostSourceLease lease=new Minecraft189MotionHostSourceLease();
        assertTrue(lease.enter(new Player(),false));
        assertFalse(lease.enter(null,false));
        assertFalse(lease.stillOwned(false));lease.exit();
    }
    @Test void realGuiFocusDuringOriginalGetterRevokesRemainingChain() {
        final Minecraft189MotionHostSourceLease lease=new Minecraft189MotionHostSourceLease();
        assertTrue(lease.enter(new Player(),false));
        assertFalse(lease.stillOwned(true));assertFalse(lease.stillOwned(false));
        lease.exit();
    }
    @Test void realGuiFocusOnNestedSamePlayerRevokesOuterChain() {
        final Minecraft189MotionHostSourceLease lease=new Minecraft189MotionHostSourceLease();
        final Player p=new Player();assertTrue(lease.enter(p,false));
        assertFalse(lease.enter(p,true));assertFalse(lease.stillOwned(false));
        lease.exit();
    }
    @Test void originalGuiAlreadyFocusedDoesNotInventSecondFocusChange() {
        final Minecraft189MotionHostSourceLease lease=new Minecraft189MotionHostSourceLease();
        final Player p=new Player();assertTrue(lease.enter(p,true));
        assertFalse(lease.enter(p,true));assertTrue(lease.stillOwned(true));
        lease.exit();
    }
    @Test void closingOriginallyFocusedGuiDoesNotReviveStaleMotion() {
        final Minecraft189MotionHostSourceLease lease=new Minecraft189MotionHostSourceLease();
        final Player p=new Player(),b=new Player();assertTrue(lease.enter(p,true));
        assertFalse(lease.enter(b,false));assertFalse(lease.stillOwned(false));
        lease.exit();
    }
    @Test void invalidationCannotBeClearedByReenteringSamePlayer() {
        final Minecraft189MotionHostSourceLease lease=new Minecraft189MotionHostSourceLease();
        final Player p=new Player();assertTrue(lease.enter(p,false));
        assertFalse(lease.enter(new Player(),false));
        assertFalse(lease.enter(p,false));assertFalse(lease.stillOwned(false));
        lease.exit();
    }
    @Test void differentOriginalPlayerCanAcquireAfterOldSourceExceptionFinally() {
        final Minecraft189MotionHostSourceLease lease=new Minecraft189MotionHostSourceLease();
        final Player a=new Player(),b=new Player();
        assertTrue(lease.enter(a,false));
        try {throw new IllegalStateException("genuine mapped setter");}
        catch(IllegalStateException expected) {assertEquals("genuine mapped setter",expected.getMessage());}
        finally {lease.exit();}
        assertTrue(lease.enter(b,false));assertTrue(lease.stillOwned(false));lease.exit();
    }
    @Test void initialNullSourceDoesNotInventOriginalPlayer() {
        final Minecraft189MotionHostSourceLease lease=new Minecraft189MotionHostSourceLease();
        final Player p=new Player();assertTrue(lease.enter(null,false));
        assertFalse(lease.enter(p,false));assertFalse(lease.stillOwned(false));
        lease.exit();assertTrue(lease.enter(p,false));lease.exit();
    }
    @Test void originalPlayerChangeAfterGuiOpenedStillCannotReviveLease() {
        final Minecraft189MotionHostSourceLease lease=new Minecraft189MotionHostSourceLease();
        final Player p=new Player();assertTrue(lease.enter(p,false));
        assertFalse(lease.stillOwned(true));assertFalse(lease.enter(new Player(),false));
        assertFalse(lease.stillOwned(false));lease.exit();
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
            runtime.playerMovementState().update(true,false,false);
            runtime.playerRotationState().update(0F);
            runtime.inputState().key(LegacyKeyboardCodes.W,true);
            controller.enable(Minecraft189MovementSpeedModule.ID);
        }
        @Override public void close(){runtime.close();}
    }
    @Test void integratedHostDifferentOriginalPlayerInMotionXSetterDoesNotRunNestedTick() {
        try(Fixture f=new Fixture()){
            final Player a=new Player(),b=new Player();
            a.onWriteX=()->f.runtime.playerMotionControl(b);
            f.runtime.playerMotionControl(a);
            assertEquals(0,b.nativeReads()+b.nativeWrites());
            assertEquals(1,a.wx);assertEquals(0,a.wz);
            // V243 also vetoes the old host's final Criticals/Y read,
            // rather than allowing a later policy to reuse old sources.
            assertEquals(0,a.ry);
            f.runtime.playerMotionControl(b);
            assertEquals(1,b.wx);assertEquals(1,b.wz);
        }
    }
    @Test void integratedHostSamePlayerNestedXGetterIsNotSecondMotionTick() {
        try(Fixture f=new Fixture()){
            final Player a=new Player();
            a.onReadX=()->f.runtime.playerMotionControl(a);
            f.runtime.playerMotionControl(a);
            assertEquals(1,a.wx);assertEquals(1,a.wz);
            assertEquals(1,a.rx);assertEquals(1,a.rz);
            assertEquals(1,a.ry);
        }
    }
    @Test void integratedHostSamePlayerNestedXSetterIsNotSecondMotionTick() {
        try(Fixture f=new Fixture()){
            final Player a=new Player();
            a.onWriteX=()->f.runtime.playerMotionControl(a);
            f.runtime.playerMotionControl(a);
            assertEquals(1,a.wx);assertEquals(1,a.wz);
            assertEquals(1,a.ry);
        }
    }
    @Test void integratedHostMappedSetterThrowReleasesOuterHostLease() {
        try(Fixture f=new Fixture()){
            final Player a=new Player();a.throwX=true;
            assertThrows(IllegalStateException.class,()->f.runtime.playerMotionControl(a));
            a.throwX=false;f.runtime.playerMotionControl(a);
            assertEquals(1,a.wz);
        }
    }
    @Test void integratedHostFreshNewPlayerOnlyMovesAfterPriorLeaseUnwinds() {
        try(Fixture f=new Fixture()){
            final Player a=new Player(),b=new Player();
            a.onWriteX=()->f.runtime.playerMotionControl(b);
            f.runtime.playerMotionControl(a);
            assertEquals(1D,b.x,1E-12);assertEquals(1D,b.z,1E-12);
            f.runtime.playerMotionControl(b);
            assertEquals(0D,b.x,1E-12);assertEquals(.45D,b.z,1E-12);
        }
    }
    @Test void integratedHostNoMotionModulesStillReadsOriginalYNormally() {
        try(Fixture f=new Fixture()){
            f.controller.disable(Minecraft189MovementSpeedModule.ID);
            final Player a=new Player();f.runtime.playerMotionControl(a);
            assertEquals(1,a.ry);assertEquals(0,a.nativeWrites());
        }
    }
    private static final class NoOpHost implements LegacyUiHostCallbacks {
        @Override public int framebufferWidth(){return 1280;}
        @Override public int framebufferHeight(){return 720;}
        @Override public float uiScale(){return 1F;}
        @Override public void beginUi(UiViewport viewport){}
        @Override public void fillRect(float x,float y,float w,float h,int color){}
        @Override public void fillRoundedRect(float x,float y,float w,float h,float radius,int color){}
        @Override public void strokeRect(float x,float y,float w,float h,float thickness,int color){}
        @Override public void pushClip(float x,float y,float w,float h){}
        @Override public void popClip(){}
        @Override public void drawText(UiFontHandle font,float x,float y,String s,int color){}
        @Override public void endUi(){}
    }
}
