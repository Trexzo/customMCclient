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
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189WeatherModuleTest {
    @Test
    void weatherHudReadsLiveWeatherAndPersistsPosition() {
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
            final Minecraft189WeatherModule weather =
                    runtime.featureCatalog()
                            .weather();

            assertEquals(
                    ModuleState.DISABLED,
                    controller.stateOf(
                            Minecraft189WeatherModule.ID));

            weather.xSetting().set(144);
            weather.ySetting().set(404);
            runtime.worldWeather(
                    weather(
                            false,
                            false));

            controller.enable(
                    Minecraft189WeatherModule.ID);
            assertTrue(
                    weather.renderPassInstalled());

            runtime.renderHud(
                    0L,
                    0.0F);
            assertEquals(
                    "Weather: Clear",
                    host.lastText);
            assertEquals(
                    144.0F,
                    host.lastX);
            assertEquals(
                    404.0F,
                    host.lastY);
            assertEquals(
                    "144",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189WeatherModule.X_SETTING_ID));
            assertEquals(
                    "404",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189WeatherModule.Y_SETTING_ID));

            runtime.worldWeather(
                    weather(
                            true,
                            false));
            runtime.renderHud(
                    1L,
                    0.0F);
            assertEquals(
                    "Weather: Rain",
                    host.lastText);

            runtime.worldWeather(
                    weather(
                            true,
                            true));
            runtime.renderHud(
                    2L,
                    0.0F);
            assertEquals(
                    "Weather: Thunder",
                    host.lastText);

            runtime.worldWeather(
                    weather(
                            false,
                            true));
            runtime.renderHud(
                    3L,
                    0.0F);
            assertEquals(
                    "Weather: Thunder",
                    host.lastText);

            final Minecraft189WorldWeatherState.Snapshot snapshot =
                    runtime.worldWeatherState()
                            .snapshot();
            assertTrue(
                    snapshot.available());
            assertFalse(
                    snapshot.raining());
            assertTrue(
                    snapshot.thundering());

            host.lastText = null;
            runtime.worldWeather(
                    null);
            runtime.renderHud(
                    4L,
                    0.0F);
            assertNull(
                    host.lastText);
            assertFalse(
                    runtime.worldWeatherState()
                            .snapshot()
                            .available());

            controller.disable(
                    Minecraft189WeatherModule.ID);
            assertFalse(
                    weather.renderPassInstalled());
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189WeatherModule.ID));
        assertNull(
                settings.find(
                        Minecraft189WeatherModule.X_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189WeatherModule.Y_SETTING_ID));
    }

    private static Minecraft189WorldWeatherAccess weather(
            final boolean raining,
            final boolean thundering) {
        return new Minecraft189WorldWeatherAccess() {
            @Override
            public boolean customMcRaining() {
                return raining;
            }

            @Override
            public boolean customMcThundering() {
                return thundering;
            }
        };
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
