package dev.trexzo.custommc.platform.v1_8_9.ui;

import org.lwjgl.opengl.Display;

import java.util.Objects;
import java.util.function.IntSupplier;

public final class Lwjgl2Minecraft189ViewportSource
        implements LegacyUiViewportSource {
    private static final int AUTO_GUI_SCALE = 1000;
    private static final int MIN_LOGICAL_WIDTH = 320;
    private static final int MIN_LOGICAL_HEIGHT = 240;

    private final Minecraft189GuiSettingsAccess settings;
    private final IntSupplier framebufferWidth;
    private final IntSupplier framebufferHeight;

    public Lwjgl2Minecraft189ViewportSource(
            final Minecraft189GuiSettingsAccess settings) {
        this(
                settings,
                Display::getWidth,
                Display::getHeight);
    }

    public Lwjgl2Minecraft189ViewportSource(
            final Minecraft189GuiSettingsAccess settings,
            final IntSupplier framebufferWidth,
            final IntSupplier framebufferHeight) {
        this.settings =
                Objects.requireNonNull(
                        settings,
                        "settings");
        this.framebufferWidth =
                Objects.requireNonNull(
                        framebufferWidth,
                        "framebufferWidth");
        this.framebufferHeight =
                Objects.requireNonNull(
                        framebufferHeight,
                        "framebufferHeight");
    }

    @Override
    public int framebufferWidth() {
        return requireDimension(
                framebufferWidth.getAsInt(),
                "framebufferWidth");
    }

    @Override
    public int framebufferHeight() {
        return requireDimension(
                framebufferHeight.getAsInt(),
                "framebufferHeight");
    }

    @Override
    public float uiScale() {
        final int width = framebufferWidth();
        final int height = framebufferHeight();

        int configured =
                settings.configuredGuiScale();
        if (configured < 0) {
            throw new IllegalStateException(
                    "configured GUI scale must be non-negative");
        }
        if (configured == 0) {
            configured = AUTO_GUI_SCALE;
        }

        int scale = 1;
        while (scale < configured
                && width / (scale + 1)
                >= MIN_LOGICAL_WIDTH
                && height / (scale + 1)
                >= MIN_LOGICAL_HEIGHT) {
            scale++;
        }

        if (settings.unicode()
                && scale % 2 != 0
                && scale != 1) {
            scale--;
        }

        return (float) scale;
    }

    private static int requireDimension(
            final int value,
            final String name) {
        if (value <= 0) {
            throw new IllegalStateException(
                    name + " must be positive");
        }
        return value;
    }
}
