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

final class Minecraft189WorldTimeWeatherHostSourceLeaseTest {
    static final class OriginalTime implements Minecraft189WorldTimeAccess {
        long ticks = 9000L;
        int reads;
        Runnable callback;
        boolean throwsRuntime, throwsError;
        @Override public long customMcWorldTime() {
            reads++;
            Runnable r=callback; callback=null;
            if(r!=null)r.run();
            if(throwsRuntime)throw new IllegalStateException("original time");
            if(throwsError)throw new AssertionError("original time");
            return ticks;
        }
    }
    static final class OriginalWeather implements Minecraft189WorldWeatherAccess {
        boolean raining=true, thunder=true;
        int rainReads, thunderReads;
        Runnable rainCallback, thunderCallback;
        boolean rainThrows, thunderThrows, rainErrors, thunderErrors;
        @Override public boolean customMcRaining() {
            rainReads++;
            Runnable r=rainCallback; rainCallback=null;
            if(r!=null)r.run();
            if(rainThrows)throw new IllegalStateException("native rain");
            if(rainErrors)throw new AssertionError("native rain");
            return raining;
        }
        @Override public boolean customMcThundering() {
            thunderReads++;
            Runnable r=thunderCallback; thunderCallback=null;
            if(r!=null)r.run();
            if(thunderThrows)throw new IllegalStateException("native thunder");
            if(thunderErrors)throw new AssertionError("native thunder");
            return thunder;
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
            platform.attach(new PlatformContext(new EventBus(),
                    modules,controller,services));
            runtime=Minecraft189HostRuntime.install(platform,
                    new ModulePresentationRegistry(),new ModuleCategoryRegistry(),
                    new ModuleSettingRegistry(modules,settings),null,null,
                    settings,new SettingPresentationRegistry(),new NoOpHost());
        }
        Minecraft189WorldTimeState.Snapshot time() {
            return runtime.worldTimeState().snapshot();
        }
        Minecraft189WorldWeatherState.Snapshot weather() {
            return runtime.worldWeatherState().snapshot();
        }
        @Override public void close(){runtime.close();}
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

    @Test void independentOriginalWorldLeasesCanEnterAndRecover() {
        Minecraft189WorldTimeHostSourceLease time=new Minecraft189WorldTimeHostSourceLease();
        Minecraft189WorldWeatherHostSourceLease weather=new Minecraft189WorldWeatherHostSourceLease();
        OriginalTime t=new OriginalTime();
        OriginalWeather w=new OriginalWeather();
        assertTrue(time.enter(t));assertTrue(weather.enter(w));
        assertTrue(time.stillOwned());assertTrue(weather.stillOwned());
        assertFalse(time.enter(t));assertFalse(weather.enter(w));
        assertTrue(time.stillOwned());assertTrue(weather.stillOwned());
        time.exit();weather.exit();
        assertTrue(time.enter(new OriginalTime()));time.exit();
        assertTrue(weather.enter(new OriginalWeather()));weather.exit();
    }
    @Test void nestedChangedWorldRevokesBothLeaseKindsIndependently() {
        Minecraft189WorldTimeHostSourceLease time=new Minecraft189WorldTimeHostSourceLease();
        Minecraft189WorldWeatherHostSourceLease weather=new Minecraft189WorldWeatherHostSourceLease();
        assertTrue(time.enter(new OriginalTime()));
        assertTrue(weather.enter(new OriginalWeather()));
        assertFalse(time.enter(new OriginalTime()));
        assertFalse(time.stillOwned());assertTrue(weather.stillOwned());
        assertFalse(weather.enter(new OriginalWeather()));
        assertFalse(weather.stillOwned());
        time.exit();weather.exit();
    }
    @Test void nestedNullWorldRevokesOriginalLeases() {
        Minecraft189WorldTimeHostSourceLease time=new Minecraft189WorldTimeHostSourceLease();
        Minecraft189WorldWeatherHostSourceLease weather=new Minecraft189WorldWeatherHostSourceLease();
        assertTrue(time.enter(new OriginalTime()));
        assertTrue(weather.enter(new OriginalWeather()));
        assertFalse(time.enter(null));
        assertFalse(weather.enter(null));
        assertFalse(time.stillOwned());assertFalse(weather.stillOwned());
        time.exit();weather.exit();
    }
    @Test void genuineWorldTimeAcceptsZeroAndLongCycleValues() {
        try(Fixture f=new Fixture()) {
            OriginalTime w=new OriginalTime();
            for(long ticks:new long[]{0L,9000L,24000L,Long.MAX_VALUE}) {
                w.ticks=ticks;
                f.runtime.worldTime(w);
                assertTrue(f.time().available());
                assertEquals(ticks,f.time().worldTime());
            }
            assertEquals(4,w.reads);
        }
    }
    @Test void genuineWeatherTwoNativeGettersPublishTogether() {
        try(Fixture f=new Fixture()) {
            OriginalWeather w=new OriginalWeather();
            f.runtime.worldWeather(w);
            assertTrue(f.weather().available());
            assertTrue(f.weather().raining());
            assertTrue(f.weather().thundering());
            assertEquals(1,w.rainReads);assertEquals(1,w.thunderReads);
        }
    }
    @Test void unchangedVanillaNoRainNoThunderIsAvailable() {
        try(Fixture f=new Fixture()) {
            OriginalWeather w=new OriginalWeather();
            w.raining=false;w.thunder=false;
            f.runtime.worldWeather(w);
            assertTrue(f.weather().available());
            assertFalse(f.weather().raining());assertFalse(f.weather().thundering());
        }
    }
    @Test void sameOriginalTimeReentryDoesNotDoubleGet() {
        try(Fixture f=new Fixture()) {
            OriginalTime w=new OriginalTime();w.callback=()->f.runtime.worldTime(w);
            f.runtime.worldTime(w);
            assertTrue(f.time().available());assertEquals(1,w.reads);
        }
    }
    @Test void changedWorldDuringTimeGetterClearsOldTime() {
        try(Fixture f=new Fixture()) {
            f.runtime.worldTime(new OriginalTime());
            OriginalTime a=new OriginalTime(),b=new OriginalTime();
            a.callback=()->f.runtime.worldTime(b);
            f.runtime.worldTime(a);
            assertFalse(f.time().available());assertEquals(0,b.reads);
        }
    }
    @Test void nullWorldDuringTimeGetterClearsOldTime() {
        try(Fixture f=new Fixture()) {
            f.runtime.worldTime(new OriginalTime());
            OriginalTime a=new OriginalTime();
            a.callback=()->f.runtime.worldTime(null);
            f.runtime.worldTime(a);
            assertFalse(f.time().available());
        }
    }
    @Test void worldTimeGetterExceptionClearsAndPropagates() {
        try(Fixture f=new Fixture()) {
            f.runtime.worldTime(new OriginalTime());
            OriginalTime bad=new OriginalTime();bad.throwsRuntime=true;
            assertThrows(IllegalStateException.class,()->f.runtime.worldTime(bad));
            assertFalse(f.time().available());
        }
    }
    @Test void worldTimeGetterErrorClearsAndPropagates() {
        try(Fixture f=new Fixture()) {
            f.runtime.worldTime(new OriginalTime());
            OriginalTime bad=new OriginalTime();bad.throwsError=true;
            assertThrows(AssertionError.class,()->f.runtime.worldTime(bad));
            assertFalse(f.time().available());
        }
    }
    @Test void nestedSameSourceRainAndThunderDoNotSpendSecondTick() {
        try(Fixture f=new Fixture()) {
            for(int stage=0;stage<2;stage++) {
                OriginalWeather w=new OriginalWeather();
                if(stage==0)w.rainCallback=()->f.runtime.worldWeather(w);
                else w.thunderCallback=()->f.runtime.worldWeather(w);
                f.runtime.worldWeather(w);
                assertTrue(f.weather().available());
                assertEquals(1,w.rainReads);assertEquals(1,w.thunderReads);
            }
        }
    }
    @Test void changedWorldDuringEitherWeatherGetterClearsSnapshot() {
        try(Fixture f=new Fixture()) {
            for(int stage=0;stage<2;stage++) {
                f.runtime.worldWeather(new OriginalWeather());
                OriginalWeather w=new OriginalWeather(),other=new OriginalWeather();
                if(stage==0)w.rainCallback=()->f.runtime.worldWeather(other);
                else w.thunderCallback=()->f.runtime.worldWeather(other);
                f.runtime.worldWeather(w);
                assertFalse(f.weather().available(),"native stage "+stage);
                assertEquals(0,other.rainReads);assertEquals(0,other.thunderReads);
                assertEquals(1,w.rainReads);
                assertEquals(stage==0?0:1,w.thunderReads);
            }
        }
    }
    @Test void nullWorldDuringEitherWeatherGetterClearsSnapshot() {
        try(Fixture f=new Fixture()) {
            for(int stage=0;stage<2;stage++) {
                f.runtime.worldWeather(new OriginalWeather());
                OriginalWeather w=new OriginalWeather();
                if(stage==0)w.rainCallback=()->f.runtime.worldWeather(null);
                else w.thunderCallback=()->f.runtime.worldWeather(null);
                f.runtime.worldWeather(w);
                assertFalse(f.weather().available());
            }
        }
    }
    @Test void rainingGetterFailureCannotKeepOldThunder() {
        try(Fixture f=new Fixture()) {
            f.runtime.worldWeather(new OriginalWeather());
            OriginalWeather w=new OriginalWeather();w.rainThrows=true;
            assertThrows(IllegalStateException.class,()->f.runtime.worldWeather(w));
            assertFalse(f.weather().available());
        }
    }
    @Test void thunderGetterFailureCannotKeepOldRain() {
        try(Fixture f=new Fixture()) {
            f.runtime.worldWeather(new OriginalWeather());
            OriginalWeather w=new OriginalWeather();w.thunderThrows=true;
            assertThrows(IllegalStateException.class,()->f.runtime.worldWeather(w));
            assertFalse(f.weather().available());
        }
    }
    @Test void mappedOriginalWeatherErrorsClearAndPropagate() {
        try(Fixture f=new Fixture()) {
            for(int stage=0;stage<2;stage++) {
                f.runtime.worldWeather(new OriginalWeather());
                OriginalWeather w=new OriginalWeather();
                if(stage==0)w.rainErrors=true;
                else w.thunderErrors=true;
                assertThrows(AssertionError.class,()->f.runtime.worldWeather(w));
                assertFalse(f.weather().available());
            }
        }
    }
    @Test void explicitWorldUnloadClearsTimeAndWeather() {
        try(Fixture f=new Fixture()) {
            f.runtime.worldTime(new OriginalTime());
            f.runtime.worldWeather(new OriginalWeather());
            f.runtime.worldTime(null);
            f.runtime.worldWeather(null);
            assertFalse(f.time().available());
            assertFalse(f.weather().available());
        }
    }
    @Test void freshWorldAfterOriginalRevocationRecovers() {
        try(Fixture f=new Fixture()) {
            OriginalTime a=new OriginalTime(),b=new OriginalTime();
            a.callback=()->f.runtime.worldTime(b);
            f.runtime.worldTime(a);
            assertFalse(f.time().available());
            b.ticks=12000L;f.runtime.worldTime(b);
            assertTrue(f.time().available());assertEquals(12000L,f.time().worldTime());

            OriginalWeather w=new OriginalWeather(),next=new OriginalWeather();
            w.rainCallback=()->f.runtime.worldWeather(next);
            f.runtime.worldWeather(w);
            assertFalse(f.weather().available());
            next.raining=false;
            f.runtime.worldWeather(next);
            assertTrue(f.weather().available());
            assertFalse(f.weather().raining());
        }
    }
    @Test void worldTimeAndWeatherRemainSeparateOriginalHostStates() {
        try(Fixture f=new Fixture()) {
            OriginalTime time=new OriginalTime();
            OriginalWeather weather=new OriginalWeather();
            time.callback=()->f.runtime.worldWeather(weather);
            f.runtime.worldTime(time);
            assertTrue(f.time().available());assertTrue(f.weather().available());
            assertEquals(9000L,f.time().worldTime());
        }
    }
}
