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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189ClickRateIntegrationTest {
    @Test
    void hostCountsOnlyMouseRisingEdgesAndOwnsCpsLifetime() {
        final ModuleRegistry modules =
                new ModuleRegistry();
        final ModuleController controller =
                new ModuleController(modules);
        final SettingRegistry settings =
                new SettingRegistry();
        final ServiceRegistry services =
                new ServiceRegistry();
        services.register(
                RenderPipeline.class,
                new RenderPipeline());

        final Minecraft189Platform platform =
                new Minecraft189Platform();
        platform.attach(
                new PlatformContext(
                        new EventBus(),
                        modules,
                        controller,
                        services));

        final Minecraft189HostRuntime runtime =
                Minecraft189HostRuntime.install(
                        platform,
                        new ModulePresentationRegistry(),
                        new ModuleCategoryRegistry(),
                        new ModuleSettingRegistry(
                                modules,
                                settings),
                        null,
                        null,
                        settings,
                        new SettingPresentationRegistry(),
                        new NoOpHost());

        final Minecraft189ClickRateTracker tracker =
                runtime.clickRateTracker();

        assertTrue(
                modules.find(
                        Minecraft189CpsModule.ID)
                        != null);
        assertTrue(
                settings.find(
                        Minecraft189CpsModule.X_SETTING_ID)
                        != null);
        assertTrue(
                settings.find(
                        Minecraft189CpsModule.Y_SETTING_ID)
                        != null);

        runtime.pointerButton(
                0,
                0,
                0,
                true);
        runtime.pointerButton(
                0,
                0,
                0,
                true);

        assertEquals(
                1,
                tracker.snapshot()
                        .left());

        runtime.pointerButton(
                0,
                0,
                0,
                false);
        runtime.pointerButton(
                0,
                0,
                0,
                true);
        runtime.pointerButton(
                0,
                0,
                1,
                true);

        Minecraft189ClickRateTracker.Rates rates =
                tracker.snapshot();
        assertEquals(
                2,
                rates.left());
        assertEquals(
                1,
                rates.right());

        runtime.close();

        rates = tracker.snapshot();
        assertEquals(
                0,
                rates.left());
        assertEquals(
                0,
                rates.right());
        assertNull(
                modules.find(
                        Minecraft189CpsModule.ID));
        assertNull(
                settings.find(
                        Minecraft189CpsModule.X_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189CpsModule.Y_SETTING_ID));
    }

    private static final class NoOpHost
            implements LegacyUiHostCallbacks {
        @Override
        public int framebufferWidth() {
            return 1280;
        }

        @Override
        public int framebufferHeight() {
            return 720;
        }

        @Override
        public float uiScale() {
            return 1.0F;
        }

        @Override
        public void beginUi(
                final UiViewport viewport) {
        }

        @Override
        public void fillRect(
                final float x,
                final float y,
                final float width,
                final float height,
                final int argb) {
        }

        @Override
        public void fillRoundedRect(
                final float x,
                final float y,
                final float width,
                final float height,
                final float radius,
                final int argb) {
        }

        @Override
        public void strokeRect(
                final float x,
                final float y,
                final float width,
                final float height,
                final float thickness,
                final int argb) {
        }

        @Override
        public void pushClip(
                final float x,
                final float y,
                final float width,
                final float height) {
        }

        @Override
        public void popClip() {
        }

        @Override
        public void drawText(
                final UiFontHandle font,
                final float x,
                final float y,
                final String text,
                final int argb) {
        }

        @Override
        public void endUi() {
        }
    }
}
