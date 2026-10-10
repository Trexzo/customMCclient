package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189TriggerBotModuleTest {
    @Test
    void clicksOnlyAfterConfirmedCrosshairPlayerAtConfiguredCps() {
        final Minecraft189TriggerBotModule bot = new Minecraft189TriggerBotModule();
        final Minecraft189PlayerMovementState movement = new Minecraft189PlayerMovementState();
        movement.update(true, false, true);
        bot.minCpsSetting().set(10);
        bot.maxCpsSetting().set(10);
        bot.onEnable();
        try {
            assertFalse(bot.shouldClick(true, false, false, movement.snapshot()));
            assertFalse(bot.shouldClick(true, false, false, movement.snapshot()));
            assertTrue(bot.shouldClick(true, false, false, movement.snapshot()));
            assertFalse(bot.shouldClick(false, false, false, movement.snapshot()));
            assertFalse(bot.shouldClick(true, false, false, movement.snapshot()));
            assertFalse(bot.shouldClick(true, false, false, movement.snapshot()));
            assertTrue(bot.shouldClick(true, false, false, movement.snapshot()));
            bot.suspend();
            assertFalse(bot.shouldClick(true, false, false, movement.snapshot()));
            assertFalse(bot.shouldClick(true, false, false, movement.snapshot()));
            assertTrue(bot.shouldClick(true, false, false, movement.snapshot()));
        } finally {
            bot.onDisable();
        }
    }

    @Test
    void physicalAndMovementGatesNeverAccumulateCredit() {
        final Minecraft189TriggerBotModule bot = new Minecraft189TriggerBotModule();
        final Minecraft189PlayerMovementState movement = new Minecraft189PlayerMovementState();
        movement.update(true, false, true);
        bot.minCpsSetting().set(20);
        bot.maxCpsSetting().set(20);
        bot.confirmTicksSetting().set(1);
        bot.requireHoldSetting().set(true);
        bot.onEnable();
        try {
            assertFalse(bot.shouldClick(true, false, false, movement.snapshot()));
            assertFalse(bot.shouldClick(true, true, true, movement.snapshot()));
            movement.update(true, true, true);
            assertFalse(bot.shouldClick(true, true, false, movement.snapshot()));
            movement.update(true, false, true);
            assertTrue(bot.shouldClick(true, true, false, movement.snapshot()));
            bot.confirmTicksSetting().set(3);
            assertFalse(bot.shouldClick(true, true, false, movement.snapshot()));
            assertFalse(bot.shouldClick(true, true, false, movement.snapshot()));
            assertTrue(bot.shouldClick(true, true, false, movement.snapshot()));
            bot.onDisable();
            assertFalse(bot.shouldClick(true, true, false, movement.snapshot()));
        } finally {
            bot.onDisable();
        }
    }

    @Test
    void settingsPersistAndCleanup() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final Minecraft189TriggerBotFeature feature = Minecraft189TriggerBotFeature.install(
                modules, controller, new ModulePresentationRegistry(),
                new ModuleSettingRegistry(modules, settings),
                settings, new SettingPresentationRegistry());
        try {
            assertEquals("2", settings.snapshotEncoded().get(
                    Minecraft189TriggerBotModule.CONFIRM));
            feature.module().confirmTicksSetting().set(4);
            assertEquals("4", settings.snapshotEncoded().get(
                    Minecraft189TriggerBotModule.CONFIRM));
            assertThrows(IllegalArgumentException.class,
                    () -> feature.module().confirmTicksSetting().set(11));
            controller.enable(Minecraft189TriggerBotModule.ID);
        } finally {
            feature.close();
        }
        assertNull(modules.find(Minecraft189TriggerBotModule.ID));
        assertNull(settings.find(Minecraft189TriggerBotModule.MIN_CPS));
    }
}
