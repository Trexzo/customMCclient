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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189RaycastGuiFocusTest {
    @Test
    void originalGuiRetainsVanillaPickDistanceBranchAndPlayerBorder() {
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
            final Minecraft189ReachModule reach =
                    runtime.featureCatalog().reach();
            final Minecraft189HitboxModule hitbox =
                    runtime.featureCatalog().hitbox();
            reach.distanceSetting().set(4.2D);
            hitbox.extraBorderSetting().set(0.2D);
            controller.enable(Minecraft189ReachModule.ID);
            controller.enable(Minecraft189HitboxModule.ID);
            assertEquals(4.2F, runtime.raycastBlockDistance(4.5F), 0.00001F);
            assertTrue(runtime.raycastExtendedBranch(false));
            assertEquals(4.2D, runtime.raycastExtendedDistance(6.0D), 0.0D);
            assertEquals(0.3F, runtime.raycastHitboxBorder(true, 0.1F),
                    0.00001F);
            assertEquals(0.1F, runtime.raycastHitboxBorder(false, 0.1F),
                    0.0F);

            runtime.clickGuiToggleController().key(
                    Minecraft189ClickGuiToggleController.DEFAULT_TOGGLE_KEY,
                    true, false);
            assertTrue(runtime.clickGuiRuntime().coreRuntime()
                    .model().snapshot().open());
            assertTrue(reach.active());
            assertTrue(hitbox.active());
            assertEquals(4.5F, runtime.raycastBlockDistance(4.5F), 0.0F);
            assertFalse(runtime.raycastExtendedBranch(false));
            assertTrue(runtime.raycastExtendedBranch(true),
                    "native true extended branch is never revoked");
            assertEquals(6.0D, runtime.raycastExtendedDistance(6.0D), 0.0D);
            assertEquals(0.1F, runtime.raycastHitboxBorder(true, 0.1F),
                    0.0F);
            assertEquals(Float.floatToRawIntBits(-0.0F),
                    Float.floatToRawIntBits(runtime.raycastBlockDistance(-0.0F)));
            assertEquals(Double.doubleToRawLongBits(-0.0D),
                    Double.doubleToRawLongBits(runtime.raycastExtendedDistance(-0.0D)));
            assertEquals(Float.POSITIVE_INFINITY,
                    runtime.raycastHitboxBorder(true, Float.POSITIVE_INFINITY));

            runtime.clickGuiToggleController().key(
                    Minecraft189ClickGuiToggleController.DEFAULT_TOGGLE_KEY,
                    true, false);
            assertFalse(runtime.clickGuiRuntime().coreRuntime()
                    .model().snapshot().open());
            assertEquals(4.2F, runtime.raycastBlockDistance(4.5F), 0.00001F);
            assertTrue(runtime.raycastExtendedBranch(false));
            assertEquals(0.3F, runtime.raycastHitboxBorder(true, 0.1F),
                    0.00001F);

            controller.disable(Minecraft189ReachModule.ID);
            controller.disable(Minecraft189HitboxModule.ID);
            assertEquals(4.5F, runtime.raycastBlockDistance(4.5F), 0.0F);
            assertFalse(runtime.raycastExtendedBranch(false));
            assertEquals(0.1F, runtime.raycastHitboxBorder(true, 0.1F),
                    0.0F);
        } finally {
            runtime.close();
        }
        assertNull(settings.find(Minecraft189ReachModule.DISTANCE));
        assertNull(settings.find(Minecraft189HitboxModule.EXTRA_BORDER));
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
