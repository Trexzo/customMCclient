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

/**
 * Deterministic host final-gate tests. Reflection seeds a scheduler grant
 * only inside this unit fixture; this is NOT original Mojang controller or
 * dedicated-server damage acceptance, which remains a separate CI gate.
 */
final class Minecraft189CriticalsNativeDispatchOwnerTest {
    private static final class Item implements Minecraft189ItemStackAccess {
        @Override public String customMcDisplayName(){return "Original sword";}
        @Override public int customMcStackSize(){return 1;}
        @Override public int customMcItemDamage(){return 0;}
        @Override public int customMcMaxDamage(){return 100;}
    }
    private static final class Inventory implements Minecraft189InventoryHotbarControl {
        int slot=0;
        @Override public int customMcSelectedHotbarSlot(){return slot;}
        @Override public void customMcSetSelectedHotbarSlot(int s){slot=s;}
    }
    private static final class Player implements Minecraft189PlayerInventoryAccess,
            Minecraft189PlayerHeldItemAccess, Minecraft189PlayerMovementStateAccess,
            Minecraft189PlayerFallDistanceControl, Minecraft189PlayerMotionControl {
        final Inventory inv=new Inventory();
        final Item item=new Item();
        boolean ground=false;
        float fall=.5F;
        double motionY=-.3D, motionX, motionZ;
        Runnable onGroundRead, onFallRead, onMotionRead;
        private static void fire(Runnable r){if(r!=null)r.run();}
        @Override public Minecraft189InventoryHotbarAccess customMcInventory(){return inv;}
        @Override public Minecraft189ItemStackAccess customMcHeldItem(){return item;}
        @Override public boolean customMcOnGround(){
            final Runnable r=onGroundRead;onGroundRead=null;fire(r);return ground;
        }
        @Override public boolean customMcSneaking(){return false;}
        @Override public boolean customMcSprinting(){return false;}
        @Override public float customMcFallDistance(){
            final Runnable r=onFallRead;onFallRead=null;fire(r);return fall;
        }
        @Override public void customMcSetFallDistance(float v){fall=v;}
        @Override public double customMcMotionX(){return motionX;}
        @Override public void customMcSetMotionX(double v){motionX=v;}
        @Override public double customMcMotionY(){
            final Runnable r=onMotionRead;onMotionRead=null;fire(r);return motionY;
        }
        @Override public void customMcSetMotionY(double v){motionY=v;}
        @Override public double customMcMotionZ(){return motionZ;}
        @Override public void customMcSetMotionZ(double v){motionZ=v;}
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
        Fixture(boolean criticalsEnabled) {
            services.register(RenderPipeline.class,new RenderPipeline());
            final Minecraft189Platform platform=new Minecraft189Platform();
            platform.attach(new PlatformContext(
                    new EventBus(),modules,controller,services));
            host=Minecraft189HostRuntime.install(platform,
                    new ModulePresentationRegistry(),
                    new ModuleCategoryRegistry(),
                    new ModuleSettingRegistry(modules,settings),
                    null,null,settings,new SettingPresentationRegistry(),new Ui());
            if(criticalsEnabled)controller.enable(Minecraft189CriticalsModule.ID);
            host.playerHotbarSlot(player);
            host.playerHeldItem(player);
            arm();
        }
        private static Object read(Object obj,String name) {
            try {
                final Field f=Minecraft189HostRuntime.class.getDeclaredField(name);
                f.setAccessible(true);
                return f.get(obj);
            } catch(ReflectiveOperationException e){throw new AssertionError(e);}
        }
        private void put(String name,Object value) {
            try {
                final Field f=Minecraft189HostRuntime.class.getDeclaredField(name);
                f.setAccessible(true);f.set(host,value);
            } catch(ReflectiveOperationException e){throw new AssertionError(e);}
        }
        void arm() {
            // This supplies a deterministic unit-test owner grant only.
            final Minecraft189CriticalsModule module=host.featureCatalog().criticals();
            put("triggerDispatchAdmissionSerial",17L);
            put("criticalsNativeAdmissionSerial",17L);
            put("criticalsNativeSourceGeneration",read(host,"criticalsSourceGeneration"));
            put("criticalsNativeLifecycleEpoch",module.lifecycleEpoch());
            put("criticalsNativeMinimumFall",module.minimumFallDistanceSetting().get());
            put("criticalsNativeRequireDescending",module.requireDescendingSetting().get());
            put("criticalsNativeActive",module.active());
            put("criticalsNativePlayer",player);
        }
        @Override public void close(){host.close();}
    }

    @Test void realSourceAirborneDescentAllowsOriginalFinalGate() {
        try(Fixture f=new Fixture(true)){
            assertTrue(f.host.mayDispatchOriginalCriticals());
        }
    }
    @Test void realSourceLandingOrFallResetBlocksNativeCritical() {
        try(Fixture f=new Fixture(true)){
            f.player.ground=true;
            assertFalse(f.host.mayDispatchOriginalCriticals());
            f.player.ground=false;f.player.fall=0F;
            assertFalse(f.host.mayDispatchOriginalCriticals());
            f.player.fall=.5F;f.player.motionY=.2D;
            assertFalse(f.host.mayDispatchOriginalCriticals());
        }
    }
    @Test void originalOnGroundTransitionDuringNativeGetterBlocks() {
        try(Fixture f=new Fixture(true)){
            f.player.onFallRead=()->f.player.ground=true;
            assertFalse(f.host.mayDispatchOriginalCriticals());
        }
    }
    @Test void originalFallDistanceMutationBetweenNativeReadbacksBlocks() {
        try(Fixture f=new Fixture(true)){
            f.player.onMotionRead=()->f.player.fall=1F;
            assertFalse(f.host.mayDispatchOriginalCriticals());
        }
    }
    @Test void nestedSamePlayerHeldCallbackRevokesOriginalNativeGrant() {
        try(Fixture f=new Fixture(true)){
            f.player.onFallRead=()->f.host.playerHeldItem(f.player);
            assertFalse(f.host.mayDispatchOriginalCriticals());
        }
    }
    @Test void newOriginalPlayerHotbarCallbackRevokesOldNativeGrant() {
        try(Fixture f=new Fixture(true)){
            final Player other=new Player();
            f.player.onMotionRead=()->{
                f.host.playerHotbarSlot(other);
                f.host.playerHeldItem(other);
            };
            assertFalse(f.host.mayDispatchOriginalCriticals());
        }
    }
    @Test void criticalsDisableEnableOrPolicyEditCannotBorrowOlderAdmission() {
        try(Fixture f=new Fixture(true)){
            f.player.onGroundRead=()->{
                f.controller.disable(Minecraft189CriticalsModule.ID);
                f.controller.enable(Minecraft189CriticalsModule.ID);
            };
            assertFalse(f.host.mayDispatchOriginalCriticals());
        }
        try(Fixture f=new Fixture(true)){
            f.player.onFallRead=()->f.host.featureCatalog().criticals()
                    .minimumFallDistanceSetting().set(1D);
            assertFalse(f.host.mayDispatchOriginalCriticals());
        }
    }
    @Test void disabledDefaultAllowsNativeClickButOffOnTransitionNeverBorrowsIt() {
        try(Fixture f=new Fixture(false)){
            assertTrue(f.host.mayDispatchOriginalCriticals());
            f.controller.enable(Minecraft189CriticalsModule.ID);
            assertFalse(f.host.mayDispatchOriginalCriticals());
        }
    }
    @Test void competingMovementModuleVetoesOriginalCriticals() {
        try(Fixture f=new Fixture(true)){
            f.controller.enable(Minecraft189NoFallModule.ID);
            assertFalse(f.host.mayDispatchOriginalCriticals());
        }
    }
}
