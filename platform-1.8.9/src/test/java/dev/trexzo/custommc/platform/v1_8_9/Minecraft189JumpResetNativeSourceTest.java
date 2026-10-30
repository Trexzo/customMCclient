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

/** Genuine interface-shaped local player; not original Mojang game success. */
final class Minecraft189JumpResetNativeSourceTest {
    private static final class Player implements Minecraft189PlayerJumpControl,
            Minecraft189PlayerMovementStateAccess,
            Minecraft189PlayerHurtTimeAccess {
        int jumps;
        int hurt;
        boolean ground = true, sneak, sprint = true;
        Runnable onHurtRead, onGroundRead, onSneakRead, onSprintRead, onJump;
        @Override public int customMcHurtTime() {
            final Runnable cb=onHurtRead;onHurtRead=null;
            if(cb!=null)cb.run();
            return hurt;
        }
        @Override public boolean customMcOnGround() {
            final Runnable cb=onGroundRead;onGroundRead=null;
            if(cb!=null)cb.run();
            return ground;
        }
        @Override public boolean customMcSneaking() {
            final Runnable cb=onSneakRead;onSneakRead=null;
            if(cb!=null)cb.run();
            return sneak;
        }
        @Override public boolean customMcSprinting() {
            final Runnable cb=onSprintRead;onSprintRead=null;
            if(cb!=null)cb.run();
            return sprint;
        }
        @Override public void customMcJump() {
            jumps++;
            final Runnable cb=onJump;onJump=null;
            if(cb!=null)cb.run();
        }
    }
    private static final class Ui implements LegacyUiHostCallbacks {
        @Override public int framebufferWidth(){return 1280;}
        @Override public int framebufferHeight(){return 720;}
        @Override public float uiScale(){return 1F;}
        @Override public void beginUi(UiViewport v){}
        @Override public void fillRect(float a,float b,float c,float d,int e){}
        @Override public void fillRoundedRect(float a,float b,float c,float d,float e,int f){}
        @Override public void strokeRect(float a,float b,float c,float d,float e,int f){}
        @Override public void pushClip(float a,float b,float c,float d){}
        @Override public void popClip(){}
        @Override public void drawText(UiFontHandle f,float a,float b,String s,int c){}
        @Override public void endUi(){}
    }
    private static final class Fixture implements AutoCloseable {
        final ModuleRegistry modules=new ModuleRegistry();
        final ModuleController controller=new ModuleController(modules);
        final ServiceRegistry services=new ServiceRegistry();
        final SettingRegistry settings=new SettingRegistry();
        final Minecraft189HostRuntime host;
        final Player player=new Player();
        Fixture() {
            services.register(RenderPipeline.class,new RenderPipeline());
            final Minecraft189Platform platform=new Minecraft189Platform();
            platform.attach(new PlatformContext(
                    new EventBus(),modules,controller,services));
            host=Minecraft189HostRuntime.install(platform,
                    new ModulePresentationRegistry(),
                    new ModuleCategoryRegistry(),
                    new ModuleSettingRegistry(modules,settings),
                    null,null,settings,new SettingPresentationRegistry(),new Ui());
            controller.enable(Minecraft189JumpResetModule.ID);
            host.featureCatalog().jumpReset().cooldownSetting().set(0);
        }
        void source() {
            host.playerMovementState(player);
            host.playerHurtTime(player);
        }
        void tick(int currentHurt) {
            player.hurt=currentHurt;source();host.playerJumpControl(player);
        }
        @Override public void close(){host.close();}
    }
    @Test void realOwnerAndOneFreshHurtEdgeCallsOneNativeJump() {
        try(Fixture f=new Fixture()) {
            f.tick(0);
            f.tick(10);
            assertEquals(1,f.player.jumps);
            f.tick(10);
            assertEquals(1,f.player.jumps);
            f.tick(0);
            f.tick(10);
            assertEquals(2,f.player.jumps);
        }
    }
    @Test void realHurtChangedAfterSnapshotNeverDispatchesStaleJump() {
        try(Fixture f=new Fixture()) {
            f.tick(0);
            f.player.hurt=10;f.source();
            f.player.onGroundRead=()->f.player.hurt=0;
            f.host.playerJumpControl(f.player);
            assertEquals(0,f.player.jumps);
            f.tick(10);
            assertEquals(0,f.player.jumps); // no replay of old edge
            f.tick(0);f.tick(10);
            assertEquals(1,f.player.jumps);
        }
    }
    @Test void originalGroundSneakSprintTransitionDuringGetterVetoes() {
        try(Fixture f=new Fixture()) {
            f.tick(0);
            f.player.hurt=10;f.source();
            f.player.onHurtRead=()->f.player.ground=false;
            f.host.playerJumpControl(f.player);
            assertEquals(0,f.player.jumps);
        }
        try(Fixture f=new Fixture()) {
            f.tick(0);
            f.player.hurt=10;f.source();
            f.player.onHurtRead=()->f.player.sneak=true;
            f.host.playerJumpControl(f.player);
            assertEquals(0,f.player.jumps);
        }
        try(Fixture f=new Fixture()) {
            f.tick(0);
            f.host.featureCatalog().jumpReset().requireSprintSetting().set(true);
            f.player.hurt=10;f.source();
            f.player.onHurtRead=()->f.player.sprint=false;
            f.host.playerJumpControl(f.player);
            assertEquals(0,f.player.jumps);
        }
    }
    @Test void nestedSamePlayerOriginalHurtSourceRevokesOuterNativeAdmission() {
        try(Fixture f=new Fixture()) {
            f.tick(0);
            f.player.hurt=10;f.source();
            f.player.onHurtRead=()->f.host.playerHurtTime(f.player);
            f.host.playerJumpControl(f.player);
            assertEquals(0,f.player.jumps);
        }
    }
    @Test void nestedDifferentPlayerMovementCannotBorrowOriginalHurt() {
        try(Fixture f=new Fixture()) {
            f.tick(0);
            f.player.hurt=10;f.source();
            final Player other=new Player();
            f.player.onGroundRead=()->f.host.playerMovementState(other);
            f.host.playerJumpControl(f.player);
            assertEquals(0,f.player.jumps);
            assertEquals(0,other.jumps);
        }
    }
    @Test void realForwardPolicyAndOtherJumpOwnerStillWin() {
        try(Fixture f=new Fixture()) {
            f.tick(0);
            // Default Require Forward is true and original W not held.
            // A real local press is needed for the positive native jump.
            f.player.hurt=10;f.source();
            f.host.playerJumpControl(f.player);
            assertEquals(0,f.player.jumps);
            f.tick(0);
            f.host.key(LegacyKeyboardCodes.W,'w',true,false,false,false,false);
            f.tick(10);
            assertEquals(1,f.player.jumps);
        }
    }
    @Test void rejectedNativeGetterConsumesHurtButNotCooldown() {
        final Minecraft189JumpResetModule module=new Minecraft189JumpResetModule();
        final Minecraft189PlayerMovementState m=new Minecraft189PlayerMovementState();
        final Minecraft189PlayerHurtTimeState h=new Minecraft189PlayerHurtTimeState();
        final Player p=new Player();
        module.onEnable();module.cooldownSetting().set(9);
        m.update(true,false,true);
        h.update(0);
        assertFalse(module.apply(p,m.snapshot(),h.snapshot(),true,false,()->true));
        h.update(10);
        assertFalse(module.apply(p,m.snapshot(),h.snapshot(),true,false,()->false));
        assertEquals(0,p.jumps);
        h.update(0);
        module.apply(p,m.snapshot(),h.snapshot(),true,false,()->true);
        h.update(10);
        assertTrue(module.apply(p,m.snapshot(),h.snapshot(),true,false,()->true));
        assertEquals(1,p.jumps);
    }
}
