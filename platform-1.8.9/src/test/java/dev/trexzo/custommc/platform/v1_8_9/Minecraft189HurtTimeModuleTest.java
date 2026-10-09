package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.event.EventBus;
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
import java.util.ArrayList;
import java.util.List;

final class Minecraft189HurtTimeModuleTest {
    @Test
    void hurtTimeHudReadsLiveCounterAndPersistsPosition() {
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
                        new dev.trexzo.custommc.core.module.ModuleCategoryRegistry(),
                        new ModuleSettingRegistry(
                                modules,
                                settings),
                        null,
                        null,
                        settings,
                        new SettingPresentationRegistry(),
                        host);

        try {
            final Minecraft189HurtTimeModule hurtTime =
                    runtime.featureCatalog()
                            .hurtTime();

            assertEquals(
                    ModuleState.DISABLED,
                    controller.stateOf(
                            Minecraft189HurtTimeModule.ID));

            hurtTime.xSetting().set(36);
            hurtTime.ySetting().set(214);
            runtime.playerHurtTime(
                    new Minecraft189PlayerHurtTimeAccess() {
                        @Override
                        public int customMcHurtTime() {
                            return 7;
                        }
                    });

            controller.enable(
                    Minecraft189HurtTimeModule.ID);
            assertTrue(
                    hurtTime.renderPassInstalled());

            runtime.renderHud(
                    0L,
                    0.0F);

            assertEquals(
                    "Hurt Time: 7",
                    host.lastText);
            assertEquals(
                    36.0F,
                    host.lastX);
            assertEquals(
                    214.0F,
                    host.lastY);
            assertEquals(
                    "36",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189HurtTimeModule.X_SETTING_ID));
            assertEquals(
                    "214",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189HurtTimeModule.Y_SETTING_ID));

            host.lastText = null;
            runtime.playerHurtTime(null);
            runtime.renderHud(
                    1L,
                    0.0F);
            assertNull(host.lastText);

            controller.disable(
                    Minecraft189HurtTimeModule.ID);
            assertFalse(
                    hurtTime.renderPassInstalled());
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189HurtTimeModule.ID));
        assertNull(
                settings.find(
                        Minecraft189HurtTimeModule.X_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189HurtTimeModule.Y_SETTING_ID));
    }

    @Test
    void hurtTimeStateRejectsNegativeValues() {
        final Minecraft189PlayerHurtTimeState state =
                new Minecraft189PlayerHurtTimeState();

        assertThrows(
                IllegalArgumentException.class,
                () -> state.update(-1));
    }

    @Test
    void hurtTimeMeterIsOptInAndRendersOnlyCertifiedCounter() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final ServiceRegistry services = new ServiceRegistry();
        services.register(RenderPipeline.class, new RenderPipeline());
        final Minecraft189Platform platform = new Minecraft189Platform();
        platform.attach(new PlatformContext(
                new EventBus(), modules, controller, services));
        final RecordingHost host = new RecordingHost();
        final Minecraft189HostRuntime runtime = Minecraft189HostRuntime.install(
                platform, new ModulePresentationRegistry(),
                new dev.trexzo.custommc.core.module.ModuleCategoryRegistry(),
                new ModuleSettingRegistry(modules, settings),
                null, null, settings, new SettingPresentationRegistry(), host);
        try {
            final Minecraft189HurtTimeModule hurt = runtime.featureCatalog().hurtTime();
            assertFalse(hurt.showMeterSetting().get().booleanValue());
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189HurtTimeModule.SHOW_METER_SETTING_ID));
            assertEquals("10", settings.snapshotEncoded().get(
                    Minecraft189HurtTimeModule.METER_MAX_SETTING_ID));
            controller.enable(Minecraft189HurtTimeModule.ID);
            runtime.playerHurtTime(() -> 5);
            runtime.renderHud(0L, 0.0F);
            assertEquals("Hurt Time: 5", host.lastText);
            assertTrue(host.rectWidths.isEmpty()); // Default text-only parity.

            hurt.showMeterSetting().set(Boolean.TRUE);
            hurt.meterMaxSetting().set(20);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189HurtTimeModule.SHOW_METER_SETTING_ID));
            assertEquals("20", settings.snapshotEncoded().get(
                    Minecraft189HurtTimeModule.METER_MAX_SETTING_ID));
            host.rectWidths.clear();
            runtime.renderHud(1L, 0.0F);
            assertEquals(2, host.rectWidths.size());
            assertEquals(84.0F, host.rectWidths.get(0), 0.0001F);
            assertEquals(21.0F, host.rectWidths.get(1), 0.0001F);
            assertEquals(Integer.valueOf(0xFF303D4A), host.rectColors.get(0));
            assertEquals(Integer.valueOf(0xFFFFB65C), host.rectColors.get(1));

            runtime.playerHurtTime(() -> 0);
            host.rectWidths.clear();
            host.rectColors.clear();
            runtime.renderHud(2L, 0.0F);
            assertEquals(1, host.rectWidths.size());
            assertEquals(84.0F, host.rectWidths.get(0), 0.0001F);
            runtime.playerHurtTime(() -> 30);
            host.rectWidths.clear();
            runtime.renderHud(3L, 0.0F);
            assertEquals(84.0F, host.rectWidths.get(1), 0.0001F);
            assertEquals(84.0F, Minecraft189HurtTimeModule.meterWidthFor(
                    Integer.MAX_VALUE, 20), 0.0001F);
            assertEquals(0.0F, Minecraft189HurtTimeModule.meterWidthFor(
                    -3, 20), 0.0001F);
            assertEquals(0.0F, Minecraft189HurtTimeModule.meterWidthFor(
                    3, 0), 0.0001F);

            hurt.showMeterSetting().set(Boolean.FALSE);
            host.rectWidths.clear();
            runtime.renderHud(4L, 0.0F);
            assertTrue(host.rectWidths.isEmpty());
            assertThrows(IllegalArgumentException.class,
                    () -> hurt.meterMaxSetting().set(0));
            assertThrows(IllegalArgumentException.class,
                    () -> hurt.meterMaxSetting().set(41));
            controller.disable(Minecraft189HurtTimeModule.ID);
            host.rectWidths.clear();
            runtime.renderHud(5L, 0.0F);
            assertTrue(host.rectWidths.isEmpty());
        } finally {
            runtime.close();
        }
        assertNull(settings.find(Minecraft189HurtTimeModule.SHOW_METER_SETTING_ID));
        assertNull(settings.find(Minecraft189HurtTimeModule.METER_MAX_SETTING_ID));
        assertNull(modules.find(Minecraft189HurtTimeModule.ID));
    }

    private static final class RecordingHost
            implements LegacyUiHostCallbacks {
        private String lastText;
        private float lastX;
        private float lastY;
        private final List<Float> rectWidths = new ArrayList<Float>();
        private final List<Integer> rectColors = new ArrayList<Integer>();

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
            rectWidths.add(width);
            rectColors.add(argb);
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
