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

final class Minecraft189PingHotbarHostSourceLeaseTest {
    static final class Ping implements Minecraft189PlayerPingAccess {
        int value=42, reads;
        Runnable reenter;
        boolean throwsRuntime, throwsError;
        @Override public int customMcPingMilliseconds() {
            reads++;
            Runnable call=reenter; reenter=null;
            if(call!=null) call.run();
            if(throwsRuntime) throw new IllegalStateException("original ping");
            if(throwsError) throw new AssertionError("original ping");
            return value;
        }
    }

    static final class Inventory implements Minecraft189InventoryHotbarControl {
        int slot=3, reads, sets;
        Runnable reenter;
        boolean throwsRuntime, throwsError;
        @Override public int customMcSelectedHotbarSlot() {
            reads++;
            Runnable call=reenter; reenter=null;
            if(call!=null) call.run();
            if(throwsRuntime) throw new IllegalStateException("original inventory slot");
            if(throwsError) throw new AssertionError("original inventory slot");
            return slot;
        }
        @Override public void customMcSetSelectedHotbarSlot(int n) {
            sets++;
            slot=n;
        }
    }

    static final class Player implements Minecraft189PlayerInventoryAccess {
        Inventory inventory=new Inventory();
        int reads;
        Runnable reenter;
        boolean throwsRuntime, throwsError;
        @Override public Minecraft189InventoryHotbarAccess customMcInventory() {
            reads++;
            Runnable call=reenter; reenter=null;
            if(call!=null) call.run();
            if(throwsRuntime) throw new IllegalStateException("native inventory");
            if(throwsError) throw new AssertionError("native inventory");
            return inventory;
        }
    }

    static final class Fixture implements AutoCloseable {
        final ModuleRegistry modules=new ModuleRegistry();
        final ModuleController controller=new ModuleController(modules);
        final SettingRegistry settings=new SettingRegistry();
        final ServiceRegistry services=new ServiceRegistry();
        final Minecraft189HostRuntime runtime;
        Fixture() {
            services.register(RenderPipeline.class,new RenderPipeline());
            Minecraft189Platform platform=new Minecraft189Platform();
            platform.attach(new PlatformContext(
                    new EventBus(),modules,controller,services));
            runtime=Minecraft189HostRuntime.install(platform,
                    new ModulePresentationRegistry(),new ModuleCategoryRegistry(),
                    new ModuleSettingRegistry(modules,settings),null,null,
                    settings,new SettingPresentationRegistry(),new NoOpHost());
        }
        Minecraft189PlayerPingState.Snapshot ping() {
            return runtime.playerPingState().snapshot();
        }
        Minecraft189HotbarSlotState.Snapshot slot() {
            return runtime.hotbarSlotState().snapshot();
        }
        @Override public void close() {runtime.close();}
    }
    static final class NoOpHost implements LegacyUiHostCallbacks {
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

    @Test void pingAndHotbarIndependentExactLeases() {
        Minecraft189PingHostSourceLease ping=new Minecraft189PingHostSourceLease();
        Minecraft189HotbarHostSourceLease slot=new Minecraft189HotbarHostSourceLease();
        Ping a=new Ping(); Player b=new Player();
        assertTrue(ping.enter(a));assertTrue(slot.enter(b));
        assertFalse(ping.enter(a));assertFalse(slot.enter(b));
        assertTrue(ping.stillOwned());assertTrue(slot.stillOwned());
        ping.exit();slot.exit();
    }
    @Test void nestedChangedPingRevokesAndFinallyRecovers() {
        Minecraft189PingHostSourceLease l=new Minecraft189PingHostSourceLease();
        Ping a=new Ping(),b=new Ping();
        assertTrue(l.enter(a));assertFalse(l.enter(b));
        assertFalse(l.stillOwned());l.exit();
        assertTrue(l.enter(b));l.exit();
    }
    @Test void nestedNullHotbarRevokesAndNextOriginalRecovers() {
        Minecraft189HotbarHostSourceLease l=new Minecraft189HotbarHostSourceLease();
        assertTrue(l.enter(new Player()));assertFalse(l.enter(null));
        assertFalse(l.stillOwned());l.exit();
        assertTrue(l.enter(new Player()));l.exit();
    }
    @Test void genuinePingValueFromRealMappedGetter() {
        try(Fixture f=new Fixture()) {
            Ping p=new Ping();p.value=73;
            f.runtime.playerPing(p);
            assertTrue(f.ping().available());assertEquals(73,f.ping().milliseconds());
            assertEquals(1,p.reads);
        }
    }
    @Test void sameSourceNestedPingDoesNotDoubleGetter() {
        try(Fixture f=new Fixture()) {
            Ping p=new Ping();p.reenter=()->f.runtime.playerPing(p);
            f.runtime.playerPing(p);
            assertTrue(f.ping().available());assertEquals(1,p.reads);
        }
    }
    @Test void changedSourceDuringPingClearsPreviousEvidence() {
        try(Fixture f=new Fixture()) {
            f.runtime.playerPing(new Ping());
            Ping a=new Ping(),b=new Ping();
            a.reenter=()->f.runtime.playerPing(b);
            f.runtime.playerPing(a);
            assertFalse(f.ping().available());assertEquals(0,b.reads);
        }
    }
    @Test void nullSourceDuringPingClearsOldEvidence() {
        try(Fixture f=new Fixture()) {
            f.runtime.playerPing(new Ping());
            Ping p=new Ping();p.reenter=()->f.runtime.playerPing(null);
            f.runtime.playerPing(p);assertFalse(f.ping().available());
        }
    }
    @Test void nativePingFailureAndErrorPropagateAndClear() {
        try(Fixture f=new Fixture()) {
            f.runtime.playerPing(new Ping());
            Ping p=new Ping();p.throwsRuntime=true;
            assertThrows(IllegalStateException.class,()->f.runtime.playerPing(p));
            assertFalse(f.ping().available());
            f.runtime.playerPing(new Ping());
            p.throwsRuntime=false;p.throwsError=true;
            assertThrows(AssertionError.class,()->f.runtime.playerPing(p));
            assertFalse(f.ping().available());
        }
    }
    @Test void negativeOriginalPingDiscardsOldValuesWithoutThrowing() {
        try(Fixture f=new Fixture()) {
            f.runtime.playerPing(new Ping());
            Ping p=new Ping();p.value=-1;
            f.runtime.playerPing(p);assertFalse(f.ping().available());
        }
    }
    @Test void originalNullPingClearsAndFreshRecovers() {
        try(Fixture f=new Fixture()) {
            f.runtime.playerPing(new Ping());
            f.runtime.playerPing(null);assertFalse(f.ping().available());
            f.runtime.playerPing(new Ping());assertTrue(f.ping().available());
        }
    }
    @Test void mappedSelectedSlotPublishesNativeIndex() {
        try(Fixture f=new Fixture()) {
            Player a=new Player();a.inventory.slot=8;
            f.runtime.playerHotbarSlot(a);
            assertTrue(f.slot().available());assertEquals(8,f.slot().zeroBasedSlot());
            assertEquals(1,a.reads);assertEquals(1,a.inventory.reads);
        }
    }
    @Test void sameSourceNestedInventoryDoesNotDoubleNativeReads() {
        try(Fixture f=new Fixture()) {
            Player a=new Player();
            a.reenter=()->f.runtime.playerHotbarSlot(a);
            f.runtime.playerHotbarSlot(a);
            assertTrue(f.slot().available());assertEquals(1,a.reads);
            assertEquals(1,a.inventory.reads);
        }
    }
    @Test void sameSourceNestedSlotGetterDoesNotDoubleNativeReads() {
        try(Fixture f=new Fixture()) {
            Player a=new Player();
            a.inventory.reenter=()->f.runtime.playerHotbarSlot(a);
            f.runtime.playerHotbarSlot(a);
            assertTrue(f.slot().available());assertEquals(1,a.reads);
            assertEquals(1,a.inventory.reads);
        }
    }
    @Test void changedOriginalDuringInventoryGetterVetoesSlot() {
        try(Fixture f=new Fixture()) {
            f.runtime.playerHotbarSlot(new Player());
            Player a=new Player(),b=new Player();
            a.reenter=()->f.runtime.playerHotbarSlot(b);
            f.runtime.playerHotbarSlot(a);
            assertFalse(f.slot().available());
            assertEquals(0,a.inventory.reads);assertEquals(0,b.reads);
        }
    }
    @Test void changedOriginalDuringSelectedSlotGetterVetoesSlot() {
        try(Fixture f=new Fixture()) {
            f.runtime.playerHotbarSlot(new Player());
            Player a=new Player(),b=new Player();
            a.inventory.reenter=()->f.runtime.playerHotbarSlot(b);
            f.runtime.playerHotbarSlot(a);
            assertFalse(f.slot().available());assertEquals(0,b.reads);
        }
    }
    @Test void nullOriginalDuringBothNativeGetterStagesClears() {
        try(Fixture f=new Fixture()) {
            for(int stage=0;stage<2;stage++) {
                f.runtime.playerHotbarSlot(new Player());
                Player a=new Player();
                if(stage==0)a.reenter=()->f.runtime.playerHotbarSlot(null);
                else a.inventory.reenter=()->f.runtime.playerHotbarSlot(null);
                f.runtime.playerHotbarSlot(a);
                assertFalse(f.slot().available());
            }
        }
    }
    @Test void invalidOriginalSlotFailsShutAndRecovers() {
        try(Fixture f=new Fixture()) {
            for(int invalid:new int[]{-1,9,Integer.MAX_VALUE}) {
                f.runtime.playerHotbarSlot(new Player());
                Player bad=new Player();bad.inventory.slot=invalid;
                f.runtime.playerHotbarSlot(bad);
                assertFalse(f.slot().available());
            }
            f.runtime.playerHotbarSlot(new Player());assertTrue(f.slot().available());
        }
    }
    @Test void nativeInventoryAndSelectedSlotFailuresClearOldValues() {
        try(Fixture f=new Fixture()) {
            f.runtime.playerHotbarSlot(new Player());
            Player a=new Player();a.throwsRuntime=true;
            assertThrows(IllegalStateException.class,()->f.runtime.playerHotbarSlot(a));
            assertFalse(f.slot().available());
            f.runtime.playerHotbarSlot(new Player());
            a.throwsRuntime=false;a.inventory.throwsError=true;
            assertThrows(AssertionError.class,()->f.runtime.playerHotbarSlot(a));
            assertFalse(f.slot().available());
        }
    }
    @Test void explicitNullInventoryClearsSlotAndRecovers() {
        try(Fixture f=new Fixture()) {
            f.runtime.playerHotbarSlot(new Player());
            Player p=new Player();p.inventory=null;
            f.runtime.playerHotbarSlot(p);assertFalse(f.slot().available());
            f.runtime.playerHotbarSlot(new Player());assertTrue(f.slot().available());
        }
    }
    @Test void nullPlayerRevokesSlotAndFreshPlayerRestores() {
        try(Fixture f=new Fixture()) {
            f.runtime.playerHotbarSlot(new Player());
            f.runtime.playerHotbarSlot(null);assertFalse(f.slot().available());
            f.runtime.playerHotbarSlot(new Player());assertTrue(f.slot().available());
        }
    }
}
