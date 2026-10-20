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

final class Minecraft189OriginalAutoBlockSwordSourceTest {
    static final class Sword implements Minecraft189ItemStackAccess {
        boolean sword=true;
        int reads;
        Runnable duringSword;
        boolean runtimeFailure, errorFailure;
        @Override public String customMcDisplayName(){return "Original Item";}
        @Override public int customMcStackSize(){return 1;}
        @Override public int customMcItemDamage(){return 0;}
        @Override public int customMcMaxDamage(){return 0;}
        @Override public boolean customMcIsSword() {
            reads++;
            Runnable callback=duringSword; duringSword=null;
            if(callback!=null)callback.run();
            if(runtimeFailure)throw new IllegalStateException("actual sword getter");
            if(errorFailure)throw new AssertionError("actual sword getter");
            return sword;
        }
    }
    static final class Player implements Minecraft189PlayerHeldItemAccess {
        Sword sword=new Sword();
        int reads;
        Runnable duringHeld;
        boolean runtimeFailure, errorFailure;
        @Override public Minecraft189ItemStackAccess customMcHeldItem() {
            reads++;
            Runnable callback=duringHeld; duringHeld=null;
            if(callback!=null)callback.run();
            if(runtimeFailure)throw new IllegalStateException("actual held item");
            if(errorFailure)throw new AssertionError("actual held item");
            return sword;
        }
    }
    static final class Fixture implements AutoCloseable {
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
                    new ModuleSettingRegistry(modules,settings),null,null,
                    settings,new SettingPresentationRegistry(),new NoOpHost());
            controller.enable(Minecraft189AutoBlockModule.ID);
        }
        void original(Player p) {runtime.playerHeldItem(p);}
        boolean nativeSwordUse() {
            long token=runtime.beginAutoBlockNativeUse();
            assertTrue(token>0,"original enabled AutoBlock must mint lease");
            runtime.recordAutoBlockStart(token);
            return runtime.featureCatalog().autoBlock().ownsBlock();
        }
        boolean available(){return runtime.heldItemState().snapshot().available();}
        @Override public void close(){runtime.close();}
    }
    static final class NoOpHost implements LegacyUiHostCallbacks{
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
    @Test void verifiedOriginalSwordAfterTwoActualGettersCommitsUse() {
        try(Fixture f=new Fixture()){
            Player player=new Player();
            f.original(player);
            assertTrue(f.available());
            assertTrue(f.nativeSwordUse());
            assertEquals(2,player.reads);
            assertEquals(1,player.sword.reads);
        }
    }
    @Test void originalNonSwordCannotCommitNativeBlock() {
        try(Fixture f=new Fixture()){
            Player player=new Player();player.sword.sword=false;
            f.original(player);
            assertFalse(f.nativeSwordUse());
            assertTrue(f.available());
        }
    }
    @Test void actualEmptyHandRemainsValidButCannotBlock() {
        try(Fixture f=new Fixture()){
            Player player=new Player();player.sword=null;
            f.original(player);
            assertFalse(f.available());
            assertFalse(f.nativeSwordUse());
            assertEquals(2,player.reads);
        }
    }
    @Test void sameOriginalNestedHeldReadDoesNotDoubleSpend() {
        try(Fixture f=new Fixture()){
            Player player=new Player();f.original(player);
            player.duringHeld=()->f.runtime.playerHeldItem(player);
            assertTrue(f.nativeSwordUse());
            assertEquals(2,player.reads);
            assertEquals(1,player.sword.reads);
        }
    }
    @Test void sameOriginalNestedSwordTypeDoesNotDoubleSpend() {
        try(Fixture f=new Fixture()){
            Player player=new Player();f.original(player);
            player.sword.duringSword=()->f.runtime.playerHeldItem(player);
            assertTrue(f.nativeSwordUse());
            assertEquals(2,player.reads);
            assertEquals(1,player.sword.reads);
        }
    }
    @Test void changedOriginalDuringHeldGetterVetoesBlockAndClearsOldOwner() {
        try(Fixture f=new Fixture()){
            Player old=new Player(),next=new Player();f.original(old);
            old.duringHeld=()->f.runtime.playerHeldItem(next);
            assertFalse(f.nativeSwordUse());
            assertFalse(f.available());
            assertEquals(0,next.reads);
            assertEquals(0,old.sword.reads);
        }
    }
    @Test void changedOriginalDuringSwordTypeVetoesEvenIfOldItemIsSword() {
        try(Fixture f=new Fixture()){
            Player old=new Player(),next=new Player();f.original(old);
            old.sword.duringSword=()->f.runtime.playerHeldItem(next);
            assertFalse(f.nativeSwordUse());
            assertFalse(f.available());
            assertEquals(0,next.reads);
            assertEquals(1,old.sword.reads);
        }
    }
    @Test void nullOriginalDuringHeldGetterRevokesNativeBlock() {
        try(Fixture f=new Fixture()){
            Player old=new Player();f.original(old);
            old.duringHeld=()->f.runtime.playerHeldItem(null);
            assertFalse(f.nativeSwordUse());
            assertFalse(f.available());
        }
    }
    @Test void nullOriginalDuringSwordTypeRevokesNativeBlock() {
        try(Fixture f=new Fixture()){
            Player old=new Player();f.original(old);
            old.sword.duringSword=()->f.runtime.playerHeldItem(null);
            assertFalse(f.nativeSwordUse());
            assertFalse(f.available());
        }
    }
    @Test void newPositionTickDuringHeldGetterVetoesFormerSwordOwner() {
        try(Fixture f=new Fixture()){
            Player old=new Player();f.original(old);
            old.duringHeld=()->f.runtime.playerPosition(null);
            assertFalse(f.nativeSwordUse());
        }
    }
    @Test void newPositionTickDuringSwordTypeVetoesFormerSwordOwner() {
        try(Fixture f=new Fixture()){
            Player old=new Player();f.original(old);
            old.sword.duringSword=()->f.runtime.playerPosition(null);
            assertFalse(f.nativeSwordUse());
        }
    }
    @Test void originalHeldGetterRuntimeFailureRevokesAndRethrows() {
        try(Fixture f=new Fixture()){
            Player old=new Player();f.original(old);
            old.runtimeFailure=true;
            assertThrows(IllegalStateException.class,()->f.nativeSwordUse());
            assertFalse(f.available());
        }
    }
    @Test void originalHeldGetterErrorRevokesAndRethrows() {
        try(Fixture f=new Fixture()){
            Player old=new Player();f.original(old);
            old.errorFailure=true;
            assertThrows(AssertionError.class,()->f.nativeSwordUse());
            assertFalse(f.available());
        }
    }
    @Test void originalSwordGetterRuntimeFailureRevokesAndRethrows() {
        try(Fixture f=new Fixture()){
            Player old=new Player();f.original(old);
            old.sword.runtimeFailure=true;
            assertThrows(IllegalStateException.class,()->f.nativeSwordUse());
            assertFalse(f.available());
        }
    }
    @Test void originalSwordGetterErrorRevokesAndRethrows() {
        try(Fixture f=new Fixture()){
            Player old=new Player();f.original(old);
            old.sword.errorFailure=true;
            assertThrows(AssertionError.class,()->f.nativeSwordUse());
            assertFalse(f.available());
        }
    }
    @Test void verifiedFreshPlayerRecoversAfterRevokedOriginalSword() {
        try(Fixture f=new Fixture()){
            Player old=new Player(),newPlayer=new Player();f.original(old);
            old.duringHeld=()->f.runtime.playerHeldItem(newPlayer);
            assertFalse(f.nativeSwordUse());
            f.original(newPlayer);
            assertTrue(f.available());
            assertTrue(f.nativeSwordUse());
        }
    }
    @Test void verifiedEmptyHandDoesNotBorrowPriorSword() {
        try(Fixture f=new Fixture()){
            Player first=new Player(),empty=new Player();
            f.original(first);
            f.original(empty);
            empty.sword=null;
            assertFalse(f.nativeSwordUse());
            assertFalse(f.available());
        }
    }
    @Test void nextOriginalPositionTickInvalidatesExistingSwordAdmission() {
        try(Fixture f=new Fixture()){
            Player original=new Player();f.original(original);
            f.runtime.playerPosition(null);
            assertFalse(f.nativeSwordUse());
        }
    }
}
