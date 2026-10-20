package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.event.EventBus;
import dev.trexzo.custommc.core.module.*;
import dev.trexzo.custommc.core.render.RenderPipeline;
import dev.trexzo.custommc.core.service.ServiceRegistry;
import dev.trexzo.custommc.core.setting.*;
import dev.trexzo.custommc.core.ui.*;
import dev.trexzo.custommc.platform.PlatformContext;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189HeldItemHostSourceLeaseTest {
    static final class Item implements Minecraft189ItemStackAccess {
        String name="Original Sword";
        int size=1,damage=4,max=250;
        final int[] reads=new int[4];
        int reenterAt=-1,throwAt=-1,errorAt=-1;
        Runnable reenter;
        void read(int stage) {
            reads[stage]++;
            if(stage==reenterAt && reenter!=null){
                Runnable call=reenter;reenter=null;call.run();
            }
            if(stage==throwAt)throw new IllegalStateException("original item getter");
            if(stage==errorAt)throw new AssertionError("original item error");
        }
        @Override public String customMcDisplayName(){read(0);return name;}
        @Override public int customMcStackSize(){read(1);return size;}
        @Override public int customMcItemDamage(){read(2);return damage;}
        @Override public int customMcMaxDamage(){read(3);return max;}
        @Override public boolean customMcIsSword(){return true;}
        int total(){int sum=0;for(int n:reads)sum+=n;return sum;}
    }
    static final class Player implements Minecraft189PlayerHeldItemAccess {
        Item item=new Item();
        int reads;
        Runnable reenter;
        boolean fail,error;
        @Override public Minecraft189ItemStackAccess customMcHeldItem(){
            reads++;
            Runnable call=reenter;reenter=null;
            if(call!=null)call.run();
            if(fail)throw new IllegalStateException("original held getter");
            if(error)throw new AssertionError("original held error");
            return item;
        }
    }
    static final class Fixture implements AutoCloseable {
        final ModuleRegistry modules=new ModuleRegistry();
        final ModuleController controller=new ModuleController(modules);
        final SettingRegistry settings=new SettingRegistry();
        final ServiceRegistry services=new ServiceRegistry();
        final Minecraft189HostRuntime runtime;
        Fixture(){
            services.register(RenderPipeline.class,new RenderPipeline());
            Minecraft189Platform platform=new Minecraft189Platform();
            platform.attach(new PlatformContext(new EventBus(),
                    modules,controller,services));
            runtime=Minecraft189HostRuntime.install(platform,
                    new ModulePresentationRegistry(),new ModuleCategoryRegistry(),
                    new ModuleSettingRegistry(modules,settings),null,null,
                    settings,new SettingPresentationRegistry(),new NoOpHost());
        }
        Minecraft189HeldItemState.Snapshot held(){
            return runtime.heldItemState().snapshot();
        }
        @Override public void close(){runtime.close();}
    }
    static final class NoOpHost implements LegacyUiHostCallbacks{
        @Override public int framebufferWidth(){return 1280;}
        @Override public int framebufferHeight(){return 720;}
        @Override public float uiScale(){return 1F;}
        @Override public void beginUi(UiViewport v){}
        @Override public void fillRect(float x,float y,float w,float h,int c){}
        @Override public void fillRoundedRect(float x,float y,float w,float h,float rad,int c){}
        @Override public void strokeRect(float x,float y,float w,float h,float t,int c){}
        @Override public void pushClip(float x,float y,float w,float h){}
        @Override public void popClip(){}
        @Override public void drawText(UiFontHandle f,float x,float y,String s,int c){}
        @Override public void endUi(){}
    }
    @Test void originalOwnerLeaseAcceptsRealSourceAndReleases(){
        Minecraft189HeldItemHostSourceLease lease=new Minecraft189HeldItemHostSourceLease();
        assertTrue(lease.enter(new Player()));
        assertTrue(lease.stillOwned());lease.exit();
        assertTrue(lease.enter(new Player()));lease.exit();
    }
    @Test void sameSourceNestedLeaseCannotDoubleRead(){
        Minecraft189HeldItemHostSourceLease lease=new Minecraft189HeldItemHostSourceLease();
        Player p=new Player();
        assertTrue(lease.enter(p));assertFalse(lease.enter(p));
        assertTrue(lease.stillOwned());lease.exit();
    }
    @Test void changedSourceRevokesOldOriginalIrrevocably(){
        Minecraft189HeldItemHostSourceLease lease=new Minecraft189HeldItemHostSourceLease();
        Player a=new Player(),b=new Player();
        assertTrue(lease.enter(a));assertFalse(lease.enter(b));
        assertFalse(lease.enter(a));assertFalse(lease.stillOwned());
        lease.exit();assertTrue(lease.enter(b));lease.exit();
    }
    @Test void nullNativeReentryRevokesOuterPlayer(){
        Minecraft189HeldItemHostSourceLease lease=new Minecraft189HeldItemHostSourceLease();
        assertTrue(lease.enter(new Player()));
        assertFalse(lease.enter(null));assertFalse(lease.stillOwned());lease.exit();
    }
    @Test void actualOriginalFiveGettersBuildOneItemSnapshot(){
        try(Fixture f=new Fixture()){
            Player p=new Player();
            f.runtime.playerHeldItem(p);
            assertTrue(f.held().available());
            assertEquals("Original Sword",f.held().displayName());
            assertEquals(1,f.held().stackSize());
            assertEquals(4,f.held().itemDamage());
            assertEquals(250,f.held().maxDamage());
            assertEquals(1,p.reads);assertEquals(4,p.item.total());
        }
    }
    @Test void validUndamageableOriginalHeldItemIsNotRejected(){
        try(Fixture f=new Fixture()){
            Player p=new Player();p.item.max=0;
            f.runtime.playerHeldItem(p);
            assertTrue(f.held().available());assertFalse(f.held().damageable());
        }
    }
    @Test void nestedSameSourceAtPlayerGetterDoesNotDoubleSample(){
        try(Fixture f=new Fixture()){
            Player p=new Player();p.reenter=()->f.runtime.playerHeldItem(p);
            f.runtime.playerHeldItem(p);
            assertTrue(f.held().available());assertEquals(1,p.reads);
            assertEquals(4,p.item.total());
        }
    }
    @Test void nestedSameSourceAtEachItemGetterDoesNotDoubleSample(){
        try(Fixture f=new Fixture()){
            for(int stage=0;stage<4;stage++){
                Player p=new Player();p.item.reenterAt=stage;
                p.item.reenter=()->f.runtime.playerHeldItem(p);
                f.runtime.playerHeldItem(p);
                assertTrue(f.held().available());
                assertEquals(1,p.reads);assertEquals(4,p.item.total());
            }
        }
    }
    @Test void differentOriginalDuringPlayerGetterRevokesOldItem(){
        try(Fixture f=new Fixture()){
            f.runtime.playerHeldItem(new Player());
            Player p=new Player(),other=new Player();
            p.reenter=()->f.runtime.playerHeldItem(other);
            f.runtime.playerHeldItem(p);
            assertFalse(f.held().available());
            assertEquals(0,other.reads);assertEquals(0,p.item.total());
        }
    }
    @Test void changedPlayerAtEachRealItemGetterDiscardsAll(){
        try(Fixture f=new Fixture()){
            for(int stage=0;stage<4;stage++){
                f.runtime.playerHeldItem(new Player());
                Player p=new Player(),other=new Player();
                p.item.reenterAt=stage;
                p.item.reenter=()->f.runtime.playerHeldItem(other);
                f.runtime.playerHeldItem(p);
                assertFalse(f.held().available(),"getter="+stage);
                assertEquals(0,other.reads);
                assertEquals(stage+1,p.item.total());
            }
        }
    }
    @Test void nullWorldDuringPlayerGetterClearsOldItem(){
        try(Fixture f=new Fixture()){
            f.runtime.playerHeldItem(new Player());
            Player p=new Player();p.reenter=()->f.runtime.playerHeldItem(null);
            f.runtime.playerHeldItem(p);assertFalse(f.held().available());
        }
    }
    @Test void nullWorldAtEachItemGetterClearsOldItem(){
        try(Fixture f=new Fixture()){
            for(int stage=0;stage<4;stage++){
                f.runtime.playerHeldItem(new Player());
                Player p=new Player();p.item.reenterAt=stage;
                p.item.reenter=()->f.runtime.playerHeldItem(null);
                f.runtime.playerHeldItem(p);
                assertFalse(f.held().available());
            }
        }
    }
    @Test void originalPlayerGetterRuntimeExceptionOrErrorClears(){
        try(Fixture f=new Fixture()){
            f.runtime.playerHeldItem(new Player());
            Player p=new Player();p.fail=true;
            assertThrows(IllegalStateException.class,()->f.runtime.playerHeldItem(p));
            assertFalse(f.held().available());
            f.runtime.playerHeldItem(new Player());
            p.fail=false;p.error=true;
            assertThrows(AssertionError.class,()->f.runtime.playerHeldItem(p));
            assertFalse(f.held().available());
        }
    }
    @Test void eachOriginalItemGetterThrowClearsLastValid(){
        try(Fixture f=new Fixture()){
            for(int stage=0;stage<4;stage++){
                f.runtime.playerHeldItem(new Player());
                Player p=new Player();p.item.throwAt=stage;
                assertThrows(IllegalStateException.class,()->f.runtime.playerHeldItem(p));
                assertFalse(f.held().available(),"stage="+stage);
            }
        }
    }
    @Test void eachOriginalItemGetterErrorClearsLastValid(){
        try(Fixture f=new Fixture()){
            for(int stage=0;stage<4;stage++){
                f.runtime.playerHeldItem(new Player());
                Player p=new Player();p.item.errorAt=stage;
                assertThrows(AssertionError.class,()->f.runtime.playerHeldItem(p));
                assertFalse(f.held().available(),"stage="+stage);
            }
        }
    }
    @Test void nullRealHeldItemClearsOldState(){
        try(Fixture f=new Fixture()){
            f.runtime.playerHeldItem(new Player());
            Player p=new Player();p.item=null;
            f.runtime.playerHeldItem(p);
            assertFalse(f.held().available());
        }
    }
    @Test void invalidRealItemNameDiscardsPreviouslyValid(){
        try(Fixture f=new Fixture()){
            f.runtime.playerHeldItem(new Player());
            Player p=new Player();p.item.name=" ";
            assertThrows(IllegalArgumentException.class,()->f.runtime.playerHeldItem(p));
            assertFalse(f.held().available());
        }
    }
    @Test void invalidOriginalQuantityAndDurabilityClearOld(){
        try(Fixture f=new Fixture()){
            for(int stage=0;stage<3;stage++){
                f.runtime.playerHeldItem(new Player());
                Player p=new Player();
                if(stage==0)p.item.size=0;
                if(stage==1)p.item.damage=-1;
                if(stage==2)p.item.max=-1;
                assertThrows(IllegalArgumentException.class,()->f.runtime.playerHeldItem(p));
                assertFalse(f.held().available());
            }
        }
    }
    @Test void explicitNullPlayerClearsAndFreshOriginalRecovers(){
        try(Fixture f=new Fixture()){
            f.runtime.playerHeldItem(new Player());
            f.runtime.playerHeldItem(null);
            assertFalse(f.held().available());
            Player next=new Player();next.item.name="Axe";
            f.runtime.playerHeldItem(next);
            assertTrue(f.held().available());assertEquals("Axe",f.held().displayName());
        }
    }
    @Test void changedNativeOwnerCanOnlyRecoverAfterFinally(){
        try(Fixture f=new Fixture()){
            Player a=new Player(),b=new Player();
            a.item.reenterAt=2;a.item.reenter=()->f.runtime.playerHeldItem(b);
            f.runtime.playerHeldItem(a);
            assertFalse(f.held().available());
            b.item.name="Fresh";
            f.runtime.playerHeldItem(b);
            assertEquals("Fresh",f.held().displayName());
        }
    }
}
