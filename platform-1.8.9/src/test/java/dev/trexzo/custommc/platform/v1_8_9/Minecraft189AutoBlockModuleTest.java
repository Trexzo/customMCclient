package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189AutoBlockModuleTest {
    @Test void swordOnlyNativeHoldExpiresAndIsReleasedBeforeActions() {
        final Minecraft189AutoBlockModule module = new Minecraft189AutoBlockModule();
        assertFalse(module.mayStart(true,true,false,false,false,false));
        module.onEnable();
        assertFalse(module.mayStart(false,true,false,false,false,false));
        assertFalse(module.mayStart(true,false,false,false,false,false));
        assertFalse(module.mayStart(true,true,true,false,false,false));
        assertFalse(module.mayStart(true,true,false,true,false,false));
        assertFalse(module.mayStart(true,true,false,false,true,false));
        assertFalse(module.mayStart(true,true,false,false,false,true));
        assertTrue(module.mayStart(true,true,false,false,false,false));
        module.started();
        assertTrue(module.ownsBlock());
        assertFalse(module.shouldStop(false,false,true,true));
        assertTrue(module.shouldStop(false,false,true,true));
        assertFalse(module.ownsBlock());
        module.started();
        assertTrue(module.releaseForAction(false));
        assertFalse(module.releaseForAction(false));
        module.started();
        assertTrue(module.shouldStop(true,false,true,true));
        module.started();
        assertTrue(module.shouldStop(false,false,false,true));
        module.started();
        assertTrue(module.shouldStop(false,false,true,false));
        module.started();
        module.onDisable();
        assertTrue(module.shouldStop(false,false,true,true));
    }

    @Test void manualInputClaimsOwnershipWithoutSyntheticRelease() {
        final Minecraft189AutoBlockModule module = new Minecraft189AutoBlockModule();
        module.onEnable();
        module.started();
        assertFalse(module.shouldStop(false,true,true,true));
        assertFalse(module.ownsBlock());
        module.started();
        assertFalse(module.releaseForAction(true));
        assertFalse(module.ownsBlock());
        module.pauseManualRightSetting().set(false);
        assertTrue(module.mayStart(true,true,false,true,false,false));
        module.holdTicksSetting().set(1);
        module.started();
        assertTrue(module.shouldStop(false,false,true,true));
    }

    @Test void settingsAreBoundedPersistedAndUnregistered() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final Minecraft189AutoBlockFeature feature = Minecraft189AutoBlockFeature.install(
                modules, controller, new ModulePresentationRegistry(),
                new ModuleSettingRegistry(modules, settings),
                settings, new SettingPresentationRegistry());
        try {
            assertEquals("2", settings.snapshotEncoded().get(
                    Minecraft189AutoBlockModule.HOLD_TICKS));
            feature.module().holdTicksSetting().set(7);
            assertEquals("7", settings.snapshotEncoded().get(
                    Minecraft189AutoBlockModule.HOLD_TICKS));
            assertThrows(IllegalArgumentException.class,
                    () -> feature.module().holdTicksSetting().set(0));
            assertThrows(IllegalArgumentException.class,
                    () -> feature.module().holdTicksSetting().set(21));
            controller.enable(Minecraft189AutoBlockModule.ID);
            feature.module().started();
            controller.disable(Minecraft189AutoBlockModule.ID);
            assertTrue(feature.module().ownsBlock());
            assertTrue(feature.module().releaseForAction(false));
        } finally {
            feature.close();
            feature.close();
        }
        assertNull(modules.find(Minecraft189AutoBlockModule.ID));
        assertNull(settings.find(Minecraft189AutoBlockModule.HOLD_TICKS));
    }
}
