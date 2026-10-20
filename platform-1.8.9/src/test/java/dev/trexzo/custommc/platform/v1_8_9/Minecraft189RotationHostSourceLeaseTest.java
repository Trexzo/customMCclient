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
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Original-shape mapped rotation evidence and host policies, not Mojang binary proof. */
final class Minecraft189RotationHostSourceLeaseTest {
    private static final class Player
            implements Minecraft189PlayerRotationControl,
                       Minecraft189PlayerMovementStateAccess {
        float yaw,pitch;
        boolean ground=true,sneak,sprint=true;
        int ry,rp,rg,rs,rsp,wy,wp;
        Runnable onYaw,onPitch,onGround,onSneak,onSprint,onWriteYaw,onWritePitch;
        boolean failYaw,failPitch,failGround,failWriteYaw;
        Player(float yaw,float pitch){this.yaw=yaw;this.pitch=pitch;}
        static void fire(Runnable cb){if(cb!=null)cb.run();}
        @Override public float customMcRotationYaw(){
            ry++;if(ry>9)throw new AssertionError("recursive native yaw");
            Runnable cb=onYaw;onYaw=null;fire(cb);
            if(failYaw)throw new IllegalStateException("native yaw getter");
            return yaw;
        }
        @Override public float customMcRotationPitch(){
            rp++;if(rp>9)throw new AssertionError("recursive native pitch");
            Runnable cb=onPitch;onPitch=null;fire(cb);
            if(failPitch)throw new IllegalStateException("native pitch getter");
            return pitch;
        }
        @Override public boolean customMcOnGround(){
            rg++;if(rg>9)throw new AssertionError("recursive ground");
            Runnable cb=onGround;onGround=null;fire(cb);
            if(failGround)throw new IllegalStateException("native ground getter");
            return ground;
        }
        @Override public boolean customMcSneaking(){
            rs++;if(rs>9)throw new AssertionError("recursive sneak");
            Runnable cb=onSneak;onSneak=null;fire(cb);return sneak;
        }
        @Override public boolean customMcSprinting(){
            rsp++;if(rsp>9)throw new AssertionError("recursive sprint");
            Runnable cb=onSprint;onSprint=null;fire(cb);return sprint;
        }
        @Override public void customMcSetRotationYaw(float next){
            wy++;if(wy>9)throw new AssertionError("recursive yaw setter");
            yaw=next;Runnable cb=onWriteYaw;onWriteYaw=null;fire(cb);
            if(failWriteYaw)throw new IllegalStateException("native yaw setter");
        }
        @Override public void customMcSetRotationPitch(float next){
            wp++;pitch=next;
            Runnable cb=onWritePitch;onWritePitch=null;fire(cb);
        }
        int readRot(){return ry+rp;}
        int readMove(){return rg+rs+rsp;}
    }
    private static final class Fixture implements AutoCloseable {
        final ModuleRegistry modules=new ModuleRegistry();
        final ModuleController controller=new ModuleController(modules);
        final SettingRegistry settings=new SettingRegistry();
        final ServiceRegistry services=new ServiceRegistry();
        final Minecraft189HostRuntime runtime;
        Fixture(){
            services.register(RenderPipeline.class,new RenderPipeline());
            Minecraft189Platform platform=new Minecraft189Platform();
            platform.attach(new PlatformContext(new EventBus(),modules,controller,services));
            runtime=Minecraft189HostRuntime.install(platform,
                new ModulePresentationRegistry(),new ModuleCategoryRegistry(),
                new ModuleSettingRegistry(modules,settings),
                null,null,settings,new SettingPresentationRegistry(),new NoOpHost());
        }
        void spin(){controller.enable(Minecraft189SpinModule.ID);}
        Minecraft189PlayerRotationState.Snapshot rotation(){
            return runtime.playerRotationState().snapshot();
        }
        Minecraft189PlayerMovementState.Snapshot movement(){
            return runtime.playerMovementState().snapshot();
        }
        @Override public void close(){runtime.close();}
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
    @Test void originalLeaseAcceptsFirstThenFreshLaterTick(){
        Minecraft189RotationHostSourceLease l=new Minecraft189RotationHostSourceLease();
        Player p=new Player(0,0);
        assertTrue(l.enter(p,false));assertTrue(l.stillOwned(false));
        l.exit();assertFalse(l.stillOwned(false));assertTrue(l.enter(p,false));l.exit();
    }
    @Test void nestedSameSourceCannotDispatchTwice(){
        Minecraft189RotationHostSourceLease l=new Minecraft189RotationHostSourceLease();
        Player p=new Player(0,0);assertTrue(l.enter(p,false));
        assertFalse(l.enter(p,false));assertTrue(l.stillOwned(false));l.exit();
    }
    @Test void differentSourceRevokesOriginalIrrevocably(){
        Minecraft189RotationHostSourceLease l=new Minecraft189RotationHostSourceLease();
        Player a=new Player(0,0),b=new Player(1,1);assertTrue(l.enter(a,false));
        assertFalse(l.enter(b,false));assertFalse(l.stillOwned(false));
        assertFalse(l.enter(a,false));l.exit();assertTrue(l.enter(b,false));l.exit();
    }
    @Test void nestedNullRevokesOriginalSource(){
        Minecraft189RotationHostSourceLease l=new Minecraft189RotationHostSourceLease();
        assertTrue(l.enter(new Player(0,0),false));assertFalse(l.enter(null,false));
        assertFalse(l.stillOwned(false));l.exit();
    }
    @Test void newGuiFocusVetoesOriginalRotation(){
        Minecraft189RotationHostSourceLease l=new Minecraft189RotationHostSourceLease();
        assertTrue(l.enter(new Player(0,0),false));
        assertFalse(l.stillOwned(true));assertFalse(l.stillOwned(false));l.exit();
    }
    @Test void initialGuiAlreadyFocusedDoesNotInventChange(){
        Minecraft189RotationHostSourceLease l=new Minecraft189RotationHostSourceLease();
        Player p=new Player(0,0);assertTrue(l.enter(p,true));
        assertFalse(l.enter(p,true));assertTrue(l.stillOwned(true));l.exit();
    }
    @Test void nullPlayerCannotCreateNativeControlAdapter(){
        Minecraft189RotationHostSourceLease l=new Minecraft189RotationHostSourceLease();
        assertTrue(l.enter(null,false));assertNull(l.guardedControl(null,()->false));l.exit();
    }
    @Test void originalGuardPreservesBothNativeAxes(){
        Minecraft189RotationHostSourceLease l=new Minecraft189RotationHostSourceLease();
        Player p=new Player(1,2);assertTrue(l.enter(p,false));
        Minecraft189PlayerRotationControl g=l.guardedControl(p,()->false);
        g.customMcSetRotationYaw(3);g.customMcSetRotationPitch(4);
        assertEquals(3F,g.customMcRotationYaw(),1e-6F);
        assertEquals(4F,g.customMcRotationPitch(),1e-6F);
        assertEquals(1,p.wy);assertEquals(1,p.wp);l.exit();
    }
    @Test void differentSourceAfterYawWriteVetoesOldPitch(){
        Minecraft189RotationHostSourceLease l=new Minecraft189RotationHostSourceLease();
        Player a=new Player(0,0),b=new Player(1,1);
        assertTrue(l.enter(a,false));
        Minecraft189PlayerRotationControl g=l.guardedControl(a,()->false);
        a.onWriteYaw=()->assertFalse(l.enter(b,false));
        g.customMcSetRotationYaw(20);g.customMcSetRotationPitch(30);
        assertEquals(20F,a.yaw,1e-6F);assertEquals(0F,a.pitch,1e-6F);
        assertEquals(0,a.wp);l.exit();
    }
    @Test void guiFocusBetweenYawAndPitchWritesVetoesPitch(){
        Minecraft189RotationHostSourceLease l=new Minecraft189RotationHostSourceLease();
        Player p=new Player(0,0);assertTrue(l.enter(p,false));
        final boolean[] gui={false};
        Minecraft189PlayerRotationControl g=l.guardedControl(p,()->gui[0]);
        g.customMcSetRotationYaw(20);gui[0]=true;g.customMcSetRotationPitch(30);
        assertEquals(1,p.wy);assertEquals(0,p.wp);l.exit();
    }
    @Test void nativeYawSetterThrowPreservesPartialRealWrite(){
        Minecraft189RotationHostSourceLease l=new Minecraft189RotationHostSourceLease();
        Player p=new Player(0,0);assertTrue(l.enter(p,false));p.failWriteYaw=true;
        assertThrows(IllegalStateException.class,
            ()->l.guardedControl(p,()->false).customMcSetRotationYaw(20));
        assertEquals(20F,p.yaw,1e-6F);l.exit();
    }
    @Test void nonControlRotationAccessProducesNoSyntheticSetters(){
        Minecraft189RotationHostSourceLease l=new Minecraft189RotationHostSourceLease();
        Minecraft189PlayerRotationAccess readOnly=()->12F;
        assertTrue(l.enter(readOnly,false));assertNull(l.guardedControl(readOnly,()->false));l.exit();
    }
    @Test void installedHostPublishesActualYawPitchGroundSneakSprint(){
        try(Fixture f=new Fixture()){
            Player p=new Player(42,12);p.ground=false;p.sneak=true;p.sprint=false;
            f.runtime.playerRotation(p);
            assertTrue(f.rotation().available());assertEquals(42F,f.rotation().yaw(),1e-6F);
            assertEquals(12F,f.rotation().pitch(),1e-6F);
            assertTrue(f.movement().available());
            assertFalse(f.movement().onGround());assertTrue(f.movement().sneaking());
            assertFalse(f.movement().sprinting());assertEquals(2,p.readRot());assertEquals(3,p.readMove());
        }
    }
    @Test void yawGetterSamePlayerReentryNeverDoublesReads(){
        try(Fixture f=new Fixture()){
            Player p=new Player(1,2);p.onYaw=()->f.runtime.playerRotation(p);
            f.runtime.playerRotation(p);assertTrue(f.rotation().available());
            assertEquals(1,p.ry);assertEquals(1,p.rp);
        }
    }
    @Test void pitchGetterSamePlayerReentryNeverDoublesReads(){
        try(Fixture f=new Fixture()){
            Player p=new Player(1,2);p.onPitch=()->f.runtime.playerRotation(p);
            f.runtime.playerRotation(p);assertTrue(f.rotation().available());
            assertEquals(2,p.readRot());
        }
    }
    @Test void differentPlayerInsideYawGetterRejectsPitchAndSnapshots(){
        try(Fixture f=new Fixture()){
            Player a=new Player(1,2),b=new Player(3,4);
            a.onYaw=()->f.runtime.playerRotation(b);f.runtime.playerRotation(a);
            assertFalse(f.rotation().available());assertFalse(f.movement().available());
            assertEquals(1,a.ry);assertEquals(0,a.rp+b.readRot());
        }
    }
    @Test void differentPlayerInsidePitchGetterRejectsGroundAndSnapshots(){
        try(Fixture f=new Fixture()){
            Player a=new Player(1,2),b=new Player(3,4);
            a.onPitch=()->f.runtime.playerRotation(b);f.runtime.playerRotation(a);
            assertFalse(f.rotation().available());assertEquals(0,a.readMove()+b.readRot());
        }
    }
    @Test void nestedNullDuringYawGetterRejectsOldRotation(){
        try(Fixture f=new Fixture()){
            Player a=new Player(1,2);a.onYaw=()->f.runtime.playerRotation(null);
            f.runtime.playerRotation(a);assertFalse(f.rotation().available());
            assertEquals(1,a.readRot());
        }
    }
    @Test void changedPlayerDuringGroundGetterDoesNotPublishSneakSprint(){
        try(Fixture f=new Fixture()){
            Player a=new Player(1,2),b=new Player(3,4);
            a.onGround=()->f.runtime.playerRotation(b);f.runtime.playerRotation(a);
            assertFalse(f.rotation().available());assertFalse(f.movement().available());
            assertEquals(1,a.rg);assertEquals(0,a.rs+a.rsp+b.readRot());
        }
    }
    @Test void changedPlayerDuringSneakGetterVetoesSprint(){
        try(Fixture f=new Fixture()){
            Player a=new Player(1,2),b=new Player(3,4);
            a.onSneak=()->f.runtime.playerRotation(b);f.runtime.playerRotation(a);
            assertFalse(f.rotation().available());assertFalse(f.movement().available());
            assertEquals(0,a.rsp+b.readRot());
        }
    }
    @Test void changedPlayerDuringSprintGetterRejectsCompleteOldEvidence(){
        try(Fixture f=new Fixture()){
            Player a=new Player(1,2),b=new Player(3,4);
            a.onSprint=()->f.runtime.playerRotation(b);f.runtime.playerRotation(a);
            assertFalse(f.rotation().available());assertFalse(f.movement().available());
            assertEquals(3,a.readMove());assertEquals(0,b.readRot());
        }
    }
    @Test void rotationYawGetterThrowClearsPreviousEvidence(){
        try(Fixture f=new Fixture()){
            f.runtime.playerRotation(new Player(12,13));
            Player bad=new Player(1,2);bad.failYaw=true;
            assertThrows(IllegalStateException.class,()->f.runtime.playerRotation(bad));
            assertFalse(f.rotation().available());assertFalse(f.movement().available());
        }
    }
    @Test void rotationPitchGetterThrowClearsPreviousEvidence(){
        try(Fixture f=new Fixture()){
            f.runtime.playerRotation(new Player(12,13));
            Player bad=new Player(1,2);bad.failPitch=true;
            assertThrows(IllegalStateException.class,()->f.runtime.playerRotation(bad));
            assertFalse(f.rotation().available());
        }
    }
    @Test void nonfinitePitchVetoesSnapshotAndDoesNotPoisonNextTick(){
        try(Fixture f=new Fixture()){
            assertThrows(IllegalArgumentException.class,
                ()->f.runtime.playerRotation(new Player(12,Float.NaN)));
            assertFalse(f.rotation().available());
            f.runtime.playerRotation(new Player(8,9));assertTrue(f.rotation().available());
        }
    }
    @Test void spinSetterNestedOtherPlayerVetoesOldHostAfterPhysicalYaw(){
        try(Fixture f=new Fixture()){
            f.spin();
            Player a=new Player(0,0),b=new Player(4,5);
            a.onWriteYaw=()->f.runtime.playerRotation(b);
            f.runtime.playerRotation(a);
            assertEquals(1,a.wy);assertEquals(20F,a.yaw,1e-6F);
            assertEquals(0,b.wy);assertFalse(f.rotation().available());
        }
    }
    @Test void spinSetterSamePlayerReentryCannotSpendSecondSpin(){
        try(Fixture f=new Fixture()){
            f.spin();Player p=new Player(0,0);
            p.onWriteYaw=()->f.runtime.playerRotation(p);
            f.runtime.playerRotation(p);
            assertEquals(1,p.wy);assertEquals(20F,p.yaw,1e-6F);
        }
    }
}
