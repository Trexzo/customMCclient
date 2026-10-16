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

final class Minecraft189CombatSlotHostLeaseTest {
    @Test void fixedCombatSlotRestoresCapturedSourceAfterNestedRespawnCallback() {
        final Harness harness = new Harness();
        try {
            harness.controller.enable(Minecraft189CombatSlotModule.ID);
            harness.runtime.featureCatalog().combatSlot()
                    .preferredSlotSetting().set(6);
            final Inventory old = new Inventory();
            final Inventory replacement = new Inventory();
            old.selected = 3;
            replacement.selected = 2;

            harness.runtime.playerHotbarSlot(new Player(old));
            assertEquals(3, harness.runtime.selectCombatSlotBeforeSyntheticClick(true));
            assertEquals(5, old.selected);
            harness.runtime.playerHotbarSlot(new Player(replacement));
            assertEquals(-1, harness.runtime.selectCombatSlotBeforeSyntheticClick(true),
                    "nested native callback cannot overwrite first lease owner");
            harness.runtime.restoreCombatSlotAfterSyntheticClick(-1);
            assertEquals(5, old.selected,
                    "a nested failure must not consume the outer lease");
            harness.runtime.restoreCombatSlotAfterSyntheticClick(3);
            assertEquals(3, old.selected,
                    "original source currentItem is restored in old player");
            assertEquals(2, replacement.selected,
                    "new local player cannot be affected by old click finally");

            harness.runtime.playerHotbarSlot(new Player(replacement));
            assertEquals(2, harness.runtime.selectCombatSlotBeforeSyntheticClick(true));
            harness.runtime.restoreCombatSlotAfterSyntheticClick(2);
            assertEquals(2, replacement.selected);
        } finally {
            harness.close();
        }
    }

    @Test void autoWeaponRemainsTheCapturedOwnerEvenIfDisabledDuringRespawn() {
        final Harness harness = new Harness();
        try {
            final Inventory old = new Inventory();
            final Inventory replacement = new Inventory();
            old.selected = 3;
            replacement.selected = 2;
            old.items[6] = new Item(true, 8.0F);
            replacement.items[7] = new Item(true, 9.0F);
            harness.controller.enable(Minecraft189AutoWeaponModule.ID);
            harness.controller.enable(Minecraft189CombatSlotModule.ID);
            harness.runtime.featureCatalog().combatSlot()
                    .preferredSlotSetting().set(8);

            harness.runtime.playerHotbarSlot(new Player(old));
            assertEquals(3, harness.runtime.selectCombatSlotBeforeSyntheticClick(true));
            assertEquals(6, old.selected,
                    "verified AutoWeapon sword owner outranks fixed CombatSlot");
            harness.runtime.playerHotbarSlot(new Player(replacement));
            harness.controller.disable(Minecraft189AutoWeaponModule.ID);
            assertEquals(-1, harness.runtime.selectCombatSlotBeforeSyntheticClick(true));
            harness.runtime.restoreCombatSlotAfterSyntheticClick(-1);
            harness.runtime.restoreCombatSlotAfterSyntheticClick(3);
            assertEquals(3, old.selected,
                    "disable cannot redirect AutoWeapon finally into CombatSlot");
            assertEquals(2, replacement.selected);
            assertEquals(-1, harness.runtime.selectCombatSlotBeforeSyntheticClick(false),
                    "fixed CombatSlot still requires a verified player-hit signal");
            assertEquals(2, replacement.selected);
        } finally {
            harness.close();
        }
    }

    private static final class Harness {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final Minecraft189HostRuntime runtime;
        Harness() {
            final ServiceRegistry services = new ServiceRegistry();
            services.register(RenderPipeline.class, new RenderPipeline());
            final Minecraft189Platform platform = new Minecraft189Platform();
            platform.attach(new PlatformContext(new EventBus(),
                    modules, controller, services));
            runtime = Minecraft189HostRuntime.install(platform,
                    new ModulePresentationRegistry(),
                    new ModuleCategoryRegistry(),
                    new ModuleSettingRegistry(modules, settings),
                    null, null, settings, new SettingPresentationRegistry(),
                    new NoOpHost());
        }
        void close() { runtime.close(); }
    }

    private static final class Inventory
            implements Minecraft189InventoryHotbarItemsAccess {
        int selected;
        final Minecraft189ItemStackAccess[] items =
                new Minecraft189ItemStackAccess[9];
        @Override public int customMcSelectedHotbarSlot() { return selected; }
        @Override public void customMcSetSelectedHotbarSlot(int value) {
            selected = value;
        }
        @Override public Minecraft189ItemStackAccess[] customMcHotbarItems() {
            return items;
        }
    }

    private static final class Player implements Minecraft189PlayerInventoryAccess {
        final Inventory inventory;
        Player(Inventory inventory) { this.inventory = inventory; }
        @Override public Minecraft189InventoryHotbarAccess customMcInventory() {
            return inventory;
        }
    }

    private static final class Item implements Minecraft189ItemStackAccess {
        final boolean sword;
        final float damage;
        Item(boolean sword, float damage) { this.sword = sword; this.damage = damage; }
        @Override public String customMcDisplayName() { return "source"; }
        @Override public int customMcStackSize() { return 1; }
        @Override public int customMcItemDamage() { return 0; }
        @Override public int customMcMaxDamage() { return 0; }
        @Override public boolean customMcIsSword() { return sword; }
        @Override public float customMcSwordBaseDamage() { return damage; }
    }

    private static final class NoOpHost implements LegacyUiHostCallbacks {
        @Override public int framebufferWidth() { return 1280; }
        @Override public int framebufferHeight() { return 720; }
        @Override public float uiScale() { return 1.0F; }
        @Override public void beginUi(UiViewport viewport) {}
        @Override public void fillRect(float x,float y,float w,float h,int c) {}
        @Override public void fillRoundedRect(float x,float y,float w,float h,float r,int c) {}
        @Override public void strokeRect(float x,float y,float w,float h,float thickness,int c) {}
        @Override public void pushClip(float x,float y,float w,float h) {}
        @Override public void popClip() {}
        @Override public void drawText(UiFontHandle f,float x,float y,String s,int c) {}
        @Override public void endUi() {}
    }
}
