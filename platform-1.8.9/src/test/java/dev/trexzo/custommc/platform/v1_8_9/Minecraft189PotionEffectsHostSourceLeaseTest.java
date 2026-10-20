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

final class Minecraft189PotionEffectsHostSourceLeaseTest {
    static final class Effect implements Minecraft189PotionEffectAccess {
        int id = 1, duration = 200, amplifier = 0;
        String name = "Speed";
        int[] calls = new int[4];
        int callbackAt = -1, failAt = -1, errorAt = -1;
        Runnable onGetter;
        void sample(int i) {
            calls[i]++;
            if (callbackAt == i && onGetter != null) {
                Runnable r = onGetter; onGetter = null; r.run();
            }
            if (errorAt == i) throw new AssertionError("native effect getter");
            if (failAt == i) throw new IllegalStateException("native effect getter");
        }
        @Override public int customMcPotionId() { sample(0); return id; }
        @Override public int customMcDurationTicks() { sample(1); return duration; }
        @Override public int customMcAmplifier() { sample(2); return amplifier; }
        @Override public String customMcEffectName() { sample(3); return name; }
        int total() { int n=0; for(int i:calls) n+=i; return n; }
    }

    static final class OriginalEffects implements Minecraft189PlayerPotionEffectsAccess {
        Minecraft189PotionEffectAccess[] effects = {new Effect()};
        Runnable onArray;
        boolean errorArray, failArray;
        int arrayReads;
        @Override public Minecraft189PotionEffectAccess[] customMcPotionEffects() {
            arrayReads++;
            Runnable r=onArray; onArray=null;
            if(r!=null) r.run();
            if(errorArray) throw new AssertionError("native potion array");
            if(failArray) throw new IllegalStateException("native potion array");
            return effects;
        }
        Effect first() { return (Effect)effects[0]; }
    }

    static final class Fixture implements AutoCloseable {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final ServiceRegistry services = new ServiceRegistry();
        final Minecraft189HostRuntime runtime;
        Fixture() {
            services.register(RenderPipeline.class, new RenderPipeline());
            Minecraft189Platform platform = new Minecraft189Platform();
            platform.attach(new PlatformContext(
                    new EventBus(),modules,controller,services));
            runtime=Minecraft189HostRuntime.install(
                    platform,new ModulePresentationRegistry(),
                    new ModuleCategoryRegistry(),
                    new ModuleSettingRegistry(modules,settings),
                    null,null,settings,new SettingPresentationRegistry(),
                    new NoOpHost());
        }
        Minecraft189PlayerPotionEffectsState.StateSnapshot state() {
            return runtime.playerPotionEffectsState().snapshot();
        }
        @Override public void close() { runtime.close(); }
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

    @Test void realOriginalLeaseReleasesAndRecovers() {
        Minecraft189PotionEffectsHostSourceLease lease =
                new Minecraft189PotionEffectsHostSourceLease();
        OriginalEffects a = new OriginalEffects(), b = new OriginalEffects();
        assertTrue(lease.enter(a));
        assertTrue(lease.stillOwned());
        lease.exit();
        assertTrue(lease.enter(b));
        lease.exit();
    }
    @Test void nestedSamePlayerDoesNotReenterNativeGetter() {
        Minecraft189PotionEffectsHostSourceLease lease =
                new Minecraft189PotionEffectsHostSourceLease();
        OriginalEffects a = new OriginalEffects();
        assertTrue(lease.enter(a));
        assertFalse(lease.enter(a));
        assertTrue(lease.stillOwned());
        lease.exit();
    }
    @Test void nestedDifferentPlayerIrrevocablyRevokes() {
        Minecraft189PotionEffectsHostSourceLease lease =
                new Minecraft189PotionEffectsHostSourceLease();
        OriginalEffects a = new OriginalEffects(), b = new OriginalEffects();
        assertTrue(lease.enter(a));
        assertFalse(lease.enter(b));
        assertFalse(lease.enter(a));
        assertFalse(lease.stillOwned());
        lease.exit();
    }
    @Test void nestedNullRevokesAndNextOriginalRecovers() {
        Minecraft189PotionEffectsHostSourceLease lease =
                new Minecraft189PotionEffectsHostSourceLease();
        assertTrue(lease.enter(new OriginalEffects()));
        assertFalse(lease.enter(null));
        assertFalse(lease.stillOwned());
        lease.exit();
        assertTrue(lease.enter(new OriginalEffects()));
        lease.exit();
    }
    @Test void validOriginalRecordPublishedWithAllFourNativeFields() {
        try(Fixture f=new Fixture()) {
            OriginalEffects p=new OriginalEffects();
            f.runtime.playerPotionEffects(p);
            assertTrue(f.state().available());
            assertEquals(1,f.state().effects().size());
            assertEquals(1,f.state().effects().get(0).potionId());
            assertEquals(200,f.state().effects().get(0).durationTicks());
            assertEquals(0,f.state().effects().get(0).amplifier());
            assertEquals("Speed",f.state().effects().get(0).effectName());
            assertEquals(1,p.arrayReads);
            assertEquals(4,p.first().total());
        }
    }
    @Test void originalEmptyArrayMeansValidNoActiveEffects() {
        try(Fixture f=new Fixture()) {
            OriginalEffects p=new OriginalEffects();
            p.effects=new Minecraft189PotionEffectAccess[0];
            f.runtime.playerPotionEffects(p);
            assertTrue(f.state().available());
            assertTrue(f.state().effects().isEmpty());
        }
    }
    @Test void sameSourceNestedArrayDoesNotDoubleRead() {
        try(Fixture f=new Fixture()) {
            OriginalEffects p=new OriginalEffects();
            p.onArray=()->f.runtime.playerPotionEffects(p);
            f.runtime.playerPotionEffects(p);
            assertTrue(f.state().available());
            assertEquals(1,p.arrayReads);
            assertEquals(4,p.first().total());
        }
    }
    @Test void differentOriginalInsideArrayClearsPreviouslyValidState() {
        try(Fixture f=new Fixture()) {
            f.runtime.playerPotionEffects(new OriginalEffects());
            OriginalEffects p=new OriginalEffects(),other=new OriginalEffects();
            p.onArray=()->f.runtime.playerPotionEffects(other);
            f.runtime.playerPotionEffects(p);
            assertFalse(f.state().available());
            assertEquals(0,other.arrayReads);
            assertEquals(0,p.first().total());
        }
    }
    @Test void nullOriginalDuringArrayInvalidatesWithoutEffectReads() {
        try(Fixture f=new Fixture()) {
            OriginalEffects p=new OriginalEffects();
            p.onArray=()->f.runtime.playerPotionEffects(null);
            f.runtime.playerPotionEffects(p);
            assertFalse(f.state().available());
            assertEquals(0,p.first().total());
        }
    }
    @Test void changedOriginalDuringEachEffectGetterDiscardsSnapshot() {
        try(Fixture f=new Fixture()) {
            for(int i=0;i<4;i++) {
                f.runtime.playerPotionEffects(new OriginalEffects());
                OriginalEffects p=new OriginalEffects(),other=new OriginalEffects();
                p.first().callbackAt=i;
                p.first().onGetter=()->f.runtime.playerPotionEffects(other);
                f.runtime.playerPotionEffects(p);
                assertFalse(f.state().available(),"getter="+i);
                assertEquals(0,other.arrayReads);
                assertEquals(4,p.first().total());
            }
        }
    }
    @Test void nullOriginalDuringEachEffectGetterDiscardsSnapshot() {
        try(Fixture f=new Fixture()) {
            for(int i=0;i<4;i++) {
                f.runtime.playerPotionEffects(new OriginalEffects());
                OriginalEffects p=new OriginalEffects();
                p.first().callbackAt=i;
                p.first().onGetter=()->f.runtime.playerPotionEffects(null);
                f.runtime.playerPotionEffects(p);
                assertFalse(f.state().available(),"getter="+i);
            }
        }
    }
    @Test void sameOriginalDuringAllFourEffectsDoesNotDoubleGet() {
        try(Fixture f=new Fixture()) {
            for(int i=0;i<4;i++) {
                OriginalEffects p=new OriginalEffects();
                p.first().callbackAt=i;
                p.first().onGetter=()->f.runtime.playerPotionEffects(p);
                f.runtime.playerPotionEffects(p);
                assertTrue(f.state().available());
                assertEquals(1,p.arrayReads);
                assertEquals(4,p.first().total());
            }
        }
    }
    @Test void everyEffectGetterRuntimeFailureClearsOldData() {
        try(Fixture f=new Fixture()) {
            for(int i=0;i<4;i++) {
                f.runtime.playerPotionEffects(new OriginalEffects());
                OriginalEffects p=new OriginalEffects();
                p.first().failAt=i;
                assertThrows(IllegalStateException.class,
                        ()->f.runtime.playerPotionEffects(p));
                assertFalse(f.state().available(),"getter="+i);
            }
        }
    }
    @Test void nativeArrayRuntimeExceptionAndErrorClearOldData() {
        try(Fixture f=new Fixture()) {
            f.runtime.playerPotionEffects(new OriginalEffects());
            OriginalEffects bad=new OriginalEffects(); bad.failArray=true;
            assertThrows(IllegalStateException.class,
                    ()->f.runtime.playerPotionEffects(bad));
            assertFalse(f.state().available());
            f.runtime.playerPotionEffects(new OriginalEffects());
            bad.failArray=false;bad.errorArray=true;
            assertThrows(AssertionError.class,
                    ()->f.runtime.playerPotionEffects(bad));
            assertFalse(f.state().available());
        }
    }
    @Test void effectGetterErrorAlsoClearsAndPropagates() {
        try(Fixture f=new Fixture()) {
            f.runtime.playerPotionEffects(new OriginalEffects());
            OriginalEffects bad=new OriginalEffects(); bad.first().errorAt=3;
            assertThrows(AssertionError.class,()->f.runtime.playerPotionEffects(bad));
            assertFalse(f.state().available());
        }
    }
    @Test void badNullEffectOrNullArrayFailClosed() {
        try(Fixture f=new Fixture()) {
            f.runtime.playerPotionEffects(new OriginalEffects());
            OriginalEffects p=new OriginalEffects();p.effects=null;
            assertThrows(NullPointerException.class,()->f.runtime.playerPotionEffects(p));
            assertFalse(f.state().available());
            p.effects=new Minecraft189PotionEffectAccess[]{null};
            assertThrows(NullPointerException.class,()->f.runtime.playerPotionEffects(p));
            assertFalse(f.state().available());
        }
    }
    @Test void invalidOriginalDurationAmplifierAndNameClearOldData() {
        try(Fixture f=new Fixture()) {
            for(int n=0;n<3;n++) {
                f.runtime.playerPotionEffects(new OriginalEffects());
                OriginalEffects p=new OriginalEffects();
                if(n==0)p.first().duration=-1;
                if(n==1)p.first().amplifier=-1;
                if(n==2)p.first().name=" ";
                assertThrows(IllegalArgumentException.class,
                        ()->f.runtime.playerPotionEffects(p));
                assertFalse(f.state().available());
            }
        }
    }
    @Test void failClosedSourceCanPublishOnlyFreshOriginalAfterFinally() {
        try(Fixture f=new Fixture()) {
            OriginalEffects first=new OriginalEffects(),other=new OriginalEffects();
            first.first().callbackAt=2;
            first.first().onGetter=()->f.runtime.playerPotionEffects(other);
            f.runtime.playerPotionEffects(first);
            assertFalse(f.state().available());
            other.first().name="Regeneration";
            f.runtime.playerPotionEffects(other);
            assertTrue(f.state().available());
            assertEquals("Regeneration",f.state().effects().get(0).effectName());
        }
    }
    @Test void explicitNullOriginalClearsPreviousAndRecovers() {
        try(Fixture f=new Fixture()) {
            f.runtime.playerPotionEffects(new OriginalEffects());
            f.runtime.playerPotionEffects(null);
            assertFalse(f.state().available());
            f.runtime.playerPotionEffects(new OriginalEffects());
            assertTrue(f.state().available());
        }
    }
    @Test void independentOriginalEffectsDoNotCorruptOneAnother() {
        try(Fixture f=new Fixture()) {
            OriginalEffects first=new OriginalEffects();
            Effect firstEffect=first.first();
            OriginalEffects second=new OriginalEffects();
            Effect secondEffect=second.first();
            secondEffect.name="Night Vision";
            firstEffect.callbackAt=1;
            firstEffect.onGetter=()->f.runtime.playerPotionEffects(second);
            f.runtime.playerPotionEffects(first);
            assertFalse(f.state().available());
            f.runtime.playerPotionEffects(second);
            assertTrue(f.state().available());
            assertEquals("Night Vision",f.state().effects().get(0).effectName());
        }
    }
}
