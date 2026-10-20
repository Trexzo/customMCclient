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
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Original-shaped world loaded-entity getters. Mojang binary suite runs separately. */
final class Minecraft189WorldEntityHostSourceLeaseTest {
    private static final class World implements
            Minecraft189WorldEntityPositionsAccess,
            Minecraft189WorldEntityUuidAccess,
            Minecraft189WorldEntityVisibilityAccess {
        double[] positions;
        UUID[] uuids={UUID.fromString("00000000-0000-0000-0000-000000000001")};
        int[] visibility={1};
        int positionsReads,uuidReads,visibilityReads;
        Runnable onPositions,onUuids,onVisibility;
        boolean failPositions,failUuids,failVisibility;
        World(double x){positions=new double[]{x,2,3};}
        @Override public double[] customMcLoadedEntityPositions(){
            positionsReads++;if(positionsReads>8)throw new AssertionError("recursive native XYZ");
            final Runnable cb=onPositions;onPositions=null;if(cb!=null)cb.run();
            if(failPositions)throw new IllegalStateException("original positions getter");
            return positions;
        }
        @Override public UUID[] customMcLoadedEntityUuids(){
            uuidReads++;if(uuidReads>8)throw new AssertionError("recursive native UUID");
            final Runnable cb=onUuids;onUuids=null;if(cb!=null)cb.run();
            if(failUuids)throw new IllegalStateException("original UUID getter");
            return uuids;
        }
        @Override public int[] customMcLoadedEntityVisibility(){
            visibilityReads++;if(visibilityReads>8)throw new AssertionError("recursive native LOS");
            final Runnable cb=onVisibility;onVisibility=null;if(cb!=null)cb.run();
            if(failVisibility)throw new IllegalStateException("original LOS getter");
            return visibility;
        }
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
        Minecraft189WorldEntityPositionState.Snapshot pos(){return runtime.worldEntityPositionState().snapshot();}
        Minecraft189WorldEntityUuidState.Snapshot ids(){return runtime.worldEntityUuidState().snapshot();}
        Minecraft189WorldEntityVisibilityState.Snapshot los(){return runtime.worldEntityVisibilityState().snapshot();}
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
    @Test void leaseAllowsFreshOriginalWorld(){Minecraft189WorldEntityHostSourceLease l=new Minecraft189WorldEntityHostSourceLease();World a=new World(1);assertTrue(l.enter(a));assertTrue(l.stillOwned());l.exit();assertFalse(l.stillOwned());assertTrue(l.enter(a));l.exit();}
    @Test void nestedSameWorldDoesNotConsumeAnotherTick(){Minecraft189WorldEntityHostSourceLease l=new Minecraft189WorldEntityHostSourceLease();World a=new World(1);assertTrue(l.enter(a));assertFalse(l.enter(a));assertTrue(l.stillOwned());l.exit();}
    @Test void differentWorldRevokesFormerOriginalIrrevocably(){Minecraft189WorldEntityHostSourceLease l=new Minecraft189WorldEntityHostSourceLease();World a=new World(1),b=new World(2);assertTrue(l.enter(a));assertFalse(l.enter(b));assertFalse(l.stillOwned());assertFalse(l.enter(a));l.exit();assertTrue(l.enter(b));l.exit();}
    @Test void nestedNullWorldRevokesFormerOriginal(){Minecraft189WorldEntityHostSourceLease l=new Minecraft189WorldEntityHostSourceLease();assertTrue(l.enter(new World(1)));assertFalse(l.enter(null));assertFalse(l.stillOwned());l.exit();}
    @Test void initialNullWorldCannotInventDifferentWorld(){Minecraft189WorldEntityHostSourceLease l=new Minecraft189WorldEntityHostSourceLease();assertTrue(l.enter(null));assertFalse(l.enter(new World(1)));assertFalse(l.stillOwned());l.exit();}
    @Test void originalWorldGetterExceptionLeaseFinallyAllowsNewWorld(){Minecraft189WorldEntityHostSourceLease l=new Minecraft189WorldEntityHostSourceLease();assertTrue(l.enter(new World(1)));try{throw new IllegalStateException("real native getter");}catch(IllegalStateException e){assertEquals("real native getter",e.getMessage());}finally{l.exit();}assertTrue(l.enter(new World(2)));l.exit();}
    @Test void originalPositionsArePublishedAsExactNativeTriples(){try(Fixture f=new Fixture()){World w=new World(1);f.runtime.worldEntityPositions(w);assertTrue(f.pos().available());assertEquals(1,f.pos().entityCount());assertEquals(1D,f.pos().x(0),1e-9);assertEquals(1,w.positionsReads);}}
    @Test void emptyGenuineLoadedEntityListStillAvailable(){try(Fixture f=new Fixture()){World w=new World(1);w.positions=new double[0];f.runtime.worldEntityPositions(w);assertTrue(f.pos().available());assertEquals(0,f.pos().entityCount());}}
    @Test void nestedSameWorldDuringPositionsGetterNeverDuplicatesRead(){try(Fixture f=new Fixture()){World w=new World(1);w.onPositions=()->f.runtime.worldEntityPositions(w);f.runtime.worldEntityPositions(w);assertTrue(f.pos().available());assertEquals(1,w.positionsReads);}}
    @Test void differentOriginalWorldDuringPositionsGetterDiscardsOldTriples(){try(Fixture f=new Fixture()){World a=new World(1),b=new World(5);a.onPositions=()->f.runtime.worldEntityPositions(b);f.runtime.worldEntityPositions(a);assertFalse(f.pos().available());assertEquals(1,a.positionsReads);assertEquals(0,b.positionsReads);}}
    @Test void nestedNullDuringPositionsGetterDiscardsOldWorld(){try(Fixture f=new Fixture()){World a=new World(1);a.onPositions=()->f.runtime.worldEntityPositions(null);f.runtime.worldEntityPositions(a);assertFalse(f.pos().available());}}
    @Test void previousWorldPositionNotRetainedAfterChangedWorld(){try(Fixture f=new Fixture()){f.runtime.worldEntityPositions(new World(1));assertTrue(f.pos().available());World a=new World(2);a.onPositions=()->f.runtime.worldEntityPositions(new World(3));f.runtime.worldEntityPositions(a);assertFalse(f.pos().available());}}
    @Test void newRealWorldAfterRevocationPublishesItsOwnSource(){try(Fixture f=new Fixture()){World a=new World(1),b=new World(3);a.onPositions=()->f.runtime.worldEntityPositions(b);f.runtime.worldEntityPositions(a);assertFalse(f.pos().available());f.runtime.worldEntityPositions(b);assertTrue(f.pos().available());assertEquals(3D,f.pos().x(0),1e-9);}}
    @Test void genuineNullOuterWorldClearsOldEntityPosition(){try(Fixture f=new Fixture()){f.runtime.worldEntityPositions(new World(1));f.runtime.worldEntityPositions(null);assertFalse(f.pos().available());assertFalse(f.ids().available());}}
    @Test void originalNullPositionArrayClearsPublishedPosition(){try(Fixture f=new Fixture()){f.runtime.worldEntityPositions(new World(1));World w=new World(2);w.positions=null;f.runtime.worldEntityPositions(w);assertFalse(f.pos().available());}}
    @Test void mappedOriginalPositionGetterThrowClearsStaleEvidence(){try(Fixture f=new Fixture()){f.runtime.worldEntityPositions(new World(1));World w=new World(3);w.failPositions=true;assertThrows(IllegalStateException.class,()->f.runtime.worldEntityPositions(w));assertFalse(f.pos().available());assertFalse(f.ids().available());}}
    @Test void malformedTripleArrayCannotLeavePartialPosition(){try(Fixture f=new Fixture()){f.runtime.worldEntityPositions(new World(1));World w=new World(3);w.positions=new double[]{1,2};assertThrows(IllegalArgumentException.class,()->f.runtime.worldEntityPositions(w));assertFalse(f.pos().available());}}
    @Test void nonFiniteNativeEntityXYZCannotLeaveOldState(){try(Fixture f=new Fixture()){f.runtime.worldEntityPositions(new World(1));World w=new World(3);w.positions=new double[]{Double.NaN,2,3};assertThrows(IllegalArgumentException.class,()->f.runtime.worldEntityPositions(w));assertFalse(f.pos().available());}}
    @Test void nativeUuidSourceReadsOnlyWhenIdentityRequired(){try(Fixture f=new Fixture()){World w=new World(1);f.runtime.worldEntityPositions(w);assertEquals(0,w.uuidReads);f.controller.enable(Minecraft189KillAuraModule.ID);f.runtime.worldEntityPositions(w);assertEquals(1,w.uuidReads);assertTrue(f.ids().available());}}
    @Test void differentWorldInsideUuidGetterRevokesAllEvidence(){try(Fixture f=new Fixture()){f.controller.enable(Minecraft189KillAuraModule.ID);World a=new World(1),b=new World(3);a.onUuids=()->f.runtime.worldEntityPositions(b);f.runtime.worldEntityPositions(a);assertEquals(0,a.positionsReads+b.positionsReads);assertFalse(f.pos().available());assertFalse(f.ids().available());}}
    @Test void sameWorldInsideUuidGetterDoesNotStartSecondSource(){try(Fixture f=new Fixture()){f.controller.enable(Minecraft189KillAuraModule.ID);World a=new World(1);a.onUuids=()->f.runtime.worldEntityPositions(a);f.runtime.worldEntityPositions(a);assertEquals(1,a.uuidReads);assertEquals(1,a.positionsReads);assertTrue(f.ids().available());}}
    @Test void originalUuidGetterFailureStillAllowsPositionButNoIdentity(){try(Fixture f=new Fixture()){f.controller.enable(Minecraft189KillAuraModule.ID);World a=new World(1);a.failUuids=true;f.runtime.worldEntityPositions(a);assertTrue(f.pos().available());assertFalse(f.ids().available());assertEquals(1,a.positionsReads);}}
    @Test void changedWorldInVisibilityGetterRevokesBeforeUuidOrPosition(){try(Fixture f=new Fixture()){f.controller.enable(Minecraft189WallCheckModule.ID);World a=new World(1),b=new World(3);a.onVisibility=()->f.runtime.worldEntityPositions(b);f.runtime.worldEntityPositions(a);assertFalse(f.pos().available());assertFalse(f.ids().available());assertEquals(0,a.positionsReads+b.positionsReads);}}
    @Test void genuineVisibilityGetterFailureKeepsOriginalPositionButClearsLOS(){try(Fixture f=new Fixture()){f.controller.enable(Minecraft189WallCheckModule.ID);World a=new World(1);a.failVisibility=true;f.runtime.worldEntityPositions(a);assertTrue(f.pos().available());assertFalse(f.los().available());}}
}
