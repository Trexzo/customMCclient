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
import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189SprintHostSourceLeaseTest {
    private static final class Player implements Minecraft189PlayerSprintControl,
            Minecraft189PlayerMovementStateAccess {
        boolean sprint, sneak, failRead; int writes,trueWrites,falseWrites; boolean fail;
        Runnable onWrite, onRead;
        Player(boolean sprint){this.sprint=sprint;}
        @Override public boolean customMcOnGround() {
            final Runnable cb=onRead;onRead=null;if(cb!=null)cb.run();
            if(failRead)throw new IllegalStateException("original movement getter");
            return true;
        }
        @Override public boolean customMcSneaking(){return sneak;}
        @Override public boolean customMcSprinting(){return sprint;}
        @Override public void customMcSetSprinting(boolean value){
            writes++; if(writes>8)throw new AssertionError("recursive native sprint");
            sprint=value;if(value)trueWrites++;else falseWrites++;
            final Runnable cb=onWrite;onWrite=null;if(cb!=null)cb.run();
            if(fail)throw new IllegalStateException("original sprint native setter");
        }
    }
    private static Minecraft189SprintHostSourceLease lease(){
        return new Minecraft189SprintHostSourceLease();
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
        void state(boolean sprint){runtime.playerMovementState().update(true,false,sprint);}
        void auto(){controller.enable(Minecraft189AutoSprintModule.ID);}
        void wtap(){controller.enable(Minecraft189WTapModule.ID);}
        void keepSprint() {
            controller.enable(Minecraft189KeepSprintModule.ID);
            runtime.featureCatalog().keepSprint().requireForwardSetting().set(false);
        }
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
    @Test void leaseAllowsNewTickAfterFinally(){final Minecraft189SprintHostSourceLease l=new Minecraft189SprintHostSourceLease();final Player a=new Player(false);assertTrue(l.enter(a,false));assertTrue(l.stillOwned(false));l.exit();assertFalse(l.stillOwned(false));assertTrue(l.enter(a,false));l.exit();}
    @Test void nestedSamePlayerNeverDoubleDispatches(){final Minecraft189SprintHostSourceLease l=new Minecraft189SprintHostSourceLease();final Player a=new Player(false);assertTrue(l.enter(a,false));assertFalse(l.enter(a,false));assertTrue(l.stillOwned(false));l.exit();}
    @Test void differentPlayerRevokesEvenIfOriginalReturns(){final Minecraft189SprintHostSourceLease l=new Minecraft189SprintHostSourceLease();final Player a=new Player(false),b=new Player(false);assertTrue(l.enter(a,false));assertFalse(l.enter(b,false));assertFalse(l.enter(a,false));assertFalse(l.stillOwned(false));l.exit();assertTrue(l.enter(b,false));l.exit();}
    @Test void nestedNullWorldUnloadRevokesOriginal(){final Minecraft189SprintHostSourceLease l=new Minecraft189SprintHostSourceLease();assertTrue(l.enter(new Player(false),false));assertFalse(l.enter(null,false));assertFalse(l.stillOwned(false));l.exit();}
    @Test void initialNullSourceDoesNotInventPlayer(){final Minecraft189SprintHostSourceLease l=new Minecraft189SprintHostSourceLease();assertTrue(l.enter(null,false));assertNull(l.guardedPlayer(null,()->false));assertFalse(l.enter(new Player(false),false));l.exit();}
    @Test void guiFocusChangeRevokesOriginalLease(){final Minecraft189SprintHostSourceLease l=new Minecraft189SprintHostSourceLease();assertTrue(l.enter(new Player(false),false));assertFalse(l.stillOwned(true));assertFalse(l.stillOwned(false));l.exit();}
    @Test void guiAlreadyFocusedDoesNotInventFocusChange(){final Minecraft189SprintHostSourceLease l=new Minecraft189SprintHostSourceLease();final Player p=new Player(false);assertTrue(l.enter(p,true));assertFalse(l.enter(p,true));assertTrue(l.stillOwned(true));l.exit();}
    @Test void nestedSamePlayerGuiFocusRevokesOldLease(){final Minecraft189SprintHostSourceLease l=new Minecraft189SprintHostSourceLease();final Player p=new Player(false);assertTrue(l.enter(p,false));assertFalse(l.enter(p,true));assertFalse(l.stillOwned(false));l.exit();}
    @Test void guardedTrueSetterDelegatesActualNativeWrite(){final Minecraft189SprintHostSourceLease l=new Minecraft189SprintHostSourceLease();final Player p=new Player(false);assertTrue(l.enter(p,false));l.guardedPlayer(p,()->false).customMcSetSprinting(true);assertTrue(p.sprint);assertEquals(1,p.trueWrites);l.exit();}
    @Test void guardedFalseSetterDelegatesActualNativeWrite(){final Minecraft189SprintHostSourceLease l=new Minecraft189SprintHostSourceLease();final Player p=new Player(true);assertTrue(l.enter(p,false));l.guardedPlayer(p,()->false).customMcSetSprinting(false);assertFalse(p.sprint);assertEquals(1,p.falseWrites);l.exit();}
    @Test void revokedSourceCannotWriteEitherSprintValue(){final Minecraft189SprintHostSourceLease l=new Minecraft189SprintHostSourceLease();final Player p=new Player(true);assertTrue(l.enter(p,false));final Minecraft189PlayerSprintControl g=l.guardedPlayer(p,()->false);assertFalse(l.enter(new Player(false),false));g.customMcSetSprinting(false);g.customMcSetSprinting(true);assertEquals(0,p.writes);assertTrue(p.sprint);l.exit();}
    @Test void actualGuiVetoBlocksNativeSetter(){final Minecraft189SprintHostSourceLease l=new Minecraft189SprintHostSourceLease();final Player p=new Player(false);assertTrue(l.enter(p,false));l.guardedPlayer(p,()->true).customMcSetSprinting(true);assertEquals(0,p.writes);assertFalse(l.stillOwned(false));l.exit();}
    @Test void nativeSetterExceptionKeepsActualPartialWrite(){final Minecraft189SprintHostSourceLease l=new Minecraft189SprintHostSourceLease();final Player p=new Player(false);assertTrue(l.enter(p,false));p.fail=true;try{assertThrows(IllegalStateException.class,()->l.guardedPlayer(p,()->false).customMcSetSprinting(true));assertTrue(p.sprint);}finally{l.exit();}assertTrue(l.enter(new Player(false),false));l.exit();}
    @Test void nativeSetterChangedSourceRevokesNextWrite(){final Minecraft189SprintHostSourceLease l=new Minecraft189SprintHostSourceLease();final Player a=new Player(true),b=new Player(false);assertTrue(l.enter(a,false));final Minecraft189PlayerSprintControl g=l.guardedPlayer(a,()->false);a.onWrite=()->assertFalse(l.enter(b,false));g.customMcSetSprinting(false);g.customMcSetSprinting(true);assertEquals(1,a.writes);assertEquals(0,b.writes);l.exit();}
    @Test void nativeSetterSameSourceNestedTickNeverWritesAgain(){final Minecraft189SprintHostSourceLease l=new Minecraft189SprintHostSourceLease();final Player p=new Player(false);assertTrue(l.enter(p,false));p.onWrite=()->assertFalse(l.enter(p,false));l.guardedPlayer(p,()->false).customMcSetSprinting(true);assertEquals(1,p.writes);assertTrue(l.stillOwned(false));l.exit();}
    @Test void integratedAutoSprintStillWritesTrue(){try(Fixture f=new Fixture()){f.auto();f.state(false);final Player p=new Player(false);f.runtime.playerSprintControl(p);assertTrue(p.sprint);assertEquals(1,p.trueWrites);}}
    @Test void integratedAutoSprintSamePlayerNativeReentryNotDoubleTick(){try(Fixture f=new Fixture()){f.auto();f.state(false);final Player p=new Player(false);p.onWrite=()->f.runtime.playerSprintControl(p);f.runtime.playerSprintControl(p);assertEquals(1,p.trueWrites);}}
    @Test void integratedAutoSprintDifferentPlayerNestedVeto(){try(Fixture f=new Fixture()){f.auto();f.state(false);final Player a=new Player(false),b=new Player(false);a.onWrite=()->f.runtime.playerSprintControl(b);f.runtime.playerSprintControl(a);assertEquals(1,a.writes);assertEquals(0,b.writes);f.runtime.playerSprintControl(b);assertEquals(1,b.trueWrites);}}
    @Test void integratedAutoSprintWorldUnloadCannotBorrowSource(){try(Fixture f=new Fixture()){f.auto();f.state(false);final Player p=new Player(false);p.onWrite=()->f.runtime.playerSprintControl(null);f.runtime.playerSprintControl(p);assertEquals(1,p.trueWrites);}}
    @Test void integratedAutoSprintOffOnDuringSetterKeepsSingleWrite(){try(Fixture f=new Fixture()){f.auto();f.state(false);final Player p=new Player(false);p.onWrite=()->{f.controller.disable(Minecraft189AutoSprintModule.ID);f.controller.enable(Minecraft189AutoSprintModule.ID);f.runtime.playerSprintControl(p);};f.runtime.playerSprintControl(p);assertEquals(1,p.trueWrites);}}
    @Test void integratedWTapResetsActualSprintOnPhysicalAttackEdge(){try(Fixture f=new Fixture()){f.wtap();f.state(true);f.runtime.inputState().pointerButton(Minecraft189ClickRateTracker.LEFT_BUTTON,true);final Player p=new Player(true);f.runtime.playerSprintControl(p);assertEquals(1,p.falseWrites);assertFalse(p.sprint);}}
    @Test void integratedWTapSameSourceSetterReentryIsNotSecondReset(){try(Fixture f=new Fixture()){f.wtap();f.state(true);f.runtime.inputState().pointerButton(Minecraft189ClickRateTracker.LEFT_BUTTON,true);final Player p=new Player(true);p.onWrite=()->f.runtime.playerSprintControl(p);f.runtime.playerSprintControl(p);assertEquals(1,p.falseWrites);}}
    @Test void integratedWTapChangedPlayerCannotResetNestedSource(){try(Fixture f=new Fixture()){f.wtap();f.state(true);f.runtime.inputState().pointerButton(Minecraft189ClickRateTracker.LEFT_BUTTON,true);final Player a=new Player(true),b=new Player(true);a.onWrite=()->f.runtime.playerSprintControl(b);f.runtime.playerSprintControl(a);assertEquals(1,a.falseWrites);assertEquals(0,b.writes);}}
    @Test void integratedNativeExceptionReleasesHostForNewSource(){try(Fixture f=new Fixture()){f.auto();f.state(false);final Player a=new Player(false);a.fail=true;assertThrows(IllegalStateException.class,()->f.runtime.playerSprintControl(a));assertTrue(a.sprint);final Player b=new Player(false);f.runtime.playerSprintControl(b);assertEquals(1,b.trueWrites);}}
    @Test void integratedOriginalGuiVetoPreservesActualSprint(){try(Fixture f=new Fixture()){f.auto();f.state(false);f.runtime.key(LegacyKeyboardCodes.RIGHT_SHIFT,'\0',true,false,true,false,false);assertTrue(f.runtime.clickGuiRuntime().coreRuntime().model().snapshot().open());final Player a=new Player(false);f.runtime.playerSprintControl(a);assertEquals(0,a.writes);}}
    @Test void integratedMissingMovementSnapshotNeverInjectsSprint(){try(Fixture f=new Fixture()){f.auto();final Player a=new Player(false);f.runtime.playerSprintControl(a);assertEquals(0,a.writes);}}
    @Test void previousHostTickGuardCannotWriteAfterSameOriginalReenters() {
        final Minecraft189SprintHostSourceLease l = lease();
        final Player p = new Player(true);
        assertTrue(l.enter(p, false));
        final Minecraft189PlayerSprintControl stale =
                l.guardedPlayer(p, () -> false);
        l.exit();
        assertTrue(l.enter(p, false));
        stale.customMcSetSprinting(false);
        assertEquals(0, p.writes);
        final Minecraft189PlayerSprintControl fresh =
                l.guardedPlayer(p, () -> false);
        fresh.customMcSetSprinting(false);
        assertEquals(1, p.falseWrites);
        l.exit();
    }

    @Test void integratedWTapReentrantFalseAfterNativeWriteCannotBeUndoneByAutoSprint() {
        try (Fixture f = new Fixture()) {
            f.wtap();
            f.auto();
            f.runtime.featureCatalog().wTap().resetTicksSetting().set(3);
            f.runtime.inputState().pointerButton(
                    Minecraft189ClickRateTracker.LEFT_BUTTON, true);
            final Player p = new Player(true);
            f.state(true);
            f.runtime.playerSprintControl(p);
            assertEquals(1, p.falseWrites);
            assertEquals(0, p.trueWrites);

            // A later reset tick is still running, but the native sprint
            // getter now truthfully says false. WTap's original setter
            // synchronously OFF->ON reenters and makes apply return false.
            // Old behavior let AutoSprint write true from this same stale
            // false-sprint snapshot, undoing WTap inside one host callback.
            f.state(false);
            p.onWrite = () -> {
                f.controller.disable(Minecraft189WTapModule.ID);
                f.controller.enable(Minecraft189WTapModule.ID);
                f.runtime.playerSprintControl(p);
            };
            f.runtime.playerSprintControl(p);
            assertEquals(2, p.falseWrites);
            assertEquals(0, p.trueWrites);
            assertFalse(p.sprint);

            // In a later separate real host callback, WTap makes no
            // setter attempt and AutoSprint is still free to resume.
            f.runtime.playerSprintControl(p);
            assertEquals(1, p.trueWrites);
            assertTrue(p.sprint);
        }
    }

    @Test void integratedWTapNoOpAllowsAutoSprintSameCallback() {
        try (Fixture f = new Fixture()) {
            f.wtap();
            f.auto();
            final Player p = new Player(false);
            f.state(false);
            f.runtime.playerSprintControl(p);
            assertEquals(0, p.falseWrites);
            assertEquals(1, p.trueWrites);
        }
    }

    @Test void keepSprintRestoresOnlyOnceAfterActualVanillaSprintLoss() {
        try (Fixture f=new Fixture()) {
            f.keepSprint();
            final Player p=new Player(true);
            f.runtime.playerSprintControl(p);
            assertTrue(f.runtime.shouldKeepSprintAfterSyntheticClick(true));
            p.sprint=false; // Original vanilla controller ended sprint.
            f.runtime.restoreSprintAfterSyntheticClick();
            assertEquals(1,p.trueWrites);
            assertTrue(p.sprint);
            f.runtime.restoreSprintAfterSyntheticClick();
            assertEquals(1,p.trueWrites); // The native click cannot replay.
        }
    }
    @Test void keepSprintNoOpWhenVanillaAlreadyStillSprinting() {
        try (Fixture f=new Fixture()) {
            f.keepSprint();
            final Player p=new Player(true);
            f.runtime.playerSprintControl(p);
            assertTrue(f.runtime.shouldKeepSprintAfterSyntheticClick(true));
            f.runtime.restoreSprintAfterSyntheticClick();
            assertEquals(0,p.writes);
        }
    }
    @Test void keepSprintGetterOtherNativePlayerRevokesOldRestore() {
        try (Fixture f=new Fixture()) {
            f.keepSprint();
            final Player a=new Player(true),b=new Player(false);
            f.runtime.playerSprintControl(a);
            assertTrue(f.runtime.shouldKeepSprintAfterSyntheticClick(true));
            a.sprint=false;
            a.onRead=()->f.runtime.playerSprintControl(b);
            f.runtime.restoreSprintAfterSyntheticClick();
            assertEquals(0,a.writes);
            assertEquals(0,b.writes);
            f.runtime.restoreSprintAfterSyntheticClick();
            assertEquals(0,a.writes);
        }
    }
    @Test void keepSprintNestedOriginalGetterCannotMintAnotherGrant() {
        try (Fixture f=new Fixture()) {
            f.keepSprint();
            final Player p=new Player(true);
            f.runtime.playerSprintControl(p);
            assertTrue(f.runtime.shouldKeepSprintAfterSyntheticClick(true));
            p.sprint=false;
            p.onRead=()->assertFalse(
                    f.runtime.shouldKeepSprintAfterSyntheticClick(true));
            f.runtime.restoreSprintAfterSyntheticClick();
            assertEquals(0,p.trueWrites);
        }
    }
    @Test void keepSprintOriginalGetterGuiAndPolicyChangesCancelRestore() {
        try (Fixture f=new Fixture()) {
            f.keepSprint();
            final Player p=new Player(true);
            f.runtime.playerSprintControl(p);
            assertTrue(f.runtime.shouldKeepSprintAfterSyntheticClick(true));
            p.sprint=false;
            p.onRead=()->f.runtime.featureCatalog().keepSprint()
                    .requireForwardSetting().set(true);
            f.runtime.restoreSprintAfterSyntheticClick();
            assertEquals(0,p.writes);
        }
        try (Fixture f=new Fixture()) {
            f.keepSprint();
            final Player p=new Player(true);
            f.runtime.playerSprintControl(p);
            assertTrue(f.runtime.shouldKeepSprintAfterSyntheticClick(true));
            p.sprint=false;
            p.onRead=()->f.runtime.key(
                    LegacyKeyboardCodes.RIGHT_SHIFT,'\\0',true,false,true,false,false);
            f.runtime.restoreSprintAfterSyntheticClick();
            assertEquals(0,p.writes);
            assertTrue(f.runtime.clickGuiRuntime().coreRuntime()
                    .model().snapshot().open());
        }
    }
    @Test void keepSprintWTapActivationDuringOriginalGetterCancelsRestore() {
        try (Fixture f=new Fixture()) {
            f.keepSprint();
            final Player p=new Player(true);
            f.runtime.playerSprintControl(p);
            assertTrue(f.runtime.shouldKeepSprintAfterSyntheticClick(true));
            p.sprint=false;
            p.onRead=()->f.wtap();
            f.runtime.restoreSprintAfterSyntheticClick();
            assertEquals(0,p.writes);
        }
    }
    @Test void keepSprintThrowingNativeGetterConsumesGrantAndClearsLease() {
        try (Fixture f=new Fixture()) {
            f.keepSprint();
            final Player p=new Player(true);
            f.runtime.playerSprintControl(p);
            assertTrue(f.runtime.shouldKeepSprintAfterSyntheticClick(true));
            p.sprint=false;p.failRead=true;
            assertThrows(IllegalStateException.class,
                    ()->f.runtime.restoreSprintAfterSyntheticClick());
            p.failRead=false;
            f.runtime.restoreSprintAfterSyntheticClick();
            assertEquals(0,p.writes);
            p.sprint=true;
            assertTrue(f.runtime.shouldKeepSprintAfterSyntheticClick(true));
            p.sprint=false;
            f.runtime.restoreSprintAfterSyntheticClick();
            assertEquals(1,p.trueWrites);
        }
    }
    @Test void keepSprintNativeSetterReentryCannotWriteTwice() {
        try (Fixture f=new Fixture()) {
            f.keepSprint();
            final Player p=new Player(true);
            f.runtime.playerSprintControl(p);
            assertTrue(f.runtime.shouldKeepSprintAfterSyntheticClick(true));
            p.sprint=false;
            p.onWrite=()->f.runtime.restoreSprintAfterSyntheticClick();
            f.runtime.restoreSprintAfterSyntheticClick();
            assertEquals(1,p.trueWrites);
        }
    }

}
