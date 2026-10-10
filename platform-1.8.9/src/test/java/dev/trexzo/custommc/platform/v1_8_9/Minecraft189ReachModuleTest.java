package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189ReachModuleTest {
    @Test void disabledPreservesVanillaAndEnabledOverridesOnlyLocalRaycast() {
        final Minecraft189ReachModule reach = new Minecraft189ReachModule();
        assertFalse(reach.active());
        assertEquals(4.5F, reach.raycastBlockDistance(4.5F), 0.0F);
        assertFalse(reach.raycastExtendedBranch(false));
        assertTrue(reach.raycastExtendedBranch(true));
        assertEquals(6.0D, reach.raycastExtendedDistance(6.0D), 0.0D);

        reach.distanceSetting().set(4.2D);
        reach.onEnable();
        try {
            assertEquals(4.2F, reach.raycastBlockDistance(4.5F), 0.00001F);
            assertTrue(reach.raycastExtendedBranch(false));
            assertTrue(reach.raycastExtendedBranch(true));
            assertEquals(4.2D, reach.raycastExtendedDistance(6.0D), 0.0D);
            reach.distanceSetting().set(6.0D);
            assertEquals(6.0D, reach.raycastExtendedDistance(6.0D), 0.0D);
            assertThrows(IllegalArgumentException.class,
                    () -> reach.distanceSetting().set(6.1D));
            assertThrows(IllegalArgumentException.class,
                    () -> reach.distanceSetting().set(2.9D));
            assertThrows(IllegalArgumentException.class,
                    () -> reach.distanceSetting().set(Double.NaN));
            assertThrows(IllegalArgumentException.class,
                    () -> reach.distanceSetting().set(Double.POSITIVE_INFINITY));
            assertEquals(6.0D, reach.distanceSetting().get(), 0.0D);
        } finally { reach.onDisable(); }
        assertEquals(4.5F, reach.raycastBlockDistance(4.5F), 0.0F);
        assertFalse(reach.raycastExtendedBranch(false));
        assertEquals(6.0D, reach.raycastExtendedDistance(6.0D), 0.0D);
    }

    @Test void registeredConfigAndLifecycle() {
        final ModuleRegistry modules = new ModuleRegistry();
        final SettingRegistry settings = new SettingRegistry();
        final ModuleController controller = new ModuleController(modules);
        final Minecraft189ReachFeature feature = Minecraft189ReachFeature.install(
                modules, controller, new ModulePresentationRegistry(),
                new ModuleSettingRegistry(modules, settings),
                settings, new SettingPresentationRegistry());
        try {
            assertNotNull(modules.find(Minecraft189ReachModule.ID));
            feature.module().distanceSetting().set(5.3D);
            assertEquals("5.3", settings.snapshotEncoded()
                    .get(Minecraft189ReachModule.DISTANCE));
            controller.enable(Minecraft189ReachModule.ID);
            assertTrue(feature.module().active());
        } finally { feature.close(); feature.close(); }
        assertNull(modules.find(Minecraft189ReachModule.ID));
        assertNull(settings.find(Minecraft189ReachModule.DISTANCE));
    }
}
