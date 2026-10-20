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

/** Original mapped ground/sneak/sprint adapter; native Mojang proof is separate. */
final class Minecraft189MovementSnapshotSourceLeaseTest {
    private static final class OriginalPlayer
            implements Minecraft189PlayerMovementStateAccess {
        boolean ground, sneak, sprint;
        int groundReads, sneakReads, sprintReads;
        Runnable onGround, onSneak, onSprint;
        boolean throwGround, throwSneak, throwSprint;
        OriginalPlayer(boolean ground, boolean sneak, boolean sprint) {
            this.ground=ground;this.sneak=sneak;this.sprint=sprint;
        }
        @Override public boolean customMcOnGround() {
            groundReads++;
            if (groundReads > 8) throw new AssertionError("recursive source ground");
            final Runnable callback=onGround;
            onGround=null;
            if(callback!=null)callback.run();
            if(throwGround)throw new IllegalStateException("original ground getter");
            return ground;
        }
        @Override public boolean customMcSneaking() {
            sneakReads++;
            if (sneakReads > 8) throw new AssertionError("recursive source sneak");
            final Runnable callback=onSneak;
            onSneak=null;
            if(callback!=null)callback.run();
            if(throwSneak)throw new IllegalStateException("original sneak getter");
            return sneak;
        }
        @Override public boolean customMcSprinting() {
            sprintReads++;
            if (sprintReads > 8) throw new AssertionError("recursive source sprint");
            final Runnable callback=onSprint;
            onSprint=null;
            if(callback!=null)callback.run();
            if(throwSprint)throw new IllegalStateException("original sprint getter");
            return sprint;
        }
        int reads(){return groundReads+sneakReads+sprintReads;}
    }
    @Test void sourceLeasePinsOneOriginalPlayerUntilExit() {
        final Minecraft189MovementSnapshotSourceLease l=new Minecraft189MovementSnapshotSourceLease();
        final OriginalPlayer p=new OriginalPlayer(true,false,false);
        assertTrue(l.enter(p));assertTrue(l.stillOwned());
        assertFalse(l.enter(p));assertTrue(l.stillOwned());
        l.exit();assertFalse(l.stillOwned());
        assertTrue(l.enter(p));l.exit();
    }
    @Test void changedOriginalSourceIrrevocablyRevokesMovementSnapshot() {
        final Minecraft189MovementSnapshotSourceLease l=new Minecraft189MovementSnapshotSourceLease();
        final OriginalPlayer a=new OriginalPlayer(true,false,false);
        final OriginalPlayer b=new OriginalPlayer(false,false,true);
        assertTrue(l.enter(a));assertFalse(l.enter(b));
        assertFalse(l.stillOwned());assertFalse(l.enter(a));
        assertFalse(l.stillOwned());l.exit();
        assertTrue(l.enter(b));l.exit();
    }
    @Test void nestedNullOriginalPlayerRevokesFormerMovementOwner() {
        final Minecraft189MovementSnapshotSourceLease l=new Minecraft189MovementSnapshotSourceLease();
        assertTrue(l.enter(new OriginalPlayer(true,false,false)));
        assertFalse(l.enter(null));assertFalse(l.stillOwned());l.exit();
    }
    @Test void initialNullOriginalPlayerNeverCreatesFakeGroundEvidence() {
        final Minecraft189MovementSnapshotSourceLease l=new Minecraft189MovementSnapshotSourceLease();
        assertTrue(l.enter(null));
        assertFalse(l.enter(new OriginalPlayer(true,false,false)));
        assertFalse(l.stillOwned());l.exit();
    }
    @Test void sourceLeaseUnwindsAfterNativeGetterException() {
        final Minecraft189MovementSnapshotSourceLease l=new Minecraft189MovementSnapshotSourceLease();
        final OriginalPlayer a=new OriginalPlayer(true,false,false);
        assertTrue(l.enter(a));
        try {throw new IllegalStateException("mapped original sprint getter");}
        catch(IllegalStateException expected) {
            assertEquals("mapped original sprint getter",expected.getMessage());
        } finally {l.exit();}
        assertTrue(l.enter(new OriginalPlayer(false,true,false)));l.exit();
    }

    private static final class Fixture implements AutoCloseable {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final ServiceRegistry services = new ServiceRegistry();
        final Minecraft189HostRuntime runtime;
        Fixture() {
            services.register(RenderPipeline.class,new RenderPipeline());
            final Minecraft189Platform platform=new Minecraft189Platform();
            platform.attach(new PlatformContext(new EventBus(),modules,controller,services));
            runtime=Minecraft189HostRuntime.install(platform,
                    new ModulePresentationRegistry(),new ModuleCategoryRegistry(),
                    new ModuleSettingRegistry(modules,settings),
                    null,null,settings,new SettingPresentationRegistry(),new NoOpHost());
        }
        Minecraft189PlayerMovementState.Snapshot snapshot() {
            return runtime.playerMovementState().snapshot();
        }
        @Override public void close(){runtime.close();}
    }
    @Test void integratedHostCommitsAllThreeActualOriginalFlags() {
        try(Fixture f=new Fixture()) {
            final OriginalPlayer a=new OriginalPlayer(true,true,false);
            f.runtime.playerMovementState(a);
            assertTrue(f.snapshot().available());
            assertTrue(f.snapshot().onGround());
            assertTrue(f.snapshot().sneaking());
            assertFalse(f.snapshot().sprinting());
            assertEquals(3,a.reads());
        }
    }
    @Test void originalAirborneSprintingWithoutSneakIsPublishedPrecisely() {
        try(Fixture f=new Fixture()) {
            final OriginalPlayer a=new OriginalPlayer(false,false,true);
            f.runtime.playerMovementState(a);
            assertTrue(f.snapshot().available());
            assertFalse(f.snapshot().onGround());
            assertFalse(f.snapshot().sneaking());
            assertTrue(f.snapshot().sprinting());
        }
    }
    @Test void reentrantSamePlayerDuringGroundGetterNeverDoubleSamples() {
        try(Fixture f=new Fixture()) {
            final OriginalPlayer a=new OriginalPlayer(true,false,true);
            a.onGround=()->f.runtime.playerMovementState(a);
            f.runtime.playerMovementState(a);
            assertTrue(f.snapshot().available());
            assertEquals(1,a.groundReads);
            assertEquals(1,a.sneakReads);
            assertEquals(1,a.sprintReads);
        }
    }
    @Test void reentrantSamePlayerDuringSneakGetterNeverDoubleSamples() {
        try(Fixture f=new Fixture()) {
            final OriginalPlayer a=new OriginalPlayer(true,true,false);
            a.onSneak=()->f.runtime.playerMovementState(a);
            f.runtime.playerMovementState(a);
            assertTrue(f.snapshot().available());
            assertEquals(3,a.reads());
        }
    }
    @Test void reentrantSamePlayerDuringSprintGetterNeverDoubleSamples() {
        try(Fixture f=new Fixture()) {
            final OriginalPlayer a=new OriginalPlayer(false,false,true);
            a.onSprint=()->f.runtime.playerMovementState(a);
            f.runtime.playerMovementState(a);
            assertTrue(f.snapshot().available());assertEquals(3,a.reads());
        }
    }
    @Test void changedPlayerInsideGroundGetterClearsWithoutReadingRemainingFlags() {
        try(Fixture f=new Fixture()) {
            final OriginalPlayer a=new OriginalPlayer(true,false,false);
            final OriginalPlayer b=new OriginalPlayer(false,true,true);
            a.onGround=()->f.runtime.playerMovementState(b);
            f.runtime.playerMovementState(a);
            assertFalse(f.snapshot().available());assertEquals(1,a.groundReads);
            assertEquals(0,a.sneakReads+a.sprintReads+b.reads());
        }
    }
    @Test void changedPlayerInsideSneakGetterDiscardsFormerGroundObservation() {
        try(Fixture f=new Fixture()) {
            final OriginalPlayer a=new OriginalPlayer(true,false,false);
            final OriginalPlayer b=new OriginalPlayer(false,true,true);
            a.onSneak=()->f.runtime.playerMovementState(b);
            f.runtime.playerMovementState(a);
            assertFalse(f.snapshot().available());assertEquals(2,a.reads());
            assertEquals(0,a.sprintReads+b.reads());
        }
    }
    @Test void changedPlayerInsideSprintGetterDiscardsAllOldFlags() {
        try(Fixture f=new Fixture()) {
            final OriginalPlayer a=new OriginalPlayer(true,false,false);
            final OriginalPlayer b=new OriginalPlayer(false,true,true);
            a.onSprint=()->f.runtime.playerMovementState(b);
            f.runtime.playerMovementState(a);
            assertFalse(f.snapshot().available());
            assertEquals(3,a.reads());assertEquals(0,b.reads());
        }
    }
    @Test void nestedWorldUnloadNullInsideGroundGetterClearsSnapshot() {
        try(Fixture f=new Fixture()) {
            final OriginalPlayer a=new OriginalPlayer(true,true,true);
            a.onGround=()->f.runtime.playerMovementState(null);
            f.runtime.playerMovementState(a);
            assertFalse(f.snapshot().available());assertEquals(1,a.reads());
        }
    }
    @Test void nestedWorldUnloadNullInsideSprintGetterClearsSnapshot() {
        try(Fixture f=new Fixture()) {
            final OriginalPlayer a=new OriginalPlayer(true,false,true);
            a.onSprint=()->f.runtime.playerMovementState(null);
            f.runtime.playerMovementState(a);
            assertFalse(f.snapshot().available());assertEquals(3,a.reads());
        }
    }
    @Test void stalePriorGroundedSprintStateIsClearedOnReplacement() {
        try(Fixture f=new Fixture()) {
            final OriginalPlayer a=new OriginalPlayer(true,false,true);
            f.runtime.playerMovementState(a);
            assertTrue(f.snapshot().available());
            final OriginalPlayer b=new OriginalPlayer(false,true,false);
            b.onGround=()->f.runtime.playerMovementState(a);
            f.runtime.playerMovementState(b);
            assertFalse(f.snapshot().available());
            assertFalse(f.snapshot().onGround());
            assertFalse(f.snapshot().sneaking());
            assertFalse(f.snapshot().sprinting());
        }
    }
    @Test void genuineReplacementPlayerNextRealTickGetsOwnExactEvidence() {
        try(Fixture f=new Fixture()) {
            final OriginalPlayer a=new OriginalPlayer(true,false,true);
            final OriginalPlayer b=new OriginalPlayer(false,true,false);
            a.onSneak=()->f.runtime.playerMovementState(b);
            f.runtime.playerMovementState(a);
            assertFalse(f.snapshot().available());
            f.runtime.playerMovementState(b);
            assertTrue(f.snapshot().available());
            assertFalse(f.snapshot().onGround());
            assertTrue(f.snapshot().sneaking());
            assertFalse(f.snapshot().sprinting());
            assertEquals(3,b.reads());
        }
    }
    @Test void actualNullOuterPlayerClearsFormerSnapshot() {
        try(Fixture f=new Fixture()) {
            f.runtime.playerMovementState(new OriginalPlayer(true,false,true));
            assertTrue(f.snapshot().available());
            f.runtime.playerMovementState(null);
            assertFalse(f.snapshot().available());
            assertFalse(f.snapshot().onGround());
        }
    }
    @Test void originalGroundGetterThrowClearsOldEvidenceAndPropagates() {
        try(Fixture f=new Fixture()) {
            f.runtime.playerMovementState(new OriginalPlayer(true,true,true));
            final OriginalPlayer bad=new OriginalPlayer(false,false,false);
            bad.throwGround=true;
            assertThrows(IllegalStateException.class,
                    ()->f.runtime.playerMovementState(bad));
            assertFalse(f.snapshot().available());
            assertEquals(1,bad.reads());
        }
    }
    @Test void originalSneakGetterThrowClearsOldEvidenceAndPropagates() {
        try(Fixture f=new Fixture()) {
            f.runtime.playerMovementState(new OriginalPlayer(true,true,true));
            final OriginalPlayer bad=new OriginalPlayer(false,false,false);
            bad.throwSneak=true;
            assertThrows(IllegalStateException.class,
                    ()->f.runtime.playerMovementState(bad));
            assertFalse(f.snapshot().available());assertEquals(2,bad.reads());
        }
    }
    @Test void originalSprintGetterThrowClearsOldEvidenceAndPropagates() {
        try(Fixture f=new Fixture()) {
            f.runtime.playerMovementState(new OriginalPlayer(true,true,true));
            final OriginalPlayer bad=new OriginalPlayer(false,false,false);
            bad.throwSprint=true;
            assertThrows(IllegalStateException.class,
                    ()->f.runtime.playerMovementState(bad));
            assertFalse(f.snapshot().available());assertEquals(3,bad.reads());
        }
    }
    @Test void actualNativeGetterThrowDoesNotPermanentlyLockNextPlayer() {
        try(Fixture f=new Fixture()) {
            final OriginalPlayer bad=new OriginalPlayer(true,false,false);
            bad.throwSneak=true;
            assertThrows(IllegalStateException.class,
                    ()->f.runtime.playerMovementState(bad));
            final OriginalPlayer valid=new OriginalPlayer(false,false,true);
            f.runtime.playerMovementState(valid);
            assertTrue(f.snapshot().available());
            assertTrue(f.snapshot().sprinting());assertEquals(3,valid.reads());
        }
    }
    @Test void rapidOriginalGroundAndSprintTransitionsStayAtomic() {
        try(Fixture f=new Fixture()) {
            final OriginalPlayer p=new OriginalPlayer(true,false,false);
            f.runtime.playerMovementState(p);
            assertTrue(f.snapshot().onGround());assertFalse(f.snapshot().sprinting());
            p.ground=false;p.sprint=true;
            f.runtime.playerMovementState(p);
            assertFalse(f.snapshot().onGround());assertTrue(f.snapshot().sprinting());
            assertEquals(6,p.reads());
        }
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
}
