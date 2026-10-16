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

    @Test
    void externalGammaChangeWinsOverOldModuleSetting() {
        final RecordingSettings settings = new RecordingSettings(0.35F);
        final Minecraft189FullbrightModule module =
                new Minecraft189FullbrightModule(settings);
        module.onEnable();
        settings.gammaSetting(1.25F); // user changes slider while enabled
        module.onDisable();
        assertEquals(1.25F, settings.gammaSetting(),
                "disabling must not overwrite latest user gamma");
        assertFalse(module.active());
    }

    @Test
    void setterThrowsAfterMutationAndOriginalGammaIsRestored() {
        final RecordingSettings settings = new RecordingSettings(0.45F);
        final Minecraft189FullbrightModule module =
                new Minecraft189FullbrightModule(settings);
        settings.throwAfterGammaWrite = true;
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class, module::onEnable);
        assertFalse(module.active(),
                "failed native enable must never appear successfully enabled");
        assertEquals(0.45F, settings.gammaSetting(),
                "partial native gamma write must roll back");
        module.onEnable();
        assertEquals(Minecraft189FullbrightModule.fullbrightGamma(),
                settings.gammaSetting());
        module.onDisable();
        assertEquals(0.45F, settings.gammaSetting());
    }

    private static final class RecordingSettings
            implements Minecraft189GuiSettingsAccess {
        private boolean viewBobbing = true;
        private float fov = 70.0F;
        private float gamma;
        private boolean throwAfterGammaWrite;

        private RecordingSettings(
                final float gamma) {
            this.gamma = gamma;
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
            if (throwAfterGammaWrite) {
                throwAfterGammaWrite = false;
                throw new IllegalStateException("original settings gamma setter failed");
            }
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
