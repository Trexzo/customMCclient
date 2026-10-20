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

final class Minecraft189HungerExperienceHostSourceLeaseTest {
  static class Hunger implements Minecraft189PlayerHungerAccess {
    int food=18;float saturation=5F;int fReads,sReads;
    Runnable onFood,onSaturation;boolean failFood,failSaturation;
    @Override public int customMcFoodLevel(){
      fReads++;Runnable r=onFood;onFood=null;if(r!=null)r.run();
      if(failFood)throw new IllegalStateException("original food getter");
      return food;
    }
    @Override public float customMcSaturationLevel(){
      sReads++;Runnable r=onSaturation;onSaturation=null;if(r!=null)r.run();
      if(failSaturation)throw new IllegalStateException("original saturation getter");
      return saturation;
    }
  }
  static class Experience implements Minecraft189PlayerExperienceAccess {
    int level=9,total=200,cap=40;float progress=0.5F;
    int lReads,tReads,pReads,cReads;
    Runnable onLevel,onTotal,onProgress,onCap;
    boolean failLevel,failTotal,failProgress,failCap;
    @Override public int customMcExperienceLevel(){
      lReads++;Runnable r=onLevel;onLevel=null;if(r!=null)r.run();
      if(failLevel)throw new IllegalStateException("original XP level getter");
      return level;
    }
    @Override public int customMcExperienceTotal(){
      tReads++;Runnable r=onTotal;onTotal=null;if(r!=null)r.run();
      if(failTotal)throw new IllegalStateException("original XP total getter");
      return total;
    }
    @Override public float customMcExperienceProgress(){
      pReads++;Runnable r=onProgress;onProgress=null;if(r!=null)r.run();
      if(failProgress)throw new IllegalStateException("original XP progress getter");
      return progress;
    }
    @Override public int customMcExperienceBarCap(){
      cReads++;Runnable r=onCap;onCap=null;if(r!=null)r.run();
      if(failCap)throw new IllegalStateException("original XP bar cap getter");
      return cap;
    }
  }
  static class Host implements AutoCloseable {
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
    Minecraft189PlayerHungerState.Snapshot h(){return runtime.playerHungerState().snapshot();}
    Minecraft189PlayerExperienceState.Snapshot xp(){return runtime.playerExperienceState().snapshot();}
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
  @Test void hungerFirstAndNextOriginal(){Minecraft189HungerHostSourceLease l=new Minecraft189HungerHostSourceLease();Hunger a=new Hunger(),b=new Hunger();assertTrue(l.enter(a));assertTrue(l.stillOwned());l.exit();assertTrue(l.enter(b));l.exit();}
  @Test void hungerNestedSameVeto(){Minecraft189HungerHostSourceLease l=new Minecraft189HungerHostSourceLease();Hunger a=new Hunger(),b=new Hunger();assertTrue(l.enter(a));assertFalse(l.enter(a));assertTrue(l.stillOwned());l.exit();}
  @Test void hungerChangedNativeSourceIrrevocable(){Minecraft189HungerHostSourceLease l=new Minecraft189HungerHostSourceLease();Hunger a=new Hunger(),b=new Hunger();assertTrue(l.enter(a));assertFalse(l.enter(b));assertFalse(l.enter(a));assertFalse(l.stillOwned());l.exit();}
  @Test void hungerNullWorldUnloadRevokes(){Minecraft189HungerHostSourceLease l=new Minecraft189HungerHostSourceLease();Hunger a=new Hunger(),b=new Hunger();assertTrue(l.enter(a));assertFalse(l.enter(null));assertFalse(l.stillOwned());l.exit();}
  @Test void hungerInitialNullDoesNotBorrowNewSource(){Minecraft189HungerHostSourceLease l=new Minecraft189HungerHostSourceLease();Hunger a=new Hunger(),b=new Hunger();assertTrue(l.enter(null));assertFalse(l.enter(a));assertFalse(l.stillOwned());l.exit();}
  @Test void hungerGetterFailureFinallyReleasesOwner(){Minecraft189HungerHostSourceLease l=new Minecraft189HungerHostSourceLease();Hunger a=new Hunger(),b=new Hunger();assertTrue(l.enter(a));try{throw new IllegalStateException("native");}catch(IllegalStateException ex){assertEquals("native",ex.getMessage());}finally{l.exit();}assertTrue(l.enter(b));l.exit();}
  @Test void xpFirstAndNextOriginal(){Minecraft189ExperienceHostSourceLease l=new Minecraft189ExperienceHostSourceLease();Experience a=new Experience(),b=new Experience();assertTrue(l.enter(a));assertTrue(l.stillOwned());l.exit();assertTrue(l.enter(b));l.exit();}
  @Test void xpNestedSameVeto(){Minecraft189ExperienceHostSourceLease l=new Minecraft189ExperienceHostSourceLease();Experience a=new Experience(),b=new Experience();assertTrue(l.enter(a));assertFalse(l.enter(a));assertTrue(l.stillOwned());l.exit();}
  @Test void xpChangedNativeSourceIrrevocable(){Minecraft189ExperienceHostSourceLease l=new Minecraft189ExperienceHostSourceLease();Experience a=new Experience(),b=new Experience();assertTrue(l.enter(a));assertFalse(l.enter(b));assertFalse(l.enter(a));assertFalse(l.stillOwned());l.exit();}
  @Test void xpNullWorldUnloadRevokes(){Minecraft189ExperienceHostSourceLease l=new Minecraft189ExperienceHostSourceLease();Experience a=new Experience(),b=new Experience();assertTrue(l.enter(a));assertFalse(l.enter(null));assertFalse(l.stillOwned());l.exit();}
  @Test void xpInitialNullDoesNotBorrowNewSource(){Minecraft189ExperienceHostSourceLease l=new Minecraft189ExperienceHostSourceLease();Experience a=new Experience(),b=new Experience();assertTrue(l.enter(null));assertFalse(l.enter(a));assertFalse(l.stillOwned());l.exit();}
  @Test void xpGetterFailureFinallyReleasesOwner(){Minecraft189ExperienceHostSourceLease l=new Minecraft189ExperienceHostSourceLease();Experience a=new Experience(),b=new Experience();assertTrue(l.enter(a));try{throw new IllegalStateException("native");}catch(IllegalStateException ex){assertEquals("native",ex.getMessage());}finally{l.exit();}assertTrue(l.enter(b));l.exit();}
  @Test void genuineFoodSaturationBothPublished(){try(Host f=new Host()){Hunger a=new Hunger();f.runtime.playerHunger(a);assertTrue(f.h().available());assertEquals(18,f.h().foodLevel());assertEquals(5F,f.h().saturationLevel(),1e-5F);assertEquals(1,a.fReads);assertEquals(1,a.sReads);}}
  @Test void genuineXpFourFieldsPublished(){try(Host f=new Host()){Experience a=new Experience();f.runtime.playerExperience(a);assertTrue(f.xp().available());assertEquals(9,f.xp().level());assertEquals(200,f.xp().total());assertEquals(0.5F,f.xp().progress(),1e-5F);assertEquals(40,f.xp().barCap());}}
  @Test void nestedSameFoodDoesNotDoubleRead(){try(Host f=new Host()){Hunger a=new Hunger();a.onFood=()->f.runtime.playerHunger(a);f.runtime.playerHunger(a);assertTrue(f.h().available());assertEquals(1,a.fReads+a.sReads-1);}}
  @Test void nestedSameSaturationDoesNotDoubleRead(){try(Host f=new Host()){Hunger a=new Hunger();a.onSaturation=()->f.runtime.playerHunger(a);f.runtime.playerHunger(a);assertEquals(1,a.sReads);assertTrue(f.h().available());}}
  @Test void changedPlayerDuringfoodGetterClears(){try(Host f=new Host()){Hunger a=new Hunger(),b=new Hunger();a.onFood=()->f.runtime.playerHunger(b);f.runtime.playerHunger(a);assertFalse(f.h().available());assertEquals(0,b.fReads+b.sReads);}}
  @Test void nullPlayerDuringfoodGetterClears(){try(Host f=new Host()){Hunger a=new Hunger();a.onFood=()->f.runtime.playerHunger(null);f.runtime.playerHunger(a);assertFalse(f.h().available());}}
  @Test void changedPlayerDuringsaturationGetterClears(){try(Host f=new Host()){Hunger a=new Hunger(),b=new Hunger();a.onSaturation=()->f.runtime.playerHunger(b);f.runtime.playerHunger(a);assertFalse(f.h().available());assertEquals(0,b.fReads+b.sReads);}}
  @Test void nullPlayerDuringsaturationGetterClears(){try(Host f=new Host()){Hunger a=new Hunger();a.onSaturation=()->f.runtime.playerHunger(null);f.runtime.playerHunger(a);assertFalse(f.h().available());}}
  @Test void nativefoodGetterExceptionClearsOld(){try(Host f=new Host()){f.runtime.playerHunger(new Hunger());Hunger bad=new Hunger();bad.failFood=true;assertThrows(IllegalStateException.class,()->f.runtime.playerHunger(bad));assertFalse(f.h().available());}}
  @Test void nativesaturationGetterExceptionClearsOld(){try(Host f=new Host()){f.runtime.playerHunger(new Hunger());Hunger bad=new Hunger();bad.failSaturation=true;assertThrows(IllegalStateException.class,()->f.runtime.playerHunger(bad));assertFalse(f.h().available());}}
  @Test void invalidOriginalFoodClearsOld(){try(Host f=new Host()){f.runtime.playerHunger(new Hunger());Hunger bad=new Hunger();bad.food=21;assertThrows(IllegalArgumentException.class,()->f.runtime.playerHunger(bad));assertFalse(f.h().available());}}
  @Test void nonfiniteOriginalSaturationClearsOld(){try(Host f=new Host()){f.runtime.playerHunger(new Hunger());Hunger bad=new Hunger();bad.saturation=Float.NaN;assertThrows(IllegalArgumentException.class,()->f.runtime.playerHunger(bad));assertFalse(f.h().available());}}
  @Test void changedOriginalXpDuringlevelClears(){try(Host f=new Host()){Experience a=new Experience(),b=new Experience();a.onLevel=()->f.runtime.playerExperience(b);f.runtime.playerExperience(a);assertFalse(f.xp().available());assertEquals(0,b.lReads+b.tReads+b.pReads+b.cReads);}}
  @Test void nestedSameXpDuringlevelNoRepeat(){try(Host f=new Host()){Experience a=new Experience();a.onLevel=()->f.runtime.playerExperience(a);f.runtime.playerExperience(a);assertTrue(f.xp().available());assertEquals(1,a.lReads);assertEquals(1,a.tReads);assertEquals(1,a.pReads);assertEquals(1,a.cReads);}}
  @Test void changedOriginalXpDuringtotalClears(){try(Host f=new Host()){Experience a=new Experience(),b=new Experience();a.onTotal=()->f.runtime.playerExperience(b);f.runtime.playerExperience(a);assertFalse(f.xp().available());assertEquals(0,b.lReads+b.tReads+b.pReads+b.cReads);}}
  @Test void nestedSameXpDuringtotalNoRepeat(){try(Host f=new Host()){Experience a=new Experience();a.onTotal=()->f.runtime.playerExperience(a);f.runtime.playerExperience(a);assertTrue(f.xp().available());assertEquals(1,a.lReads);assertEquals(1,a.tReads);assertEquals(1,a.pReads);assertEquals(1,a.cReads);}}
  @Test void changedOriginalXpDuringprogressClears(){try(Host f=new Host()){Experience a=new Experience(),b=new Experience();a.onProgress=()->f.runtime.playerExperience(b);f.runtime.playerExperience(a);assertFalse(f.xp().available());assertEquals(0,b.lReads+b.tReads+b.pReads+b.cReads);}}
  @Test void nestedSameXpDuringprogressNoRepeat(){try(Host f=new Host()){Experience a=new Experience();a.onProgress=()->f.runtime.playerExperience(a);f.runtime.playerExperience(a);assertTrue(f.xp().available());assertEquals(1,a.lReads);assertEquals(1,a.tReads);assertEquals(1,a.pReads);assertEquals(1,a.cReads);}}
  @Test void changedOriginalXpDuringcapClears(){try(Host f=new Host()){Experience a=new Experience(),b=new Experience();a.onCap=()->f.runtime.playerExperience(b);f.runtime.playerExperience(a);assertFalse(f.xp().available());assertEquals(0,b.lReads+b.tReads+b.pReads+b.cReads);}}
  @Test void nestedSameXpDuringcapNoRepeat(){try(Host f=new Host()){Experience a=new Experience();a.onCap=()->f.runtime.playerExperience(a);f.runtime.playerExperience(a);assertTrue(f.xp().available());assertEquals(1,a.lReads);assertEquals(1,a.tReads);assertEquals(1,a.pReads);assertEquals(1,a.cReads);}}
  @Test void nativeXplevelThrowClearsOld(){try(Host f=new Host()){f.runtime.playerExperience(new Experience());Experience bad=new Experience();bad.failLevel=true;assertThrows(IllegalStateException.class,()->f.runtime.playerExperience(bad));assertFalse(f.xp().available());}}
  @Test void nativeXptotalThrowClearsOld(){try(Host f=new Host()){f.runtime.playerExperience(new Experience());Experience bad=new Experience();bad.failTotal=true;assertThrows(IllegalStateException.class,()->f.runtime.playerExperience(bad));assertFalse(f.xp().available());}}
  @Test void nativeXpprogressThrowClearsOld(){try(Host f=new Host()){f.runtime.playerExperience(new Experience());Experience bad=new Experience();bad.failProgress=true;assertThrows(IllegalStateException.class,()->f.runtime.playerExperience(bad));assertFalse(f.xp().available());}}
  @Test void nativeXpcapThrowClearsOld(){try(Host f=new Host()){f.runtime.playerExperience(new Experience());Experience bad=new Experience();bad.failCap=true;assertThrows(IllegalStateException.class,()->f.runtime.playerExperience(bad));assertFalse(f.xp().available());}}
  @Test void invalidOriginalXpProgressClearsOld(){try(Host f=new Host()){f.runtime.playerExperience(new Experience());Experience bad=new Experience();bad.progress=Float.POSITIVE_INFINITY;assertThrows(IllegalArgumentException.class,()->f.runtime.playerExperience(bad));assertFalse(f.xp().available());}}
  @Test void invalidOriginalXpCapClearsOld(){try(Host f=new Host()){f.runtime.playerExperience(new Experience());Experience bad=new Experience();bad.cap=0;assertThrows(IllegalArgumentException.class,()->f.runtime.playerExperience(bad));assertFalse(f.xp().available());}}
  @Test void freshHungerAfterRevocationRecovers(){try(Host f=new Host()){Hunger a=new Hunger(),b=new Hunger();a.onFood=()->f.runtime.playerHunger(b);f.runtime.playerHunger(a);assertFalse(f.h().available());b.food=5;f.runtime.playerHunger(b);assertTrue(f.h().available());assertEquals(5,f.h().foodLevel());}}
  @Test void freshXpAfterRevocationRecovers(){try(Host f=new Host()){Experience a=new Experience(),b=new Experience();a.onLevel=()->f.runtime.playerExperience(b);f.runtime.playerExperience(a);assertFalse(f.xp().available());b.level=11;f.runtime.playerExperience(b);assertEquals(11,f.xp().level());}}
  @Test void originalNullClearsBothIndependentSnapshots(){try(Host f=new Host()){f.runtime.playerExperience(new Experience());f.runtime.playerHunger(new Hunger());f.runtime.playerExperience(null);f.runtime.playerHunger(null);assertFalse(f.xp().available());assertFalse(f.h().available());}}
  @Test void nestedXpDoesNotRevokeIndependentHunger(){try(Host f=new Host()){Hunger a=new Hunger();a.onFood=()->f.runtime.playerExperience(new Experience());f.runtime.playerHunger(a);assertTrue(f.h().available());assertTrue(f.xp().available());}}
}
