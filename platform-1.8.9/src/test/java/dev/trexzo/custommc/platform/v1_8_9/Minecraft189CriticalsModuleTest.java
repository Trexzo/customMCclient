package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189CriticalsModuleTest {
    @Test void onlyNaturalAirborneDescendingFallPermitsSyntheticAttack() {
        final Minecraft189CriticalsModule module = new Minecraft189CriticalsModule();
        final Minecraft189PlayerMovementState movement = new Minecraft189PlayerMovementState();
        final Minecraft189CriticalsEvidence evidence = new Minecraft189CriticalsEvidence();
        movement.update(false, false, true);
        assertTrue(module.permits(movement.snapshot(), evidence.snapshot(), false));
        module.onEnable();
        assertFalse(module.permits(movement.snapshot(), evidence.snapshot(), false));
        evidence.fall(0.25F);
        assertFalse(module.permits(movement.snapshot(), evidence.snapshot(), false));
        evidence.motion(-0.2D);
        assertTrue(module.permits(movement.snapshot(), evidence.snapshot(), false));
        assertFalse(module.permits(movement.snapshot(), evidence.snapshot(), true));
        movement.update(true, false, true);
        assertFalse(module.permits(movement.snapshot(), evidence.snapshot(), false));
        movement.update(false, false, true);
        evidence.motion(0.2D);
        assertFalse(module.permits(movement.snapshot(), evidence.snapshot(), false));
        module.requireDescendingSetting().set(false);
        assertTrue(module.permits(movement.snapshot(), evidence.snapshot(), false));
        module.minimumFallDistanceSetting().set(0.5D);
        assertFalse(module.permits(movement.snapshot(), evidence.snapshot(), false));
        evidence.fall(0.75F);
        assertTrue(module.permits(movement.snapshot(), evidence.snapshot(), false));
        evidence.reset();
        assertFalse(module.permits(movement.snapshot(), evidence.snapshot(), false));
        evidence.fall(Float.NaN);
        evidence.motion(-1.0D);
        assertFalse(module.permits(movement.snapshot(), evidence.snapshot(), false));
        module.onDisable();
        assertTrue(module.permits(null, null, true));
    }

    @Test void persistsValidatedSettingsAndReleasesAllRegistrations() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final Minecraft189CriticalsFeature feature = Minecraft189CriticalsFeature.install(
                modules, controller, new ModulePresentationRegistry(),
                new ModuleSettingRegistry(modules, settings),
                settings, new SettingPresentationRegistry());
        try {
            assertEquals("0.1", settings.snapshotEncoded().get(
                    Minecraft189CriticalsModule.MIN_FALL_DISTANCE));
            feature.module().minimumFallDistanceSetting().set(0.4D);
            assertEquals("0.4", settings.snapshotEncoded().get(
                    Minecraft189CriticalsModule.MIN_FALL_DISTANCE));
            assertThrows(IllegalArgumentException.class,
                    () -> feature.module().minimumFallDistanceSetting().set(-0.1D));
            assertThrows(IllegalArgumentException.class,
                    () -> feature.module().minimumFallDistanceSetting().set(5.0D));
            controller.enable(Minecraft189CriticalsModule.ID);
        } finally {
            feature.close();
            feature.close();
        }
        assertNull(modules.find(Minecraft189CriticalsModule.ID));
        assertNull(settings.find(Minecraft189CriticalsModule.MIN_FALL_DISTANCE));
    }
}
