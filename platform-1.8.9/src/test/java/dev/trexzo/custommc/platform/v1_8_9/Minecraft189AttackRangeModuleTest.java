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

    @Test void optionalNativeHitboxGateUsesExactBoundsWithoutRaycastExpansion(){
        final Minecraft189AttackRangeModule gate = new Minecraft189AttackRangeModule();
        final Minecraft189PlayerPositionState player = new Minecraft189PlayerPositionState();
        final Minecraft189WorldEntityPositionState world = new Minecraft189WorldEntityPositionState();
        final Minecraft189WorldEntityKindState kinds = new Minecraft189WorldEntityKindState();
        player.update(0, 0, 0);
        world.update(new double[]{0, 0, 3.5, 0, 0, 8});
        kinds.update(new int[]{3, 3});
        gate.onEnable();
        try {
            gate.maxRangeSetting().set(3.0D);
            assertFalse(gate.permits(0, player.snapshot(), world.snapshot(), kinds.snapshot()));
            gate.useNativeHitboxSetting().set(true);
            assertTrue(gate.needsNativeHitbox());
            assertFalse(gate.permits(0, player.snapshot(), world.snapshot(), kinds.snapshot()));
            assertTrue(gate.permits(0, player.snapshot(), world.snapshot(), kinds.snapshot(),
                    new double[]{-0.3, 0, 2.75, 0.3, 1.8, 4.0}));
            assertFalse(gate.permits(1, player.snapshot(), world.snapshot(), kinds.snapshot(),
                    new double[]{-0.3, 0, 7.7, 0.3, 1.8, 8.3}));
            assertFalse(gate.permits(0, player.snapshot(), world.snapshot(), kinds.snapshot(),
                    new double[]{0, 0, 1, 1, 1}));
            assertFalse(gate.permits(0, player.snapshot(), world.snapshot(), kinds.snapshot(),
                    new double[]{0, 0, 1, Double.NaN, 1, 2}));
            assertFalse(gate.permits(0, player.snapshot(), world.snapshot(), kinds.snapshot(),
                    new double[]{2, 0, 1, 1, 1, 2}));
            gate.useNativeHitboxSetting().set(false);
            assertFalse(gate.needsNativeHitbox());
            assertFalse(gate.permits(0, player.snapshot(), world.snapshot(), kinds.snapshot()));
        } finally { gate.onDisable(); }
        assertFalse(gate.needsNativeHitbox());
    }

    @Test void registrationAndTeardown(){
        ModuleRegistry modules=new ModuleRegistry();
        ModuleController controller=new ModuleController(modules);
        SettingRegistry settings=new SettingRegistry();
        Minecraft189AttackRangeFeature f=Minecraft189AttackRangeFeature.install(
            modules,controller,new ModulePresentationRegistry(),
            new ModuleSettingRegistry(modules,settings),settings,new SettingPresentationRegistry());
        try{f.module().maxRangeSetting().set(4D);
            f.module().useNativeHitboxSetting().set(true);
            assertEquals("true",settings.snapshotEncoded().get(
                    Minecraft189AttackRangeModule.USE_NATIVE_HITBOX));
            assertEquals("4.0",settings.snapshotEncoded().get(Minecraft189AttackRangeModule.MAX_RANGE));
            controller.enable(Minecraft189AttackRangeModule.ID);
        }finally{f.close();f.close();}
        assertNull(modules.find(Minecraft189AttackRangeModule.ID));
        assertNull(settings.find(Minecraft189AttackRangeModule.MAX_RANGE));
    }
}
