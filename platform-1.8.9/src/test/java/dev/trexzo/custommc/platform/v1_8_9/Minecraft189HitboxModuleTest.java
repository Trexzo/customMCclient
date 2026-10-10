package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189HitboxModuleTest {
    @Test void onlyPlayerNativeBorderCanExpandWhileEnabled() {
        final Minecraft189HitboxModule hitbox = new Minecraft189HitboxModule();
        assertFalse(hitbox.active());
        assertEquals(0.1F, hitbox.adjustNativeBorder(true, 0.1F), 0.0F);
        assertEquals(0.1F, hitbox.adjustNativeBorder(false, 0.1F), 0.0F);
        hitbox.onEnable();
        try {
            assertEquals(0.3F, hitbox.adjustNativeBorder(true, 0.1F), 0.00001F);
            assertEquals(0.1F, hitbox.adjustNativeBorder(false, 0.1F), 0.0F);
            assertEquals(-1.0F, hitbox.adjustNativeBorder(true, -1.0F), 0.0F);
            assertEquals(Float.NaN, hitbox.adjustNativeBorder(true, Float.NaN));
            hitbox.extraBorderSetting().set(0.6D);
            assertEquals(0.7F, hitbox.adjustNativeBorder(true, 0.1F), 0.00001F);
            assertThrows(IllegalArgumentException.class,
                    () -> hitbox.extraBorderSetting().set(0.7D));
            assertThrows(IllegalArgumentException.class,
                    () -> hitbox.extraBorderSetting().set(-0.01D));
            assertThrows(IllegalArgumentException.class,
                    () -> hitbox.extraBorderSetting().set(Double.NaN));
            assertThrows(IllegalArgumentException.class,
                    () -> hitbox.extraBorderSetting().set(Double.POSITIVE_INFINITY));
            hitbox.extraBorderSetting().set(0.0D);
            assertEquals(0.1F, hitbox.adjustNativeBorder(true, 0.1F), 0.0F);
        } finally { hitbox.onDisable(); }
        assertEquals(0.1F, hitbox.adjustNativeBorder(true, 0.1F), 0.0F);
    }

    @Test void profilePersistenceAndLifecycle() {
        final ModuleRegistry modules = new ModuleRegistry();
        final SettingRegistry settings = new SettingRegistry();
        final ModuleController controller = new ModuleController(modules);
        final Minecraft189HitboxFeature feature = Minecraft189HitboxFeature.install(
                modules, controller, new ModulePresentationRegistry(),
                new ModuleSettingRegistry(modules, settings),
                settings, new SettingPresentationRegistry());
        try {
            feature.module().extraBorderSetting().set(0.45D);
            assertEquals("0.45", settings.snapshotEncoded()
                    .get(Minecraft189HitboxModule.EXTRA_BORDER));
            controller.enable(Minecraft189HitboxModule.ID);
            assertTrue(feature.module().active());
        } finally { feature.close(); feature.close(); }
        assertNull(settings.find(Minecraft189HitboxModule.EXTRA_BORDER));
        assertNull(modules.find(Minecraft189HitboxModule.ID));
    }
}
