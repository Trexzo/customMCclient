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
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class Minecraft189CombatItemUseBoundaryTest {
    @Test void sourceHostVetoesFinalRodAndPotionUseOnActualClickGuiFocus() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final ServiceRegistry services = new ServiceRegistry();
        services.register(RenderPipeline.class, new RenderPipeline());
        final Minecraft189Platform platform = new Minecraft189Platform();
        platform.attach(new PlatformContext(new EventBus(),
                modules, controller, services));
        final Minecraft189HostRuntime runtime = Minecraft189HostRuntime.install(
                platform, new ModulePresentationRegistry(),
                new ModuleCategoryRegistry(),
                new ModuleSettingRegistry(modules, settings),
                null, null, settings, new SettingPresentationRegistry(),
                new NoOpHost());
        try {
            final Inventory inventory = new Inventory();
            inventory.selected = 4;
            inventory.items[1] = new Item(true, false, 1);
            inventory.items[2] = new Item(false, true, 1);
            final Player player = new Player(inventory);
            runtime.playerHotbarSlot(player);
            runtime.playerHeldItem(player);
            controller.enable(Minecraft189AutoRodModule.ID);
            controller.enable(Minecraft189AutoPotModule.ID);

            final int originalRod = runtime.selectAutoRodSlot();
            assertEquals(4, originalRod);
            assertEquals(1, inventory.selected);
            assertTrue(runtime.mayExecuteAutoRodUse(originalRod));
            runtime.clickGuiToggleController().key(
                    Minecraft189ClickGuiToggleController.DEFAULT_TOGGLE_KEY,
                    true, false);
            assertTrue(runtime.clickGuiRuntime().coreRuntime()
                    .model().snapshot().open());
            assertFalse(runtime.mayExecuteAutoRodUse(originalRod));
            assertEquals(1, inventory.selected,
                    "GUI veto must not mutate original module-owned lease");
            runtime.restoreAutoRodSlot(originalRod);
            assertEquals(4, inventory.selected);

            runtime.clickGuiToggleController().key(
                    Minecraft189ClickGuiToggleController.DEFAULT_TOGGLE_KEY,
                    true, false);
            assertFalse(runtime.clickGuiRuntime().coreRuntime()
                    .model().snapshot().open());
            final int originalPotion = runtime.selectAutoPotSlot();
            assertEquals(4, originalPotion);
            assertTrue(runtime.mayExecuteAutoPotUse(originalPotion));
            assertEquals(2, inventory.selected);

            // Native AutoBlock release can change the mapped held item
            // without touching the hotbar slot; this must revoke the use.
            inventory.items[2] = new Item(false, false, 1);
            assertFalse(runtime.mayExecuteAutoPotUse(originalPotion));
            inventory.items[2] = new Item(false, true, 0);
            assertFalse(runtime.mayExecuteAutoPotUse(originalPotion));
            inventory.items[2] = new Item(false, true, 1);
            assertTrue(runtime.mayExecuteAutoPotUse(originalPotion));
            inventory.selected = 7;
            assertFalse(runtime.mayExecuteAutoPotUse(originalPotion));
            runtime.restoreAutoPotSlot(originalPotion);
            assertEquals(7, inventory.selected,
                    "the user's later hotbar selection must survive cleanup");
            controller.disable(Minecraft189AutoRodModule.ID);
            controller.disable(Minecraft189AutoPotModule.ID);
        } finally {
            runtime.close();
        }
    }


    @Test void sourcePlayerRespawnCannotBorrowOldItemOrStrandCapturedInventory() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final ServiceRegistry services = new ServiceRegistry();
        services.register(RenderPipeline.class, new RenderPipeline());
        final Minecraft189Platform platform = new Minecraft189Platform();
        platform.attach(new PlatformContext(new EventBus(),
                modules, controller, services));
        final Minecraft189HostRuntime runtime = Minecraft189HostRuntime.install(
                platform, new ModulePresentationRegistry(),
                new ModuleCategoryRegistry(),
                new ModuleSettingRegistry(modules, settings),
                null, null, settings, new SettingPresentationRegistry(),
                new NoOpHost());
        try {
            final Inventory oldInventory = new Inventory();
            oldInventory.selected = 4;
            oldInventory.items[1] = new Item(true, false, 1);
            oldInventory.items[2] = new Item(false, true, 1);
            final Player oldPlayer = new Player(oldInventory);
            final Inventory newInventory = new Inventory();
            newInventory.selected = 3;
            newInventory.items[3] = new Item(true, false, 1);
            final Player newPlayer = new Player(newInventory);
            controller.enable(Minecraft189AutoRodModule.ID);
            controller.enable(Minecraft189AutoPotModule.ID);

            runtime.playerHotbarSlot(oldPlayer);
            runtime.playerHeldItem(newPlayer);
            assertEquals(-1, runtime.selectAutoRodSlot(),
                    "source held item from a different player cannot authorize old hotbar");
            assertEquals(4, oldInventory.selected);
            assertEquals(3, newInventory.selected);

            runtime.playerHeldItem(oldPlayer);
            final int rodPrevious = runtime.selectAutoRodSlot();
            assertEquals(4, rodPrevious);
            assertEquals(1, oldInventory.selected);
            // The synchronous action can reenter source player callbacks
            // after respawn; cleanup must still restore captured OLD owner.
            runtime.playerHotbarSlot(newPlayer);
            runtime.playerHeldItem(newPlayer);
            assertFalse(runtime.mayExecuteAutoRodUse(rodPrevious),
                    "respawned inventory must never borrow a pre-respawn rod lease");
            runtime.restoreAutoRodSlot(rodPrevious);
            assertEquals(4, oldInventory.selected,
                    "the old module-owned native currentItem must be restored");
            assertEquals(3, newInventory.selected,
                    "a new player must not be written by old finally");

            runtime.playerHotbarSlot(oldPlayer);
            runtime.playerHeldItem(oldPlayer);
            final int potPrevious = runtime.selectAutoPotSlot();
            assertEquals(4, potPrevious);
            assertEquals(2, oldInventory.selected);
            runtime.playerHotbarSlot(newPlayer);
            runtime.playerHeldItem(newPlayer);
            assertFalse(runtime.mayExecuteAutoPotUse(potPrevious));
            runtime.restoreAutoPotSlot(potPrevious);
            assertEquals(4, oldInventory.selected);
            assertEquals(3, newInventory.selected);
        } finally {
            runtime.close();
        }
    }


    @Test void originalHotbarLeaseExcludesConcurrentRodPotionAndMeleeOwners() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final ServiceRegistry services = new ServiceRegistry();
        services.register(RenderPipeline.class, new RenderPipeline());
        final Minecraft189Platform platform = new Minecraft189Platform();
        platform.attach(new PlatformContext(new EventBus(),
                modules, controller, services));
        final Minecraft189HostRuntime runtime = Minecraft189HostRuntime.install(
                platform, new ModulePresentationRegistry(),
                new ModuleCategoryRegistry(),
                new ModuleSettingRegistry(modules, settings),
                null, null, settings, new SettingPresentationRegistry(),
                new NoOpHost());
        try {
            final Inventory inventory = new Inventory();
            inventory.selected = 4;
            inventory.items[1] = new Item(true, false, 1);
            inventory.items[2] = new Item(false, true, 1);
            final Player player = new Player(inventory);
            runtime.playerHotbarSlot(player);
            runtime.playerHeldItem(player);
            controller.enable(Minecraft189AutoRodModule.ID);
            controller.enable(Minecraft189AutoPotModule.ID);
            controller.enable(Minecraft189CombatSlotModule.ID);

            // The source held-item getter is itself a callback boundary:
            // while Rod is selecting, Potion and melee cannot steal slot.
            player.duringHeldItemRead = () -> {
                assertEquals(-1, runtime.selectAutoPotSlot());
                assertEquals(-1, runtime.selectCombatSlotBeforeSyntheticClick(true));
                runtime.restoreCombatSlotAfterSyntheticClick(-1);
                assertEquals(1, inventory.selected);
            };
            assertEquals(4, runtime.selectAutoRodSlot());
            assertEquals(1, inventory.selected);
            assertEquals(-1, runtime.selectAutoPotSlot());
            assertEquals(-1, runtime.selectCombatSlotBeforeSyntheticClick(true));
            runtime.restoreCombatSlotAfterSyntheticClick(-1);
            assertTrue(runtime.mayExecuteAutoRodUse(4));
            runtime.restoreAutoRodSlot(4);
            assertEquals(4, inventory.selected);

            assertEquals(4, runtime.selectAutoPotSlot());
            assertEquals(2, inventory.selected);
            assertEquals(-1, runtime.selectAutoRodSlot());
            assertEquals(-1, runtime.selectCombatSlotBeforeSyntheticClick(true));
            runtime.restoreCombatSlotAfterSyntheticClick(-1);
            assertTrue(runtime.mayExecuteAutoPotUse(4));
            runtime.restoreAutoPotSlot(4);
            assertEquals(4, inventory.selected);

            assertEquals(4, runtime.selectCombatSlotBeforeSyntheticClick(true));
            assertEquals(0, inventory.selected,
                    "fixed CombatSlot's default setting chooses first hotbar slot");
            assertEquals(-1, runtime.selectAutoRodSlot());
            assertEquals(-1, runtime.selectAutoPotSlot());
            runtime.restoreCombatSlotAfterSyntheticClick(4);
            assertEquals(4, inventory.selected);
            assertEquals(4, runtime.selectAutoRodSlot(),
                    "release of melee lease must allow new Rod ownership");
            runtime.restoreAutoRodSlot(4);
            assertEquals(4, inventory.selected);
        } finally {
            runtime.close();
        }
    }

    private static final class Inventory
            implements Minecraft189InventoryHotbarControl {
        int selected;
        final Minecraft189ItemStackAccess[] items =
                new Minecraft189ItemStackAccess[9];
        @Override public int customMcSelectedHotbarSlot() { return selected; }
        @Override public void customMcSetSelectedHotbarSlot(final int slot) {
            selected = slot;
        }
    }

    private static final class Player implements Minecraft189PlayerInventoryAccess,
            Minecraft189PlayerHeldItemAccess {
        final Inventory inventory;
        Runnable duringHeldItemRead;
        Player(final Inventory inventory) { this.inventory = inventory; }
        @Override public Minecraft189InventoryHotbarAccess customMcInventory() {
            return inventory;
        }
        @Override public Minecraft189ItemStackAccess customMcHeldItem() {
            final Runnable callback = duringHeldItemRead;
            duringHeldItemRead = null;
            if (callback != null) callback.run();
            return inventory.items[inventory.selected];
        }
    }

    private static final class Item implements Minecraft189ItemStackAccess {
        final boolean rod;
        final boolean potion;
        final int count;
        Item(final boolean rod, final boolean potion, final int count) {
            this.rod = rod;
            this.potion = potion;
            this.count = count;
        }
        @Override public String customMcDisplayName() { return "source item"; }
        @Override public int customMcStackSize() { return count; }
        @Override public int customMcItemDamage() { return 0; }
        @Override public int customMcMaxDamage() { return 0; }
        @Override public boolean customMcIsFishingRod() { return rod; }
        @Override public boolean customMcIsPotion() { return potion; }
    }

    private static final class NoOpHost implements LegacyUiHostCallbacks {
        @Override public int framebufferWidth() { return 1280; }
        @Override public int framebufferHeight() { return 720; }
        @Override public float uiScale() { return 1.0F; }
        @Override public void beginUi(final UiViewport viewport) {}
        @Override public void fillRect(final float x, final float y,
                final float width, final float height, final int argb) {}
        @Override public void fillRoundedRect(final float x, final float y,
                final float width, final float height, final float radius,
                final int argb) {}
        @Override public void strokeRect(final float x, final float y,
                final float width, final float height, final float thickness,
                final int argb) {}
        @Override public void pushClip(final float x, final float y,
                final float width, final float height) {}
        @Override public void popClip() {}
        @Override public void drawText(final UiFontHandle font,
                final float x, final float y, final String text, final int argb) {}
        @Override public void endUi() {}
    }
}
