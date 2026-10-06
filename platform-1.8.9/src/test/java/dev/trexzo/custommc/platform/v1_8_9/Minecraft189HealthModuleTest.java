package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.event.EventBus;
import dev.trexzo.custommc.core.module.ModuleCategoryRegistry;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.module.ModuleState;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189HealthModuleTest {
    @Test
    void healthHudReadsLiveHealthAndPersistsPosition() {
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

        final RecordingHost host =
                new RecordingHost();
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
                        host);

        try {
            final Minecraft189HealthModule health =
                    runtime.featureCatalog()
                            .health();

            assertEquals(
                    ModuleState.DISABLED,
                    controller.stateOf(
                            Minecraft189HealthModule.ID));

            health.xSetting().set(32);
            health.ySetting().set(196);
            runtime.playerHealth(
                    new Minecraft189PlayerHealthAccess() {
                        @Override
                        public float customMcHealth() {
                            return 17.5F;
                        }

                        @Override
                        public float customMcMaxHealth() {
                            return 20.0F;
                        }
                    });

            controller.enable(
                    Minecraft189HealthModule.ID);
            assertTrue(
                    health.renderPassInstalled());

            runtime.renderHud(
                    0L,
                    0.0F);

            assertEquals(
                    "Health: 17.5 / 20.0",
                    host.lastText);
            assertEquals(
                    32.0F,
                    host.lastX);
            assertEquals(
                    196.0F,
                    host.lastY);
            assertEquals(
                    "32",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189HealthModule.X_SETTING_ID));
            assertEquals(
                    "196",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189HealthModule.Y_SETTING_ID));

            host.lastText = null;
            runtime.playerHealth(null);
            runtime.renderHud(
                    1L,
                    0.0F);
            assertNull(host.lastText);

            controller.disable(
                    Minecraft189HealthModule.ID);
            assertFalse(
                    health.renderPassInstalled());
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189HealthModule.ID));
        assertNull(
                settings.find(
                        Minecraft189HealthModule.X_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189HealthModule.Y_SETTING_ID));
    }

    @Test
    void healthStateRejectsInvalidValues() {
        final Minecraft189PlayerHealthState state =
                new Minecraft189PlayerHealthState();

        assertThrows(
                IllegalArgumentException.class,
                () -> state.update(
                        Float.NaN,
                        20.0F));
        assertThrows(
                IllegalArgumentException.class,
                () -> state.update(
                        10.0F,
                        Float.POSITIVE_INFINITY));
        assertThrows(
                IllegalArgumentException.class,
                () -> state.update(
                        10.0F,
                        0.0F));
    }

    private static final class RecordingHost
            implements LegacyUiHostCallbacks {
        private String lastText;
        private float lastX;
        private float lastY;

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
            lastText = text;
            lastX = x;
            lastY = y;
        }

        @Override
        public void endUi() {
        }
    }
}
