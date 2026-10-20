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

final class Minecraft189HurtDimensionHostSourceLeaseTest {
  private static final class Hurt implements Minecraft189PlayerHurtTimeAccess {
    int value,reads; boolean fail; Runnable reenter;
    Hurt(int n){value=n;}
    @Override public int customMcHurtTime(){
      reads++;Runnable r=reenter;reenter=null;if(r!=null)r.run();
      if(fail)throw new IllegalStateException("original hurt getter");
      return value;
    }
  }
  private static final class Dim implements Minecraft189PlayerDimensionAccess {
    int value,reads; boolean fail; Runnable reenter;
    Dim(int n){value=n;}
    @Override public int customMcDimension(){
      reads++;Runnable r=reenter;reenter=null;if(r!=null)r.run();
      if(fail)throw new IllegalStateException("original dimension getter");
      return value;
    }
  }
  private static final class Host implements AutoCloseable {
    final ModuleRegistry mods=new ModuleRegistry();
    final ModuleController ctl=new ModuleController(mods);
    final SettingRegistry settings=new SettingRegistry();
    final ServiceRegistry services=new ServiceRegistry();
    final Minecraft189HostRuntime runtime;
    Host(){
      services.register(RenderPipeline.class,new RenderPipeline());
      Minecraft189Platform p=new Minecraft189Platform();
      p.attach(new PlatformContext(new EventBus(),mods,ctl,services));
      runtime=Minecraft189HostRuntime.install(p,new ModulePresentationRegistry(),
        new ModuleCategoryRegistry(),new ModuleSettingRegistry(mods,settings),
        null,null,settings,new SettingPresentationRegistry(),new NoOpHost());
    }
    Minecraft189PlayerHurtTimeState.Snapshot hurt(){return runtime.playerHurtTimeState().snapshot();}
    Minecraft189PlayerDimensionState.Snapshot dim(){return runtime.playerDimensionState().snapshot();}
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
  @Test void hurtFirstAndNextOwner(){Minecraft189HurtTimeHostSourceLease l=new Minecraft189HurtTimeHostSourceLease();Hurt a=new Hurt(1),b=new Hurt(2);assertTrue(l.enter(a));assertTrue(l.stillOwned());l.exit();assertTrue(l.enter(b));l.exit();}
  @Test void hurtNestedSameOwnerVeto(){Minecraft189HurtTimeHostSourceLease l=new Minecraft189HurtTimeHostSourceLease();Hurt a=new Hurt(1),b=new Hurt(2);assertTrue(l.enter(a));assertFalse(l.enter(a));assertTrue(l.stillOwned());l.exit();}
  @Test void hurtChangedOwnerIrreversible(){Minecraft189HurtTimeHostSourceLease l=new Minecraft189HurtTimeHostSourceLease();Hurt a=new Hurt(1),b=new Hurt(2);assertTrue(l.enter(a));assertFalse(l.enter(b));assertFalse(l.enter(a));assertFalse(l.stillOwned());l.exit();}
  @Test void hurtNestedNullRevokes(){Minecraft189HurtTimeHostSourceLease l=new Minecraft189HurtTimeHostSourceLease();Hurt a=new Hurt(1),b=new Hurt(2);assertTrue(l.enter(a));assertFalse(l.enter(null));assertFalse(l.stillOwned());l.exit();}
  @Test void hurtInitialNullNoBorrow(){Minecraft189HurtTimeHostSourceLease l=new Minecraft189HurtTimeHostSourceLease();Hurt a=new Hurt(1),b=new Hurt(2);assertTrue(l.enter(null));assertFalse(l.enter(a));assertFalse(l.stillOwned());l.exit();}
  @Test void hurtFinallyReleasesAfterThrow(){Minecraft189HurtTimeHostSourceLease l=new Minecraft189HurtTimeHostSourceLease();Hurt a=new Hurt(1),b=new Hurt(2);assertTrue(l.enter(a));try{throw new IllegalStateException("native getter");}catch(IllegalStateException e){assertEquals("native getter",e.getMessage());}finally{l.exit();}assertTrue(l.enter(b));l.exit();}
  @Test void dimensionFirstAndNextOwner(){Minecraft189DimensionHostSourceLease l=new Minecraft189DimensionHostSourceLease();Dim a=new Dim(1),b=new Dim(2);assertTrue(l.enter(a));assertTrue(l.stillOwned());l.exit();assertTrue(l.enter(b));l.exit();}
  @Test void dimensionNestedSameOwnerVeto(){Minecraft189DimensionHostSourceLease l=new Minecraft189DimensionHostSourceLease();Dim a=new Dim(1),b=new Dim(2);assertTrue(l.enter(a));assertFalse(l.enter(a));assertTrue(l.stillOwned());l.exit();}
  @Test void dimensionChangedOwnerIrreversible(){Minecraft189DimensionHostSourceLease l=new Minecraft189DimensionHostSourceLease();Dim a=new Dim(1),b=new Dim(2);assertTrue(l.enter(a));assertFalse(l.enter(b));assertFalse(l.enter(a));assertFalse(l.stillOwned());l.exit();}
  @Test void dimensionNestedNullRevokes(){Minecraft189DimensionHostSourceLease l=new Minecraft189DimensionHostSourceLease();Dim a=new Dim(1),b=new Dim(2);assertTrue(l.enter(a));assertFalse(l.enter(null));assertFalse(l.stillOwned());l.exit();}
  @Test void dimensionInitialNullNoBorrow(){Minecraft189DimensionHostSourceLease l=new Minecraft189DimensionHostSourceLease();Dim a=new Dim(1),b=new Dim(2);assertTrue(l.enter(null));assertFalse(l.enter(a));assertFalse(l.stillOwned());l.exit();}
  @Test void dimensionFinallyReleasesAfterThrow(){Minecraft189DimensionHostSourceLease l=new Minecraft189DimensionHostSourceLease();Dim a=new Dim(1),b=new Dim(2);assertTrue(l.enter(a));try{throw new IllegalStateException("native getter");}catch(IllegalStateException e){assertEquals("native getter",e.getMessage());}finally{l.exit();}assertTrue(l.enter(b));l.exit();}
  @Test void hurtPublishesOriginal(){try(Host f=new Host()){Hurt p=new Hurt(4);f.runtime.playerHurtTime(p);assertTrue(f.hurt().available());assertEquals(4,f.hurt().hurtTime());assertEquals(1,p.reads);}}
  @Test void hurtSameReentryNoDoubleRead(){try(Host f=new Host()){Hurt p=new Hurt(4);p.reenter=()->f.runtime.playerHurtTime(p);f.runtime.playerHurtTime(p);assertEquals(1,p.reads);assertEquals(4,f.hurt().hurtTime());}}
  @Test void hurtDifferentReentryClears(){try(Host f=new Host()){Hurt a=new Hurt(4),b=new Hurt(7);a.reenter=()->f.runtime.playerHurtTime(b);f.runtime.playerHurtTime(a);assertFalse(f.hurt().available());assertEquals(0,b.reads);}}
  @Test void hurtNullReentryClears(){try(Host f=new Host()){Hurt a=new Hurt(4);a.reenter=()->f.runtime.playerHurtTime(null);f.runtime.playerHurtTime(a);assertFalse(f.hurt().available());}}
  @Test void hurtNativeThrowClearsPrior(){try(Host f=new Host()){f.runtime.playerHurtTime(new Hurt(4));Hurt bad=new Hurt(9);bad.fail=true;assertThrows(IllegalStateException.class,()->f.runtime.playerHurtTime(bad));assertFalse(f.hurt().available());}}
  @Test void hurtNewPlayerAfterRevocation(){try(Host f=new Host()){Hurt a=new Hurt(4),b=new Hurt(7);a.reenter=()->f.runtime.playerHurtTime(b);f.runtime.playerHurtTime(a);f.runtime.playerHurtTime(b);assertTrue(f.hurt().available());assertEquals(7,f.hurt().hurtTime());}}
  @Test void hurtNullOuterClears(){try(Host f=new Host()){f.runtime.playerHurtTime(new Hurt(4));f.runtime.playerHurtTime(null);assertFalse(f.hurt().available());}}
  @Test void dimPublishesOriginal(){try(Host f=new Host()){Dim p=new Dim(4);f.runtime.playerDimension(p);assertTrue(f.dim().available());assertEquals(4,f.dim().dimensionId());assertEquals(1,p.reads);}}
  @Test void dimSameReentryNoDoubleRead(){try(Host f=new Host()){Dim p=new Dim(4);p.reenter=()->f.runtime.playerDimension(p);f.runtime.playerDimension(p);assertEquals(1,p.reads);assertEquals(4,f.dim().dimensionId());}}
  @Test void dimDifferentReentryClears(){try(Host f=new Host()){Dim a=new Dim(4),b=new Dim(7);a.reenter=()->f.runtime.playerDimension(b);f.runtime.playerDimension(a);assertFalse(f.dim().available());assertEquals(0,b.reads);}}
  @Test void dimNullReentryClears(){try(Host f=new Host()){Dim a=new Dim(4);a.reenter=()->f.runtime.playerDimension(null);f.runtime.playerDimension(a);assertFalse(f.dim().available());}}
  @Test void dimNativeThrowClearsPrior(){try(Host f=new Host()){f.runtime.playerDimension(new Dim(4));Dim bad=new Dim(9);bad.fail=true;assertThrows(IllegalStateException.class,()->f.runtime.playerDimension(bad));assertFalse(f.dim().available());}}
  @Test void dimNewPlayerAfterRevocation(){try(Host f=new Host()){Dim a=new Dim(4),b=new Dim(7);a.reenter=()->f.runtime.playerDimension(b);f.runtime.playerDimension(a);f.runtime.playerDimension(b);assertTrue(f.dim().available());assertEquals(7,f.dim().dimensionId());}}
  @Test void dimNullOuterClears(){try(Host f=new Host()){f.runtime.playerDimension(new Dim(4));f.runtime.playerDimension(null);assertFalse(f.dim().available());}}
  @Test void negativeNativeHurtClearsPriorAndThrows(){try(Host f=new Host()){f.runtime.playerHurtTime(new Hurt(8));assertThrows(IllegalArgumentException.class,()->f.runtime.playerHurtTime(new Hurt(-1)));assertFalse(f.hurt().available());}}
  @Test void negativeDimensionIsGenuineValidSource(){try(Host f=new Host()){f.runtime.playerDimension(new Dim(-1));assertTrue(f.dim().available());assertEquals(-1,f.dim().dimensionId());}}
  @Test void moddedDimensionIsNotRejected(){try(Host f=new Host()){f.runtime.playerDimension(new Dim(700));assertEquals(700,f.dim().dimensionId());}}
  @Test void independentDamageAndDimensionSourcesDoNotSteal(){try(Host f=new Host()){Hurt a=new Hurt(9);a.reenter=()->f.runtime.playerDimension(new Dim(-1));f.runtime.playerHurtTime(a);assertTrue(f.hurt().available());assertTrue(f.dim().available());}}
}
