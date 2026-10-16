package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import java.util.UUID;
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
    void nativeUuidSwitchNeverInheritsConfirmationOrAccruedCpsCredit() {
        final Minecraft189TriggerBotModule bot = new Minecraft189TriggerBotModule();
        final Minecraft189PlayerMovementState movement = new Minecraft189PlayerMovementState();
        movement.update(true, false, true);
        final UUID a = new UUID(0L, 111L);
        final UUID b = new UUID(0L, 222L);
        bot.minCpsSetting().set(20);
        bot.maxCpsSetting().set(20);
        bot.confirmTicksSetting().set(3);
        bot.onEnable();
        try {
            assertFalse(bot.shouldClick(true, false, false, movement.snapshot(), a));
            assertFalse(bot.shouldClick(true, false, false, movement.snapshot(), a));
            assertFalse(bot.shouldClick(true, false, false, movement.snapshot(), b),
                    "new B must not inherit two real A crosshair confirmations");
            assertFalse(bot.shouldClick(true, false, false, movement.snapshot(), b));
            assertTrue(bot.shouldClick(true, false, false, movement.snapshot(), b),
                    "B only clicks after three consecutive verified B ticks");
            assertFalse(bot.shouldClick(true, false, false, movement.snapshot(), a),
                    "switching back to A must reverify A instead of replaying B credit");
            assertFalse(bot.shouldClick(true, false, false, movement.snapshot(), a));
            assertTrue(bot.shouldClick(true, false, false, movement.snapshot(), a));
            assertFalse(bot.shouldClick(true, false, false, movement.snapshot(), null),
                    "unavailable native world UUID cancels all scheduler authority");
            assertFalse(bot.shouldClick(true, false, false, movement.snapshot(), a));
            assertFalse(bot.shouldClick(true, false, false, movement.snapshot(), a));
            assertTrue(bot.shouldClick(true, false, false, movement.snapshot(), a));
        } finally { bot.onDisable(); }
    }

    @Test
    void identitySwitchDropsPartialRateCreditEvenAtOneTickConfirmation() {
        final Minecraft189TriggerBotModule bot = new Minecraft189TriggerBotModule();
        final Minecraft189PlayerMovementState movement = new Minecraft189PlayerMovementState();
        movement.update(true, false, true);
        final UUID a = new UUID(0L, 1L), b = new UUID(0L, 2L);
        bot.confirmTicksSetting().set(1);
        bot.minCpsSetting().set(8);
        bot.maxCpsSetting().set(8);
        bot.onEnable();
        try {
            assertFalse(bot.shouldClick(true, false, false, movement.snapshot(), a));
            assertFalse(bot.shouldClick(true, false, false, movement.snapshot(), a));
            assertFalse(bot.shouldClick(true, false, false, movement.snapshot(), b),
                    "A's 16/20 click credit must never be spent on B");
            assertFalse(bot.shouldClick(true, false, false, movement.snapshot(), b));
            assertTrue(bot.shouldClick(true, false, false, movement.snapshot(), b));
            bot.suspend();
            assertFalse(bot.shouldClick(true, false, false, movement.snapshot(), b),
                    "GUI/combat ownership suspension must discard rate credit");
        } finally { bot.onDisable(); }
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
