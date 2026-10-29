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
 * Original player health and native potion ItemStack evidence must agree
 * at the final production-only rightClickMouse boundary. No packets or hits.
 */
final class Minecraft189AutoPotLiveNativeDispatchTest {
    private static final class Potion implements Minecraft189ItemStackAccess {
        int count = 1;
        Runnable onTypeRead;
        @Override public String customMcDisplayName() { return "Splash Healing"; }
        @Override public int customMcStackSize() { return count; }
        @Override public int customMcItemDamage() { return 16389; }
        @Override public int customMcMaxDamage() { return 0; }
        @Override public boolean customMcIsPotion() {
            final Runnable cb = onTypeRead;
            onTypeRead = null;
            if (cb != null) cb.run();
            return true;
        }
    }
    private static final class Inventory
            implements Minecraft189InventoryHotbarItemsAccess {
        int slot = 4;
        final Minecraft189ItemStackAccess[] items =
                new Minecraft189ItemStackAccess[9];
        Inventory() { items[2] = new Potion(); }
        @Override public int customMcSelectedHotbarSlot() { return slot; }
        @Override public void customMcSetSelectedHotbarSlot(final int s) {
            slot = s;
        }
        @Override public Minecraft189ItemStackAccess[] customMcHotbarItems() {
            return items.clone();
        }
    }
    private static final class Player implements Minecraft189PlayerInventoryAccess,
            Minecraft189PlayerHeldItemAccess, Minecraft189PlayerHealthAccess {
        final Inventory inventory = new Inventory();
        float health = 5F, maxHealth = 20F;
        Runnable onHealthRead, onMaxRead, onHeldRead;
        @Override public Minecraft189InventoryHotbarAccess customMcInventory() {
            return inventory;
        }
        @Override public Minecraft189ItemStackAccess customMcHeldItem() {
            final Runnable cb = onHeldRead;
            onHeldRead = null;
            if (cb != null) cb.run();
            return inventory.items[inventory.slot];
        }
        @Override public float customMcMaxHealth() {
            final Runnable cb = onMaxRead;
            onMaxRead = null;
            if (cb != null) cb.run();
            return maxHealth;
        }
        @Override public float customMcHealth() {
            final Runnable cb = onHealthRead;
            onHealthRead = null;
            if (cb != null) cb.run();
            return health;
        }
        Potion potion() { return (Potion) inventory.items[2]; }
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
        final Player player = new Player();
        final Minecraft189HostRuntime host;
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
            controller.enable(Minecraft189AutoPotModule.ID);
            host.playerHotbarSlot(player);
            host.playerHeldItem(player);
        }
        int select() { return host.selectAutoPotSlot(); }
        void restore(final int token) { host.restoreAutoPotSlot(token); }
        @Override public void close() { host.close(); }
    }

    @Test void genuineLowHealthOriginalSourceCanUseExactlySelectedSplashPotion() {
        try (Fixture f = new Fixture()) {
            final int oldSlot = f.select();
            assertEquals(4, oldSlot);
            assertEquals(2, f.player.inventory.slot);
            assertTrue(f.host.mayExecuteAutoPotUse(oldSlot));
            assertTrue(f.host.mayDispatchAutoPotNativeUse(oldSlot));
            f.restore(oldSlot);
            assertEquals(4, f.player.inventory.slot);
            assertFalse(f.host.mayDispatchAutoPotNativeUse(oldSlot));
        }
    }

    @Test void readOnlyOriginalGivePotionDiscoveryDoesNotInventLowHealth() {
        try (Fixture f = new Fixture()) {
            f.player.health = 20F;
            final int original = f.select();
            assertEquals(4, original);
            assertTrue(f.host.mayExecuteAutoPotUse(original));
            assertFalse(f.host.mayDispatchAutoPotNativeUse(original));
            f.restore(original);
        }
    }

    @Test void recoveringHealthOrInvalidCurrentHealthRejectsPotionBeforeClick() {
        try (Fixture f = new Fixture()) {
            final int original = f.select();
            assertEquals(4, original);
            f.player.health = 16F;
            assertFalse(f.host.mayDispatchAutoPotNativeUse(original));
            f.player.health = Float.NaN;
            assertFalse(f.host.mayDispatchAutoPotNativeUse(original));
            f.player.health = 0F;
            assertFalse(f.host.mayDispatchAutoPotNativeUse(original));
            f.player.health = 5F;
            assertTrue(f.host.mayDispatchAutoPotNativeUse(original));
            f.restore(original);
        }
    }

    @Test void manualPhysicalRightClickCannotBeBorrowedBySyntheticPotion() {
        try (Fixture f = new Fixture()) {
            final int original = f.select();
            assertEquals(4, original);
            f.host.inputState().pointerButton(
                    Minecraft189ClickRateTracker.RIGHT_BUTTON, true);
            assertFalse(f.host.mayDispatchAutoPotNativeUse(original));
            f.host.inputState().pointerButton(
                    Minecraft189ClickRateTracker.RIGHT_BUTTON, false);
            assertTrue(f.host.mayDispatchAutoPotNativeUse(original));
            f.restore(original);
        }
    }

    @Test void reentrantHealthGetterRecoveryRejectsStaleNativeUse() {
        try (Fixture f = new Fixture()) {
            final int original = f.select();
            assertEquals(4, original);
            f.player.onMaxRead = () -> f.player.health = 20F;
            assertFalse(f.host.mayDispatchAutoPotNativeUse(original));
            f.restore(original);
        }
    }

    @Test void reentrantOriginalPlayerChangeDuringHealthGetterRejectsNativeUse() {
        try (Fixture f = new Fixture()) {
            final int original = f.select();
            assertEquals(4, original);
            final Player other = new Player();
            f.player.onHealthRead = () -> {
                f.host.playerHotbarSlot(other);
                f.host.playerHeldItem(other);
            };
            assertFalse(f.host.mayDispatchAutoPotNativeUse(original));
            f.restore(original);
        }
    }

    @Test void nativePotionItemGetterGuiOrHealthChangeVetoesFinalNativeClick() {
        try (Fixture f = new Fixture()) {
            final int original = f.select();
            assertEquals(4, original);
            f.player.potion().onTypeRead = () ->
                    f.host.key(LegacyKeyboardCodes.RIGHT_SHIFT, (char) 0,
                            true, false, true, false, false);
            assertFalse(f.host.mayDispatchAutoPotNativeUse(original));
            f.restore(original);
        }
        try (Fixture f = new Fixture()) {
            final int original = f.select();
            assertEquals(4, original);
            f.player.potion().onTypeRead = () -> f.player.health = 19F;
            assertFalse(f.host.mayDispatchAutoPotNativeUse(original));
            f.restore(original);
        }
    }

    @Test void originalPotionThresholdEditsDoNotAuthorizeAnOldItemLease() {
        try (Fixture f = new Fixture()) {
            final int original = f.select();
            assertEquals(4, original);
            f.host.featureCatalog().autoPot()
                    .healthThresholdSetting().set(40D);
            assertFalse(f.host.mayDispatchAutoPotNativeUse(original));
            f.restore(original);
        }
    }

    @Test void nativeHealthPredicateRejectsFullDeadOrUnknownAndAdmitsLowHealth() {
        final Minecraft189AutoPotModule module = new Minecraft189AutoPotModule();
        assertFalse(module.permitsLiveNativeHealth(5F, 20F));
        module.onEnable();
        assertFalse(module.permitsLiveNativeHealth(20F, 20F));
        assertFalse(module.permitsLiveNativeHealth(0F, 20F));
        assertFalse(module.permitsLiveNativeHealth(Float.NaN, 20F));
        assertFalse(module.permitsLiveNativeHealth(4F, Float.POSITIVE_INFINITY));
        assertFalse(module.permitsLiveNativeHealth(15F, 20F));
        assertTrue(module.permitsLiveNativeHealth(5F, 20F));
        module.healthThresholdSetting().set(20D);
        assertFalse(module.permitsLiveNativeHealth(5F, 20F));
        module.onDisable();
        assertFalse(module.permitsLiveNativeHealth(2F, 20F));
    }
}
