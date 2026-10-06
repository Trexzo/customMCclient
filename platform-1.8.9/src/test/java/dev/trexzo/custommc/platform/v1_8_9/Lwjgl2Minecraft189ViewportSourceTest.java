package dev.trexzo.custommc.platform.v1_8_9;

import dev.trexzo.custommc.platform.v1_8_9.ui.Lwjgl2Minecraft189ViewportSource;
import dev.trexzo.custommc.platform.v1_8_9.ui.Minecraft189GuiSettingsAccess;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class Lwjgl2Minecraft189ViewportSourceTest {
    @Test
    void autoScaleHonorsVanillaMinimumLogicalDimensions() {
        final MutableSettings settings =
                new MutableSettings();
        final MutableDimensions dimensions =
                new MutableDimensions(
                        1920,
                        1080);
        final Lwjgl2Minecraft189ViewportSource source =
                source(
                        settings,
                        dimensions);

        settings.guiScale = 0;
        settings.unicode = false;

        assertEquals(
                1920,
                source.framebufferWidth());
        assertEquals(
                1080,
                source.framebufferHeight());
        assertEquals(
                4.0F,
                source.uiScale());

        dimensions.width = 640;
        dimensions.height = 480;

        assertEquals(
                2.0F,
                source.uiScale());

        dimensions.width = 300;
        dimensions.height = 200;

        assertEquals(
                1.0F,
                source.uiScale());
    }

    @Test
    void configuredScaleAndUnicodeCorrectionAreReadLive() {
        final MutableSettings settings =
                new MutableSettings();
        final MutableDimensions dimensions =
                new MutableDimensions(
                        1920,
                        1080);
        final Lwjgl2Minecraft189ViewportSource source =
                source(
                        settings,
                        dimensions);

        settings.guiScale = 3;
        settings.unicode = false;
        assertEquals(
                3.0F,
                source.uiScale());

        settings.unicode = true;
        assertEquals(
                2.0F,
                source.uiScale());

        settings.guiScale = 1;
        assertEquals(
                1.0F,
                source.uiScale());
    }

    @Test
    void invalidLiveStateIsRejectedInsteadOfProducingInvalidViewport() {
        final MutableSettings settings =
                new MutableSettings();
        final MutableDimensions dimensions =
                new MutableDimensions(
                        800,
                        600);
        final Lwjgl2Minecraft189ViewportSource source =
                source(
                        settings,
                        dimensions);

        settings.guiScale = -1;
        assertThrows(
                IllegalStateException.class,
                source::uiScale);

        settings.guiScale = 1;
        dimensions.width = 0;
        assertThrows(
                IllegalStateException.class,
                source::framebufferWidth);

        dimensions.width = 800;
        dimensions.height = -1;
        assertThrows(
                IllegalStateException.class,
                source::framebufferHeight);
    }

    private static Lwjgl2Minecraft189ViewportSource source(
            final MutableSettings settings,
            final MutableDimensions dimensions) {
        return new Lwjgl2Minecraft189ViewportSource(
                settings,
                () -> dimensions.width,
                () -> dimensions.height);
    }

    private static final class MutableSettings
            implements Minecraft189GuiSettingsAccess {
        private boolean viewBobbing = true;
        private float fov;
        private float gamma;
        private int guiScale;
        private boolean unicode;

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
            return guiScale;
        }

        @Override
        public boolean unicode() {
            return unicode;
        }
    }

    private static final class MutableDimensions {
        private int width;
        private int height;

        MutableDimensions(
                final int width,
                final int height) {
            this.width = width;
            this.height = height;
        }
    }
}
