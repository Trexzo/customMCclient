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

/** Actual original-shaped XYZ getters; official Mojang binary proof runs separately. */
final class Minecraft189PositionHostSourceLeaseTest {
    private static final class Player implements Minecraft189PlayerPositionAccess {
        double x,y,z;int rx,ry,rz;
        Runnable onX,onY,onZ;
        boolean failX,failY,failZ;
        Player(double x,double y,double z){this.x=x;this.y=y;this.z=z;}
        @Override public double customMcPositionX(){
            rx++;if(rx>8)throw new AssertionError("recursive native posX");
            final Runnable callback=onX;onX=null;if(callback!=null)callback.run();
            if(failX)throw new IllegalStateException("native X getter");
            return x;
        }
        @Override public double customMcPositionY(){
            ry++;if(ry>8)throw new AssertionError("recursive native posY");
            final Runnable callback=onY;onY=null;if(callback!=null)callback.run();
            if(failY)throw new IllegalStateException("native Y getter");
            return y;
        }
        @Override public double customMcPositionZ(){
            rz++;if(rz>8)throw new AssertionError("recursive native posZ");
            final Runnable callback=onZ;onZ=null;if(callback!=null)callback.run();
            if(failZ)throw new IllegalStateException("native Z getter");
            return z;
        }
        int reads(){return rx+ry+rz;}
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
            runtime=Minecraft189HostRuntime.install(platform,
                new ModulePresentationRegistry(),new ModuleCategoryRegistry(),
                new ModuleSettingRegistry(modules,settings),
                null,null,settings,new SettingPresentationRegistry(),new NoOpHost());
        }
        Minecraft189PlayerPositionState.Snapshot pos(){return runtime.playerPositionState().snapshot();}
        Minecraft189MovementSpeedTracker.Snapshot speed(){return runtime.movementSpeedTracker().snapshot();}
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
    @Test void originalSourceLeaseEnterAndRelease(){final Minecraft189PositionHostSourceLease l=new Minecraft189PositionHostSourceLease();final Player p=new Player(1,2,3);assertTrue(l.enter(p));assertTrue(l.stillOwned());l.exit();assertFalse(l.stillOwned());assertTrue(l.enter(p));l.exit();}
    @Test void sameSourceNestedReadNotAnotherTick(){final Minecraft189PositionHostSourceLease l=new Minecraft189PositionHostSourceLease();final Player p=new Player(1,2,3);assertTrue(l.enter(p));assertFalse(l.enter(p));assertTrue(l.stillOwned());l.exit();}
    @Test void changedPlayerRevocationIsIrreversible(){final Minecraft189PositionHostSourceLease l=new Minecraft189PositionHostSourceLease();final Player a=new Player(1,2,3),b=new Player(5,6,7);assertTrue(l.enter(a));assertFalse(l.enter(b));assertFalse(l.stillOwned());assertFalse(l.enter(a));assertFalse(l.stillOwned());l.exit();assertTrue(l.enter(b));l.exit();}
    @Test void nestedNullWorldUnloadRevokesOriginal(){final Minecraft189PositionHostSourceLease l=new Minecraft189PositionHostSourceLease();assertTrue(l.enter(new Player(1,2,3)));assertFalse(l.enter(null));assertFalse(l.stillOwned());l.exit();}
    @Test void initialNullSourceCannotInventReplacement(){final Minecraft189PositionHostSourceLease l=new Minecraft189PositionHostSourceLease();assertTrue(l.enter(null));assertFalse(l.enter(new Player(1,2,3)));assertFalse(l.stillOwned());l.exit();}
    @Test void integratedOriginalXYZPublishedWithoutMixing(){try(Fixture f=new Fixture()){final Player a=new Player(3,4,5);f.runtime.playerPosition(a);assertTrue(f.pos().available());assertEquals(3D,f.pos().x(),1e-9);assertEquals(4D,f.pos().y(),1e-9);assertEquals(5D,f.pos().z(),1e-9);assertEquals(3,a.reads());}}
    @Test void integratedSourceSamePlayerXGetterNeverDoubleSamples(){try(Fixture f=new Fixture()){final Player p=new Player(1,2,3);p.onX=()->f.runtime.playerPosition(p);f.runtime.playerPosition(p);assertEquals(3,p.reads());assertTrue(f.pos().available());}}
    @Test void integratedSourceSamePlayerYGetterNeverDoubleSamples(){try(Fixture f=new Fixture()){final Player p=new Player(1,2,3);p.onY=()->f.runtime.playerPosition(p);f.runtime.playerPosition(p);assertEquals(3,p.reads());assertTrue(f.pos().available());}}
    @Test void integratedSourceSamePlayerZGetterNeverDoubleSamples(){try(Fixture f=new Fixture()){final Player p=new Player(1,2,3);p.onZ=()->f.runtime.playerPosition(p);f.runtime.playerPosition(p);assertEquals(3,p.reads());assertTrue(f.pos().available());}}
    @Test void integratedChangedOriginalPlayerInXGetterDiscardsPosition(){try(Fixture f=new Fixture()){final Player a=new Player(1,2,3),b=new Player(4,5,6);a.onX=()->f.runtime.playerPosition(b);f.runtime.playerPosition(a);assertFalse(f.pos().available());assertEquals(1,a.reads());assertEquals(0,b.reads());}}
    @Test void integratedChangedOriginalPlayerInYGetterDiscardsOldX(){try(Fixture f=new Fixture()){final Player a=new Player(1,2,3),b=new Player(4,5,6);a.onY=()->f.runtime.playerPosition(b);f.runtime.playerPosition(a);assertFalse(f.pos().available());assertEquals(2,a.reads());assertEquals(0,b.reads());}}
    @Test void integratedChangedOriginalPlayerInZGetterDiscardsXYZ(){try(Fixture f=new Fixture()){final Player a=new Player(1,2,3),b=new Player(4,5,6);a.onZ=()->f.runtime.playerPosition(b);f.runtime.playerPosition(a);assertFalse(f.pos().available());assertEquals(3,a.reads());assertEquals(0,b.reads());}}
    @Test void integratedNestedNullInXGetterClearsOriginal(){try(Fixture f=new Fixture()){final Player a=new Player(1,2,3);a.onX=()->f.runtime.playerPosition(null);f.runtime.playerPosition(a);assertFalse(f.pos().available());assertFalse(f.speed().available());}}
    @Test void integratedNestedNullInZGetterClearsOriginal(){try(Fixture f=new Fixture()){final Player a=new Player(1,2,3);a.onZ=()->f.runtime.playerPosition(null);f.runtime.playerPosition(a);assertFalse(f.pos().available());assertEquals(3,a.reads());}}
    @Test void integratedPreviousValidStateClearedOnReplacement(){try(Fixture f=new Fixture()){f.runtime.playerPosition(new Player(1,2,3));f.runtime.playerPosition(new Player(3,2,3));assertTrue(f.speed().available());final Player a=new Player(5,2,3);a.onY=()->f.runtime.playerPosition(new Player(7,8,9));f.runtime.playerPosition(a);assertFalse(f.pos().available());assertFalse(f.speed().available());assertEquals(0D,f.speed().peakBlocksPerSecond(),1e-9);}}
    @Test void integratedGenuineNextPlayerAfterRevocationStartsNewSample(){try(Fixture f=new Fixture()){final Player a=new Player(1,2,3),b=new Player(4,5,6);a.onX=()->f.runtime.playerPosition(b);f.runtime.playerPosition(a);assertFalse(f.pos().available());f.runtime.playerPosition(b);assertTrue(f.pos().available());assertEquals(4D,f.pos().x(),1e-9);assertFalse(f.speed().available());}}
    @Test void integratedPositionSpeedRequiresTwoActualCallbacks(){try(Fixture f=new Fixture()){f.runtime.playerPosition(new Player(0,2,0));assertFalse(f.speed().available());f.runtime.playerPosition(new Player(3,2,4));assertTrue(f.speed().available());assertEquals(100D,f.speed().blocksPerSecond(),1e-9);}}
    @Test void integratedGenuineNullClearsPositionAndSpeed(){try(Fixture f=new Fixture()){f.runtime.playerPosition(new Player(0,0,0));f.runtime.playerPosition(new Player(1,0,0));f.runtime.playerPosition(null);assertFalse(f.pos().available());assertFalse(f.speed().available());}}
    @Test void integratedOriginalXGetterThrowInvalidatesSharedState(){try(Fixture f=new Fixture()){f.runtime.playerPosition(new Player(1,2,3));final Player bad=new Player(4,5,6);bad.failX=true;assertThrows(IllegalStateException.class,()->f.runtime.playerPosition(bad));assertFalse(f.pos().available());assertFalse(f.speed().available());}}
    @Test void integratedOriginalYGetterThrowInvalidatesSharedState(){try(Fixture f=new Fixture()){f.runtime.playerPosition(new Player(1,2,3));final Player bad=new Player(4,5,6);bad.failY=true;assertThrows(IllegalStateException.class,()->f.runtime.playerPosition(bad));assertFalse(f.pos().available());assertEquals(2,bad.reads());}}
    @Test void integratedOriginalZGetterThrowInvalidatesSharedState(){try(Fixture f=new Fixture()){f.runtime.playerPosition(new Player(1,2,3));final Player bad=new Player(4,5,6);bad.failZ=true;assertThrows(IllegalStateException.class,()->f.runtime.playerPosition(bad));assertFalse(f.pos().available());assertEquals(3,bad.reads());}}
    @Test void integratedNonfinitePositionXCannotPublishStaleSpeed(){try(Fixture f=new Fixture()){f.runtime.playerPosition(new Player(1,2,3));assertThrows(IllegalArgumentException.class,()->f.runtime.playerPosition(new Player(Double.NaN,2,3)));assertFalse(f.pos().available());assertFalse(f.speed().available());}}
    @Test void integratedNonfinitePositionYCannotPublishStaleSpeed(){try(Fixture f=new Fixture()){f.runtime.playerPosition(new Player(1,2,3));assertThrows(IllegalArgumentException.class,()->f.runtime.playerPosition(new Player(1,Double.POSITIVE_INFINITY,3)));assertFalse(f.pos().available());assertFalse(f.speed().available());}}
    @Test void integratedGetterExceptionReleasesLeaseForSubsequentPlayer(){try(Fixture f=new Fixture()){final Player bad=new Player(1,2,3);bad.failZ=true;assertThrows(IllegalStateException.class,()->f.runtime.playerPosition(bad));final Player p=new Player(7,8,9);f.runtime.playerPosition(p);assertTrue(f.pos().available());assertEquals(7D,f.pos().x(),1e-9);assertFalse(f.speed().available());}}
}
