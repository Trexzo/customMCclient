package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.core.module.ModuleController;
import dev.trexzo.custommc.core.module.ModulePresentationRegistry;
import dev.trexzo.custommc.core.module.ModuleRegistry;
import dev.trexzo.custommc.platform.v1_8_9.ui.Minecraft189GuiSettingsAccess;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189NoBobbingModuleTest {
    @Test
    void enableDisableRestoresExactPriorPreference() {
        final RecordingSettings settings =
                new RecordingSettings(true);
        final Minecraft189NoBobbingModule module =
                new Minecraft189NoBobbingModule(
                        settings);

        module.onEnable();
        assertTrue(module.active());
        assertTrue(module.originalViewBobbing());
        assertFalse(settings.viewBobbing());

        module.onDisable();
        assertTrue(settings.viewBobbing());
        assertFalse(module.active());

        settings.viewBobbing(false);
        module.onEnable();
        assertFalse(module.originalViewBobbing());
        module.onDisable();
        assertFalse(settings.viewBobbing());
    }

    @Test
    void featureCloseRestoresAndRemovesModule() {
        final RecordingSettings settings =
                new RecordingSettings(true);
        final ModuleRegistry modules =
                new ModuleRegistry();
        final ModuleController controller =
                new ModuleController(modules);
        final ModulePresentationRegistry presentations =
                new ModulePresentationRegistry();

        final Minecraft189NoBobbingFeature feature =
                Minecraft189NoBobbingFeature.install(
                        modules,
                        controller,
                        presentations,
                        settings);

        controller.enable(
                Minecraft189NoBobbingModule.ID);
        assertFalse(settings.viewBobbing());

        feature.close();

        assertTrue(settings.viewBobbing());
        assertNull(
                modules.find(
                        Minecraft189NoBobbingModule.ID));
        assertNull(
                presentations.find(
                        Minecraft189NoBobbingModule.ID));
    }

    @Test
    void userBobbingChangeIsPreservedWhenDisablingOverride() {
        final RecordingSettings settings = new RecordingSettings(false);
        final Minecraft189NoBobbingModule module =
                new Minecraft189NoBobbingModule(settings);
        module.onEnable();
        settings.viewBobbing(true);
        module.onDisable();
        assertTrue(settings.viewBobbing(),
                "a real user preference change must win over old capture");
        assertFalse(module.active());
    }

    @Test
    void failedOriginalSetterAfterMutationRollsBackBobbing() {
        final RecordingSettings settings = new RecordingSettings(true);
        final Minecraft189NoBobbingModule module =
                new Minecraft189NoBobbingModule(settings);
        settings.throwAfterBobbingWrite = true;
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class, module::onEnable);
        assertFalse(module.active());
        assertTrue(settings.viewBobbing(), "partial native setter must roll back");
        module.onEnable();
        assertFalse(settings.viewBobbing());
        module.onDisable();
        assertTrue(settings.viewBobbing());
    }

    private static final class RecordingSettings
            implements Minecraft189GuiSettingsAccess {
        private boolean viewBobbing;
        private boolean throwAfterBobbingWrite;
        private float fov = 70.0F;
        private float gamma = 0.5F;

        private RecordingSettings(
                final boolean viewBobbing) {
            this.viewBobbing = viewBobbing;
        }

        @Override
        public boolean viewBobbing() {
            return viewBobbing;
        }

        @Override
        public void viewBobbing(
                final boolean value) {
            viewBobbing = value;
            if (throwAfterBobbingWrite) {
                throwAfterBobbingWrite = false;
                throw new IllegalStateException("original bobbing setter failed");
            }
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
