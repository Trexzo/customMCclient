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
import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Original mapped source at AutoRod's final vanilla rightClickMouse boundary.
 * Does not fabricate a controller call or a dedicated-server damage pass.
 */
final class Minecraft189AutoRodNativeDispatchOwnerTest {
    private static final class Item implements Minecraft189ItemStackAccess {
        boolean rod = true;
        int count = 1;
        Runnable onRodGetter;
        @Override public String customMcDisplayName() { return "Fishing rod"; }
        @Override public int customMcStackSize() { return count; }
        @Override public int customMcItemDamage() { return 0; }
        @Override public int customMcMaxDamage() { return 64; }
        @Override public boolean customMcIsFishingRod() {
            final Runnable callback = onRodGetter;
            onRodGetter = null;
            if (callback != null) callback.run();
            return rod;
        }
    }

    private static final class Inventory
            implements Minecraft189InventoryHotbarItemsAccess {
        int slot = 4;
        final Minecraft189ItemStackAccess[] items =
                new Minecraft189ItemStackAccess[9];
        @Override public int customMcSelectedHotbarSlot() { return slot; }
        @Override public void customMcSetSelectedHotbarSlot(final int selected) {
            slot = selected;
        }
        @Override public Minecraft189ItemStackAccess[] customMcHotbarItems() {
            return items.clone();
        }
    }

    private static final class Player implements Minecraft189PlayerInventoryAccess,
            Minecraft189PlayerHeldItemAccess, Minecraft189PlayerFishingHookAccess {
        final Inventory inventory = new Inventory();
        Runnable onHeldGetter;
        Runnable onHookGetter;
        boolean hooked;
        Player() { inventory.items[1] = new Item(); }
        @Override public Minecraft189InventoryHotbarAccess customMcInventory() {
            return inventory;
        }
        @Override public Minecraft189ItemStackAccess customMcHeldItem() {
            final Runnable callback = onHeldGetter;
            onHeldGetter = null;
            if (callback != null) callback.run();
            return inventory.items[inventory.slot];
        }
        @Override public boolean customMcHasFishingHook() {
            final Runnable callback = onHookGetter;
            onHookGetter = null;
            if (callback != null) callback.run();
            return hooked;
        }
        Item rod() { return (Item) inventory.items[1]; }
    }

    private static final class Ui implements LegacyUiHostCallbacks {
        @Override public int framebufferWidth() { return 1280; }
        @Override public int framebufferHeight() { return 720; }
        @Override public float uiScale() { return 1F; }
        @Override public void beginUi(UiViewport v) {}
        @Override public void fillRect(float a,float b,float c,float d,int e) {}
        @Override public void fillRoundedRect(float a,float b,float c,float d,
                float e,int f) {}
        @Override public void strokeRect(float a,float b,float c,float d,
                float e,int f) {}
        @Override public void pushClip(float a,float b,float c,float d) {}
        @Override public void popClip() {}
        @Override public void drawText(UiFontHandle f,float a,float b,
                String c,int d) {}
        @Override public void endUi() {}
    }

    private static final class Fixture implements AutoCloseable {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final ServiceRegistry services = new ServiceRegistry();
        final SettingRegistry settings = new SettingRegistry();
        final Minecraft189HostRuntime host;
        final Player player = new Player();
        Fixture() {
            services.register(RenderPipeline.class, new RenderPipeline());
            final Minecraft189Platform platform = new Minecraft189Platform();
            platform.attach(new PlatformContext(
                    new EventBus(), modules, controller, services));
            host = Minecraft189HostRuntime.install(platform,
                    new ModulePresentationRegistry(),
                    new ModuleCategoryRegistry(),
                    new ModuleSettingRegistry(modules, settings),
                    null, null, settings,
                    new SettingPresentationRegistry(), new Ui());
            controller.enable(Minecraft189AutoRodModule.ID);
            host.inputState().pointerButton(
                    Minecraft189ClickRateTracker.LEFT_BUTTON, true);
            host.playerHotbarSlot(player);
            host.playerHeldItem(player);
        }
        int select() { return host.selectAutoRodSlot(); }
        void restore(final int original) { host.restoreAutoRodSlot(original); }
        @Override public void close() { host.close(); }
    }

    @Test void realOriginalRodStillAdmittedAndSlotRestored() {
        try (Fixture f = new Fixture()) {
            final int original = f.select();
            assertEquals(4, original);
            assertEquals(1, f.player.inventory.slot);
            assertTrue(f.host.mayDispatchAutoRodNativeUse(original));
            assertTrue(f.host.mayDispatchAutoRodNativeUse(original));
            f.restore(original);
            assertEquals(4, f.player.inventory.slot);
            assertFalse(f.host.mayDispatchAutoRodNativeUse(original));
        }
    }

    @Test void readOnlyNativeRodDiscoveryDoesNotRequireMouseButNeverDispatches() {
        try (Fixture f = new Fixture()) {
            final int original = f.select();
            assertEquals(4, original);
            f.host.inputState().pointerButton(
                    Minecraft189ClickRateTracker.LEFT_BUTTON, false);
            // V132's genuine /give discovery checks source ItemStack and
            // currentItem without issuing an attack or a fake LMB event.
            assertTrue(f.host.mayExecuteAutoRodUse(original));
            assertFalse(f.host.mayDispatchAutoRodNativeUse(original));
            f.host.inputState().pointerButton(
                    Minecraft189ClickRateTracker.LEFT_BUTTON, true);
            assertTrue(f.host.mayDispatchAutoRodNativeUse(original));
            f.restore(original);
        }
    }

    @Test void physicalAttackReleaseOrManualRightClickRevokesFinalRodUse() {
        try (Fixture f = new Fixture()) {
            final int original = f.select();
            assertEquals(4, original);
            f.host.inputState().pointerButton(
                    Minecraft189ClickRateTracker.LEFT_BUTTON, false);
            assertFalse(f.host.mayDispatchAutoRodNativeUse(original));
            f.host.inputState().pointerButton(
                    Minecraft189ClickRateTracker.LEFT_BUTTON, true);
            assertTrue(f.host.mayDispatchAutoRodNativeUse(original));
            f.host.inputState().pointerButton(
                    Minecraft189ClickRateTracker.RIGHT_BUTTON, true);
            assertFalse(f.host.mayDispatchAutoRodNativeUse(original));
            f.restore(original);
        }
    }

    @Test void originalHeldItemCallbackReentryWithSamePlayerRevokesOldGeneration() {
        try (Fixture f = new Fixture()) {
            final int original = f.select();
            assertEquals(4, original);
            f.player.onHeldGetter = () -> f.host.playerHeldItem(f.player);
            assertFalse(f.host.mayDispatchAutoRodNativeUse(original));
            f.restore(original);
        }
    }

    @Test void originalHeldItemCallbackFromAnotherPlayerCannotStealOldCast() {
        try (Fixture f = new Fixture()) {
            final int original = f.select();
            assertEquals(4, original);
            final Player other = new Player();
            f.player.onHeldGetter = () -> {
                f.host.playerHotbarSlot(other);
                f.host.playerHeldItem(other);
            };
            assertFalse(f.host.mayDispatchAutoRodNativeUse(original));
            f.restore(original);
        }
    }

    @Test void reentrantNativeRodItemGetterCannotBorrowNewPolicyOrSource() {
        try (Fixture f = new Fixture()) {
            final int original = f.select();
            assertEquals(4, original);
            f.player.rod().onRodGetter = () ->
                    f.host.featureCatalog().autoRod()
                        .cooldownTicksSetting().set(45);
            assertFalse(f.host.mayDispatchAutoRodNativeUse(original));
            f.restore(original);
        }
    }

    @Test void preserveHookIsVerifiedOnCurrentOriginalPlayerAtUse() {
        try (Fixture f = new Fixture()) {
            f.host.featureCatalog().autoRod()
                    .preserveActiveHookSetting().set(true);
            final int original = f.select();
            assertEquals(4, original);
            assertTrue(f.host.mayDispatchAutoRodNativeUse(original));
            f.player.hooked = true;
            assertFalse(f.host.mayDispatchAutoRodNativeUse(original));
            f.restore(original);
        }
    }

    @Test void sourceHookReadReentryRevokesSamePlayerGeneration() {
        try (Fixture f = new Fixture()) {
            f.host.featureCatalog().autoRod()
                    .preserveActiveHookSetting().set(true);
            final int original = f.select();
            assertEquals(4, original);
            f.player.onHookGetter = () -> f.host.playerHeldItem(f.player);
            assertFalse(f.host.mayDispatchAutoRodNativeUse(original));
            f.restore(original);
        }
    }

    @Test void guiFocusAndChangedSlotCannotDispatchRodOrOverwriteUserSlot() {
        try (Fixture f = new Fixture()) {
            final int original = f.select();
            assertEquals(4, original);
            f.host.key(LegacyKeyboardCodes.RIGHT_SHIFT, (char) 0,
                    true, false, true, false, false);
            assertFalse(f.host.mayDispatchAutoRodNativeUse(original));
            f.player.inventory.slot = 6;
            f.restore(original);
            assertEquals(6, f.player.inventory.slot);
        }
    }
}
