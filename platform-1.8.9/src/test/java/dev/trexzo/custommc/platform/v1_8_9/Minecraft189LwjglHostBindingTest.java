package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.bootstrap.BootstrapContext;
import dev.trexzo.custommc.platform.v1_8_9.ui.Minecraft189FontRendererAccess;
import dev.trexzo.custommc.platform.v1_8_9.ui.Minecraft189GuiSettingsAccess;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Minecraft189LwjglHostBindingTest {
    @Test
    void bindingInstallsOneHostIntoActiveBootstrapWithoutRendering() {
        final Minecraft189BootstrapRuntime runtime =
                Minecraft189BootstrapRuntime.create(
                        new BootstrapContext(
                                "net.minecraft.client.main.Main",
                                new String[0]));

        final Minecraft189HostRuntime host =
                Minecraft189LwjglHostBinding.install(
                        new FixedSettings(),
                        new NoOpFontAccess());

        assertTrue(
                Minecraft189RuntimeBridge.active());
        assertTrue(
                Minecraft189RuntimeBridge.hostInstalled());
        assertSame(
                runtime.platform(),
                host.platform());
        assertTrue(
                runtime.modules()
                        .find(
                                Minecraft189FullbrightModule.ID)
                        != null);
        assertTrue(
                runtime.modules()
                        .find(
                                Minecraft189FovModule.ID)
                        != null);

        runtime.close();

        assertFalse(
                Minecraft189RuntimeBridge.active());
        assertTrue(host.closed());
    }

    private static final class FixedSettings
            implements Minecraft189GuiSettingsAccess {
        private float fov = 70.0F;
        private float gamma = 0.5F;

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
            return 2;
        }

        @Override
        public boolean unicode() {
            return false;
        }
    }

    private static final class NoOpFontAccess
            implements Minecraft189FontRendererAccess {
        @Override
        public int drawString(
                final String text,
                final float x,
                final float y,
                final int argb,
                final boolean shadow) {
            return 0;
        }
    }
}
