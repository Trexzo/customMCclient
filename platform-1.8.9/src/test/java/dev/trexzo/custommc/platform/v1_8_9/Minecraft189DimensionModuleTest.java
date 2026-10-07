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

final class Minecraft189DimensionModuleTest {
    @Test
    void dimensionHudReadsLiveDimensionAndPersistsPosition() {
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
            final Minecraft189DimensionModule dimension =
                    runtime.featureCatalog()
                            .dimension();

            assertEquals(
                    ModuleState.DISABLED,
                    controller.stateOf(
                            Minecraft189DimensionModule.ID));

            dimension.xSetting().set(128);
            dimension.ySetting().set(388);
            runtime.playerDimension(
                    playerInDimension(
                            -1));

            controller.enable(
                    Minecraft189DimensionModule.ID);
            assertTrue(
                    dimension.renderPassInstalled());

            runtime.renderHud(
                    0L,
                    0.0F);

            assertEquals(
                    "Dimension: Nether (-1)",
                    host.lastText);
            assertEquals(
                    128.0F,
                    host.lastX);
            assertEquals(
                    388.0F,
                    host.lastY);
            assertEquals(
                    "128",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189DimensionModule.X_SETTING_ID));
            assertEquals(
                    "388",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189DimensionModule.Y_SETTING_ID));

            final Minecraft189PlayerDimensionState.Snapshot snapshot =
                    runtime.playerDimensionState()
                            .snapshot();
            assertTrue(
                    snapshot.available());
            assertEquals(
                    -1,
                    snapshot.dimensionId());

            runtime.playerDimension(
                    playerInDimension(
                            0));
            runtime.renderHud(
                    1L,
                    0.0F);
            assertEquals(
                    "Dimension: Overworld (0)",
                    host.lastText);

            runtime.playerDimension(
                    playerInDimension(
                            1));
            runtime.renderHud(
                    2L,
                    0.0F);
            assertEquals(
                    "Dimension: End (1)",
                    host.lastText);

            runtime.playerDimension(
                    playerInDimension(
                            7));
            runtime.renderHud(
                    3L,
                    0.0F);
            assertEquals(
                    "Dimension: 7",
                    host.lastText);

            host.lastText = null;
            runtime.playerDimension(
                    null);
            runtime.renderHud(
                    4L,
                    0.0F);
            assertNull(
                    host.lastText);
            assertFalse(
                    runtime.playerDimensionState()
                            .snapshot()
                            .available());

            controller.disable(
                    Minecraft189DimensionModule.ID);
            assertFalse(
                    dimension.renderPassInstalled());
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189DimensionModule.ID));
        assertNull(
                settings.find(
                        Minecraft189DimensionModule.X_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189DimensionModule.Y_SETTING_ID));
    }

    private static Minecraft189PlayerDimensionAccess playerInDimension(
            final int dimensionId) {
        return new Minecraft189PlayerDimensionAccess() {
            @Override
            public int customMcDimension() {
                return dimensionId;
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
