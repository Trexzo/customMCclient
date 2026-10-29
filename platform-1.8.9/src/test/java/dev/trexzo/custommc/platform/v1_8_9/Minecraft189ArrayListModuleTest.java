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
import dev.trexzo.custommc.core.ui.UiThemeSettings;
import dev.trexzo.custommc.platform.PlatformContext;
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189ArrayListModuleTest {
    @Test
    void arrayListTracksEnabledModulesAndConfiguredPosition() {
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
            assertEquals(Boolean.TRUE, runtime.featureCatalog().arrayList()
                    .rightAlignedSetting().get());
            runtime.featureCatalog()
                    .arrayList()
                    .xSetting()
                    .set(40);
            runtime.featureCatalog()
                    .arrayList()
                    .ySetting()
                    .set(50);

            controller.enable(
                    Minecraft189WatermarkModule.ID);
            controller.enable(
                    Minecraft189ArrayListModule.ID);

            runtime.renderHud(
                    0L,
                    0.0F);

            assertEquals(
                    Arrays.asList(
                            "CustomMC",
                            "Watermark",
                            "Array List"),
                    host.text);
            assertEquals(
                    Arrays.asList(
                            "8.0,8.0",
                            "1184.0,50.0",
                            "1184.0,62.0"),
                    host.positions);

            assertEquals(0xFFEE8755, host.lastAccentColor);
            assertEquals(Boolean.FALSE,
                    runtime.featureCatalog().arrayList().showBackgroundSetting().get());
            assertEquals(0, host.roundedRects);
            runtime.featureCatalog().arrayList().showBackgroundSetting()
                    .set(Boolean.TRUE);
            host.clear();
            runtime.renderHud(9L, 0.0F);
            assertEquals(2, host.roundedRects);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189ArrayListModule.SHOW_BACKGROUND_SETTING_ID));
            runtime.featureCatalog().arrayList().showBackgroundSetting()
                    .set(Boolean.FALSE);
            host.clear();
            assertTrue(UiThemeSettings.cycle(
                    settings.find(UiThemeSettings.ID), true));
            host.clear();
            runtime.renderHud(4L, 0.0F);
            assertEquals(0xFF5994EA, host.lastAccentColor);
            assertEquals("blue", settings.snapshotEncoded()
                    .get(UiThemeSettings.ID));
            assertTrue(UiThemeSettings.cycle(
                    settings.find(UiThemeSettings.ID), false));

            host.clear();
            controller.disable(
                    Minecraft189WatermarkModule.ID);

            runtime.renderHud(
                    1L,
                    0.0F);

            assertEquals(
                    Arrays.asList(
                            "Array List"),
                    host.text);
            assertEquals(
                    Arrays.asList(
                            "1184.0,50.0"),
                    host.positions);
            host.clear();
            // Persisted opt-out keeps exact legacy left-X behavior.
            runtime.featureCatalog().arrayList().rightAlignedSetting()
                    .set(Boolean.FALSE);
            runtime.renderHud(2L, 0.0F);
            assertEquals(Arrays.asList("40.0,50.0"), host.positions);
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189ArrayListModule.RIGHT_ALIGNED_SETTING_ID));
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189ArrayListModule.ID));
        assertNull(
                settings.find(
                        Minecraft189ArrayListModule.X_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189ArrayListModule.Y_SETTING_ID));
        assertNull(settings.find(Minecraft189ArrayListModule.SHOW_CATEGORIES_SETTING_ID));
        assertNull(settings.find(Minecraft189ArrayListModule.GROUP_CATEGORIES_SETTING_ID));
    }

    @Test
    void nativeHudSlidesNewModuleWithoutShowingDisabledGhostRows() {
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
            final Minecraft189ArrayListModule list =
                    runtime.featureCatalog().arrayList();
            assertEquals(Boolean.TRUE, list.animateSetting().get());
            controller.enable(Minecraft189WatermarkModule.ID);
            controller.enable(Minecraft189ArrayListModule.ID);
            runtime.renderHud(0L, 0.0F);
            controller.enable(Minecraft189AutoSprintModule.ID);
            host.clear();
            runtime.renderHud(1L, 0.0F);
            final int entering = host.text.indexOf("Auto Sprint");
            assertTrue(entering >= 0);
            final float enteringX = Float.parseFloat(
                    host.positions.get(entering).split(",")[0]);
            assertTrue(enteringX > Minecraft189ArrayListModule.rightAlignedTextX(
                    1280.0F, 8.0F, "Auto Sprint"));
            assertTrue((host.textColors.get(entering) >>> 24) < 255);
            for (int frame = 2; frame <= 5; frame++) {
                host.clear();
                runtime.renderHud(frame, 0.0F);
            }
            final int settled = host.text.indexOf("Auto Sprint");
            assertEquals(0xFFF2F4F6,
                    host.textColors.get(settled).intValue());
            assertEquals(Minecraft189ArrayListModule.rightAlignedTextX(
                    1280.0F, 8.0F, "Auto Sprint"),
                    Float.parseFloat(
                            host.positions.get(settled).split(",")[0]), 0.001F);

            controller.disable(Minecraft189AutoSprintModule.ID);
            host.clear();
            runtime.renderHud(6L, 0.0F);
            assertFalse(host.text.contains("Auto Sprint"));

            list.animateSetting().set(Boolean.FALSE);
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189ArrayListModule.ANIMATE_SETTING_ID));
            controller.enable(Minecraft189AutoSprintModule.ID);
            host.clear();
            runtime.renderHud(7L, 0.0F);
            assertEquals(0xFFF2F4F6, host.textColors.get(
                    host.text.indexOf("Auto Sprint")).intValue());
        } finally {
            runtime.close();
        }
        assertNull(settings.find(Minecraft189ArrayListModule.ANIMATE_SETTING_ID));
    }

    @Test
    void categoryLabelsCoverAllFourSupportedCategoriesAndFallback() {
        assertEquals("[Other] unmapped.module",
                Minecraft189ArrayListModule.moduleLabel("unmapped.module", null, true));
        assertEquals("unmapped.module",
                Minecraft189ArrayListModule.moduleLabel("unmapped.module", null, false));
        assertEquals(4, Minecraft189ArrayListModule.categoryOrder(null));
        assertEquals("[Combat] No Hit Delay",
                Minecraft189ArrayListModule.moduleLabel(
                        Minecraft189NoHitDelayModule.ID,
                        new dev.trexzo.custommc.core.module.ModuleDescriptor(
                                Minecraft189NoHitDelayModule.ID, "No Hit Delay", "",
                                Minecraft189FeatureCatalog.COMBAT_CATEGORY_ID, 0), true));
        assertEquals("[Movement] Auto Sprint",
                Minecraft189ArrayListModule.moduleLabel(
                        Minecraft189AutoSprintModule.ID,
                        new dev.trexzo.custommc.core.module.ModuleDescriptor(
                                Minecraft189AutoSprintModule.ID, "Auto Sprint", "",
                                Minecraft189FeatureCatalog.MOVEMENT_CATEGORY_ID, 0), true));
        assertEquals("[Player] Fast Place",
                Minecraft189ArrayListModule.moduleLabel(
                        Minecraft189FastPlaceModule.ID,
                        new dev.trexzo.custommc.core.module.ModuleDescriptor(
                                Minecraft189FastPlaceModule.ID, "Fast Place", "",
                                Minecraft189FeatureCatalog.PLAYER_CATEGORY_ID, 0), true));
        assertEquals("[Visuals] Array List",
                Minecraft189ArrayListModule.moduleLabel(
                        Minecraft189ArrayListModule.ID,
                        new dev.trexzo.custommc.core.module.ModuleDescriptor(
                                Minecraft189ArrayListModule.ID, "Array List", "",
                                Minecraft189FeatureCatalog.VISUALS_CATEGORY_ID, 0), true));
    }

    @Test
    void categoryModesApplyToCombatMovementPlayerAndVisualModules() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final ServiceRegistry services = new ServiceRegistry();
        services.register(RenderPipeline.class, new RenderPipeline());
        final Minecraft189Platform platform = new Minecraft189Platform();
        platform.attach(new PlatformContext(new EventBus(), modules,
                controller, services));
        final RecordingHost host = new RecordingHost();
        final Minecraft189HostRuntime runtime =
                Minecraft189HostRuntime.install(platform,
                        new ModulePresentationRegistry(), new ModuleCategoryRegistry(),
                        new ModuleSettingRegistry(modules, settings),
                        null, null, settings, new SettingPresentationRegistry(), host);
        try {
            final Minecraft189ArrayListModule arrayList = runtime.featureCatalog().arrayList();
            assertEquals(Boolean.FALSE, arrayList.showCategoriesSetting().get());
            assertEquals(Boolean.FALSE, arrayList.groupCategoriesSetting().get());
            controller.enable(Minecraft189WatermarkModule.ID);
            controller.enable(Minecraft189ArrayListModule.ID);
            controller.enable(Minecraft189AutoSprintModule.ID);
            controller.enable(Minecraft189FastPlaceModule.ID);
            controller.enable(Minecraft189NoHitDelayModule.ID);
            runtime.renderHud(100L, 0.0F);
            assertEquals(6, host.text.size());
            assertEquals("CustomMC", host.text.get(0));
            assertEquals(1, java.util.Collections.frequency(host.text, "Auto Sprint"));
            assertEquals(1, java.util.Collections.frequency(host.text, "Fast Place"));
            assertEquals(1, java.util.Collections.frequency(host.text, "No Hit Delay"));
            host.clear();

            arrayList.showCategoriesSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189ArrayListModule.SHOW_CATEGORIES_SETTING_ID));
            runtime.renderHud(101L, 0.0F);
            assertEquals(6, host.text.size());
            assertEquals(1, java.util.Collections.frequency(host.text,
                    "[Combat] No Hit Delay"));
            assertEquals(1, java.util.Collections.frequency(host.text,
                    "[Movement] Auto Sprint"));
            assertEquals(1, java.util.Collections.frequency(host.text,
                    "[Player] Fast Place"));
            assertEquals(1, java.util.Collections.frequency(host.text,
                    "[Visuals] Watermark"));
            assertEquals(1, java.util.Collections.frequency(host.text,
                    "[Visuals] Array List"));
            host.clear();

            arrayList.groupCategoriesSetting().set(Boolean.TRUE);
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189ArrayListModule.GROUP_CATEGORIES_SETTING_ID));
            runtime.renderHud(102L, 0.0F);
            assertEquals(Arrays.asList(
                    "CustomMC", "[Combat] No Hit Delay", "[Movement] Auto Sprint",
                    "[Player] Fast Place", "[Visuals] Watermark",
                    "[Visuals] Array List"), host.text);
            assertEquals(Arrays.asList("8.0,8.0", "1160.0,24.0", "1152.0,36.0",
                    "1170.0,48.0", "1170.0,60.0", "1170.0,72.0"), host.positions);
            host.clear();

            // Grouping works with labels off and reflects live enable states.
            arrayList.showCategoriesSetting().set(Boolean.FALSE);
            controller.disable(Minecraft189NoHitDelayModule.ID);
            runtime.renderHud(103L, 0.0F);
            assertEquals(Arrays.asList("CustomMC", "Auto Sprint",
                    "Fast Place", "Watermark", "Array List"), host.text);
            host.clear();
            arrayList.groupCategoriesSetting().set(Boolean.FALSE);
            runtime.renderHud(104L, 0.0F);
            assertEquals(Arrays.asList("CustomMC", "Auto Sprint",
                    "Fast Place", "Watermark", "Array List"), host.text);
        } finally {
            runtime.close();
        }
        assertNull(settings.find(Minecraft189ArrayListModule.SHOW_CATEGORIES_SETTING_ID));
        assertNull(settings.find(Minecraft189ArrayListModule.GROUP_CATEGORIES_SETTING_ID));
    }

    @Test
    void realPlatformHudStackRecoversOn854x480WithoutFakeEnabledRows() {
        final ModuleRegistry modules = new ModuleRegistry();
        final ModuleController controller = new ModuleController(modules);
        final SettingRegistry settings = new SettingRegistry();
        final ServiceRegistry services = new ServiceRegistry();
        services.register(RenderPipeline.class, new RenderPipeline());
        final Minecraft189Platform platform = new Minecraft189Platform();
        platform.attach(new PlatformContext(
                new EventBus(), modules, controller, services));
        final RecordingHost host = new RecordingHost();
        host.windowWidth = 854;
        host.windowHeight = 480;
        final Minecraft189HostRuntime runtime =
                Minecraft189HostRuntime.install(
                        platform, new ModulePresentationRegistry(),
                        new ModuleCategoryRegistry(),
                        new ModuleSettingRegistry(modules, settings),
                        null, null, settings,
                        new SettingPresentationRegistry(), host);
        try {
            final Minecraft189ArrayListModule list = runtime.featureCatalog().arrayList();
            controller.enable(Minecraft189WatermarkModule.ID);
            controller.enable(Minecraft189ArrayListModule.ID);
            controller.enable(Minecraft189AutoSprintModule.ID);
            controller.enable(Minecraft189FastPlaceModule.ID);
            controller.enable(Minecraft189NoHitDelayModule.ID);

            // A saved coordinate from a larger window must not hide the
            // enabled-module list after shrinking to actual 854x480.
            list.ySetting().set(4096);
            list.xSetting().set(4096);
            list.showOverflowSetting().set(Boolean.TRUE);
            runtime.renderHud(12L, 0.0F);
            assertEquals(Arrays.asList("CustomMC", "+ 5 more"), host.text);
            final float lastX = Float.parseFloat(
                    host.positions.get(1).split(",")[0]);
            final float lastY = Float.parseFloat(
                    host.positions.get(1).split(",")[1]);
            assertTrue(lastX >= 4.0F && lastX <= 846.0F);
            assertEquals(464.0F, lastY, 0.001F);
            assertEquals("4096", settings.snapshotEncoded().get(
                    Minecraft189ArrayListModule.X_SETTING_ID));
            assertEquals("4096", settings.snapshotEncoded().get(
                    Minecraft189ArrayListModule.Y_SETTING_ID));

            // Restore ordinary placement: all real enabled modules return.
            list.ySetting().set(24);
            list.xSetting().set(8);
            host.clear();
            runtime.renderHud(13L, 0.0F);
            assertEquals(6, host.text.size());
            assertFalse(host.text.contains("+ 5 more"));
        } finally {
            runtime.close();
        }
    }

    @Test
    void maxRowsAndOverflowOnlyShowActualEnabledModules() {
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
                new ModuleCategoryRegistry(),
                new ModuleSettingRegistry(modules, settings),
                null, null, settings, new SettingPresentationRegistry(),
                host);
        try {
            final Minecraft189ArrayListModule list = runtime.featureCatalog().arrayList();
            assertEquals(Integer.valueOf(128), list.maxVisibleSetting().get());
            assertEquals(Boolean.FALSE, list.showOverflowSetting().get());
            assertEquals("128", settings.snapshotEncoded().get(
                    Minecraft189ArrayListModule.MAX_VISIBLE_SETTING_ID));
            assertEquals("false", settings.snapshotEncoded().get(
                    Minecraft189ArrayListModule.SHOW_OVERFLOW_SETTING_ID));
            controller.enable(Minecraft189WatermarkModule.ID);
            controller.enable(Minecraft189ArrayListModule.ID);
            controller.enable(Minecraft189AutoSprintModule.ID);
            controller.enable(Minecraft189FastPlaceModule.ID);
            controller.enable(Minecraft189NoHitDelayModule.ID);
            runtime.renderHud(0L, 0.0F);
            assertEquals(6, host.text.size()); // Default full list.
            host.clear();

            list.maxVisibleSetting().set(2);
            runtime.renderHud(1L, 0.0F);
            assertEquals(Arrays.asList("CustomMC", "No Hit Delay", "Auto Sprint"),
                    host.text);
            host.clear();
            list.showOverflowSetting().set(Boolean.TRUE);
            assertEquals("2", settings.snapshotEncoded().get(
                    Minecraft189ArrayListModule.MAX_VISIBLE_SETTING_ID));
            assertEquals("true", settings.snapshotEncoded().get(
                    Minecraft189ArrayListModule.SHOW_OVERFLOW_SETTING_ID));
            runtime.renderHud(2L, 0.0F);
            assertEquals(Arrays.asList(
                    "CustomMC", "No Hit Delay", "Auto Sprint", "+ 3 more"), host.text);
            assertEquals("1222.0,48.0", host.positions.get(3));
            host.clear();

            list.groupCategoriesSetting().set(Boolean.TRUE);
            list.showCategoriesSetting().set(Boolean.TRUE);
            runtime.renderHud(3L, 0.0F);
            assertEquals(Arrays.asList("CustomMC",
                    "[Combat] No Hit Delay", "[Movement] Auto Sprint",
                    "+ 3 more"), host.text);
            host.clear();
            list.maxVisibleSetting().set(1);
            controller.disable(Minecraft189NoHitDelayModule.ID);
            runtime.renderHud(4L, 0.0F);
            assertEquals(Arrays.asList(
                    "CustomMC", "[Movement] Auto Sprint", "+ 3 more"), host.text);
            host.clear();
            list.maxVisibleSetting().set(128);
            runtime.renderHud(5L, 0.0F);
            assertEquals(5, host.text.size());
            assertEquals(0, java.util.Collections.frequency(
                    host.text, "+ 3 more"));

            assertThrows(IllegalArgumentException.class,
                    () -> list.maxVisibleSetting().set(0));
            assertThrows(IllegalArgumentException.class,
                    () -> list.maxVisibleSetting().set(129));
            controller.disable(Minecraft189ArrayListModule.ID);
            host.clear();
            runtime.renderHud(6L, 0.0F);
            assertEquals(Arrays.asList("CustomMC"), host.text);
        } finally {
            runtime.close();
        }
        assertNull(settings.find(Minecraft189ArrayListModule.MAX_VISIBLE_SETTING_ID));
        assertNull(settings.find(Minecraft189ArrayListModule.SHOW_OVERFLOW_SETTING_ID));
        assertNull(settings.find(Minecraft189ArrayListModule.RIGHT_ALIGNED_SETTING_ID));
        assertNull(settings.find(Minecraft189ArrayListModule.SHOW_BACKGROUND_SETTING_ID));
        assertNull(modules.find(Minecraft189ArrayListModule.ID));
    }

    private static final class RecordingHost
            implements LegacyUiHostCallbacks {
        private final List<String> text =
                new ArrayList<String>();
        private final List<String> positions =
                new ArrayList<String>();
        private final List<Integer> textColors = new ArrayList<Integer>();
        private int lastAccentColor;
        private int roundedRects;
        private int windowWidth = 1280;
        private int windowHeight = 720;

        private void clear() {
            text.clear();
            positions.clear();
            textColors.clear();
            roundedRects = 0;
        }

        @Override
        public int framebufferWidth() {
            return windowWidth;
        }

        @Override
        public int framebufferHeight() {
            return windowHeight;
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
            if (width == 2.0F && height == 9.0F) {
                lastAccentColor = argb;
            }
        }

        @Override
        public void fillRoundedRect(
                final float x,
                final float y,
                final float width,
                final float height,
                final float radius,
                final int argb) {
            roundedRects++;
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
                final String value,
                final int argb) {
            text.add(value);
            textColors.add(Integer.valueOf(argb));
            positions.add(
                    Float.toString(x)
                            + ","
                            + Float.toString(y));
        }

        @Override
        public void endUi() {
        }
    }
}
