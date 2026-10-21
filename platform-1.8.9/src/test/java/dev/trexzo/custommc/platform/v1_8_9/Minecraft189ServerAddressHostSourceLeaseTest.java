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

final class Minecraft189ServerAddressHostSourceLeaseTest {
    private static final class Server implements Minecraft189ServerDataAccess {
        String address="vanilla.example:25565";
        int reads;
        Runnable reenter;
        boolean throwsRuntime, throwsError;
        @Override public String customMcServerAddress() {
            reads++;
            final Runnable callback=reenter;
            reenter=null;
            if(callback!=null)callback.run();
            if(throwsRuntime)throw new IllegalStateException("original address getter");
            if(throwsError)throw new AssertionError("original address getter");
            return address;
        }
    }

    private static final class Fixture implements AutoCloseable {
        final ModuleRegistry modules=new ModuleRegistry();
        final ModuleController controller=new ModuleController(modules);
        final SettingRegistry settings=new SettingRegistry();
        final ServiceRegistry services=new ServiceRegistry();
        final Minecraft189HostRuntime runtime;

        Fixture() {
            services.register(RenderPipeline.class,new RenderPipeline());
            Minecraft189Platform platform=new Minecraft189Platform();
            platform.attach(new PlatformContext(new EventBus(),
                    modules,controller,services));
            runtime=Minecraft189HostRuntime.install(platform,
                    new ModulePresentationRegistry(),new ModuleCategoryRegistry(),
                    new ModuleSettingRegistry(modules,settings),null,null,
                    settings,new SettingPresentationRegistry(),new NoOpHost());
        }

        Minecraft189ServerAddressState.Snapshot snapshot() {
            return runtime.serverAddressState().snapshot();
        }
        @Override public void close() {runtime.close();}
    }

    private static final class NoOpHost implements LegacyUiHostCallbacks {
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

    @Test void leaseOwnsOneOriginalServerDataByReference() {
        Minecraft189ServerAddressHostSourceLease lease=new Minecraft189ServerAddressHostSourceLease();
        Server a=new Server();
        assertTrue(lease.enter(a));
        assertTrue(lease.stillOwned());
        lease.exit();
    }

    @Test void sameSourceNestedLeaseIsNotSecondOriginalSample() {
        Minecraft189ServerAddressHostSourceLease lease=new Minecraft189ServerAddressHostSourceLease();
        Server a=new Server();
        assertTrue(lease.enter(a));
        assertFalse(lease.enter(a));
        assertTrue(lease.stillOwned());
        lease.exit();
    }

    @Test void changedSourceNestedLeaseIrreversiblyRevokes() {
        Minecraft189ServerAddressHostSourceLease lease=new Minecraft189ServerAddressHostSourceLease();
        Server a=new Server(),b=new Server();
        assertTrue(lease.enter(a));
        assertFalse(lease.enter(b));
        assertFalse(lease.enter(a));
        assertFalse(lease.stillOwned());
        lease.exit();
        assertTrue(lease.enter(b));
        lease.exit();
    }

    @Test void nullSourceNestedLeaseRevokes() {
        Minecraft189ServerAddressHostSourceLease lease=new Minecraft189ServerAddressHostSourceLease();
        assertTrue(lease.enter(new Server()));
        assertFalse(lease.enter(null));
        assertFalse(lease.stillOwned());
        lease.exit();
    }

    @Test void originalNonblankAddressIsPublishedExactlyOnce() {
        try(Fixture f=new Fixture()) {
            Server a=new Server();
            f.runtime.serverAddress(a);
            assertTrue(f.snapshot().available());
            assertEquals("vanilla.example:25565",f.snapshot().address());
            assertEquals(1,a.reads);
        }
    }

    @Test void originalWhitespaceIsNormalizedByExistingState() {
        try(Fixture f=new Fixture()) {
            Server a=new Server();a.address="  vanilla.example:25565  ";
            f.runtime.serverAddress(a);
            assertTrue(f.snapshot().available());
            assertEquals("vanilla.example:25565",f.snapshot().address());
        }
    }

    @Test void originalIpv6AddressIsPreservedAsText() {
        try(Fixture f=new Fixture()) {
            Server a=new Server();a.address="[::1]:25565";
            f.runtime.serverAddress(a);
            assertEquals("[::1]:25565",f.snapshot().address());
        }
    }

    @Test void sameServerSourceGetterReentryNeverDuplicatesRealGet() {
        try(Fixture f=new Fixture()) {
            Server a=new Server();
            a.reenter=()->f.runtime.serverAddress(a);
            f.runtime.serverAddress(a);
            assertTrue(f.snapshot().available());
            assertEquals(1,a.reads);
        }
    }

    @Test void differentServerDataDuringGetterCannotPublishStaleAddress() {
        try(Fixture f=new Fixture()) {
            f.runtime.serverAddress(new Server());
            Server a=new Server(),b=new Server();
            a.reenter=()->f.runtime.serverAddress(b);
            f.runtime.serverAddress(a);
            assertFalse(f.snapshot().available());
            assertEquals(0,b.reads);
        }
    }

    @Test void nullSourceDuringGetterRevokesOldServerAddress() {
        try(Fixture f=new Fixture()) {
            f.runtime.serverAddress(new Server());
            Server a=new Server();a.reenter=()->f.runtime.serverAddress(null);
            f.runtime.serverAddress(a);
            assertFalse(f.snapshot().available());
        }
    }

    @Test void duplicateOldServerAfterChangedNestedSourceStillRejected() {
        try(Fixture f=new Fixture()) {
            Server old=new Server(),next=new Server();
            old.reenter=()->{f.runtime.serverAddress(next);f.runtime.serverAddress(old);};
            f.runtime.serverAddress(old);
            assertFalse(f.snapshot().available());
            assertEquals(1,old.reads);assertEquals(0,next.reads);
        }
    }

    @Test void explicitServerDisconnectClearsLastKnownAddress() {
        try(Fixture f=new Fixture()) {
            f.runtime.serverAddress(new Server());
            f.runtime.serverAddress(null);
            assertFalse(f.snapshot().available());
            assertNull(f.snapshot().address());
        }
    }

    @Test void nullOriginalAddressClearsOldSourceEvidence() {
        try(Fixture f=new Fixture()) {
            f.runtime.serverAddress(new Server());
            Server a=new Server();a.address=null;
            f.runtime.serverAddress(a);
            assertFalse(f.snapshot().available());
        }
    }

    @Test void blankOriginalAddressClearsOldSourceEvidence() {
        try(Fixture f=new Fixture()) {
            f.runtime.serverAddress(new Server());
            Server a=new Server();a.address="";
            f.runtime.serverAddress(a);
            assertFalse(f.snapshot().available());
        }
    }

    @Test void whitespaceOnlyOriginalAddressClearsOldSourceEvidence() {
        try(Fixture f=new Fixture()) {
            f.runtime.serverAddress(new Server());
            Server a=new Server();a.address="   ";
            f.runtime.serverAddress(a);
            assertFalse(f.snapshot().available());
        }
    }

    @Test void originalAddressGetterRuntimeFailurePropagatesAndClears() {
        try(Fixture f=new Fixture()) {
            f.runtime.serverAddress(new Server());
            Server a=new Server();a.throwsRuntime=true;
            assertThrows(IllegalStateException.class,()->f.runtime.serverAddress(a));
            assertFalse(f.snapshot().available());
        }
    }

    @Test void originalAddressGetterErrorPropagatesAndClears() {
        try(Fixture f=new Fixture()) {
            f.runtime.serverAddress(new Server());
            Server a=new Server();a.throwsError=true;
            assertThrows(AssertionError.class,()->f.runtime.serverAddress(a));
            assertFalse(f.snapshot().available());
        }
    }

    @Test void newServerCanRecoverAfterRevokedNativeGetter() {
        try(Fixture f=new Fixture()) {
            Server old=new Server(),next=new Server();
            old.reenter=()->f.runtime.serverAddress(next);
            f.runtime.serverAddress(old);
            assertFalse(f.snapshot().available());
            next.address="new.example:25565";
            f.runtime.serverAddress(next);
            assertEquals("new.example:25565",f.snapshot().address());
        }
    }

    @Test void newServerCanRecoverAfterNativeError() {
        try(Fixture f=new Fixture()) {
            Server old=new Server();old.throwsError=true;
            assertThrows(AssertionError.class,()->f.runtime.serverAddress(old));
            Server next=new Server();
            f.runtime.serverAddress(next);
            assertTrue(f.snapshot().available());
            assertEquals("vanilla.example:25565",f.snapshot().address());
        }
    }

    @Test void originalValidSourceAfterNullUnloadMustReadAgain() {
        try(Fixture f=new Fixture()) {
            Server a=new Server();
            f.runtime.serverAddress(a);
            f.runtime.serverAddress(null);
            f.runtime.serverAddress(a);
            assertTrue(f.snapshot().available());
            assertEquals(2,a.reads);
        }
    }
}
