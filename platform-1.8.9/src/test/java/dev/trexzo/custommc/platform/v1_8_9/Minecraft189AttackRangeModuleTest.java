package dev.trexzo.custommc.platform.v1_8_9;
import dev.trexzo.custommc.core.module.*;
import dev.trexzo.custommc.core.setting.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
final class Minecraft189AttackRangeModuleTest{
    @Test void disabledTransparentEnabledFailClosed(){
        Minecraft189AttackRangeModule m=new Minecraft189AttackRangeModule();
        Minecraft189PlayerPositionState local=new Minecraft189PlayerPositionState();
        Minecraft189WorldEntityPositionState world=new Minecraft189WorldEntityPositionState();
        Minecraft189WorldEntityKindState kinds=new Minecraft189WorldEntityKindState();
        assertTrue(m.permits(-1,local.snapshot(),world.snapshot(),kinds.snapshot()));
        m.onEnable();
        assertFalse(m.permits(-1,local.snapshot(),world.snapshot(),kinds.snapshot()));
        local.update(0,0,0);
        world.update(new double[]{0,0,3,0,0,3.1,0,0,1});
        kinds.update(new int[]{3,3,7});
        assertTrue(m.permits(0,local.snapshot(),world.snapshot(),kinds.snapshot()));
        assertFalse(m.permits(1,local.snapshot(),world.snapshot(),kinds.snapshot()));
        assertFalse(m.permits(2,local.snapshot(),world.snapshot(),kinds.snapshot()));
        m.maxRangeSetting().set(3.2);
        assertTrue(m.permits(1,local.snapshot(),world.snapshot(),kinds.snapshot()));
        assertFalse(m.permits(4,local.snapshot(),world.snapshot(),kinds.snapshot()));
        assertThrows(IllegalArgumentException.class,()->m.maxRangeSetting().set(7D));
        kinds.clear();
        assertFalse(m.permits(0,local.snapshot(),world.snapshot(),kinds.snapshot()));
        m.onDisable();
        assertTrue(m.permits(-1,local.snapshot(),world.snapshot(),kinds.snapshot()));
    }
    @Test void registrationAndTeardown(){
        ModuleRegistry modules=new ModuleRegistry();
        ModuleController controller=new ModuleController(modules);
        SettingRegistry settings=new SettingRegistry();
        Minecraft189AttackRangeFeature f=Minecraft189AttackRangeFeature.install(
            modules,controller,new ModulePresentationRegistry(),
            new ModuleSettingRegistry(modules,settings),settings,new SettingPresentationRegistry());
        try{f.module().maxRangeSetting().set(4D);
            assertEquals("4.0",settings.snapshotEncoded().get(Minecraft189AttackRangeModule.MAX_RANGE));
            controller.enable(Minecraft189AttackRangeModule.ID);
        }finally{f.close();f.close();}
        assertNull(modules.find(Minecraft189AttackRangeModule.ID));
        assertNull(settings.find(Minecraft189AttackRangeModule.MAX_RANGE));
    }
}
