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

final class Minecraft189DirectionModuleTest {
    @Test
    void directionHudReadsLiveYawAndPersistsPosition() {
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
            final Minecraft189DirectionModule direction =
                    runtime.featureCatalog()
                            .direction();

            assertEquals(
                    ModuleState.DISABLED,
                    controller.stateOf(
                            Minecraft189DirectionModule.ID));

            direction.xSetting().set(24);
            direction.ySetting().set(180);
            runtime.playerRotation(
                    () -> 91.2F);

            controller.enable(
                    Minecraft189DirectionModule.ID);
            assertTrue(
                    direction.renderPassInstalled());

            runtime.renderHud(
                    0L,
                    0.0F);

            assertEquals(
                    "Direction: W (91\u00B0)",
                    host.lastText);
            assertEquals(
                    24.0F,
                    host.lastX);
            assertEquals(
                    180.0F,
                    host.lastY);
            assertEquals(
                    "24",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189DirectionModule.X_SETTING_ID));
            assertEquals(
                    "180",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189DirectionModule.Y_SETTING_ID));

            assertEquals(
                    "Direction: E (270\u00B0)",
                    Minecraft189DirectionModule
                            .textForYaw(
                                    -89.6F));

            host.lastText = null;
            runtime.playerRotation(null);
            runtime.renderHud(
                    1L,
                    0.0F);
            assertNull(host.lastText);

            controller.disable(
                    Minecraft189DirectionModule.ID);
            assertFalse(
                    direction.renderPassInstalled());
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189DirectionModule.ID));
        assertNull(
                settings.find(
                        Minecraft189DirectionModule.X_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189DirectionModule.Y_SETTING_ID));
    }

    @Test
    void rotationStateRejectsNonFiniteYaw() {
        final Minecraft189PlayerRotationState state =
                new Minecraft189PlayerRotationState();

        assertThrows(
                IllegalArgumentException.class,
                () -> state.update(
                        Float.NaN));
        assertThrows(
                IllegalArgumentException.class,
                () -> state.update(
                        Float.POSITIVE_INFINITY));
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
