package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.platform.v1_8_9.ui.Minecraft189GuiSettingsAccess;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189FullbrightModuleTest {
    @Test
    void featureCloseRestoresExactPreEnableGamma() {
        final ModuleRegistry modules =
                new ModuleRegistry();
        final ModuleController controller =
                new ModuleController(modules);
        final ModulePresentationRegistry presentations =
                new ModulePresentationRegistry();
        final RecordingSettings settings =
                new RecordingSettings(0.42F);

        final Minecraft189FullbrightFeature feature =
                Minecraft189FullbrightFeature.install(
                        modules,
                        controller,
                        presentations,
                        settings);

        controller.enable(
                Minecraft189FullbrightModule.ID);

        assertTrue(
                feature.module()
                        .active());
        assertEquals(
                Minecraft189FullbrightModule.fullbrightGamma(),
                settings.gammaSetting());

        feature.close();

        assertEquals(
                0.42F,
                settings.gammaSetting());
        assertNull(
                modules.find(
                        Minecraft189FullbrightModule.ID));
        assertNull(
                presentations.find(
                        Minecraft189FullbrightModule.ID));

        feature.close();
        assertEquals(
                0.42F,
                settings.gammaSetting());
    }

    @Test
    void repeatedEnableDisableCapturesFreshUserGammaEachTime() {
        final RecordingSettings settings =
                new RecordingSettings(0.3F);
        final Minecraft189FullbrightModule module =
                new Minecraft189FullbrightModule(
                        settings);

        module.onEnable();
        assertEquals(
                Minecraft189FullbrightModule.fullbrightGamma(),
                settings.gammaSetting());
        module.onDisable();
        assertEquals(
                0.3F,
                settings.gammaSetting());
        assertFalse(module.active());

        settings.gammaSetting(
                0.8F);
        module.onEnable();
        assertEquals(
                0.8F,
                module.originalGamma());
        module.onDisable();
        assertEquals(
                0.8F,
                settings.gammaSetting());
    }

    private static final class RecordingSettings
            implements Minecraft189GuiSettingsAccess {
        private float fov = 70.0F;
        private float gamma;

        private RecordingSettings(
                final float gamma) {
            this.gamma = gamma;
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
