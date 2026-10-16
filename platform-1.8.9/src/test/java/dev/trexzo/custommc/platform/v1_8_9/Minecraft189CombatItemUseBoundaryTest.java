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


    @Test void ownerChangedDuringSourceGetterOrNativeSetterCannotPublishLease() {
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
            final Inventory original = new Inventory();
            original.selected = 4;
            original.items[1] = new Item(true, false, 1);
            final Player oldPlayer = new Player(original);
            final Inventory successor = new Inventory();
            successor.selected = 3;
            final Player newPlayer = new Player(successor);
            controller.enable(Minecraft189AutoRodModule.ID);
            controller.enable(Minecraft189CombatSlotModule.ID);

            runtime.playerHotbarSlot(oldPlayer);
            runtime.playerHeldItem(oldPlayer);
            oldPlayer.duringHeldItemRead = () -> {
                // Original getter reenters the host after its own source
                // inventory slot has been selected but before admission.
                runtime.playerHotbarSlot(newPlayer);
                runtime.playerHeldItem(newPlayer);
            };
            assertEquals(-1, runtime.selectAutoRodSlot(),
                    "ownership changed inside native held-item getter");
            assertEquals(4, original.selected,
                    "failed selection must return temporary source slot");
            assertEquals(3, successor.selected);
            assertFalse(runtime.mayExecuteAutoRodUse(4));
            runtime.restoreAutoRodSlot(-1);
            assertEquals(4, original.selected);

            // The same callback race may occur inside a native setter.
            runtime.playerHotbarSlot(oldPlayer);
            runtime.playerHeldItem(oldPlayer);
            original.duringNextSetter = () -> {
                runtime.playerHotbarSlot(newPlayer);
                runtime.playerHeldItem(newPlayer);
                assertEquals(-1, runtime.selectAutoRodSlot(),
                        "selection reservation must block native setter reentry");
            };
            assertEquals(-1, runtime.selectCombatSlotBeforeSyntheticClick(true));
            assertEquals(4, original.selected);
            assertEquals(3, successor.selected);
            runtime.restoreCombatSlotAfterSyntheticClick(-1);
            assertEquals(4, original.selected);

            // A later genuine callback from the old player must still be
            // eligible; no zombie module lease remains after either veto.
            runtime.playerHotbarSlot(oldPlayer);
            runtime.playerHeldItem(oldPlayer);
            assertEquals(4, runtime.selectAutoRodSlot());
            assertEquals(1, original.selected);
            runtime.restoreAutoRodSlot(4);
            assertEquals(4, original.selected);
        } finally {
            runtime.close();
        }
    }


    @Test void sourceClickGuiOpeningInsideNativeItemReadImmediatelyRevokesSlot() {
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

            player.duringHeldItemRead = () ->
                    runtime.clickGuiToggleController().key(
                            Minecraft189ClickGuiToggleController.DEFAULT_TOGGLE_KEY,
                            true, false);
            assertEquals(-1, runtime.selectAutoRodSlot(),
                    "native getter opening the menu must veto the rod lease");
            assertTrue(runtime.clickGuiRuntime().coreRuntime()
                    .model().snapshot().open());
            assertEquals(4, inventory.selected);
            assertFalse(runtime.mayExecuteAutoRodUse(4));

            runtime.clickGuiToggleController().key(
                    Minecraft189ClickGuiToggleController.DEFAULT_TOGGLE_KEY,
                    true, false);
            player.duringHeldItemRead = () ->
                    runtime.clickGuiToggleController().key(
                            Minecraft189ClickGuiToggleController.DEFAULT_TOGGLE_KEY,
                            true, false);
            assertEquals(-1, runtime.selectAutoPotSlot(),
                    "native getter opening the menu must veto the potion lease");
            assertEquals(4, inventory.selected);
            assertTrue(runtime.clickGuiRuntime().coreRuntime()
                    .model().snapshot().open());
            runtime.clickGuiToggleController().key(
                    Minecraft189ClickGuiToggleController.DEFAULT_TOGGLE_KEY,
                    true, false);

            assertEquals(4, runtime.selectAutoRodSlot(),
                    "a fresh gameplay-owned action must be eligible after GUI close");
            assertEquals(1, inventory.selected);
            runtime.restoreAutoRodSlot(4);
            assertEquals(4, inventory.selected);
            assertEquals(4, runtime.selectAutoPotSlot());
            assertEquals(2, inventory.selected);
            runtime.restoreAutoPotSlot(4);
            assertEquals(4, inventory.selected);
        } finally {
            runtime.close();
        }
    }


    @Test void wrongPositiveFinallyTokensCannotReleaseNativeRodPotionOrMeleeOwners() {
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

            assertEquals(4, runtime.selectAutoRodSlot());
            assertEquals(1, inventory.selected);
            runtime.restoreAutoRodSlot(5);
            runtime.restoreAutoRodSlot(-1);
            assertEquals(1, inventory.selected);
            assertTrue(runtime.mayExecuteAutoRodUse(4));
            assertEquals(-1, runtime.selectAutoPotSlot(),
                    "incorrect positive Rod finally cannot unlock Potion");
            assertEquals(-1, runtime.selectCombatSlotBeforeSyntheticClick(true),
                    "incorrect Rod finally cannot unlock melee");
            runtime.restoreCombatSlotAfterSyntheticClick(4);
            assertEquals(1, inventory.selected);
            runtime.restoreAutoRodSlot(4);
            assertEquals(4, inventory.selected);

            assertEquals(4, runtime.selectAutoPotSlot());
            assertEquals(2, inventory.selected);
            runtime.restoreAutoPotSlot(3);
            runtime.restoreAutoPotSlot(-1);
            assertEquals(2, inventory.selected);
            assertTrue(runtime.mayExecuteAutoPotUse(4));
            assertEquals(-1, runtime.selectAutoRodSlot(),
                    "incorrect Potion finally cannot unlock Rod");
            runtime.restoreAutoPotSlot(4);
            assertEquals(4, inventory.selected);

            assertEquals(4, runtime.selectCombatSlotBeforeSyntheticClick(true));
            assertEquals(0, inventory.selected);
            runtime.restoreCombatSlotAfterSyntheticClick(6);
            runtime.restoreCombatSlotAfterSyntheticClick(-1);
            assertEquals(0, inventory.selected);
            assertEquals(-1, runtime.selectAutoRodSlot(),
                    "incorrect positive melee finally cannot unlock Rod");
            assertEquals(-1, runtime.selectAutoPotSlot());
            runtime.restoreCombatSlotAfterSyntheticClick(4);
            assertEquals(4, inventory.selected);
            assertEquals(4, runtime.selectAutoRodSlot(),
                    "genuine finally releases lease and restores eligibility");
            runtime.restoreAutoRodSlot(4);
            assertEquals(4, inventory.selected);
        } finally {
            runtime.close();
        }
    }


    @Test void nativeSetterExceptionMustReleaseHostReservationForAllCombatLanes() {
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

            // A real original InventoryPlayer setter can mutate its
            // currentItem backing field before an adapter throws.
            inventory.duringNextSetter = () -> {
                assertEquals(-1, runtime.selectAutoPotSlot(),
                        "native setter reservation must block nested Potion");
                throw new IllegalStateException("original setter threw");
            };
            assertThrows(IllegalStateException.class,
                    runtime::selectAutoRodSlot);
            assertEquals(4, inventory.selected,
                    "original Rod module must roll back its own source slot");
            assertEquals(4, runtime.selectAutoPotSlot(),
                    "native exception must clear shared selecting reservation");
            assertEquals(2, inventory.selected);
            runtime.restoreAutoPotSlot(4);
            assertEquals(4, inventory.selected);

            inventory.duringNextSetter = () -> {
                assertEquals(-1, runtime.selectAutoRodSlot());
                throw new IllegalStateException("potion setter threw");
            };
            assertThrows(IllegalStateException.class,
                    runtime::selectAutoPotSlot);
            assertEquals(4, inventory.selected);
            assertEquals(4, runtime.selectAutoRodSlot());
            runtime.restoreAutoRodSlot(4);
            assertEquals(4, inventory.selected);

            inventory.duringNextSetter = () -> {
                assertEquals(-1, runtime.selectAutoPotSlot());
                throw new IllegalStateException("melee setter threw");
            };
            assertThrows(IllegalStateException.class,
                    () -> runtime.selectCombatSlotBeforeSyntheticClick(true));
            assertEquals(4, inventory.selected);
            assertEquals(4, runtime.selectAutoPotSlot(),
                    "melee setter exception must release exclusive owner");
            runtime.restoreAutoPotSlot(4);
            assertEquals(4, inventory.selected);
        } finally {
            runtime.close();
        }
    }


    @Test void deniedNestedMeleeLeaseCannotDispatchWithAnothersOriginalRod() {
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
            final Player player = new Player(inventory);
            runtime.playerHotbarSlot(player);
            runtime.playerHeldItem(player);
            controller.enable(Minecraft189AutoRodModule.ID);
            controller.enable(Minecraft189CombatSlotModule.ID);
            assertTrue(runtime.mayDispatchSyntheticMelee(-1),
                    "no switching module owner must preserve vanilla clicks");
            assertEquals(4, runtime.selectAutoRodSlot());
            assertEquals(1, inventory.selected);
            assertEquals(-1, runtime.selectCombatSlotBeforeSyntheticClick(true));
            assertFalse(runtime.mayDispatchSyntheticMelee(-1),
                    "denied nested melee may not click with temporary rod");
            runtime.restoreCombatSlotAfterSyntheticClick(-1);
            assertEquals(1, inventory.selected);
            assertFalse(runtime.mayDispatchSyntheticMelee(4),
                    "old positive token never grants a foreign melee action");
            runtime.restoreAutoRodSlot(4);
            assertEquals(4, inventory.selected);
            assertTrue(runtime.mayDispatchSyntheticMelee(-1),
                    "the real Rod finally releases original vanilla click lane");
            assertEquals(4, runtime.selectCombatSlotBeforeSyntheticClick(true));
            assertEquals(0, inventory.selected);
            assertTrue(runtime.mayDispatchSyntheticMelee(4));
            inventory.selected = 7; // genuine user changed original currentItem
            assertFalse(runtime.mayDispatchSyntheticMelee(4),
                    "stale selected native slot cannot authorize melee attack");
            runtime.restoreCombatSlotAfterSyntheticClick(4);
            assertEquals(7, inventory.selected,
                    "original user-selected slot is never overwritten by finally");
        } finally {
            runtime.close();
        }
    }

    @Test void sourceRespawnAndGuiFocusRevokeSelectedMeleeBeforeActualDispatch() {
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
            final Inventory old = new Inventory();
            old.selected = 4;
            final Player oldPlayer = new Player(old);
            final Inventory next = new Inventory();
            next.selected = 3;
            final Player nextPlayer = new Player(next);
            runtime.playerHotbarSlot(oldPlayer);
            runtime.playerHeldItem(oldPlayer);
            controller.enable(Minecraft189CombatSlotModule.ID);
            assertEquals(4, runtime.selectCombatSlotBeforeSyntheticClick(true));
            assertEquals(0, old.selected);
            assertTrue(runtime.mayDispatchSyntheticMelee(4));
            runtime.clickGuiToggleController().key(
                    Minecraft189ClickGuiToggleController.DEFAULT_TOGGLE_KEY,
                    true, false);
            assertFalse(runtime.mayDispatchSyntheticMelee(4),
                    "real CustomMC UI input focus must veto original attack");
            runtime.clickGuiToggleController().key(
                    Minecraft189ClickGuiToggleController.DEFAULT_TOGGLE_KEY,
                    true, false);
            runtime.playerHotbarSlot(nextPlayer);
            runtime.playerHeldItem(nextPlayer);
            assertFalse(runtime.mayDispatchSyntheticMelee(4),
                    "native new local-player source invalidates old melee");
            runtime.restoreCombatSlotAfterSyntheticClick(4);
            assertEquals(4, old.selected,
                    "old original InventoryPlayer receives its own restore");
            assertEquals(3, next.selected,
                    "no previous melee action can write the respawned hotbar");
            assertTrue(runtime.mayDispatchSyntheticMelee(-1),
                    "after old finally a clean new-player vanilla action is legal");
        } finally {
            runtime.close();
        }
    }

    private static final class Inventory
            implements Minecraft189InventoryHotbarControl {
        int selected;
        Runnable duringNextSetter;
        final Minecraft189ItemStackAccess[] items =
                new Minecraft189ItemStackAccess[9];
        @Override public int customMcSelectedHotbarSlot() { return selected; }
        @Override public void customMcSetSelectedHotbarSlot(final int slot) {
            selected = slot;
            final Runnable callback = duringNextSetter;
            duringNextSetter = null;
            if (callback != null) callback.run();
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
