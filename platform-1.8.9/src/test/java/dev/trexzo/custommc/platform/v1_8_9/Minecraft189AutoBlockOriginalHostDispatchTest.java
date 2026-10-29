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

/** Native held-item boundary between AutoBlock reservation and rightClickMouse. */
final class Minecraft189AutoBlockOriginalHostDispatchTest {
    private static final class Item implements Minecraft189ItemStackAccess {
        boolean sword = true;
        Runnable onIsSword;
        @Override public String customMcDisplayName() { return "Real sword"; }
        @Override public int customMcStackSize() { return 1; }
        @Override public int customMcItemDamage() { return 0; }
        @Override public int customMcMaxDamage() { return 100; }
        @Override public boolean customMcIsSword() {
            final Runnable cb = onIsSword;
            onIsSword = null;
            if (cb != null) cb.run();
            return sword;
        }
    }

    private static final class Player implements Minecraft189PlayerHeldItemAccess {
        Item held = new Item();
        Runnable onHeldRead;
        @Override public Minecraft189ItemStackAccess customMcHeldItem() {
            final Runnable cb = onHeldRead;
            onHeldRead = null;
            if (cb != null) cb.run();
            return held;
        }
    }

    private static final class NoOpHost implements LegacyUiHostCallbacks {
        @Override public int framebufferWidth() { return 1280; }
        @Override public int framebufferHeight() { return 720; }
        @Override public float uiScale() { return 1F; }
        @Override public void beginUi(UiViewport viewport) {}
        @Override public void fillRect(float x,float y,float w,float h,int color) {}
        @Override public void fillRoundedRect(float x,float y,float w,float h,float r,int color) {}
        @Override public void strokeRect(float x,float y,float w,float h,float t,int color) {}
        @Override public void pushClip(float x,float y,float w,float h) {}
        @Override public void popClip() {}
        @Override public void drawText(UiFontHandle font,float x,float y,String text,int color) {}
        @Override public void endUi() {}
    }

    private static final class Fixture implements AutoCloseable {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final ServiceRegistry services = new ServiceRegistry();
        final Minecraft189HostRuntime runtime;
        Fixture() {
            services.register(RenderPipeline.class, new RenderPipeline());
            final Minecraft189Platform platform = new Minecraft189Platform();
            platform.attach(new PlatformContext(
                    new EventBus(), modules, controller, services));
            runtime = Minecraft189HostRuntime.install(
                    platform, new ModulePresentationRegistry(),
                    new ModuleCategoryRegistry(),
                    new ModuleSettingRegistry(modules, settings),
                    null, null, settings,
                    new SettingPresentationRegistry(), new NoOpHost());
            controller.enable(Minecraft189AutoBlockModule.ID);
        }
        @Override public void close() { runtime.close(); }
    }

    @Test void realHeldSwordAllowsExactPendingOriginalRightClick() {
        try (Fixture f = new Fixture()) {
            final Player p = new Player();
            f.runtime.playerHeldItem(p);
            final long token = f.runtime.beginAutoBlockNativeUse();
            assertTrue(token > 0L);
            assertTrue(f.runtime.mayDispatchAutoBlockNativeUse(token));
            assertTrue(f.runtime.mayDispatchAutoBlockNativeUse(token));
            assertFalse(f.runtime.featureCatalog().autoBlock().ownsBlock());
            f.runtime.cancelAutoBlockNativeUse(token);
            assertFalse(f.runtime.mayDispatchAutoBlockNativeUse(token));
        }
    }

    @Test void originalSwordReplacedAfterReservationIsVetoedPreClick() {
        try (Fixture f = new Fixture()) {
            final Player p = new Player();
            f.runtime.playerHeldItem(p);
            final long token = f.runtime.beginAutoBlockNativeUse();
            p.held.sword = false;
            assertFalse(f.runtime.mayDispatchAutoBlockNativeUse(token));
        }
    }

    @Test void nestedNewHeldItemSourceWhileReadingSwordCannotBorrowOldToken() {
        try (Fixture f = new Fixture()) {
            final Player p = new Player(), other = new Player();
            f.runtime.playerHeldItem(p);
            final long token = f.runtime.beginAutoBlockNativeUse();
            // A genuine heldItem callback from within original native
            // isSword must revoke the old owner even if it also holds sword.
            p.held.onIsSword = () -> f.runtime.playerHeldItem(other);
            assertFalse(f.runtime.mayDispatchAutoBlockNativeUse(token));
            assertFalse(f.runtime.mayDispatchAutoBlockNativeUse(token));
        }
    }

    @Test void originalSwordGetterPolicyChangeRevokesPendingRightClick() {
        try (Fixture f = new Fixture()) {
            final Player p = new Player();
            f.runtime.playerHeldItem(p);
            final long token = f.runtime.beginAutoBlockNativeUse();
            p.held.onIsSword = () ->
                    f.runtime.featureCatalog().autoBlock().holdTicksSetting().set(4);
            assertFalse(f.runtime.mayDispatchAutoBlockNativeUse(token));
        }
    }

    @Test void originalSwordGetterOpeningClickGuiRejectsNativeDispatch() {
        try (Fixture f = new Fixture()) {
            final Player p = new Player();
            f.runtime.playerHeldItem(p);
            final long token = f.runtime.beginAutoBlockNativeUse();
            p.held.onIsSword = () -> f.runtime.key(
                    LegacyKeyboardCodes.RIGHT_SHIFT, (char) 0,
                    true, false, true, false, false);
            assertFalse(f.runtime.mayDispatchAutoBlockNativeUse(token));
            assertTrue(f.runtime.clickGuiRuntime().coreRuntime()
                    .model().snapshot().open());
        }
    }

    @Test void originalSwordGetterChangingModuleLifecycleRejectsNativeDispatch() {
        try (Fixture f = new Fixture()) {
            final Player p = new Player();
            f.runtime.playerHeldItem(p);
            final long token = f.runtime.beginAutoBlockNativeUse();
            p.onHeldRead = () -> {
                f.controller.disable(Minecraft189AutoBlockModule.ID);
                f.controller.enable(Minecraft189AutoBlockModule.ID);
            };
            assertFalse(f.runtime.mayDispatchAutoBlockNativeUse(token));
        }
    }
}
