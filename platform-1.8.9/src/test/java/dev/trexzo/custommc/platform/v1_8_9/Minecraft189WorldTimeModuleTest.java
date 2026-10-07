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

final class Minecraft189WorldTimeModuleTest {
    @Test
    void worldTimeHudReadsLiveTimeAndPersistsPosition() {
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
            final Minecraft189WorldTimeModule worldTime =
                    runtime.featureCatalog()
                            .worldTime();

            assertEquals(
                    ModuleState.DISABLED,
                    controller.stateOf(
                            Minecraft189WorldTimeModule.ID));

            worldTime.xSetting().set(96);
            worldTime.ySetting().set(356);
            runtime.worldTime(
                    worldAt(
                            6000L));

            controller.enable(
                    Minecraft189WorldTimeModule.ID);
            assertTrue(
                    worldTime.renderPassInstalled());

            runtime.renderHud(
                    0L,
                    0.0F);

            assertEquals(
                    "Time: 12:00",
                    host.lastText);
            assertEquals(
                    96.0F,
                    host.lastX);
            assertEquals(
                    356.0F,
                    host.lastY);
            assertEquals(
                    "96",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189WorldTimeModule.X_SETTING_ID));
            assertEquals(
                    "356",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189WorldTimeModule.Y_SETTING_ID));

            final Minecraft189WorldTimeState.Snapshot snapshot =
                    runtime.worldTimeState()
                            .snapshot();
            assertTrue(
                    snapshot.available());
            assertEquals(
                    6000L,
                    snapshot.worldTime());
            assertEquals(
                    6000L,
                    snapshot.timeOfDayTicks());
            assertEquals(
                    12,
                    snapshot.hour());
            assertEquals(
                    0,
                    snapshot.minute());

            runtime.worldTime(
                    worldAt(
                            18000L));
            runtime.renderHud(
                    1L,
                    0.0F);
            assertEquals(
                    "Time: 00:00",
                    host.lastText);

            host.lastText = null;
            runtime.worldTime(null);
            runtime.renderHud(
                    2L,
                    0.0F);
            assertNull(
                    host.lastText);
            assertFalse(
                    runtime.worldTimeState()
                            .snapshot()
                            .available());

            controller.disable(
                    Minecraft189WorldTimeModule.ID);
            assertFalse(
                    worldTime.renderPassInstalled());
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189WorldTimeModule.ID));
        assertNull(
                settings.find(
                        Minecraft189WorldTimeModule.X_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189WorldTimeModule.Y_SETTING_ID));
    }

    @Test
    void worldTimeStateNormalizesNegativeAndOverflowTimeOfDay() {
        final Minecraft189WorldTimeState state =
                new Minecraft189WorldTimeState();

        state.update(-1L);
        assertEquals(
                23999L,
                state.snapshot()
                        .timeOfDayTicks());

        state.update(24000L);
        assertEquals(
                0L,
                state.snapshot()
                        .timeOfDayTicks());
        assertEquals(
                6,
                state.snapshot()
                        .hour());
        assertEquals(
                0,
                state.snapshot()
                        .minute());

        state.update(Long.MIN_VALUE);
        assertTrue(
                state.snapshot()
                        .timeOfDayTicks() >= 0L);
        assertTrue(
                state.snapshot()
                        .timeOfDayTicks() < 24000L);
    }

    private static Minecraft189WorldTimeAccess worldAt(
            final long worldTime) {
        return new Minecraft189WorldTimeAccess() {
            @Override
            public long customMcWorldTime() {
                return worldTime;
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
