package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.event.EventBus;
import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.core.module.ModuleSettingRegistry;
import dev.trexzo.custommc.core.setting.SettingPresentationRegistry;
import dev.trexzo.custommc.core.setting.SettingRegistry;
import dev.trexzo.custommc.platform.v1_8_9.ui.Minecraft189GuiSettingsAccess;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189FovModuleTest {
    @Test
    void liveTargetEditsApplyOnTickAndDisableRestoresOriginalFov() {
        final EventBus events =
                new EventBus();
        final RecordingSettings settings =
                new RecordingSettings(
                        70.0F);
        final Minecraft189FovModule module =
                new Minecraft189FovModule(
                        settings,
                        events);

        module.onEnable();
        assertTrue(module.active());
        assertEquals(
                70.0F,
                module.originalFov());
        assertEquals(
                110.0F,
                settings.fovSetting());

        module.targetFovSetting()
                .set(125);
        events.publish(
                new Minecraft189Hooks.TickEvent(
                        1L));
        assertEquals(
                125.0F,
                settings.fovSetting());

        module.onDisable();
        assertEquals(
                70.0F,
                settings.fovSetting());

        settings.fovSetting(
                82.0F);
        module.onEnable();
        module.onDisable();
        assertEquals(
                82.0F,
                settings.fovSetting());
    }

    @Test
    void featureCloseRestoresFovAndRemovesPersistentSetting() {
        final EventBus events =
                new EventBus();
        final RecordingSettings gameSettings =
                new RecordingSettings(
                        75.0F);
        final ModuleRegistry modules =
                new ModuleRegistry();
        final ModuleController controller =
                new ModuleController(
                        modules);
        final ModulePresentationRegistry presentations =
                new ModulePresentationRegistry();
        final SettingRegistry settings =
                new SettingRegistry();
        final SettingPresentationRegistry settingPresentations =
                new SettingPresentationRegistry();
        final ModuleSettingRegistry moduleSettings =
                new ModuleSettingRegistry(
                        modules,
                        settings);

        final Minecraft189FovFeature feature =
                Minecraft189FovFeature.install(
                        modules,
                        controller,
                        presentations,
                        moduleSettings,
                        settings,
                        settingPresentations,
                        gameSettings,
                        events);

        controller.enable(
                Minecraft189FovModule.ID);
        assertEquals(
                110.0F,
                gameSettings.fovSetting());

        feature.close();

        assertEquals(
                75.0F,
                gameSettings.fovSetting());
        assertNull(
                modules.find(
                        Minecraft189FovModule.ID));
        assertNull(
                settings.find(
                        Minecraft189FovModule.VALUE_SETTING_ID));
        assertNull(
                presentations.find(
                        Minecraft189FovModule.ID));
    }

    private static final class RecordingSettings
            implements Minecraft189GuiSettingsAccess {
        private boolean viewBobbing = true;
        private float fov;
        private float gamma = 0.5F;

        private RecordingSettings(
                final float fov) {
            this.fov = fov;
        }

        @Override
        public boolean viewBobbing() {
            return viewBobbing;
        }

        @Override
        public void viewBobbing(
                final boolean value) {
            viewBobbing = value;
        }

        @Override
        public float fovSetting() {
            return fov;
        }

        @Override
        public void fovSetting(
                final float value) {
            fov = value;
        }

        @Override
        public float gammaSetting() {
            return gamma;
        }

        @Override
        public void gammaSetting(
                final float value) {
            gamma = value;
        }

        @Override
        public int configuredGuiScale() {
            return 1;
        }

        @Override
        public boolean unicode() {
            return false;
        }
    }
}
