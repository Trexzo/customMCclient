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

final class Minecraft189ExperienceModuleTest {
    @Test
    void experienceHudReadsLiveValuesAndPersistsPosition() {
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
            final Minecraft189ExperienceModule experience =
                    runtime.featureCatalog()
                            .experience();

            assertEquals(
                    ModuleState.DISABLED,
                    controller.stateOf(
                            Minecraft189ExperienceModule.ID));

            experience.xSetting().set(64);
            experience.ySetting().set(292);
            runtime.playerExperience(
                    new Minecraft189PlayerExperienceAccess() {
                        @Override
                        public int customMcExperienceLevel() {
                            return 27;
                        }

                        @Override
                        public int customMcExperienceTotal() {
                            return 12345;
                        }

                        @Override
                        public float customMcExperienceProgress() {
                            return 0.5F;
                        }

                        @Override
                        public int customMcExperienceBarCap() {
                            return 42;
                        }
                    });

            controller.enable(
                    Minecraft189ExperienceModule.ID);
            assertTrue(
                    experience.renderPassInstalled());

            runtime.renderHud(
                    0L,
                    0.0F);

            assertEquals(
                    "XP: Lv 27 | 21/42 | Total 12345",
                    host.lastText);
            assertEquals(
                    64.0F,
                    host.lastX);
            assertEquals(
                    292.0F,
                    host.lastY);
            assertEquals(
                    "64",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189ExperienceModule.X_SETTING_ID));
            assertEquals(
                    "292",
                    settings.snapshotEncoded()
                            .get(
                                    Minecraft189ExperienceModule.Y_SETTING_ID));

            final Minecraft189PlayerExperienceState.Snapshot snapshot =
                    runtime.playerExperienceState()
                            .snapshot();
            assertTrue(
                    snapshot.available());
            assertEquals(
                    27,
                    snapshot.level());
            assertEquals(
                    12345,
                    snapshot.total());
            assertEquals(
                    21,
                    snapshot.progressPoints());

            host.lastText = null;
            runtime.playerExperience(null);
            runtime.renderHud(
                    1L,
                    0.0F);
            assertNull(
                    host.lastText);

            controller.disable(
                    Minecraft189ExperienceModule.ID);
            assertFalse(
                    experience.renderPassInstalled());
        } finally {
            runtime.close();
        }

        assertNull(
                modules.find(
                        Minecraft189ExperienceModule.ID));
        assertNull(
                settings.find(
                        Minecraft189ExperienceModule.X_SETTING_ID));
        assertNull(
                settings.find(
                        Minecraft189ExperienceModule.Y_SETTING_ID));
    }

    @Test
    void experienceStateRejectsInvalidValues() {
        final Minecraft189PlayerExperienceState state =
                new Minecraft189PlayerExperienceState();

        assertThrows(
                IllegalArgumentException.class,
                () -> state.update(
                        -1,
                        0,
                        0.0F,
                        7));
        assertThrows(
                IllegalArgumentException.class,
                () -> state.update(
                        0,
                        -1,
                        0.0F,
                        7));
        assertThrows(
                IllegalArgumentException.class,
                () -> state.update(
                        0,
                        0,
                        Float.NaN,
                        7));
        assertThrows(
                IllegalArgumentException.class,
                () -> state.update(
                        0,
                        0,
                        1.1F,
                        7));
        assertThrows(
                IllegalArgumentException.class,
                () -> state.update(
                        0,
                        0,
                        0.5F,
                        0));
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
