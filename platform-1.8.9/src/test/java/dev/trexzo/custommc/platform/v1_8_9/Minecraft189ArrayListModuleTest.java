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
import dev.trexzo.custommc.platform.v1_8_9.ui.LegacyUiHostCallbacks;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
                            "40.0,50.0",
                            "40.0,62.0"),
                    host.positions);

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
                            "40.0,50.0"),
                    host.positions);
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
            assertEquals(Arrays.asList("8.0,8.0", "8.0,24.0", "8.0,36.0",
                    "8.0,48.0", "8.0,60.0", "8.0,72.0"), host.positions);
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
            assertEquals(Arrays.asList("CustomMC", "Watermark",
                    "Auto Sprint", "Fast Place", "Array List"), host.text);
        } finally {
            runtime.close();
        }
        assertNull(settings.find(Minecraft189ArrayListModule.SHOW_CATEGORIES_SETTING_ID));
        assertNull(settings.find(Minecraft189ArrayListModule.GROUP_CATEGORIES_SETTING_ID));
    }

    private static final class RecordingHost
            implements LegacyUiHostCallbacks {
        private final List<String> text =
                new ArrayList<String>();
        private final List<String> positions =
                new ArrayList<String>();

        private void clear() {
            text.clear();
            positions.clear();
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
