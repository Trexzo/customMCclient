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
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Genuine-shaped native fall read/write; game binary admission is separate. */
final class Minecraft189FallHostSourceLeaseTest {
    private static final class Player implements Minecraft189PlayerFallDistanceControl {
        float distance;
        int reads,writes;
        Runnable onFirstRead,onSecondRead,onWrite;
        boolean failRead,failAfterWrite;
        Player(float fall){distance=fall;}
        @Override public float customMcFallDistance(){
            reads++;
            if(reads>8)throw new AssertionError("recursive native fall getter");
            final Runnable callback=reads==1?onFirstRead:onSecondRead;
            if(reads==1)onFirstRead=null;
            if(reads==2)onSecondRead=null;
            if(callback!=null)callback.run();
            if(failRead)throw new IllegalStateException("original fall getter");
            return distance;
        }
        @Override public void customMcSetFallDistance(float value){
            writes++;if(writes>8)throw new AssertionError("recursive native fall setter");
            distance=value;
            final Runnable callback=onWrite;onWrite=null;
            if(callback!=null)callback.run();
            if(failAfterWrite)throw new IllegalStateException("original fall setter");
        }
    }
    private static final class Fixture implements AutoCloseable {
        final ModuleRegistry modules=new ModuleRegistry();
        final ModuleController controller=new ModuleController(modules);
        final SettingRegistry settings=new SettingRegistry();
        final ServiceRegistry services=new ServiceRegistry();
        final Minecraft189HostRuntime runtime;
        final Minecraft189CriticalsEvidence crit;
        Fixture(){
            services.register(RenderPipeline.class,new RenderPipeline());
            final Minecraft189Platform platform=new Minecraft189Platform();
            platform.attach(new PlatformContext(new EventBus(),modules,controller,services));
            runtime=Minecraft189HostRuntime.install(platform,
                new ModulePresentationRegistry(),new ModuleCategoryRegistry(),
                new ModuleSettingRegistry(modules,settings),
                null,null,settings,new SettingPresentationRegistry(),new NoOpHost());
            try{
                final Field f=Minecraft189HostRuntime.class.getDeclaredField("criticalsEvidence");
                f.setAccessible(true);
                crit=(Minecraft189CriticalsEvidence)f.get(runtime);
            }catch(ReflectiveOperationException e){throw new AssertionError(e);}
            // Genuine independent vertical motion evidence is required
            // before a fall sample can form an available Criticals pair.
            crit.motion(-0.25D);
        }
        void noFall(){controller.enable(Minecraft189NoFallModule.ID);}
        void fall(Player p){runtime.playerFallDistanceControl(p);}
        Minecraft189CriticalsEvidence.Snapshot evidence(){return crit.snapshot();}
        @Override public void close(){runtime.close();}
    }
    private static final class NoOpHost implements LegacyUiHostCallbacks {
        @Override public int framebufferWidth(){return 1280;}
        @Override public int framebufferHeight(){return 720;}
        @Override public float uiScale(){return 1F;}
        @Override public void beginUi(UiViewport v){}
        @Override public void fillRect(float x,float y,float w,float h,int c){}
        @Override public void fillRoundedRect(float x,float y,float h,float w,float r,int c){}
        @Override public void strokeRect(float x,float y,float w,float h,float t,int c){}
        @Override public void pushClip(float x,float y,float w,float h){}
        @Override public void popClip(){}
        @Override public void drawText(UiFontHandle f,float x,float y,String s,int c){}
        @Override public void endUi(){}
    }
    @Test void nativeSourceFirstAndSubsequentRealCallback(){Minecraft189FallHostSourceLease l=new Minecraft189FallHostSourceLease();Player p=new Player(3F);assertTrue(l.enter(p));assertTrue(l.stillOwned());l.exit();assertFalse(l.stillOwned());assertTrue(l.enter(p));l.exit();}
    @Test void nestedSamePlayerNeverCreatesSecondFallTick(){Minecraft189FallHostSourceLease l=new Minecraft189FallHostSourceLease();Player p=new Player(3F);assertTrue(l.enter(p));assertFalse(l.enter(p));assertTrue(l.stillOwned());l.exit();}
    @Test void nestedChangedPlayerRevokesFallReadPermanently(){Minecraft189FallHostSourceLease l=new Minecraft189FallHostSourceLease();Player a=new Player(2F),b=new Player(4F);assertTrue(l.enter(a));assertFalse(l.enter(b));assertFalse(l.enter(a));assertFalse(l.stillOwned());l.exit();}
    @Test void nestedNullWorldUnloadRevokesFallOwner(){Minecraft189FallHostSourceLease l=new Minecraft189FallHostSourceLease();assertTrue(l.enter(new Player(3F)));assertFalse(l.enter(null));assertFalse(l.stillOwned());l.exit();}
    @Test void initialNullCannotBorrowRealSource(){Minecraft189FallHostSourceLease l=new Minecraft189FallHostSourceLease();assertTrue(l.enter(null));assertFalse(l.enter(new Player(3F)));assertFalse(l.stillOwned());l.exit();}
    @Test void sourceExceptionFinallyUnwindsNativeStack(){Minecraft189FallHostSourceLease l=new Minecraft189FallHostSourceLease();assertTrue(l.enter(new Player(3F)));try{throw new IllegalStateException("original native getter");}catch(IllegalStateException e){assertEquals("original native getter",e.getMessage());}finally{l.exit();}assertTrue(l.enter(new Player(2F)));l.exit();}
    @Test void guardedSourceDelegatesActualNativeFallRead(){Minecraft189FallHostSourceLease l=new Minecraft189FallHostSourceLease();Player p=new Player(5F);assertTrue(l.enter(p));assertEquals(5F,l.guardedPlayer(p,()->false).customMcFallDistance(),0F);assertEquals(1,p.reads);l.exit();}
    @Test void guardedSourceWritesRealNoFallZeroWhileOwned(){Minecraft189FallHostSourceLease l=new Minecraft189FallHostSourceLease();Player p=new Player(5F);assertTrue(l.enter(p));l.guardedPlayer(p,()->false).customMcSetFallDistance(0F);assertEquals(0F,p.distance,0F);assertEquals(1,p.writes);l.exit();}
    @Test void changedOriginalSourcePreventsLaterNoFallSetter(){Minecraft189FallHostSourceLease l=new Minecraft189FallHostSourceLease();Player p=new Player(5F);assertTrue(l.enter(p));Minecraft189PlayerFallDistanceControl g=l.guardedPlayer(p,()->false);assertFalse(l.enter(new Player(1F)));g.customMcSetFallDistance(0F);assertEquals(0,p.writes);assertEquals(5F,p.distance,0F);l.exit();}
    @Test void genuineGuiFocusPreventsNativeNoFallSetter(){Minecraft189FallHostSourceLease l=new Minecraft189FallHostSourceLease();Player p=new Player(5F);assertTrue(l.enter(p));l.guardedPlayer(p,()->true).customMcSetFallDistance(0F);assertEquals(0,p.writes);assertEquals(5F,p.distance,0F);l.exit();}
    @Test void guardedNullSourceIsNeverSyntheticNativePlayer(){Minecraft189FallHostSourceLease l=new Minecraft189FallHostSourceLease();assertTrue(l.enter(null));assertNull(l.guardedPlayer(null,()->false));l.exit();}
    @Test void nativeSetterPartialWriteRemainsRealIfItThrows(){Minecraft189FallHostSourceLease l=new Minecraft189FallHostSourceLease();Player p=new Player(6F);p.failAfterWrite=true;assertTrue(l.enter(p));try{assertThrows(IllegalStateException.class,()->l.guardedPlayer(p,()->false).customMcSetFallDistance(0F));assertEquals(0F,p.distance,0F);}finally{l.exit();}assertTrue(l.enter(p));l.exit();}
    @Test void hostDisabledNoFallSamplesActualPositiveFall(){try(Fixture f=new Fixture()){Player p=new Player(7F);f.fall(p);assertTrue(f.evidence().available());assertEquals(7F,f.evidence().fallDistance(),0F);assertEquals(1,p.reads);assertEquals(0,p.writes);}}
    @Test void hostDisabledNoFallSamePlayerNestedGetterDoesNotDoubleRead(){try(Fixture f=new Fixture()){Player p=new Player(7F);p.onFirstRead=()->f.fall(p);f.fall(p);assertEquals(1,p.reads);assertTrue(f.evidence().available());}}
    @Test void hostDisabledNoFallChangedPlayerGetterRevokesCriticals(){try(Fixture f=new Fixture()){Player a=new Player(7F),b=new Player(3F);a.onFirstRead=()->f.fall(b);f.fall(a);assertFalse(f.evidence().available());assertEquals(1,a.reads);assertEquals(0,b.reads);}}
    @Test void hostDisabledNoFallNestedNullGetterRevokesCriticals(){try(Fixture f=new Fixture()){Player p=new Player(7F);p.onFirstRead=()->f.runtime.playerFallDistanceControl(null);f.fall(p);assertFalse(f.evidence().available());}}
    @Test void hostNewRealPlayerCanRecoverAfterRevocation(){try(Fixture f=new Fixture()){Player a=new Player(7F),b=new Player(3F);a.onFirstRead=()->f.fall(b);f.fall(a);assertFalse(f.evidence().available());f.fall(b);assertTrue(f.evidence().available());assertEquals(3F,f.evidence().fallDistance(),0F);}}
    @Test void hostNullOuterSourceRemovesFormerPositiveCriticals(){try(Fixture f=new Fixture()){f.fall(new Player(8F));assertTrue(f.evidence().available());f.runtime.playerFallDistanceControl(null);assertFalse(f.evidence().available());}}
    @Test void hostOriginalGetterFailureClearsOldCriticalsEvidence(){try(Fixture f=new Fixture()){f.fall(new Player(8F));Player p=new Player(3F);p.failRead=true;assertThrows(IllegalStateException.class,()->f.fall(p));assertFalse(f.evidence().available());}}
    @Test void hostNegativeNativeFallCannotAuthorizeCriticals(){try(Fixture f=new Fixture()){f.fall(new Player(8F));f.fall(new Player(-1F));assertFalse(f.evidence().available());}}
    @Test void hostNanNativeFallCannotAuthorizeCriticals(){try(Fixture f=new Fixture()){f.fall(new Player(8F));f.fall(new Player(Float.NaN));assertFalse(f.evidence().available());}}
    @Test void hostInfiniteNativeFallCannotAuthorizeCriticals(){try(Fixture f=new Fixture()){f.fall(new Player(8F));f.fall(new Player(Float.POSITIVE_INFINITY));assertFalse(f.evidence().available());}}
    @Test void hostEnabledNoFallZerosOnlyOneOriginalNativeSetter(){try(Fixture f=new Fixture()){f.noFall();Player p=new Player(6F);f.fall(p);assertEquals(1,p.writes);assertEquals(2,p.reads);assertEquals(0F,p.distance,0F);assertTrue(f.evidence().available());assertEquals(0F,f.evidence().fallDistance(),0F);}}
    @Test void hostEnabledNoFallFirstGetterSameSourceNestedHostDoesNotDouble(){try(Fixture f=new Fixture()){f.noFall();Player p=new Player(6F);p.onFirstRead=()->f.fall(p);f.fall(p);assertEquals(1,p.writes);assertEquals(2,p.reads);assertTrue(f.evidence().available());}}
    @Test void hostEnabledNoFallFirstGetterOtherSourceVetoesNativeZero(){try(Fixture f=new Fixture()){f.noFall();Player a=new Player(6F),b=new Player(4F);a.onFirstRead=()->f.fall(b);f.fall(a);assertEquals(0,a.writes+b.writes);assertFalse(f.evidence().available());assertEquals(6F,a.distance,0F);}}
    @Test void hostEnabledNoFallSetterChangedPlayerClearsCriticals(){try(Fixture f=new Fixture()){f.noFall();Player a=new Player(6F),b=new Player(4F);a.onWrite=()->f.fall(b);f.fall(a);assertEquals(1,a.writes);assertEquals(0F,a.distance,0F);assertEquals(0,b.reads+b.writes);assertFalse(f.evidence().available());}}
    @Test void hostEnabledNoFallSecondGetterChangedPlayerRevokesCriticals(){try(Fixture f=new Fixture()){f.noFall();Player a=new Player(6F),b=new Player(4F);a.onSecondRead=()->f.fall(b);f.fall(a);assertEquals(2,a.reads);assertEquals(1,a.writes);assertFalse(f.evidence().available());}}
    @Test void hostEnabledNoFallSecondGetterNullRevokesCriticals(){try(Fixture f=new Fixture()){f.noFall();Player a=new Player(6F);a.onSecondRead=()->f.runtime.playerFallDistanceControl(null);f.fall(a);assertFalse(f.evidence().available());}}
    @Test void hostEnabledNoFallSecondGetterSameSourceDoesNotDouble(){try(Fixture f=new Fixture()){f.noFall();Player a=new Player(6F);a.onSecondRead=()->f.fall(a);f.fall(a);assertEquals(2,a.reads);assertEquals(1,a.writes);assertTrue(f.evidence().available());}}
    @Test void hostEnabledNoFallSetterThrowLeavesRealZeroButNoCriticals(){try(Fixture f=new Fixture()){f.noFall();Player a=new Player(6F);a.failAfterWrite=true;assertThrows(IllegalStateException.class,()->f.fall(a));assertEquals(0F,a.distance,0F);assertEquals(1,a.writes);assertFalse(f.evidence().available());}}
    @Test void hostEnabledNoFallNextRealTickRecoversAfterGetterThrow(){try(Fixture f=new Fixture()){f.noFall();Player a=new Player(6F);a.failRead=true;assertThrows(IllegalStateException.class,()->f.fall(a));assertFalse(f.evidence().available());a.failRead=false;f.fall(a);assertTrue(f.evidence().available());assertEquals(1,a.writes);}}
}
