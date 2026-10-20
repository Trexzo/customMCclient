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

/** Real-shaped mapped native World evidence; official Mojang test is separate. */
final class Minecraft189WorldEntityDownstreamSourceLeaseTest {
    private static final class World implements
            Minecraft189WorldEntityPositionsAccess,
            Minecraft189WorldEntityKindsAccess,
            Minecraft189WorldEntityCombatAccess,
            Minecraft189WorldEntityUuidAccess,
            Minecraft189WorldEntityVisibilityAccess {
        double[] positions;
        int[] kinds={Minecraft189WorldEntityKindState.LIVING
                | Minecraft189WorldEntityKindState.PLAYER};
        int[] combat={1};
        UUID[] uuidValues={UUID.fromString("00000000-0000-0000-0000-000000000001")};
        int[] visibilityValues={1};
        int positionsReads,kindReads,combatReads,uuidReads,visibilityReads;
        Runnable onPositions,onKind,onCombat,onUuid,onVisibility;
        Runnable onThirdUuid,onThirdVisibility;
        boolean failKind,failCombat,failThirdUuid,failThirdVisibility;
        World(double x){positions=new double[]{x,1D,2D};}
        @Override public double[] customMcLoadedEntityPositions(){
            positionsReads++;
            Runnable r=onPositions;onPositions=null;if(r!=null)r.run();
            return positions;
        }
        @Override public int[] customMcLoadedEntityKinds(){
            kindReads++;if(kindReads>9)throw new AssertionError("recursive original kind");
            Runnable r=onKind;onKind=null;if(r!=null)r.run();
            if(failKind)throw new IllegalStateException("native kind getter");
            return kinds;
        }
        @Override public int[] customMcLoadedEntityCombatStates(){
            combatReads++;if(combatReads>9)throw new AssertionError("recursive original combat");
            Runnable r=onCombat;onCombat=null;if(r!=null)r.run();
            if(failCombat)throw new IllegalStateException("native combat getter");
            return combat;
        }
        @Override public UUID[] customMcLoadedEntityUuids(){
            uuidReads++;
            Runnable r=onUuid;onUuid=null;if(r!=null)r.run();
            if(uuidReads==3){
                Runnable late=onThirdUuid;onThirdUuid=null;
                if(late!=null)late.run();
                if(failThirdUuid)throw new IllegalStateException("post combat original UUID");
            }
            return uuidValues;
        }
        @Override public int[] customMcLoadedEntityVisibility(){
            visibilityReads++;
            Runnable r=onVisibility;onVisibility=null;if(r!=null)r.run();
            if(visibilityReads==3){
                Runnable late=onThirdVisibility;onThirdVisibility=null;
                if(late!=null)late.run();
                if(failThirdVisibility)throw new IllegalStateException("post combat original LOS");
            }
            return visibilityValues;
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
            Minecraft189Platform platform=new Minecraft189Platform();
            platform.attach(new PlatformContext(new EventBus(),modules,controller,services));
            runtime=Minecraft189HostRuntime.install(platform,
                new ModulePresentationRegistry(),new ModuleCategoryRegistry(),
                new ModuleSettingRegistry(modules,settings),
                null,null,settings,new SettingPresentationRegistry(),new NoOpHost());
        }
        void position(World w){runtime.worldEntityPositions(w);}
        void kind(World w){runtime.worldEntityKinds(w);}
        void combat(World w){runtime.worldEntityCombat(w);}
        void local(){
            runtime.playerPositionState().update(0D,1D,0D);
            runtime.playerRotationState().update(0F,0F);
        }
        Minecraft189NearestPlayerTargetState.Snapshot nearest(){
            return runtime.nearestPlayerTargetState().snapshot();
        }
        Minecraft189TargetRotationState.Snapshot aim(){
            return runtime.targetRotationState().snapshot();
        }
        void aura(){controller.enable(Minecraft189KillAuraModule.ID);}
        void wall(){controller.enable(Minecraft189WallCheckModule.ID);}
        Minecraft189WorldEntityPositionState.Snapshot pos(){return runtime.worldEntityPositionState().snapshot();}
        Minecraft189WorldEntityKindState.Snapshot kinds(){return runtime.worldEntityKindState().snapshot();}
        Minecraft189WorldEntityCombatState.Snapshot combats(){return runtime.worldEntityCombatState().snapshot();}
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
    @Test void firstWorldCanOwnKindOrCombatGetter(){Minecraft189WorldEntityDownstreamSourceLease l=new Minecraft189WorldEntityDownstreamSourceLease();World w=new World(1);assertTrue(l.enter(w));assertTrue(l.stillOwned());l.exit();assertTrue(l.enter(w));l.exit();}
    @Test void nestedSameWorldIsNotAnotherHostTick(){Minecraft189WorldEntityDownstreamSourceLease l=new Minecraft189WorldEntityDownstreamSourceLease();World w=new World(1);assertTrue(l.enter(w));assertFalse(l.enter(w));assertTrue(l.stillOwned());l.exit();}
    @Test void differentWorldRevokesNativeGetterStack(){Minecraft189WorldEntityDownstreamSourceLease l=new Minecraft189WorldEntityDownstreamSourceLease();World a=new World(1),b=new World(2);assertTrue(l.enter(a));assertFalse(l.enter(b));assertFalse(l.stillOwned());assertFalse(l.enter(a));l.exit();assertTrue(l.enter(b));l.exit();}
    @Test void nestedNullOriginalWorldRevokesGetter(){Minecraft189WorldEntityDownstreamSourceLease l=new Minecraft189WorldEntityDownstreamSourceLease();assertTrue(l.enter(new World(1)));assertFalse(l.enter(null));assertFalse(l.stillOwned());l.exit();}
    @Test void initialNullWorldDoesNotInventNewOne(){Minecraft189WorldEntityDownstreamSourceLease l=new Minecraft189WorldEntityDownstreamSourceLease();assertTrue(l.enter(null));assertFalse(l.enter(new World(1)));assertFalse(l.stillOwned());l.exit();}
    @Test void genuineNativeFailureFinallyReleasesOriginalLease(){Minecraft189WorldEntityDownstreamSourceLease l=new Minecraft189WorldEntityDownstreamSourceLease();assertTrue(l.enter(new World(1)));try{throw new IllegalStateException("native kind getter");}catch(IllegalStateException e){assertEquals("native kind getter",e.getMessage());}finally{l.exit();}assertTrue(l.enter(new World(2)));l.exit();}
    @Test void originalPositionsThenKindsThenCombatStayOnSameWorld(){try(Fixture f=new Fixture()){World w=new World(1);f.position(w);f.kind(w);f.combat(w);assertTrue(f.pos().available());assertTrue(f.kinds().available());assertTrue(f.combats().available());assertTrue(f.combats().alive(0));assertEquals(1,w.kindReads);assertEquals(1,w.combatReads);}}
    @Test void genuineEmptyLoadedListHasAvailableEmptyKindsAndCombat(){try(Fixture f=new Fixture()){World w=new World(1);w.positions=new double[0];w.kinds=new int[0];w.combat=new int[0];f.position(w);f.kind(w);f.combat(w);assertEquals(0,f.pos().entityCount());assertTrue(f.kinds().available());assertTrue(f.combats().available());}}
    @Test void kindsBeforeAnyPositionCannotInventOriginalWorld(){try(Fixture f=new Fixture()){World w=new World(1);f.kind(w);assertFalse(f.kinds().available());assertEquals(0,w.kindReads);}}
    @Test void differentOriginalWorldAtKindEntryRevokesOldXYZ(){try(Fixture f=new Fixture()){World a=new World(1),b=new World(2);f.position(a);f.kind(b);assertFalse(f.pos().available());assertFalse(f.kinds().available());assertEquals(0,b.kindReads);}}
    @Test void sameWorldReentryDuringKindGetterDoesNotReadTwice(){try(Fixture f=new Fixture()){World w=new World(1);f.position(w);w.onKind=()->f.kind(w);f.kind(w);assertTrue(f.kinds().available());assertEquals(1,w.kindReads);}}
    @Test void differentWorldDuringKindGetterDiscardsOldPosition(){try(Fixture f=new Fixture()){World a=new World(1),b=new World(2);f.position(a);a.onKind=()->f.kind(b);f.kind(a);assertFalse(f.pos().available());assertFalse(f.kinds().available());assertEquals(1,a.kindReads);assertEquals(0,b.kindReads);}}
    @Test void nullWorldInsideKindGetterClearsOldLoadedList(){try(Fixture f=new Fixture()){World w=new World(1);f.position(w);w.onKind=()->f.kind(null);f.kind(w);assertFalse(f.pos().available());assertFalse(f.kinds().available());}}
    @Test void originalKindGetterThrowDoesNotRetainOldCombatOrPosition(){try(Fixture f=new Fixture()){World w=new World(1);f.position(w);w.failKind=true;assertThrows(IllegalStateException.class,()->f.kind(w));assertFalse(f.pos().available());assertFalse(f.kinds().available());}}
    @Test void malformedNativeKindsClearAllOlderEntityEvidence(){try(Fixture f=new Fixture()){World w=new World(1);f.position(w);w.kinds=new int[]{0x400000};assertThrows(IllegalArgumentException.class,()->f.kind(w));assertFalse(f.pos().available());assertFalse(f.kinds().available());}}
    @Test void differentNativeKindCountCannotBorrowXYZByIndex(){try(Fixture f=new Fixture()){World w=new World(1);f.position(w);w.kinds=new int[0];f.kind(w);assertFalse(f.pos().available());assertFalse(f.kinds().available());}}
    @Test void nextPositionSampleForSameWorldInvalidatesOldKinds(){try(Fixture f=new Fixture()){World w=new World(1);f.position(w);f.kind(w);assertTrue(f.kinds().available());f.position(w);assertFalse(f.kinds().available());f.combat(w);assertFalse(f.combats().available());}}
    @Test void combatBeforeKindCannotInventEligibleEntity(){try(Fixture f=new Fixture()){World w=new World(1);f.position(w);f.combat(w);assertFalse(f.combats().available());assertFalse(f.pos().available());}}
    @Test void differentOriginalWorldAtCombatEntryRevokesOldData(){try(Fixture f=new Fixture()){World a=new World(1),b=new World(2);f.position(a);f.kind(a);f.combat(b);assertFalse(f.pos().available());assertFalse(f.combats().available());assertEquals(0,b.combatReads);}}
    @Test void sameWorldReentryDuringCombatGetterCannotSpendSecondSample(){try(Fixture f=new Fixture()){World w=new World(1);f.position(w);f.kind(w);w.onCombat=()->f.combat(w);f.combat(w);assertTrue(f.combats().available());assertEquals(1,w.combatReads);}}
    @Test void changedWorldDuringCombatGetterCannotPublishOldStatus(){try(Fixture f=new Fixture()){World a=new World(1),b=new World(2);f.position(a);f.kind(a);a.onCombat=()->f.combat(b);f.combat(a);assertFalse(f.combats().available());assertFalse(f.kinds().available());assertEquals(0,b.combatReads);}}
    @Test void nativeCombatGetterThrowClearsOldWorldEvidence(){try(Fixture f=new Fixture()){World w=new World(1);f.position(w);f.kind(w);w.failCombat=true;assertThrows(IllegalStateException.class,()->f.combat(w));assertFalse(f.pos().available());assertFalse(f.kinds().available());}}
    @Test void wrongSizedOriginalCombatCannotProduceAliveTarget(){try(Fixture f=new Fixture()){World w=new World(1);f.position(w);f.kind(w);w.combat=new int[0];f.combat(w);assertFalse(f.combats().available());}}
    @Test void genuineNextWorldAfterRevocationRebuildsSource(){try(Fixture f=new Fixture()){World a=new World(1),b=new World(2);f.position(a);f.kind(b);assertFalse(f.pos().available());f.position(b);f.kind(b);f.combat(b);assertTrue(f.combats().available());assertEquals(2D,f.pos().x(0),1e-9);}}
    @Test void uuidRecheckOnChangedWorldDoesNotPublishCombat(){try(Fixture f=new Fixture()){f.aura();World a=new World(1),b=new World(2);f.position(a);f.kind(a);a.onUuid=()->f.combat(b);f.combat(a);assertFalse(f.pos().available());assertFalse(f.combats().available());assertEquals(0,a.combatReads+b.combatReads);}}
    @Test void visibilityRecheckOnChangedWorldDoesNotPublishCombat(){try(Fixture f=new Fixture()){f.wall();World a=new World(1),b=new World(2);f.position(a);f.kind(a);a.onVisibility=()->f.combat(b);f.combat(a);assertFalse(f.pos().available());assertFalse(f.combats().available());assertEquals(0,a.combatReads+b.combatReads);}}
    @Test void invalidUuidRecheckPreservesFailClosedLegacyIdentity(){try(Fixture f=new Fixture()){f.aura();World w=new World(1);f.position(w);f.kind(w);w.uuidValues=new UUID[]{UUID.randomUUID()};f.combat(w);assertFalse(f.combats().available());}}
    @Test void invalidVisibilityRecheckPreservesFailClosedLegacyWallCheck(){try(Fixture f=new Fixture()){f.wall();World w=new World(1);f.position(w);f.kind(w);w.visibilityValues=new int[]{0};f.combat(w);assertFalse(f.combats().available());}}    @Test void identityEnabledReadsExactOriginalUuidAfterCombat(){try(Fixture f=new Fixture()){f.aura();World w=new World(1);f.position(w);f.kind(w);f.combat(w);assertTrue(f.combats().available());assertEquals(3,w.uuidReads);assertEquals(1,w.combatReads);}}
    @Test void wallCheckEnabledReadsOriginalLosAfterCombat(){try(Fixture f=new Fixture()){f.wall();World w=new World(1);f.position(w);f.kind(w);f.combat(w);assertTrue(f.combats().available());assertEquals(3,w.visibilityReads);assertEquals(1,w.combatReads);}}
    @Test void bothOriginalPoliciesRevalidateAfterCombat(){try(Fixture f=new Fixture()){f.aura();f.wall();World w=new World(1);f.position(w);f.kind(w);f.combat(w);assertTrue(f.combats().available());assertEquals(3,w.uuidReads);assertEquals(3,w.visibilityReads);}}
    @Test void disabledPoliciesNeverInventUuidOrLosNativeReads(){try(Fixture f=new Fixture()){World w=new World(1);f.position(w);f.kind(w);f.combat(w);assertTrue(f.combats().available());assertEquals(0,w.uuidReads+w.visibilityReads);}}
    @Test void uuidChangesInsideRealCombatGetterVetoesPublication(){try(Fixture f=new Fixture()){f.aura();World w=new World(1);f.position(w);f.kind(w);w.onCombat=()->w.uuidValues=new UUID[]{UUID.randomUUID()};f.combat(w);assertFalse(f.combats().available());assertEquals(3,w.uuidReads);assertEquals(1,w.combatReads);}}
    @Test void sameLengthUuidReplacementStillFailsExactIdentity(){try(Fixture f=new Fixture()){f.aura();World w=new World(1);f.position(w);f.kind(w);w.onCombat=()->w.uuidValues=new UUID[]{UUID.fromString("00000000-0000-0000-0000-000000000002")};f.combat(w);assertFalse(f.combats().available());assertEquals(1,w.uuidValues.length);}}
    @Test void reorderedTwoUuidSourcesFailAfterCombatGetter(){try(Fixture f=new Fixture()){f.aura();World w=new World(1);w.positions=new double[]{1,1,2,2,1,2};w.kinds=new int[]{Minecraft189WorldEntityKindState.LIVING|Minecraft189WorldEntityKindState.PLAYER,Minecraft189WorldEntityKindState.LIVING|Minecraft189WorldEntityKindState.PLAYER};w.combat=new int[]{1,1};UUID first=w.uuidValues[0],second=UUID.fromString("00000000-0000-0000-0000-000000000002");w.uuidValues=new UUID[]{first,second};f.position(w);f.kind(w);w.onCombat=()->w.uuidValues=new UUID[]{second,first};f.combat(w);assertFalse(f.combats().available());assertEquals(2,w.uuidValues.length);}}
    @Test void nullOriginalUuidAfterCombatFailsClosed(){try(Fixture f=new Fixture()){f.aura();World w=new World(1);f.position(w);f.kind(w);w.onCombat=()->w.uuidValues=null;f.combat(w);assertFalse(f.combats().available());}}
    @Test void shortenedOriginalUuidAfterCombatFailsClosed(){try(Fixture f=new Fixture()){f.aura();World w=new World(1);f.position(w);f.kind(w);w.onCombat=()->w.uuidValues=new UUID[0];f.combat(w);assertFalse(f.combats().available());}}
    @Test void losBecomesOccludedInsideCombatGetterVetoesPublication(){try(Fixture f=new Fixture()){f.wall();World w=new World(1);f.position(w);f.kind(w);w.onCombat=()->w.visibilityValues=new int[]{0};f.combat(w);assertFalse(f.combats().available());assertEquals(3,w.visibilityReads);}}
    @Test void losBecomesUnknownInsideCombatGetterVetoesPublication(){try(Fixture f=new Fixture()){f.wall();World w=new World(1);f.position(w);f.kind(w);w.onCombat=()->w.visibilityValues=new int[]{-1};f.combat(w);assertFalse(f.combats().available());}}
    @Test void losArrayBecomesNullInsideCombatGetterFailsClosed(){try(Fixture f=new Fixture()){f.wall();World w=new World(1);f.position(w);f.kind(w);w.onCombat=()->w.visibilityValues=null;f.combat(w);assertFalse(f.combats().available());}}
    @Test void unchangedUuidCloneAfterCombatCountsAsSameOriginalSequence(){try(Fixture f=new Fixture()){f.aura();World w=new World(1);f.position(w);f.kind(w);w.onCombat=()->w.uuidValues=w.uuidValues.clone();f.combat(w);assertTrue(f.combats().available());}}
    @Test void unchangedLosCloneAfterCombatCountsAsSameOriginalSequence(){try(Fixture f=new Fixture()){f.wall();World w=new World(1);f.position(w);f.kind(w);w.onCombat=()->w.visibilityValues=w.visibilityValues.clone();f.combat(w);assertTrue(f.combats().available());}}
    @Test void identityDisabledAllowsPhysicalCombatWithoutUuidAuthority(){try(Fixture f=new Fixture()){World w=new World(1);f.position(w);f.kind(w);w.onCombat=()->w.uuidValues=new UUID[]{UUID.randomUUID()};f.combat(w);assertTrue(f.combats().available());assertEquals(0,w.uuidReads);}}
    @Test void wallCheckDisabledAllowsPhysicalCombatWithoutLosAuthority(){try(Fixture f=new Fixture()){World w=new World(1);f.position(w);f.kind(w);w.onCombat=()->w.visibilityValues=new int[]{0};f.combat(w);assertTrue(f.combats().available());assertEquals(0,w.visibilityReads);}}
    @Test void postCombatUuidNativeGetterExceptionFailsClosed(){try(Fixture f=new Fixture()){f.aura();World w=new World(1);f.position(w);f.kind(w);w.failThirdUuid=true;f.combat(w);assertFalse(f.combats().available());assertEquals(3,w.uuidReads);}}
    @Test void postCombatLosNativeGetterExceptionFailsClosed(){try(Fixture f=new Fixture()){f.wall();World w=new World(1);f.position(w);f.kind(w);w.failThirdVisibility=true;f.combat(w);assertFalse(f.combats().available());assertEquals(3,w.visibilityReads);}}
    @Test void changedWorldNestedInsidePostCombatUuidReadRevokesSource(){try(Fixture f=new Fixture()){f.aura();World a=new World(1),b=new World(2);f.position(a);f.kind(a);a.onThirdUuid=()->f.combat(b);f.combat(a);assertFalse(f.pos().available());assertFalse(f.kinds().available());assertFalse(f.combats().available());assertEquals(0,b.combatReads);}}
    @Test void changedWorldNestedInsidePostCombatLosReadRevokesSource(){try(Fixture f=new Fixture()){f.wall();World a=new World(1),b=new World(2);f.position(a);f.kind(a);a.onThirdVisibility=()->f.combat(b);f.combat(a);assertFalse(f.pos().available());assertFalse(f.kinds().available());assertFalse(f.combats().available());assertEquals(0,b.combatReads);}}
    @Test void sameWorldNestedInsidePostUuidReadCannotDuplicateCombat(){try(Fixture f=new Fixture()){f.aura();World w=new World(1);f.position(w);f.kind(w);w.onThirdUuid=()->f.combat(w);f.combat(w);assertTrue(f.combats().available());assertEquals(1,w.combatReads);}}
    @Test void sameWorldNestedInsidePostLosReadCannotDuplicateCombat(){try(Fixture f=new Fixture()){f.wall();World w=new World(1);f.position(w);f.kind(w);w.onThirdVisibility=()->f.combat(w);f.combat(w);assertTrue(f.combats().available());assertEquals(1,w.combatReads);}}
    @Test void freshRealWorldAfterLateUuidMismatchCanRecover(){try(Fixture f=new Fixture()){f.aura();World a=new World(1);f.position(a);f.kind(a);a.onCombat=()->a.uuidValues=new UUID[]{UUID.randomUUID()};f.combat(a);assertFalse(f.combats().available());World b=new World(2);f.position(b);f.kind(b);f.combat(b);assertTrue(f.combats().available());assertEquals(3,b.uuidReads);}}
    @Test void freshRealSampleOnSameWorldAfterLateLosMismatchCanRecover(){try(Fixture f=new Fixture()){f.wall();World w=new World(1);f.position(w);f.kind(w);w.onCombat=()->w.visibilityValues=new int[]{0};f.combat(w);assertFalse(f.combats().available());f.position(w);f.kind(w);f.combat(w);assertTrue(f.combats().available());assertEquals(6,w.visibilityReads);}}
    private static void assertRejectsMalformedCombat(
            final World w) {
        try (Fixture f = new Fixture()) {
            f.local();
            f.position(w);
            f.kind(w);
            assertTrue(f.nearest().available(), "original kind candidate should be published");
            assertTrue(f.nearest().found(), "original kind target should exist");
            assertTrue(f.aim().available(), "original kind aim should exist");
            f.combat(w);
            assertFalse(f.combats().available(), "invalid native combat remains unavailable");
            assertFalse(f.nearest().available(), "no kind-only target may survive bad combat");
            assertFalse(f.aim().available(), "no stale aim may survive bad combat");
            assertTrue(f.pos().available(), "the real original XYZ remains intact");
            assertTrue(f.kinds().available(), "the real original Kind remains intact");
            assertEquals(1,w.combatReads,"one genuine original combat sample only");
        }
    }
    @Test void nativeNullCombatRejectsOldNearest(){World w=new World(1);w.combat=null;assertRejectsMalformedCombat(w);}
    @Test void nativeEmptyCombatRejectsOldNearest(){World w=new World(1);w.combat=new int[0];assertRejectsMalformedCombat(w);}
    @Test void nativeExtraCombatIndexRejectsOldNearest(){World w=new World(1);w.combat=new int[]{1,1};assertRejectsMalformedCombat(w);}
    @Test void nativeNegativeTwoCombatRejectsOldNearest(){World w=new World(1);w.combat=new int[]{-2};assertRejectsMalformedCombat(w);}
    @Test void nativeNegativeThreeCombatRejectsOldNearest(){World w=new World(1);w.combat=new int[]{-3};assertRejectsMalformedCombat(w);}
    @Test void nativeMinimumIntegerCombatRejectsOldNearest(){World w=new World(1);w.combat=new int[]{Integer.MIN_VALUE};assertRejectsMalformedCombat(w);}
    @Test void nativeMaximumIntegerCombatRejectsOldNearest(){World w=new World(1);w.combat=new int[]{Integer.MAX_VALUE};assertRejectsMalformedCombat(w);}
    @Test void nativeOrphanedSameTeamBitRejectsOldNearest(){World w=new World(1);w.combat=new int[]{1|Minecraft189WorldEntityCombatState.SAME_TEAM};assertRejectsMalformedCombat(w);}
    @Test void nativeOrphanedNetworkPresentBitRejectsOldNearest(){World w=new World(1);w.combat=new int[]{1|Minecraft189WorldEntityCombatState.NETWORK_INFO_PRESENT};assertRejectsMalformedCombat(w);}
    @Test void nativeOversizedHurtEncodingRejectsOldNearest(){World w=new World(1);w.combat=new int[]{1<<14};assertRejectsMalformedCombat(w);}
    @Test void nativeGiantPositiveCombatRejectsOldNearest(){World w=new World(1);w.combat=new int[]{1000000};assertRejectsMalformedCombat(w);}
    @Test void nativeGetterBecomesNullDuringReadRejectsOldNearest(){World w=new World(1);w.onCombat=()->w.combat=null;assertRejectsMalformedCombat(w);}
    @Test void nativeGetterBecomesMalformedDuringReadRejectsOldNearest(){World w=new World(1);w.onCombat=()->w.combat=new int[]{-2};assertRejectsMalformedCombat(w);}
    @Test void nativeGetterShrinksDuringReadRejectsOldNearest(){World w=new World(1);w.onCombat=()->w.combat=new int[0];assertRejectsMalformedCombat(w);}
    @Test void nativeGetterExpandsDuringReadRejectsOldNearest(){World w=new World(1);w.onCombat=()->w.combat=new int[]{1,1};assertRejectsMalformedCombat(w);}
    @Test void genuineValidCombatPreservesOriginalSourceAndNearest(){try(Fixture f=new Fixture()){f.local();World w=new World(1);f.position(w);f.kind(w);f.combat(w);assertTrue(f.combats().available());assertTrue(f.combats().alive(0));assertTrue(f.nearest().found());assertTrue(f.aim().available());}}
    @Test void validCopiedCombatArrayRetainsActualTargetEvidence(){try(Fixture f=new Fixture()){f.local();World w=new World(1);w.onCombat=()->w.combat=w.combat.clone();f.position(w);f.kind(w);f.combat(w);assertTrue(f.combats().alive(0));assertTrue(f.nearest().found());assertTrue(f.aim().available());}}
    @Test void validNetworkInfoKnownCombatRetainsSource(){try(Fixture f=new Fixture()){f.local();World w=new World(1);w.combat=new int[]{1|Minecraft189WorldEntityCombatState.NETWORK_INFO_KNOWN|Minecraft189WorldEntityCombatState.NETWORK_INFO_PRESENT};f.position(w);f.kind(w);f.combat(w);assertTrue(f.combats().available());assertTrue(f.nearest().found());}}
    @Test void validTeamKnownCombatRetainsSource(){try(Fixture f=new Fixture()){f.local();World w=new World(1);w.combat=new int[]{1|Minecraft189WorldEntityCombatState.TEAM_KNOWN|Minecraft189WorldEntityCombatState.SAME_TEAM};f.position(w);f.kind(w);f.combat(w);assertTrue(f.combats().available());assertTrue(f.nearest().found());}}
    @Test void validMaxHurtEncodingRetainsSource(){try(Fixture f=new Fixture()){f.local();World w=new World(1);w.combat=new int[]{255};f.position(w);f.kind(w);f.combat(w);assertTrue(f.combats().available());assertTrue(f.nearest().found());assertEquals(127,f.combats().hurtTime(0));}}
    @Test void malformedAfterPreviouslyValidCombatClearsTargetAndAim(){try(Fixture f=new Fixture()){f.local();World w=new World(1);f.position(w);f.kind(w);f.combat(w);assertTrue(f.combats().available());assertTrue(f.nearest().found());w.combat=new int[]{-2};f.combat(w);assertFalse(f.combats().available());assertFalse(f.nearest().available());assertFalse(f.aim().available());}}
    @Test void freshRealCombatAndKindSampleRecoversAfterInvalid(){try(Fixture f=new Fixture()){f.local();World w=new World(1);f.position(w);f.kind(w);w.combat=null;f.combat(w);assertFalse(f.nearest().available());w.combat=new int[]{1};f.position(w);f.kind(w);f.combat(w);assertTrue(f.combats().alive(0));assertTrue(f.nearest().found());assertTrue(f.aim().available());}}
    @Test void malformedCombatWithEnabledWallCheckClearsCandidate(){try(Fixture f=new Fixture()){f.local();f.wall();World w=new World(1);w.combat=new int[]{-2};f.position(w);f.kind(w);assertTrue(f.nearest().found());f.combat(w);assertFalse(f.nearest().available());assertFalse(f.aim().available());assertTrue(f.pos().available());}}
    @Test void malformedCombatWithEnabledAuraClearsCandidate(){try(Fixture f=new Fixture()){f.local();f.aura();World w=new World(1);w.combat=new int[]{-2};f.position(w);f.kind(w);assertTrue(f.nearest().found());f.combat(w);assertFalse(f.combats().available());assertFalse(f.nearest().available());assertFalse(f.aim().available());}}
}
