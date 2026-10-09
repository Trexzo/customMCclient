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

final class Minecraft189PingModuleTest {
    @Test
    void pingHudReadsLiveValueAndPersistsPosition() {
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
            final Minecraft189PingModule ping =
                    runtime.featureCatalog()
                            .ping();

            assertEquals(
                    ModuleState.DISABLED,
                    controller.stateOf(
                            Minecraft189PingModule.ID));

            ping.xSetting().set(72);
            ping.ySetting().set(308);
            runtime.playerPing(
                    new Minecraft189PlayerPingAccess() {
                        @Override
                        public int customMcPingMilliseconds() {
                            return 57;
                        }
                    });

            controller.enable(
                    Minecraft189PingModule.ID);
            assertTrue(
                    ping.renderPassInstalled());

            runtime.renderHud(
                    0L,
                    0.0F);

            assertEquals(
                    "Ping: 57 ms",
                    host.lastText);
            assertEquals(
                    72.0F,
                    host.lastX);
            assertEquals(
                    308.0F,
                    host.lastY);
            assertEquals(
                    "72",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189PingModule.X_SETTING_ID));
            assertEquals(
                    "308",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189PingModule.Y_SETTING_ID));

            final Minecraft189PlayerPingState.Snapshot snapshot =
                    runtime.playerPingState()
                            .snapshot();
            assertTrue(
                    snapshot.available());
            assertEquals(
                    57,
                    snapshot.milliseconds());

            host.lastText = null;
            runtime.playerPing(
                    new Minecraft189PlayerPingAccess() {
                        @Override
                        public int customMcPingMilliseconds() {
                            return -1;
                        }
                    });
            runtime.renderHud(
                    1L,
                    0.0F);
            assertNull(
                    host.lastText);
            assertFalse(
                    runtime.playerPingState()
                            .snapshot()
                            .available());

            controller.disable(
                    Minecraft189PingModule.ID);
            assertFalse(
                    ping.renderPassInstalled());
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189PingModule.ID));
        assertNull(
                settings.find(
                        Minecraft189PingModule.X_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189PingModule.Y_SETTING_ID));
    }

    @Test
    void pingStateRejectsNegativeLatency() {
        final Minecraft189PlayerPingState state =
                new Minecraft189PlayerPingState();

        assertThrows(
                IllegalArgumentException.class,
                () -> state.update(-1));
    }

    @Test
    void optionalLatencyColorsRespectThresholdsAndDefaultWhite() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final ServiceRegistry services = new ServiceRegistry();
        services.register(RenderPipeline.class, new RenderPipeline());
        final Minecraft189Platform platform = new Minecraft189Platform();
        platform.attach(new PlatformContext(new EventBus(), modules,
                controller, services));
        final RecordingHost host = new RecordingHost();
        final Minecraft189HostRuntime runtime = Minecraft189HostRuntime.install(
                platform, new ModulePresentationRegistry(),
                new ModuleCategoryRegistry(),
                new ModuleSettingRegistry(modules, settings),
                null, null, settings, new SettingPresentationRegistry(), host);
        try {
            final Minecraft189PingModule module = runtime.featureCatalog().ping();
            assertFalse(module.colorByLatencySetting().get().booleanValue());
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189PingModule.COLOR_BY_LATENCY_SETTING_ID));
            controller.enable(Minecraft189PingModule.ID);
            runtime.playerPing(() -> 500);
            runtime.renderHud(1L, 0.0F);
            assertEquals(0xFFFFFFFF, host.lastColor); // Default unchanged.

            module.colorByLatencySetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189PingModule.COLOR_BY_LATENCY_SETTING_ID));
            final int[] samples = {0, 99, 100, 199, 200, 1000};
            final int[] expected = {0xFF55FF55, 0xFF55FF55, 0xFFFFAA00,
                    0xFFFFAA00, 0xFFFF5555, 0xFFFF5555};
            for (int i = 0; i < samples.length; i++) {
                final int latency = samples[i];
                runtime.playerPing(() -> latency);
                runtime.renderHud(i + 2L, 0.0F);
                assertEquals(expected[i], host.lastColor);
                assertEquals("Ping: " + latency + " ms", host.lastText);
            }

            module.colorByLatencySetting().set(Boolean.FALSE);
            runtime.renderHud(20L, 0.0F);
            assertEquals(0xFFFFFFFF, host.lastColor);
            host.lastText = null;
            runtime.playerPing(() -> -1);
            runtime.renderHud(21L, 0.0F);
            assertNull(host.lastText); // Unknown ping is never drawn.
        } finally {
            runtime.close();
        }
        assertNull(settings.find(
                Minecraft189PingModule.COLOR_BY_LATENCY_SETTING_ID));
        assertNull(modules.find(Minecraft189PingModule.ID));
    }

    private static final class RecordingHost
            implements LegacyUiHostCallbacks {
        private String lastText;
        private float lastX;
        private float lastY;
        private int lastColor;

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
            lastColor = argb;
        }

        @Override
        public void endUi() {
        }
    }
}
