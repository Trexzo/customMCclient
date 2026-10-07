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

final class Minecraft189ServerModuleTest {
    @Test
    void serverHudReadsLiveAddressAndPersistsPosition() {
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
            final Minecraft189ServerModule server =
                    runtime.featureCatalog()
                            .server();

            assertEquals(
                    ModuleState.DISABLED,
                    controller.stateOf(
                            Minecraft189ServerModule.ID));

            server.xSetting().set(80);
            server.ySetting().set(324);
            runtime.serverAddress(
                    serverData(
                            " play.example.net:25565 "));

            controller.enable(
                    Minecraft189ServerModule.ID);
            assertTrue(
                    server.renderPassInstalled());

            runtime.renderHud(
                    0L,
                    0.0F);

            assertEquals(
                    "Server: play.example.net:25565",
                    host.lastText);
            assertEquals(
                    80.0F,
                    host.lastX);
            assertEquals(
                    324.0F,
                    host.lastY);
            assertEquals(
                    "80",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189ServerModule.X_SETTING_ID));
            assertEquals(
                    "324",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189ServerModule.Y_SETTING_ID));

            final Minecraft189ServerAddressState.Snapshot snapshot =
                    runtime.serverAddressState()
                            .snapshot();
            assertTrue(
                    snapshot.available());
            assertEquals(
                    "play.example.net:25565",
                    snapshot.address());

            host.lastText = null;
            runtime.serverAddress(
                    serverData("   "));
            runtime.renderHud(
                    1L,
                    0.0F);
            assertNull(
                    host.lastText);
            assertFalse(
                    runtime.serverAddressState()
                            .snapshot()
                            .available());

            runtime.serverAddress(
                    serverData(
                            "mc.example.org"));
            assertTrue(
                    runtime.serverAddressState()
                            .snapshot()
                            .available());
            runtime.serverAddress(null);
            assertFalse(
                    runtime.serverAddressState()
                            .snapshot()
                            .available());

            controller.disable(
                    Minecraft189ServerModule.ID);
            assertFalse(
                    server.renderPassInstalled());
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189ServerModule.ID));
        assertNull(
                settings.find(
                        Minecraft189ServerModule.X_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189ServerModule.Y_SETTING_ID));
    }

    @Test
    void serverAddressStateRejectsInvalidAddresses() {
        final Minecraft189ServerAddressState state =
                new Minecraft189ServerAddressState();

        assertThrows(
                NullPointerException.class,
                () -> state.update(null));
        assertThrows(
                IllegalArgumentException.class,
                () -> state.update("   "));
    }

    private static Minecraft189ServerDataAccess serverData(
            final String address) {
        return new Minecraft189ServerDataAccess() {
            @Override
            public String customMcServerAddress() {
                return address;
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
