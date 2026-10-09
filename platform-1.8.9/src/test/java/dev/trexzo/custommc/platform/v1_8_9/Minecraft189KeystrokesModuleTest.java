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
import dev.trexzo.custommc.platform.v1_8_9.input.LegacyKeyboardCodes;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189KeystrokesModuleTest {
    @Test
    void keystrokesTracksRawInputBeforeConsumptionAndRendersLiveState() {
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

        final Minecraft189InputState inputState =
                runtime.inputState();

        try {
            runtime.featureCatalog()
                    .keystrokes()
                    .xSetting()
                    .set(100);
            runtime.featureCatalog()
                    .keystrokes()
                    .ySetting()
                    .set(200);
            controller.enable(
                    Minecraft189KeystrokesModule.ID);

            assertTrue(
                    runtime.key(
                            LegacyKeyboardCodes.RIGHT_SHIFT,
                            '\0',
                            true,
                            false,
                            true,
                            false,
                            false));
            assertTrue(
                    inputState.keyPressed(
                            LegacyKeyboardCodes.RIGHT_SHIFT));

            runtime.key(
                    LegacyKeyboardCodes.RIGHT_SHIFT,
                    '\0',
                    false,
                    false,
                    false,
                    false,
                    false);
            assertFalse(
                    inputState.keyPressed(
                            LegacyKeyboardCodes.RIGHT_SHIFT));
            assertTrue(
                    runtime.key(
                            LegacyKeyboardCodes.RIGHT_SHIFT,
                            '\0',
                            true,
                            false,
                            true,
                            false,
                            false));
            runtime.key(
                    LegacyKeyboardCodes.RIGHT_SHIFT,
                    '\0',
                    false,
                    false,
                    false,
                    false,
                    false);

            runtime.key(
                    LegacyKeyboardCodes.W,
                    'w',
                    true,
                    false,
                    false,
                    false,
                    false);
            runtime.pointerButton(
                    10,
                    10,
                    0,
                    true);

            assertTrue(
                    inputState.keyPressed(
                            LegacyKeyboardCodes.W));
            assertTrue(
                    inputState.pointerPressed(0));

            runtime.renderHud(
                    0L,
                    0.0F);

            assertEquals(
                    Arrays.asList(
                            "W",
                            "A",
                            "S",
                            "D",
                            "LMB",
                            "RMB"),
                    host.labels);
            assertEquals(
                    "122.0,200.0",
                    host.rectPositions.get(0));
            assertEquals(
                    "100.0,244.0",
                    host.rectPositions.get(4));
            assertEquals(
                    Integer.valueOf(0xD0FFFFFF),
                    host.rectColors.get(0));
            assertEquals(
                    Integer.valueOf(0xD0FFFFFF),
                    host.rectColors.get(4));
            assertEquals(
                    Integer.valueOf(0x90000000),
                    host.rectColors.get(1));

            host.clear();
            runtime.key(
                    LegacyKeyboardCodes.W,
                    'w',
                    false,
                    false,
                    false,
                    false,
                    false);
            runtime.pointerButton(
                    10,
                    10,
                    0,
                    false);

            assertFalse(
                    inputState.keyPressed(
                            LegacyKeyboardCodes.W));
            assertFalse(
                    inputState.pointerPressed(0));

            runtime.renderHud(
                    1L,
                    0.0F);

            assertEquals(
                    Integer.valueOf(0x90000000),
                    host.rectColors.get(0));
            assertEquals(
                    Integer.valueOf(0x90000000),
                    host.rectColors.get(4));
        } finally {
            runtime.close();
        }

        assertFalse(
                inputState.keyPressed(
                        LegacyKeyboardCodes.RIGHT_SHIFT));
        assertNull(
                modules.find(
                        Minecraft189KeystrokesModule.ID));
        assertNull(
                settings.find(
                        Minecraft189KeystrokesModule.X_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189KeystrokesModule.Y_SETTING_ID));
    }

    @Test
    void optionalSpaceAndShiftKeysRenderIndependentLiveRows() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final ServiceRegistry services = new ServiceRegistry();
        services.register(RenderPipeline.class, new RenderPipeline());
        final Minecraft189Platform platform = new Minecraft189Platform();
        platform.attach(new PlatformContext(new EventBus(),
                modules, controller, services));
        final RecordingHost host = new RecordingHost();
        final Minecraft189HostRuntime runtime = Minecraft189HostRuntime.install(
                platform, new ModulePresentationRegistry(),
                new ModuleCategoryRegistry(),
                new ModuleSettingRegistry(modules, settings),
                null, null, settings, new SettingPresentationRegistry(), host);
        try {
            final Minecraft189KeystrokesModule keys =
                    runtime.featureCatalog().keystrokes();
            assertFalse(keys.showSpaceSetting().get().booleanValue());
            assertFalse(keys.showShiftSetting().get().booleanValue());
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189KeystrokesModule.SHOW_SPACE_SETTING_ID));
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189KeystrokesModule.SHOW_SHIFT_SETTING_ID));
            keys.xSetting().set(100);
            keys.ySetting().set(200);
            controller.enable(Minecraft189KeystrokesModule.ID);
            runtime.renderHud(0L, 0.0F);
            assertEquals(Arrays.asList("W", "A", "S", "D", "LMB", "RMB"),
                    host.labels);

            keys.showSpaceSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189KeystrokesModule.SHOW_SPACE_SETTING_ID));
            runtime.inputState().key(LegacyKeyboardCodes.SPACE, true);
            host.clear();
            runtime.renderHud(1L, 0.0F);
            assertEquals(Arrays.asList(
                    "W", "A", "S", "D", "LMB", "RMB", "SPACE"),
                    host.labels);
            assertEquals("100.0,266.0", host.rectPositions.get(6));
            assertEquals(Integer.valueOf(0xD0FFFFFF), host.rectColors.get(6));

            keys.showShiftSetting().set(Boolean.TRUE);
            runtime.inputState().key(LegacyKeyboardCodes.RIGHT_SHIFT, true);
            host.clear();
            runtime.renderHud(2L, 0.0F);
            assertEquals(Arrays.asList(
                    "W", "A", "S", "D", "LMB", "RMB", "SPACE", "SHIFT"),
                    host.labels);
            assertEquals("100.0,288.0", host.rectPositions.get(7));
            assertEquals(Integer.valueOf(0xD0FFFFFF), host.rectColors.get(7));

            runtime.inputState().key(LegacyKeyboardCodes.RIGHT_SHIFT, false);
            runtime.inputState().key(LegacyKeyboardCodes.LEFT_SHIFT, true);
            host.clear();
            runtime.renderHud(3L, 0.0F);
            assertEquals(Integer.valueOf(0xD0FFFFFF), host.rectColors.get(7));
            runtime.inputState().key(LegacyKeyboardCodes.LEFT_SHIFT, false);
            runtime.inputState().key(LegacyKeyboardCodes.SPACE, false);
            host.clear();
            runtime.renderHud(4L, 0.0F);
            assertEquals(Integer.valueOf(0x90000000), host.rectColors.get(6));
            assertEquals(Integer.valueOf(0x90000000), host.rectColors.get(7));

            // SHIFT alone occupies the first extension row.
            keys.showSpaceSetting().set(Boolean.FALSE);
            host.clear();
            runtime.renderHud(5L, 0.0F);
            assertEquals(Arrays.asList(
                    "W", "A", "S", "D", "LMB", "RMB", "SHIFT"),
                    host.labels);
            assertEquals("100.0,266.0", host.rectPositions.get(6));
            keys.showShiftSetting().set(Boolean.FALSE);
            host.clear();
            runtime.renderHud(6L, 0.0F);
            assertEquals(6, host.labels.size());
            controller.disable(Minecraft189KeystrokesModule.ID);
            host.clear();
            runtime.renderHud(7L, 0.0F);
            assertTrue(host.labels.isEmpty());
        } finally {
            runtime.close();
        }
        assertNull(settings.find(Minecraft189KeystrokesModule.SHOW_SPACE_SETTING_ID));
        assertNull(settings.find(Minecraft189KeystrokesModule.SHOW_SHIFT_SETTING_ID));
        assertNull(modules.find(Minecraft189KeystrokesModule.ID));
    }

    private static final class RecordingHost
            implements LegacyUiHostCallbacks {
        private final List<String> labels =
                new ArrayList<String>();
        private final List<String> rectPositions =
                new ArrayList<String>();
        private final List<Integer> rectColors =
                new ArrayList<Integer>();

        private void clear() {
            labels.clear();
            rectPositions.clear();
            rectColors.clear();
        }

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
            rectPositions.add(
                    Float.toString(x)
                            + ","
                            + Float.toString(y));
            rectColors.add(argb);
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
            labels.add(text);
        }

        @Override
        public void endUi() {
        }
    }
}
